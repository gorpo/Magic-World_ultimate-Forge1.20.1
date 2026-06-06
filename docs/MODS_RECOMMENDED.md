# Mods recomendados para `run/mods`

Atualizado em 2026-06-05.

## Regra atual

Por enquanto o projeto usa mods soltos em `run/mods`.

No futuro deve existir um pacote all-in-one do MagicWorld, mas isso fica para depois da estabilizacao. A prioridade agora e usar os mods externos separados para evitar bugs acumulados durante o port Forge 1.20.1.

Baixar sempre arquivos para:

- Minecraft `1.20.1`
- Loader `Forge`
- Nao usar arquivos `Fabric` nem `NeoForge` nesta base

## Essenciais para shaders

- Embeddium `0.3.31+mc1.20.1` ou versao Forge 1.20.1 equivalente:
  https://www.curseforge.com/minecraft/mc-mods/embeddium/files/all?gameVersionTypeId=1&version=1.20.1
- Oculus `mc1.20.1`:
  https://www.curseforge.com/minecraft/mc-mods/oculus/files?page=1&pageSize=20&version=1.20.1

Observacao: shaderpack sozinho em `run/shaderpacks` nao renderiza no Forge puro. O Oculus e o loader de shader; o Embeddium e a base/performance exigida para esta pilha.

## Essenciais para os resource packs MagicWorld

Os packs locais em `run/resourcepacks` possuem recursos no formato OptiFine, principalmente:

- `assets/minecraft/optifine/`
- `ctm/`
- `cem/`
- colormaps

Mods recomendados para esses recursos:

- Entity Texture Features:
  https://www.curseforge.com/minecraft/mc-mods/entity-texture-features-fabric/files/all?version=1.20.1
- Entity Model Features:
  https://www.curseforge.com/minecraft/mc-mods/entity-model-features/files/all?version=1.20.1
- Fusion Connected Textures:
  https://www.curseforge.com/minecraft/mc-mods/fusion-connected-textures/files/6070260
- Forge CIT:
  https://www.curseforge.com/minecraft/mc-mods/forge-cit/files/all?version=1.20.1

## Recomendados para desempenho e memoria

- ModernFix:
  https://www.curseforge.com/projects/790626/files/all?version=1.20.1
- FerriteCore:
  https://www.curseforge.com/minecraft/mc-mods/ferritecore/files/all?page=1&version=1.20.1
- Entity Culling:
  https://www.curseforge.com/minecraft/mc-mods/entityculling/files/all?gameVersionTypeId=1&page=1&pageSize=20&version=1.20.1

## Opcional para etapa futura

- Distant Horizons:
  https://www.curseforge.com/minecraft/mc-mods/distant-horizons/files/all?gameVersionTypeId=1&page=1&version=1.20.1

Nao ativar Distant Horizons como obrigatorio ainda. Ele deve entrar apenas depois de estabilizar Oculus, Embeddium e os resource packs, porque a combinacao de LOD + shaders pode exigir ajustes de compatibilidade.

## Nao usar nesta base

- Sodium
- Iris Fabric
- Continuity Fabric
- Arquivos NeoForge 26.x
- Qualquer JAR que nao declare Forge 1.20.1
