param(
    [switch]$SelfTest,
    [switch]$LaunchDryRun,
    [switch]$InstallOnly,
    [switch]$LaunchOnly
)

$ErrorActionPreference = "Stop"

$CurrentScriptPath = $(if ($env:MAGICWORLD_LAUNCHER_SCRIPT_PATH) { $env:MAGICWORLD_LAUNCHER_SCRIPT_PATH } else { $MyInvocation.MyCommand.Path })
$LauncherRoot = Split-Path -Parent $CurrentScriptPath
$AssetsRoot = Join-Path $LauncherRoot "assets"
$BackgroundPath = Join-Path $AssetsRoot "title_background_static.png"
$LogoPath = Join-Path $AssetsRoot "title_logo.png"
$WindowIconPath = Join-Path $AssetsRoot "magicworld.ico"
if (!(Test-Path -LiteralPath $WindowIconPath)) {
    $WindowIconPath = Join-Path $LauncherRoot "MagicWorldLauncher.ico"
}
$InstallerScriptPath = Join-Path $LauncherRoot "install-magicworld-forge.ps1"
$MagicWorldDataRoot = Join-Path $env:APPDATA "MagicWorldLauncher"
$MinecraftDir = Join-Path $MagicWorldDataRoot ".minecraft"
$MagicJavaRoot = Join-Path $MagicWorldDataRoot "runtime\java17"
$AccountsPath = Join-Path $MagicWorldDataRoot "accounts.json"
$SettingsPath = Join-Path $MagicWorldDataRoot "settings.json"
$ServerFavoritesPath = Join-Path $MagicWorldDataRoot "servidores.json"
$ServerHelpPath = Join-Path $MagicWorldDataRoot "servidores-magicworld.txt"
$MinecraftServerListPath = Join-Path $MinecraftDir "servers.dat"
$SavedPasswordPath = Join-Path $MagicWorldDataRoot "tlauncher-password.xml"
$LaunchLogPath = Join-Path $MagicWorldDataRoot "magicworld-launcher-last-launch.log"
$LaunchOutLogPath = Join-Path $MagicWorldDataRoot "magicworld-launcher-last-stdout.log"
$LaunchErrLogPath = Join-Path $MagicWorldDataRoot "magicworld-launcher-last-stderr.log"
$TLauncherAuthApiUrl = $env:MAGICWORLD_TLAUNCHER_AUTH_API_URL
$RepoUrl = "https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1"
$ReleaseUrl = "https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1/releases/tag/installer-upload-manual-forge-1.20.1-v1.0.0.1-main"
$ReleaseApi = "https://api.github.com/repos/gorpo/Magic-World_ultimate-Forge1.20.1/releases/tags/installer-upload-manual-forge-1.20.1-v1.0.0.1-main"
$FallbackInstallerUrl = "https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1/releases/download/installer-upload-manual-forge-1.20.1-v1.0.0.1-main/MagicWorldInstaller.exe"
$InstallerSha256 = "dfed9ff0a8b6c316ad5c133b1389b612a707947b5586befa1f61ca1dd6a1468e"
$BundledInstallerPath = Join-Path $LauncherRoot "MagicWorldInstaller.exe"

function Get-MagicWorldAccountName {
    $account = Get-MagicWorldAccount
    return $account.username
}

function Get-MagicWorldSettings {
    $defaults = [ordered]@{
        minMemoryGb = 2
        maxMemoryGb = 8
        customResolution = $false
        resolutionWidth = 1280
        resolutionHeight = 720
        minimizeLauncherOnPlay = $true
    }

    if (!(Test-Path -LiteralPath $SettingsPath)) {
        return [pscustomobject]$defaults
    }

    try {
        $json = Get-Content -LiteralPath $SettingsPath -Raw | ConvertFrom-Json
        foreach ($key in @($defaults.Keys)) {
            if ($json.PSObject.Properties.Name -notcontains $key) {
                $json | Add-Member -NotePropertyName $key -NotePropertyValue $defaults[$key]
            }
        }
        return $json
    } catch {
        return [pscustomobject]$defaults
    }
}

function Save-MagicWorldSettings {
    param(
        [int]$MinMemoryGb,
        [int]$MaxMemoryGb,
        [bool]$CustomResolution,
        [int]$ResolutionWidth,
        [int]$ResolutionHeight,
        [bool]$MinimizeLauncherOnPlay
    )

    $settings = [ordered]@{
        minMemoryGb = [Math]::Max(1, [Math]::Min(16, $MinMemoryGb))
        maxMemoryGb = [Math]::Max(2, [Math]::Min(16, $MaxMemoryGb))
        customResolution = $CustomResolution
        resolutionWidth = [Math]::Max(640, [Math]::Min(7680, $ResolutionWidth))
        resolutionHeight = [Math]::Max(480, [Math]::Min(4320, $ResolutionHeight))
        minimizeLauncherOnPlay = $MinimizeLauncherOnPlay
    }
    if ($settings.maxMemoryGb -lt $settings.minMemoryGb) {
        $settings.maxMemoryGb = $settings.minMemoryGb
    }

    New-Item -ItemType Directory -Force -Path $MagicWorldDataRoot | Out-Null
    $settings | ConvertTo-Json -Depth 3 | Set-Content -LiteralPath $SettingsPath -Encoding UTF8
    return [pscustomobject]$settings
}

function Get-MagicWorldAccount {
    if (!(Test-Path -LiteralPath $AccountsPath)) {
        return [pscustomobject]@{
            username = "Player"
            uuid = ""
            accessToken = "0"
            userType = "legacy"
            loginProvider = "offline"
            authApiUrl = $TLauncherAuthApiUrl
            savedPassword = $false
        }
    }

    try {
        $json = Get-Content -LiteralPath $AccountsPath -Raw | ConvertFrom-Json
        if ($json.username -and ![string]::IsNullOrWhiteSpace([string]$json.username)) {
            return [pscustomobject]@{
                username = [string]$json.username
                uuid = $(if ($json.uuid) { [string]$json.uuid } else { "" })
                accessToken = $(if ($json.accessToken) { [string]$json.accessToken } else { "0" })
                userType = $(if ($json.userType) { [string]$json.userType } else { "legacy" })
                loginProvider = $(if ($json.loginProvider) { [string]$json.loginProvider } else { "offline" })
                authApiUrl = $(if ($json.authApiUrl) { [string]$json.authApiUrl } else { $TLauncherAuthApiUrl })
                savedPassword = Test-Path -LiteralPath $SavedPasswordPath
            }
        }
    } catch {
    }
    return [pscustomobject]@{
        username = "Player"
        uuid = ""
        accessToken = "0"
        userType = "legacy"
        loginProvider = "offline"
        authApiUrl = $TLauncherAuthApiUrl
        savedPassword = $false
    }
}

function Save-MagicWorldAccount {
    param(
        [string]$Username,
        [string]$Uuid = "",
        [string]$AccessToken = "0",
        [string]$UserType = "legacy",
        [string]$LoginProvider = "TLauncher API",
        [string]$AuthApiUrl = ""
    )

    $clean = ([string]$Username).Trim()
    if ([string]::IsNullOrWhiteSpace($clean)) {
        throw "Informe um nome de usuario."
    }
    if ($clean.Length -gt 16 -or $clean -notmatch '^[A-Za-z0-9_]+$') {
        throw "Use apenas letras, numeros e underline, com ate 16 caracteres."
    }

    New-Item -ItemType Directory -Force -Path $MagicWorldDataRoot | Out-Null
    $data = [ordered]@{
        username = $clean
        uuid = $Uuid
        accessToken = $AccessToken
        userType = $UserType
        loginProvider = $LoginProvider
        authApiUrl = $AuthApiUrl
        updatedAt = (Get-Date).ToString("o")
    }
    $data | ConvertTo-Json -Depth 3 | Set-Content -LiteralPath $AccountsPath -Encoding UTF8
    return $clean
}

function Save-TLauncherPassword {
    param([string]$Password)

    New-Item -ItemType Directory -Force -Path $MagicWorldDataRoot | Out-Null
    ConvertTo-SecureString -String $Password -AsPlainText -Force |
        Export-Clixml -LiteralPath $SavedPasswordPath
}

function Get-SavedTLauncherPassword {
    if (!(Test-Path -LiteralPath $SavedPasswordPath)) {
        return ""
    }

    try {
        $secure = Import-Clixml -LiteralPath $SavedPasswordPath
        $credential = New-Object System.Management.Automation.PSCredential("tlauncher", $secure)
        return $credential.GetNetworkCredential().Password
    } catch {
        return ""
    }
}

function Remove-SavedTLauncherPassword {
    Remove-Item -LiteralPath $SavedPasswordPath -Force -ErrorAction SilentlyContinue
}

function Invoke-TLauncherApiLogin {
    param(
        [string]$Username,
        [string]$Password,
        [string]$ApiUrl
    )

    if ([string]::IsNullOrWhiteSpace($ApiUrl)) {
        $ApiUrl = (Get-MagicWorldAccount).authApiUrl
    }
    if ([string]::IsNullOrWhiteSpace($ApiUrl)) {
        throw "Informe a URL oficial da API de login do TLauncher."
    }
    if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($Password)) {
        throw "Informe login e senha."
    }

    $body = @{
        username = $Username
        password = $Password
        launcher = "MagicWorldLauncher"
        game = "Minecraft"
    } | ConvertTo-Json -Depth 4

    $response = Invoke-RestMethod -Uri $ApiUrl -Method Post -Body $body -ContentType "application/json" -Headers @{ "User-Agent" = "MagicWorldLauncher/1.0" }
    $token = $(if ($response.accessToken) { $response.accessToken } elseif ($response.token) { $response.token } else { "" })
    $displayName = $(if ($response.displayName) { $response.displayName } elseif ($response.username) { $response.username } elseif ($response.login) { $response.login } elseif ($response.profile -and $response.profile.name) { $response.profile.name } else { $Username })
    $uuid = $(if ($response.uuid) { $response.uuid } elseif ($response.id) { $response.id } elseif ($response.profile -and $response.profile.id) { $response.profile.id } else { "" })

    if ([string]::IsNullOrWhiteSpace([string]$token)) {
        throw "A API de login respondeu sem accessToken/token. Verifique o endpoint oficial configurado."
    }

    Save-MagicWorldAccount -Username $displayName -Uuid $uuid -AccessToken $token -UserType "tlauncher" -LoginProvider "TLauncher API" -AuthApiUrl $ApiUrl | Out-Null
    return Get-MagicWorldAccount
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

    $boundedPercent = -1
    if ($Percent -ge 0) {
        $boundedPercent = [Math]::Min(100, [Math]::Max(0, $Percent))
    }

    if ($script:StatusText) {
        $script:StatusText.Text = $Text
    }
    if ($script:ProgressBar -and $boundedPercent -ge 0) {
        $script:ProgressBar.Value = $boundedPercent
    }
    if ($script:ProgressPercentText -and $boundedPercent -ge 0) {
        $script:ProgressPercentText.Text = "$boundedPercent%"
    }
    if ($script:StatusText) {
        [System.Windows.Forms.Application]::DoEvents()
    }
    if ($InstallOnly -or $LaunchOnly) {
        if ($boundedPercent -ge 0) {
            Write-Output ("PROGRESS:{0}:{1}" -f $boundedPercent, $Text)
        } else {
            Write-Output ("STATUS:{0}" -f $Text)
        }
    }
}

function Resolve-MinecraftPath {
    param([string]$Path)
    if ($Path -like "libraries/*" -or $Path -like "libraries\\*") {
        return Join-Path $MinecraftDir $Path
    }
    return Join-Path (Join-Path $MinecraftDir "libraries") $Path
}

function Test-MagicRuleAllows {
    param($Rules)
    if ($null -eq $Rules -or $Rules.Count -eq 0) {
        return $true
    }

    $allowed = $false
    foreach ($rule in $Rules) {
        if (Test-MagicRuleMatches $rule) {
            $allowed = $rule.action -eq "allow"
        }
    }
    return $allowed
}

function Test-MagicRuleMatches {
    param($Rule)

    if ($null -eq $Rule) {
        return $false
    }

    if ($Rule.os -and $Rule.os.name -and [string]$Rule.os.name -ne "windows") {
        return $false
    }

    if ($Rule.features) {
        foreach ($property in $Rule.features.PSObject.Properties) {
            $expected = [bool]$property.Value
            $actual = switch ([string]$property.Name) {
                "has_custom_resolution" { [bool](Get-MagicWorldSettings).customResolution }
                "is_demo_user" { $false }
                "has_quick_plays_support" { $false }
                "is_quick_play_singleplayer" { $false }
                "is_quick_play_multiplayer" { $false }
                "is_quick_play_realms" { $false }
                default { $false }
            }

            if ($actual -ne $expected) {
                return $false
            }
        }
    }

    return $true
}

function Test-MagicLibraryIsNative {
    param($Library)

    if ($Library.natives) {
        return $true
    }
    if ($Library.name -and ([string]$Library.name -match ":natives-")) {
        return $true
    }
    if ($Library.downloads -and $Library.downloads.artifact -and $Library.downloads.artifact.path -and ([string]$Library.downloads.artifact.path -match "natives-")) {
        return $true
    }
    if ($Library.artifact -and $Library.artifact.path -and ([string]$Library.artifact.path -match "natives-")) {
        return $true
    }
    return $false
}

function Test-MagicNativeMatchesThisWindows {
    param([string]$PathOrName)

    if ([string]::IsNullOrWhiteSpace($PathOrName)) {
        return $false
    }
    if ($PathOrName -notmatch "natives-windows") {
        return $false
    }
    if ([Environment]::Is64BitOperatingSystem) {
        return ($PathOrName -notmatch "natives-windows-arm64" -and $PathOrName -notmatch "natives-windows-x86")
    }
    return ($PathOrName -match "natives-windows-x86")
}

function Add-MagicArguments {
    param(
        [System.Collections.ArrayList]$Target,
        $Items
    )
    foreach ($item in $Items) {
        if ($item -is [string]) {
            [void]$Target.Add($item)
            continue
        }
        if (!(Test-MagicRuleAllows $item.rules)) {
            continue
        }
        $valueItems = $null
        if ($null -ne $item.value) {
            $valueItems = $item.value
        } elseif ($null -ne $item.values) {
            $valueItems = $item.values
        }

        if ($valueItems -is [array]) {
            foreach ($value in $valueItems) {
                [void]$Target.Add([string]$value)
            }
        } elseif ($null -ne $valueItems) {
            [void]$Target.Add([string]$valueItems)
        }
    }
}

function Get-MagicWorldVersionJsonPath {
    $candidates = @(
        (Join-Path $MinecraftDir "versions\1.20.1-forge-47.4.10\1.20.1-forge-47.4.10.json"),
        (Join-Path $MinecraftDir "versions\Forge 1.20.1\Forge 1.20.1.json"),
        (Join-Path $MinecraftDir "versions\1.20.1-forge-47.4.20\1.20.1-forge-47.4.20.json")
    )

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    $found = Get-ChildItem -LiteralPath (Join-Path $MinecraftDir "versions") -Recurse -Filter "*.json" -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match "forge" -and $_.FullName -match "1\\.20\\.1" } |
        Select-Object -First 1
    if ($found) {
        return $found.FullName
    }
    return $null
}

function Get-MagicVanillaVersionJsonPath {
    param([string]$VersionId)

    $versionDir = Join-Path $MinecraftDir ("versions\" + $VersionId)
    $versionJsonPath = Join-Path $versionDir ($VersionId + ".json")
    if (Test-Path -LiteralPath $versionJsonPath) {
        return $versionJsonPath
    }

    Update-Status "Baixando metadados do Minecraft $VersionId..." 24
    New-Item -ItemType Directory -Force -Path $versionDir | Out-Null
    $manifest = Invoke-RestMethod -Uri "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json" -Headers @{ "User-Agent" = "MagicWorldLauncher" }
    $entry = $manifest.versions | Where-Object { $_.id -eq $VersionId } | Select-Object -First 1
    if (!$entry -or !$entry.url) {
        throw "Nao encontrei metadados oficiais do Minecraft $VersionId."
    }

    Invoke-MagicDownload -Url $entry.url -Destination $versionJsonPath
    return $versionJsonPath
}

function Add-MagicMember {
    param(
        $Object,
        [string]$Name,
        $Value
    )
    if ($Object.PSObject.Properties.Name -contains $Name) {
        $Object.$Name = $Value
    } else {
        $Object | Add-Member -NotePropertyName $Name -NotePropertyValue $Value
    }
}

function Get-MagicWorldVersionJson {
    param([string]$VersionJsonPath)

    $child = Get-Content -LiteralPath $VersionJsonPath -Raw | ConvertFrom-Json
    if (!$child.inheritsFrom) {
        Add-MagicMember -Object $child -Name "magicVersionJsonPath" -Value $VersionJsonPath
        Add-MagicMember -Object $child -Name "magicClientVersionId" -Value $child.id
        Add-MagicMember -Object $child -Name "magicInheritedProfile" -Value $false
        return $child
    }

    $parentPath = Get-MagicVanillaVersionJsonPath -VersionId $child.inheritsFrom
    $parent = Get-Content -LiteralPath $parentPath -Raw | ConvertFrom-Json

    $merged = [pscustomobject]@{
        id = $child.id
        mainClass = $(if ($child.mainClass) { $child.mainClass } else { $parent.mainClass })
        assetIndex = $(if ($child.assetIndex) { $child.assetIndex } else { $parent.assetIndex })
        downloads = $(if ($child.downloads) { $child.downloads } else { $parent.downloads })
        libraries = @()
        arguments = [pscustomobject]@{
            jvm = @()
            game = @()
        }
        magicVersionJsonPath = $VersionJsonPath
        magicClientVersionId = $child.inheritsFrom
        magicInheritedProfile = $true
    }

    $merged.libraries = @($parent.libraries) + @($child.libraries)
    if ($parent.arguments -and $parent.arguments.jvm) { $merged.arguments.jvm = @($merged.arguments.jvm) + @($parent.arguments.jvm) }
    if ($child.arguments -and $child.arguments.jvm) { $merged.arguments.jvm = @($merged.arguments.jvm) + @($child.arguments.jvm) }
    if ($parent.arguments -and $parent.arguments.game) { $merged.arguments.game = @($merged.arguments.game) + @($parent.arguments.game) }
    if ($child.arguments -and $child.arguments.game) { $merged.arguments.game = @($merged.arguments.game) + @($child.arguments.game) }

    return $merged
}

function Get-MagicJavaPath {
    $javaw = Get-ChildItem -LiteralPath $MagicJavaRoot -Recurse -Filter "javaw.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($javaw) {
        return $javaw.FullName
    }

    try {
        Ensure-MagicJavaRuntime | Out-Null
        $javaw = Get-ChildItem -LiteralPath $MagicJavaRoot -Recurse -Filter "javaw.exe" -ErrorAction SilentlyContinue |
            Select-Object -First 1
        if ($javaw) {
            return $javaw.FullName
        }
    } catch {
    }

    if ($env:JAVA_HOME) {
        $fromHome = Join-Path $env:JAVA_HOME "bin\javaw.exe"
        if (Test-Path -LiteralPath $fromHome) {
            return $fromHome
        }
    }

    $command = Get-Command javaw.exe -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }
    throw "Java 17 nao encontrado no runtime proprio do Magic World."
}

function Get-MagicJavaExePath {
    $javaw = Get-MagicJavaPath
    $java = Join-Path (Split-Path -Parent $javaw) "java.exe"
    if (Test-Path -LiteralPath $java) {
        return $java
    }
    throw "java.exe nao encontrado ao lado de $javaw."
}

function Ensure-MagicJavaRuntime {
    $existing = Get-ChildItem -LiteralPath $MagicJavaRoot -Recurse -Filter "javaw.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($existing) {
        return $existing.FullName
    }

    Update-Status "Baixando Java 17 proprio do Magic World..." 18
    New-Item -ItemType Directory -Force -Path $MagicJavaRoot | Out-Null
    $tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) "MagicWorldLauncher"
    New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
    $javaZip = Join-Path $tempRoot "magicworld-java17.zip"
    if (Test-Path -LiteralPath $javaZip) {
        Remove-Item -LiteralPath $javaZip -Force
    }

    $javaUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
    Invoke-WebRequest -Uri $javaUrl -OutFile $javaZip -UseBasicParsing -MaximumRedirection 10
    Expand-Archive -LiteralPath $javaZip -DestinationPath $MagicJavaRoot -Force

    $javaw = Get-ChildItem -LiteralPath $MagicJavaRoot -Recurse -Filter "javaw.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if (!$javaw) {
        throw "Falha ao preparar Java 17 proprio do Magic World."
    }
    return $javaw.FullName
}

function Invoke-MagicDownload {
    param(
        [string]$Url,
        [string]$Destination
    )
    if ([string]::IsNullOrWhiteSpace($Url) -or [string]::IsNullOrWhiteSpace($Destination)) {
        return
    }
    if (Test-Path -LiteralPath $Destination) {
        return
    }

    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $Destination) | Out-Null
    Invoke-WebRequest -Uri $Url -OutFile $Destination -UseBasicParsing
}

function ConvertTo-MagicCommandLine {
    param([string[]]$Arguments)

    $quoted = foreach ($argument in $Arguments) {
        $value = [string]$argument
        if ($value.Length -eq 0) {
            '""'
            continue
        }
        if ($value -notmatch '[\s"]') {
            $value
            continue
        }

        '"' + $value.Replace('"', '\"') + '"'
    }
    return [string]::Join(" ", $quoted)
}

function Write-MagicLaunchLog {
    param(
        [string]$Java,
        [string]$Arguments,
        [string]$WorkingDirectory
    )

    New-Item -ItemType Directory -Force -Path $MagicWorldDataRoot | Out-Null
    $content = @(
        "Time: $(Get-Date -Format o)",
        "Java: $Java",
        "WorkingDirectory: $WorkingDirectory",
        "Arguments: $Arguments"
    )
    Set-Content -LiteralPath $LaunchLogPath -Value $content -Encoding UTF8
}

function Ensure-MagicWorldRuntimeFiles {
    param($VersionJson)

    Update-Status "Verificando bibliotecas do Minecraft e Forge..." 35
    foreach ($library in $VersionJson.libraries) {
        if (!(Test-MagicRuleAllows $library.rules)) {
            continue
        }
        if ($library.artifact -and $library.artifact.path) {
            $destination = Resolve-MinecraftPath $library.artifact.path
            Invoke-MagicDownload -Url $library.artifact.url -Destination $destination
        } elseif ($library.downloads -and $library.downloads.artifact -and $library.downloads.artifact.path) {
            $destination = Resolve-MinecraftPath $library.downloads.artifact.path
            Invoke-MagicDownload -Url $library.downloads.artifact.url -Destination $destination
        }
    }

    if ($VersionJson.downloads -and $VersionJson.downloads.client -and $VersionJson.downloads.client.url) {
        $clientVersionId = $(if ($VersionJson.magicClientVersionId) { $VersionJson.magicClientVersionId } else { $VersionJson.id })
        $versionDir = Join-Path $MinecraftDir ("versions\" + $clientVersionId)
        $clientJar = Join-Path $versionDir ($clientVersionId + ".jar")
        Invoke-MagicDownload -Url $VersionJson.downloads.client.url -Destination $clientJar
    }

    if ($VersionJson.assetIndex -and $VersionJson.assetIndex.id -and $VersionJson.assetIndex.url) {
        Update-Status "Verificando assets do Minecraft..." 45
        $assetIndexPath = Join-Path $MinecraftDir ("assets\indexes\" + $VersionJson.assetIndex.id + ".json")
        Invoke-MagicDownload -Url $VersionJson.assetIndex.url -Destination $assetIndexPath
        $assetIndex = Get-Content -LiteralPath $assetIndexPath -Raw | ConvertFrom-Json
        $objectsRoot = Join-Path $MinecraftDir "assets\objects"
        $checked = 0
        foreach ($property in $assetIndex.objects.PSObject.Properties) {
            $hash = $property.Value.hash
            if ([string]::IsNullOrWhiteSpace($hash) -or $hash.Length -lt 2) {
                continue
            }
            $objectPath = Join-Path $objectsRoot (Join-Path $hash.Substring(0, 2) $hash)
            if (!(Test-Path -LiteralPath $objectPath)) {
                Invoke-MagicDownload -Url ("https://resources.download.minecraft.net/" + $hash.Substring(0, 2) + "/" + $hash) -Destination $objectPath
            }
            $checked++
            if ($checked % 250 -eq 0) {
                Update-Status "Verificando assets do Minecraft... $checked" 50
            }
        }
    }
}

function Ensure-MagicWorldNatives {
    param(
        $VersionJson,
        [string]$NativesDir
    )

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    foreach ($library in $VersionJson.libraries) {
        if (!(Test-MagicRuleAllows $library.rules)) {
            continue
        }

        $nativeArtifact = $null
        if ($library.natives -and $library.natives.windows -and $library.downloads -and $library.downloads.classifiers) {
            $classifierName = [string]$library.natives.windows
            $classifierName = $classifierName.Replace('${arch}', '64')
            $classifierProperty = $library.downloads.classifiers.PSObject.Properties | Where-Object { $_.Name -eq $classifierName } | Select-Object -First 1
            if ($classifierProperty -and $classifierProperty.Value.path) {
                $nativeArtifact = $classifierProperty.Value
            }
        } elseif ($library.downloads -and $library.downloads.artifact -and $library.downloads.artifact.path -and (Test-MagicNativeMatchesThisWindows ([string]$library.downloads.artifact.path))) {
            $nativeArtifact = $library.downloads.artifact
        } elseif ($library.artifact -and $library.artifact.path -and (Test-MagicNativeMatchesThisWindows ([string]$library.artifact.path))) {
            $nativeArtifact = $library.artifact
        } elseif ($library.name -and (Test-MagicNativeMatchesThisWindows ([string]$library.name)) -and $library.downloads -and $library.downloads.artifact) {
            $nativeArtifact = $library.downloads.artifact
        }

        if (!$nativeArtifact -or !$nativeArtifact.path) {
            continue
        }

        $nativeJar = Resolve-MinecraftPath $nativeArtifact.path
        Invoke-MagicDownload -Url $nativeArtifact.url -Destination $nativeJar
        if (!(Test-Path -LiteralPath $nativeJar)) {
            continue
        }

        $archive = [System.IO.Compression.ZipFile]::OpenRead($nativeJar)
        try {
            foreach ($entry in $archive.Entries) {
                if ([string]::IsNullOrWhiteSpace($entry.Name) -or $entry.FullName -like "META-INF/*") {
                    continue
                }
                $target = Join-Path $NativesDir $entry.Name
                [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
            }
        } finally {
            $archive.Dispose()
        }
    }
}

function Start-MagicWorldMinecraft {
    param([switch]$DryRun)

    Update-Status "Preparando Magic World..." 5
    if (!(Test-Path -LiteralPath (Join-Path $MinecraftDir "mods\Magic_World_Mod_1.20.1-1.0.0.1.jar"))) {
        Invoke-MagicWorldInstaller
    }

    $versionJsonPath = Get-MagicWorldVersionJsonPath
    if (!$versionJsonPath) {
        Invoke-MagicWorldInstaller
        $versionJsonPath = Get-MagicWorldVersionJsonPath
    }
    if (!$versionJsonPath) {
        throw "Forge 1.20.1 nao foi encontrado apos a instalacao."
    }

    $version = Ensure-MagicWorldClientInstall -VersionJsonPath $versionJsonPath

    Update-Status "Abrindo Minecraft Magic World..." 80
    $versionDir = Split-Path -Parent $versionJsonPath
    $nativesDir = Join-Path $versionDir "natives"

    $classpathItems = New-Object System.Collections.Generic.List[string]
    foreach ($library in $version.libraries) {
        if (!(Test-MagicRuleAllows $library.rules)) {
            continue
        }
        if (Test-MagicLibraryIsNative $library) {
            continue
        }
        $path = $null
        if ($library.artifact -and $library.artifact.path) {
            $path = Resolve-MinecraftPath $library.artifact.path
        } elseif ($library.downloads -and $library.downloads.artifact -and $library.downloads.artifact.path) {
            $path = Resolve-MinecraftPath $library.downloads.artifact.path
        }
        if ($path -and (Test-Path -LiteralPath $path)) {
            $classpathItems.Add($path)
        }
    }

    $clientVersionId = $(if ($version.magicClientVersionId) { $version.magicClientVersionId } else { $version.id })
    $clientJar = Join-Path (Join-Path $MinecraftDir ("versions\" + $clientVersionId)) ($clientVersionId + ".jar")
    if (!$version.magicInheritedProfile -and (Test-Path -LiteralPath $clientJar)) {
        $classpathItems.Add($clientJar)
    }
    $classpath = [string]::Join(";", $classpathItems)

    $account = Get-MagicWorldAccount
    $accountName = $account.username
    $uuid = $account.uuid
    if ([string]::IsNullOrWhiteSpace($uuid)) {
        $uuidSource = [Text.Encoding]::UTF8.GetBytes("OfflinePlayer:$accountName")
        $md5 = [Security.Cryptography.MD5]::Create().ComputeHash($uuidSource)
        $uuid = (New-Object Guid -ArgumentList (, $md5)).ToString()
    }

    $settings = Get-MagicWorldSettings
    $replacements = @{
        '${natives_directory}' = $nativesDir
        '${launcher_name}' = 'MagicWorldLauncher'
        '${launcher_version}' = '1.0.0'
        '${classpath}' = $classpath
        '${classpath_separator}' = ';'
        '${library_directory}' = (Join-Path $MinecraftDir 'libraries')
        '${version_name}' = $version.id
        '${auth_player_name}' = $accountName
        '${game_directory}' = $MinecraftDir
        '${assets_root}' = (Join-Path $MinecraftDir 'assets')
        '${assets_index_name}' = $version.assetIndex.id
        '${auth_uuid}' = $uuid
        '${auth_access_token}' = $account.accessToken
        '${clientid}' = '0'
        '${auth_xuid}' = '0'
        '${user_type}' = $account.userType
        '${version_type}' = 'Magic World'
        '${resolution_width}' = [string]$settings.resolutionWidth
        '${resolution_height}' = [string]$settings.resolutionHeight
    }

    $jvmArgs = New-Object System.Collections.ArrayList
    [void]$jvmArgs.Add("-Xms$($settings.minMemoryGb)G")
    [void]$jvmArgs.Add("-Xmx$($settings.maxMemoryGb)G")
    Add-MagicArguments -Target $jvmArgs -Items $version.arguments.jvm
    [void]$jvmArgs.Add($version.mainClass)

    $gameArgs = New-Object System.Collections.ArrayList
    Add-MagicArguments -Target $gameArgs -Items $version.arguments.game

    $allArgs = @($jvmArgs + $gameArgs) | ForEach-Object {
        $value = [string]$_
        foreach ($key in $replacements.Keys) {
            $value = $value.Replace($key, [string]$replacements[$key])
        }
        $value
    }

    $java = Get-MagicJavaExePath
    if ($DryRun) {
        $unresolvedArguments = @($allArgs | Where-Object { [string]$_ -match '\$\{[^}]+\}' })
        return [pscustomobject]@{
            java = $java
            workingDirectory = $MinecraftDir
            mainClass = $version.mainClass
            version = $version.id
            clientVersion = $clientVersionId
            classpathEntries = $classpathItems.Count
            argumentCount = $allArgs.Count
            unresolvedArgumentCount = $unresolvedArguments.Count
            opensTLauncher = $false
        }
    }

    $argumentLine = ConvertTo-MagicCommandLine -Arguments $allArgs
    Write-MagicLaunchLog -Java $java -Arguments $argumentLine -WorkingDirectory $MinecraftDir
    $process = Start-Process -FilePath $java `
        -ArgumentList $argumentLine `
        -WorkingDirectory $MinecraftDir `
        -WindowStyle Hidden `
        -RedirectStandardOutput $LaunchOutLogPath `
        -RedirectStandardError $LaunchErrLogPath `
        -PassThru
    Start-Sleep -Seconds 5
    if ($process.HasExited -and $process.ExitCode -ne 0) {
        $errorText = ""
        if (Test-Path -LiteralPath $LaunchErrLogPath) {
            $errorText = (Get-Content -LiteralPath $LaunchErrLogPath -Tail 20 -ErrorAction SilentlyContinue) -join "`n"
        }
        if ([string]::IsNullOrWhiteSpace($errorText)) {
            $errorText = "Processo Java encerrou com codigo $($process.ExitCode). Veja $LaunchErrLogPath."
        }
        throw "Minecraft fechou ao iniciar: $errorText"
    }
    Update-Status "Minecraft Magic World iniciado." 100
    return $process
}

function Ensure-MagicWorldClientInstall {
    param([string]$VersionJsonPath)

    if ([string]::IsNullOrWhiteSpace($VersionJsonPath) -or !(Test-Path -LiteralPath $VersionJsonPath)) {
        throw "Forge 1.20.1 nao foi encontrado para preparar o cliente Minecraft."
    }

    Ensure-MagicJavaRuntime | Out-Null
    $version = Get-MagicWorldVersionJson -VersionJsonPath $VersionJsonPath
    Ensure-MagicWorldRuntimeFiles -VersionJson $version

    $versionDir = Split-Path -Parent $VersionJsonPath
    $nativesDir = Join-Path $versionDir "natives"
    New-Item -ItemType Directory -Force -Path $nativesDir | Out-Null
    Ensure-MagicWorldNatives -VersionJson $version -NativesDir $nativesDir

    return $version
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
    $javaExe = Get-MagicJavaExePath
    $javaHome = Split-Path -Parent (Split-Path -Parent $javaExe)
    $oldJavaHome = $env:JAVA_HOME
    $env:JAVA_HOME = $javaHome
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
    try {
        $process = Start-Process -FilePath "powershell.exe" -ArgumentList $installArgs -WorkingDirectory $LauncherRoot -WindowStyle Hidden -Wait -PassThru
        if ($process.ExitCode -ne 0) {
            throw "Instalacao retornou codigo $($process.ExitCode). Log: $env:TEMP\magicworld-forge-installer.log"
        }
    } finally {
        $env:JAVA_HOME = $oldJavaHome
    }

    Update-Status "Baixando cliente Minecraft, assets e bibliotecas..." 86
    $versionJsonPath = Get-MagicWorldVersionJsonPath
    if (!$versionJsonPath) {
        throw "Forge 1.20.1 nao foi encontrado apos a instalacao."
    }
    Ensure-MagicWorldClientInstall -VersionJsonPath $versionJsonPath | Out-Null
    Set-MagicWorldFolderIcon -Path $MagicWorldDataRoot
    Set-MagicWorldFolderIcon -Path $MinecraftDir
    Ensure-MagicWorldServerFiles

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

function Open-MagicWorldMinecraftFolder {
    New-Item -ItemType Directory -Force -Path $MinecraftDir | Out-Null
    Set-MagicWorldFolderIcon -Path $MinecraftDir
    Start-Process -FilePath "explorer.exe" -ArgumentList $MinecraftDir
}

function Set-MagicWorldFolderIcon {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path) -or !(Test-Path -LiteralPath $Path) -or !(Test-Path -LiteralPath $WindowIconPath)) {
        return
    }

    try {
        $targetIcon = Join-Path $Path "MagicWorldLauncher.ico"
        Copy-Item -LiteralPath $WindowIconPath -Destination $targetIcon -Force
        $desktopIni = Join-Path $Path "desktop.ini"
        Set-Content -LiteralPath $desktopIni -Encoding ASCII -Value "[.ShellClassInfo]`r`nIconResource=MagicWorldLauncher.ico,0`r`n"
        $iniItem = Get-Item -LiteralPath $desktopIni
        $iniItem.Attributes = [System.IO.FileAttributes]::Hidden -bor [System.IO.FileAttributes]::System
        $folderItem = Get-Item -LiteralPath $Path
        $folderItem.Attributes = $folderItem.Attributes -bor [System.IO.FileAttributes]::System
    } catch {
    }
}

function Write-MagicNbtShort {
    param(
        [System.IO.BinaryWriter]$Writer,
        [int]$Value
    )
    $Writer.Write([byte](($Value -shr 8) -band 0xff))
    $Writer.Write([byte]($Value -band 0xff))
}

function Write-MagicNbtInt {
    param(
        [System.IO.BinaryWriter]$Writer,
        [int]$Value
    )
    $Writer.Write([byte](($Value -shr 24) -band 0xff))
    $Writer.Write([byte](($Value -shr 16) -band 0xff))
    $Writer.Write([byte](($Value -shr 8) -band 0xff))
    $Writer.Write([byte]($Value -band 0xff))
}

function Write-MagicNbtStringPayload {
    param(
        [System.IO.BinaryWriter]$Writer,
        [string]$Value
    )
    $bytes = [System.Text.Encoding]::UTF8.GetBytes([string]$Value)
    Write-MagicNbtShort -Writer $Writer -Value $bytes.Length
    $Writer.Write($bytes)
}

function Write-MagicNbtNamedString {
    param(
        [System.IO.BinaryWriter]$Writer,
        [string]$Name,
        [string]$Value
    )
    $Writer.Write([byte]8)
    Write-MagicNbtStringPayload -Writer $Writer -Value $Name
    Write-MagicNbtStringPayload -Writer $Writer -Value $Value
}

function Save-MagicWorldServersDat {
    param($Servers)

    New-Item -ItemType Directory -Force -Path $MinecraftDir | Out-Null
    $stream = [System.IO.File]::Create($MinecraftServerListPath)
    try {
        $writer = New-Object System.IO.BinaryWriter($stream)
        try {
            $serverItems = @($Servers | Where-Object { $_.name -and $_.ip })
            $writer.Write([byte]10)
            Write-MagicNbtStringPayload -Writer $writer -Value ""
            $writer.Write([byte]9)
            Write-MagicNbtStringPayload -Writer $writer -Value "servers"
            $writer.Write([byte]10)
            Write-MagicNbtInt -Writer $writer -Value $serverItems.Count
            foreach ($server in $serverItems) {
                Write-MagicNbtNamedString -Writer $writer -Name "name" -Value ([string]$server.name)
                Write-MagicNbtNamedString -Writer $writer -Name "ip" -Value ([string]$server.ip)
                $writer.Write([byte]0)
            }
            $writer.Write([byte]0)
        } finally {
            $writer.Dispose()
        }
    } finally {
        $stream.Dispose()
    }
}

function Get-MagicWorldServerFavorites {
    if (!(Test-Path -LiteralPath $ServerFavoritesPath)) {
        return @()
    }

    try {
        $json = Get-Content -LiteralPath $ServerFavoritesPath -Raw | ConvertFrom-Json
        return @($json | Where-Object { $_.name -and $_.ip })
    } catch {
        return @()
    }
}

function Save-MagicWorldServerFavorites {
    param($Servers)

    New-Item -ItemType Directory -Force -Path $MagicWorldDataRoot | Out-Null
    $clean = @($Servers | Where-Object { $_.name -and $_.ip } | ForEach-Object {
        [pscustomobject]@{
            name = ([string]$_.name).Trim()
            ip = ([string]$_.ip).Trim()
        }
    })
    $clean | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $ServerFavoritesPath -Encoding UTF8
    Save-MagicWorldServersDat -Servers $clean
    Save-MagicWorldServerHelp
}

function Add-MagicWorldServerFavorite {
    param(
        [string]$Name,
        [string]$Address
    )

    $cleanName = ([string]$Name).Trim()
    $cleanAddress = ([string]$Address).Trim()
    if ([string]::IsNullOrWhiteSpace($cleanName)) {
        $cleanName = "Servidor Magic World"
    }
    if ([string]::IsNullOrWhiteSpace($cleanAddress)) {
        throw "Informe o endereco do servidor, por exemplo 192.168.0.25:25565."
    }
    if ($cleanAddress -notmatch '^[A-Za-z0-9_\.\-:]+$') {
        throw "Endereco invalido. Use IP/dominio e porta, por exemplo 192.168.0.25:25565."
    }

    $servers = New-Object System.Collections.ArrayList
    foreach ($server in Get-MagicWorldServerFavorites) {
        if ([string]$server.ip -ne $cleanAddress) {
            [void]$servers.Add($server)
        }
    }
    [void]$servers.Add([pscustomobject]@{ name = $cleanName; ip = $cleanAddress })
    Save-MagicWorldServerFavorites -Servers $servers
}

function Save-MagicWorldServerHelp {
    New-Item -ItemType Directory -Force -Path $MagicWorldDataRoot | Out-Null
    $content = @(
        "Servidores Magic World",
        "",
        "Pasta exclusiva do jogo: $MinecraftDir",
        "Lista lida pelo Minecraft: $MinecraftServerListPath",
        "",
        "Servidor local no mesmo PC: 127.0.0.1:25565",
        "Servidor em outro computador da rede: IP_DA_MAQUINA:25565, por exemplo 192.168.0.25:25565",
        "Servidor de amigo: dominio-ou-ip:porta",
        "",
        "Conta offline entra apenas em servidores que aceitam offline/cracked.",
        "Servidor premium com online-mode=true exige autenticacao Microsoft/TLauncher valida."
    )
    Set-Content -LiteralPath $ServerHelpPath -Value $content -Encoding UTF8
}

function Ensure-MagicWorldServerFiles {
    Save-MagicWorldServerHelp
    $servers = Get-MagicWorldServerFavorites
    if ($servers.Count -gt 0) {
        Save-MagicWorldServersDat -Servers $servers
    }
}

function Show-TLauncherApiLoginDialog {
    $dialog = New-Object System.Windows.Window
    $dialog.Title = "Login TLauncher"
    $dialog.Width = 460
    $dialog.Height = 310
    $dialog.WindowStartupLocation = "CenterOwner"
    $dialog.ResizeMode = "NoResize"
    $dialog.Background = "#101820"
    $dialog.Owner = $window
    if (Test-Path -LiteralPath $WindowIconPath) {
        $dialog.Icon = [System.Windows.Media.Imaging.BitmapImage]::new([Uri]$WindowIconPath)
    }

    $panel = New-Object System.Windows.Controls.StackPanel
    $panel.Margin = "24"
    $dialog.Content = $panel

    $label = New-Object System.Windows.Controls.TextBlock
    $label.Text = "Entre com sua conta TLauncher."
    $label.Foreground = "White"
    $label.TextWrapping = "Wrap"
    $label.Margin = "0,0,0,14"
    $panel.Children.Add($label) | Out-Null

    $savedAccount = Get-MagicWorldAccount

    $userLabel = New-Object System.Windows.Controls.TextBlock
    $userLabel.Text = "Usuario"
    $userLabel.Foreground = "#FFDAA520"
    $userLabel.Margin = "0,0,0,4"
    $panel.Children.Add($userLabel) | Out-Null

    $username = New-Object System.Windows.Controls.TextBox
    $username.Text = Get-MagicWorldAccountName
    $username.Height = 30
    $username.Margin = "0,0,0,10"
    $panel.Children.Add($username) | Out-Null

    $passwordLabel = New-Object System.Windows.Controls.TextBlock
    $passwordLabel.Text = "Senha"
    $passwordLabel.Foreground = "#FFDAA520"
    $passwordLabel.Margin = "0,0,0,4"
    $panel.Children.Add($passwordLabel) | Out-Null

    $password = New-Object System.Windows.Controls.PasswordBox
    $password.Password = Get-SavedTLauncherPassword
    $password.Height = 30
    $password.Margin = "0,0,0,8"
    $panel.Children.Add($password) | Out-Null

    $savePassword = New-Object System.Windows.Controls.CheckBox
    $savePassword.Content = "Salvar senha neste Windows"
    $savePassword.Foreground = "White"
    $savePassword.IsChecked = [bool]$savedAccount.savedPassword
    $savePassword.Margin = "0,0,0,16"
    $panel.Children.Add($savePassword) | Out-Null

    $buttons = New-Object System.Windows.Controls.StackPanel
    $buttons.Orientation = "Horizontal"
    $buttons.HorizontalAlignment = "Right"
    $panel.Children.Add($buttons) | Out-Null

    $cancel = New-Object System.Windows.Controls.Button
    $cancel.Content = "Cancelar"
    $cancel.Width = 95
    $cancel.Height = 32
    $cancel.Margin = "0,0,8,0"
    $cancel.Add_Click({ $dialog.DialogResult = $false })
    $buttons.Children.Add($cancel) | Out-Null

    $ok = New-Object System.Windows.Controls.Button
    $ok.Content = "Entrar"
    $ok.Width = 95
    $ok.Height = 32
    $ok.Add_Click({
        try {
            $apiUrl = $(if ($savedAccount.authApiUrl) { [string]$savedAccount.authApiUrl } else { $TLauncherAuthApiUrl })
            if ([string]::IsNullOrWhiteSpace($apiUrl)) {
                Save-MagicWorldAccount -Username $username.Text -LoginProvider "offline" | Out-Null
                Remove-SavedTLauncherPassword
                [System.Windows.MessageBox]::Show("API oficial do TLauncher nao configurada. Usuario salvo em modo offline para manter o jogo funcional.", "Login TLauncher", "OK", "Information") | Out-Null
                $dialog.DialogResult = $true
                return
            }
            Invoke-TLauncherApiLogin -Username $username.Text -Password $password.Password -ApiUrl $apiUrl | Out-Null
            if ($savePassword.IsChecked) {
                Save-TLauncherPassword -Password $password.Password
            } else {
                Remove-SavedTLauncherPassword
            }
            $dialog.DialogResult = $true
        } catch {
            [System.Windows.MessageBox]::Show($_.Exception.Message, "Login TLauncher", "OK", "Error") | Out-Null
        }
    })
    $buttons.Children.Add($ok) | Out-Null

    return $dialog.ShowDialog()
}

function Show-MagicWorldSettingsDialog {
    $settings = Get-MagicWorldSettings
    $dialog = New-Object System.Windows.Window
    $dialog.Title = "Configuracoes Magic World"
    $dialog.Width = 560
    $dialog.Height = 430
    $dialog.WindowStartupLocation = "CenterOwner"
    $dialog.ResizeMode = "NoResize"
    $dialog.Background = "#101820"
    $dialog.Owner = $window
    if (Test-Path -LiteralPath $WindowIconPath) {
        $dialog.Icon = [System.Windows.Media.Imaging.BitmapImage]::new([Uri]$WindowIconPath)
    }

    $panel = New-Object System.Windows.Controls.StackPanel
    $panel.Margin = "26"
    $dialog.Content = $panel

    $title = New-Object System.Windows.Controls.TextBlock
    $title.Text = "Configuracoes"
    $title.FontSize = 24
    $title.FontWeight = "Bold"
    $title.Foreground = "White"
    $title.Margin = "0,0,0,18"
    $panel.Children.Add($title) | Out-Null

    $ramHeader = New-Object System.Windows.Controls.DockPanel
    $ramHeader.Margin = "0,0,0,6"
    $panel.Children.Add($ramHeader) | Out-Null

    $ramLabel = New-Object System.Windows.Controls.TextBlock
    $ramLabel.Text = "Memoria RAM"
    $ramLabel.Foreground = "#FFDAA520"
    $ramLabel.FontSize = 15
    [System.Windows.Controls.DockPanel]::SetDock($ramLabel, "Left")
    $ramHeader.Children.Add($ramLabel) | Out-Null

    $ramValue = New-Object System.Windows.Controls.TextBlock
    $ramValue.Foreground = "White"
    $ramValue.FontSize = 15
    $ramValue.HorizontalAlignment = "Right"
    [System.Windows.Controls.DockPanel]::SetDock($ramValue, "Right")
    $ramHeader.Children.Add($ramValue) | Out-Null

    $ramSlider = New-Object System.Windows.Controls.Slider
    $ramSlider.Minimum = 2
    $ramSlider.Maximum = 16
    $ramSlider.Value = [Math]::Max(2, [Math]::Min(16, [int]$settings.maxMemoryGb))
    $ramSlider.TickFrequency = 1
    $ramSlider.IsSnapToTickEnabled = $true
    $ramSlider.TickPlacement = "BottomRight"
    $ramSlider.Margin = "0,0,0,22"
    $ramValue.Text = "$([int]$ramSlider.Value) GB"
    $ramSlider.Add_ValueChanged({ $ramValue.Text = "$([int]$ramSlider.Value) GB" })
    $panel.Children.Add($ramSlider) | Out-Null

    $resolutionBox = New-Object System.Windows.Controls.CheckBox
    $resolutionBox.Content = "Resolucao personalizada"
    $resolutionBox.Foreground = "White"
    $resolutionBox.IsChecked = [bool]$settings.customResolution
    $resolutionBox.Margin = "0,0,0,10"
    $panel.Children.Add($resolutionBox) | Out-Null

    $resolutionPanel = New-Object System.Windows.Controls.StackPanel
    $resolutionPanel.Orientation = "Horizontal"
    $resolutionPanel.Margin = "0,0,0,18"
    $panel.Children.Add($resolutionPanel) | Out-Null

    $widthBox = New-Object System.Windows.Controls.TextBox
    $widthBox.Text = [string]$settings.resolutionWidth
    $widthBox.Width = 90
    $widthBox.Height = 30
    $widthBox.Margin = "0,0,8,0"
    $resolutionPanel.Children.Add($widthBox) | Out-Null

    $heightBox = New-Object System.Windows.Controls.TextBox
    $heightBox.Text = [string]$settings.resolutionHeight
    $heightBox.Width = 90
    $heightBox.Height = 30
    $heightBox.Margin = "0,0,12,0"
    $resolutionPanel.Children.Add($heightBox) | Out-Null

    $hideLauncherBox = New-Object System.Windows.Controls.CheckBox
    $hideLauncherBox.Content = "Ocultar launcher enquanto o Minecraft roda"
    $hideLauncherBox.Foreground = "White"
    $hideLauncherBox.IsChecked = [bool]$settings.minimizeLauncherOnPlay
    $hideLauncherBox.Margin = "0,0,0,14"
    $panel.Children.Add($hideLauncherBox) | Out-Null

    $folder = New-Object System.Windows.Controls.TextBlock
    $folder.Text = "Pasta isolada: $MinecraftDir"
    $folder.Foreground = "#D0D8E8"
    $folder.TextWrapping = "Wrap"
    $folder.Margin = "0,0,0,22"
    $panel.Children.Add($folder) | Out-Null

    $buttons = New-Object System.Windows.Controls.StackPanel
    $buttons.Orientation = "Horizontal"
    $buttons.HorizontalAlignment = "Right"
    $panel.Children.Add($buttons) | Out-Null

    $cancel = New-Object System.Windows.Controls.Button
    $cancel.Content = "Cancelar"
    $cancel.Width = 100
    $cancel.Height = 34
    $cancel.Margin = "0,0,8,0"
    $cancel.Add_Click({ $dialog.DialogResult = $false })
    $buttons.Children.Add($cancel) | Out-Null

    $save = New-Object System.Windows.Controls.Button
    $save.Content = "Salvar"
    $save.Width = 100
    $save.Height = 34
    $save.Add_Click({
        try {
            Save-MagicWorldSettings `
                -MinMemoryGb 2 `
                -MaxMemoryGb ([int]$ramSlider.Value) `
                -CustomResolution ([bool]$resolutionBox.IsChecked) `
                -ResolutionWidth ([int]$widthBox.Text) `
                -ResolutionHeight ([int]$heightBox.Text) `
                -MinimizeLauncherOnPlay ([bool]$hideLauncherBox.IsChecked) | Out-Null
            $dialog.DialogResult = $true
        } catch {
            [System.Windows.MessageBox]::Show($_.Exception.Message, "Configuracoes Magic World", "OK", "Error") | Out-Null
        }
    })
    $buttons.Children.Add($save) | Out-Null

    return $dialog.ShowDialog()
}

function Show-MagicWorldServersDialog {
    Ensure-MagicWorldServerFiles

    $dialog = New-Object System.Windows.Window
    $dialog.Title = "Servidores Magic World"
    $dialog.Width = 600
    $dialog.Height = 450
    $dialog.WindowStartupLocation = "CenterOwner"
    $dialog.ResizeMode = "NoResize"
    $dialog.Background = "#101820"
    $dialog.Owner = $window
    if (Test-Path -LiteralPath $WindowIconPath) {
        $dialog.Icon = [System.Windows.Media.Imaging.BitmapImage]::new([Uri]$WindowIconPath)
    }

    $panel = New-Object System.Windows.Controls.StackPanel
    $panel.Margin = "26"
    $dialog.Content = $panel

    $title = New-Object System.Windows.Controls.TextBlock
    $title.Text = "Servidores"
    $title.FontSize = 24
    $title.FontWeight = "Bold"
    $title.Foreground = "White"
    $title.Margin = "0,0,0,10"
    $panel.Children.Add($title) | Out-Null

    $hint = New-Object System.Windows.Controls.TextBlock
    $hint.Text = "Use IP:porta. Local no mesmo PC: 127.0.0.1:25565. Outro PC da rede: IP_DA_MAQUINA:25565."
    $hint.Foreground = "#D0D8E8"
    $hint.TextWrapping = "Wrap"
    $hint.Margin = "0,0,0,14"
    $panel.Children.Add($hint) | Out-Null

    $list = New-Object System.Windows.Controls.ListBox
    $list.Height = 120
    $list.Margin = "0,0,0,16"
    $panel.Children.Add($list) | Out-Null

    function Refresh-ServerListBox {
        $list.Items.Clear()
        foreach ($server in Get-MagicWorldServerFavorites) {
            $list.Items.Add("$($server.name) - $($server.ip)") | Out-Null
        }
        if ($list.Items.Count -eq 0) {
            $list.Items.Add("Nenhum servidor salvo ainda.") | Out-Null
        }
    }

    $nameLabel = New-Object System.Windows.Controls.TextBlock
    $nameLabel.Text = "Nome"
    $nameLabel.Foreground = "#FFDAA520"
    $nameLabel.Margin = "0,0,0,4"
    $panel.Children.Add($nameLabel) | Out-Null

    $nameBox = New-Object System.Windows.Controls.TextBox
    $nameBox.Text = "Servidor Magic World"
    $nameBox.Height = 30
    $nameBox.Margin = "0,0,0,10"
    $panel.Children.Add($nameBox) | Out-Null

    $addressLabel = New-Object System.Windows.Controls.TextBlock
    $addressLabel.Text = "Endereco"
    $addressLabel.Foreground = "#FFDAA520"
    $addressLabel.Margin = "0,0,0,4"
    $panel.Children.Add($addressLabel) | Out-Null

    $addressBox = New-Object System.Windows.Controls.TextBox
    $addressBox.Text = "192.168.0.25:25565"
    $addressBox.Height = 30
    $addressBox.Margin = "0,0,0,16"
    $panel.Children.Add($addressBox) | Out-Null

    $buttons = New-Object System.Windows.Controls.StackPanel
    $buttons.Orientation = "Horizontal"
    $buttons.HorizontalAlignment = "Right"
    $panel.Children.Add($buttons) | Out-Null

    $openFolder = New-Object System.Windows.Controls.Button
    $openFolder.Content = "Abrir pasta"
    $openFolder.Width = 105
    $openFolder.Height = 34
    $openFolder.Margin = "0,0,8,0"
    $openFolder.Add_Click({
        Ensure-MagicWorldServerFiles
        Open-MagicWorldMinecraftFolder
    })
    $buttons.Children.Add($openFolder) | Out-Null

    $add = New-Object System.Windows.Controls.Button
    $add.Content = "Adicionar"
    $add.Width = 105
    $add.Height = 34
    $add.Margin = "0,0,8,0"
    $add.Add_Click({
        try {
            Add-MagicWorldServerFavorite -Name $nameBox.Text -Address $addressBox.Text
            Refresh-ServerListBox
            Update-Status "Servidor salvo em servers.dat da pasta exclusiva." 0
        } catch {
            [System.Windows.MessageBox]::Show($_.Exception.Message, "Servidores Magic World", "OK", "Error") | Out-Null
        }
    })
    $buttons.Children.Add($add) | Out-Null

    $close = New-Object System.Windows.Controls.Button
    $close.Content = "Fechar"
    $close.Width = 95
    $close.Height = 34
    $close.Add_Click({ $dialog.DialogResult = $true })
    $buttons.Children.Add($close) | Out-Null

    Refresh-ServerListBox
    return $dialog.ShowDialog()
}

if ($SelfTest) {
    $javaPath = ""
    try {
        $javaPath = Get-MagicJavaPath
    } catch {
        $javaPath = ""
    }

    $result = [ordered]@{
        launcherRoot = $LauncherRoot
        backgroundExists = Test-Path -LiteralPath $BackgroundPath
        logoExists = Test-Path -LiteralPath $LogoPath
        installerScriptExists = Test-Path -LiteralPath $InstallerScriptPath
        bundledInstallerExists = Test-Path -LiteralPath $BundledInstallerPath
        magicWorldDataRoot = $MagicWorldDataRoot
        minecraftDir = $MinecraftDir
        serverFavorites = $ServerFavoritesPath
        minecraftServerList = $MinecraftServerListPath
        accountName = Get-MagicWorldAccountName
        accountProvider = (Get-MagicWorldAccount).loginProvider
        tLauncherAuthApiConfigured = ![string]::IsNullOrWhiteSpace($TLauncherAuthApiUrl)
        versionJson = Get-MagicWorldVersionJsonPath
        java = $javaPath
        installerUrl = Get-InstallerUrl
    }
    $result | ConvertTo-Json -Depth 4
    exit 0
}

if ($LaunchDryRun) {
    $result = Start-MagicWorldMinecraft -DryRun
    $result | ConvertTo-Json -Depth 4
    exit 0
}

if ($InstallOnly) {
    Invoke-MagicWorldInstaller
    exit 0
}

if ($LaunchOnly) {
    Start-MagicWorldMinecraft | Out-Null
    exit 0
}

Add-Type -AssemblyName PresentationFramework
Add-Type -AssemblyName System.Windows.Forms
Add-Type @"
using System;
using System.Runtime.InteropServices;
public static class MagicWorldTaskbar {
    [DllImport("shell32.dll", CharSet = CharSet.Unicode)]
    public static extern int SetCurrentProcessExplicitAppUserModelID(string appID);
}
"@
[MagicWorldTaskbar]::SetCurrentProcessExplicitAppUserModelID("MagicWorld.Launcher") | Out-Null

$window = New-Object System.Windows.Window
$window.Title = "Magic World Launcher"
$window.Width = 980
$window.Height = 620
$window.WindowStartupLocation = "CenterScreen"
$window.ResizeMode = "CanMinimize"
if (Test-Path -LiteralPath $WindowIconPath) {
    $window.Icon = [System.Windows.Media.Imaging.BitmapImage]::new([Uri]$WindowIconPath)
}

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

$layout = New-Object System.Windows.Controls.DockPanel
$layout.Margin = "30"
$layout.LastChildFill = $true
$grid.Children.Add($layout) | Out-Null

function New-MagicButton {
    param(
        [string]$Text,
        [int]$Width = 150,
        [int]$Height = 40,
        [switch]$Primary
    )
    $button = New-Object System.Windows.Controls.Button
    $button.Content = $Text
    $button.Width = $Width
    $button.Height = $Height
    $button.Margin = "6"
    $button.FontSize = $(if ($Primary) { 17 } else { 13 })
    $button.FontWeight = "SemiBold"
    $button.Foreground = "White"
    $button.Background = $(if ($Primary) { "#D4A520" } else { "#D0101820" })
    $button.BorderBrush = $(if ($Primary) { "#FFF1C85B" } else { "#B0DAA520" })
    $button.BorderThickness = "1.5"
    return $button
}

function New-MagicBitmapImage {
    param([string]$Path)
    $bitmap = New-Object System.Windows.Media.Imaging.BitmapImage
    $bitmap.BeginInit()
    $bitmap.UriSource = New-Object System.Uri($Path)
    $bitmap.CacheOption = [System.Windows.Media.Imaging.BitmapCacheOption]::OnLoad
    $bitmap.EndInit()
    return $bitmap
}

$footer = New-Object System.Windows.Controls.Border
$footer.Background = "#D0101820"
$footer.BorderBrush = "#66DAA520"
$footer.BorderThickness = "1"
$footer.Padding = "14,10,14,10"
$footer.Margin = "0,14,0,0"
[System.Windows.Controls.DockPanel]::SetDock($footer, "Bottom")
$layout.Children.Add($footer) | Out-Null

$footerPanel = New-Object System.Windows.Controls.DockPanel
$footerPanel.LastChildFill = $false
$footer.Child = $footerPanel

$accountPanel = New-Object System.Windows.Controls.StackPanel
$accountPanel.VerticalAlignment = "Center"
[System.Windows.Controls.DockPanel]::SetDock($accountPanel, "Left")
$footerPanel.Children.Add($accountPanel) | Out-Null

$accountCaption = New-Object System.Windows.Controls.TextBlock
$accountCaption.Text = "Conta"
$accountCaption.Foreground = "#FFDAA520"
$accountCaption.FontSize = 12
$accountPanel.Children.Add($accountCaption) | Out-Null

$script:AccountText = New-Object System.Windows.Controls.TextBlock
$script:AccountText.Foreground = "White"
$script:AccountText.FontSize = 15
$script:AccountText.FontWeight = "SemiBold"
$accountPanel.Children.Add($script:AccountText) | Out-Null

function Update-MagicAccountFooter {
    $accountInfo = Get-MagicWorldAccount
    if ($script:AccountText) {
        $script:AccountText.Text = "$($accountInfo.username) [$($accountInfo.loginProvider)]"
    }
}

$footerButtons = New-Object System.Windows.Controls.StackPanel
$footerButtons.Orientation = "Horizontal"
$footerButtons.HorizontalAlignment = "Right"
$footerButtons.VerticalAlignment = "Center"
[System.Windows.Controls.DockPanel]::SetDock($footerButtons, "Right")
$footerPanel.Children.Add($footerButtons) | Out-Null

$loginButton = New-MagicButton "Login" 84 38
$serversButton = New-MagicButton "Servidores" 108 38
$folderButton = New-MagicButton ".minecraft" 108 38
$settingsButton = New-MagicButton "Configuracoes" 126 38
$playButton = New-MagicButton "Jogar Magic World" 194 48 -Primary

$footerButtons.Children.Add($loginButton) | Out-Null
$footerButtons.Children.Add($serversButton) | Out-Null
$footerButtons.Children.Add($folderButton) | Out-Null
$footerButtons.Children.Add($settingsButton) | Out-Null
$footerButtons.Children.Add($playButton) | Out-Null

$progressPanel = New-Object System.Windows.Controls.StackPanel
$progressPanel.Width = 720
$progressPanel.HorizontalAlignment = "Center"
$progressPanel.Margin = "0,0,0,4"
[System.Windows.Controls.DockPanel]::SetDock($progressPanel, "Bottom")
$layout.Children.Add($progressPanel) | Out-Null

$script:StatusText = New-Object System.Windows.Controls.TextBlock
$script:StatusText.Text = "Pronto para jogar. O Minecraft do Magic World usa uma pasta exclusiva."
$script:StatusText.FontSize = 16
$script:StatusText.Foreground = "White"
$script:StatusText.TextAlignment = "Center"
$script:StatusText.TextWrapping = "Wrap"
$script:StatusText.Margin = "0,0,0,8"
$progressPanel.Children.Add($script:StatusText) | Out-Null

$progressRow = New-Object System.Windows.Controls.DockPanel
$progressRow.LastChildFill = $true
$progressPanel.Children.Add($progressRow) | Out-Null

$script:ProgressPercentText = New-Object System.Windows.Controls.TextBlock
$script:ProgressPercentText.Text = "0%"
$script:ProgressPercentText.Width = 54
$script:ProgressPercentText.Foreground = "#FFDAA520"
$script:ProgressPercentText.FontSize = 15
$script:ProgressPercentText.FontWeight = "Bold"
$script:ProgressPercentText.TextAlignment = "Right"
$script:ProgressPercentText.VerticalAlignment = "Center"
[System.Windows.Controls.DockPanel]::SetDock($script:ProgressPercentText, "Right")
$progressRow.Children.Add($script:ProgressPercentText) | Out-Null

$script:ProgressBar = New-Object System.Windows.Controls.ProgressBar
$script:ProgressBar.Minimum = 0
$script:ProgressBar.Maximum = 100
$script:ProgressBar.Value = 0
$script:ProgressBar.Height = 16
$script:ProgressBar.Margin = "0,0,10,0"
$progressRow.Children.Add($script:ProgressBar) | Out-Null

$hero = New-Object System.Windows.Controls.StackPanel
$hero.Width = 720
$hero.HorizontalAlignment = "Center"
$hero.VerticalAlignment = "Center"
$layout.Children.Add($hero) | Out-Null

if (Test-Path -LiteralPath $LogoPath) {
    $logo = New-Object System.Windows.Controls.Image
    $logo.Source = New-MagicBitmapImage -Path $LogoPath
    $logo.Width = 610
    $logo.Height = 205
    $logo.Stretch = "Uniform"
    $logo.Margin = "0,0,0,10"
    $hero.Children.Add($logo) | Out-Null
}

$title = New-Object System.Windows.Controls.TextBlock
$title.Text = "Magic World Forge 1.20.1"
$title.FontSize = 32
$title.FontWeight = "Bold"
$title.Foreground = "White"
$title.TextAlignment = "Center"
$title.Margin = "0,0,0,8"
$hero.Children.Add($title) | Out-Null

$subtitle = New-Object System.Windows.Controls.TextBlock
$subtitle.Text = "Launcher completo, Forge e pacote Magic World em ambiente separado."
$subtitle.FontSize = 16
$subtitle.Foreground = "#E9EEF7"
$subtitle.TextAlignment = "Center"
$subtitle.TextWrapping = "Wrap"
$hero.Children.Add($subtitle) | Out-Null

Update-MagicAccountFooter

$folderButton.Add_Click({ Open-MagicWorldMinecraftFolder })
$serversButton.Add_Click({
    Show-MagicWorldServersDialog | Out-Null
})
$settingsButton.Add_Click({
    if (Show-MagicWorldSettingsDialog) {
        Update-Status "Configuracoes salvas." 0
    }
})
$loginButton.Add_Click({
    try {
        if (Show-TLauncherApiLoginDialog) {
            Update-MagicAccountFooter
            Update-Status "Conta salva. Pronto para jogar." 0
        }
    } catch {
        Update-Status "Erro: $($_.Exception.Message)" 0
        [System.Windows.MessageBox]::Show($_.Exception.Message, "Magic World Launcher", "OK", "Error") | Out-Null
    }
})
$playButton.Add_Click({
    $playButton.IsEnabled = $false
    try {
        $process = Start-MagicWorldMinecraft
        $settings = Get-MagicWorldSettings
        if ($process -and !$process.HasExited) {
            if ($settings.minimizeLauncherOnPlay) {
                $window.Hide()
            }
            while (!$process.HasExited) {
                [System.Windows.Forms.Application]::DoEvents()
                Start-Sleep -Milliseconds 500
            }
            if (!$window.IsVisible) {
                $window.Show()
            }
            $window.WindowState = "Normal"
            $window.Activate() | Out-Null
            Update-Status "Minecraft fechado. Pronto para jogar novamente." 100
        }
    } catch {
        if (!$window.IsVisible) {
            $window.Show()
        }
        $window.WindowState = "Normal"
        $window.Activate() | Out-Null
        Update-Status "Erro: $($_.Exception.Message)" 0
        [System.Windows.MessageBox]::Show($_.Exception.Message, "Magic World Launcher", "OK", "Error") | Out-Null
    } finally {
        $playButton.IsEnabled = $true
    }
})

$window.ShowDialog() | Out-Null
