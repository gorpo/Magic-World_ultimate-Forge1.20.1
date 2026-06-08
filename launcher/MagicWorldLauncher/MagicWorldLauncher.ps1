param(
    [switch]$SelfTest,
    [switch]$LaunchDryRun
)

$ErrorActionPreference = "Stop"

$LauncherRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$AssetsRoot = Join-Path $LauncherRoot "assets"
$BackgroundPath = Join-Path $AssetsRoot "title_background_static.png"
$LogoPath = Join-Path $AssetsRoot "title_logo.png"
$InstallerScriptPath = Join-Path $LauncherRoot "install-magicworld-forge.ps1"
$MagicWorldDataRoot = Join-Path $env:APPDATA "MagicWorldLauncher"
$MinecraftDir = Join-Path $MagicWorldDataRoot ".minecraft"
$MagicJavaRoot = Join-Path $MagicWorldDataRoot "runtime\java17"
$AccountsPath = Join-Path $MagicWorldDataRoot "accounts.json"
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

function Get-MagicWorldAccount {
    if (!(Test-Path -LiteralPath $AccountsPath)) {
        return [pscustomobject]@{
            username = "Player"
            uuid = ""
            accessToken = "0"
            userType = "legacy"
            loginProvider = "offline"
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
    }
}

function Save-MagicWorldAccount {
    param(
        [string]$Username,
        [string]$Uuid = "",
        [string]$AccessToken = "0",
        [string]$UserType = "legacy",
        [string]$LoginProvider = "TLauncher API"
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
        updatedAt = (Get-Date).ToString("o")
    }
    $data | ConvertTo-Json -Depth 3 | Set-Content -LiteralPath $AccountsPath -Encoding UTF8
    return $clean
}

function Invoke-TLauncherApiLogin {
    param(
        [string]$Username,
        [string]$Password
    )

    if ([string]::IsNullOrWhiteSpace($TLauncherAuthApiUrl)) {
        throw "API de login do TLauncher nao configurada. Defina MAGICWORLD_TLAUNCHER_AUTH_API_URL com o endpoint oficial de login."
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

    $response = Invoke-RestMethod -Uri $TLauncherAuthApiUrl -Method Post -Body $body -ContentType "application/json" -Headers @{ "User-Agent" = "MagicWorldLauncher/1.0" }
    $token = $(if ($response.accessToken) { $response.accessToken } elseif ($response.token) { $response.token } else { "" })
    $displayName = $(if ($response.displayName) { $response.displayName } elseif ($response.username) { $response.username } elseif ($response.login) { $response.login } elseif ($response.profile -and $response.profile.name) { $response.profile.name } else { $Username })
    $uuid = $(if ($response.uuid) { $response.uuid } elseif ($response.id) { $response.id } elseif ($response.profile -and $response.profile.id) { $response.profile.id } else { "" })

    if ([string]::IsNullOrWhiteSpace([string]$token)) {
        throw "A API de login respondeu sem accessToken/token. Verifique o endpoint oficial configurado."
    }

    Save-MagicWorldAccount -Username $displayName -Uuid $uuid -AccessToken $token -UserType "tlauncher" -LoginProvider "TLauncher API" | Out-Null
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

    if ($script:StatusText) {
        $script:StatusText.Text = $Text
    }
    if ($script:ProgressBar -and $Percent -ge 0) {
        $script:ProgressBar.Value = [Math]::Min(100, [Math]::Max(0, $Percent))
    }
    if ($script:StatusText) {
        [System.Windows.Forms.Application]::DoEvents()
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
                "has_custom_resolution" { $false }
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
    if (Test-Path -LiteralPath $clientJar) {
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
    }

    $jvmArgs = New-Object System.Collections.ArrayList
    [void]$jvmArgs.Add("-Xms2G")
    [void]$jvmArgs.Add("-Xmx8G")
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

function Show-TLauncherApiLoginDialog {
    if ([string]::IsNullOrWhiteSpace($TLauncherAuthApiUrl)) {
        throw "API de login do TLauncher nao configurada. Defina MAGICWORLD_TLAUNCHER_AUTH_API_URL com o endpoint oficial de login antes de usar este botao."
    }

    $dialog = New-Object System.Windows.Window
    $dialog.Title = "Login TLauncher"
    $dialog.Width = 420
    $dialog.Height = 250
    $dialog.WindowStartupLocation = "CenterOwner"
    $dialog.ResizeMode = "NoResize"
    $dialog.Background = "#101820"
    $dialog.Owner = $window

    $panel = New-Object System.Windows.Controls.StackPanel
    $panel.Margin = "24"
    $dialog.Content = $panel

    $label = New-Object System.Windows.Controls.TextBlock
    $label.Text = "Entre com sua conta TLauncher. A senha nao sera salva; o launcher guarda apenas o token retornado pela API."
    $label.Foreground = "White"
    $label.TextWrapping = "Wrap"
    $label.Margin = "0,0,0,14"
    $panel.Children.Add($label) | Out-Null

    $username = New-Object System.Windows.Controls.TextBox
    $username.Text = Get-MagicWorldAccountName
    $username.Height = 30
    $username.Margin = "0,0,0,10"
    $panel.Children.Add($username) | Out-Null

    $password = New-Object System.Windows.Controls.PasswordBox
    $password.Height = 30
    $password.Margin = "0,0,0,16"
    $panel.Children.Add($password) | Out-Null

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
            Invoke-TLauncherApiLogin -Username $username.Text -Password $password.Password | Out-Null
            $dialog.DialogResult = $true
        } catch {
            [System.Windows.MessageBox]::Show($_.Exception.Message, "Login TLauncher", "OK", "Error") | Out-Null
        }
    })
    $buttons.Children.Add($ok) | Out-Null

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
$accountInfo = Get-MagicWorldAccount
$account.Text = "Login: $($accountInfo.username) [$($accountInfo.loginProvider)]"
$account.FontSize = 16
$account.Foreground = "#FFDAA520"
$account.TextAlignment = "Center"
$account.Margin = "0,0,0,24"
$stack.Children.Add($account) | Out-Null

$script:StatusText = New-Object System.Windows.Controls.TextBlock
$script:StatusText.Text = "Instale uma vez. Depois clique em Jogar Magic World para abrir o Minecraft direto por este launcher."
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

$buttonPanel = New-Object System.Windows.Controls.WrapPanel
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
$playButton = New-MagicButton "Jogar Magic World" 190
$loginButton = New-MagicButton "Login TLauncher API" 190
$repoButton = New-MagicButton "Repositorio" 150

$buttonPanel.Children.Add($installButton) | Out-Null
$buttonPanel.Children.Add($playButton) | Out-Null
$buttonPanel.Children.Add($loginButton) | Out-Null
$buttonPanel.Children.Add($repoButton) | Out-Null

$installButton.Add_Click({
    $installButton.IsEnabled = $false
    try {
        Invoke-MagicWorldInstaller
        [System.Windows.MessageBox]::Show("Tudo instalado. Clique em Jogar Magic World para abrir o Minecraft direto por este launcher.", "Magic World Launcher", "OK", "Information") | Out-Null
    } catch {
        Update-Status "Erro: $($_.Exception.Message)" 0
        [System.Windows.MessageBox]::Show($_.Exception.Message, "Magic World Launcher", "OK", "Error") | Out-Null
    } finally {
        $installButton.IsEnabled = $true
    }
})

$repoButton.Add_Click({ Open-Repo })
$loginButton.Add_Click({
    try {
        if (Show-TLauncherApiLoginDialog) {
            $accountInfo = Get-MagicWorldAccount
            $account.Text = "Login: $($accountInfo.username) [$($accountInfo.loginProvider)]"
            Update-Status "Login TLauncher API salvo. Pronto para jogar." 0
        }
    } catch {
        Update-Status "Erro: $($_.Exception.Message)" 0
        [System.Windows.MessageBox]::Show($_.Exception.Message, "Magic World Launcher", "OK", "Error") | Out-Null
    }
})
$playButton.Add_Click({
    $playButton.IsEnabled = $false
    try {
        Start-MagicWorldMinecraft
    } catch {
        Update-Status "Erro: $($_.Exception.Message)" 0
        [System.Windows.MessageBox]::Show($_.Exception.Message, "Magic World Launcher", "OK", "Error") | Out-Null
    } finally {
        $playButton.IsEnabled = $true
    }
})

$window.ShowDialog() | Out-Null
