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
  - comandos/cheats sao forÃƒÆ’Ã‚Â§ados quando opcoes Magic World exigem comandos;
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
- Antes: se encontrasse um piso alto sem teto, aceitava a coordenada e `decorateCastleResidentStation(...)` criava abrigo/cama/mesa/baÃƒÆ’Ã‚Âº no ar.
- Agora: se o ponto nao tem teto e existe chao caminhavel proximo bem abaixo, o resident/abrigo desce para esse chao antes da decoracao.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Criar mundo novo.
- Conferir os abrigos/estacoes dos villagers do castelo que antes ficavam flutuando.

## Bloco em 2026-06-05 - buracos no entorno da casa

- Corrigido entorno lateral/fundos da casa importada em `StarterPortalEvents`.
- Criado `stabilizeImportedHousePerimeterTerrain(...)`.
- A correÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o roda depois da casa ser colocada.
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

## Bloco em 2026-06-06 - ajustes finos de terreno, rua, mina e castelo

- Corrigido `prepareBreathingSurface(...)` para nao apagar blocos estruturais/decorativos na frente da casa importada.
- A limpeza ao redor da casa agora remove apenas terreno natural, plantas, fluidos e blocos substituiveis.
- Ruas principais e caminhos entre estruturas receberam acabamento lateral com `SMOOTH_STONE_SLAB`, criando meia linha de bloco sem elevar a rua inteira.
- Adicionado `stabilizeEstateOpenGapTerrain(...)` para fechar faixas abertas:
  - lateral oeste/perto do limite;
  - fundos/lado da casa;
  - linha faltante perto das plantacoes.
- A casa da mina recebeu `reinforceStoneTreasureMineHouseShell(...)`, recolocando piso, paredes, telhado e porta depois da decoracao para evitar aparecerem apenas armaduras/itens.
- Villagers/estacoes do castelo agora preferem chao caminhavel proximo quando o ponto original esta alto demais, mesmo que exista teto no ponto alto.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Criar mundo novo.
- Conferir frente da casa importada.
- Conferir meia-linha de slabs nas ruas.
- Conferir se o predio da mina aparece junto com armaduras e baus.
- Conferir se villagers/casas do castelo nao ficam presos no ar.
- Conferir fechamento da lateral perto do limite e da linha faltante perto das plantacoes.

## Bloco em 2026-06-06 - menu secreto do pause

- Portado o `MagicWorldSecretMinecraftScreen` do NeoForge para Forge 1.20.1.
- `MagicWorldCentralPauseScreen` voltou a ter o botao `Menu secreto do Minecraft`.
- O menu secreto contem abas:
  - Jogador;
  - Mundo;
  - Teleporte;
  - Itens;
  - Resources.
- A aba de itens lista o registro vanilla/modded e envia `secret_give:<id>` ao servidor.
- `MagicWorldNetwork` agora trata as acoes secretas:
  - dar item;
  - god mode/protecao/velocidade sem particulas visiveis;
  - limpar efeitos;
  - criativo/sobrevivencia;
  - keep inventory on/off;
  - random tick normal/rapido/turbo/lento;
  - spawn aqui/spawn na casa;
  - subir +64;
  - kits Nether/Fim.
- A acao visual `resource_magicworld` foi adaptada para Forge atual: mostra instrucao e recarrega resource packs, pois o controller visual do NeoForge ainda nao existe nesta base.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Entrar no mundo, apertar ESC, clicar `MagicWorld`.
- Abrir `Menu secreto do Minecraft`.
- Validar abas, rolagem, clique em itens e acoes principais.

## Bloco em 2026-06-06 - correcao layout pause Mods/MagicWorld

- Corrigido novamente `ClientEvents#tunePauseMenu`.
- O ajuste anterior reposicionava `Mods`, `MagicWorld` e apenas o botao de sair, causando sobreposicao com botoes vanilla intermediarios.
- Agora o codigo:
  - usa o botao LAN/Opcoes como ancora;
  - expande `Mods` e `MagicWorld` para largura de botao principal;
  - coloca `Mods` em uma linha propria;
  - coloca `MagicWorld` na linha abaixo;
  - empurra todos os botoes que estavam abaixo da ancora, preservando a ordem visual original.
- Validado com `./gradlew.bat build`: BUILD SUCCESSFUL.

Teste manual:

- Abrir ESC e conferir se `Mods`, `MagicWorld`, `Salvar e sair/Menu principal` e demais botoes nao se sobrepoem.

## Bloco em 2026-06-06 - personalizacao da tela grafica Embeddium

- O codigo de personalizacao Sodium do NeoForge foi adaptado para a API real do Embeddium `0.3.31` usada no Forge 1.20.1.
- Criado `EmbeddiumVideoOptionsScreenMagicWorldMixin` opcional:
  - aplica o background estatico Magic World;
  - troca o titulo para `Magic World - Graficos`;
  - troca o logo principal do Embeddium pelo logo grafico Magic World;
  - oculta os botoes de doacao do Embeddium.
- `magicworld.mixins.json` passou a ser opcional para nao impedir a inicializacao quando Embeddium nao estiver instalado.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- Validado com `./gradlew.bat runClient --stacktrace`: cliente iniciou com Embeddium/Oculus e permaneceu aberto ate o timeout, sem erro do novo mixin.

Teste pendente:

- Abrir `Opcoes > Configuracoes de video` com Embeddium ativo e validar o visual.

## Bloco em 2026-06-06 - conclusao do menu grafico e ampliacao do casarao

- Regra de trabalho solicitada pelo usuario: nao executar `runClient` nem abrir o cliente Minecraft.
- O usuario realiza todos os testes visuais e funcionais dentro do cliente.
- A validacao do Codex deve usar somente tarefas Gradle de compilacao/build.
- O rodape esquerdo do menu grafico agora cria o botao `Horizontes Distantes` mesmo quando a inicializacao do Distant Horizons ocorre depois da tela.
- `Fechar`, `Aplicar` e `Desfazer` foram reposicionados no rodape esquerdo, seguindo o layout NeoForge.
- Adicionada rolagem circular entre as paginas do Embeddium quando a rolagem ultrapassa o inicio/fim da pagina atual.
- Ampliadas as substituicoes de cores para o tema ciano Magic World.
- O reparo da propriedade foi elevado para a versao 3 e executara uma vez no proximo login.
- O casarao importado recebeu:
  - acabamento de madeira, escadas decorativas e lanternas acima das portas existentes;
  - alargamento conservador de janelas existentes, somente em paredes entre dois espacos de ar;
  - mais iluminacao interna;
  - estantes, vasos, plantas, bancadas, barris e itens uteis em locais internos cobertos;
  - bau cheio de varinhas Magic World;
  - baus com ferramentas e itens raros/premium;
  - exposicao de armadura Netherite;
  - exposicao de armadura Netherite nomeada `Magic World`.
- O Forge 1.20.1 ainda nao registra os itens da armadura personalizada do NeoForge. Por isso, a exposicao Magic World usa Netherite nomeada sem referenciar IDs inexistentes.
- `compileJava` validado com sucesso. Nao foi aberto cliente.

### Fix do crash ao abrir Configuracoes de video

- O teste manual revelou `VerifyError: Bad type on operand stack` no construtor de `EmbeddiumVideoOptionsScreen`.
- Causa: `@ModifyConstant` no construtor chamava o handler de instancia antes da chamada ao construtor de `Screen`.
- `magicworld$renameScreen` foi alterado para metodo estatico, evitando acesso a `this` ainda nao inicializado.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

### Port da identidade completa do menu grafico

- Adicionados mixins especificos para os componentes reais do Embeddium Forge 1.20.1:
  - `EmbeddiumTabHeaderMagicWorldMixin`;
  - `EmbeddiumAccentColorMagicWorldMixin`;
  - `EmbeddiumLineColorMagicWorldMixin`.
- Cabecalhos laterais:
  - `Embeddium` agora aparece como `Magic World`;
  - `Oculus/Iris` agora aparece como `Magic World Shaders`;
  - icones laterais originais foram substituidos pelo icone Magic World.
- Cores rosas de selecao/checks e linhas cinzas foram substituidas pelo tema azul-ciano do NeoForge.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- Validado com `./gradlew.bat runClient --stacktrace`: cliente iniciou sem erro de aplicacao dos novos mixins.

Teste visual pendente:

- Abrir `Opcoes > Configuracoes de video` e confirmar nomes, icones laterais, checks, selecoes e linhas em azul-ciano.

### Revisao fiel do port e Distant Horizons

- Descoberto que o Embeddium Forge agrupa seus cabecalhos pelos IDs internos `sodium` e `iris`, nao somente `embeddium` e `oculus`.
- `EmbeddiumTabHeaderMagicWorldMixin` agora trata os quatro IDs:
  - `sodium`/`embeddium` -> `Magic World`;
  - `iris`/`oculus` -> `Magic World Shaders`.
- Portado `MagicWorldDistantHorizonsButton` para a API GUI do Forge 1.20.1.
- O botao e adicionado no rodape esquerdo da tela grafica quando Distant Horizons esta carregado.
- A abertura usa primeiro `GetConfigScreen_forge`, classe existente no Distant Horizons `3.0.3` para Forge 1.20.1.
- O jar `DistantHorizons-3.0.3-b-1.20.1-fabric-forge.jar` foi movido de `run/mods` para `run/dev-mods`.
- Corrigidos os nomes runtime Forge usados nos hooks opcionais:
  - inicializacao: `m_7856_`;
  - background: `m_280273_`;
  - renderizacao: `m_88315_`.
- Essa correcao fez o botao, background, logo e substituicoes de cores/linhas realmente aplicarem no Embeddium `0.3.31`.
- Removido o icone avulso do canto inferior esquerdo para nao sobrepor o botao do Distant Horizons.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- Em `runClient`, a tela grafica foi carregada e o log confirmou os mixins Magic World em `EmbeddiumVideoOptionsScreen`, `TabHeaderWidget`, `FlatButtonWidget`, frames, busca e barras sem `MixinApplyError`, `InjectionError` ou `VerifyError`.
- Distant Horizons `3.0.3-b` inicializou no mesmo teste.

## Bloco em 2026-06-06 - reparo da propriedade existente e menu grafico

- `StarterPortalEvents` agora possui reparo versionado `MagicWorldForgeEstateRepairVersion=2`.
- No proximo login de uma propriedade ja criada, o reparo executa uma unica vez:
  - restaura `imported_house.nbt` sem limpar novamente o volume;
  - normaliza a rua frontal e adiciona acabamento lateral de slab;
  - preenche somente vazios de ar no nivel do solo, preservando agua e blocos existentes;
  - exclui a area da mina do preenchimento geral;
  - reconstrÃƒÆ’Ã‚Â³i a casa da mina por ultimo.
- O NBT da casa Forge e NeoForge possui o mesmo SHA-256:
  - `160F27F53D3163CF32C4E4F854618C080DBD52E2564E22B486B48827187F491D`.
- A casa da mina recebeu novamente paredes, janelas, telhado com escadas, entrada, decoracao, baus e armaduras internas.
- `ClientEvents#tunePauseMenu` oculta o botao do Distant Horizons injetado no menu de pausa.
- O mixin do Embeddium renderiza explicitamente `MagicWorldDistantHorizonsButton` no menu grafico e o posiciona acima da barra de acoes.
- `Support Sodium` e ocultado antes da montagem do frame e novamente ao final do `init`.
- Distant Horizons foi movido de `run/mods` para `run/dev-mods`.
- Validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `runClient` iniciou com Distant Horizons `3.0.3-b` sem `MixinApplyError`, `InjectionError`, `VerifyError` ou erro fatal.

## Bloco em 2026-06-06 - layout responsivo e remocao do icone DH

- `MagicWorldSecretMinecraftScreen` passou a calcular abas, categorias e colunas de itens pela largura disponivel.
- Categorias quebram em linhas e a grade recebe scissor, eliminando vazamento em janela pequena.
- A injecao do pequeno icone do Distant Horizons foi localizada em `MixinOptionsScreen`.
- `ClientEvents` agora oculta widgets cujo pacote pertence ao Distant Horizons em telas vanilla; o botao Magic World no menu grafico nao e afetado.
- O descritor de `parentBasicFrameBuilder` no mixin Embeddium usa `CallbackInfoReturnable<?>`.
- O reparo da mina agora detecta armaduras nomeadas existentes antes de gerar novas, evitando duplicatas.
- Build final validado com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.

## Commit consolidado - 2026-06-06 10:24:52 -03:00

- Escopo consolidado: menu grafico Embeddium/Distant Horizons, menu secreto responsivo, reparo versionado da propriedade, rua, valos, casa da mina e decoracao do casarao.
- Validacao obrigatoria: somente Gradle.
- Validacao final executada antes do commit: `./gradlew.bat build --stacktrace`.
- Resultado: `BUILD SUCCESSFUL`.
- Nao executar `runClient`; o usuario realiza os testes no cliente.

## Correcao pendente apos commit - 2026-06-06 11:06:00 -03:00

- Usuario reportou que a frente da casa perdeu blocos/muro e que a rua ainda nao ficou nivelada na linha da casa.
- Causa identificada: `normalizeImportedHouseFrontRoad` desenhava rua dentro do footprint da casa importada (`z=14..78`, enquanto `IMPORTED_HOUSE_MAX_Z=75`).
- Correcao aplicada:
  - `CURRENT_ESTATE_REPAIR_VERSION` elevado para 4;
  - mensagem do reparo atualizada para frente/muro/rua;
  - rua movida para fora da estrutura, iniciando em `IMPORTED_HOUSE_MAX_Z + 1`;
  - removido meio bloco lateral nesta faixa;
  - rua usa bloco inteiro no nivel `base.y`, alinhada com a casa.
- Validacao final desta correcao: `./gradlew.bat build --stacktrace` com `BUILD SUCCESSFUL`.
- Nao executar `runClient`; usuario testa o cliente.

## Entrega reforcada concluida - 2026-06-06 11:23:16 -03:00

- Usuario reportou que o casarao, Distant Horizons no menu grafico e villagers ainda nao estavam completos visualmente.
- Reparo versionado elevado para 5.
- Casarao:
  - fachada explicita no sul da estrutura, fora da rotina automatica;
  - arco/blocos acima da porta;
  - muro/guarnicao frontal;
  - janelas novas;
  - luzes e plantas externas;
  - baus premium, varinhas e armaduras no interior.
- Armadura personalizada:
  - registrados os itens Forge reais `Draconic Aether`;
  - baus e suporte de armadura usam os itens reais, nao apenas Netherite renomeada.
- Menu grafico:
  - rodape esquerdo reservado por mixin;
  - botao `Horizontes Distantes` adicionado ao frame do Embeddium por reflexao;
  - overlay com icone Magic World ainda renderiza o botao no rodape esquerdo;
  - background opaco oculta o jogo atras;
  - cores/fundos/busca/contornos reforcados.
- Villagers:
  - casas contam camas e geram trabalhadores na mesma quantidade;
  - villagers ficam invulneraveis, persistentes, nivel 5, alcance ampliado e efeitos longos;
  - casas recebem mesa, cadeiras, fogao, bau/barril, luzes premium, plantas e uma ave;
  - distritos recebem guardioes de ferro.
- Validacao parcial passou com `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- Validacao final passou com `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- Nao executar `runClient`; usuario testa o cliente.

## Fix do crash ao abrir Graficos - 2026-06-06 11:32:30 -03:00

- Usuario testou o cliente e reportou crash ao clicar em Graficos.
- Crash report novo: `crash-2026-06-06_11.27...txt`.
- Erro principal: `NoClassDefFoundError: org/spongepowered/asm/synthetic/args/Args$1` em `SodiumOptionsGUI.init`.
- Causa no patch anterior: `@ModifyArgs` em `EmbeddiumVideoOptionsScreenMagicWorldMixin` para reservar altura no frame do Embeddium.
- Correcao:
  - removido o `@ModifyArgs`;
  - removida a criacao reflexiva de `FlatButtonWidget` dentro do frame;
  - mantido botao proprio `MagicWorldDistantHorizonsButton` no rodape esquerdo;
  - adicionado tratamento prioritario de `mouseClicked` para o botao do Distant Horizons antes do frame consumir o clique.
- Auditoria NeoForge:
  - rolagem circular ja existe no Forge via `EmbeddiumTabFrameCircularScrollMagicWorldMixin`;
  - fundo opaco, logo, icones laterais, cores, linhas e busca estao cobertos pelos mixins Embeddium;
  - mixin NeoForge `IrisConfigMagicWorldMixin` nao tem alvo identico no Oculus Forge 1.20.1; o jar usa `IrisSodiumOptions`/`MixinSodiumGameOptionPages`, e o titulo Oculus/Iris ja e renomeado por `EmbeddiumTabHeaderMagicWorldMixin`.
- Validar somente com Gradle. Nao executar `runClient`.

## Correcao cirurgica menu grafico, casa e rua - 2026-06-06 11:56:55 -03:00

Regra operacional:

- Nao executar `runClient`.
- O usuario testa o cliente.
- Codex valida somente com Gradle.

Feito:

- `EmbeddiumVideoOptionsScreenMagicWorldMixin` deixou de posicionar o botao `Horizontes Distantes` por `height - 68`.
- O botao agora procura o `TabFrame` real do Embeddium, le `tabSection`/`tabSectionInner` e fica abaixo da lista lateral, na area correta do menu grafico.
- O painel do botao Distant Horizons agora envolve apenas o botao, sem criar um bloco solto ate o rodape.
- Background do menu grafico ficou mais opaco para ocultar o jogo atras.
- Botoes/textos de suporte/doacao do Embeddium/Sodium agora sao ocultados, desabilitados, sem label e movidos para fora da tela.
- `EmbeddiumTabFrameCircularScrollMagicWorldMixin` usa tambem `getOffset()` para detectar limite da barra, reforcando a rolagem circular entre abas.
- `CURRENT_ESTATE_REPAIR_VERSION` elevado para 7.
- O reparo existente remove drops soltos antes/depois da casa e dos baus.
- `imported_house.nbt` e restaurada sem limpar volume novamente, depois das rotinas de terreno, preservando frente/muro originais.
- Estradas e caminhos agora ignoram o footprint da casa importada para nao cortar a estrutura.
- Rua frontal/lateral ao redor da casa foi elevada com bloco inteiro, em vez de manter o tracado baixo com slabs.
- `placeChest` limpa containers existentes antes de substituir o bloco, evitando drops de conteudo em reparos repetidos.
- Casas de trabalhadores receberam acabamento externo: blocos acima das portas, janelas extras, postes, luzes e plantas.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- Cliente nao foi aberto.

Teste manual esperado:

- Abrir `Opcoes > Configuracoes de video`.
- Conferir `Horizontes Distantes` abaixo das entradas laterais, perto de `Magic World Shaders`, sem icone solto no rodape.
- Testar rolagem circular nas abas do menu grafico.
- Entrar no save existente e conferir que a mensagem do reparo versao 7 aparece uma vez.
- Conferir frente da casa/muro restaurados, rua elevada na linha da casa e ausencia de itens de baus voando.

## Correcao definitiva do menu grafico - 2026-06-06 13:17:59 -03:00

Regra operacional obrigatoria:

- Nao executar `runClient`.
- O usuario realiza o teste visual e funcional no cliente.
- O Codex valida somente com Gradle.

Diagnostico corrigido:

- A implementacao anterior nao criava uma aba real do Embeddium. Ela desenhava um widget externo e, quando nao encontrava o `TabFrame`, deixava somente um icone solto no rodape.
- No Embeddium `0.3.31`, o ponto correto e `EmbeddiumVideoOptionsScreen#createShaderPackButton`.
- Com Oculus carregado, o grupo real dos shaders e `oculus`, nao `iris`.

Implementacao atual:

- Removido completamente o widget externo do Distant Horizons e seu tratamento manual de clique/render.
- Criada uma aba Embeddium real `Horizontes Distantes`, inserida depois de `Pacote de sombreadores...` no mesmo grupo Oculus/Iris.
- A aba abre a tela do Distant Horizons pela acao oficial de selecao e possui o icone Magic World dentro da propria entrada.
- O fundo estatico Magic World agora e desenhado no inicio do render principal e tambem substitui o background do Embeddium, ocultando o jogo atras.
- O tema global dos botoes do Embeddium recebeu paineis azul-escuros, hover ciano, estados desabilitados personalizados, sliders ciano, linhas e destaques ciano.
- A rolagem circular detecta inicio/fim da pagina direita, avanca/retorna entre abas com `floorMod`, volta da ultima para a primeira e executa corretamente a acao de abas externas.
- `Support Sodium`/doacao continua oculto, desabilitado, sem texto e fora da tela.

Auditoria do NeoForge:

- Revisados `SodiumConfigBuilderMagicWorldMixin`, `SodiumVideoSettingsScreenMagicWorldLayoutMixin`, `SodiumDonationButtonCompatMixin`, `SodiumExternalPageEntryMagicWorldMixin` e `IrisConfigMagicWorldMixin`.
- O Forge/Embeddium antigo nao possui o mesmo construtor global do Sodium novo; o resultado equivalente foi aplicado nos componentes reais desta versao.
- O prefixo de pagina externa do Sodium novo nao existe no `TabFrame` deste Embeddium.
- O Oculus usa o grupo dinamico `oculus`; ele permanece renomeado para `Magic World Shaders`.

Validacao executada ate este registro:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- Cliente nao foi aberto.

## Correcao completa do menu grafico, casas e villagers - 2026-06-06 13:42:26 -03:00

Regra obrigatoria:

- Nao executar `runClient` nem abrir o cliente Minecraft.
- O usuario realiza os testes visuais e funcionais no cliente.
- O Codex valida somente com tarefas Gradle.

Diagnostico confirmado pelos prints:

- `Horizontes Distantes` ja aparecia abaixo de `Pacote de sombreadores...`.
- O icone solto no canto inferior esquerdo era o logo nativo `logoDim` do Embeddium.
- O fundo Magic World era desenhado antes do background nativo e depois era sobrescrito pelo jogo.
- Linhas selecionadas, checkboxes e barra de rolagem ainda usavam constantes rosas/cinzas do Embeddium.
- A casa grande dos agricultores era a casa gerada da plantacao, nao somente a `imported_house.nbt`.
- A rotina `decorateImportedHouseFrontFacade` existia, mas nao era chamada.

Menu grafico:

- O background Magic World agora e desenhado depois da chamada nativa do Embeddium, ocultando o jogo atras.
- O logo nativo solto do Embeddium e movido para fora da tela em toda inicializacao/renderizacao.
- `Horizontes Distantes` permanece como aba real logo abaixo dos shaders, com icone dentro da entrada.
- A rolagem circular/infinita do `TabFrame` permanece ativa entre inicio/fim das paginas.
- Mixins dedicados, com alvos exatos, substituem selecao, foco, checkboxes e barra de rolagem pelo ciano `0xFF00D9FF`.
- Botoes de suporte/doacao continuam ocultos.
- Portado `MagicWorldClientCompat` do NeoForge para Forge 1.20.1:
  - forÃƒÆ’Ã‚Â§a `showDhOptionsButtonInMinecraftUi = false`;
  - forÃƒÆ’Ã‚Â§a `renderingApi = "OPEN_GL"`.

Propriedade e casas:

- `CURRENT_ESTATE_REPAIR_VERSION` elevado para `8`; o reparo executara uma vez no proximo login.
- A fachada frontal do casarao importado agora e realmente aplicada.
- O bau de varinhas fica completamente preenchido.
- Casas da plantacao receberam telhado inclinado, frontoes fechados, janelas superiores, mais janelas laterais, vigas externas e acabamento acima das portas.
- Interior das casas recebeu mais luzes, mesa/cadeiras, tapetes, fogao, estacoes uteis, estantes, plantas e uma ave por casa.
- O rancho grande recebeu janelas, telhado inclinado, luzes premium, decoracao, estacoes uteis, bau cheio de varinhas, bau de itens raros, armadura Netherite e armadura Draconic Aether.

Villagers:

- As rotinas de cada casa continuam contando camas e gerando um trabalhador por cama.
- Busca por nomes usa toda a area da propriedade para impedir duplicatas quando villagers se afastam.
- Todos os villagers da propriedade sao reforcados novamente a cada 200 ticks:
  - invulneraveis e persistentes;
  - vida maxima restaurada;
  - profissao nivel 5;
  - efeitos longos;
  - raio de trabalho/restricao minimo de 192 blocos.
- Monstros dentro da propriedade sao removidos na manutencao periodica para os villagers nao perderem tempo com inimigos.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- Cliente nao foi aberto.

## Reparo pontual do portal, casa da mina e currais - 2026-06-06

- Reparo elevado para a versao `11`.
- Base, suporte, pilares, topo e vidros laterais do portal inicial sao preenchidos somente quando o bloco esperado estiver em ar.
- Nenhum bloco existente e substituido e a abertura central do portal permanece livre.
- Corrigida a regressao que deixava armaduras e itens expostos no gramado: a limpeza/geracao posterior do castelo podia apagar a casa fechada construida sobre a mina.
- A casa de pedra da mina agora e restaurada como ultima etapa tanto na criacao inicial quanto no reparo de propriedades existentes.
- Itens soltos ao redor da casa da mina sao removidos antes e depois da restauracao; paredes, telhado, janelas, porta, baus, decoracao e acesso a mina sao reconstruidos.
- Os seis currais sao reparados somente em sua linha exata de cerca, sem limpar a plantacao ou estruturas vizinhas, e mantem uma unica porteira cada.
- Cada curral recebe agua, feno, composteira, bau de alimento, uma especie exclusiva, tres adultos e dois filhotes.
- Cuidadores existentes sao vinculados individualmente aos seis currais; a manutencao repoe apenas animais nomeados ausentes e incentiva reproducao ate o limite controlado.
- Validacao em 2026-06-06 14:05:06 -03:00:
  - `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
  - `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
  - `git diff --check`: sem erros.
  - Cliente nao foi aberto; nao executar `runClient`.

## Conselho do castelo e alcance global dos aldeoes - 2026-06-06

- Reparo elevado para a versao `12`.
- Quatro aldeoes sao posicionados ao redor da mesa central do castelo nos offsets confirmados pelos prints:
  - `(+2, 0, +5)`: Bibliotecario do Conselho;
  - `(+2, 0, -8)`: Cartografo do Conselho;
  - `(-8, 0, -1)`: Clerigo do Conselho;
  - `(+8, 0, 0)`: Armoreiro do Conselho.
- Cada membro possui profissao nivel 5, item e estacao de trabalho distintos, sem substituir blocos existentes da sala.
- A manutencao reconhece os nomes dos profissionais antigos do castelo e preserva/corrige suas profissoes.
- O raio minimo de trabalho, caminhada e alcance de todos os aldeoes gerenciados passou para `384` blocos.
- A area de manutencao agora inclui integralmente propriedade e castelo.
- Validacao:
  - `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
  - `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
  - `git diff --check`: sem erros.
  - Cliente nao foi aberto; nao executar `runClient`.

## Casa grande premium, Praca Verde e cerejeiras - 2026-06-06 14:33:55 -03:00

Pedido atual:

- Usar as quatro coordenadas dos prints como limites da casa grande.
- Transformar a casa em centro premium de trabalho dos aldeoes.
- Parede leste com armazenamento e porta central de saida.
- Baus com todos os itens possiveis, bau cheio de varinhas magicas, itens premium/raros, set Netherite e set Draconic Aether.
- Mesa, bancadas, fogoes, forjas, ferramentas, decoracao, plantas, janelas e iluminacao forte para monstros nao se aproximarem.
- Villagers imortais; um guardiao aldeao por local para lidar com hostis sem destruir estruturas.
- Mais um bloco/apoio abaixo do sino na Praca Verde, mais decoracao e aves na area.
- Trocar as arvores em volta da casa por cerejeiras rosas, sem converter toda a propriedade e sem interferir nas estruturas.
- Nao abrir cliente; validar somente com Gradle.

Implementado:

- `CURRENT_ESTATE_REPAIR_VERSION` elevado para `14`.
- Volume da casa dos prints: `premiumAnimalWorkCenterCorner(base) = base.offset(106,-1,-72)`, largura `18`, profundidade `14`.
- Esse volume corresponde aos prints atuais em torno de `X 46..64`, `Y 73/74`, `Z -42..-28` quando a base da propriedade esta em `(-60,74,30)`.
- `buildPremiumAnimalWorkCenter` substitui a primeira casa pequena dos cuidadores por um centro premium com:
  - paredes de pedra decorada, postes de dark oak, telhado inclinado e janelas extras;
  - porta oeste e porta leste central, com blocos acima da porta;
  - parede leste de baus/barris e armazenamento auxiliar;
  - preenchimento automatico dos containers pelo registro `BuiltInRegistries.ITEM`;
  - bau de varinhas, bau raro/premium, armaduras Netherite e Draconic Aether;
  - mesa grande, cadeiras, camas, estacoes de trabalho, fornos/forjas, biblioteca, plantas e ave.
- Novos aldeoes profissionais nomeados:
  - `Guardiao Aldeao da Casa Grande`;
  - `Armoreiro da Casa Grande`;
  - `Ferreiro da Casa Grande`;
  - `Ferramenteiro da Casa Grande`;
  - `Bibliotecario da Casa Grande`;
  - `Clerigo da Casa Grande`;
  - `Pedreiro da Casa Grande`.
- `professionForNamedVillager` preserva as profissoes desses novos nomes durante manutencao.
- `spawnEstateGuardianVillagers` garante um guardiao aldeao em Casa Grande, Mina, Currais, Plantacao e Praca Verde.
- `clearHostilesNearGuardianVillagers` remove monstros dentro do raio dos guardioes; a limpeza global de monstros da propriedade/castelo foi mantida como backup seguro.
- Iluminacao reforcada com sea lanterns internas, luzes no perimetro e postes externos da casa.
- `reinforceGreenSquareBell` coloca suporte solido sob o sino da Praca Verde e acabamento de pedra ao redor.
- `decorateGreenSquareGarden` adiciona flores, azaleias, luzes, parrots, chicken ornamental e allay nomeados na Praca Verde.
- `convertImportedHouseExteriorTreesToCherry` converte apenas troncos/folhas naturais perto da casa importada para `CHERRY_LOG`/`CHERRY_LEAVES`, ignorando o footprint da casa e a faixa principal de rua/muro.
- `placeCherryPetalsNearHouseTree` aplica petalas rosas somente em grama livre perto dessas arvores.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros.
- Falta ainda nesta entrega: commit e push.
- Cliente nao foi aberto; nao executar `runClient`.

## Casa NBT no fim da rua durante o loading - 2026-06-06 14:41:27 -03:00

Pedido atual:

- Usar `tmp/extracted/starter_house_1.nbt`, que tambem ja existe rastreado em `src/main/resources/data/magicworld/structures/starter_house_1.nbt`.
- Posicionar a casa dentro das quatro coordenadas enviadas no fim da rua.
- Frente/porta principal/area frontal viradas para o lado da rua.
- A casa precisa estar dentro do loading criado pelo mod, sem gerar depois do loading.

Implementado:

- `CURRENT_ESTATE_REPAIR_VERSION` elevado para `15`.
- Adicionado `STARTER_ROAD_END_HOUSE = magicworld:starter_house_1`.
- O NBT tem tamanho `[20, 15, 30]`.
- Portas lidas do NBT:
  - principal em `[12,3,17]`, `facing=south`;
  - secundaria em `[12,3,23]`, `facing=west`.
- Como a rua fica no lado `+Z/south`, a estrutura e colocada sem rotacao.
- Origem relativa usada: `starterRoadEndHouseOrigin(base) = base.offset(217,-4,-148)`.
- Volume esperado no mundo atual dos prints: aproximadamente `X 157..176`, `Y 70..84`, `Z -118..-89`.
- `handleEstateTask` ganhou etapa propria:
  - step `5`, progresso `94`, mensagem `Carregando casa do fim da rua...`;
  - chama `buildStarterRoadEndHouse` antes de marcar `ESTATE_CREATED_KEY` e antes do progresso `100%`.
- `repairExistingEstate` tambem chama `buildStarterRoadEndHouse` como fallback para saves ja existentes.
- A chamada indireta dentro de `buildImportedEstateFarms` foi removida para evitar duplicidade e dependencia de fazendas.
- A rotina prepara plateu/suporte, coloca o template e adiciona caminho ate a rua, postes, azaleias e animais decorativos nomeados.

Validacao atual:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- Ainda falta nesta entrega: `./gradlew.bat build --stacktrace`, `git diff --check`, commit e push.
- Cliente nao foi aberto; nao executar `runClient`.

## Correcao visual do menu antes do commit - 2026-06-06 14:48:13 -03:00

Bug reportado:

- Ao abrir o cliente, o menu principal apareceu com blocos preto/magenta de textura ausente.
- O log `run/logs/latest.log` apontou `FileNotFoundException` para:
  - `magicworld:textures/gui/title/title_background_static.png`;
  - `magicworld:textures/gui/title/logo_full.png`.

Implementado:

- `MagicWorldStaticBackground` agora usa os assets que existem no pacote atual:
  - `magicworld:textures/gui/title_background_static.png`;
  - `magicworld:textures/gui/title_logo.png`.
- As dimensoes da logo foram ajustadas para `512x171`, compatÃƒÆ’Ã‚Â­veis com `title_logo.png`.
- Esta correcao evita o fallback preto/magenta antes do commit online.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- Cliente nao foi aberto pelo Codex; o usuario testa o cliente.

## Revisao do dropdown de seeds - 2026-06-06 15:42:01 -03:00

Pedido atual:

- Revisar a ultima alteracao para nao deixar erro.

Implementado:

- Identificado risco de a lista com 10 seeds sair da tela em resolucoes menores quando abrisse sempre para baixo.
- `MagicSeedDropdown` agora calcula o topo da lista dinamicamente: abre para baixo quando couber e abre para cima quando nao couber.
- Mantido o tratamento de clique em `ScreenEvent.MouseButtonPressed.Pre`, consumindo cliques do dropdown antes dos botoes por baixo.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- Cliente nao foi aberto pelo Codex; o usuario testa o cliente.

## Ajuste da casa grande, cerejeiras e casas dos animais - 2026-06-06 14:58:27 -03:00

Pedido atual:

- O usuario esclareceu que a casa grande vazia indicada nos prints precisa receber o mesmo pacote premium que foi criado no centro perto dos animais.
- Manter a casa premium existente perto dos animais como esta.
- Adicionar duas portas no lado voltado para as fazendas/currais.
- Coordenadas lidas do print atual: jogador em `55.092 / 74 / -34.441`, bloco alvo `55 73 -29`.
- Novo print mostrou as casas em frente ao ponto `56 / 74 / -11` um bloco abaixo do nivel do chao.

Implementado:

- Confirmado pelo calculo da base atual `(-60,74,30)` que o centro `55 74 -35` cai dentro de `premiumAnimalWorkCenterCorner(base).offset(9,1,7)`.
- O volume premium existente e `X 46..64`, `Y 73+`, `Z -42..-28`, com centro em `55 / 74 / -35`.
- `CURRENT_ESTATE_REPAIR_VERSION` elevado para `17`, para forcar o reparo no save ja criado.
- A rotina `buildPremiumAnimalWorkCenter` continua reconstruindo e mobiliando esse volume com armazenamento, baus premium, varinhas, estacoes de trabalho, mesa, camas, armaduras, luzes e aldeoes.
- A porta leste central foi mantida na parede de armazenamento.
- O lado oeste agora tem duas portas, em `z = depth/2 - 3` e `z = depth/2 + 3`, ambas com caminho curto para o lado das fazendas/currais.
- O usuario pediu para manter as cerejeiras externas ja convertidas e tambem converter as arvores verdes que ficaram no entorno da casa importada.
- `convertImportedHouseExteriorTreesToCherry` agora cobre uma faixa maior ao redor da casa (`64` blocos para fora do footprint) e nao pula mais a zona de rua/muro; como a rotina so troca troncos/folhas naturais, ruas, muros e blocos solidos da casa continuam preservados.
- As casas dos animais/centro premium em frente a `56 74 -11` foram elevadas de `base.offset(106,-1,...)` para `base.offset(106,0,...)`.
- `assignAnimalCaretakersToPens` e `buildWorkerSettlement` foram alinhados para usar o mesmo nivel novo, evitando aldeoes ou referencias no nivel antigo.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- Cliente nao foi aberto pelo Codex.

## Retomada e auditoria do port - 2026-06-06 15:52:33 -03:00

Pedido atual:

- Ler handoff e wiki para retomar o contexto do projeto.
- Conferir ultimas alteracoes para prosseguir.
- Comparar com o projeto NeoForge e identificar se falta portar algo para esta base Forge.

Estado encontrado:

- Branch atual: `Inicio-Port-Neoforge`, alinhado com `origin/Inicio-Port-Neoforge`.
- Worktree estava limpo antes da atualizacao desta documentacao.
- Ultimo commit: `6a0a554` (`Revisa dropdown de seeds do Magic World - 2026-06-06 15:42:01 -03:00`).
- Ultimas entregas reais, em ordem do Git:
  - `6a0a554`: revisao do dropdown de seeds para abrir para baixo/cima conforme espaco;
  - `9b47857`: campo `Seed manual`, dropdown de seeds e `docs/SEEDS_MAGIC_WORLD.md`;
  - `c4eaaa4`: Santuario magico no fim da rua, loading em `97%` e reparo versao `18`;
  - `dcc83be`: casa NBT no fim da rua durante loading e correcao dos paths de textura do menu.

Auditoria NeoForge -> Forge:

- Contagem Java: Forge atual tem 98 arquivos `.java`; NeoForge tambem tem 98.
- Diferencas principais sao adaptacoes, nao copia pendente direta:
  - mixins Sodium/Iris do NeoForge foram substituidos por mixins Embeddium/Oculus/Distant Horizons compativeis com Forge 1.20.1;
  - `MagicWorldLoadingOverlayMixin` do NeoForge era vazio/intencional, enquanto o Forge usa `MagicWorldLevelLoadingScreenMixin`;
  - `MagicWorldStaticBackground`, `MagicWorldScreenBackgrounds` e mixins especificos de criacao/selecao de mundo sao adaptacoes Forge 1.20.1.
- Recursos NBT aparecem como `data/magicworld/structure/*.nbt` no NeoForge, mas no Forge 1.20.1 estao corretamente em `data/magicworld/structures/*.nbt`.
- `dh_forge_icon.png` e `dh_forge_logo.png` do NeoForge nao aparecem referenciados no codigo Forge; o Forge atual usa assets em `assets/distanthorizons/` e `assets/magicworld/textures/gui/embeddium_magicworld_icon.png`.

O que ainda falta portar do NeoForge, se o escopo for ampliado:

- `PremiumPortalOptionsScreen` e `MagicWorldPortalVisualController`: fluxo premium de portal/resource pack/shader. Fora do escopo atual porque envolve resource/shader installers e compat visual.
- `MagicWorldEntityCulling` e `MagicWorldEntityCullingMixin`: portados no bloco de 2026-06-06 16:01:54 -03:00.
- `PeacefulDragon`, `PremiumEntityTags` e `StarterDragonManager`: dragao/entidade custom e gameplay. Fora do escopo atual.
- `RenderSetupSamplerCompatMixin` e `IrisConfigMagicWorldMixin`: compatibilidade Iris/render moderna do NeoForge; nao ha port direto seguro para Forge 1.20.1/Oculus sem bloco dedicado.
- `MagicWorldClient`: entrada client NeoForge especifica; nao e necessario portar como arquivo equivalente porque o Forge atual registra eventos/configuracoes por outro caminho.

Conclusao:

- Dentro do escopo autorizado atual (inicio do jogo, menus iniciais, criacao de mundo, backgrounds e loading), nao encontrei uma classe NeoForge critica ainda pendente.
- As pendencias reais restantes sao de escopo maior: portal visual/resource/shader, culling/performance e entidades/gameplay custom.

Validacao executada nesta retomada:

- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros.
- Cliente nao foi aberto pelo Codex.

Proximo passo recomendado:

- Se continuar no escopo atual: usuario deve testar visualmente no cliente a tela principal, criacao de mundo, dropdown de seeds, loading, casa do fim da rua e Santuario.
- Se abrir novo escopo de port: escolher um bloco isolado entre `portal visual/resource/shader`, `entidades/dragao`, ou `culling/performance`, sempre com downgrade Forge 1.20.1 manual e validacao Gradle.

## Correcao dropdown seeds e port Entity Culling - 2026-06-06 16:01:54 -03:00

Pedido atual:

- Corrigir a sobreposicao do menu mostrada no print.
- Trazer `MagicWorldEntityCulling` funcional para Forge.
- Explicar os demais arquivos faltantes do NeoForge e sua necessidade.

Implementado:

- `MagicSeedDropdown` deixou de abrir a lista diretamente sobre o painel.
- A lista de seeds agora abre como popup central opaco com overlay escuro, titulo, hint para clicar fora e selecao por linha.
- Em telas normais o popup mostra todas as seeds; em telas baixas limita a quantidade de linhas e usa `mouseScrolled` para rolagem.
- O clique fora do popup fecha a lista e continua impedindo clique acidental em `Criar Mundo`/`Voltar`.
- Portado `src/main/java/com/magicworld/client/MagicWorldEntityCulling.java`.
- Portado `src/main/java/com/magicworld/mixin/MagicWorldEntityCullingMixin.java`.
- Registrado `MagicWorldEntityCullingMixin` em `src/main/resources/magicworld.mixins.json`.
- A dependencia direta do NeoForge em `PeacefulDragon` foi removida do culling porque `PeacefulDragon` ainda nao existe no Forge atual.

Como o Entity Culling funciona:

- Mantem cache por entidade, celula de camera e celula da entidade.
- Nunca culla o player, a entidade da camera, entidades montadas ou entidades com passageiros.
- Mantem entidades proximas sempre visiveis.
- Culla entidades pequenas muito distantes.
- Para entidades mais distantes, faz raycast para centro/topo/pes e pontos laterais em entidades grandes; se todos estiverem bloqueados, pula a renderizacao.
- Pode ser desligado com `-Dmagicworld.entity_culling=false`.

Faltantes NeoForge apos este port e avaliacao:

- `MagicWorldPortalVisualController`: aplica resource packs premium e shader Iris/Oculus, escreve configuracao de shader e recarrega packs. Necessidade: alta somente se o projeto for reativar portal premium/resource/shader; caso contrario manter fora para evitar instabilidade e recarregamentos inesperados.
- `PremiumPortalOptionsScreen`: tela cliente para escolher ResourcePack, ShaderPack ou pacote completo ao usar portal premium. Necessidade: depende do item acima; sem o controller/rede do portal premium ela nao agrega valor.
- `PeacefulDragon`: dragao pacifico customizado baseado no Ender Dragon, com rota ao redor da propriedade. Necessidade: baixa nesta fase; exige registro de entidade, renderer/teste de IA e pode pesar FPS/loading.
- `StarterDragonManager`: spawn/limpeza do `PeacefulDragon`. Necessidade: baixa enquanto o dragao estiver fora.
- `PremiumEntityTags`: helper simples para marcar animais premium por tag. Necessidade: baixa/media; so vale portar se menus/sistemas de animais premium usarem essas tags de novo.
- `MagicWorldClient`: bootstrap client NeoForge e tela de config NeoForge. Necessidade: baixa; o Forge atual ja registra eventos por outro caminho.
- `CubeMapMagicWorldDepthMixin`: altera FOV do panorama cubemap vanilla. Necessidade: baixa; o Forge atual usa background estatico, entao o panorama praticamente nao importa.
- `MagicWorldLoadingOverlayMixin`: mixin vazio no NeoForge. Necessidade: nenhuma neste momento.
- `RenderSetupSamplerCompatMixin`: compat de render moderno/Iris para samplers ausentes. Necessidade: baixa no Forge 1.20.1 atual; alvo `RenderSetup` e APIs usadas sao de versoes modernas e nao portam direto.
- `IrisConfigMagicWorldMixin`: renomeia/tema configuracoes Iris dentro do Sodium moderno. Necessidade: baixa no Forge atual; ja ha adaptacao para Embeddium/Oculus e os nomes/classes diferem.
- `SodiumConfigBuilderMagicWorldMixin`, `SodiumDonationButtonCompatMixin`, `SodiumExternalPageEntryMagicWorldMixin`, `SodiumVideoSettingsScreenMagicWorldLayoutMixin`: personalizacao Sodium/Iris do NeoForge. Necessidade: nenhuma como copia direta; foram substituidos por mixins Embeddium/Oculus especificos do Forge 1.20.1.

Validacao executada:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- Cliente nao foi aberto pelo Codex.

## Correcao casa e Santuario do fim da rua - 2026-06-06 16:10:00 -03:00

Pedido atual:

- A casa do fim da rua nao esta no local do print 1.
- O Santuario criado mais cedo nao esta no sentido da rua/caverna do print 2.
- Reposicionar/corrigir sem abrir o cliente pelo Codex.

Diagnostico:

- O save local `run/saves/Novo mundo` foi lido por NBT.
- Base real do save: `MagicWorldForgeStarterEstateBaseX/Y/Z = 0/105/0`.
- Posicao salva do jogador: aproximadamente `8.19 / 111.08 / -0.69`.
- Rotacao salva: `yaw ~ -95.99`, ou seja, olhando para oeste.
- A casa do fim da rua estava em area distante; sem carregar chunks explicitamente antes da colocacao, o NBT podia ficar parcialmente aplicado.
- O Santuario anterior estava em `base.offset(296,0,-86)`, lado leste/nordeste, contrario ao sentido oeste mostrado no print 2.

Implementado:

- `CURRENT_ESTATE_REPAIR_VERSION` elevado de `18` para `19`.
- Mensagem de reparo atualizada para `Magic World: casa e Santuario do fim da rua reposicionados e atualizados.`
- `buildStarterRoadEndHouse` agora chama `forceLoadStructureArea` antes de limpar volume/posicionar `starter_house_1.nbt`.
- `clearStructureVolume` agora limpa o conteudo de qualquer `Container` antes de trocar o bloco por ar, evitando drops de baus antigos no reparo.
- `roadEndMagicSanctuaryOrigin(base)` foi movido para `base.offset(-176,0,-8)`, no eixo oeste da estrada real.
- `buildRoadEndMagicSanctuary` agora:
  - carrega os chunks da estrada/Santuario antes de mexer na area;
  - estende a estrada do oeste a partir de `base.offset(-76,-1,0)` ate a entrada leste do Santuario;
  - constrÃƒÆ’Ã‚Â³i o Santuario depois da estrada, com o mesmo conteudo premium anterior.
- Novos helpers:
  - `forceLoadStructureArea`;
  - `forceLoadAreaBetween`.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- Cliente nao foi aberto pelo Codex.

Teste esperado pelo usuario:

- Entrar novamente no save existente para disparar o reparo versao `19`.
- Conferir a casa do fim da rua no ponto antigo do print 1, agora com NBT completo.
- Conferir o Santuario no sentido oeste da rua/caverna mostrado no print 2.

## Seeds no menu Magic World - 2026-06-06 15:30:59 -03:00

Pedido atual:

- Na tela de criacao de mundo, manter `Modo: Normal` abaixo de `Dificuldade`, no mesmo tamanho.
- Adicionar a esquerda um campo manual de seed e um dropdown de seeds predefinidas.
- A primeira opcao do dropdown deve ser nula: `Selecione a seed`.
- Se escolher preset, usar preset; se nao escolher preset e digitar seed manual, usar manual; se ambos vazios, deixar o Minecraft gerar seed aleatoria.
- Criar documento ensinando como adicionar mais seeds.

Implementado:

- `MagicWorldWorldOptions` ganhou estado para `customSeed` e `presetSeedIndex`.
- `ClientEvents` aplica `WorldCreationUiState.setSeed(...)` com a prioridade preset > manual > aleatoria.
- A aba `Magic World` recebeu `EditBox` para `Seed manual`.
- Foi criado dropdown customizado com 10 seeds nomeadas mais a opcao nula.
- O dropdown e renderizado por ultimo no painel para a lista abrir por cima dos botoes.
- `Modo` fica na coluna direita, abaixo de `Dificuldade`, com a mesma largura.
- Criado `docs/SEEDS_MAGIC_WORLD.md` com instrucoes para adicionar/remover seeds.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- Cliente nao foi aberto pelo Codex; o usuario testa o cliente.

## Santuario magico do fim da rua - 2026-06-06 15:05:02 -03:00

Pedido atual:

- Dentro das coordenadas dos prints, no final da rua em frente a casa, transformar a caverna/espaco vazio em Santuario.
- O Santuario deve ter muita luz, decoracao, blocos coloridos, coisas premium, parede de baus com todos os itens do jogo, ferramentas, armaduras, quadros/decoracao, plantas, mesa central, sinos, brilhos, redstone, coelhos e passaros.
- Precisa entrar no sistema de loading.

Coordenadas lidas dos prints:

- Pontos de referencia: `236 75 -40`, `281 75 -56`, `237 75 -56`, `281 75 -39`.
- Volume implementado: aproximadamente `X 236..281`, `Y 74+`, `Z -56..-39`.
- Origem relativa: `roadEndMagicSanctuaryOrigin(base) = base.offset(296,0,-86)`.
- Tamanho usado: largura `45`, profundidade `17`, altura interna `10`.

Implementado:

- `CURRENT_ESTATE_REPAIR_VERSION` elevado para `18`.
- `handleEstateTask` ganhou etapa `6` em `97%`, mensagem `Carregando Santuario magico do fim da rua...`.
- O santuÃƒÆ’Ã‚Â¡rio e construido antes do `ESTATE_CREATED_KEY` e antes do progresso `100%`.
- `repairExistingEstate` chama `buildRoadEndMagicSanctuary` para saves ja existentes.
- O santuÃƒÆ’Ã‚Â¡rio tem shell proprio com piso colorido/iluminado, paredes decoradas, teto de calcite/amethyst, redstone blocks, redstone lamps, glowstone e sea lanterns.
- A parede leste e as laterais recebem baus/barris preenchidos por `fillContainersWithAllRegisteredItems`.
- Ha bau de varinhas, bau premium, bau de ferramentas, estacoes de trabalho, mesa central, sino, banners, paineis decorativos, plantas, armor stands para couro/malha/ferro/ouro/diamante/netherite/Draconic Aether, allays, parrots e rabbits.

Validacao:

- `./gradlew.bat compileJava --stacktrace`: BUILD SUCCESSFUL.
- `./gradlew.bat build --stacktrace`: BUILD SUCCESSFUL.
- `git diff --check`: sem erros; apenas avisos esperados de CRLF no Windows.
- Cliente nao foi aberto pelo Codex.

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

## Handoff 2026-06-06 - commit de preservacao solicitado
- Branch: Inicio-Port-Neoforge.
- Usuario pediu commit/push imediato para nao perder versionamento antes de continuar testes.
- Alteracoes incluidas: seed dropdown sem sobreposicao; EntityCulling portado; defaults Criativo/Facil; loading com logo menor e painel mais transparente; casa do fim da rua reposicionada e sem gramado extra; Santuario rebaixado e com porta voltada para a casa; entradas da casa grande perto dos currais desobstruidas; cerejeiras restritas ao espaco do jogador/castelo.
- Observacao: compileJava foi iniciado e interrompido pelo usuario nesta rodada; proxima sessao deve rodar ./gradlew.bat compileJava --stacktrace e depois ./gradlew.bat build --stacktrace.

## Handoff 2026-06-06 - ajuste pos-prints casa/Santuario
- Usuario reportou aviso Can't keep up durante geracao; provavelmente pico de geracao inicial. Foi reduzido o range vertical de conversao das arvores para aliviar custo, mas a geracao grande ainda pode causar aviso pontual.
- Casa do fim da rua: StarterPortalEvents.starterRoadEndHouseOrigin agora base.offset(-5, -4, -90). Template starter_house_1 usa Rotation.CLOCKWISE_180 com pivot central. Porta/decoracao recalculada por size para coordenada rotacionada.
- Santuario: roadEndMagicSanctuaryOrigin agora base.offset(-54, -4, -8), width 36, entrada leste em x ~= -18 relativa ao base, voltada para a casa.
- Cerejeiras: convertMainHousePerimeterTreesToCherry cobre todo o terreno cercado sem excluir o footprint amplo da casa; scan vertical reduzido para surfaceY -10 ate surfaceY +34.
- Validacao executada: ./gradlew.bat compileJava --stacktrace = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - overlay menu criacao
- Corrigida sobreposicao visual no painel Magic World da tela Criar Mundo.
- MagicCreateWorldLineCover agora cobre a tela inteira quando o painel esta aberto; MagicCreateWorldBackdrop ficou mais opaco.
- MagicCreateWorldInfo foi reduzido para duas linhas curtas para nao invadir os botoes em escala GUI grande.
- Validacao executada: ./gradlew.bat compileJava --stacktrace = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - casa fim da rua +5Y +16Z
- Ajuste solicitado por prints: casa do fim da rua precisava subir 5 blocos, avancar 16 blocos para mais perto da rua e remover pedras sobrepostas que nao eram da casa.
- StarterPortalEvents.starterRoadEndHouseOrigin agora base.offset(-5, 1, -74).
- clearStructureVolume da casa do fim da rua usa margem 3.
- decorateStarterRoadEndHouseFront nao usa mais buildHousePathToFarm/fillGroundAt; adicionada buildStarterRoadEndHouseEntrance para criar acesso direto no nivel da porta sem colocar smooth stone sobre o template.
- Validacao executada: ./gradlew.bat compileJava --stacktrace = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - casa das bruxas e casa fim da rua limpa
- Branch: Inicio-Port-Neoforge.
- Pedido implementado: casa de bruxas na mata do print, com ponto absoluto observado `447 103 -87` convertido para `witchCovenAnchor(base) = base.offset(507, 0, -117)` usando a base historica aproximada `-60 74 30` registrada nos prints anteriores.
- A geracao agora tem etapa `7` em `98%` (`Carregando casa das bruxas na mata...`) depois do Santuario em `97%`, evitando construir Santuario e casa das bruxas no mesmo tick.
- A casa das bruxas usa origem calculada a partir do portao: portao no ponto indicado, casa 34 blocos para oeste e 12 para norte, largura 28, profundidade 24, porta voltada para leste/entrada visivel.
- Conteudo: cerca, portao, placa `fiquem longe` / `daqui`, 3 quartos, mesa/cadeiras, canto de alquimia, caldeiroes, lareira/chamine, teias, morcegos, feno, jack o lanterns, cogumelos, iluminacao com soul lantern/lantern/glowstone.
- Baus: pocoes diversas, varinha Magic World, Totem, livros, XP bottles, ender pearls, blaze/ghast/spider ingredients, armaduras Draconic Aether/netherite/diamond, armas, escudo, flechas e foguetes.
- Bruxas: 3 entidades Witch marcadas com `MagicWorldFriendlyWitch`, persistentes, invulneraveis e NoAI para nao hostilizar; `handleWitchCovenSupport` aplica buffs ao jogador perto da casa e remove monstros comuns da area.
- Casa NBT do fim da rua: mantida em `starterRoadEndHouseOrigin(base) = base.offset(-5, 1, -74)` com `Rotation.CLOCKWISE_180`; removidos `decorateStarterRoadEndHouseFront` e `buildStarterRoadEndHouseEntrance`; suporte agora e apenas dirt invisivel, sem cobblestone/plataforma/caminho/postes/mobs por cima da estrutura.
- `repairExistingEstate` tambem chama `buildWitchCovenHouse` se o reparo existente for executado em save antigo; nao foi elevado `CURRENT_ESTATE_REPAIR_VERSION` nesta rodada para respeitar a regra do usuario de testar em mapas novos.
- Validacao executada: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL; `./gradlew.bat build --stacktrace` = BUILD SUCCESSFUL; `git diff --check` = sem erros.
- Cliente nao foi aberto pelo Codex.

## Handoff 2026-06-06 - casa das bruxas -1Y
- Usuario pediu pelo print: `desca a casa um bloco`.
- A casa afetada e a casa das bruxas recem-criada.
- Alteracao: `buildWitchCovenHouse` usa `floorY = heightmap - 2` em vez de `heightmap - 1`, baixando todo o conjunto em 1 bloco.
- Validacao parcial: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - reposicionamento casa das bruxas
- Usuario reportou que a casa das bruxas com as bruxas nao apareceu no local combinado.
- Imagens novas: jogador em `X 67 / Y 68 / Z -4`, alvo/mata em `X 58 / Y 68 / Z -5`.
- Causa: o offset anterior `base.offset(507,0,-117)` foi calculado com print antigo em `X 447 / Z -87` e ficou distante demais da area correta.
- Fix: `witchCovenAnchor(base)` agora retorna `base.offset(114,0,-35)`. Como o gate e construido em `anchor + 4X`, o portao fica aproximadamente no alvo `X 58 / Z -5` usando base historica `X -60 / Z 30`.
- Mantido ajuste de altura anterior: `floorY = heightmap - 2`.
- Validacao executada: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - casa das bruxas compacta 12x12
- Pedido: reduzir a casa das bruxas enorme para `12x12`, cercado `16x16`, mantendo tematica/itens/bruxas e usando a marcacao do print.
- Coordenada do print: usuario no centro em `Block 55 67 3`; alvo/marker visivel em `59 67 3`.
- `witchCovenAnchor(base)` agora e o centro da casa, `base.offset(115,0,-27)` com base historica `X -60 / Z 30`.
- `buildWitchCovenHouse` deixou de chamar o gerador grande e agora usa gerador compacto: casa `12x12`, cercado `16x16`, limpeza somente dentro do cercado.
- Footprint esperado no mapa de teste: casa `X 49..60 / Z -3..8`; cercado `X 47..62 / Z -5..10`.
- Conteudo compacto mantido: 3 camas, 3 witches amigaveis, baus de pocoes/magia/equipamentos, cauldron, brewing stand, enchanting table, lareira/chamine, teias, morcegos, feno, jack o lanterns, placa e luzes.
- Validacao parcial: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - premium/altura/icone DH
- Usuario perguntou por que aparecia `experiencia premium desativada`: causa era o toggle em `activatePremiumPortal`, que invertia `PREMIUM_UNLOCKED_KEY` a cada acionamento do portal.
- Fix: `activatePremiumPortal` agora e idempotente; seta `PREMIUM_UNLOCKED_KEY=true`, mostra mensagem de ativacao apenas se ainda nao estava ativo e nunca envia mensagem de desativacao.
- Casa das bruxas compacta: `floorY` ajustado para `heightmap - 2`, mantendo a casa 1 bloco mais baixa.
- Icone inferior esquerdo visto nas opcoes de video/shaders: `hideDistantHorizonsInjectedWidgets` agora esconde widgets por classe e por label relacionados a Distant Horizons/Horizontes Distantes.
- Validacao parcial: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - Distant Horizons continua usavel
- Usuario confirmou que quer Distant Horizons ativo/usavel nos menus.
- Regra ajustada: ocultar somente widgets pequenos ou sem texto relacionados a Distant Horizons; botoes grandes/textuais continuam visiveis e usaveis.
- O mod/compat continua ativo; a alteracao remove apenas o icone flutuante inferior esquerdo visto no print.
- Validacao: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - casa das bruxas rotacao e morro
- Pedido: virar a casa das bruxas 180 graus e mover 10 blocos mais proxima/adentrando o morro.
- `witchCovenAnchor(base)` agora retorna `base.offset(125,0,-27)`, 10 blocos a +X em relacao ao centro anterior.
- Fachada invertida: porta/portao compactos agora ficam no lado oeste (`Direction.WEST`), antes estavam no lado leste.
- Tamanho e altura mantidos: casa `12x12`, cercado `16x16`, `floorY = heightmap - 2`.
- Validacao: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - casa fim da rua -1Y e entrada mina
- Casa do fim da rua: `starterRoadEndHouseOrigin(base)` agora e `base.offset(-5,0,-74)`, baixando 1 bloco em relacao ao ajuste anterior.
- Mina: entrada no centro da casa da mina estava sendo fechada pelo reforco depois de a ladder ser criada.
- Fix: chamada `reopenTreasureMineEntrance(level, center)` apos `reinforceStoneTreasureMineHouseShell`; ela recoloca ladder no bloco central, remove o bloco acima e garante stone bricks de apoio ao sul para conectar com as ladders abaixo.
- Validacao: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL.

## Handoff 2026-06-06 - casa das bruxas no bloco apontado
- Pedido atual: mover a casa das bruxas exatamente para o ponto do print, mantendo a porta/fachada virada para onde ja estava.
- Print atual: jogador em `Block 37 70 124`; bloco apontado em `Targeted Block 37 69 124`.
- `witchCovenAnchor(base)` agora retorna `base.offset(97,0,94)`, mantendo o ponto apontado como centro da casa compacta/cercado quando a base historica e `X -60 / Z 30`.
- A porta e o portao continuam no lado oeste (`Direction.WEST`); nao foi alterada a rotacao/fachada.
- `buildWitchCovenHouse` agora usa `floorY = base.getY() - 1` em vez de `heightmap - 2`, fixando o piso no nivel do bloco apontado e evitando variacao por relevo, folhas ou arvores.
- Validacao: `./gradlew.bat compileJava --stacktrace` = BUILD SUCCESSFUL; `git diff --check` sem erros.

## Handoff 2026-06-06 - casa das bruxas ponto correto na mata
- Usuario reportou que nao havia casa no local esperado e enviou novo print.
- Coordenadas do print novo: jogador em `Block 69 74 -5`, alvo em `Targeted Block 69 73 -5`.
- Causa do erro anterior: a casa tinha sido movida para `X 37 / Z 124`, fora do ponto real conferido.
- Fix aplicado: `witchCovenAnchor(base)` agora retorna `base.offset(129,0,-35)`, posicionando centro da casa/cercado em `X 69 / Z -5` com base historica `X -60 / Z 30`.
- Altura segue fixa com `floorY = base.getY() - 1`, piso esperado em `Y 73`; porta/portao continuam no lado oeste.
- Validacao: `./gradlew.bat clean compileJava --stacktrace` = BUILD SUCCESSFUL; `git diff --check` sem erros.

## Handoff 2026-06-06 - casa das bruxas no miolo das arvores
- Usuario reportou que a casa das bruxas voltou para posicao antiga/errada e enviou print visual sem F3.
- Referencia definitiva deste ajuste: o X da mira no meio das arvores entre os currais/fazendas e a casa de pedra da mina.
- Save local do print: base real `MagicWorldForgeStarterEstateBaseX/Y/Z = -80/64/-352`; jogador em `-28.685 / 112.419 / -352.440`, rotacao `-95.101 / 67.350`.
- Calculado pelo save/visual: o centro da casa deve ficar no miolo da mata em torno de `base.offset(68,0,-2)`.
- Fix aplicado: `witchCovenAnchor(base)` mudou de `base.offset(129,0,-35)` para `base.offset(68,0,-2)`.
- Mantido: casa compacta `12x12`, cercado `16x16`, `floorY = base.getY() - 1`, porta/portao no lado oeste (`Direction.WEST`).
- Validacao: `./gradlew.bat clean compileJava --stacktrace` = BUILD SUCCESSFUL; `git diff --check` sem erros.

## Handoff 2026-06-06 - bruxas preservadas, efeitos invisiveis e spawn frontal
- Problema: a casa das bruxas aparecia, mas as bruxas sumiam. Causa encontrada: `Witch` herda de `Monster` e as rotinas globais de limpeza do terreno/aldeoes removiam todos os monstros, apagando tambem as bruxas amigaveis.
- Fix: limpezas de monstros agora preservam entidades com NBT `MagicWorldFriendlyWitch`; `spawnFriendlyWitch` passou a criar a entidade manualmente com `EntityType.WITCH.create`, raio de duplicidade pequeno e `addFreshEntityWithPassengers`, sem depender das checagens vanilla de spawn.
- Reparo automatico: `maintainEstateLife` e `handleWitchCovenSupport` chamam `ensureCompactWitchCovenResidents`, garantindo as 3 bruxas na casa compacta se o save estiver sem elas.
- Brilhos no jogador: efeitos de premium e suporte das bruxas agora usam `hiddenEffect(..., false, false, false)`, removendo particulas/icone visual sem tirar os poderes.
- Spawn: `findEstateSpawn` deixou de procurar piso interno e agora usa a estrada/frente da casa principal (`IMPORTED_HOUSE_MAX_Z + 3`) com heightmap de superficie e limpeza de dois blocos de ar; login tambem atualiza o respawn para esse ponto.
- Validacao: `./gradlew.bat clean compileJava --stacktrace` passou.

## Handoff 2026-06-06 - bruxas moveis e otimizacao de ticks
- Problemas reportados: as bruxas nao se moviam e o jogo perdeu muito FPS depois da casa das bruxas/novo spawn.
- Causa da imobilidade: `spawnFriendlyWitch` e `handleWitchCovenSupport` aplicavam `setNoAi(true)`.
- Fix de comportamento: bruxas amigaveis usam IA ativa, ficam restritas ao entorno da casa e o evento `LivingChangeTargetEvent` impede que adquiram alvo.
- Causa principal de peso recente: `ensureCompactWitchCovenResidents` chamava a populacao completa a cada suporte/manutencao, incluindo duas buscas de morcegos com AABB de `256` blocos.
- Fix de residentes: reparo periodico verifica apenas as tres bruxas; morcegos so sao criados durante a construcao.
- `maintainEstateLife` deixou de varrer/reconfigurar toda a propriedade e remover monstros em areas enormes a cada 10 segundos; agora executa apenas a verificacao pequena de reproducao dos currais, uma vez por minuto e perto da propriedade.
- Portais funcionais agora sao verificados a cada 10 ticks e somente perto da praca; reparo visual caiu para uma vez por minuto. O efeito ambiente usa a posicao conhecida do portal em vez de procurar blocos em um volume grande.
- `AuraEvents` deixou de enviar `onUpdateAbilities()` todo tick e atualiza sobrevivencia/efeitos a cada 10 ticks.
- Validacao: `./gradlew.bat compileJava --stacktrace` e `./gradlew.bat build --stacktrace` passaram; `git diff --check` sem erros.

## Handoff 2026-06-06 - reforma correta do Rancho da Plantacao
- Os novos prints identificaram definitivamente o predio alvo como o rancho/deposito central da plantacao, entre as casas dos agricultores e as quatro lavouras.
- Evidencias visuais: moradores `Morador do Rancho da Plantacao`, agricultores das casas 1/2 ao redor, salao quase vazio e ancora do codigo `base.offset(-119, -1, -76)`.
- A tentativa anterior de reformar a Casa Verde 2 foi removida do fluxo ativo; ela voltou para `buildSimplePlantationWorkerHouse` na ancora original `base.offset(-101, -1, 50)`.
- O rancho agora usa `buildGrandPlantationWarehouse`: paredes medievais de pedra/madeira, muitas janelas, quatro portas com acessos livres, telhado alto com dormers e chamine.
- Interior: arquivo de baus/barris dimensionado para conter todos os itens registrados, ferramentas, estacoes de trabalho/forja, camas, galeria de armaduras, quadros, plantas, teias, morcegos e iluminacao rustica.
- Subsolo: escada oculta por alcapoes e sala de tesouro dourada com baus de preciosidades, beacon, ovo do dragao, ender chest, armaduras e decoracao.
- Reparo da propriedade elevado para versao `21`, garantindo a reconstrucao unica no save existente ao entrar.
- Validacao: `./gradlew.bat build --stacktrace` passou; `git diff --check` sem erros.

## Handoff 2026-06-06 - spawn na Casa do Ultimo Farol, README e mods de desenvolvimento
- `CURRENT_ESTATE_REPAIR_VERSION = 22`; o spawn/respawn procura a cama da estrutura `starter_house_1` e posiciona o jogador com seguranca ao lado dela, usando o spawn frontal antigo apenas como fallback.
- O README foi reconstruido a partir do README amplo do projeto NeoForge e adaptado ao port Forge 1.20.1, preservando conteudo visual, ampliando a historia do Horizonte Partido e documentando Casa do Ultimo Farol, Santuario Violeta, Coven e Rancho do Cofre Dourado.
- O artefato atual documentado e `Magic_World_Mod_1.20.1-1.0.0.1.jar`; mods graficos continuam opcionais e separados.
- `run/mods/` e carregado diretamente pelo Forge para testes simples. `run/dev-mods/` e detectado automaticamente pelo `build.gradle` e remapeado pelo ForgeGradle para mods complexos.
- `run/mods-disabled/`, `run/dev-mods-disabled/` e `mods/` na raiz nao sao carregados pelo `runClient`.
- Validacao real: `runClient` abriu mundo com Distant Horizons, Embeddium e Oculus remapeados, sem o erro anterior de mixin.

## Handoff 2026-06-06 - casa correta reformada: Arquivo Medieval da Praca Verde
- Os novos prints identificaram definitivamente o alvo: nao era o Rancho da Plantacao, mas o grande `buildCommunityHall` central entre as plantacoes e as Casas Verdes.
- Confirmacao visual/posicional: interior quase vazio com lectern, cartography table e um bau; coordenadas aproximadas `X -105 / Z 18`; construcao em `center.offset(-8, 0, -28)`, equivalente a `base.offset(-114, -1, 6)`.
- `buildCommunityHall` agora usa `buildGrandMedievalArchiveHouse(level, corner, 18, 14)`, mantendo a casa exatamente no local dos prints.
- A casa recebeu quatro portas com acessos livres, paredes de pedra/madeira, muitas janelas, telhado medieval inclinado com dormers e chamine, iluminacao rustica, ferramentas, forja, armaduras, quadros, plantas, teias e morcegos.
- Uma parede virou arquivo de recipientes preenchido com todos os itens registrados. A escada oculta leva a uma camara subterranea dourada com baus de preciosidades.
- `CURRENT_ESTATE_REPAIR_VERSION = 23` aplica a reforma no save existente.
- Validacao: `compileJava` passou; `runClient` entrou no save e exibiu `Magic World: Arquivo Medieval da Praca Verde e estruturas iniciais atualizados.` sem excecao da construcao.

## Handoff 2026-06-06 - placas de identificacao no chao
- Pedido: colocar placas no chao em frente a casas, portais, predios e areas importantes para o usuario conseguir informar exatamente qual local quer alterar.
- `CURRENT_ESTATE_REPAIR_VERSION = 24`, aplicando as placas no save existente ao entrar.
- Foi criada `placeEstateIdentificationSigns`, chamada no reparo e no fim da geracao inicial, depois das estruturas para nao ser sobrescrita.
- As placas usam `DARK_OAK_SIGN`, texto duplicado na frente e no verso, e sao posicionadas fora do bloco de entrada para nao tampar portas.
- Locais cobertos: casa principal, portal inicial, praca de portais, Nether, End Portal, End Gateway, Casa do Ultimo Farol, Santuario Violeta, Coven das Tres Guardias, Mina do Tesouro, Rancho do Cofre Dourado, casas da plantacao, Arquivo Medieval da Praca Verde, Casas Verdes, Praca Verde, centro/casa dos animais, jardim de racao, seis currais, quatro plantacoes e castelo quando ativo.

## Handoff 2026-06-06 - nome do mundo, modal de seeds e reparo leve
- Tela de criacao Magic World recebeu campo `Nome do mundo` acima dos botoes; o valor sincroniza com `WorldCreationUiState.setName` antes de criar o mundo.
- Se o jogador abriu a aba Magic World depois de mexer no nome vanilla, o campo importa o nome atual da tela em vez de forcar um padrao fixo.
- Selecao de seed virou modal frontal: fundo preto opaco, painel central maior, render com z elevado e fechamento imediato ao selecionar uma seed.
- Reparo `24` agora e leve quando o save ja estava no `23`: aplica apenas `placeEstateIdentificationSigns`, sem reconstruir casas, baus e estruturas inteiras no login.
- Morcegos decorativos usam `spawnDecorativeBat`, que procura bolso de ar proximo e evita spawn dentro de parede; todos os antigos `spawnNamed(... EntityType.BAT ...)` foram substituidos.

## Handoff 2026-06-06 - foco definitivo em mapas novos
- Diretriz do usuario: nao gastar mais tempo com reparo de save existente; ele sempre cria um mapa novo para testar.
- Em pedidos futuros, priorizar a geracao limpa do novo mundo e so mexer em reparo versionado se for explicitamente solicitado.
- Santuario Violeta reposicionado usando o bloco exato do print como centro: `Targeted Block 201 110 13`; piso em `Y=110`.
- Entrada do Santuario invertida 180 graus para o lado oeste, com caminho/degraus externos para acesso quando houver desnivel.
- Casa NBT `starter_house_1` passou a escanear portas apos ser colocada e criar degraus externos automaticamente.
- Mesas centrais das casas grandes/areas de profissao foram substituidas por espaco central livre iluminado para evitar aldeoes presos.
- Todos os nomes customizados de entidades continuam como NBT, mas ficam invisiveis acima da cabeca (`setCustomNameVisible(false)`).
- Tela visual de progresso/avisos de carregamento foi desativada; mensagem final curta permanece no chat.

## Handoff 2026-06-06 - entrada do Santuario estendida
- Pedido: escada do Santuario com mais 10 degraus, plataforma reta 8x8 no fim, muita iluminacao e aves na entrada.
- `buildSanctuaryWestEntranceStairs` passou de 18 para 28 degraus, mantendo a descida para oeste.
- Foi adicionada `buildSanctuaryEntrancePlatform`, criando plataforma 8x8 de pedra no fim da escada com sea lanterns e end rods.
- A entrada recebeu 6 parrots nomeados com nome invisivel, usando o helper `spawnNamed` ja configurado para `setCustomNameVisible(false)`.

## Handoff 2026-06-07 - otimizacao forte para mapa novo
- Regra operacional reforcada: o alvo padrao e sempre mapa novo. Nao implementar nem religar reparo de save antigo sem pedido explicito.
- Login de mundo ja criado nao reconstroi propriedade, nao aplica placas, nao varre estruturas e nao envia mensagem de reparo. Ele apenas marca a versao atual para impedir fluxo legado.
- `repairExistingEstate` e helpers mortos de reparo/manutencao foram removidos do fluxo/codigo ativo.
- `StarterPortalEvents.onPlayerTick` agora sai cedo quando nao ha tarefa de geracao ou propriedade criada. Depois da geracao, o runtime fica limitado a portais, premium e efeitos raros.
- Manutencao periodica dos currais foi removida do runtime. Animais nascem em quantidade menor na geracao inicial.
- Suporte das bruxas nao varre mais monstros nem reconfigura witches por AABB; aplica apenas efeitos discretos quando o jogador esta perto do coven.
- Portais funcionais usam posicoes conhecidas no runtime e evitam recalcular superficie com busca por raio a cada checagem.
- Catalogo completo de itens fica limitado ao Arquivo Medieval da Praca Verde, preenchendo cada item registrado uma unica vez e usando cache da lista de itens. Santuario/Rancho usam poucos recipientes tematicos.
- Entidades decorativas foram reduzidas em Santuario, portal, rancho, praca verde e currais para priorizar FPS.
- Regra de performance futura: nenhum tick/login deve reconstruir propriedade, procurar entidades em AABB gigante, varrer volumes grandes, preencher baus em massa ou chamar `getChunk` fora da geracao inicial.

## Handoff 2026-06-07 - Santuario abaixo do castelo sem coordenada absoluta
- Pedido final antes de fechar o projeto: manter o Santuario no local da segunda imagem, abaixo do castelo, sem quebrar as construcoes do castelo nem reativar rotinas pesadas.
- `roadEndMagicSanctuaryOrigin(base)` nao usa mais `new BlockPos(201, 110, 13)` diretamente.
- A posicao agora e relativa ao castelo: centro em `castleOrigin(base).offset(-15, 43, -95)`, equivalente a `base.offset(25, 43, -115)`.
- Para o save de referencia com base `176 67 128`, o resultado continua no centro visual `201 110 13`.
- O volume do Santuario fica fora do footprint do castelo importado; entrada oeste, escada de 28 degraus, plataforma 8x8, conteudo e otimizacoes de entidades foram preservados.
- Reparo de save antigo continua desligado. Teste esperado: criar mapa novo e conferir visualmente o Santuario abaixo do castelo.

## Handoff 2026-06-07 - primeiro pacote all-in-one local
- Pedido: alimentar somente `pacote_distribuivel/.minecraft`, mantendo shaders/resource packs estaveis e atualizando futuramente apenas o JAR do mod.
- Foi criada/limpa a estrutura local:
  - `pacote_distribuivel/.minecraft/mods`;
  - `pacote_distribuivel/.minecraft/resourcepacks`;
  - `pacote_distribuivel/.minecraft/shaderpacks`.
- Copiado para `mods`: JAR atual do Magic World, Embeddium, Oculus e Distant Horizons validados.
- Copiado para `resourcepacks`: quatro ZIPs Magic World atuais.
- Copiado para `shaderpacks`: `MagicWorld_Shaders_Extreme_v1.0_.zip`.
- Regra local: nao gerar ZIP; o pacote de teste e a propria pasta `pacote_distribuivel/.minecraft`.
- Criado helper local: `pacote_distribuivel/atualizar_apenas_mod_all_in_one.ps1`, que troca somente o JAR do Magic World em `.minecraft/mods`.
- `pacote_distribuivel/` esta ignorado pelo Git, entao os binarios locais nao aparecem como arquivos rastreados.
- Entity Culling externo nao deve entrar nas pastas futuras: o Magic World ja tem culling interno via `MagicWorldEntityCulling` e `MagicWorldEntityCullingMixin`, registrado no mixin config. Para diagnostico, desligar com `-Dmagicworld.entity_culling=false`.
- Proximos mods a testar separadamente, se necessario: ETF, EMF, Fusion, Forge CIT, ModernFix e FerriteCore.

## Handoff 2026-06-07 - hotfix urgente loading, cama e Santuario
- Problema reportado: a geracao continuava visivel apos o loading, estruturas apareciam na frente do jogador, o spawn final nao caia ao lado da cama da Casa do Ultimo Farol e o Santuario ainda nao estava no ponto pedido.
- Causa principal do loading: `openInitialLoadNotice` e `sendInitialLoadProgress` estavam vazios por otimizacao anterior; a rede/tela existiam, mas nao eram chamadas.
- Fix: `StarterPortalEvents` voltou a chamar `MagicWorldNetwork.openInitialLoadNotice` e `MagicWorldNetwork.sendInitialLoadProgress`.
- `InitialLoadNoticeScreen.shouldCloseOnEsc()` agora retorna `false`, impedindo fechar a tela durante a geracao.
- `FINAL_DELAY_TICKS` subiu de `80` para `160`, mantendo a etapa final em loading por mais tempo antes de teleportar/liberar.
- Spawn: `findEstateSpawn` agora tenta a busca generica da cama e, se falhar, usa `forceRoadEndHouseBedsideSpawn`.
- A cama do `starter_house_1.nbt` foi identificada com pe local `[10,3,22]`; como a estrutura e colocada com rotacao 180, o fallback prepara um ponto seguro ao lado da cama rotacionada.
- Santuario: revertido o offset relativo ao castelo; agora usa centro absoluto do print F3 novo `Block 234 113 12`.
- Validado com `compileJava` e `build`.
- All-in-one local atualizado em `pacote_distribuivel/.minecraft/mods`.

## Handoff 2026-06-07 - branch inicio-all-in-one e pacote gameplay
- Pedido: criar branch para iniciar o all-in-one com os mods locais antigos do usuario e priorizar performance, sem incluir Controllable.
- Branch criado: `inicio-all-in-one` porque Git nao aceita espacos em nome de branch.
- A pasta `mods/` foi auditada por metadados dos JARs (`META-INF/mods.toml`) para confirmar Forge/1.20.1 e dependencias.
- `pacote_distribuivel/.minecraft/mods` agora contem o conjunto expandido para teste local.
- Incluidos para performance/render: Embeddium, Oculus, Distant Horizons, ModernFix `5.27.44`, FerriteCore, ImmediatelyFast e culling interno do Magic World.
- Incluidos para resource pack/visual: ETF, EMF, CTM, Fusion, CIT Resewn e BetterFoliage.
- Incluidos por uso antigo/gameplay do usuario: JourneyMap, MineColonies, MCA, REI, WorldEdit e Tectonic.
- Incluidas dependencias: Architectury, Cloth Config, BlockUI, Structurize, Domum Ornamentum, Lithostitched e Framework.
- Excluidos:
  - `entityculling-forge-1.10.2-mc1.20.1.jar`, pois o Magic World ja tem `MagicWorldEntityCulling`/Mixin;
  - `modernfix-forge-5.27.22+mc1.20.1.jar`, duplicado antigo;
  - `effortlessbuilding-1.20.1-3.11.jar`, pois falta `flywheel` `[1.0.0,2.0)` e `ponder` `[0.8,)`;
  - Controllable, por decisao do usuario.
- Proximo bloco se o pacote abrir: personalizar MineColonies com tema Magic World por assets/resource overrides e, se necessario, baixar/referenciar Flywheel/Ponder para liberar Effortless Building.

## Handoff 2026-06-07 - pes do Santuario e reparo da Casa do Ultimo Farol
- Pedido: manter o Santuario onde ficou aprovado, mas tirar a aparencia de estar voando; corrigir a Casa do Ultimo Farol porque uma escada apareceu dentro dela e destruiu parte do interior.
- Santuario: `buildRoadEndMagicSanctuary` agora chama `buildSanctuarySupportPiers` logo apos a shell.
- Foram adicionados oito pilares 2x2 sob o Santuario, ancorados nos cantos e no meio, descendo ate encontrar terreno solido ou ate 96 blocos abaixo.
- Materiais dos pilares: `POLISHED_DEEPSLATE`, `DEEPSLATE_BRICKS`, pontos de `AMETHYST_BLOCK` e `SEA_LANTERN`.
- Casa: removida a chamada `buildStarterRoadEndHouseDoorAccess` e removidos os helpers de escada automatica por porta.
- Motivo: a varredura de portas podia pegar porta interna da NBT rotacionada e abrir escada no meio da casa.
- Spawn junto da cama permanece ativo via `findRoadEndHouseBedsideSpawn` e fallback `forceRoadEndHouseBedsideSpawn`.
- Validado com `compileJava`, `build` e `git diff --check`.
- `pacote_distribuivel/.minecraft/mods` foi atualizado com o JAR novo.

## Handoff 2026-06-07 - limpeza leve de drops de geracao
- Pedido: ao gerar a casa onde o jogador spawna e o Santuario, limpar os itens soltos criados por arvores/blocos destruidos para eles nao ficarem coletaveis no chao.
- Implementado sem tick permanente: apenas chamadas pontuais durante a geracao e uma chamada final antes do teleporte.
- Novas rotinas: `removeLooseDroppedItemsAroundStarterRoadEndHouse`, `removeLooseDroppedItemsAroundRoadEndMagicSanctuary`, `removeInitialGenerationLooseDrops` e helper comum `removeLooseDroppedItems`.
- A casa importada inicial tambem chama `removeLooseDroppedItemsAroundImportedHouse` logo apos ser construida.
- A limpeza descarta somente `ItemEntity` em AABBs locais; nao altera blocos, containers, baus, mobs ou inventarios.
- Validado com `compileJava`, `build` e `git diff --check`.
- `pacote_distribuivel/.minecraft/mods` foi atualizado com o JAR novo.

## Handoff permanente - regra do pacote local all-in-one
- Para testes locais, usar somente `pacote_distribuivel/.minecraft`.
- Nao gerar ZIP dentro de `pacote_distribuivel`.
- Em `.minecraft/mods`, manter o JAR all-in-one sempre atualizado e apenas os mods externos que ainda nao couberem nele.
- `shaderpacks` e `resourcepacks` sao alimentados uma vez e so mudam quando houver atualizacao real.
- Nome fixo do JAR principal: `Magic_World_Mod_1.20.1-1.0.0.1.jar`.

## Handoff 2026-06-07 - renome do artefato principal
- Pedido: padronizar o artefato principal como `Magic_World_Mod_1.20.1-1.0.0.1.jar`.
- `build.gradle` agora usa `archivesName = 'Magic_World_Mod_1.20.1'`.
- `settings.gradle` agora usa `rootProject.name = 'Magic_World_Mod'`.
- O script `pacote_distribuivel/atualizar_apenas_mod_all_in_one.ps1` procura o novo JAR e remove o JAR antigo da pasta de mods durante a transicao.

## Handoff 2026-06-07 - hotfix criacao de mundo no launcher local
- Problemas no TLauncher: tela vanilla de criacao nao mostrava personalizacao suficiente e o botao do painel Magic World ficava parado ao tentar criar o mundo.
- Causa provavel: `createWorldFromMagicTab` chamava `CreateWorldScreen.onCreate()` por reflexao, caminho fragil em cliente real/remapeado.
- Correcao: `createWorldFromMagicTab` agora sincroniza as opcoes e chama o botao vanilla real `Criar novo mundo` salvo em `vanillaWidgets`.
- Foi adicionada faixa visual `MAGIC WORLD` em `ScreenEvent.Render.Post` quando a tela vanilla de criacao esta aberta e o painel Magic World nao esta visivel.
- Validado com `compileJava`, `build` e `git diff --check`.
- `pacote_distribuivel/.minecraft/mods/Magic_World_Mod_1.20.1-1.0.0.1.jar` foi atualizado sem gerar ZIP.

## Handoff 2026-06-07 - menu unico Magic World na criacao de mundo
- Pedido: esconder a experiencia vanilla fragmentada e deixar somente o menu Magic World com funcoes vanilla + funcoes proprias.
- `onScreenInit` agora chama `showMagicPanel(screen, true)` apos registrar os widgets.
- `updateMagicTabButton` nao força mais voltar para a aba Jogo; ele apenas esconde o botao Magic World quando o painel ja esta aberto.
- O painel foi compactado em 4 colunas e 5 linhas de botoes: Portal, Castelo, Fazendas, Aura, PC, Comandos, Modo, Dificuldade, Seed manual, Seed preset, Bau bonus, Estruturas, Regras, Data packs, Avancado, Criar Mundo e Cancelar.
- `Criar Mundo` continua chamando o botao vanilla real; `Regras` e `Data packs` chamam os botoes vanilla ocultos quando disponiveis.
- `Avancado` e fallback tecnico para mostrar a interface vanilla original se alguma funcao nao estiver acessivel pelo painel.

## Handoff 2026-06-07 - remocao da faixa informativa e compactacao
- Pedido: remover o quadro/texto abaixo do botao Magic World nas abas vanilla e fazer o painel caber na tela.
- Removida a chamada de `renderCreateWorldVanillaBranding`; a faixa `MAGIC WORLD / Use o botao...` nao e mais renderizada.
- `MagicCreateWorldTitle` e `MagicCreateWorldInfo` ficam invisiveis no painel para remover o texto introdutorio interno.
- Layout compactado: `MAGIC_PANEL_HEIGHT = 174`, `MAGIC_PANEL_BUTTON_HEIGHT = 18`, `MAGIC_PANEL_GAP = 5`, 4 colunas e 5 linhas.

## Handoff 2026-06-07 - hotfix portais funcionais da praca
- Pedido: corrigir portal vertical grande e demais portais da praca, evitando textura quebrada, retorno errado e queda no vazio.
- Causa principal: a praca era criada pela altura real do terreno, mas o runtime usava `compactPortalPlazaCenter(base)` cru para detectar/reparar/retornar.
- Correcao: o centro real da praca e salvo em `MagicWorldForgeFunctionalPortalPlazaX/Y/Z` quando a praca e criada.
- Runtime, reparo visual e retorno ao Overworld usam a posicao persistida; saves antigos tentam localizar o portal funcional ja existente e salvar essa posicao.
- Nether/End agora usam Y seguro e recriam a plataforma/portal de retorno antes do teleporte, evitando spawn no vazio.
- Validado com `./gradlew.bat build`.
- `pacote_distribuivel/.minecraft/mods/Magic_World_Mod_1.20.1-1.0.0.1.jar` foi atualizado sem gerar ZIP.

## Handoff 2026-06-07 - perfis de PC no menu de criacao
- Pedido: verificar se `PC: Ultra fraco/Fraco/Intermediario/Medio/Forte/Ultra forte` realmente funciona no menu de criacao.
- Diagnostico: no Forge, o botao `PC` so chamava `nextHardwareProfileIndex(...)` e atualizava o texto; nao aplicava `MagicWorldGraphicsProfile.apply(...)`.
- Correcao: o handler agora aplica o perfil selecionado ao clicar sem mostrar mensagem no chat ou overlay.
- `Criar Mundo` tambem chama `applySelectedHardwareProfile()` antes de acionar o botao vanilla, garantindo que o perfil padrao visivel tambem seja aplicado.
- O comportamento foi adaptado do NeoForge, ajustado para Forge 1.20.1 (`GraphicsStatus`/`ParticleStatus` atuais).
- Validado com `./gradlew.bat build`.

## Handoff 2026-06-07 - portal premium visual sem chat
- Pedido: ao atravessar o portal de resource/shader, abrir o menu especial Magic World para escolher `Shader`, `Resource` ou ambos; remover as mensagens que aparecem durante o jogo.
- Correcao: `StarterPortalEvents.activatePremiumPortal` agora chama `MagicWorldNetwork.openPremiumPortalOptions(player)`, nao a tela generica/antiga.
- Novo menu: `PremiumPortalOptionsScreen` com botoes `ResourcePack`, `ShaderPack`, `Shader + Resource` e `Cancelar`.
- Confirmacao: `ConfirmPremiumPortalOptionsPacket` grava a escolha no servidor e chama `applyPremiumPortalVisual`.
- Aplicacao client-side: `MagicWorldPortalVisualController` seleciona os resourcepacks MagicWorld e escreve/aplica `iris.properties` para Oculus/Iris.
- Mensagens removidas: ativacao premium, final do loading, perfil grafico e toast de menu nao escrevem mais no chat.
- Pacote local: atualizado somente `pacote_distribuivel/.minecraft/mods/Magic_World_Mod_1.20.1-1.0.0.1.jar`; nao foi gerado ZIP.
- Validado com `./gradlew.bat build`.

## Handoff 2026-06-07 - opcao remover efeitos do portal visual
- Pedido: no menu de shader/resource do portal, adicionar opcao para remover efeitos.
- `PremiumPortalOptionsScreen` agora tem `Remover efeitos`.
- A escolha envia `confirm(false, false, false)`.
- `StarterPortalEvents.confirmPremiumPortalOptions` trata ausencia de selecao como desativacao: flags premium visuais ficam falsas, night vision/luck do portal sao removidos e o cliente recebe `applyPremiumPortalVisual(false, false, false)`.
- Build validada e JAR all-in-one atualizado no pacote local, sem ZIP.

## Handoff 2026-06-07 - sincronizacao dos mods validados
- Pedido: usar os mods testados no Minecraft pessoal e colocados em `run/mods` para montar o pacote local all-in-one.
- Regra aplicada: `run/mods` e a fonte validada; `tl_skin_cape*` fica fora por decisao do usuario.
- Tambem foi ignorado qualquer `Magic_World_Mod_1.20.1-*.jar` dentro de `run/mods`, porque o JAR correto vem de `build/libs` pelo script oficial.
- Removidos do pacote distribuivel por nao estarem na lista validada atual: `RoughlyEnoughItems-12.1.785-forge.jar` e `worldedit-mod-7.2.15.jar`.
- Mantidos no pacote: Architectury, BetterFoliage, BlockUI, CIT Resewn, Cloth Config, CTM, Distant Horizons, Domum Ornamentum, Embeddium, EMF, ETF, FerriteCore, Framework, Fusion, ImmediatelyFast, JourneyMap, Lithostitched, MineColonies, MCA, ModernFix, Oculus, Structurize e Tectonic.
- `resourcepacks` e `shaderpacks` nao foram alterados; nao foi gerado ZIP.
- Pendente apos APROVADO: atualizar `README.md` com explicacao de uso dos mods usaveis, incluindo JourneyMap, MineColonies, Distant Horizons, shader/resource portal, mods de construcao/visual e como ativar cada um.

## Handoff 2026-06-07 - integracao opcional MineColonies
- Branch de trabalho: `integracao-com-mods`.
- Pedido: integrar MineColonies ao Magic World antes do installer, com menu de teleporte e visual proprio.
- Implementado `MagicWorldMineColoniesIntegration` sem dependencia direta de classes MineColonies.
- Registro automatico: clique direito ou colocacao de blocos `minecolonies:blockhuttownhall`, outros `blockhut*`, `supplycamp`, `supplychest`, `decorationcontroller`, `colonysign` e `blockwaypoint`.
- Dados salvos por jogador: ultima colonia, ultimo Town Hall e ultima construcao MineColonies.
- Menu: nova aba `Colonias` no `PremiumMenuScreen`, aberta tambem pelo centro `Sistema`.
- Acoes: voltar para casa, registrar colonia atual, ir para ultima colonia, ir para Town Hall e ir para ultima construcao.
- Acoes usam `PANEL_ACTION:*` e `MagicWorldPanelActionPacket`, nao comandos vanilla.
- Teleporte: server-side, por dimensao salva, procurando coluna segura ao redor do alvo.
- Visual: overrides em `assets/minecolonies/lang/en_us.json`, `assets/minecolonies/lang/pt_br.json`, `assets/minecolonies/gui/townhall/windowtownhall.xml`, `assets/minecolonies/gui/map/windowcolonymap.xml` e `assets/minecolonies/gui/windowbuildbuilding.xml`.
- Validado com `./gradlew.bat build`; JAR all-in-one atualizado no pacote local sem ZIP.

## Handoff 2026-06-07 - rollback skin completa MineColonies
- A skin completa aplicada em `08ac9ad` bugou o jogo no teste do usuario.
- Revertido o pacote amplo de 94 XMLs/93 texturas para voltar ao estado estavel anterior.
- Mantida a integracao funcional MineColonies: aba `Colonias`, registro automatico de colonia/Town Hall/construcao e teleportes seguros.
- Mantida apenas a personalizacao inicial segura de idioma, Town Hall, mapa e construcao.
- Regra fixa do usuario: nao criar scripts/rotinas que carreguem ou varram saves antigos; isso so pode acontecer com pedido explicito.
- Proximo caminho: personalizar por lotes pequenos com base em prints/texto, sem rotina retroativa de saves.

## Handoff 2026-06-07 - hotfix crash EMF
- Erro recebido do usuario: `entity_model_features.mixins.json:MixinModelPart` falhou durante APPLY.
- Diagnostico: crash vem do mod externo EMF, nao do JAR principal Magic World.
- Movidos para `mods_desativados_magicworld` no pacote e no Minecraft local: `entity_model_features`, `entity_texture_features`, `fusion`, `citresewn`, `modernfix` e `ferritecore`.
- Tambem foram removidos de `run/dev-mods` e da pasta antiga `mods/`; `run/dev-mods` e carregavel pelo Gradle via `build.gradle`.
- Esses JARs nao devem retornar para nenhuma pasta carregavel sem teste isolado um por vez.
- `entityculling` externo continua proibido no pacote porque o Magic World ja possui culling interno proprio.

## Handoff 2026-06-07 - pacote abriu com alerta
- Usuario confirmou que o jogo abriu apos remover EMF/ETF e correlatos das pastas carregaveis.
- Build executada novamente e JAR `Magic_World_Mod_1.20.1-1.0.0.1.jar` atualizado no pacote local e no `.minecraft` pessoal.
- Alerta obrigatorio: este commit e experimental e pode nao funcionar se a instalacao usada ainda carregar EMF/ETF/Fusion/CIT/ModernFix/FerriteCore por outro caminho.
- Proximo diagnostico deve sempre conferir horario do `latest.log`; erro antigo de EMF antes da limpeza nao deve ser tratado como crash novo.

## Handoff 2026-06-07 - GUI MCA com paleta Magic World
- Pedido: usuario ja alterou a logo dentro do JAR `minecraft-comes-alive-7.6.16+1.20.1-universal.jar` e quer fundo/botoes/telas GUI com cores Magic World.
- A copia modificada do MCA em `.minecraft/mods` foi copiada para `pacote_distribuivel/.minecraft/mods`, preservando a logo alterada no pacote local.
- Implementado override pelo nosso mod, sem editar o JAR MCA: `assets/mca/textures/gui.png` e `assets/mca/textures/gui/books/*.png`.
- Nao sobrescrever `mca.png` nem `banner.png` no nosso mod; manter esses arquivos controlados pela copia editada do MCA.

## Handoff 2026-06-07 - locais seguros e protecao de teleporte externo
- Pedido: jogador nasce certo, mas depois e movido para ponto anterior de MCA/MineColonies; precisa de teleporte, explicacao e apoio do JourneyMap.
- Implementado `MagicWorldLocationManager`: salva casa, santuario, praca de portais, castelo, marcador manual, ultimo teleporte externo, ultima colonia, Town Hall e ultima construcao.
- Implementado `MagicWorldTeleportGuard`: nos primeiros 30 segundos apos login/final da geracao, teleporte grande nao autorizado e tratado como externo, salvo como waypoint e revertido para casa.
- Menus: nova aba `Locais` no menu da varinha `H`; botao `Locais Magic World` e atalhos rapidos tambem no menu MagicWorld do `Esc`.
- MineColonies: registros antigos sao migrados de NBT legado quando usados; novos cliques/colocacoes ja alimentam os locais oficiais.
- JourneyMap: `MagicWorldJourneyMapWaypoints` escreve waypoints oficiais locais quando JourneyMap esta instalado; nao apagar cache/tiles automaticamente.
- Pacote: atualizado somente `pacote_distribuivel/.minecraft/mods/Magic_World_Mod_1.20.1-1.0.0.1.jar`; sem ZIP.
- Validado com `./gradlew.bat build`.

## Handoff 2026-06-07 - Screen Overlays e portal visual dinamico
- Pedido: separar tudo que e GUI/telas/botoes do resource pack em pacote proprio chamado `Screen Overlays`.
- Criado `resourcepacks/Screen Overlays` com `pack.mcmeta`, `pack.png` vindo de `screenshots/icone_screenoverlays.png` e assets GUI extraidos do pack 256x.
- Copias criadas para teste local em `run/resourcepacks/Screen Overlays` e `pacote_distribuivel/.minecraft/resourcepacks/Screen Overlays`.
- Ordem aplicada no codigo: base vanilla/mods, packs Magic World (`bonus`, `addon`, `models`, `256x`), packs extras escolhidos e `Screen Overlays` sempre por ultimo/maior prioridade.
- `PremiumPortalOptionsScreen` deixou de usar checkboxes simples; agora abre pop-ups para selecionar resourcepacks e shaderpacks detectados nas pastas locais.
- `MagicWorldPortalVisualController` lista dinamicamente `.zip` e pastas de `resourcepacks`/`shaderpacks`.

## Handoff 2026-06-07 - locais premium e portais pelo centro
- Pedido: `Locais Magic World` nao deve abrir a tela da varinha `H`; precisa de tela propria premium, coordenada manual e foco em MineColonies/JourneyMap.
- Implementado `MagicWorldPremiumLocationsScreen` em `src/main/java/com/magicworld/client/`, com cards, icones, secoes de destino e campo `X Y Z`.
- `MagicWorldCentralPauseScreen` agora abre essa tela propria, remove os textos centrais abaixo da logo e inicia botoes mais acima.
- `MagicWorldLocationManager` aceita `location_teleport_manual_coords:x:y:z`, salva como marcador manual e teleporta com seguranca na dimensao atual.
- `MagicWorldScreenBackgrounds` inclui telas externas MCA/MineColonies; `ClientEvents` repinta widgets externos com botoes Magic World.
- `StarterPortalEvents` removeu gatilho por raio dos portais: praca funcional, retornos e portal premium exigem alinhamento no centro do portal.
- README atualizado com uso da tela, MineColonies, JourneyMap, coordenadas manuais e regra dos portais pelo centro.

## Handoff 2026-06-07 - ajustes finais antes do installer Forge
- Ajustado `MagicWorldCentralPauseScreen`: mais botoes em duas colunas, inclui `Castelo` e reduz espacamento para nao cortar botoes.
- Ajustado `MagicWorldPremiumLocationsScreen`: rodape nao cobre coordenadas e cards; acoes sem fechamento mostram status na tela.
- `location_update_waypoints` agora mostra retorno em chat/actionbar.
- `MagicWorldJourneyMapWaypoints` limpa duplicados legados do proprio Magic World e atualiza `journeymap.waypoint.config` para manter waypoints no mapa/lista sem beacon/linha/nome/distancia 3D.
- Atualizado JAR local em `pacote_distribuivel/.minecraft/mods/Magic_World_Mod_1.20.1-1.0.0.1.jar`; sem ZIP.
- Validado com `./gradlew.bat build`.

## Handoff 2026-06-07 - instalador Forge
- Adicionados `scripts/install-magicworld-forge-tlauncher.ps1`, `scripts/MagicWorldForgeInstallerLauncher.cs` e `scripts/build-magicworld-forge-installer.ps1`.
- Instalador local gerado em `installer/MagicWorldInstaller.exe`; `installer/` segue ignorado pelo Git.
- Estado anterior: o EXE embutia script e Forge installer, mas nao o pacote completo.
- Validacao executada: `install-magicworld-forge-tlauncher.ps1 -SkipForgeInstall` copiou 18 mods, resources, shaderpacks e JourneyMap para `tmp/installer-test/.minecraft`; build do EXE passou.

## Handoff 2026-06-07 - menu central fechado antes dos releases
- Usuario pediu parar releases e fechar mod/installer antes.
- Release parcial `mods-pack-forge-1.20.1-v1.0.0.1` foi removido para nao deixar pacote antigo online.
- `MagicWorldCentralPauseScreen` agora usa somente duas colunas, 8 linhas e calculo dinamico de altura/espacamento.
- Labels encurtadas para caber em escala alta: `Menu secreto`, `Menu varinha`, `Locais Magic`.
- Build passou, JAR distribuivel atualizado e installer local recompilado.

## Handoff 2026-06-07 - installer FULL standalone
- Pedido atualizado: installer deve ser um EXE grande com tudo dentro para instalar facil em TLauncher com Minecraft 1.20.1 ja aberto uma vez.
- `MagicWorldForgeInstallerLauncher.cs` agora detecta payload ZIP anexado ao fim do proprio EXE via marcador `MAGICWORLD_FULL_PAYLOAD_V1`.
- `build-magicworld-forge-installer.ps1` gera FULL por padrao: cria ZIP interno com `payload/.minecraft` e `payload/forge/forge-1.20.1-47.4.10-installer.jar`, depois anexa ao EXE.
- `install-magicworld-forge-tlauncher.ps1` copia mods, resourcepacks, shaderpacks, JourneyMap, config/defaultconfigs/options opcionais, remove conflitos conhecidos e aplica config JourneyMap/Oculus.
- `-NoFullPayload` ainda gera a versao leve antiga se necessario.
- EXE local FULL gerado em `installer/MagicWorldInstaller.exe`; tamanho aproximado 1.78 GiB / 1.91 GB.
- Validacao: marcador do payload OK, entries do JAR principal, shader e Forge installer presentes no ZIP interno.

## Handoff 2026-06-08 - launcher FULL Stable V1.0.0.2
- Pedido do usuario: deixar launcher premium, sem terminal cmd/PowerShell visivel, com icones em atalhos/pastas/janelas, progresso real em porcentagem, configuracoes separadas, RAM em slider ate 16 GB, launcher oculto durante o Minecraft e release local `Stable V1.0.0.2`.
- Pastas exclusivas confirmadas e documentadas: instalacao em `%LOCALAPPDATA%\MagicWorldLauncher`; Minecraft interno em `%APPDATA%\MagicWorldLauncher\.minecraft`; contas/configuracoes em `%APPDATA%\MagicWorldLauncher`.
- Isso permite instalar TLauncher e outros launchers em paralelo sem misturar mods, assets, versoes ou saves do Magic World.
- `MagicWorldLauncher.ps1`: tela principal remodelada com rodape tipo launcher, sem botoes `Instalar`/`Repositorio`; adicionados botoes `Login`, `.minecraft`, `Configuracoes` e `Jogar Magic World`.
- `MagicWorldLauncher.ps1`: RAM/resolucao foram movidas para janela `Configuracoes`; RAM usa slider 2-16 GB; opcao de ocultar launcher durante o jogo fica nessa tela.
- `MagicWorldLauncher.ps1`: `Start-MagicWorldMinecraft` retorna o processo Java; botao `Jogar` oculta a janela enquanto o processo roda e mostra novamente quando fechar.
- `MagicWorldLauncher.ps1`: `Update-Status` escreve `PROGRESS:<percent>:<texto>` em modo `-InstallOnly` para o instalador FULL acompanhar porcentagem.
- `MagicWorldLauncher.ps1`: botao `Servidores` cria/edita favoritos em `%APPDATA%\MagicWorldLauncher\servidores.json` e gera `servers.dat` na `.minecraft` exclusiva via NBT simples.
- Login TLauncher: tela pede usuario/senha e caixa `Salvar senha`; online depende de `MAGICWORLD_TLAUNCHER_AUTH_API_URL`. Sem API oficial configurada, salva usuario offline para manter o jogo funcional.
- `MagicWorldLauncherFullInstaller.cs`: instalador mostra porcentagem textual, barra de progresso e pulso durante instalacao interna; chama PowerShell escondido com `-Sta`; recria atalhos `.lnk` apagando antigos antes.
- `MagicWorldLauncherApp.cs`: janela principal roda dentro do `MagicWorldLauncher.exe` via `System.Management.Automation` para melhorar icone da taskbar; modos `--install-only`, `--launch-only` e `--uninstall` usam PowerShell escondido.
- Atalhos esperados na area de trabalho: `Magic World Launcher.lnk` e `Desinstalar Magic World Launcher.lnk`, ambos apontando para `MagicWorldLauncher.exe` com `MagicWorldLauncher.ico`.
- O instalador pode ser apagado apos instalar; o desinstalador remove `%LOCALAPPDATA%\MagicWorldLauncher`, `%APPDATA%\MagicWorldLauncher` e atalhos Magic World, sem tocar em TLauncher nem na `.minecraft` global.
- Servidores: conta offline so entra em servidor offline/cracked; servidor premium `online-mode=true` exige autenticacao valida e nao recebe bypass. Local no mesmo PC: `127.0.0.1:25565`; outro PC da LAN: IP da maquina servidora, exemplo `192.168.0.25:25565`; amigo: dominio/IP e porta informados por ele.
- Docs atualizados: `README.md`, `docs/WIKI.md`, `CODEX_HANDOFF.md` e `launcher/MagicWorldLauncher/README.txt`.
- Validacoes executadas antes do build final: parse do PowerShell, `-SelfTest`, compilacao C# do installer e compilacao C# do wrapper.
