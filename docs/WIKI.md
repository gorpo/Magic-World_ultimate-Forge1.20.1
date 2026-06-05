# Magic World Ultimate Forge - Wiki de Trabalho

Esta wiki foi reiniciada em 2026-06-05 para o novo port controlado do projeto NeoForge 26 para Forge 1.20.1.

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
