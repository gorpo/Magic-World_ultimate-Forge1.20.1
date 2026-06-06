# Codex Handoff - Inicio Port Neoforge

Atualizado em 2026-06-05.

## Pedido atual

Refazer o port para Forge 1.20.1 usando este repositorio como base local e o projeto NeoForge 26 ao lado como referencia.

- Base Forge: `C:\Users\guilh\Desktop\MinecraftProjects\Magic-World_ultimate-Forge1.20.1`
- Origem NeoForge: `C:\Users\guilh\Desktop\MinecraftProjects\neoforge-26.1.2-mdk`
- Remoto: `https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1.git`
- Branch de trabalho: `Inicio-Port-Neoforge`

Observacao: `Inicio Port Neoforge` com espacos nao e nome valido de branch Git. Usar sempre `Inicio-Port-Neoforge`.

## Regra de retomada obrigatoria

Se a sessao cair ou outro agente continuar:

1. Ler este arquivo primeiro.
2. Rodar `git status --short --branch`.
3. Confirmar branch `Inicio-Port-Neoforge`.
4. Ler `docs/WIKI.md`.
5. Usar `docs/neoforge-reference/` apenas como referencia do NeoForge.
6. Nao portar nada fora do escopo atual sem pedido novo do usuario.
7. Atualizar este handoff ao final de cada bloco de trabalho.

## Regra atual para mods externos

- Usar mods soltos em `run/mods` durante esta fase.
- Futuramente teremos um pacote all-in-one do MagicWorld, mas isso fica para depois de estabilizar o port.
- Nao tentar embutir Oculus, Embeddium, mods de resource pack ou performance dentro do jar do MagicWorld agora.
- Lista registrada em `docs/MODS_RECOMMENDED.md`.
- Shaderpack em `run/shaderpacks` nao funciona sozinho em Forge puro; precisa de Oculus/Iris compat. Para Forge 1.20.1, usar Oculus + Embeddium.
- Baixar sempre arquivos `Forge 1.20.1`; nao usar Fabric nem NeoForge nesta base.

## Escopo autorizado nesta etapa

Portar somente:

- inicio do jogo;
- comportamento inicial dos menus ao abrir;
- menus iniciais da criacao de mundo;
- planos de fundo estaticos das telas;
- tela de loading inicial.

Nao portar nesta etapa:

- gameplay completo do NeoForge;
- entidades custom novas;
- resource/shader installers;
- estruturas/mundo inicial alem do minimo ja existente;
- sistemas extras que aparecerem na wiki NeoForge.

## Feito nesta sessao

- Handoff antigo apagado e substituido por este arquivo novo.
- Wiki local recriada em `docs/WIKI.md`.
- Confirmado que nao havia pasta wiki local anterior.
- Confirmado projeto NeoForge de origem em `neoforge-26.1.2-mdk`.
- Git local inicializado neste projeto.
- Remoto `origin` configurado para `https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1.git`.
- Branch local criado: `Inicio-Port-Neoforge`.
- Documentacao NeoForge copiada como referencia para `docs/neoforge-reference/`:
  - `MAGIC_WORLD_WIKI.md`
  - `README_NEOFORGE.md`
  - `CODEX_HANDOFF_NEOFORGE.md`
  - `building_mod_candidates.md`
  - `screenshots_to_update.md`
  - `porting-forge-1.20.1/README.md`
- `.gitignore` ajustado para nao versionar pastas locais/pesadas como `tmp`, `pacote_distribuivel`, `installer`, `screenshots` e `run`.

## Observacoes importantes

- `tmp` tem conteudo local grande e pode ajudar no port, mas nao deve ir para GitHub.
- `pacote_distribuivel` tem cerca de 1.6 GB e nao deve ir para GitHub.
- A wiki NeoForge e referencia de destino, nao estado atual do Forge.
- A base Forge atual ainda usa `mod_id=magicworld` e pacote `com.magicworld`.

## Proximo passo imediato

Portar somente as telas/menus iniciais e backgrounds:

1. Copiar assets estaticos de GUI do NeoForge para `src/main/resources/assets/magicworld/textures/gui/`.
2. Criar/adaptar classes Forge 1.20.1 para:
   - tela de titulo customizada;
   - botoes/tema visual;
   - painel Magic World na criacao de mundo;
   - loading inicial com fechamento automatico apos concluir.
3. Rodar `./gradlew.bat build`.
4. Atualizar este handoff com resultado.
5. Commitar e enviar branch `Inicio-Port-Neoforge` ao GitHub.

## Bloco concluido em 2026-06-05 - background e logo iniciais

- Copiado `screenshots/title_background_static_2560x1440_final.png` para `src/main/resources/assets/magicworld/textures/gui/title/title_background_static.png`.
- Copiado `screenshots/logo_full.png` para `src/main/resources/assets/magicworld/textures/gui/title/logo_full.png`.
- Criado `MagicWorldStaticBackground` para desenhar o fundo estatico em modo cover 16:9.
- Criado `MagicWorldTitleScreen` para substituir a tela vanilla `TitleScreen`:
  - usa o background estatico;
  - remove a logo Minecraft vanilla;
  - desenha a logo full Magic World;
  - mantem botoes principais de entrada: um jogador, multijogador, mods, opcoes e sair.
- Atualizado `ClientEvents` para:
  - trocar `TitleScreen` por `MagicWorldTitleScreen` ao abrir/tickar;
  - desenhar o background estatico nas telas iniciais via `ScreenEvent.BackgroundRendered`.
- Atualizado `InitialLoadNoticeScreen` para usar o mesmo background estatico no loading inicial e fechar 5s apos completar.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

## Proximo passo

Testar visualmente no Minecraft:

1. Tela inicial deve mostrar `title_background_static.png` e `logo_full.png`.
2. Telas iniciais como selecao/criacao de mundo devem usar o mesmo background sem cobrir botoes.
3. Loading inicial do Magic World deve usar o mesmo background durante criacao do mapa.
4. Se o loading vanilla do Minecraft ainda aparecer com outro visual antes da tela do mod, avaliar mixin/overlay em etapa separada.

## Ajuste em 2026-06-05 - logo menor e botoes NeoForge

- Corrigida a tela inicial depois do teste visual do usuario.
- Logo `logo_full.png` reduzida para largura maxima de 300 px, mais proxima da proporcao visual da logo vanilla do Minecraft.
- Corrigido o desenho da logo para usar o overload correto de `GuiGraphics.blit`, evitando escala estourada.
- Criado `MagicWorldMenuTheme` adaptado do NeoForge para Forge 1.20.1.
- Criado `MagicWorldMenuButton` adaptado do NeoForge para Forge 1.20.1.
- `MagicWorldTitleScreen` deixou de usar `Button.builder` vanilla nos botoes principais.
- Botoes principais agora usam layout central estreito e visual azul/dourado do NeoForge.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

## Proximo teste visual

- Confirmar se a logo ficou pequena o suficiente.
- Confirmar se os botoes customizados aparecem alinhados e clicaveis.
- Se aprovado, portar os botoes rapidos/menus adicionais do NeoForge na proxima etapa.

## Bloco concluido em 2026-06-05 - rename para magicworld e Java

- Renomeado o pacote Java de `com.example.examplemod` para `com.magicworld`.
- Classe principal renomeada de `ExampleMod` para `MagicWorld`.
- `mod_id` alterado de `examplemod` para `magicworld`.
- `mod_group_id` alterado para `com.magicworld`.
- Namespace de recursos movido de `assets/examplemod` para `assets/magicworld`.
- Namespace de dados movido de `data/examplemod` para `data/magicworld`.
- Referencias JSON, lang, recipes, modelos, README e codigo atualizadas para `magicworld`.
- Confirmado que nao restam ocorrencias de `examplemod` nos arquivos fonte/recursos/docs checados.
- IntelliJ pode usar Java 21 como SDK da IDE, mas o Gradle deve continuar com `java.toolchain.languageVersion = 17` para compatibilidade com Minecraft Forge 1.20.1 e jogadores usando runtime Java 17.
- Pastas auxiliares `assets/distanthorizons`, `assets/iris`, `assets/sodium` e `assets/minecraft` foram tratadas como referencia local: ignoradas no Git e excluidas do empacotamento por enquanto.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

## Proximo passo

- Testar o jogo no IntelliJ usando a configuracao Java 21 da IDE, confirmando que o Gradle ainda compila/roda o mod com toolchain Java 17.
- Se o IntelliJ tentar compilar fora do Gradle e reclamar de language level, ajustar apenas a configuracao do modulo/Gradle JVM, nao o target do mod.

## Ajuste em 2026-06-05 - IntelliJ voltou para Java 17

- Usuario voltou as configuracoes do IntelliJ para Java 17.
- Esta e a configuracao recomendada para Forge 1.20.1.
- Gradle confirmado com JVM 17.0.19 e `java.toolchain.languageVersion = 17`.

## Fix em 2026-06-05 - crash no runClient por BOM em mods.toml

- O erro `ParsingException: Invalid bare key: ?#` vinha de BOM UTF-8 no inicio de `src/main/resources/META-INF/mods.toml`.
- Removido BOM dos arquivos textuais de recursos/configs/docs afetados.
- `mods.toml` agora inicia com `#` real, sem caractere oculto.
- `./gradlew.bat runClient --stacktrace` deixou de falhar com non-zero exit; o cliente carregou ate logs de ResourceManager, receitas e advancements.
- Nao houve crash report novo apos a correcao.

## Ajuste em 2026-06-05 - memoria, versao e aba Magic World na criacao de mundo

Feito:

- `gradle.properties` alterado para `org.gradle.jvmargs=-Xmx8G`.
- `build.gradle` passou a aplicar `jvmArgs '-Xms4G', '-Xmx8G'` nas run configs do ForgeGradle.
- `mod_version` alterado para `1.0.0.1`.
- Tela inicial agora exibe `Magic World 1.0.0.1` no canto inferior esquerdo.
- Restaurados botoes pequenos de idioma (`L`) e acessibilidade (`A`) na tela inicial.
- `MagicWorldWorldOptions` expandido com opcoes vindas do NeoForge: portal, castelo, fazendas, aura, comandos, perfil de PC, modo e dificuldade inicial.
- Criado `MagicWorldGraphicsProfile` leve para sustentar o menu de perfil de PC nesta etapa.
- `ClientEvents` recebeu painel Magic World na tela `CreateWorldScreen`:
  - botao `Magic World` na criacao de mundo;
  - painel com Portal, Castelo, Fazendas, Aura, PC, Dificuldade, Modo, Criar Mundo e Voltar;
  - comandos/cheats sao forçados quando opcoes Magic World exigem comandos;
  - botao Criar Mundo chama o fluxo vanilla por reflexao, depois de sincronizar modo/dificuldade/comandos.
- Background estatico foi ampliado para telas de selecao/criacao de mundo, opcoes, multiplayer, packs, mods e `LevelLoadingScreen`.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL em Java 17.

Plano de downgrade controlado a partir daqui:

1. Manter o escopo atual em telas antes do mapa ate o usuario aprovar visualmente.
2. Se o background do loading vanilla ainda nao cobrir tudo, portar uma solucao especifica de overlay/mixin compativel com Forge 1.20.1.
3. Depois portar os sistemas ligados aos botoes da aba Magic World: portal/casa inicial, castelos, fazendas, aura e perfis graficos reais.
4. So depois portar menus centrais, compat de Sodium/Iris/Distant Horizons e mixins, em blocos pequenos e sempre compilando.
5. Nao copiar `com.magicworld` inteiro do NeoForge sem adaptacao; cada classe precisa passar por downgrade NeoForge -> Forge 1.20.1.
## Ajuste em 2026-06-05 - title screen alinhado ao NeoForge aprovado

Feito:

- Comparado diretamente com `MagicWorldTitleScreen` e `MagicWorldIconButton` do projeto NeoForge.
- Portado `MagicWorldIconButton` para Forge 1.20.1 usando `GuiGraphics`.
- Tela inicial voltou ao layout aprovado:
  - 4 botoes principais: JOGAR, OPCOES, MODS, SAIR;
  - 5 botoes rapidos customizados abaixo: Multiplayer, Idioma, Controles, Pacotes e Acessibilidade;
  - removidos botoes vanilla pequenos `L/A` que estavam desalinhados e fora do tema;
  - logo usa largura limitada ao menu (`MENU_WIDTH`) mantendo proporcao;
  - adicionados ornamentos/borda dourada/azul como no NeoForge.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.
## Fix em 2026-06-05 - background em menus de criacao de mundo

Problema:

- Algumas telas iniciais aplicavam o background Magic World, mas submenus da criacao de mundo ainda mostravam o fundo vanilla marrom/cinza.
- Motivo: essas telas chamam `renderBackground`/`renderDirtBackground` e/ou usam a textura vanilla `assets/minecraft/textures/gui/options_background.png`, sobrescrevendo o desenho feito por evento.

Feito:

- Criado `MagicWorldScreenBackgrounds` para centralizar quais telas devem usar o fundo estatico.
- Criado mixin client-side `MagicWorldScreenBackgroundMixin` interceptando `Screen.renderBackground` e `Screen.renderDirtBackground`.
- Registrado `magicworld.mixins.json` no jar e no `mods.toml`.
- Adicionado override especifico `assets/minecraft/textures/gui/options_background.png` copiando o background estatico do Magic World.
- Ajustado `.gitignore` e `build.gradle` para incluir apenas esse override vanilla e continuar excluindo os panoramas auxiliares de `assets/minecraft/textures/gui/title/**`.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.
- Verificado no jar: `assets/minecraft/textures/gui/options_background.png`, `magicworld.mixins.json` e `MagicWorldScreenBackgroundMixin.class` estao presentes.

Proximo teste visual:

- Reabrir criacao de mundo e entrar nos submenus/opcoes avancadas.
- Confirmar que nao aparece mais o fundo vanilla marrom/cinza.
- Se alguma tela ainda escapar, mapear o nome da tela no log/screenshot e adicionar na regra de `MagicWorldScreenBackgrounds`.
## Fix em 2026-06-05 - logo ausente na title screen

Problema:

- A tela inicial ficou sem a logo Magic World.
- O codigo Forge ainda apontava para `textures/gui/title/logo_full.png`, enquanto o layout aprovado do NeoForge usa `textures/gui/title_logo.png` com proporcao 512x171.

Feito:

- `MagicWorldStaticBackground.FULL_LOGO` agora aponta para `textures/gui/title_logo.png`.
- Dimensoes da logo ajustadas para 512x171, iguais ao asset aprovado do NeoForge.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.
- Verificado no jar: `assets/magicworld/textures/gui/title_logo.png` esta presente.
## Fix em 2026-06-05 - remover mosaico e forcar mixin em dev

Problema:

- O override `assets/minecraft/textures/gui/options_background.png` fez o Minecraft repetir o background como mosaico, porque essa textura vanilla e usada em tile, nao em cover/fullscreen.
- Nos menus de criacao de mundo, o background ainda falhava porque o mixin nao estava explicitamente sendo passado ao `runClient` em ambiente dev.
- A logo tambem estava invisivel por uso do overload errado de `GuiGraphics.blit`, que recortava a textura em vez de escalar a imagem inteira.

Feito:

- Removido `assets/minecraft/textures/gui/options_background.png` do projeto/jar.
- `.gitignore` voltou a ignorar `assets/minecraft/**` inteiro como referencia local.
- `build.gradle` voltou a excluir `assets/minecraft/**` do jar.
- `build.gradle` agora passa `--mixin.config magicworld.mixins.json` nas run configs, alem do registro no manifest/mods.toml.
- `MagicWorldTitleScreen.drawLogo` usa o overload correto de `GuiGraphics.blit` para escalar a logo inteira 512x171.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.
- Validado com `./gradlew.bat runClient --stacktrace`: cliente carregou ate timeout, sem crash; log mostra `--mixin.config magicworld.mixins.json` e `Compatibility level set to JAVA_17`.

Proximo teste visual:

- Tela inicial deve exibir logo novamente.
- O background nao deve mais aparecer em quadradinhos/mosaico.
- Menus/submenus de criacao de mundo devem depender do mixin carregado para cobrir `renderBackground`/`renderDirtBackground`.
## Ajuste em 2026-06-05 - abas Mundo/Mais e fundo restante

Problema:

- O botao `Magic World` aparecia tambem nas abas `Mundo` e `Mais` da criacao de mundo, onde nao deve aparecer e nao abre fluxo util.
- Uma area/subtela da criacao ainda ficava sem o background Magic World porque `CreateWorldScreen` sobrescreve `renderDirtBackground`, escapando do mixin generico em `Screen`.

Feito:

- `ClientEvents` agora inspeciona a aba ativa do `CreateWorldScreen` via `tabManager.getCurrentTab().getTabTitle()`.
- O botao `Magic World` fica visivel/ativo apenas quando a aba ativa nao e `Mundo/World` nem `Mais/More`.
- Se o painel Magic World estiver aberto e o usuario trocar para outra aba, ele e fechado e o botao fica oculto.
- Criado `MagicWorldCreateWorldScreenBackgroundMixin` para interceptar especificamente `CreateWorldScreen.renderDirtBackground`.
- `magicworld.mixins.json` atualizado para carregar o novo mixin.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.
- Validado com `./gradlew.bat runClient --stacktrace`: cliente carregou ate timeout, sem erro de mixin/crash.

## Fix em 2026-06-05 - tela preparando mundo e abas com fundo vanilla

Problema:

- A tela rapida `Preparando a criacao do mundo...` ainda mostrava o dirt vanilla.
- A barra superior/abas `Jogo`, `Mundo` e `Mais` na criacao de mundo ainda usavam fundo/textura vanilla.
- A tela `Selecionar mundo` tambem precisava remover o fundo proprio da lista para deixar o background Magic World aparecer.

Feito:

- `MagicWorldScreenBackgrounds` agora inclui `GenericDirtMessageScreen` e `ProgressScreen`, cobrindo telas transitorias de loading/mensagem.
- Criado `MagicWorldSelectWorldScreenBackgroundMixin`:
  - desativa `WorldSelectionList.setRenderBackground(false)`;
  - desativa `WorldSelectionList.setRenderTopAndBottom(false)`;
  - desenha o background Magic World no inicio da renderizacao da selecao de mundos.
- `MagicWorldCreateWorldScreenBackgroundMixin` agora redesenha o background antes dos widgets da `CreateWorldScreen`, para cobrir a faixa que ficava por baixo das abas.
- Criado `MagicWorldTabNavigationBarMixin` para remover o preenchimento preto e o separador vanilla da barra de abas.
- Criado `MagicWorldTabButtonMixin` para substituir a textura vanilla `textures/gui/tab_button.png` por um recorte do background estatico Magic World, mantendo moldura, texto e sublinhado da aba selecionada.
- `magicworld.mixins.json` atualizado com os novos mixins.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Pendente para teste visual:

- Reabrir o client para confirmar que `Preparando a criacao do mundo...` usa o background Magic World.
- Confirmar se as abas da criacao de mundo agora aparecem sem dirt vanilla.
- Se alguma aba ainda precisar do visual exato NeoForge, comparar o widget de abas do projeto NeoForge antes de novos ajustes.

## Import em 2026-06-05 - terreno inicial, fazendas, portais funcionais e aura

Pedido:

- Comecar o import importante do terreno ja criado no NeoForge.
- Trazer casa/castelo importados, fazendas, portais funcionais e aura ligados ao menu Magic World da criacao de mundo.

Feito neste primeiro bloco funcional Forge 1.20.1:

- `StarterPortalEvents` foi refeito como controlador servidor do mundo inicial:
  - usa `data/magicworld/structures/imported_house.nbt`;
  - usa `data/magicworld/structures/imported_castle.nbt`;
  - guarda a base da propriedade no `PersistentData` do jogador;
  - cria a sequencia por etapas com progresso no loading inicial;
  - respeita `Portal`, `Castelo`, `Fazendas`, `Modo` e `Aura` definidos no menu Magic World.
- Casa importada:
  - limpa/achata volume ao redor;
  - posiciona a estrutura NBT;
  - cria bau seguro com ferramentas, comida, materiais, sementes e varinha.
- Fazendas:
  - adicionadas lavouras maduras de trigo, cenoura, batata e beterraba;
  - adicionados currais e jardim de alimento animal;
  - adicionados animais vanilla iniciais;
  - adicionadas casas simples de trabalhadores e villagers nomeados.
- Portal inicial:
  - construcao visual magic/premium;
  - bau de equipamentos;
  - alternancia premium por entrada/clique sem ficar alternando repetidamente enquanto o jogador fica parado nele.
- Portais funcionais:
  - praca compacta com portal do Nether, portal do End e gateway;
  - teleporte custom para Nether/End;
  - plataformas/portais de retorno criados nas dimensoes destino;
  - retorno para a praca da propriedade no Overworld;
  - cooldown persistente para evitar loop de teleporte.
- Castelo:
  - usa o NBT `imported_castle.nbt` quando `Castelo: ON`;
  - adiciona bau de equipamentos, golem/villagers e marcador visual de dragao via armor stand leve.
- Aura:
  - criado `AuraEvents` Forge;
  - registrado no `MagicWorld`;
  - se `Aura: ON`, o jogador recebe aura ao entrar;
  - aura remove fogo, afogamento/congelamento/fome, aplica efeitos invisiveis, cancela dano ambiental, cancela queda, permite quebrar bloco com clique esquerdo e preserva retorno pos-morte.
- Validado com `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Limites intencionais deste bloco:

- Nao foi copiado o `StarterPortalEvents` NeoForge bruto de 6 mil linhas.
- Nao entraram ainda entidades custom pesadas do NeoForge, armaduras draconicas custom, compat de Iris/Sodium/Distant Horizons ou menus centrais.
- O dragao do castelo esta como marcador leve por `ArmorStand` para evitar risco de bossfight/crash antes do teste do terreno.

Proximo teste manual:

1. Criar mundo novo com `Portal`, `Castelo`, `Fazendas` e `Aura` ligados.
2. Confirmar que casa e castelo NBT aparecem no terreno.
3. Confirmar que fazendas/animais/trabalhadores aparecem perto da propriedade.
4. Entrar nos portais Nether/End/Gateway e testar retorno.
5. Testar aura: fogo, queda, fome/agua e quebra rapida de bloco.

Proximos blocos se aprovado:

1. Portar acabamentos finos do NeoForge que ainda ficaram fora: aldeoes trabalhadores com IA de cuidado real, patrulhas do castelo, ambience maior e reparos de decoracao flutuante.
2. Portar entidades/itens custom necessarios, um pacote por vez.
3. Substituir o marcador leve do dragao pelo sistema correto apenas quando a entidade custom estiver portada e estavel.

## Fix em 2026-06-05 - remover quadrado central do loading de chunks

Problema:

- A tela de loading de mundo ainda mostrava o quadrado central vanilla de progresso de chunks sobre o background Magic World.

Feito:

- Criado `MagicWorldLevelLoadingScreenMixin`.
- O mixin redireciona a chamada `LevelLoadingScreen.renderChunks(...)` para no-op.
- Mantem a tela `LevelLoadingScreen`, o background Magic World e a porcentagem/texto.
- Remove somente o mapa quadrado central de chunks.
- `magicworld.mixins.json` atualizado com o novo mixin.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Teste manual:

- Reiniciar o client e criar/carregar mundo.
- A tela deve mostrar o background Magic World e porcentagem, sem o quadrado central.

## Fix em 2026-06-05 - castelo apagava casa, portal e fazendas

Problema confirmado pelo teste manual:

- Em mapa novo apareceu apenas o castelo.
- Casa importada, fazendas, portais e area plana da propriedade nao apareciam.
- O jogador tambem nao nascia/ficava na casa.

Causa:

- O calculo do castelo no Forge tratava `CASTLE_X_OFFSET`/`CASTLE_Z_OFFSET` como centro.
- No NeoForge esse ponto e a ancora lateral/oeste do castelo.
- Ao subtrair metade do X, a limpeza grande do castelo passava por cima da propriedade e apagava casa, portal e fazendas.

Feito:

- `castleOrigin(base)` agora replica a logica do NeoForge:
  - X usa a ancora lateral sem subtrair metade da largura;
  - Z centraliza pela metade da profundidade.
- `castleCenter(base)` agora deriva do origin corrigido.
- Ao finalizar a geracao, o jogador e teleportado para um ponto seguro perto da casa.
- O respawn do jogador e definido nesse ponto da propriedade.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Teste manual necessario:

- Criar outro mapa novo.
- Confirmar que casa, portal, fazendas e castelo aparecem juntos.
- Confirmar que o jogador termina a geracao dentro/perto da casa.

## Fix em 2026-06-05 - menu Magic World aplica criativo, cheats e aura completa

Problema:

- O botao `Modo: Criativo` e a dificuldade do menu Magic World nao estavam sendo aplicados de forma confiavel na criacao real do mundo.
- O fluxo dependia de clicar/ler botoes vanilla por texto, o que podia falhar por idioma, aba ou estado visual.
- O usuario precisa criar mundos de teste com criativo, comandos, cheats, keep inventory e aura/superpoderes ativos.

Feito:

- `ClientEvents` agora grava diretamente no `CreateWorldScreen.getUiState()` antes de chamar `onCreate()`:
  - `setGameMode(CREATIVE/SURVIVAL)`;
  - `setDifficulty(...)`;
  - `setAllowCheats(true)`;
  - `setGameRules(...)`.
- Game rules aplicadas ja na criacao:
  - `keepInventory=true`;
  - `drowningDamage=false`;
  - `fallDamage=false`;
  - `fireDamage=false`;
  - `freezeDamage=false`;
  - `doImmediateRespawn=true`;
  - `sendCommandFeedback=true`;
  - `commandBlockOutput=true`;
  - `logAdminCommands=true`.
- Removida a dependencia de alternar botao vanilla de modo/dificuldade por texto.
- `StarterPortalEvents` reforca as mesmas regras no servidor no login e no fim da geracao da propriedade.
- Se `Modo: Criativo`, o servidor define default game type e o jogador entra em criativo.
- Se comandos estao ligados, o jogador recebe OP/permissao de comando no server local.
- `AuraEvents` permanece ativo com:
  - protecao contra fogo/lava, queda, afogamento e congelamento;
  - sem fome/sem falta de ar;
  - keep inventory reforcado por gamerule;
  - retorno para local da morte;
  - quebra de bloco com clique esquerdo;
  - matar entidade em um golpe.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Teste manual:

- Criar novo mundo pelo painel Magic World com `Modo: Criativo`, dificuldade desejada e `Aura: ON`.
- Confirmar que entra em criativo.
- Confirmar `/gamemode`, `/tp` ou outro comando no chat.
- Testar lava, agua, queda, morte/respawn e inventario.
- Testar quebrar bloco e atacar entidade com aura.

## Fix em 2026-06-05 - background na tela de apagar mundo

Problema:

- A confirmacao `Deseja mesmo apagar esse mundo?` ainda mostrava o fundo vanilla de terra.
- Essa tela nao pertence ao pacote de criacao/selecao de mundo; ela usa `ConfirmScreen`.

Feito:

- `MagicWorldScreenBackgrounds` agora inclui `ConfirmScreen`, `AlertScreen` e `BackupConfirmScreen` na lista de telas com background estatico.
- Isso cobre a tela de apagar mundo e outras confirmacoes/alertas do fluxo inicial.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Teste manual:

- Reiniciar o client.
- Abrir selecao de mundos e clicar em apagar um mundo.
- A tela de confirmacao deve usar o background Magic World, sem fundo de terra vanilla.

## Bloco em 2026-06-05 - loading com logo e reforco do entorno NeoForge

Pedido:

- Colocar a logo Magic World na tela de loading inicial e trocar o texto solto por uma barra de carregamento visualmente coerente com o layout.
- Reforcar o port do terreno NeoForge porque o primeiro import trouxe casa/castelo, mas perdeu chao, decoracao, iluminacao, casas de trabalhadores, villagers e acabamento dos portais.

Feito:

- `InitialLoadNoticeScreen` agora desenha `title_logo.png` acima da mensagem de loading.
- A barra de progresso passou a usar duas cores do layout Magic World: azul/ciano e dourado.
- O painel de loading ficou maior e com moldura dupla para combinar com a tela inicial.
- `StarterPortalEvents` recebeu um primeiro port controlado dos complementos do NeoForge:
  - assentamento dos trabalhadores da plantacao com casas maiores, deposito/rancho, camas, bau, iluminacao e villagers nomeados;
  - assentamento dos cuidadores das fazendas com casas, plantacao de comida dos animais e villagers nomeados;
  - caminhos/estradas principais saindo da casa e entre estruturas;
  - iluminacao reforcada nas ruas, entorno da propriedade, portais e castelo;
  - decoracao extra na praca dos portais com beacons magicos, campfires e vidro roxo;
  - residentes adicionais do castelo com nomes/profissoes visuais, estacoes de trabalho, camas, baus e pequenos abrigos quando o ponto esta ao ar livre.
- Mantida a casa importada que o usuario aprovou visualmente na frente.
- Mantido fora deste bloco o port bruto das entidades custom e IA completa do NeoForge.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Teste manual necessario:

1. Reiniciar o client.
2. Criar mundo novo pelo menu Magic World com fazendas, portal, castelo e aura ligados.
3. Conferir se aparecem os caminhos, luzes, casas do canto das plantacoes, cuidadores das fazendas, moradores do castelo e decoracao ao redor dos portais.
4. Mandar screenshots dos pontos ainda vazios para o segundo ajuste fino do terreno.

## Fix em 2026-06-05 - loading, aura, ESC, currais, mina e terreno

Pedido:

- Remover texto `Primeira criacao de mapa` do loading.
- Corrigir logo pixelada do loading usando asset de maior resolucao.
- Trazer menu Magic World ao apertar ESC, como no NeoForge.
- Fazer efeitos da aura nao brilharem/nao mostrarem particulas/icones.
- Corrigir rebaixo marrom das ruas/caminhos.
- Corrigir currais que ganharam camada interna e deixavam animais fugir.
- Corrigir abrigos/estruturas dos villagers do castelo aparecendo voando.
- Trazer predio da mina e mina que faltaram no gramado.

Feito:

- `MagicWorldStaticBackground.FULL_LOGO` agora aponta para `textures/gui/title/logo_full.png` em 2172x724.
- `InitialLoadNoticeScreen` removeu os textos extras e manteve logo + mensagem atual + barra de progresso.
- `AuraEvents` remove efeito visivel existente antes de reaplicar versao invisivel/sem icone/sem particulas.
- `ClientEvents` adiciona botao `MagicWorld` no PauseScreen/ESC.
- Criada `MagicWorldCentralPauseScreen` com botoes:
  - abrir menu completo da varinha;
  - receber varinha;
  - teleportar para casa;
  - dia/noite;
  - sol/chuva;
  - voltar/continuar.
- `MagicWorldNetwork` recebeu pacote client->server para executar esses atalhos.
- Removido uso de `DIRT_PATH` dos caminhos novos para evitar o rebaixo marrom; caminhos agora usam blocos inteiros (`SMOOTH_STONE`/`POLISHED_ANDESITE`) com preenchimento abaixo.
- Currais agora seguem a logica NeoForge: piso de grama no chao e cercas/gates um bloco acima, evitando camada interna errada e reduzindo fuga dos animais.
- Villagers do castelo agora caem para ponto de chao/superficie se nao houver piso interno seguro, evitando abrigos voando.
- Adicionado predio da mina e mina subterranea leve no offset NeoForge `base.offset(67, -1, 46)`:
  - casa de pedra/deepslate;
  - bau de ferramentas, minerios, comida e construcao;
  - shaft com escada;
  - galerias subterraneas com minerios, trilhos, suportes e baus.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Teste manual necessario:

1. Reiniciar o client.
2. Criar mundo novo pelo menu Magic World.
3. Conferir loading sem texto extra e logo sem pixelacao.
4. Entrar no mundo, apertar ESC e testar o botao `MagicWorld`.
5. Conferir aura sem brilho visual no personagem.
6. Conferir currais sem camada interna alta e com animais presos.
7. Conferir se estruturas dos villagers do castelo nao ficam voando.
8. Conferir se o predio da mina e a mina aparecem no gramado.

## Bloco em 2026-06-05 - mods externos, packs e menu grafico

Pedido:

- Listar os mods externos necessarios para shaders/resource packs.
- Registrar que futuramente teremos um pacote all-in-one, mas por enquanto os mods ficam soltos em `run/mods`.
- Corrigir resource packs locais da pasta `run` para Forge/Minecraft 1.20.1.
- Portar o menu grafico do NeoForge de forma compativel com Forge 1.20.1.

Feito:

- Criado `docs/MODS_RECOMMENDED.md` com lista de mods para baixar em `run/mods`.
- Registrada a regra de trabalho:
  - usar mods soltos em `run/mods` agora;
  - futuro all-in-one so depois da estabilizacao;
  - baixar sempre `Forge 1.20.1`, nunca `Fabric` ou `NeoForge` nesta base.
- Verificado que os resource packs locais usam recursos OptiFine:
  - `assets/minecraft/optifine/`;
  - `ctm/`;
  - `cem/`;
  - colormaps.
- Criado `tools/magicworld_packs.py` para:
  - corrigir `pack.mcmeta` dos ZIPs locais para `pack_format: 15`;
  - habilitar os resource packs em `run/options.txt`;
  - gerar `docs/PACKS_MANIFEST.json` com tamanho, quantidade de entradas e SHA-256.
- Corrigidos localmente os quatro packs:
  - `MagicWorldResource_1.20.1-256x.zip`;
  - `MagicWorldResource_1.20.1-models.zip`;
  - `MagicWorldResource_1.20.1-addon.zip`;
  - `MagicWorldResource_1.20.1-bonus.zip`.
- `run/options.txt` agora lista esses packs com `file/...` e `incompatibleResourcePacks:[]`.
- Portado/adaptado `MagicWorldGraphicsProfile` do NeoForge para Forge 1.20.1:
  - usa `GraphicsStatus` e `net.minecraft.client.ParticleStatus`;
  - aplica distancia de renderizacao, simulacao, particulas, nuvens, modo grafico, distancia de entidades, oclusao ambiente e limite de FPS.
- Criado `GraphicsProfilesMenu` no Forge:
  - perfis Ultra fraco, Fraco, Intermediario, Medio, Forte e Ultra forte;
  - abrir pasta `resourcepacks`;
  - abrir pasta `shaderpacks`;
  - verificar se Oculus/Iris compat esta instalado.
- Adicionada entrada `Graficos` no menu `Sistema`.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

Observacao importante:

- Shaderpack sozinho em `run/shaderpacks` nao renderiza no Forge puro.
- Para Forge 1.20.1, usar Oculus + Embeddium em `run/mods`.
- Os ZIPs grandes em `run/resourcepacks` continuam fora do Git por limite normal do GitHub; apenas script/manifesto/documentacao entram no repositorio.

## Bloco em 2026-06-05 - logo menor, central ESC e menu grafico visual

Pedido:

- Reduzir a logo da tela inicial porque estava grande/pixelada e invadindo textos.
- Corrigir o menu `MagicWorld` ao lado/abaixo de Mods no pause para abrir a mesma central do NeoForge.
- Corrigir abas/botoes comprimidos do menu premium.
- Dar ao menu grafico icone/texto mais coerente com o NeoForge.

Feito:

- `MagicWorldTitleScreen#getLogoWidth()` reduzido para escala menor e mais proxima da proporcao visual do Minecraft.
- Portado do NeoForge para Forge o pacote leve `com.magicworld.central`:
  - `MagicWorldCentralData`;
  - `MagicWorldCentralSnapshot`;
  - `MagicWorldCentralSector`;
  - `MagicWorldCentralResidentPlan`.
- Criadas telas Forge:
  - `MagicWorldCentralOverviewScreen`;
  - `MagicWorldCentralDetailScreen`;
  - `MagicWorldCentralUi`.
- `ClientEvents#tunePauseMenu` agora faz o botao `MagicWorld` abrir `MagicWorldCentralOverviewScreen`, nao mais a tela curta de atalhos.
- `MagicWorldMenuTheme` ganhou `drawFrame(...)` para as telas da central usarem moldura Magic World.
- `PremiumMenuScreen` recebeu o calculo de abas em multiplas linhas portado do NeoForge:
  - `TAB_HEIGHT`;
  - `TAB_ROW_GAP`;
  - `TAB_MIN_WIDTH`;
  - `getTabsPerRow()`;
  - `getTabRows()`.
- O menu grafico em `Sistema` agora usa `Items.SPYGLASS` e textos voltados a Oculus/Embeddium/resourcepacks.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Observacao:

- `mods/mods.txt` aparece deletado no Git, mas isso nao foi alterado nesta etapa. Nao commitar sem confirmacao do usuario.

## Bloco em 2026-06-05 - terreno importado, limite vivo e villagers no chao

Pedido:

- Corrigir a area inicial importada porque havia buracos, chao incompleto e estruturas fora/soltas.
- Respeitar o limite de arvores/folhas do NeoForge como area fechada da propriedade.
- Fazer villagers do castelo e estacoes irem para o chao quando nao houver piso interno seguro.
- Garantir que a mina/gramado e estruturas internas sejam geradas sobre base plana.

Feito:

- `StarterPortalEvents` agora prepara uma fundacao global antes da casa e antes das fazendas.
- A fundacao usa os limites fixos do NeoForge:
  - X: `-128..122`;
  - Z: `-76..80`.
- A fundacao:
  - preenche subsolo com dirt;
  - coloca gramado em `base - 1`;
  - limpa ar acima ate 24 blocos;
  - preserva blocos protegidos como baus, portais, gateway, end portal frame e bedrock.
- O footprint da casa importada e preservado para nao apagar a estrutura colocada pelo template.
- O limite da propriedade deixou de ser so flores soltas e passou a usar a logica viva do NeoForge:
  - arbustos de folhas;
  - flowering azalea leaves;
  - pequenas arvores de borda;
  - aberturas ocasionais controladas.
- `buildImportedEstateFarms` continua gerando:
  - fazendas;
  - currais;
  - trabalhadores;
  - ruas;
  - praca verde;
  - mina em `base.offset(67, -1, 46)`;
  - iluminacao.
- `castleGroundResidentSpot` foi corrigido:
  - antes podia manter coordenada alta antiga;
  - agora procura chao caminhavel proximo e usa isso como fallback quando nao acha piso interno seguro.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual necessario:

- Criar mundo novo.
- Confirmar se o gramado da area inicial esta preenchido sem buracos.
- Conferir se o limite lateral aparece com folhas/arvores delimitando a area.
- Conferir se a mina aparece no gramado.
- Conferir se villagers/estacoes do castelo nao ficam mais no ar.
- Conferir se estruturas novas nao ficaram fora do limite visual da propriedade.

## Bloco em 2026-06-05 - fix botao MagicWorld no ESC

- Corrigido `ClientEvents#tunePauseMenu`.
- O Forge agora reposiciona o botao `Mods` como o NeoForge:
  - largura reduzida;
  - alinhado abaixo de `Opcoes`.
- O botao `MagicWorld` nao sobrepoe mais o botao `Mods`.
- O clique em `MagicWorld` voltou a abrir `MagicWorldCentralPauseScreen`, igual ao fluxo do NeoForge para o ESC.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

## Bloco em 2026-06-05 - logo pequena no menu ESC

- A logo gigante estava em `MagicWorldCentralPauseScreen`, nao na tela inicial.
- Reduzido `logoWidth` do menu ESC de ate `250px` para no maximo `120px`.
- Titulo/subtitulo e botoes foram reposicionados para acompanhar a logo menor.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

## Bloco em 2026-06-05 - abrigos dos villagers do castelo no chao

- Corrigido `spawnCastleResident(...)` em `StarterPortalEvents`.
- Antes: se encontrasse um piso alto sem teto, aceitava a coordenada e `decorateCastleResidentStation(...)` criava abrigo/cama/mesa/baú no ar.
- Agora: se o ponto nao tem teto e existe chao caminhavel proximo bem abaixo, o resident/abrigo desce para esse chao antes da decoracao.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Criar mundo novo.
- Conferir os abrigos/estacoes dos villagers do castelo que antes ficavam flutuando.

## Bloco em 2026-06-05 - buracos no entorno da casa

- Corrigido entorno lateral/fundos da casa importada em `StarterPortalEvents`.
- Criado `stabilizeImportedHousePerimeterTerrain(...)`.
- A correção roda depois da casa ser colocada.
- Ela preenche um anel ao redor da casa:
  - dentro dos limites da propriedade;
  - sem entrar no footprint da casa;
  - sem limpar estruturas;
  - preenchendo suporte abaixo com dirt;
  - colocando gramado na altura do terreno quando a coluna esta em ar/fluido/bloco natural.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Criar mundo novo.
- Conferir laterais e fundos da casa, especialmente os buracos mostrados nos prints.

## Bloco em 2026-06-05 - praca verde dentro do limite

- Corrigido `StarterPortalEvents#buildGreenVillageSquare`.
- A praca verde estava em `base.offset(-54, 0, 96)`, fora do limite da propriedade (`Z maximo 80`).
- A praca agora nasce em `base.offset(-106, -1, 34)`, dentro do setor verde protegido.
- As duas casas da praca foram reposicionadas para `base.offset(-124, -1, 50)` e `base.offset(-101, -1, 50)`, evitando invadir o footprint da casa importada.
- Criado `stabilizeGreenVillageDistrictTerrain(...)` para preencher o gramado e fechar vaos no distrito verde (`X -126..-84`, `Z 0..70`).
- Adicionadas ruas internas ligando o setor verde ao eixo oeste da propriedade.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Criar mundo novo.
- Conferir se casas, construcao comunitaria e ruas da praca verde aparecem dentro do limite de folhas/arvores.
- Conferir se o vao mostrado nos prints nessa regiao foi fechado com gramado.
- Confirmar se nenhuma parte da praca atravessa a casa importada.

Nota:

- `mods/mods.txt` ainda aparece deletado no Git, mas essa delecao nao foi feita nesta etapa e segue sem commit ate confirmacao.

## Bloco em 2026-06-05 - portal original e casa da mina

- Corrigido `StarterPortalEvents` para recuperar decoracao do portal no estilo NeoForge.
- `buildStarterPortal(...)` agora chama:
  - `decoratePortalGarden(...)`;
  - `clearPortalGrassRing(...)`.
- O jardim do portal voltou a gerar:
  - flores;
  - musgo;
  - samambaias;
  - azaleias;
  - lampioes extras;
  - fogueiras com vidro azul;
  - animais nomeados ao redor do portal: papagaios, coelhos e allay.
- Corrigida a casa da mina:
  - antes o Forge recalculava altura com `filledGroundAt(...)`;
  - agora usa a ancora fixa do NeoForge em `base.offset(67, -1, 46)` sem heightmap;
  - isso evita separar predio, bau, armaduras e mina subterranea.
- A casa da mina recebeu:
  - baus internos;
  - suportes de armadura com couro, malha, ferro, ouro, diamante e netherite;
  - banners externos;
  - decoracao de oficina/forja;
  - lampioes adicionais.
- Mantida a logica de villagers do castelo que procura chao caminhavel quando o ponto original fica alto/sem teto.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Criar mundo novo.
- Conferir se o portal aparece com jardim/decoracao/animais ao redor.
- Conferir se a casa da mina aparece no gramado entre castelo e casa, exatamente sobre a mina subterranea.
- Conferir se os baus e suportes de armadura da mina nao aparecem quebrados/soltos no chao.
- Conferir se villagers/estacoes do castelo nao ficam flutuando sem acesso ao chao.

## Bloco em 2026-06-05 - layout dos botoes Mods e MagicWorld no ESC

- Corrigido `ClientEvents#tunePauseMenu`.
- O botao `Mods` nao fica mais pequeno nem espremido ao lado/abaixo de outro botao.
- `Mods` agora usa uma linha propria, centralizada.
- `MagicWorld` agora usa a linha abaixo, com mesma largura, altura e alinhamento.
- O botao de sair/salvar e sair e empurrado para baixo quando existe, evitando sobreposicao.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Abrir um mundo, apertar ESC.
- Conferir `Mods` em uma linha, `MagicWorld` na linha seguinte e botao de saida abaixo, todos alinhados e sem sobreposicao.
