param(
    [string]$MinecraftDir = "",
    [string]$ModUrl = "",
    [string[]]$ResourcePackUrl = @(),
    [string]$ShaderPackUrl = "",
    [string]$NeoForgeInstallerUrl = "",
    [switch]$NoGui
)

$ErrorActionPreference = "Stop"

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot "build.gradle"))) {
    $ProjectRoot = $ScriptRoot
}
$BannerPath = Join-Path $ProjectRoot "screenshots\banner_installer.png"
if (-not (Test-Path -LiteralPath $BannerPath)) {
    $BannerPath = Join-Path $ScriptRoot "screenshots\banner_installer.png"
}
$LogRoot = Join-Path $ProjectRoot "tmp"
if (-not (Test-Path -LiteralPath (Split-Path -Parent $LogRoot))) {
    $LogRoot = Join-Path $env:TEMP "magicworld-installer"
}
$LogPath = Join-Path $LogRoot "magicworld-installer.log"
$NeoForgeVersion = "26.1.2.65-beta"
$MinecraftVersion = "26.1.2"
$GitHubReleaseBaseUrl = "https://github.com/gorpo/Magic-World-Ultimate-Neoforge-26.1.2/releases/download/v1.3"
$DefaultModUrl = "$GitHubReleaseBaseUrl/MagicWorld-Ultimate-NeoForge-26.1.2-1.2.0-all-in-one.jar"
$DefaultResourcePackUrls = @(
    "$GitHubReleaseBaseUrl/MagicWorldResource_1.20.1-256x.zip",
    "$GitHubReleaseBaseUrl/MagicWorldResource_1.20.1-models.zip",
    "$GitHubReleaseBaseUrl/MagicWorldResource_1.20.1-addon.zip",
    "$GitHubReleaseBaseUrl/MagicWorldResource_1.20.1-bonus.zip"
)
$DefaultShaderPackUrl = "$GitHubReleaseBaseUrl/MagicWorld-Ultimate-NeoForge-26.1.2-ShaderPack.zip"
$DefaultNeoForgeInstallerUrl = "https://maven.neoforged.net/releases/net/neoforged/neoforge/$NeoForgeVersion/neoforge-$NeoForgeVersion-installer.jar"

function Write-InstallLog {
    param([string]$Message)
    $line = "[{0}] {1}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $Message
    Add-Content -LiteralPath $LogPath -Value $line -Encoding UTF8
    Write-Output $line
}

function Find-MinecraftDir {
    if ($MinecraftDir -and (Test-Path -LiteralPath $MinecraftDir)) {
        return (Resolve-Path -LiteralPath $MinecraftDir).Path
    }

    $candidates = @(
        (Join-Path $env:APPDATA ".minecraft"),
        (Join-Path $env:APPDATA "TLauncher\.minecraft"),
        (Join-Path $env:APPDATA ".tlauncher\.minecraft"),
        (Join-Path $env:USERPROFILE "AppData\Roaming\.minecraft")
    ) | Select-Object -Unique

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }

    $defaultPath = Join-Path $env:APPDATA ".minecraft"
    New-Item -ItemType Directory -Path $defaultPath -Force | Out-Null
    return (Resolve-Path -LiteralPath $defaultPath).Path
}

function Get-LatestFile {
    param(
        [string[]]$SearchRoots,
        [string]$Filter
    )

    foreach ($root in $SearchRoots) {
        if (-not (Test-Path -LiteralPath $root)) {
            continue
        }
        $file = Get-ChildItem -LiteralPath $root -File -Filter $Filter -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
        if ($file) {
            return $file.FullName
        }
    }

    return $null
}

function Copy-Or-Download {
    param(
        [string]$Url,
        [string]$LocalPath,
        [string]$DestinationDir,
        [string]$Name
    )

    New-Item -ItemType Directory -Path $DestinationDir -Force | Out-Null

    if ($Url) {
        $fileName = Split-Path -Leaf ([uri]$Url).LocalPath
        if (-not $fileName) {
            $fileName = $Name
        }
        $destination = Join-Path $DestinationDir $fileName
        Write-InstallLog "Baixando $Name de $Url"
        try {
            Invoke-WebRequest -Uri $Url -OutFile $destination
            return $destination
        } catch {
            Write-InstallLog "Falha ao baixar ${Name}: $($_.Exception.Message)"
            if (-not $LocalPath -or -not (Test-Path -LiteralPath $LocalPath)) {
                throw
            }
            Write-InstallLog "Usando arquivo local de fallback para $Name."
        }
    }

    if (-not $LocalPath -or -not (Test-Path -LiteralPath $LocalPath)) {
        Write-InstallLog "Arquivo nao encontrado para $Name. Pulei esta etapa."
        return $null
    }

    $destination = Join-Path $DestinationDir (Split-Path -Leaf $LocalPath)
    Write-InstallLog "Copiando $Name para $destination"
    Copy-Item -LiteralPath $LocalPath -Destination $destination -Force
    return $destination
}

function Write-InstallerOverview {
    param([System.Windows.Forms.TextBox]$Output)

    $text = @"
O que sera instalado:
- NeoForge $NeoForgeVersion para Minecraft $MinecraftVersion.
- Magic World all-in-one em .minecraft\mods.
- Resource Packs Magic World em .minecraft\resourcepacks.
- Shader Pack Magic World em .minecraft\shaderpacks.

Antes de continuar:
- Tenha o TLauncher ja instalado e aberto pelo menos uma vez.
- Confirme se a pasta abaixo e a .minecraft usada pelo TLauncher.
- O instalador baixa os arquivos dos Releases do GitHub quando possivel.
- Se um arquivo de Release ainda nao estiver publicado, ele tenta usar o arquivo local mais recente deste projeto.

Depois da instalacao:
- Abra o TLauncher.
- Selecione ou crie um perfil NeoForge $MinecraftVersion.
- Inicie o jogo e ative os Resource Packs e o Shader Pack se desejar.

"@
    $Output.AppendText($text.Replace("`n", "`r`n"))
}

function Install-MagicWorld {
    $mcDir = Find-MinecraftDir
    Write-InstallLog "Pasta Minecraft detectada: $mcDir"

    if (-not $ModUrl) {
        $script:ModUrl = $DefaultModUrl
    }
    if (-not $ResourcePackUrl -or $ResourcePackUrl.Count -eq 0) {
        $script:ResourcePackUrl = $DefaultResourcePackUrls
    }
    if (-not $ShaderPackUrl) {
        $script:ShaderPackUrl = $DefaultShaderPackUrl
    }
    $modsDir = Join-Path $mcDir "mods"
    $resourcepacksDir = Join-Path $mcDir "resourcepacks"
    $shaderpacksDir = Join-Path $mcDir "shaderpacks"
    $versionsDir = Join-Path $mcDir "versions"

    New-Item -ItemType Directory -Path $modsDir, $resourcepacksDir, $shaderpacksDir, $versionsDir -Force | Out-Null

    $localMod = Get-LatestFile -SearchRoots @(
        $ScriptRoot,
        (Join-Path $ProjectRoot "run\mods"),
        (Join-Path $ProjectRoot "build\libs")
    ) -Filter "*all-in-one.jar"

    $localResourcePack = Get-LatestFile -SearchRoots @(
        $ScriptRoot,
        (Join-Path $ProjectRoot "run\resourcepacks"),
        (Join-Path $ProjectRoot "build\tmp")
    ) -Filter "MagicWorldResource*.zip"

    $localShaderPack = Get-LatestFile -SearchRoots @(
        $ScriptRoot,
        (Join-Path $ProjectRoot "run\shaderpacks")
    ) -Filter "MagicWorld*Shader*.zip"

    $localNeoForgeInstaller = Get-LatestFile -SearchRoots @($ScriptRoot, $ProjectRoot) -Filter "neoforge-$NeoForgeVersion-installer.jar"
    if (-not $localNeoForgeInstaller) {
        $localNeoForgeInstaller = Get-LatestFile -SearchRoots @($ScriptRoot, $ProjectRoot) -Filter "neoforge-*-installer.jar"
    }

    Copy-Or-Download -Url $ModUrl -LocalPath $localMod -DestinationDir $modsDir -Name "Magic World all-in-one"
    foreach ($packUrl in $ResourcePackUrl) {
        Copy-Or-Download -Url $packUrl -LocalPath $localResourcePack -DestinationDir $resourcepacksDir -Name "Magic World Resource Pack"
    }
    Copy-Or-Download -Url $ShaderPackUrl -LocalPath $localShaderPack -DestinationDir $shaderpacksDir -Name "Magic World Shader Pack"

    $optionsFile = Join-Path $mcDir "options.txt"
    if (Test-Path -LiteralPath $optionsFile) {
        Write-InstallLog "options.txt encontrado. Mantive configuracoes do usuario sem sobrescrever."
    }

    $installer = $null
    if ($NeoForgeInstallerUrl) {
        $installerDir = Join-Path $ProjectRoot "tmp\installer-downloads"
        New-Item -ItemType Directory -Path $installerDir -Force | Out-Null
        $installer = Join-Path $installerDir (Split-Path -Leaf ([uri]$NeoForgeInstallerUrl).LocalPath)
        Write-InstallLog "Baixando NeoForge installer de $NeoForgeInstallerUrl"
        Invoke-WebRequest -Uri $NeoForgeInstallerUrl -OutFile $installer
    } elseif ($localNeoForgeInstaller) {
        $installer = $localNeoForgeInstaller
        Write-InstallLog "Usando NeoForge installer incluso: $installer"
    } else {
        $installerDir = Join-Path $ProjectRoot "tmp\installer-downloads"
        New-Item -ItemType Directory -Path $installerDir -Force | Out-Null
        $installer = Join-Path $installerDir (Split-Path -Leaf ([uri]$DefaultNeoForgeInstallerUrl).LocalPath)
        Write-InstallLog "Baixando NeoForge installer de $DefaultNeoForgeInstallerUrl"
        Invoke-WebRequest -Uri $DefaultNeoForgeInstallerUrl -OutFile $installer
    }

    if ($installer -and (Test-Path -LiteralPath $installer)) {
        Write-InstallLog "Abrindo instalador NeoForge: $installer"
        Write-InstallLog "No instalador, selecione Install client e a pasta Minecraft detectada: $mcDir"
        Start-Process -FilePath "java" -ArgumentList @("-jar", "`"$installer`"") -WorkingDirectory $mcDir
    } else {
        Write-InstallLog "NeoForge installer nao encontrado. Instale NeoForge $MinecraftVersion manualmente no TLauncher."
    }

    Write-InstallLog "Instalacao local concluida. Abra o TLauncher, selecione/crie perfil NeoForge $MinecraftVersion e use a pasta: $mcDir"
    return $mcDir
}

function Show-InstallerGui {
    Add-Type -AssemblyName System.Windows.Forms
    Add-Type -AssemblyName System.Drawing

    $form = New-Object System.Windows.Forms.Form
    $form.Text = "Magic World Installer"
    $form.Width = 760
    $form.Height = 540
    $form.StartPosition = "CenterScreen"
    $form.BackColor = [System.Drawing.Color]::FromArgb(18, 20, 28)

    if (Test-Path -LiteralPath $BannerPath) {
        $picture = New-Object System.Windows.Forms.PictureBox
        $picture.Image = [System.Drawing.Image]::FromFile($BannerPath)
        $picture.SizeMode = "Zoom"
        $picture.Left = 18
        $picture.Top = 16
        $picture.Width = 706
        $picture.Height = 170
        $form.Controls.Add($picture)
    }

    $label = New-Object System.Windows.Forms.Label
    $label.ForeColor = [System.Drawing.Color]::White
    $label.BackColor = [System.Drawing.Color]::Transparent
    $label.Left = 24
    $label.Top = 205
    $label.Width = 690
    $label.Height = 52
    $label.Font = New-Object System.Drawing.Font("Segoe UI", 10)
    $label.Text = "Este instalador prepara a pasta .minecraft usada pelo TLauncher: copia o all-in-one, Resource Pack e Shader Pack, e abre o instalador NeoForge correto. Ele nao altera o executavel do TLauncher e nao mexe em login."
    $form.Controls.Add($label)

    $pathBox = New-Object System.Windows.Forms.TextBox
    $pathBox.Left = 24
    $pathBox.Top = 278
    $pathBox.Width = 560
    $pathBox.Text = Find-MinecraftDir
    $form.Controls.Add($pathBox)

    $browse = New-Object System.Windows.Forms.Button
    $browse.Left = 596
    $browse.Top = 276
    $browse.Width = 128
    $browse.Height = 28
    $browse.Text = "Escolher pasta"
    $browse.FlatStyle = "Flat"
    $browse.BackColor = [System.Drawing.Color]::FromArgb(70, 84, 116)
    $browse.ForeColor = [System.Drawing.Color]::White
    $browse.Add_Click({
        $dialog = New-Object System.Windows.Forms.FolderBrowserDialog
        $dialog.Description = "Escolha a pasta .minecraft usada pelo TLauncher"
        if ($dialog.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) {
            $pathBox.Text = $dialog.SelectedPath
        }
    })
    $form.Controls.Add($browse)

    $output = New-Object System.Windows.Forms.TextBox
    $output.Left = 24
    $output.Top = 324
    $output.Width = 700
    $output.Height = 120
    $output.Multiline = $true
    $output.ScrollBars = "Vertical"
    $output.ReadOnly = $true
    $output.BackColor = [System.Drawing.Color]::FromArgb(236, 242, 252)
    $output.ForeColor = [System.Drawing.Color]::FromArgb(14, 20, 32)
    $output.Font = New-Object System.Drawing.Font("Consolas", 9)
    $form.Controls.Add($output)
    Write-InstallerOverview -Output $output

    $install = New-Object System.Windows.Forms.Button
    $install.Left = 24
    $install.Top = 460
    $install.Width = 210
    $install.Height = 34
    $install.Text = "Instalar / Atualizar"
    $install.FlatStyle = "Flat"
    $install.BackColor = [System.Drawing.Color]::FromArgb(86, 105, 170)
    $install.ForeColor = [System.Drawing.Color]::White
    $install.Add_Click({
        try {
            $script:MinecraftDir = $pathBox.Text
            $output.AppendText("Instalando em $script:MinecraftDir`r`n")
            $installedPath = Install-MagicWorld
            $output.AppendText("Concluido. Pasta: $installedPath`r`n")
            [System.Windows.Forms.MessageBox]::Show("Magic World instalado. Abra o TLauncher e selecione NeoForge $MinecraftVersion.", "Magic World Installer")
        } catch {
            $output.AppendText("Erro: $($_.Exception.Message)`r`n")
            [System.Windows.Forms.MessageBox]::Show($_.Exception.Message, "Erro no instalador")
        }
    })
    $form.Controls.Add($install)

    $openFolder = New-Object System.Windows.Forms.Button
    $openFolder.Left = 248
    $openFolder.Top = 460
    $openFolder.Width = 160
    $openFolder.Height = 34
    $openFolder.Text = "Abrir pasta"
    $openFolder.FlatStyle = "Flat"
    $openFolder.BackColor = [System.Drawing.Color]::FromArgb(70, 84, 116)
    $openFolder.ForeColor = [System.Drawing.Color]::White
    $openFolder.Add_Click({
        if (Test-Path -LiteralPath $pathBox.Text) {
            Start-Process explorer.exe $pathBox.Text
        }
    })
    $form.Controls.Add($openFolder)

    $close = New-Object System.Windows.Forms.Button
    $close.Left = 564
    $close.Top = 460
    $close.Width = 160
    $close.Height = 34
    $close.Text = "Fechar"
    $close.FlatStyle = "Flat"
    $close.BackColor = [System.Drawing.Color]::FromArgb(70, 84, 116)
    $close.ForeColor = [System.Drawing.Color]::White
    $close.Add_Click({ $form.Close() })
    $form.Controls.Add($close)

    [void]$form.ShowDialog()
}

New-Item -ItemType Directory -Path (Split-Path -Parent $LogPath) -Force | Out-Null
Write-InstallLog "Magic World Installer iniciado."

if ($NoGui) {
    Install-MagicWorld | Out-Null
} else {
    Show-InstallerGui
}
