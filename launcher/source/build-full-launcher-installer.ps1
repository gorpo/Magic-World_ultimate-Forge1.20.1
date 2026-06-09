param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path,
    [string]$BundledInstallerPath,
    [string]$OutputName = "MagicWorldLauncherFullInstaller-Stable V1.0.0.2.exe"
)

$ErrorActionPreference = "Stop"

$launcherSource = Join-Path $ProjectRoot "launcher\MagicWorldLauncher"
$dist = Join-Path $ProjectRoot "launcher\dist"
$staging = Join-Path $ProjectRoot "tmp\launcher-full-payload"
$buildTemp = Join-Path $ProjectRoot "tmp\launcher-full-build"
$payloadZip = Join-Path $buildTemp "MagicWorldLauncherPayload.zip"
$stub = Join-Path $buildTemp "MagicWorldLauncherFullInstaller.stub.exe"
$out = Join-Path $dist $OutputName
$source = Join-Path $ProjectRoot "launcher\source\MagicWorldLauncherFullInstaller.cs"
$launcherAppSource = Join-Path $ProjectRoot "launcher\source\MagicWorldLauncherApp.cs"
$icon = Join-Path $ProjectRoot "launcher\MagicWorldLauncher\assets\magicworld.ico"
if (!(Test-Path -LiteralPath $icon)) {
    $icon = Join-Path $ProjectRoot "installer\MagicWorldInstaller.ico"
}
$csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
$powerShellAutomation = Join-Path $env:WINDIR "System32\WindowsPowerShell\v1.0\System.Management.Automation.dll"
if (!(Test-Path -LiteralPath $powerShellAutomation)) {
    $powerShellAutomation = Get-ChildItem -Path (Join-Path $env:WINDIR "Microsoft.NET\assembly") -Recurse -Filter "System.Management.Automation.dll" -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}
$marker = [Text.Encoding]::ASCII.GetBytes("MAGICWORLD_LAUNCHER_PAYLOAD_V1")

if (!(Test-Path -LiteralPath $csc)) {
    throw "csc.exe nao encontrado em $csc"
}
if ([string]::IsNullOrWhiteSpace($powerShellAutomation) -or !(Test-Path -LiteralPath $powerShellAutomation)) {
    throw "System.Management.Automation.dll nao encontrado em $powerShellAutomation"
}
if (!(Test-Path -LiteralPath $launcherSource)) {
    throw "Pasta do launcher nao encontrada: $launcherSource"
}
if ([string]::IsNullOrWhiteSpace($BundledInstallerPath)) {
    $BundledInstallerPath = Join-Path $ProjectRoot "installer\MagicWorldInstaller.exe"
}
if (!(Test-Path -LiteralPath $BundledInstallerPath)) {
    throw "Instalador FULL nao encontrado para embutir: $BundledInstallerPath"
}

New-Item -ItemType Directory -Force -Path $dist | Out-Null
if (Test-Path -LiteralPath $buildTemp) { Remove-Item -LiteralPath $buildTemp -Recurse -Force }
New-Item -ItemType Directory -Force -Path $buildTemp | Out-Null
Get-ChildItem -LiteralPath $dist -File -ErrorAction SilentlyContinue |
    Where-Object {
        $_.Name -like "MagicWorldLauncherFullInstaller*.exe" -or
        $_.Name -eq "MagicWorldLauncherPayload.zip"
    } |
    Remove-Item -Force
if (Test-Path -LiteralPath $payloadZip) { Remove-Item -LiteralPath $payloadZip -Force }
if (Test-Path -LiteralPath $stub) { Remove-Item -LiteralPath $stub -Force }
if (Test-Path -LiteralPath $out) { Remove-Item -LiteralPath $out -Force }
if (Test-Path -LiteralPath $staging) { Remove-Item -LiteralPath $staging -Recurse -Force }
New-Item -ItemType Directory -Force -Path $staging | Out-Null

$installerArgs = @(
    "/nologo",
    "/target:winexe",
    "/out:$stub",
    "/reference:System.Windows.Forms.dll",
    "/reference:System.Drawing.dll",
    "/reference:System.IO.Compression.dll",
    "/reference:System.IO.Compression.FileSystem.dll"
)
if (Test-Path -LiteralPath $icon) {
    $installerArgs += "/win32icon:$icon"
}
$installerArgs += $source
& $csc @installerArgs
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao compilar installer."
}

Copy-Item -Path (Join-Path $launcherSource "*") -Destination $staging -Recurse -Force
Get-ChildItem -LiteralPath $staging -Filter "*.cmd" -File -ErrorAction SilentlyContinue | Remove-Item -Force
Remove-Item -LiteralPath (Join-Path $staging "MagicWorldInstaller.exe") -Force -ErrorAction SilentlyContinue
if (Test-Path -LiteralPath $icon) {
    Copy-Item -LiteralPath $icon -Destination (Join-Path $staging "MagicWorldLauncher.ico") -Force
}
$launcherExe = Join-Path $staging "MagicWorldLauncher.exe"
$launcherArgs = @(
    "/nologo",
    "/target:winexe",
    "/out:$launcherExe",
    "/reference:System.Windows.Forms.dll",
    "/reference:$powerShellAutomation",
    $launcherAppSource
)
if (Test-Path -LiteralPath $icon) {
    $launcherArgs += "/win32icon:$icon"
}
& $csc @launcherArgs
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao compilar MagicWorldLauncher.exe."
}
Copy-Item -LiteralPath $BundledInstallerPath -Destination (Join-Path $staging "MagicWorldPayload.bin") -Force
Compress-Archive -Path (Join-Path $staging "*") -DestinationPath $payloadZip -CompressionLevel Optimal

Copy-Item -LiteralPath $stub -Destination $out -Force
$payloadBytes = [IO.File]::ReadAllBytes($payloadZip)
$lengthBytes = [BitConverter]::GetBytes([Int64]$payloadBytes.Length)
$stream = [IO.File]::Open($out, [IO.FileMode]::Append, [IO.FileAccess]::Write)
try {
    $stream.Write($payloadBytes, 0, $payloadBytes.Length)
    $stream.Write($lengthBytes, 0, $lengthBytes.Length)
    $stream.Write($marker, 0, $marker.Length)
} finally {
    $stream.Dispose()
}

Get-Item -LiteralPath $out
