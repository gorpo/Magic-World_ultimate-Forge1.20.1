# Mods recomendados para `run/mods`

Atualizado em 2026-06-07.

## Regra atual

Por enquanto o projeto usa mods soltos em `run/mods`/`run/dev-mods` durante desenvolvimento.

O primeiro pacote all-in-one local fica em `pacote_distribuivel/.minecraft` e deve conter apenas os mods ja validados.

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

Nao adicionar estes mods no all-in-one inicial. Eles devem ser testados depois, em lotes pequenos.

## Validado no all-in-one inicial

- Distant Horizons:
  https://www.curseforge.com/minecraft/mc-mods/distant-horizons/files/all?gameVersionTypeId=1&page=1&version=1.20.1

Distant Horizons foi validado junto com Embeddium e Oculus no `runClient` e entra no primeiro pacote all-in-one de teste.

## All-in-one gameplay local

Branch de trabalho: `inicio-all-in-one`.

Pacote local gerado:

```text
pacote_distribuivel/MagicWorldUltimate-Forge1.20.1-AllInOne-Gameplay-Teste2.zip
```

Entraram no pacote expandido:

- Performance/render: Embeddium, Oculus, Distant Horizons, ModernFix `5.27.44`, FerriteCore e ImmediatelyFast.
- Resource pack/visual: Entity Texture Features, Entity Model Features, CTM, Fusion, CIT Resewn e BetterFoliage.
- Gameplay antigo do usuario: JourneyMap, MineColonies, Minecraft Comes Alive, Roughly Enough Items, WorldEdit e Tectonic.
- Dependencias incluidas: Architectury, Cloth Config, BlockUI, Structurize, Domum Ornamentum, Lithostitched e Framework.

Atencao: MineColonies entrou com suas dependencias locais obrigatorias:

- `structurize`;
- `blockui`;
- `domum_ornamentum`;
- `journeymap` como integracao opcional.

## Pendencias do all-in-one

Effortless Building `1.20.1-3.11` ainda nao entrou.

Motivo tecnico: o proprio `mods.toml` do JAR declara dependencias obrigatorias ausentes:

- `flywheel` `[1.0.0,2.0)`;
- `ponder` `[0.8,)`.

Se `effortlessbuilding-1.20.1-3.11.jar` for colocado agora em `mods`, o Forge deve falhar antes do menu. A entrada correta e adicionar primeiro JARs Forge 1.20.1 compativeis de Flywheel e Ponder ou manter Effortless Building como download separado com credito ao criador.

## Nao adicionar Entity Culling externo

Nao colocar o mod externo **Entity Culling** nas pastas futuras do all-in-one.

Motivo: o Magic World ja inclui culling interno no proprio mod:

- `MagicWorldEntityCulling`;
- `MagicWorldEntityCullingMixin`;
- registro em `magicworld.mixins.json`.

Esse culling interno usa cache por entidade/camera e raycast de oclusao. Se for necessario desligar para diagnostico, use:

```text
-Dmagicworld.entity_culling=false
```

## Nao usar nesta base

- Sodium
- Iris Fabric
- Continuity Fabric
- Arquivos NeoForge 26.x
- Qualquer JAR que nao declare Forge 1.20.1
