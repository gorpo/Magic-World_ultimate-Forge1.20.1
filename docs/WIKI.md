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

- Handoff antigo foi apagado e recriado para este novo port.
- Wiki local foi recriada.
- Documentacao NeoForge foi copiada para `docs/neoforge-reference/` como referencia.
- Git local foi inicializado no branch `Inicio-Port-Neoforge`.
- Remoto configurado para `https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1.git`.
- Ainda falta portar codigo/telas desta etapa.
- Ainda falta compilar, commitar e enviar o branch.

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
