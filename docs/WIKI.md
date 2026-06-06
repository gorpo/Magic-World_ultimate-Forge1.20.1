# Magic World Ultimate Forge - Wiki de Trabalho

Esta wiki foi reiniciada em 2026-06-05 para o novo port controlado do projeto NeoForge 26 para Forge 1.20.1.

## Mods externos em `run/mods`

Nesta fase, os mods externos ficam soltos em `run/mods`.

Futuramente o projeto deve ter um pacote all-in-one do MagicWorld, mas isso so deve ser feito depois que shaders, resource packs e compatibilidade estiverem estaveis.

Lista atual de mods recomendados: `docs/MODS_RECOMMENDED.md`.

Regra: baixar sempre `Minecraft 1.20.1` + `Forge`. Nao usar arquivos `Fabric` nem `NeoForge` nesta base Forge.

## Regra principal

A pasta `docs/neoforge-reference/` e somente referencia historica do projeto NeoForge. Ela guia o que queremos portar, mas nao representa automaticamente o estado atual deste Forge.

O estado real deste projeto Forge deve ser registrado neste arquivo e em `CODEX_HANDOFF.md`.

## Escopo da etapa atual

Portar somente:

- inicio do jogo;
- comportamento inicial dos menus ao abrir;
- menus iniciais da criacao de mundo;
- planos de fundo estaticos das telas;
- tela de loading inicial.

Fora de escopo nesta etapa:

- gameplay completo do NeoForge;
- entidades custom novas;
- sistemas de resource/shader/installer;
- estruturas novas alem do minimo ja existente;
- refatorar modid/pacote inteiro.

## Branch de trabalho

`Inicio-Port-Neoforge`

Motivo: `Inicio Port Neoforge` com espacos nao e nome valido de branch Git.

## Origem NeoForge

`C:\Users\guilh\Desktop\MinecraftProjects\neoforge-26.1.2-mdk`

Arquivos de referencia importados:

- `docs/neoforge-reference/MAGIC_WORLD_WIKI.md`
- `docs/neoforge-reference/README_NEOFORGE.md`
- `docs/neoforge-reference/CODEX_HANDOFF_NEOFORGE.md`
- `docs/neoforge-reference/porting-forge-1.20.1/README.md`

## Protocolo se a sessao cair

1. Abrir `CODEX_HANDOFF.md` primeiro.
2. Conferir `git status --short --branch`.
3. Confirmar que esta no branch `Inicio-Port-Neoforge`.
4. Ler esta wiki de trabalho.
5. Consultar `docs/neoforge-reference/` somente para entender a origem NeoForge.
6. Continuar apenas o escopo da etapa atual, sem puxar sistemas extras.
7. Ao terminar qualquer bloco, atualizar `CODEX_HANDOFF.md` com feito, pendente e proximo passo.

## Estado atual

- Branch atual: `Inicio-Port-Neoforge`, alinhado com `origin/Inicio-Port-Neoforge`.
- Remoto configurado para `https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1.git`.
- Handoff e wiki locais existem e devem continuar sendo atualizados ao fim de cada bloco.
- Documentacao NeoForge esta em `docs/neoforge-reference/` apenas como referencia.
- O escopo inicial de telas, menus, backgrounds e loading foi portado em blocos posteriores desta wiki.
- HEAD atual na retomada de 2026-06-06: `6a0a554` (`Revisa dropdown de seeds do Magic World - 2026-06-06 15:42:01 -03:00`).
- Validacao de retomada em 2026-06-06 15:52:33 -03:00: `./gradlew.bat build --stacktrace` com `BUILD SUCCESSFUL` e `git diff --check` sem erros.
- O cliente Minecraft nao deve ser aberto pelo Codex; testes visuais continuam com o usuario.

## Atualizacao visual inicial - 2026-06-05

Implementado nesta etapa:

- Background estatico oficial importado de `screenshots/title_background_static_2560x1440_final.png`.
- Logo full importada de `screenshots/logo_full.png`.
- Tela de titulo customizada `MagicWorldTitleScreen` substitui a tela vanilla do Minecraft.
- `MagicWorldStaticBackground` centraliza o desenho do fundo para telas iniciais e loading.
- `InitialLoadNoticeScreen` agora usa o background estatico e fecha automaticamente 5s apos completar.
- Build local validado com sucesso.

Pendencia visual para teste manual:

- Confirmar dentro do jogo se o evento `ScreenEvent.BackgroundRendered` cobre todas as telas iniciais esperadas sem esconder widgets.
- Confirmar se o loading vanilla antes da abertura do menu precisa de mixin/overlay adicional.

## Ajuste de tela inicial - 2026-06-05

Apos teste visual:

- A logo full estava grande demais e foi reduzida para largura maxima de 300 px.
- Os botoes vanilla foram substituidos por `MagicWorldMenuButton`, adaptado do NeoForge.
- `MagicWorldMenuTheme` foi portado de forma compativel com Forge 1.20.1 usando `GuiGraphics`.
- Build local passou.

Pendente:

- Validar no jogo se o layout ficou igual ao esperado.
- Portar os botoes rapidos/menus extras do NeoForge se o layout principal estiver aprovado.

## Rename e Java - 2026-06-05

Decisao tecnica:

- O projeto pode ser aberto no IntelliJ com SDK Java 21.
- O artefato Forge 1.20.1 deve continuar compilando com target/toolchain Java 17.
- Motivo: Minecraft Forge 1.20.1 e o runtime padrao da versao usam Java 17; compilar para Java 21 pode quebrar o carregamento do mod para jogadores.

Rename feito:

- `examplemod` foi substituido por `magicworld` em codigo, configs, assets, data e docs do projeto.
- Pacote Java atual: `com.magicworld`.
- Classe principal atual: `MagicWorld`.
- Mod id atual: `magicworld`.

Pastas auxiliares:

- `assets/distanthorizons`, `assets/iris`, `assets/sodium` e `assets/minecraft` ficam como referencia local por enquanto.
- Elas nao devem ir para o Git nem para o jar nesta etapa.

## IntelliJ Java 17 - 2026-06-05

O IntelliJ foi configurado de volta para Java 17. Essa e a configuracao recomendada para este projeto Forge 1.20.1.

Manter:

- IDE SDK: Java 17
- Gradle JVM: Java 17
- `java.toolchain.languageVersion`: 17

## Fix runClient - 2026-06-05

Problema:

- Minecraft falhava ao iniciar com `ParsingException: Invalid bare key: ?#`.

Causa:

- Arquivo `mods.toml` tinha BOM UTF-8 no primeiro caractere, gerado durante rename/script.

Correcao:

- Removido BOM de `mods.toml` e demais arquivos textuais afetados.
- `runClient` voltou a carregar o cliente sem crash inicial.

## Downgrade controlado NeoForge -> Forge 1.20.1 - 2026-06-05

Regra desta fase:

- Primeiro portar somente telas antes do mapa: tela inicial, menus iniciais, criacao de mundo e loadings.
- Nao trazer sistemas de mundo, entidades, rede, mixins de compat ou dependencias externas no mesmo bloco.
- Usar `docs/neoforge-reference/` como referencia, mas adaptar APIs para Forge 1.20.1.

Aplicado agora:

- Memoria Gradle/runClient ajustada para 8 GB (`-Xmx8G`) com inicio em 4 GB (`-Xms4G`).
- Versao do mod definida como `1.0.0.1`.
- Title screen mostra `Magic World 1.0.0.1` no canto inferior esquerdo.
- Botoes pequenos de idioma e acessibilidade voltaram ao title screen.
- Criacao de mundo recebeu botao e painel `Magic World`, inspirado no projeto NeoForge.
- Painel atual inclui Portal, Castelo, Fazendas, Aura, PC, Dificuldade, Modo, Criar Mundo e Voltar.
- Background estatico foi aplicado tambem a telas de criacao/selecao de mundo e `LevelLoadingScreen`.
- `./gradlew.bat build` passou com sucesso.

Pendente para teste visual:

- Confirmar se o background cobre a tela de loading de mapa vista pelo usuario.
- Confirmar se o painel Magic World aparece na criacao de mundo e se o botao `Voltar` restaura a tela vanilla.
- Confirmar se idioma/acessibilidade aparecem em tamanho e posicao aceitaveis.

Proximos blocos planejados:

1. Ajustar overlay/mixin se algum loading vanilla ainda ignorar o background.
2. Portar comportamento real dos botoes da aba Magic World, um sistema por vez.
3. Portar menus centrais e compat somente depois dos menus iniciais estarem aprovados.
## Ajuste title screen aprovado - 2026-06-05

A tela inicial foi realinhada ao codigo aprovado da versao NeoForge:

- `MagicWorldIconButton` foi portado para Forge 1.20.1.
- Botoes inferiores agora sao customizados, nao vanilla.
- Layout inferior contem 5 atalhos: Multiplayer, Idioma, Controles, Pacotes e Acessibilidade.
- Main menu voltou ao desenho do NeoForge: JOGAR, OPCOES, MODS e SAIR.
- Logo limitada ao painel central para manter proporcao visual.
- Build passou com sucesso.
## Fix background nos submenus da criacao de mundo - 2026-06-05

Correcoes aplicadas:

- Background Magic World agora tambem cobre telas que usam `renderDirtBackground`.
- Foi criado override de textura vanilla `assets/minecraft/textures/gui/options_background.png` para bloquear o fundo vanilla das telas de opcoes/criacao.
- Foi adicionado mixin client-side para interceptar `Screen.renderBackground` e `Screen.renderDirtBackground` nas telas iniciais.
- O jar foi verificado e contem o override de textura e o mixin.

Regra mantida:

- A pasta auxiliar `assets/minecraft/textures/gui/title/**` continua excluida do jar.
- Apenas `assets/minecraft/textures/gui/options_background.png` entra no build nesta etapa.
## Fix logo title screen - 2026-06-05

- A logo da tela inicial agora usa o asset aprovado do NeoForge: `assets/magicworld/textures/gui/title_logo.png`.
- O codigo deixou de depender de `textures/gui/title/logo_full.png` para o title screen.
- Proporcao corrigida para 512x171.
- Build validado com sucesso.

## Fix mosaico/background/logo - 2026-06-05

- Removido override vanilla `options_background.png`, pois o Minecraft repete essa textura em mosaico.
- `assets/minecraft/**` voltou a ser apenas referencia local e nao entra no jar.
- `runClient` agora recebe `--mixin.config magicworld.mixins.json` para garantir carregamento do mixin em ambiente dev.
- Logo corrigida usando o overload correto de `GuiGraphics.blit`, escalando a textura inteira.
- Build passou e `runClient` carregou sem crash.
## Ajuste abas criacao de mundo - 2026-06-05

- O botao `Magic World` agora so aparece na aba principal da criacao de mundo.
- O botao fica oculto nas abas `Mundo` e `Mais`.
- Adicionado mixin especifico para `CreateWorldScreen.renderDirtBackground`, cobrindo a subtela que ainda ficava sem fundo.
- Build e runClient sem crash.
## Fix final de fundos iniciais - 2026-06-05

- `GenericDirtMessageScreen` e `ProgressScreen` entraram na lista de telas com background estatico, cobrindo a mensagem `Preparando a criacao do mundo...`.
- `SelectWorldScreen` recebeu mixin proprio para remover o fundo da lista de mundos e desenhar o background antes da lista/widgets.
- `CreateWorldScreen` redesenha o background antes dos widgets para evitar faixa vanilla por baixo das abas.
- `TabNavigationBar` nao desenha mais preenchimento preto nem separador vanilla nesta etapa.
- `TabButton` nao usa mais `textures/gui/tab_button.png`; cada aba desenha um recorte do background Magic World com moldura/texto por cima.
- Build passou com `./gradlew.bat build --stacktrace`.

Pendente:

- Teste visual no jogo apos reiniciar o client.
- Confirmar se as abas estao boas com o recorte do background ou se devem copiar exatamente o estilo NeoForge.
## Import funcional terreno NeoForge - 2026-06-05

O projeto entrou na fase seguinte do downgrade controlado: terreno inicial e sistemas de mundo ligados ao menu Magic World.

Implementado:

- `StarterPortalEvents` agora controla a criacao da propriedade inicial em etapas.
- Casa importada usa `data/magicworld/structures/imported_house.nbt`.
- Castelo importado usa `data/magicworld/structures/imported_castle.nbt`.
- O menu Magic World da criacao de mundo agora tem efeito real em:
  - `Portal: ON/OFF`
  - `Castelo: ON/OFF`
  - `Fazendas: ON/OFF`
  - `Aura: ON/OFF`
  - `Modo: Normal/Criativo`
- Foram adicionadas lavouras maduras, currais, animais vanilla, casas simples de trabalhadores e villagers nomeados.
- Foi adicionada praca compacta de portais funcionais com Nether, End e Gateway.
- Portais criam plataformas de retorno nas dimensoes destino e retornam para a propriedade.
- `AuraEvents` foi criado e registrado no Forge event bus.
- Aura inicial protege contra dano ambiental, queda, fogo, fome/agua, aplica efeitos invisiveis e preserva retorno pos-morte.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: sucesso.
- `./gradlew.bat build --stacktrace`: sucesso.

Nao incluido ainda:

- Port bruto completo do `StarterPortalEvents` NeoForge de 6 mil linhas.
- Entidade custom real do dragao.
- Armaduras draconicas custom.
- IA completa dos trabalhadores do NeoForge.
- Compat/menus de Iris, Sodium, Distant Horizons e central premium.

Teste manual necessario:

- Criar mundo novo com todos os toggles ligados.
- Confirmar casa/castelo/fazendas/portais/aura.
- Se o terreno ficar pesado demais ou demorar demais na geracao, ajustar os passos de limpeza/colocacao de estrutura antes de portar mais detalhes.
## Loading sem mapa de chunks - 2026-06-05

- Removido o quadrado central vanilla da `LevelLoadingScreen`.
- O mixin `MagicWorldLevelLoadingScreenMixin` cancela apenas `renderChunks(...)`.
- A porcentagem e o background Magic World continuam aparecendo.
- Build passou com `./gradlew.bat build --stacktrace`.
## Fix posicionamento castelo/propriedade - 2026-06-05

- O primeiro teste de mundo novo mostrou apenas o castelo.
- Causa: o Forge estava posicionando o castelo como se o offset fosse centro; no NeoForge ele e uma ancora lateral.
- A limpeza do castelo apagava casa, portal e fazendas.
- Corrigido `castleOrigin`/`castleCenter` para seguir a logica do NeoForge.
- O jogador agora e teleportado para spawn seguro da propriedade ao fim da geracao e recebe respawn ali.
- Build passou.
## Criativo, cheats e aura pelo menu Magic World - 2026-06-05

- O menu Magic World nao depende mais de clicar nos botoes vanilla por texto para modo/dificuldade.
- Antes de criar o mundo, o codigo grava direto no `WorldCreationUiState`:
  - modo criativo/sobrevivencia;
  - dificuldade;
  - cheats/comandos ligados;
  - gamerules de teste.
- Gamerules Magic World:
  - `keepInventory=true`
  - `drowningDamage=false`
  - `fallDamage=false`
  - `fireDamage=false`
  - `freezeDamage=false`
  - `doImmediateRespawn=true`
  - `sendCommandFeedback=true`
  - `commandBlockOutput=true`
- O servidor tambem reaplica essas regras no login e ao fim da geracao.
- Em modo criativo, o jogador entra em criativo e recebe permissao de comandos.
- Aura ON protege contra lava/fogo, agua/afogamento, altura/queda, fome, preserva inventario, tenta voltar ao local da morte, quebra bloco com clique esquerdo e mata entidades em um golpe.
- Build passou.

## Background em confirmacoes - 2026-06-05

- A tela de apagar mundo usa `ConfirmScreen`, fora do pacote `worldselection`.
- `ConfirmScreen`, `AlertScreen` e `BackupConfirmScreen` agora entram na regra global de background Magic World.
- Isso cobre a confirmacao de apagar mundo e alertas iniciais semelhantes.
- Build passou com `./gradlew.bat build --stacktrace`.

Teste manual:

- Reiniciar o client e abrir a tela de apagar mundo.
- Confirmar que nao aparece mais o fundo vanilla de terra.

## Loading com logo e reforco do terreno - 2026-06-05

- A tela de loading inicial agora exibe a logo Magic World e barra de progresso azul/dourada.
- O entorno importado recebeu reforco parcial do codigo NeoForge:
  - casas maiores dos trabalhadores da plantacao;
  - rancho/deposito da plantacao;
  - casas dos cuidadores das fazendas;
  - villagers nomeados nesses pontos;
  - caminhos e iluminacao de ruas;
  - decoracao extra na praca dos portais;
  - residentes do castelo com estacoes, camas e baus.
- A casa importada foi preservada; o foco foi o entorno que tinha ficado vazio.
- Build passou com `./gradlew.bat build --stacktrace`.

Observacao:

- Este bloco nao e o port bruto completo de `StarterPortalEvents` NeoForge. Ainda podem faltar detalhes finos de decoracao/IA, que devem ser ajustados por screenshot apos criar mundo novo.

## Fix loading, ESC, aura e terreno - 2026-06-05

- Loading usa a logo full 2172x724 e nao mostra mais `Primeira criacao de mapa`.
- Aura reforcada para remover efeitos visiveis antes de aplicar efeitos invisiveis/sem particulas/sem icones.
- Menu Magic World do ESC foi portado em versao Forge: botao `MagicWorld` no pause abre atalhos e menu da varinha.
- Caminhos novos nao usam mais `DIRT_PATH`, removendo o rebaixo marrom.
- Currais seguem o padrao NeoForge: piso de grama e cerca/gates acima do piso.
- Estruturas de villagers do castelo agora usam fallback para chao/superficie quando nao encontram piso interno seguro.
- Predio da mina e mina subterranea foram adicionados no setor do gramado.
- Build passou com `./gradlew.bat build --stacktrace`.

Pendente de teste visual:

- Criar mundo novo e conferir se ainda existe area vazia, estrutura voando, animal escapando ou terreno desnivelado.

## Ajuste logo, central ESC e menu grafico - 2026-06-05

- Logo da tela inicial reduzida para nao invadir texto nem estourar pixel.
- O botao `MagicWorld` no pause agora abre a central portada do NeoForge, com tela de visao geral e detalhes.
- Portado pacote leve `com.magicworld.central` para alimentar a central:
  - secoes Varinha, Tempo, Portais, Fazendas, Castelo, Biomas, Encantos, Mundo, Premium e Proximas etapas.
- `PremiumMenuScreen` recebeu abas em multiplas linhas como no NeoForge, evitando layout quebrado em submenus.
- Menu `Graficos` ganhou icone de luneta e textos focados em Oculus, Embeddium, shaderpacks e resourcepacks.
- Build validado com `./gradlew.bat build`.

Teste manual:

- Abrir o client e conferir a logo menor na tela inicial.
- Entrar no mundo, apertar ESC e clicar em `MagicWorld`.
- Confirmar se a central abre igual ao fluxo do NeoForge.
- Abrir `Menu completo` > `Sistema` > `Graficos` e conferir layout/icone.

Nota:

- `mods/mods.txt` aparece deletado no status do Git, mas esta alteracao nao foi feita nesta etapa e precisa de confirmacao antes de commit.

## Terreno importado e limite da propriedade - 2026-06-05

- O Forge estava usando uma versao reduzida do `StarterPortalEvents` comparada ao NeoForge.
- Corrigida a fundacao global da area inicial importada antes da casa/fazendas:
  - X `-128..122`;
  - Z `-76..80`;
  - subsolo preenchido com dirt;
  - gramado fixo;
  - ar limpo acima para evitar buracos e sobras de terreno.
- O limite da propriedade agora usa folhas, arbustos e pequenas arvores como no conceito do NeoForge.
- O fallback dos villagers do castelo agora procura chao caminhavel proximo em vez de manter coordenada alta antiga.
- A mina continua marcada para gerar em `base.offset(67, -1, 46)` dentro da etapa de fazendas/estrutura da propriedade.
- Build validado com `./gradlew.bat build`.

Teste manual:

- Criar mundo novo sempre.
- Conferir gramado sem buracos.
- Conferir limite vivo da area.
- Conferir mina no gramado.
- Conferir villagers do castelo sem estruturas voando.

## Fix botao MagicWorld no ESC - 2026-06-05

- O botao `MagicWorld` do pause estava sobreposto/competindo com `Mods`.
- `ClientEvents#tunePauseMenu` agora replica o ajuste do NeoForge:
  - reposiciona `Mods`;
  - cria `MagicWorld` abaixo de LAN;
  - abre `MagicWorldCentralPauseScreen`.
- Build passou.

## Logo menor no menu ESC - 2026-06-05

- A escala grande estava em `MagicWorldCentralPauseScreen`.
- A logo do menu ESC agora usa no maximo `120px`.
- Textos e botoes foram reposicionados para nao ficarem por tras da logo.
- Build passou.

## Abrigos dos villagers do castelo no chao - 2026-06-05

- Corrigido spawn/decoracao dos residents do castelo.
- Se o ponto encontrado for alto e sem teto, o codigo procura chao caminhavel proximo e desce o abrigo antes de criar cama, mesa, bau e estacao.
- Build passou.

## Buracos no entorno da casa - 2026-06-05

- Adicionada estabilizacao localizada do terreno ao redor da casa importada.
- O patch preenche laterais/fundos da casa sem apagar estruturas.
- Ele cria suporte de terra e gramado no anel externo da casa, preservando o footprint da construcao.
- Build passou.


## Praca verde dentro dos limites - 2026-06-05

- A praca verde do Forge estava sendo gerada fora da propriedade, em `Z=96`, enquanto o limite maximo do terreno importado e `Z=80`.
- `buildGreenVillageSquare` foi movido para o setor verde interno da area inicial.
- O distrito verde agora recebe estabilizacao propria de chao antes da praca ser criada:
  - faixa `X -126..-84`;
  - faixa `Z 0..70`;
  - suporte de dirt abaixo;
  - gramado na altura correta;
  - limpeza de ar acima sem apagar blocos protegidos.
- Casas da praca foram reposicionadas para ficarem dentro do limite e fora do footprint da casa importada.
- Ruas internas foram adicionadas para conectar esse setor verde ao eixo oeste.
- Build passou com `./gradlew.bat build`.

Teste pendente:

- Criar mundo novo e validar visualmente se as casas/ruas/construcoes nao aparecem mais fora da barreira viva.
- Conferir se o vao do gramado nessa area foi fechado.

## Portal original e casa da mina - 2026-06-05

- O portal inicial recebeu de volta a decoracao do jardim do NeoForge:
  - flores;
  - musgo;
  - samambaias;
  - azaleias;
  - lampioes;
  - fogueiras decorativas;
  - animais nomeados ao redor.
- A casa da mina deixou de usar heightmap/fallback de altura e voltou a usar a ancora fixa `base.offset(67, -1, 46)`.
- Isso alinha predio, baus, armaduras e mina subterranea na mesma posicao.
- A casa da mina agora inclui suportes de armadura, banners, baus e decoracao de oficina.
- Build validado com `./gradlew.bat build`.

Teste pendente:

- Criar mundo novo e validar se o portal esta como no NeoForge.
- Validar se o predio da mina aparece no gramado grande e se os itens nao ficam soltos no chao.
- Validar se villagers do castelo continuam no chao/acessiveis.

## Layout dos botoes Mods e MagicWorld no ESC - 2026-06-05

- `Mods` e `MagicWorld` foram reorganizados no menu ESC.
- Ambos ficam em linhas separadas, centralizados e com a mesma largura/altura.
- O botao de saida e reposicionado para baixo para nao sobrepor.
- Build passou com `./gradlew.bat build`.

Teste pendente:

- Abrir o pause menu e confirmar alinhamento visual dos tres botoes inferiores.

## Ajustes finos de terreno, rua, mina e castelo - 2026-06-06

- A limpeza ao redor da casa importada foi limitada para nao apagar partes decorativas/estruturais da frente.
- Ruas receberam acabamento de meia altura com slabs nas laterais.
- Foram adicionadas faixas de estabilizacao para fechar buracos perto do limite, da casa e das plantacoes.
- A casa da mina agora reforca piso, paredes, telhado e porta depois da decoracao para evitar que aparecam somente armaduras/itens.
- Os residents do castelo agora descem para chao caminhavel quando o ponto original esta alto demais.
- Build passou com `./gradlew.bat build`.

Teste pendente:

- Criar mundo novo e validar visualmente frente da casa, ruas, predio da mina, villagers do castelo e buracos perto das plantacoes.

## Menu secreto do pause - 2026-06-06

- O menu `ESC > MagicWorld` recebeu de volta o botao `Menu secreto do Minecraft`.
- Foi criada a tela `MagicWorldSecretMinecraftScreen` em Forge 1.20.1.
- Abas disponiveis: Jogador, Mundo, Teleporte, Itens e Resources.
- Acoes de servidor foram adicionadas ao `MagicWorldNetwork` para poderes, itens, gamemode, gamerules, spawn, kits e teleporte.
- Build passou com `./gradlew.bat build`.

Teste pendente:

- Abrir `ESC > MagicWorld > Menu secreto do Minecraft` e conferir se as abas e acoes funcionam.

## Correcao layout pause Mods/MagicWorld - 2026-06-06

- `Mods` e `MagicWorld` agora sao inseridos abaixo do LAN/Opcoes como botoes largos.
- Todos os botoes vanilla abaixo sao deslocados para baixo, evitando sobreposicao.
- Build passou com `./gradlew.bat build`.

Teste pendente:

- Abrir o pause menu e validar alinhamento/espacamento visual.

## Personalizacao da tela grafica Embeddium - 2026-06-06

- Portada para Forge 1.20.1 a personalizacao visual da tela grafica existente no projeto NeoForge.
- A tela moderna do Embeddium agora usa background estatico Magic World, titulo `Magic World - Graficos` e o icone `screenshots/config-icon.png`.
- Os botoes de doacao do Embeddium foram ocultados.
- O mixin e opcional para manter o Magic World funcional quando Embeddium nao estiver instalado.
- Build passou e o cliente iniciou com Embeddium/Oculus sem erro do novo mixin.

### Correcao ao abrir Configuracoes de video

- Corrigido `VerifyError: Bad type on operand stack` ao clicar em Configuracoes de video.
- Causa: o injector que troca o titulo usava uma chamada de instancia antes do construtor do Embeddium terminar.
- O injector de titulo agora e estatico e nao acessa `this` durante o construtor.
- Build passou com `./gradlew.bat build --stacktrace`.

### Identidade visual completa do menu grafico

- O cabecalho `Embeddium` foi renomeado para `Magic World`.
- O cabecalho `Oculus` foi renomeado para `Magic World Shaders`.
- Os icones originais de Embeddium/Oculus na barra esquerda foram substituidos pelo icone Magic World.
- Destaques rosas, selecoes, checkboxes e linhas do menu foram trocados pelo tema azul-ciano usado no projeto NeoForge.
- Build passou e o cliente iniciou sem erro de aplicacao dos novos mixins.

### Revisao fiel do port NeoForge

- Corrigidos os IDs internos reais usados pelo Embeddium Forge:
  - `sodium` passa a aparecer como `Magic World`;
  - `iris` passa a aparecer como `Magic World Shaders`.
- Portado o botao `Horizontes Distantes` para o rodape esquerdo do menu grafico.
- O botao usa o icone Magic World e abre a classe Forge real `GetConfigScreen_forge`.
- O jar do Distant Horizons deve ficar em `run/dev-mods` durante o desenvolvimento.
- Corrigidos os hooks herdados do Minecraft para os nomes runtime do Forge:
  - `m_7856_` adiciona o botao do Distant Horizons durante a inicializacao;
  - `m_280273_` aplica o background Magic World;
  - `m_88315_` aplica logo, cores e linhas personalizadas.
- O logo principal do Embeddium tambem foi substituido pelo logo grafico Magic World.
- Removido o icone avulso do rodape que poderia sobrepor o botao do Distant Horizons.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

## Conclusao do menu grafico e decoracao do casarao - 2026-06-06

- O menu grafico recebeu rolagem circular entre paginas ao ultrapassar o limite superior ou inferior.
- O botao `Horizontes Distantes` e criado no rodape esquerdo mesmo com inicializacao tardia do mod.
- Botoes de acao do Embeddium foram movidos para o rodape esquerdo, como no layout NeoForge.
- O tema ciano Magic World foi ampliado para mais estados visuais do Embeddium.
- O reparo versionado da propriedade passou para a versao 3.
- O casarao importado agora recebe acabamento acima das portas, mais janelas, iluminacao, plantas, mobiliario e estacoes uteis.
- Foram adicionados baus internos com varinhas Magic World, ferramentas e itens raros/premium.
- Foram adicionadas exposicoes de armadura Netherite e de um conjunto Netherite nomeado `Magic World`.
- A armadura customizada real do NeoForge ainda nao existe como item registrado neste Forge 1.20.1.

### Regra de validacao

- O Codex nao deve executar `runClient` nem abrir o cliente Minecraft.
- Testes do Codex devem usar somente Gradle; testes visuais dentro do cliente ficam com o usuario.
- O cliente abriu a tela grafica com Magic World, Embeddium, Oculus e Distant Horizons `3.0.3-b` sem erro de mixin.

## Reparo conservador da propriedade e menu grafico - 2026-06-06

- Adicionado reparo versionado para propriedades ja criadas; ele executa uma vez no proximo login.
- A casa importada e restaurada pelo NBT original sem repetir a limpeza destrutiva ao redor.
- O NBT `imported_house.nbt` do Forge foi confirmado como identico ao NeoForge por hash SHA-256.
- Valos com ar no nivel do solo sao preenchidos com suporte de terra e grama, sem substituir agua ou construcoes.
- A rua frontal foi normalizada e recebeu acabamento de meio bloco nas laterais.
- A casa de pedra da mina e reconstruida por ultimo, com piso, paredes, janelas, telhado decorado, entrada, baus e armaduras internas.
- O botao injetado pelo Distant Horizons no menu de pausa agora e ocultado.
- O botao `Horizontes Distantes` e renderizado no rodape esquerdo do menu grafico, acima dos botoes de acao.
- `Support Sodium` e ocultado antes e depois da montagem do frame do Embeddium.
- O jar do Distant Horizons foi movido novamente para `run/dev-mods`.
- Build validado com `./gradlew.bat build --stacktrace`.

## Correcao responsiva do menu Itens e icone do Distant Horizons - 2026-06-06

- O menu secreto `Itens` agora calcula a quantidade de colunas pela largura real do painel.
- As categorias quebram em varias linhas em janelas pequenas e a grade usa recorte para impedir itens fora do painel.
- Abas superiores tambem reduzem de largura quando necessario.
- O icone pequeno injetado pelo Distant Horizons foi identificado em `OptionsScreen` e agora e ocultado em qualquer tela vanilla.
- O acesso ao Distant Horizons permanece somente no rodape esquerdo do menu grafico Magic World.
- O reparo da casa da mina preserva armaduras nomeadas existentes e evita criar duplicatas.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

## Registro consolidado - 2026-06-06 10:24:52 -03:00

Este registro documenta o conjunto completo preparado para o commit de conclusao do menu grafico, menus responsivos e reparos da propriedade.

### Menu grafico Magic World

- Portada a identidade visual do projeto NeoForge para o Embeddium Forge 1.20.1.
- Renomeados os grupos Sodium/Embeddium e Iris/Oculus para `Magic World` e `Magic World Shaders`.
- Aplicados logo, icones laterais, background, linhas e destaques em azul-ciano.
- Removido `Support Sodium` e ocultados os botoes de doacao.
- Adicionado acesso ao Distant Horizons no rodape esquerdo do menu grafico.
- Removidos os botoes/atalhos do Distant Horizons injetados nas telas vanilla e no menu de pausa.
- Reorganizados `Fechar`, `Aplicar` e `Desfazer` no rodape esquerdo.
- Adicionada rolagem circular entre paginas ao ultrapassar o inicio ou fim da pagina atual.

### Menus responsivos

- A tela secreta de itens calcula abas, categorias, colunas e linhas conforme a largura disponivel.
- Categorias podem ocupar varias linhas.
- A grade usa recorte para impedir itens fora do painel em janela pequena.
- Tooltips continuam aparecendo fora da area recortada.

### Propriedade e construcoes

- Criado reparo versionado da propriedade, atualmente na versao 3.
- O reparo restaura a casa importada, normaliza a rua frontal, adiciona acabamento de meio bloco e preenche valos de ar no nivel do solo.
- A area da mina e preservada pelo nivelamento geral e sua casa de pedra e reconstruida por ultimo.
- A casa da mina possui paredes, janelas, telhado decorado, entrada, baus e armaduras, sem duplicar suportes nomeados.
- O casarao recebe acabamento acima das portas, mais janelas, iluminacao, plantas, mobiliario e estacoes uteis.
- Foram adicionados baus com varinhas Magic World, ferramentas e itens raros/premium.
- Foram adicionadas exposicoes de Netherite e de Netherite nomeada `Magic World`.
- O conjunto personalizado real do NeoForge nao foi referenciado porque ainda nao possui itens registrados neste Forge 1.20.1.

### Validacao e processo

- Validacao final: `./gradlew.bat build --stacktrace` com `BUILD SUCCESSFUL`.
- O cliente Minecraft nao deve ser aberto pelo Codex.
- Testes visuais e funcionais dentro do cliente sao responsabilidade do usuario.

## Correcao da frente da casa e rua nivelada - 2026-06-06 11:06:00 -03:00

- Corrigido o reparo anterior da rua frontal.
- Causa: `normalizeImportedHouseFrontRoad` reconstruia a rua de `z=14` ate `z=78`, mas a casa importada ocupa ate `z=75`; isso podia sobrescrever blocos da frente da casa e partes do muro depois da restauracao do NBT.
- O reparo versionado foi elevado para a versao 4 para aplicar novamente no proximo login.
- O reparo agora restaura a casa importada pelo NBT e so depois cria a rua fora do limite da estrutura.
- A rua frontal agora comeca em `IMPORTED_HOUSE_MAX_Z + 1`, fora da casa, e usa blocos inteiros no nivel da casa.
- O acabamento lateral com meio bloco foi removido desta faixa, atendendo a opcao de rua nivelada com bloco inteiro.
- A frente/muro da casa deixa de ser substituida pela rotina da rua.
- Validacao do Codex deve continuar somente com Gradle; o cliente Minecraft nao deve ser aberto.

## Entrega reforcada do casarao, menu grafico e villagers - 2026-06-06 11:23:16 -03:00

- Reparo versionado elevado para `MagicWorldForgeEstateRepairVersion=5`.
- No proximo login, o reparo restaura novamente o NBT da casa importada e aplica explicitamente a fachada frontal.
- A frente do casarao recebe arco/blocos acima da porta, guarnicao de pedra, muro frontal, janelas novas, luzes e plantas externas.
- O interior do casarao recebe mais luzes, decoracao, baus premium e suportes de armadura em area central.
- O conjunto personalizado `Draconic Aether` foi registrado de verdade no Forge 1.20.1:
  - `draconic_aether_helmet`;
  - `draconic_aether_chestplate`;
  - `draconic_aether_leggings`;
  - `draconic_aether_boots`.
- Os baus e suportes passam a usar o set real `Draconic Aether`, alem do set Netherite.
- O menu grafico Embeddium reserva rodape esquerdo, adiciona o botao `Horizontes Distantes` dentro do frame do Embeddium e tambem renderiza o botao com icone Magic World.
- O background do menu grafico agora usa camada opaca para ocultar o jogo atras.
- Cores, paineis, busca, linhas e contornos do Embeddium foram reforcados no tema ciano Magic World.
- A rolagem circular entre paginas permanece ativa no `TabFrame`.
- Casas de trabalhadores agora contam os pes de cama e geram um villager trabalhador por cama.
- Villagers Magic World ficam persistentes, invulneraveis, com vida/velocidade/alcance aumentados, profissao nivel 5 e efeitos longos.
- Cada casa de trabalhadores recebe decoracao interna/externa extra, fogao, bau/barril, mesa, cadeiras, luzes premium, plantas e uma ave.
- Distritos recebem guardioes de ferro para impedir que inimigos ocupem os villagers.
- Validacao feita pelo Codex: `./gradlew.bat compileJava --stacktrace` e `./gradlew.bat build --stacktrace`, ambos com `BUILD SUCCESSFUL`.
- O cliente Minecraft continua sendo testado pelo usuario; o Codex nao deve executar `runClient`.

## Fix do crash ao abrir Graficos - 2026-06-06 11:32:30 -03:00

- Crash report analisado: `mouseClicked event handler` ao abrir Graficos.
- Causa real: `NoClassDefFoundError: org/spongepowered/asm/synthetic/args/Args$1` dentro de `SodiumOptionsGUI.init`.
- Origem no Magic World: uso de `@ModifyArgs` no mixin do Embeddium para reservar rodape esquerdo. Esse caminho gerava dependencia sintetica instavel no runtime Forge.
- Correcao aplicada:
  - removido `@ModifyArgs` do menu grafico;
  - removida a criacao reflexiva de `FlatButtonWidget` interno do frame;
  - mantido o botao proprio `MagicWorldDistantHorizonsButton` no rodape esquerdo, com icone Magic World;
  - o clique no botao agora e tratado antes do frame do Embeddium para evitar o frame consumir o clique.
- Auditoria do port NeoForge:
  - rolagem circular ja esta portada em `EmbeddiumTabFrameCircularScrollMagicWorldMixin`;
  - fundo opaco, cores ciano, contornos, busca, linhas, logo e icones laterais continuam portados;
  - o mixin NeoForge de Iris nao tem classe equivalente direta no Oculus Forge 1.20.1; o Forge usa `IrisSodiumOptions` e injecoes no `SodiumGameOptionPages`, enquanto o titulo Oculus/Iris ja e renomeado pelo `EmbeddiumTabHeaderMagicWorldMixin`.
- Validacao final desta correcao: `./gradlew.bat build --stacktrace` com `BUILD SUCCESSFUL`.
- O Codex nao abriu o cliente Minecraft; o cliente continua sendo testado pelo usuario.

## Correcao precisa menu grafico, casa e rua - 2026-06-06 11:56:55 -03:00

- Regra mantida: nao abrir `runClient`; o usuario testa o cliente. A validacao do Codex foi somente Gradle.
- `Horizontes Distantes` no menu grafico nao fica mais preso no rodape solto: o botao agora calcula a area real das abas do Embeddium e aparece logo abaixo da lista lateral, abaixo das entradas de shaders.
- O painel avulso do botao Distant Horizons deixou de esticar ate o fim da tela; agora desenha somente a moldura do proprio botao.
- Background do menu grafico ficou mais opaco para esconder melhor o jogo atras.
- Botao/texto de suporte/doacao do Embeddium/Sodium agora e ocultado, desabilitado, sem label e movido para fora da tela.
- Rolagem circular entre abas do Embeddium foi reforcada para usar tambem `getOffset()` da barra de rolagem quando o campo interno muda.
- Reparo da propriedade elevado para versao 7, forÃƒÂ§ando nova execucao no proximo login de saves ja criados.
- Frente da casa importada e protegida: estradas e caminhos agora ignoram o footprint da `imported_house.nbt`.
- A casa importada e restaurada pelo NBT depois das rotinas de terreno/rua, preservando frente, muro e estrutura original.
- Rua ao redor da casa foi elevada com bloco inteiro no nivel acima do tracado antigo, alinhando melhor com a linha da casa.
- Drops soltos de itens ao redor da casa sao removidos antes/depois do reparo, evitando pilhas de baus quebrados voando.
- Reposicao de baus gerados limpa o container antes de trocar o bloco, evitando novo drop de conteudo em reparos repetidos.
- Casas de trabalhadores receberam reforco visual: blocos acima das portas, novas janelas quando ha parede, postes externos, luzes e plantas.
- Build final executado: `./gradlew.bat build --stacktrace`.
- Resultado: `BUILD SUCCESSFUL`.

## Menu grafico: aba real, tema completo e rolagem circular - 2026-06-06 13:17:59 -03:00

- Corrigida a causa do Distant Horizons nao aparecer: a versao anterior desenhava um botao externo, nao uma aba do Embeddium.
- `Horizontes Distantes` agora e uma aba real criada em `createShaderPackButton`.
- A aba entra no mesmo grupo real do Oculus/Iris e aparece imediatamente abaixo de `Pacote de sombreadores...`.
- O grupo e escolhido dinamicamente: `oculus` quando Oculus esta carregado, com fallback para `iris`.
- O icone Magic World agora fica dentro da entrada `Horizontes Distantes`; o icone solto do rodape foi removido.
- Ao selecionar a entrada, a tela de configuracao do Distant Horizons e aberta e o menu Embeddium nao tenta construir uma pagina vazia.
- A rolagem na pagina direita agora troca de aba ao ultrapassar o topo/fim e usa ciclo infinito da ultima aba para a primeira e vice-versa.
- A troca circular executa tambem a acao de abas externas, como shaders e Distant Horizons.
- O fundo estatico Magic World e desenhado antes do frame em toda renderizacao e recebe overlay azul-escuro, ocultando o jogo atras.
- O tema do NeoForge foi adaptado para os componentes reais do Embeddium 1.20.1:
  - fundos globais azul-escuros;
  - hover, selecao, sliders, linhas e contornos ciano;
  - estado desabilitado personalizado;
  - cabecalhos `Magic World` e `Magic World Shaders` com icones Magic World;
  - suporte/doacao ocultos.
- Auditorados os cinco mixins de menu do projeto NeoForge. O Forge antigo nao possui o mesmo `SodiumConfigBuilder`; por isso o tema equivalente foi aplicado diretamente em `FlatButtonWidget.Style`, controles e frames do Embeddium.
- Regra de validacao mantida: o Codex nao executa `runClient`; somente Gradle. O usuario testa visualmente no cliente.
- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

## Entrega completa do menu grafico, casas e villagers - 2026-06-06 13:42:26 -03:00

- O print mais recente confirmou que `Horizontes Distantes` ja estava como aba real abaixo dos shaders.
- O icone solto no rodape foi identificado como o `logoDim` nativo do Embeddium e agora e ocultado.
- O background Magic World agora e desenhado depois do background nativo, impedindo que o mundo apareca atras do menu.
- Checkboxes, selecoes, foco, linhas e barra de rolagem receberam mixins dedicados com o ciano exato do NeoForge.
- A rolagem circular/infinita entre abas continua implementada no `TabFrame`.
- Portada a preparacao do `DistantHorizons.toml` existente no NeoForge:
  - remove o botao nativo do DH das telas vanilla;
  - fixa a API de renderizacao OpenGL.
- O reparo da propriedade foi elevado para a versao `8`.
- A fachada do casarao importado, que existia mas nao era chamada, agora e aplicada.
- O bau de varinhas do casarao fica completamente preenchido.
- As casas grandes da plantacao receberam telhado inclinado, frontoes, vigas, novas janelas, mais luzes, decoracao, moveis e estacoes uteis.
- O rancho grande recebeu bau cheio de varinhas, itens raros, armadura Netherite, armadura Draconic Aether, plantas e ave.
- Cada casa conta camas e gera trabalhadores correspondentes.
- Villagers da propriedade sao mantidos invulneraveis, nivel 5, fortalecidos e com raio minimo de 192 blocos.
- Monstros dentro da propriedade sao removidos periodicamente.

Validacao e processo:

- Nao executar `runClient`; o usuario testa o cliente.
- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

## Reparo breve do portal, casa da mina e currais - 2026-06-06

- O reparo versao `11` preenche apenas blocos ausentes/em ar na base e estrutura do portal inicial.
- A abertura central e todos os blocos existentes sao preservados.
- A casa fechada de pedra sobre a mina e reconstruida por ultimo, depois das rotinas que poderiam limpar sua area.
- A restauracao recoloca piso, paredes, telhado, janelas, porta, baus, decoracao e acesso a mina, alem de remover os itens soltos deixados pela destruicao anterior.
- As cercas dos seis currais sao fechadas somente no perimetro existente, sem tocar na plantacao, com uma porteira por curral.
- Cada curral possui agua, alimento, uma especie diferente, adultos e filhotes; cuidadores sao vinculados aos currais e a reproducao e mantida com limite.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Revisao do dropdown de seeds - 2026-06-06 15:42:01 -03:00

- Revisada a ultima alteracao do menu `Magic World` para reduzir risco de erro visual em telas menores.
- O dropdown de seeds agora abre para baixo quando ha espaco e abre automaticamente para cima quando a lista nao cabe abaixo do botao.
- O clique do dropdown continua sendo consumido antes dos outros botoes, evitando acionamento acidental de `Criar Mundo` ou `Voltar` por baixo da lista.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Correcao do popup de seeds e Entity Culling - 2026-06-06 16:01:54 -03:00

- O print mostrou a lista de seeds abrindo sobre textos, botoes e abas do painel `Magic World`.
- O dropdown agora abre como popup central opaco, com overlay escuro, titulo proprio e fechamento ao clicar fora.
- Em telas normais a lista mostra todas as seeds; em telas muito baixas limita linhas e permite rolagem.
- `MagicWorldEntityCulling` foi portado para Forge 1.20.1 com cache por entidade/camera e raycast de oclusao.
- `MagicWorldEntityCullingMixin` foi registrado em `magicworld.mixins.json`.
- A dependencia direta em `PeacefulDragon` foi removida porque essa entidade ainda nao existe nesta base Forge.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- O Codex valida somente com Gradle e nao abre o cliente.

## Correcao casa e santuario do fim da rua - 2026-06-06 16:10:00 -03:00

- Os prints mostraram que a casa do fim da rua nao ficou completa no local esperado e que o santuario nao apareceu no sentido da rua olhando para o morro.
- O save local `Novo mundo` registrava base real `MagicWorldForgeStarterEstateBaseX/Y/Z = 0/105/0` e jogador olhando para oeste (`yaw ~ -96`).
- O reparo foi elevado para `CURRENT_ESTATE_REPAIR_VERSION = 19`, forcando reaplicacao no proximo login do save existente.
- A casa `starter_house_1.nbt` continua no ponto definido anteriormente, mas agora a rotina forca carregamento dos chunks antes de limpar e posicionar a estrutura.
- A limpeza de volumes agora esvazia containers antes de remover blocos, evitando drops de baus antigos durante reparos repetidos.
- O santuario foi movido para o eixo oeste da estrada real, no sentido do print olhando para o morro: `roadEndMagicSanctuaryOrigin(base) = base.offset(-176,0,-8)`.
- A rotina cria/estende uma estrada ate a entrada leste do santuario antes de construir o shell.
- O santuario tambem forca carregamento de chunks da area antes da construcao.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- O Codex valida somente com Gradle e nao abre o cliente.

## Seeds no menu Magic World - 2026-06-06 15:30:59 -03:00

- A aba `Magic World` da tela de criacao de mundo agora tem campo `Seed manual` e dropdown de seeds predefinidas.
- O botao `Modo: Normal/Criativo` foi movido para baixo de `Dificuldade` e usa a mesma largura dos demais botoes da coluna direita.
- A prioridade aplicada e: seed predefinida selecionada, depois seed manual, depois seed aleatoria padrao do Minecraft.
- A primeira opcao do dropdown e `Selecione a seed`, com valor vazio, para nao forcar seed.
- A lista inicial tem 10 seeds nomeadas, incluindo as quatro informadas pelo usuario.
- O guia para adicionar novas seeds foi criado em `docs/SEEDS_MAGIC_WORLD.md`.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Santuario magico do fim da rua - 2026-06-06 15:05:02 -03:00

- O reparo foi elevado para a versao `18`, para aplicar no proximo login de saves ja criados.
- O santuÃƒÂ¡rio usa o volume dos prints no final da rua, aproximadamente `X 236..281`, `Y 74+`, `Z -56..-39`.
- A origem relativa usada e `base.offset(296,0,-86)`, com largura `45`, profundidade `17` e altura interna de `10`.
- O loading inicial ganhou etapa propria em `97%`, mensagem `Carregando santuario magico do fim da rua...`, antes de marcar `100%`.
- `repairExistingEstate` tambem chama a rotina, garantindo aplicacao em mundos ja existentes.
- O espaÃƒÂ§o recebe piso colorido com quartz, andesite, purpur, amethyst e sea lanterns, paredes decoradas, teto iluminado, redstone blocks, redstone lamps e glowstone.
- A parede leste e laterais recebem baus/barris preenchidos com o catalogo de itens registrados do jogo/mod.
- Ha bau de varinhas magicas, bau premium, ferramentas de todos os tiers principais, estacoes de trabalho, mesa central de reuniao, sino, luzes, banners, paineis decorativos, plantas, armaduras em stands, allays, parrots e rabbits.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Elevacao das casas dos animais - 2026-06-06 14:58:27 -03:00

- O reparo foi elevado para a versao `17`, forcando atualizacao no proximo login do save ja criado.
- O print indicou casas em frente ao ponto `56 74 -11` um bloco abaixo do nivel do chao.
- As casas dos animais e o centro premium foram elevados de `base.offset(106,-1,...)` para `base.offset(106,0,...)`.
- Isso sobe piso, paredes, portas, camas, baus, decoracao e referencias dos cuidadores em um bloco.
- `assignAnimalCaretakersToPens` e `buildWorkerSettlement` foram alinhados ao mesmo nivel novo.
- O Codex valida somente com Gradle e nao abre o cliente.

## Conselho profissional do castelo - 2026-06-06

- O reparo versao `12` adiciona quatro aldeoes ao redor da mesa central indicada nos prints.
- Profissoes distintas: bibliotecario, cartografo, clerigo e armeiro, todos nivel 5 e com estacoes de trabalho.
- Pontos relativos ao centro do castelo: `(+2,+5)`, `(+2,-8)`, `(-8,-1)` e `(+8,0)`.
- Profissoes dos demais moradores nomeados do castelo sao preservadas/corrigidas pela manutencao.
- Todos os aldeoes gerenciados na propriedade e no castelo possuem raio minimo de caminhada/trabalho de `384` blocos.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Casa grande premium, Praca Verde e cerejeiras - 2026-06-06 14:33:55 -03:00

- O reparo foi elevado para a versao `14`, aplicando no proximo login dos mundos ja criados.
- A casa grande indicada pelos prints foi tratada no volume relativo `base.offset(106,-1,-72)` ate `base.offset(124,-1,-58)`, que corresponde ao retangulo absoluto observado `X 46..64` e `Z -42..-28`.
- A primeira casa pequena dos cuidadores de animais agora vira um centro premium de trabalho, com paredes de pedra decoradas, telhado inclinado, janelas adicionais, blocos acima das portas, plantas, flores e ave.
- O lado leste possui porta central de saida e parede de armazenamento; os baus/barris sao preenchidos automaticamente com os itens registrados do jogo/mod.
- Foram adicionados bau cheio de varinhas magicas, bau premium de itens raros, set Netherite e set Draconic Aether.
- O interior recebeu mesa grande, cadeiras, camas, bancada, fornalha, alto-forno, defumador, forja, bigorna, grindstone, stonecutter, mesa de encantamentos, estante, lectern, cartography table, fletching table, loom, caldeirao e brewing stand.
- Foram adicionados profissionais fixos da Casa Grande: guardiao, armoreiro, ferreiro, ferramenteiro, bibliotecario, clerigo e pedreiro.
- Guardioes aldeoes por setor sao recriados se sumirem: Casa Grande, Mina, Currais, Plantacao e Praca Verde.
- Todos os aldeoes gerenciados seguem invulneraveis, nivel 5, com raio minimo global de `384` blocos.
- A defesa remove hostis perto dos guardioes e mantem a limpeza global de monstros na area da propriedade/castelo, evitando dano em estruturas.
- A iluminacao da casa foi reforcada com sea lanterns internas, postes externos e luzes no entorno para reduzir spawn/aproximacao de monstros.
- A Praca Verde recebeu suporte solido extra abaixo do sino, pedra ao redor, mais flores, azaleias, luzes e aves nomeadas.
- As arvores naturais em volta da casa importada sao convertidas para cerejeiras, fora do footprint da casa e evitando a faixa principal de rua/muro.
- Petalas rosas sao aplicadas apenas em chao de grama livre perto dessas arvores.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Casa NBT no fim da rua - 2026-06-06 14:41:27 -03:00

- O reparo foi elevado para a versao `15`.
- A estrutura `starter_house_1.nbt`, ja presente em `src/main/resources/data/magicworld/structures`, agora e posicionada no fim da rua dentro das quatro coordenadas enviadas.
- A casa entra como etapa propria do loading inicial em `94%`, com a mensagem `Carregando casa do fim da rua...`.
- O reparo existente chama a mesma rotina apenas como fallback para saves antigos.
- O volume usado fica em torno de `X 157..176`, `Y 70..84`, `Z -118..-89` no mundo atual observado nos prints.
- A origem relativa usada e `base.offset(217,-4,-148)`.
- A porta principal do NBT tem `facing=south`; por isso a casa e colocada sem rotacao, deixando a frente/area frontal virada para a rua.
- Antes da colocacao, a area recebe platÃƒÂ´ de suporte e limpeza local controlada.
- Depois da colocacao, a frente recebe caminho de smooth stone/polished andesite ate a rua, postes de luz, azaleias e animais decorativos nomeados.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Correcao visual do menu principal - 2026-06-06 14:48:13 -03:00

- Antes do commit online, o usuario reportou menu principal com textura ausente preto/magenta.
- O log do cliente apontou falta de `magicworld:textures/gui/title/title_background_static.png` e `magicworld:textures/gui/title/logo_full.png`.
- `MagicWorldStaticBackground` foi ajustado para usar os assets existentes `textures/gui/title_background_static.png` e `textures/gui/title_logo.png`.
- A logo do menu agora usa as dimensoes `512x171`, correspondentes ao arquivo existente.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Ajuste da casa grande nas coordenadas dos prints - 2026-06-06 14:52:19 -03:00

- O reparo foi elevado para a versao `16`, forÃƒÂ§ando atualizacao no proximo login do save ja criado.
- As coordenadas do print atual indicam o centro `55 74 -35` e alvo `55 73 -29`.
- Esse ponto corresponde ao volume premium `X 46..64`, `Y 73+`, `Z -42..-28` quando a base da propriedade esta em `-60 74 30`.
- A rotina da casa grande premium continua garantindo armazenamento, baus com itens premium, bau de varinhas, estacoes de trabalho, mesa, camas, armaduras, luzes, decoracao, ave e aldeoes profissionais.
- A porta leste central foi mantida.
- O lado oeste, voltado para os currais/fazendas, agora tem duas portas com caminhos curtos saindo para fora.
- A conversao de cerejeiras no entorno da casa importada foi ampliada para uma faixa de `64` blocos para fora do footprint protegido.
- A zona de rua/muro nao e mais pulada nessa conversao, mas apenas troncos e folhas naturais sao trocados por cerejeira.
- Validado com `compileJava`, `build` e `git diff --check`; todos passaram.
- O Codex valida somente com Gradle e nao abre o cliente.

## Cerejeiras restritas a casa principal e castelo - 2026-06-06 16:22:20 -03:00

- O usuario confirmou que sempre gera mapa novo; esta alteracao nao aumentou CURRENT_ESTATE_REPAIR_VERSION e nao adiciona peso extra para forcar save atual.
- A conversao antiga usava faixa ampla ao redor da casa importada; foi substituida por zonas fixas e curtas das estruturas do usuario.
- Casa principal: converte somente o anel de ate 24 blocos ao redor do footprint da casa, cortado pelos limites da cerca da propriedade e excluindo o volume protegido da propria casa.
- Castelo: converte somente o anel de ate 24 blocos ao redor do volume do castelo, excluindo o volume protegido do castelo.
- Nao planta arvores novas: apenas troca troncos naturais com folhas proximas e folhas naturais ja existentes por cerejeira.
- Troncos/folhas de jungle e mangrove tambem entram na conversao; madeira decorativa sem copa nao entra.
- Petalas rosas agora sao colocadas apenas na mesma coluna permitida da arvore convertida, sem procurar blocos fora da zona autorizada.
- A casa aplica a regra em decorateImportedHouseAddons; o castelo aplica logo depois de uildImportedCastle e decorateCastleStarterLife durante a geracao do mapa novo.
- Validacao parcial: ./gradlew.bat compileJava --stacktrace passou; uild completo e git diff --check ainda serao rodados apos esta anotacao.
- O Codex valida somente com Gradle e nao abre o cliente.

## Correcao final do popup de seeds - 2026-06-06 16:29:12 -03:00

- O print mostrou textos, botoes e tooltip sobrepondo a lista de selecao de seed.
- MagicSeedDropdown nao desenha mais a lista aberta no render normal do widget; o widget normal agora mostra apenas o botao compacto.
- A lista aberta passou a ser renderizada em ScreenEvent.Render.Post, ficando acima de botoes e tooltips do Minecraft/Forge.
- O fundo da tela e a caixa da lista ficaram bem mais opacos (xF6000000 na mascara e xFF050916 no painel), removendo a leitura dupla do menu atras.
- Cliques continuam sendo consumidos enquanto a lista esta aberta, inclusive clique fora para fechar.
- Scroll agora e interceptado em ScreenEvent.MouseScrolled.Pre, impedindo outros widgets de rolarem por baixo da lista.
- Ao fechar a aba Magic World, o dropdown aberto e recolhido automaticamente.
- Validacao parcial: ./gradlew.bat compileJava --stacktrace passou; uild completo e git diff --check ainda serao rodados apos esta anotacao.
- O Codex valida somente com Gradle e nao abre o cliente.

## Registro 2026-06-06 - ajuste rapido antes do commit
- Menu de criacao: padrao agora abre em modo Criativo e dificuldade Facil, mantendo botoes para o usuario trocar.
- Loading inicial: logo reduzida, mensagem separada abaixo da logo e painel azul mais transparente.
- Seed dropdown: lista renderizada como overlay no final do render para nao ficar sob widgets vanilla.
- MagicWorldEntityCulling: portado do NeoForge para Forge via mixin client-side em EntityRenderer.shouldRender; desliga com propriedade magicworld.entity_culling=false.
- Geracao nova: casa do fim da rua reposicionada para o fim da rua principal e sem plataforma extra de grama fora do footprint.
- Geracao nova: santuario rebaixado 4 blocos, com entrada unica voltada para a casa, fora do volume do castelo e sem invadir a casa principal.
- Geracao nova: entradas da casa grande perto dos currais limpas por dentro e por fora, mantendo iluminacao embutida.
- Geracao nova: borda viva e arvores naturais dentro do terreno da casa principal passam para cerejeira; conversao fica limitada aos limites do terreno e ao perimetro do castelo.
- Validacao: compileJava foi iniciado mas interrompido pelo usuario para priorizar commit/versionamento; precisa rodar depois.

## Registro 2026-06-06 - ajuste casa/santuario/cerejeiras
- Casa do fim da rua: template rotacionado 180 graus para a porta ficar de frente para a rua.
- Casa do fim da rua: origem ajustada para base + (-5, -4, -90), deslocando lateralmente para centralizar e avancando a construcao para colar 2 blocos no eixo da rua.
- Casa do fim da rua: decoracao da porta agora usa a coordenada da porta rotacionada, mantendo acesso no mesmo nivel da rua.
- Santuario: reposicionado para a boca ficar cerca de 15 blocos apos a rua, em direcao ao morro/castelo, com entrada voltada para a casa.
- Cerejeiras: conversao da casa principal nao exclui mais o footprint grande da estrutura, para converter tambem arvores verdes dentro do terreno do jogador.
- Performance: varredura vertical da conversao de arvores foi reduzida para diminuir custo no carregamento inicial.
- Validacao: ./gradlew.bat compileJava --stacktrace passou.

## Registro 2026-06-06 - correcao sobreposicao menu Magic World
- Menu de criacao Magic World: quando aberto, agora aplica uma camada escura de tela inteira antes do painel para esconder textos/widgets vanilla por baixo.
- Painel central do Magic World ficou praticamente opaco para impedir leitura cruzada com o menu original.
- Texto informativo interno foi reduzido para duas linhas curtas, removendo listas longas que ficavam sobre os botoes em GUI scale alta.
- Validacao: ./gradlew.bat compileJava --stacktrace passou.

## Registro 2026-06-06 - casa do fim da rua altura e acesso
- Casa do fim da rua: origem ajustada para base + (-5, +1, -74), subindo 5 blocos e avancando 16 blocos em direcao a rua.
- Casa do fim da rua: limpeza do volume aumentada para margem 3 para remover terreno/pedra que encoste na estrutura.
- Casa do fim da rua: caminho generico baseado em surface scan foi removido desta casa; agora a entrada usa acesso dedicado no nivel da porta para nao gerar pedras por cima da construcao.
- Validacao: ./gradlew.bat compileJava --stacktrace passou.
