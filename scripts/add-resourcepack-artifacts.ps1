param(
    [string] $ResourcePackZip = (Join-Path $PSScriptRoot '..\run\resourcepacks\MagicWorldResource_1.20.1-models.zip'),
    [switch] $NoBackup
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

function New-ModelVariant {
    param(
        [string] $Model,
        [Nullable[int]] $X = $null,
        [Nullable[int]] $Y = $null,
        [Nullable[bool]] $UvLock = $null
    )

    $variant = [ordered]@{
        model = "block/$Model"
    }

    if ($null -ne $X) {
        $variant.x = $X
    }

    if ($null -ne $Y) {
        $variant.y = $Y
    }

    if ($null -ne $UvLock) {
        $variant.uvlock = $UvLock
    }

    return $variant
}

function New-CrystalBlockStateJson {
    param(
        [string] $BaseModel,
        [string] $SmallModel,
        [string] $MediumModel,
        [string] $LargeModel,
        [object[]] $BaseVariants
    )

    $baseApply = if ($BaseVariants -and $BaseVariants.Count -gt 0) {
        $BaseVariants
    } else {
        New-ModelVariant -Model $BaseModel
    }

    $blockState = [ordered]@{
        multipart = @(
            [ordered]@{
                apply = $baseApply
            },
            [ordered]@{
                apply = @(
                    (New-ModelVariant -Model 'air'),
                    (New-ModelVariant -Model $SmallModel),
                    (New-ModelVariant -Model $SmallModel -Y 180),
                    (New-ModelVariant -Model $MediumModel),
                    (New-ModelVariant -Model $MediumModel -Y 180),
                    (New-ModelVariant -Model $LargeModel),
                    (New-ModelVariant -Model $LargeModel -Y 180)
                )
            },
            [ordered]@{
                apply = @(
                    (New-ModelVariant -Model $SmallModel -X 180),
                    (New-ModelVariant -Model $SmallModel -X 180 -Y 180),
                    (New-ModelVariant -Model $MediumModel -X 180),
                    (New-ModelVariant -Model $MediumModel -X 180 -Y 180),
                    (New-ModelVariant -Model 'air'),
                    (New-ModelVariant -Model $LargeModel -X 180),
                    (New-ModelVariant -Model $LargeModel -X 180 -Y 180)
                )
            },
            [ordered]@{
                apply = @(
                    (New-ModelVariant -Model 'air'),
                    (New-ModelVariant -Model $SmallModel -X 90),
                    (New-ModelVariant -Model $SmallModel -X 270 -Y 180),
                    (New-ModelVariant -Model $MediumModel -X 90),
                    (New-ModelVariant -Model $MediumModel -X 270 -Y 180)
                )
            },
            [ordered]@{
                apply = @(
                    (New-ModelVariant -Model 'air'),
                    (New-ModelVariant -Model $SmallModel -X 90 -Y 180),
                    (New-ModelVariant -Model $SmallModel -X 270),
                    (New-ModelVariant -Model $MediumModel -X 90 -Y 180),
                    (New-ModelVariant -Model $MediumModel -X 270)
                )
            },
            [ordered]@{
                apply = @(
                    (New-ModelVariant -Model $SmallModel -X 90 -Y 90),
                    (New-ModelVariant -Model $SmallModel -X 270 -Y 270),
                    (New-ModelVariant -Model 'air'),
                    (New-ModelVariant -Model $MediumModel -X 90 -Y 90),
                    (New-ModelVariant -Model $MediumModel -X 270 -Y 270)
                )
            },
            [ordered]@{
                apply = @(
                    (New-ModelVariant -Model $SmallModel -X 90 -Y 270),
                    (New-ModelVariant -Model $SmallModel -X 270 -Y 90),
                    (New-ModelVariant -Model 'air'),
                    (New-ModelVariant -Model $MediumModel -X 90 -Y 270),
                    (New-ModelVariant -Model $MediumModel -X 270 -Y 90)
                )
            }
        )
    }

    return ($blockState | ConvertTo-Json -Depth 30)
}

function New-CrystalModelJson {
    param(
        [string] $Parent,
        [string] $Texture
    )

    $model = [ordered]@{
        parent = "block/$Parent"
        textures = [ordered]@{
            crystal = "block/$Texture"
            particle = "block/$Texture"
        }
    }

    return ($model | ConvertTo-Json -Depth 10)
}

function Set-ZipTextEntry {
    param(
        [System.IO.Compression.ZipArchive] $Zip,
        [string] $EntryName,
        [string] $Content
    )

    $existing = $Zip.GetEntry($EntryName)
    if ($null -ne $existing) {
        $existing.Delete()
    }

    $entry = $Zip.CreateEntry($EntryName, [System.IO.Compression.CompressionLevel]::Optimal)
    $writer = [System.IO.StreamWriter]::new($entry.Open(), [System.Text.UTF8Encoding]::new($false))
    try {
        $writer.Write($Content)
    } finally {
        $writer.Dispose()
    }
}

function Get-Rotations {
    param(
        [string] $BaseModel,
        [string] $Style
    )

    switch ($Style) {
        'axis' {
            return @(
                (New-ModelVariant -Model $BaseModel),
                (New-ModelVariant -Model $BaseModel -Y 180),
                (New-ModelVariant -Model $BaseModel -X 180),
                (New-ModelVariant -Model $BaseModel -X 180 -Y 180)
            )
        }
        'horizontal' {
            return @(
                (New-ModelVariant -Model $BaseModel),
                (New-ModelVariant -Model $BaseModel -Y 90),
                (New-ModelVariant -Model $BaseModel -Y 180),
                (New-ModelVariant -Model $BaseModel -Y 270)
            )
        }
        'full' {
            return @(
                (New-ModelVariant -Model $BaseModel),
                (New-ModelVariant -Model $BaseModel -Y 90),
                (New-ModelVariant -Model $BaseModel -Y 180),
                (New-ModelVariant -Model $BaseModel -Y 270),
                (New-ModelVariant -Model $BaseModel -X 90),
                (New-ModelVariant -Model $BaseModel -X 90 -Y 90),
                (New-ModelVariant -Model $BaseModel -X 90 -Y 180),
                (New-ModelVariant -Model $BaseModel -X 90 -Y 270)
            )
        }
        default {
            return @((New-ModelVariant -Model $BaseModel))
        }
    }
}

$zipFullPath = [System.IO.Path]::GetFullPath($ResourcePackZip)
if (-not (Test-Path -LiteralPath $zipFullPath)) {
    throw "Resource pack zip not found: $zipFullPath"
}

if (-not $NoBackup) {
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $backupPath = "$zipFullPath.before-artifacts-$stamp.bak"
    Copy-Item -LiteralPath $zipFullPath -Destination $backupPath
    Write-Host "Backup: $backupPath"
}

$oreTargets = @(
    @{ block = 'coal_ore'; texture = 'coal_block' },
    @{ block = 'deepslate_coal_ore'; texture = 'coal_block' },
    @{ block = 'iron_ore'; texture = 'iron_block' },
    @{ block = 'deepslate_iron_ore'; texture = 'iron_block' },
    @{ block = 'gold_ore'; texture = 'gold_block' },
    @{ block = 'deepslate_gold_ore'; texture = 'gold_block' },
    @{ block = 'nether_gold_ore'; texture = 'gold_block' },
    @{ block = 'copper_ore'; texture = 'copper_block' },
    @{ block = 'deepslate_copper_ore'; texture = 'copper_block' },
    @{ block = 'lapis_ore'; texture = 'lapis_block' },
    @{ block = 'deepslate_lapis_ore'; texture = 'lapis_block' }
)

$storageTargets = @(
    @{ block = 'coal_block'; texture = 'coal_block'; rotation = 'single' },
    @{ block = 'iron_block'; texture = 'iron_block'; rotation = 'axis' },
    @{ block = 'gold_block'; texture = 'gold_block'; rotation = 'axis' },
    @{ block = 'diamond_block'; texture = 'diamond_block'; rotation = 'full' },
    @{ block = 'emerald_block'; texture = 'emerald_block'; rotation = 'single' },
    @{ block = 'redstone_block'; texture = 'redstone_block'; rotation = 'single' },
    @{ block = 'lapis_block'; texture = 'lapis_block'; rotation = 'single' },
    @{ block = 'copper_block'; texture = 'copper_block'; rotation = 'horizontal' },
    @{ block = 'netherite_block'; texture = 'netherite_block'; rotation = 'full' },
    @{ block = 'raw_iron_block'; texture = 'raw_iron_block'; rotation = 'single' },
    @{ block = 'raw_gold_block'; texture = 'raw_gold_block'; rotation = 'single' },
    @{ block = 'raw_copper_block'; texture = 'raw_copper_block'; rotation = 'single' },
    @{ block = 'amethyst_block'; texture = 'amethyst_block'; rotation = 'single' },
    @{ block = 'budding_amethyst'; texture = 'budding_amethyst'; rotation = 'single' },
    @{ block = 'crying_obsidian'; texture = 'crying_obsidian'; rotation = 'single' },
    @{ block = 'purpur_block'; texture = 'purpur_block'; rotation = 'single' }
)

$zip = [System.IO.Compression.ZipFile]::Open($zipFullPath, [System.IO.Compression.ZipArchiveMode]::Update)
try {
    foreach ($target in $oreTargets) {
        $block = $target.block
        $texture = $target.texture
        $small = "${block}2"
        $medium = "${block}3"
        $large = "${block}4"

        Set-ZipTextEntry -Zip $zip -EntryName "assets/minecraft/models/block/$small.json" -Content (New-CrystalModelJson -Parent 'crystal1' -Texture $texture)
        Set-ZipTextEntry -Zip $zip -EntryName "assets/minecraft/models/block/$medium.json" -Content (New-CrystalModelJson -Parent 'crystal2' -Texture $texture)
        Set-ZipTextEntry -Zip $zip -EntryName "assets/minecraft/models/block/$large.json" -Content (New-CrystalModelJson -Parent 'crystal3' -Texture $texture)
        Set-ZipTextEntry -Zip $zip -EntryName "assets/minecraft/blockstates/$block.json" -Content (New-CrystalBlockStateJson -BaseModel $block -SmallModel $small -MediumModel $medium -LargeModel $large -BaseVariants @())
    }

    foreach ($target in $storageTargets) {
        $block = $target.block
        $texture = $target.texture
        $small = "${block}_artifact2"
        $medium = "${block}_artifact3"
        $large = "${block}_artifact4"
        $baseVariants = Get-Rotations -BaseModel $block -Style $target.rotation

        Set-ZipTextEntry -Zip $zip -EntryName "assets/minecraft/models/block/$small.json" -Content (New-CrystalModelJson -Parent 'crystal1' -Texture $texture)
        Set-ZipTextEntry -Zip $zip -EntryName "assets/minecraft/models/block/$medium.json" -Content (New-CrystalModelJson -Parent 'crystal2' -Texture $texture)
        Set-ZipTextEntry -Zip $zip -EntryName "assets/minecraft/models/block/$large.json" -Content (New-CrystalModelJson -Parent 'crystal3' -Texture $texture)
        Set-ZipTextEntry -Zip $zip -EntryName "assets/minecraft/blockstates/$block.json" -Content (New-CrystalBlockStateJson -BaseModel $block -SmallModel $small -MediumModel $medium -LargeModel $large -BaseVariants $baseVariants)
    }
} finally {
    $zip.Dispose()
}

Write-Host "Artifact blockstates/models updated in: $zipFullPath"
