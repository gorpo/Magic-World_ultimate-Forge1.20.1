param(
    [switch]$SelfTest
)

$ErrorActionPreference = "Stop"

$LauncherRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$AssetsRoot = Join-Path $LauncherRoot "assets"
$BackgroundPath = Join-Path $AssetsRoot "title_background_static.png"
$LogoPath = Join-Path $AssetsRoot "title_logo.png"
$InstallerScriptPath = Join-Path $LauncherRoot "install-magicworld-forge-tlauncher.ps1"
$MinecraftDir = Join-Path $env:APPDATA ".minecraft"
$TLauncherExe = Join-Path $MinecraftDir "TLauncher.exe"
$RepoUrl = "https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1"
$ReleaseUrl = "https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1/releases/tag/installer-upload-manual-forge-1.20.1-v1.0.0.1-main"
$ReleaseApi = "https://api.github.com/repos/gorpo/Magic-World_ultimate-Forge1.20.1/releases/tags/installer-upload-manual-forge-1.20.1-v1.0.0.1-main"
$FallbackInstallerUrl = "https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1/releases/download/installer-upload-manual-forge-1.20.1-v1.0.0.1-main/MagicWorldInstaller.exe"
$InstallerSha256 = "5cafd886fe3cd7e2d9f84e93e79f4110eb1250f08313ebc4172391b5ee3e48d3"
$BundledInstallerPath = Join-Path $LauncherRoot "MagicWorldInstaller.exe"

function Get-MagicWorldAccountName {
    $profiles = Join-Path $MinecraftDir "TlauncherProfiles.json"
    if (!(Test-Path -LiteralPath $profiles)) {
        return "Usuario TLauncher"
    }

    try {
        $json = Get-Content -LiteralPath $profiles -Raw | ConvertFrom-Json
        $selected = $json.selectedAccountUUID
        if ($selected -and $json.accounts.PSObject.Properties.Name -contains $selected) {
            return $json.accounts.$selected.displayName
        }
    } catch {
    }
    return "Usuario TLauncher"
}

function Get-InstallerUrl {
    try {
        $release = Invoke-RestMethod -Uri $ReleaseApi -Headers @{ "User-Agent" = "MagicWorldLauncher" }
        $asset = $release.assets | Where-Object { $_.name -eq "MagicWorldInstaller.exe" } | Select-Object -First 1
        if ($asset -and $asset.browser_download_url) {
            return $asset.browser_download_url
        }
    } catch {
    }
    return $FallbackInstallerUrl
}

function Update-Status {
    param(
        [string]$Text,
        [int]$Percent = -1
    )

    $script:StatusText.Text = $Text
    if ($Percent -ge 0) {
        $script:ProgressBar.Value = [Math]::Min(100, [Math]::Max(0, $Percent))
    }
    [System.Windows.Forms.Application]::DoEvents()
}

function Invoke-MagicWorldInstaller {
    $tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) "MagicWorldLauncher"
    $extractRoot = Join-Path $tempRoot "full-payload"
    New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
    if (Test-Path -LiteralPath $extractRoot) {
        Remove-Item -LiteralPath $extractRoot -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $extractRoot | Out-Null
    $installerPath = Join-Path $tempRoot "MagicWorldInstaller.exe"

    if (Test-Path -LiteralPath $BundledInstallerPath) {
        Update-Status "Usando pacote FULL local do Magic World..." 8
        Copy-Item -LiteralPath $BundledInstallerPath -Destination $installerPath -Force
    } else {
        Update-Status "Baixando installer FULL do Magic World..." 8
        $installerUrl = Get-InstallerUrl
        Invoke-WebRequest -Uri $installerUrl -OutFile $installerPath -UseBasicParsing
    }

    Update-Status "Validando download..." 50
    $hash = (Get-FileHash -LiteralPath $installerPath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($hash -ne $InstallerSha256) {
        throw "Hash SHA256 inesperado. Download abortado para sua seguranca."
    }

    Update-Status "Extraindo pacote FULL em pasta temporaria..." 60
    Expand-MagicWorldFullPayload -InstallerPath $installerPath -DestinationRoot $extractRoot

    $packageMinecraftDir = Join-Path $extractRoot "payload\.minecraft"
    $forgeInstallerPath = Join-Path $extractRoot "payload\forge\forge-1.20.1-47.4.10-installer.jar"
    if (!(Test-Path -LiteralPath $InstallerScriptPath)) {
        throw "Script de instalacao ausente: $InstallerScriptPath"
    }
    if (!(Test-Path -LiteralPath $packageMinecraftDir)) {
        throw "Payload .minecraft nao encontrado dentro do installer FULL."
    }
    if (!(Test-Path -LiteralPath $forgeInstallerPath)) {
        throw "Forge installer 1.20.1-47.4.10 nao encontrado dentro do installer FULL."
    }

    Update-Status "Instalando Forge 1.20.1, mods, resource pack e shader..." 72
    $installArgs = @(
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-WindowStyle", "Hidden",
        "-File", $InstallerScriptPath,
        "-NoGui",
        "-MinecraftDir", $MinecraftDir,
        "-PackageMinecraftDir", $packageMinecraftDir,
        "-ForgeInstallerPath", $forgeInstallerPath
    )
    $process = Start-Process -FilePath "powershell.exe" -ArgumentList $installArgs -WorkingDirectory $LauncherRoot -WindowStyle Hidden -Wait -PassThru
    if ($process.ExitCode -ne 0) {
        throw "Instalacao retornou codigo $($process.ExitCode). Log: $env:TEMP\magicworld-forge-installer.log"
    }

    Update-Status "Instalacao concluida. Pronto para jogar." 100
}

function Expand-MagicWorldFullPayload {
    param(
        [string]$InstallerPath,
        [string]$DestinationRoot
    )

    $markerText = "MAGICWORLD_FULL_PAYLOAD_V1"
    $marker = [System.Text.Encoding]::ASCII.GetBytes($markerText)
    $zipPath = Join-Path $DestinationRoot "magicworld-full-payload.zip"
    $buffer = New-Object byte[] (1024 * 1024)

    $input = [System.IO.File]::OpenRead($InstallerPath)
    try {
        if ($input.Length -lt ($marker.Length + 8)) {
            throw "Installer FULL invalido ou incompleto."
        }

        $input.Position = $input.Length - $marker.Length
        $actualMarker = New-Object byte[] $marker.Length
        [void]$input.Read($actualMarker, 0, $actualMarker.Length)
        for ($i = 0; $i -lt $marker.Length; $i++) {
            if ($actualMarker[$i] -ne $marker[$i]) {
                throw "Payload FULL nao encontrado no installer baixado."
            }
        }

        $input.Position = $input.Length - $marker.Length - 8
        $lengthBytes = New-Object byte[] 8
        [void]$input.Read($lengthBytes, 0, 8)
        $payloadLength = [System.BitConverter]::ToInt64($lengthBytes, 0)
        $payloadStart = $input.Length - $marker.Length - 8 - $payloadLength
        if ($payloadLength -le 0 -or $payloadStart -lt 0) {
            throw "Tamanho de payload FULL invalido."
        }

        $input.Position = $payloadStart
        $output = [System.IO.File]::Create($zipPath)
        try {
            $remaining = $payloadLength
            while ($remaining -gt 0) {
                $toRead = [Math]::Min($buffer.Length, $remaining)
                $read = $input.Read($buffer, 0, [int]$toRead)
                if ($read -le 0) {
                    throw "Fim inesperado ao extrair payload FULL."
                }
                $output.Write($buffer, 0, $read)
                $remaining -= $read
            }
        } finally {
            $output.Dispose()
        }
    } finally {
        $input.Dispose()
    }

    Expand-Archive -LiteralPath $zipPath -DestinationPath $DestinationRoot -Force
}

function Open-Repo {
    Start-Process $RepoUrl
}

function Open-TLauncher {
    if (Test-Path -LiteralPath $TLauncherExe) {
        Start-Process -FilePath $TLauncherExe -WorkingDirectory $MinecraftDir
    } else {
        [System.Windows.MessageBox]::Show("Nao encontrei o TLauncher.exe em $MinecraftDir.", "Magic World Launcher", "OK", "Warning") | Out-Null
    }
}

if ($SelfTest) {
    $result = [ordered]@{
        launcherRoot = $LauncherRoot
        backgroundExists = Test-Path -LiteralPath $BackgroundPath
        logoExists = Test-Path -LiteralPath $LogoPath
        installerScriptExists = Test-Path -LiteralPath $InstallerScriptPath
        bundledInstallerExists = Test-Path -LiteralPath $BundledInstallerPath
        minecraftDir = $MinecraftDir
        tLauncherExists = Test-Path -LiteralPath $TLauncherExe
        accountName = Get-MagicWorldAccountName
        installerUrl = Get-InstallerUrl
    }
    $result | ConvertTo-Json -Depth 4
    exit 0
}

Add-Type -AssemblyName PresentationFramework
Add-Type -AssemblyName System.Windows.Forms

$window = New-Object System.Windows.Window
$window.Title = "Magic World Launcher"
$window.Width = 980
$window.Height = 620
$window.WindowStartupLocation = "CenterScreen"
$window.ResizeMode = "CanMinimize"

$grid = New-Object System.Windows.Controls.Grid
$window.Content = $grid

if (Test-Path -LiteralPath $BackgroundPath) {
    $image = New-Object System.Windows.Media.Imaging.BitmapImage
    $image.BeginInit()
    $image.UriSource = New-Object System.Uri($BackgroundPath)
    $image.CacheOption = [System.Windows.Media.Imaging.BitmapCacheOption]::OnLoad
    $image.EndInit()

    $brush = New-Object System.Windows.Media.ImageBrush
    $brush.ImageSource = $image
    $brush.Stretch = [System.Windows.Media.Stretch]::UniformToFill
    $grid.Background = $brush
} else {
    $grid.Background = "#101820"
}

$overlay = New-Object System.Windows.Controls.Border
$overlay.Background = "#99000000"
$grid.Children.Add($overlay) | Out-Null

$stack = New-Object System.Windows.Controls.StackPanel
$stack.Width = 700
$stack.HorizontalAlignment = "Center"
$stack.VerticalAlignment = "Center"
$stack.Margin = "0,20,0,20"
$grid.Children.Add($stack) | Out-Null

if (Test-Path -LiteralPath $LogoPath) {
    $logo = New-Object System.Windows.Controls.Image
    $logo.Source = $LogoPath
    $logo.Width = 560
    $logo.Height = 185
    $logo.Stretch = "Uniform"
    $logo.Margin = "0,0,0,12"
    $stack.Children.Add($logo) | Out-Null
}

$title = New-Object System.Windows.Controls.TextBlock
$title.Text = "Magic World Forge 1.20.1"
$title.FontSize = 30
$title.FontWeight = "Bold"
$title.Foreground = "White"
$title.TextAlignment = "Center"
$title.Margin = "0,0,0,8"
$stack.Children.Add($title) | Out-Null

$account = New-Object System.Windows.Controls.TextBlock
$account.Text = "Login detectado: $(Get-MagicWorldAccountName)"
$account.FontSize = 16
$account.Foreground = "#FFDAA520"
$account.TextAlignment = "Center"
$account.Margin = "0,0,0,24"
$stack.Children.Add($account) | Out-Null

$script:StatusText = New-Object System.Windows.Controls.TextBlock
$script:StatusText.Text = "Instale uma vez. Depois abra o TLauncher e jogue em Forge 1.20.1."
$script:StatusText.FontSize = 16
$script:StatusText.Foreground = "White"
$script:StatusText.TextAlignment = "Center"
$script:StatusText.TextWrapping = "Wrap"
$script:StatusText.Margin = "0,0,0,12"
$stack.Children.Add($script:StatusText) | Out-Null

$script:ProgressBar = New-Object System.Windows.Controls.ProgressBar
$script:ProgressBar.Minimum = 0
$script:ProgressBar.Maximum = 100
$script:ProgressBar.Value = 0
$script:ProgressBar.Height = 18
$script:ProgressBar.Margin = "50,0,50,26"
$stack.Children.Add($script:ProgressBar) | Out-Null

$buttonPanel = New-Object System.Windows.Controls.StackPanel
$buttonPanel.Orientation = "Horizontal"
$buttonPanel.HorizontalAlignment = "Center"
$stack.Children.Add($buttonPanel) | Out-Null

function New-MagicButton {
    param(
        [string]$Text,
        [int]$Width = 210
    )
    $button = New-Object System.Windows.Controls.Button
    $button.Content = $Text
    $button.Width = $Width
    $button.Height = 44
    $button.Margin = "8"
    $button.FontSize = 15
    $button.FontWeight = "SemiBold"
    $button.Foreground = "White"
    $button.Background = "#CC101820"
    $button.BorderBrush = "#FFDAA520"
    $button.BorderThickness = "2"
    return $button
}

$installButton = New-MagicButton "Instalar Magic World" 230
$repoButton = New-MagicButton "Repositorio" 160
$playButton = New-MagicButton "Abrir TLauncher" 180

$buttonPanel.Children.Add($installButton) | Out-Null
$buttonPanel.Children.Add($repoButton) | Out-Null
$buttonPanel.Children.Add($playButton) | Out-Null

$installButton.Add_Click({
    $installButton.IsEnabled = $false
    try {
        Invoke-MagicWorldInstaller
        [System.Windows.MessageBox]::Show("Tudo instalado. Selecione Forge 1.20.1 no TLauncher e jogue Magic World.", "Magic World Launcher", "OK", "Information") | Out-Null
    } catch {
        Update-Status "Erro: $($_.Exception.Message)" 0
        [System.Windows.MessageBox]::Show($_.Exception.Message, "Magic World Launcher", "OK", "Error") | Out-Null
    } finally {
        $installButton.IsEnabled = $true
    }
})

$repoButton.Add_Click({ Open-Repo })
$playButton.Add_Click({ Open-TLauncher })

$window.ShowDialog() | Out-Null
