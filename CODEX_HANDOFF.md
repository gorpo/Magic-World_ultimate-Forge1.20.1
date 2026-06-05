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
