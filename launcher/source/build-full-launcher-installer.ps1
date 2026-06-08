param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path,
    [string]$BundledInstallerPath
)

$ErrorActionPreference = "Stop"

$launcherSource = Join-Path $ProjectRoot "launcher\MagicWorldLauncher"
$dist = Join-Path $ProjectRoot "launcher\dist"
$staging = Join-Path $ProjectRoot "tmp\launcher-full-payload"
$payloadZip = Join-Path $dist "MagicWorldLauncherPayload.zip"
$stub = Join-Path $dist "MagicWorldLauncherFullInstaller.stub.exe"
$out = Join-Path $dist "MagicWorldLauncherFullInstaller.exe"
$source = Join-Path $ProjectRoot "launcher\source\MagicWorldLauncherFullInstaller.cs"
$csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
$marker = [Text.Encoding]::ASCII.GetBytes("MAGICWORLD_LAUNCHER_PAYLOAD_V1")

if (!(Test-Path -LiteralPath $csc)) {
    throw "csc.exe nao encontrado em $csc"
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
if (Test-Path -LiteralPath $payloadZip) { Remove-Item -LiteralPath $payloadZip -Force }
if (Test-Path -LiteralPath $stub) { Remove-Item -LiteralPath $stub -Force }
if (Test-Path -LiteralPath $out) { Remove-Item -LiteralPath $out -Force }
if (Test-Path -LiteralPath $staging) { Remove-Item -LiteralPath $staging -Recurse -Force }
New-Item -ItemType Directory -Force -Path $staging | Out-Null

& $csc /nologo /target:winexe /out:$stub /reference:System.Windows.Forms.dll /reference:System.Drawing.dll /reference:System.IO.Compression.dll /reference:System.IO.Compression.FileSystem.dll $source
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao compilar installer."
}

Copy-Item -Path (Join-Path $launcherSource "*") -Destination $staging -Recurse -Force
Copy-Item -LiteralPath $BundledInstallerPath -Destination (Join-Path $staging "MagicWorldInstaller.exe") -Force
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
