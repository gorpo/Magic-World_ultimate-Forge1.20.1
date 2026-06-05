# Wiki Tecnica MagicWorld

## Escopo

Projeto NeoForge 26.1.2 do mod `magicworld`. A base final concentra a experiencia em geracao inicial de mundo, portais funcionais, menus premium, varinha, castelo, propriedades, aldeoes vanilla, compatibilidade visual e pacote all-in-one.

## Fluxo Principal

1. `MagicWorld.java` registra itens, entidades, rede, eventos comuns e eventos de cliente.
2. `PlayerJoinEvents.java` entrega/ativa experiencia inicial conforme configuracao.
3. `StarterPortalEvents.java` controla portal inicial, propriedade, castelo, fazendas, casas, portais funcionais, retorno dimensional e populacao vanilla.
4. `ClientEvents.java` registra telas, atalhos, menus e ajustes visuais do cliente.
5. Menus em `client/menus` enviam comandos para o servidor via `MagicWorldNetwork.java`.
6. Recursos em `src/main/resources` definem metadados, assets, receitas, mixins e estruturas NBT.

## Arquivos Java Raiz

- `src/main/java/com/magicworld/MagicWorld.java`: classe principal do mod; registra item da varinha, armadura draconica, dragao pacifico, eventos, rede e configuracao.
- `src/main/java/com/magicworld/Config.java`: configuracoes comuns do mod e modos de inicio da experiencia visual/portal.
- `src/main/java/com/magicworld/MagicWorldClient.java`: entrada de inicializacao do lado cliente quando aplicavel.
- `src/main/java/com/magicworld/MagicWorldWorldOptions.java`: estado auxiliar usado na criacao de mundo e ativacao de comandos/opcoes MagicWorld.

## Eventos De Servidor

- `src/main/java/com/magicworld/event/StarterPortalEvents.java`: maior arquivo do projeto; gera propriedade, casa, castelo, fazendas, currais, aldeoes, portais e retornos dimensionais; tambem corrige visual/funcao dos portais e cooldown de uso.
- `src/main/java/com/magicworld/event/PlayerJoinEvents.java`: fluxo de entrada do jogador, entrega inicial e disparos de inicializacao.
- `src/main/java/com/magicworld/event/AuraEvents.java`: efeitos persistentes ligados a aura/poderes do jogador.
- `src/main/java/com/magicworld/event/CraftEvents.java`: interacoes de craft/clique associadas a itens do mod.
- `src/main/java/com/magicworld/event/MobEvents.java`: interacoes e regras envolvendo mobs.
- `src/main/java/com/magicworld/event/PremiumBlocks.java`: utilitarios/acoes premium relacionadas a blocos.
- `src/main/java/com/magicworld/event/PremiumEnemies.java`: geracao/controle de inimigos premium.
- `src/main/java/com/magicworld/event/StarterDragonManager.java`: suporte ao dragao inicial/pacifico.

## Mundo Inicial E Estruturas

- Casa inicial importada: `src/main/resources/data/magicworld/structure/imported_house.nbt`.
- Castelo importado: `src/main/resources/data/magicworld/structure/imported_castle.nbt`.
- Casa starter legacy: `src/main/resources/data/magicworld/structures/starter_house_1.nbt`.
- A geracao final usa mapa novo como fluxo principal. Evitar rotinas pesadas de correcao em tick para saves antigos.
- A praca compacta dos portais fica relativa a base em `base.offset(-24, 0, 48)`.
- Portais funcionais: Nether, End Portal e End Gateway. Ao chegar na outra dimensao, o spawn fica 4 blocos ao sul do portal de retorno salvo.
- Portais de retorno usam dados persistentes por jogador com prefixo `MagicWorldFunctionalReturnPortal`.
- Castelo: os rios de agua/lava da ponte foram removidos como liquidos superiores; ficam apenas canais secos baixos quando aplicado o ajuste final.
- Moradores externos do castelo usam abrigos abertos; `Ferreiro de Ferramentas` e `Cozinheiro do Salao` usam posicionamento forçado no chao.
- Casas verdes usam janelas coloridas, floreiras, varanda, cristais e decoracao magica.

## Cliente E Telas

- `src/main/java/com/magicworld/client/ClientEvents.java`: registro central de eventos do cliente; adiciona botoes, menus, title screen, painel MagicWorld na criacao de mundo e fechamento automatico quando abas vanilla retomam controle.
- `src/main/java/com/magicworld/client/KeyBindings.java`: atalhos de teclado, incluindo abertura da varinha.
- `src/main/java/com/magicworld/client/InitialLoadNoticeScreen.java`: aviso inicial visual.
- `src/main/java/com/magicworld/client/MagicWorldTitleScreen.java`: tela inicial customizada.
- `src/main/java/com/magicworld/client/MagicWorldSecretMinecraftScreen.java`: tela secreta/alternativa.
- `src/main/java/com/magicworld/client/PremiumMenuScreen.java`: menu premium principal.
- `src/main/java/com/magicworld/client/PremiumDetailsScreen.java`: tela de detalhes de entrada premium.
- `src/main/java/com/magicworld/client/PremiumPortalOptionsScreen.java`: tela de opcoes de portal/resource/shader.
- `src/main/java/com/magicworld/client/PremiumEntry.java`: modelo de entrada dos menus premium.
- `src/main/java/com/magicworld/client/MagicWorldCentralPauseScreen.java`: central MagicWorld acessada pelo pause.
- `src/main/java/com/magicworld/client/MagicWorldCentralOverviewScreen.java`: resumo da central.
- `src/main/java/com/magicworld/client/MagicWorldCentralDetailScreen.java`: detalhes por setor/residente.
- `src/main/java/com/magicworld/client/MagicWorldCentralUi.java`: componentes comuns da UI central.
- `src/main/java/com/magicworld/client/MagicWorldIconButton.java`: botao com icone.
- `src/main/java/com/magicworld/client/MagicWorldMenuButton.java`: botao padronizado dos menus.
- `src/main/java/com/magicworld/client/MagicWorldMenuTheme.java`: cores e estilo visual dos menus.
- `src/main/java/com/magicworld/client/MagicWorldPortalVisualController.java`: efeitos/visual do portal.
- `src/main/java/com/magicworld/client/MagicWorldClientCompat.java`: ajustes de compatibilidade cliente, incluindo Distant Horizons.
- `src/main/java/com/magicworld/client/MagicWorldDistantHorizonsButton.java`: integracao visual com Distant Horizons.
- `src/main/java/com/magicworld/client/MagicWorldEntityCulling.java`: controle/compatibilidade de culling visual.
- `src/main/java/com/magicworld/client/MagicWorldGraphicsProfile.java`: perfis graficos.

## Menus Premium

Todos ficam em `src/main/java/com/magicworld/client/menus`.

- `VarinhaMagicaControlCenter.java`: hub principal da varinha.
- `MenuEntryFactory.java`: fabrica/organiza entradas visuais dos menus.
- `WeatherControlMenu.java`: clima.
- `TimeControlMenu.java`: tempo/dia/noite.
- `BiomeTeleportMenu.java`: teleporte por bioma.
- `DimensionMenu.java`: dimensoes.
- `PortalMenu.java`: portais.
- `StructureRainMenu.java`: estruturas/recompensas.
- `DungeonSpawnerMenu.java`: spawners/dungeons.
- `MobSpawnerMenu.java`: mobs.
- `BossControlMenu.java`: bosses.
- `LuckyBlockMenu.java`: lucky blocks.
- `ParticleEffectsMenu.java`: particulas.
- `PremiumToolsMenu.java`: ferramentas.
- `PremiumArmorMenu.java`: armaduras.
- `PremiumPowersMenu.java`: poderes.
- `PremiumCompanionMenu.java`: companheiros.
- `PremiumMountsMenu.java`: montarias.
- `NPCMenu.java`: entradas antigas/compatibilidade de NPCs premium.
- `TransformationEncyclopedia.java`: transformacoes.
- `TrollMenu.java`: efeitos troll.
- `WaveSurvivalMenu.java`: sobrevivencia em ondas.
- `WorldEventsMenu.java`: eventos de mundo.
- `GraphicsProfilesMenu.java`: perfis graficos.

## Entidades E Itens

- `src/main/java/com/magicworld/item/VarinhaMagicaItem.java`: item da varinha e comportamento base.
- `src/main/java/com/magicworld/entity/PeacefulDragon.java`: dragao pacifico registrado pelo mod.
- `src/main/java/com/magicworld/entity/Premium*.java`: variantes premium de criaturas e utilitarios relacionados.
- `src/main/java/com/magicworld/entity/PremiumEntityTags.java`: marcadores auxiliares para entidades premium.

## Rede

- `src/main/java/com/magicworld/network/MagicWorldNetwork.java`: registro de payloads e execucao server-side das acoes chamadas pelos menus.

## Central MagicWorld

Arquivos em `src/main/java/com/magicworld/central`.

- `MagicWorldCentralData.java`: dados agregados exibidos pela central.
- `MagicWorldCentralSnapshot.java`: fotografia do estado atual.
- `MagicWorldCentralSector.java`: setores da propriedade/castelo/fazendas.
- `MagicWorldCentralResidentPlan.java`: planejamento/listagem de moradores.

## Mixins

Arquivos em `src/main/java/com/magicworld/mixin` e configurados por `src/main/resources/magicworld.mixins.json`.

- `MagicWorldLoadingOverlayMixin.java`: mantido vazio/intencional para preservar compatibilidade e evitar tela preta no carregamento.
- `MagicWorldScreenBackgroundMixin.java`: fundo visual MagicWorld.
- `CubeMapMagicWorldDepthMixin.java`: ajuste visual de profundidade no cubemap/tela.
- `MagicWorldEntityCullingMixin.java`: integracao com culling.
- `RenderSetupSamplerCompatMixin.java`: compatibilidade de render.
- `IrisConfigMagicWorldMixin.java`: ajuste de UI/config Iris.
- `SodiumConfigBuilderMagicWorldMixin.java`: ajuste de UI/config Sodium.
- `SodiumDonationButtonCompatMixin.java`: ajuste de botao externo Sodium.
- `SodiumExternalPageEntryMagicWorldMixin.java`: ajuste de entrada externa Sodium.
- `SodiumVideoSettingsScreenMagicWorldLayoutMixin.java`: ajuste de layout Sodium.

## Recursos

- `src/main/resources/META-INF/mods.toml`: metadados do mod.
- `src/main/resources/pack.mcmeta`: metadados do pacote de recursos interno.
- `src/main/resources/assets/magicworld/lang`: textos do mod.
- `src/main/resources/assets/magicworld/textures`: texturas de itens, GUI, equipamentos, previews e mobs.
- `src/main/resources/assets/magicworld/models`: modelos de item.
- `src/main/resources/assets/magicworld/equipment`: definicao visual de equipamento.
- `src/main/resources/data/magicworld/recipes/varinha_magica.json`: receita da varinha.
- `src/main/resources/assets/iris`, `assets/sodium`, `assets/distanthorizons`: traducoes/recursos de compatibilidade visual.

## Build E Distribuicao

- `build.gradle`: configura NeoForge, dependencias locais, jar normal e tarefa `magicWorldAllInOneJar`.
- `gradle.properties`: versoes do Minecraft/NeoForge/mod.
- `scripts/install-magicworld-tlauncher.ps1`: instalador PowerShell para TLauncher/.minecraft.
- `scripts/MagicWorldInstallerLauncher.cs`: launcher Windows do instalador.
- `installer/MagicWorldInstaller.exe`: executavel gerado do instalador.
- `pacote_distribuivel/.minecraft`: pacote local ignorado pelo Git para teste manual.
- Artefatos finais principais: `build/libs/MagicWorld-Ultimate-NeoForge-26.1.2-1.2.0.jar` e `build/libs/MagicWorld-Ultimate-NeoForge-26.1.2-1.2.0-all-in-one.jar`.

## Regras De Manutencao

- README deve continuar ludico, simples e voltado ao jogador.
- Wiki deve concentrar detalhes tecnicos.
- Nao reintroduzir o sistema antigo de habitantes customizados.
- Nao criar scans grandes em tick.
- Nao mover a praca dos portais sem pedido explicito.
- Nao remover cercas dos currais ao mexer na borda viva.
- Compilar antes de commitar.
- Gerar jar e pacote local antes de release final.
