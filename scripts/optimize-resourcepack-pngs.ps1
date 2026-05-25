param(
    [Parameter(Mandatory = $true)]
    [string] $Path,

    [string] $BackupRoot = "",

    [string] $ReportPath = "",

    [string] $Oxipng = "oxipng",

    [switch] $WhatIfOnly
)

$ErrorActionPreference = "Stop"

function Resolve-RequiredPath([string] $InputPath) {
    if (!(Test-Path -LiteralPath $InputPath)) {
        throw "Path not found: $InputPath"
    }

    return (Resolve-Path -LiteralPath $InputPath).Path
}

function Get-TextureKind([string] $RelativePath) {
    $normalized = $RelativePath.Replace("\", "/").ToLowerInvariant()

    if ($normalized.EndsWith("_n.png")) {
        return "normal_lossless"
    }

    if ($normalized.EndsWith("_s.png")) {
        return "specular_lossless"
    }

    if (($normalized.Contains("/gui/")) -or ($normalized.Contains("/icon")) -or ($normalized.EndsWith("pack.png"))) {
        return "gui_icon_lossless"
    }

    return "diffuse_lossless"
}

function Get-RelativePathCompat([string] $RootPath, [string] $FilePath) {
    $rootFull =
            [System.IO.Path]::GetFullPath($RootPath).TrimEnd(
                    [System.IO.Path]::DirectorySeparatorChar,
                    [System.IO.Path]::AltDirectorySeparatorChar
            )

    $fileFull =
            [System.IO.Path]::GetFullPath($FilePath)

    if ($fileFull.StartsWith($rootFull, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $fileFull.Substring($rootFull.Length).TrimStart(
                [System.IO.Path]::DirectorySeparatorChar,
                [System.IO.Path]::AltDirectorySeparatorChar
        )
    }

    return Split-Path -Leaf $FilePath
}

function Invoke-Oxipng([string] $FilePath, [string] $Kind) {
    $args = @("--preserve", "--strip", "safe")

    if (($Kind -eq "normal_lossless") -or ($Kind -eq "specular_lossless") -or ($Kind -eq "gui_icon_lossless")) {
        $args += @("--opt", "2")
    }
    else {
        $args += @("--opt", "4")
    }

    $args += $FilePath

    & $Oxipng @args

    if ($LASTEXITCODE -ne 0) {
        throw "oxipng failed for $FilePath with exit code $LASTEXITCODE"
    }
}

$root = Resolve-RequiredPath $Path
$rootItem = Get-Item -LiteralPath $root

if (!$rootItem.PSIsContainer) {
    throw "This first safe implementation expects an extracted resource pack directory. Extract zip packs first, then pass the folder path."
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

if ([string]::IsNullOrWhiteSpace($BackupRoot)) {
    $BackupRoot = Join-Path $root ("..\\png-backup-$timestamp")
}

if ([string]::IsNullOrWhiteSpace($ReportPath)) {
    $ReportPath = Join-Path $root ("png-optimization-report-$timestamp.csv")
}

$oxipngCommand = Get-Command $Oxipng -ErrorAction SilentlyContinue

if ($null -eq $oxipngCommand -and !$WhatIfOnly) {
    throw "oxipng was not found. Install oxipng or run with -WhatIfOnly to create a dry report."
}

$pngFiles =
        Get-ChildItem -LiteralPath $root -Recurse -File -Filter "*.png" |
                Sort-Object FullName

$report = New-Object System.Collections.Generic.List[object]

foreach ($file in $pngFiles) {
    $relative =
            Get-RelativePathCompat $root $file.FullName

    $kind =
            Get-TextureKind $relative

    $before =
            $file.Length

    $backupPath =
            Join-Path $BackupRoot $relative

    if (!$WhatIfOnly) {
        $backupDirectory =
                Split-Path -Parent $backupPath

        New-Item -ItemType Directory -Force -Path $backupDirectory |
                Out-Null

        Copy-Item -LiteralPath $file.FullName -Destination $backupPath -Force

        Invoke-Oxipng $file.FullName $kind
    }

    $after =
            if ($WhatIfOnly) {
                $before
            }
            else {
                (Get-Item -LiteralPath $file.FullName).Length
            }

    $report.Add(
            [pscustomobject] @{
                File = $relative.Replace("\", "/")
                Kind = $kind
                BeforeBytes = $before
                AfterBytes = $after
                SavedBytes = $before - $after
                Changed = (!$WhatIfOnly)
            }
    )
}

$report |
        Export-Csv -LiteralPath $ReportPath -NoTypeInformation -Encoding UTF8

$beforeTotal =
        ($report | Measure-Object -Property BeforeBytes -Sum).Sum

$afterTotal =
        ($report | Measure-Object -Property AfterBytes -Sum).Sum

Write-Host "PNG files: $($report.Count)"
Write-Host "Before bytes: $beforeTotal"
Write-Host "After bytes: $afterTotal"
Write-Host "Saved bytes: $($beforeTotal - $afterTotal)"
Write-Host "Backup root: $BackupRoot"
Write-Host "Report: $ReportPath"
