param(
    [string]$MinecraftDir,
    [string]$PackageMinecraftDir,
    [string]$ForgeInstallerPath,
    [string]$ForgeInstallerUrl = "https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-installer.jar",
    [switch]$NoGui,
    [switch]$SkipForgeInstall
)

$ErrorActionPreference = "Stop"
$MinecraftVersion = "1.20.1"
$ForgeVersion = "47.4.10"
$ModJarName = "Magic_World_Mod_1.20.1-1.0.0.1.jar"
$LogFile = Join-Path $env:TEMP "magicworld-forge-installer.log"

function Write-Log {
    param([string]$Message)
    $line = "[{0}] {1}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $Message
    Add-Content -LiteralPath $LogFile -Value $line -Encoding UTF8
    Write-Output $line
}

function Get-FullPathIfExists {
    param([string]$PathValue)
    if ([string]::IsNullOrWhiteSpace($PathValue)) { return $null }
    $full = [System.IO.Path]::GetFullPath($PathValue)
    if (Test-Path -LiteralPath $full) { return $full }
    return $null
}

function Find-MinecraftDir {
    if (-not [string]::IsNullOrWhiteSpace($MinecraftDir)) {
        return [System.IO.Path]::GetFullPath($MinecraftDir)
    }

    $candidates = @(
        (Join-Path $env:APPDATA ".minecraft"),
        (Join-Path $env:APPDATA "TLauncher\.minecraft"),
        (Join-Path $env:APPDATA ".tlauncher\.minecraft"),
        (Join-Path $env:USERPROFILE "AppData\Roaming\.minecraft")
    ) | Select-Object -Unique

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return [System.IO.Path]::GetFullPath($candidate)
        }
    }

    return [System.IO.Path]::GetFullPath((Join-Path $env:APPDATA ".minecraft"))
}

function Find-PackageMinecraftDir {
    $explicit = Get-FullPathIfExists $PackageMinecraftDir
    if ($explicit) { return $explicit }

    $scriptDir = Split-Path -Parent $PSCommandPath
    $candidates = @(
        (Join-Path $scriptDir "..\pacote_distribuivel\.minecraft"),
        (Join-Path $scriptDir "pacote_distribuivel\.minecraft"),
        (Join-Path (Get-Location) "pacote_distribuivel\.minecraft"),
        (Join-Path (Get-Location) "..\pacote_distribuivel\.minecraft")
    )

    foreach ($candidate in $candidates) {
        $found = Get-FullPathIfExists $candidate
        if ($found) { return $found }
    }

    throw "Nao encontrei pacote_distribuivel\.minecraft. Execute ao lado do projeto ou informe -PackageMinecraftDir."
}

function Copy-DirectoryContents {
    param([string]$Source, [string]$Destination)
    if (-not (Test-Path -LiteralPath $Source)) {
        Write-Log "Pasta opcional ausente, pulando: $Source"
        return
    }

    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    Get-ChildItem -LiteralPath $Source -Force | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $Destination $_.Name) -Recurse -Force
    }
    Write-Log "Copiado: $Source -> $Destination"
}

function Remove-KnownConflictMods {
    param([string]$ModsDir)
    New-Item -ItemType Directory -Path $ModsDir -Force | Out-Null
    $patterns = @(
        "Magic_World_Mod_1.20.1-*.jar",
        "MagicWorld-Ultimate*.jar",
        "tl_skin_cape*.jar",
        "tl-cape*.jar",
        "controllable*.jar",
        "entity_model_features*.jar",
        "entity_texture_features*.jar",
        "fusion*.jar",
        "citresewn*.jar",
        "forge-cit*.jar",
        "modernfix*.jar",
        "ferritecore*.jar"
    )

    foreach ($pattern in $patterns) {
        Get-ChildItem -LiteralPath $ModsDir -Filter $pattern -File -ErrorAction SilentlyContinue | ForEach-Object {
            Write-Log "Removendo mod conflitante/antigo: $($_.Name)"
            Remove-Item -LiteralPath $_.FullName -Force
        }
    }
}

function Ensure-JourneyMap3DWaypointsHidden {
    param([string]$TargetMinecraftDir)
    $configDir = Join-Path $TargetMinecraftDir "journeymap\config\5.10"
    New-Item -ItemType Directory -Path $configDir -Force | Out-Null
    $configFile = Join-Path $configDir "journeymap.waypoint.config"
    $config = @'
// jm.config.file_header_1
// jm.config.file_header_2
// jm.config.file_header_5
{
  "managerEnabled": "true",
  "beaconEnabled": "false",
  "showTexture": "false",
  "showStaticBeam": "false",
  "showRotatingBeam": "false",
  "showName": "false",
  "showDistance": "false",
  "autoHideLabel": "true",
  "showDeviationLabel": "false",
  "disableStrikeThrough": "false",
  "boldLabel": "false",
  "fontScale": "2.0",
  "textureSmall": "true",
  "shaderBeacon": "false",
  "maxDistance": "0",
  "minDistance": "4",
  "createDeathpoints": "true",
  "autoRemoveDeathpoints": "false",
  "autoRemoveDeathpointDistance": "2",
  "autoRemoveTempWaypoints": "2",
  "showDeathpointlabel": "true",
  "fullscreenDoubleClickToCreate": "true",
  "teleportCommand": "/tp {name} {x} {y} {z}",
  "dateFormat": "MM-dd-yyyy",
  "timeFormat": "HH:mm:ss",
  "managerDimensionFocus": "false",
  "configVersion": "5.10.3"
}
'@
    Set-Content -LiteralPath $configFile -Value $config -Encoding UTF8
    Write-Log "JourneyMap configurado sem beacons/linhas/nomes 3D."
}

function Find-Java {
    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        $javaFromHome = Join-Path $env:JAVA_HOME "bin\java.exe"
        if (Test-Path -LiteralPath $javaFromHome) { return $javaFromHome }
    }

    $command = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    throw "Java nao encontrado. Instale Java 17+ ou ajuste JAVA_HOME antes de instalar Forge."
}

function Get-ForgeInstaller {
    $explicit = Get-FullPathIfExists $ForgeInstallerPath
    if ($explicit) { return $explicit }

    $scriptDir = Split-Path -Parent $PSCommandPath
    $candidates = @(
        (Join-Path $scriptDir "..\installer\forge-1.20.1-47.4.10-installer.jar"),
        (Join-Path $scriptDir "payload\forge\forge-1.20.1-47.4.10-installer.jar"),
        (Join-Path $scriptDir "..\payload\forge\forge-1.20.1-47.4.10-installer.jar")
    )
    foreach ($candidate in $candidates) {
        $found = Get-FullPathIfExists $candidate
        if ($found) { return $found }
    }

    $downloadDir = Join-Path $env:TEMP "MagicWorldForgeInstaller"
    New-Item -ItemType Directory -Path $downloadDir -Force | Out-Null
    $downloadPath = Join-Path $downloadDir "forge-1.20.1-47.4.10-installer.jar"
    Write-Log "Baixando Forge installer: $ForgeInstallerUrl"
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    (New-Object Net.WebClient).DownloadFile($ForgeInstallerUrl, $downloadPath)
    return $downloadPath
}

function Install-ForgeClient {
    param([string]$TargetMinecraftDir)
    if ($SkipForgeInstall) {
        Write-Log "Instalacao Forge pulada por -SkipForgeInstall."
        return
    }

    $java = Find-Java
    $installer = Get-ForgeInstaller
    Write-Log "Instalando Forge $ForgeVersion para Minecraft $MinecraftVersion em: $TargetMinecraftDir"
    Write-Log "Java: $java"
    Write-Log "Forge installer: $installer"

    & $java -jar $installer --install-client $TargetMinecraftDir 2>&1 | ForEach-Object { Write-Log "Forge: $_" }
    if ($LASTEXITCODE -ne 0) {
        throw "Forge installer retornou codigo $LASTEXITCODE. Veja $LogFile."
    }
    Write-Log "Forge instalado/atualizado."
}

Write-Log "Magic World Forge Installer iniciado."
$targetMinecraft = Find-MinecraftDir
$packageMinecraft = Find-PackageMinecraftDir
New-Item -ItemType Directory -Path $targetMinecraft -Force | Out-Null
Write-Log "Pasta Minecraft destino: $targetMinecraft"
Write-Log "Pacote fonte: $packageMinecraft"

Remove-KnownConflictMods -ModsDir (Join-Path $targetMinecraft "mods")
Copy-DirectoryContents -Source (Join-Path $packageMinecraft "mods") -Destination (Join-Path $targetMinecraft "mods")
Copy-DirectoryContents -Source (Join-Path $packageMinecraft "resourcepacks") -Destination (Join-Path $targetMinecraft "resourcepacks")
Copy-DirectoryContents -Source (Join-Path $packageMinecraft "shaderpacks") -Destination (Join-Path $targetMinecraft "shaderpacks")
Copy-DirectoryContents -Source (Join-Path $packageMinecraft "journeymap") -Destination (Join-Path $targetMinecraft "journeymap")
Ensure-JourneyMap3DWaypointsHidden -TargetMinecraftDir $targetMinecraft

$modJar = Join-Path $targetMinecraft ("mods\" + $ModJarName)
if (-not (Test-Path -LiteralPath $modJar)) {
    throw "JAR principal nao encontrado apos copia: $modJar"
}

Install-ForgeClient -TargetMinecraftDir $targetMinecraft
Write-Log "Instalacao local concluida. No TLauncher, selecione Forge $MinecraftVersion-$ForgeVersion e use: $targetMinecraft"
