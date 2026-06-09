# Release: Magic World Launcher FULL V1.0.0.2

Tag sugerida: `launcher-full-v1.0.0.2`
Titulo sugerido: `Magic World Launcher FULL V1.0.0.2`

Arquivo para anexar manualmente no GitHub Releases:

```text
launcher/dist/MagicWorldLauncherFullInstaller-Stable V1.0.0.2.exe
```

Nome do asset no release:

```text
MagicWorldLauncherFullInstaller-Stable V1.0.0.2.exe
```

SHA256:

```text
CBF291D56836FDA6D7739F763247EEF0805789F029CBFFD71A142EFAA2B393E2
```

Link que o README ja referencia apos criar o release:

```text
https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1/releases/download/launcher-full-v1.0.0.2/MagicWorldLauncherFullInstaller-Stable%20V1.0.0.2.exe
```

## Texto do release para copiar

Magic World Launcher FULL V1.0.0.2 e o instalador recomendado para jogar Magic World no Windows.

Ele instala o launcher proprio do Magic World, prepara a pasta isolada do jogo e deixa o pacote pronto para abrir pelo atalho criado na area de trabalho.

### O que vem neste instalador

- Launcher oficial do Magic World para Windows.
- Ambiente proprio em `%LOCALAPPDATA%\MagicWorldLauncher`.
- Pasta do jogo isolada em `%LOCALAPPDATA%\MagicWorldLauncher\.minecraft`.
- Mod Magic World, Resource Pack, Shader Pack e configuracoes do pacote.
- Atalhos com icone proprio para abrir e desinstalar.
- Tela de configuracoes com acesso a `Pasta do jogo`, `Pasta do launcher` e `GitHub`.
- Perfil local para definir o nome de usuario usado dentro do jogo.

### Como instalar

1. Baixe `MagicWorldLauncherFullInstaller-Stable V1.0.0.2.exe` nos assets deste release.
2. Execute o instalador no Windows.
3. Aguarde a instalacao terminar.
4. Abra `Magic World Launcher` pelo atalho criado na area de trabalho.
5. Defina seu nome de usuario em `Perfil`, se quiser.
6. Clique em `Jogar Magic World`.

Depois que a instalacao terminar, o arquivo `.exe` baixado pode ser apagado. O jogo continua funcionando pelo launcher instalado.

### Onde ficam os arquivos

- Raiz do launcher: `%LOCALAPPDATA%\MagicWorldLauncher`
- Pasta do jogo: `%LOCALAPPDATA%\MagicWorldLauncher\.minecraft`
- Mods: `%LOCALAPPDATA%\MagicWorldLauncher\.minecraft\mods`
- Resource packs: `%LOCALAPPDATA%\MagicWorldLauncher\.minecraft\resourcepacks`
- Shaders: `%LOCALAPPDATA%\MagicWorldLauncher\.minecraft\shaderpacks`

A instalacao nao usa a `.minecraft` global do Windows.

### Como adicionar arquivos extras

- Mods extras: coloque `.jar` em `%LOCALAPPDATA%\MagicWorldLauncher\.minecraft\mods`.
- Resource packs extras: coloque `.zip` em `%LOCALAPPDATA%\MagicWorldLauncher\.minecraft\resourcepacks`.
- Shaders extras: coloque `.zip` em `%LOCALAPPDATA%\MagicWorldLauncher\.minecraft\shaderpacks`.

Use apenas arquivos compativeis com Minecraft Forge 1.20.1.

### Servidores

Para jogar em servidor, abra o jogo pelo launcher, entre em `Multiplayer` dentro do Minecraft e cadastre o servidor pela propria tela do jogo.

### Observacao

Este release e focado no instalador FULL. O instalador simples continua sendo uma opcao separada para quem ja possui uma instalacao compativel e quer apenas copiar os arquivos do pacote Magic World para uma pasta escolhida.

## Checklist de publicacao manual

- Criar release com tag `launcher-full-v1.0.0.2`.
- Usar titulo `Magic World Launcher FULL V1.0.0.2`.
- Colar o texto acima na descricao.
- Anexar `launcher/dist/MagicWorldLauncherFullInstaller-Stable V1.0.0.2.exe`.
- Confirmar que o asset ficou exatamente com o nome `MagicWorldLauncherFullInstaller-Stable V1.0.0.2.exe`.
- Conferir o link do README apos publicar.
