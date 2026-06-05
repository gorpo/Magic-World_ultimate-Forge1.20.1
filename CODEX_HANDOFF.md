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
