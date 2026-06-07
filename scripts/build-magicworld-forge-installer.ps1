param(
    [string]$ForgeInstallerUrl = "https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-installer.jar"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$installerDir = Join-Path $root "installer"
New-Item -ItemType Directory -Path $installerDir -Force | Out-Null

$forgeInstaller = Join-Path $installerDir "forge-1.20.1-47.4.10-installer.jar"
if (-not (Test-Path -LiteralPath $forgeInstaller)) {
    Write-Host "Baixando Forge installer..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    (New-Object Net.WebClient).DownloadFile($ForgeInstallerUrl, $forgeInstaller)
}

$icon = Join-Path $installerDir "MagicWorldInstaller.ico"
if (-not (Test-Path -LiteralPath $icon)) {
    $sourceIcon = Join-Path $root "tmp\neoforge-readme-source\installer\MagicWorldInstaller.ico"
    if (Test-Path -LiteralPath $sourceIcon) {
        Copy-Item -LiteralPath $sourceIcon -Destination $icon -Force
    }
}

$cscCandidates = @(
    (Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"),
    (Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe")
)
$csc = $cscCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $csc) {
    $cmd = Get-Command csc.exe -ErrorAction SilentlyContinue
    if ($cmd) { $csc = $cmd.Source }
}
if (-not $csc) {
    throw "csc.exe nao encontrado para compilar o instalador."
}

$out = Join-Path $installerDir "MagicWorldInstaller.exe"
$source = Join-Path $root "scripts\MagicWorldForgeInstallerLauncher.cs"
$script = Join-Path $root "scripts\install-magicworld-forge-tlauncher.ps1"
$banner = Join-Path $root "screenshots\banner_installer.png"
$args = @(
    "/nologo",
    "/target:winexe",
    "/platform:anycpu",
    "/out:$out",
    "/reference:System.Windows.Forms.dll",
    "/reference:System.Drawing.dll",
    "/resource:$script,install-magicworld-forge-tlauncher.ps1",
    "/resource:$forgeInstaller,forge-installer.jar"
)

if (Test-Path -LiteralPath $banner) {
    $args += "/resource:$banner,banner_installer.png"
}
if (Test-Path -LiteralPath $icon) {
    $args += "/win32icon:$icon"
}
$args += $source

& $csc @args
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao compilar MagicWorldInstaller.exe"
}

Get-Item -LiteralPath $out
