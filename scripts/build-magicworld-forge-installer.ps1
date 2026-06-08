param(
    [string]$ForgeInstallerUrl = "https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-installer.jar",
    [string]$PackageMinecraftDir,
    [switch]$NoFullPayload
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$installerDir = Join-Path $root "installer"
$tmpDir = Join-Path $root "tmp\full-installer"
$payloadMarker = "MAGICWORLD_FULL_PAYLOAD_V1"
New-Item -ItemType Directory -Path $installerDir -Force | Out-Null
New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null

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
$stubOut = Join-Path $tmpDir "MagicWorldInstaller.stub.exe"
$compileOut = if ($NoFullPayload) { $out } else { $stubOut }
$source = Join-Path $root "scripts\MagicWorldForgeInstallerLauncher.cs"
$script = Join-Path $root "scripts\install-magicworld-forge.ps1"
$banner = Join-Path $root "screenshots\banner_installer.png"
$args = @(
    "/nologo",
    "/target:winexe",
    "/platform:anycpu",
    "/out:$compileOut",
    "/reference:System.Windows.Forms.dll",
    "/reference:System.Drawing.dll",
    "/reference:System.IO.Compression.dll",
    "/reference:System.IO.Compression.FileSystem.dll",
    "/resource:$script,install-magicworld-forge.ps1"
)

if ($NoFullPayload) {
    $args += "/resource:$forgeInstaller,forge-installer.jar"
}

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

function Resolve-PackageMinecraftDir {
    if (-not [string]::IsNullOrWhiteSpace($PackageMinecraftDir)) {
        $resolved = [System.IO.Path]::GetFullPath($PackageMinecraftDir)
        if (Test-Path -LiteralPath $resolved) {
            return $resolved
        }
        throw "PackageMinecraftDir nao encontrado: $resolved"
    }

    $default = Join-Path $root "pacote_distribuivel\.minecraft"
    if (Test-Path -LiteralPath $default) {
        return (Resolve-Path -LiteralPath $default).Path
    }
    throw "Nao encontrei pacote_distribuivel\.minecraft para embutir no instalador FULL."
}

function Add-ZipEntry {
    param(
        [System.IO.Compression.ZipArchive]$Zip,
        [string]$SourceFile,
        [string]$EntryName
    )
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
        $Zip,
        $SourceFile,
        $EntryName.Replace('\', '/'),
        [System.IO.Compression.CompressionLevel]::Optimal
    ) | Out-Null
}

function New-FullPayloadZip {
    param(
        [string]$DestinationZip,
        [string]$ResolvedPackageMinecraftDir,
        [string]$ResolvedForgeInstaller
    )

    if (Test-Path -LiteralPath $DestinationZip) {
        Remove-Item -LiteralPath $DestinationZip -Force
    }

    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::Open($DestinationZip, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        $packageRoot = (Get-Item -LiteralPath $ResolvedPackageMinecraftDir).FullName.TrimEnd([char[]]@('\', '/'))
        $files = Get-ChildItem -LiteralPath $ResolvedPackageMinecraftDir -Recurse -File
        $index = 0
        foreach ($file in $files) {
            $index++
            if ($index % 25 -eq 0) {
                Write-Host "Payload FULL: $index/$($files.Count) arquivos..."
            }
            $relative = $file.FullName.Substring($packageRoot.Length).TrimStart([char[]]@('\', '/'))
            Add-ZipEntry -Zip $zip -SourceFile $file.FullName -EntryName ("payload/.minecraft/" + $relative)
        }

        Add-ZipEntry -Zip $zip -SourceFile $ResolvedForgeInstaller -EntryName "payload/forge/forge-1.20.1-47.4.10-installer.jar"
    } finally {
        $zip.Dispose()
    }
}

function Add-AppendedPayload {
    param(
        [string]$StubPath,
        [string]$PayloadZip,
        [string]$OutputPath
    )

    Copy-Item -LiteralPath $StubPath -Destination $OutputPath -Force
    $markerBytes = [System.Text.Encoding]::ASCII.GetBytes($payloadMarker)
    $payloadStream = [System.IO.File]::OpenRead($PayloadZip)
    try {
        $outputStream = [System.IO.File]::Open($OutputPath, [System.IO.FileMode]::Append, [System.IO.FileAccess]::Write)
        try {
            $payloadStream.CopyTo($outputStream)
            $lengthBytes = [System.BitConverter]::GetBytes([Int64]$payloadStream.Length)
            $outputStream.Write($lengthBytes, 0, $lengthBytes.Length)
            $outputStream.Write($markerBytes, 0, $markerBytes.Length)
        } finally {
            $outputStream.Dispose()
        }
    } finally {
        $payloadStream.Dispose()
    }
}

if (-not $NoFullPayload) {
    $resolvedPackage = Resolve-PackageMinecraftDir
    $payloadZip = Join-Path $tmpDir "magicworld-full-payload.zip"
    Write-Host "Gerando payload FULL a partir de: $resolvedPackage"
    New-FullPayloadZip -DestinationZip $payloadZip -ResolvedPackageMinecraftDir $resolvedPackage -ResolvedForgeInstaller $forgeInstaller
    Write-Host "Anexando payload FULL ao EXE..."
    Add-AppendedPayload -StubPath $stubOut -PayloadZip $payloadZip -OutputPath $out
}

Get-Item -LiteralPath $out
