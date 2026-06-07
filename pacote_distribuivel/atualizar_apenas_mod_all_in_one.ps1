$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$dist = Join-Path $root 'pacote_distribuivel'
$mods = Join-Path $dist '.minecraft\mods'

if (-not (Test-Path $mods)) {
    throw "Pasta mods do pacote nao encontrada: $mods"
}

$magicJar = Get-ChildItem -Path (Join-Path $root 'build\libs') -Filter 'Magic_World_Mod_1.20.1-*.jar' |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $magicJar) {
    throw 'JAR do Magic World nao encontrado em build/libs. Rode .\gradlew.bat build antes.'
}

Get-ChildItem -Path $mods -Filter 'Magic_World_Mod_1.20.1-*.jar' | Remove-Item -Force
Copy-Item -Path $magicJar.FullName -Destination $mods -Force

Write-Output "Mod atualizado no all-in-one: $($magicJar.Name)"
Write-Output "Padrao local: copie a pasta $dist\\.minecraft para seu Minecraft pessoal."
