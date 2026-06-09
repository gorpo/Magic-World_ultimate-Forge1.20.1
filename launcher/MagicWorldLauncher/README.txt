Magic World Launcher proprio

Este launcher instala e executa o Magic World em uma raiz propria unica:
%LOCALAPPDATA%\MagicWorldLauncher

Pastas usadas dentro dessa raiz:
- Pasta do jogo: %LOCALAPPDATA%\MagicWorldLauncher\.minecraft
- Launcher, Java, contas, configuracoes e logs: %LOCALAPPDATA%\MagicWorldLauncher

Ele NAO usa, NAO abre e NAO sugere a .minecraft global do Windows.
Ele tambem nao depende de outro launcher instalado.

Fluxo:
1. Execute MagicWorldLauncherFullInstaller.exe.
2. O instalador prepara a raiz local, preservando .minecraft, runtime, contas, configuracoes e logs em atualizacoes.
3. Depois da instalacao, voce pode apagar o instalador.
4. Abra "Magic World Launcher" pela area de trabalho.
5. Clique em "Jogar".
6. O launcher verifica o que faltar, abre o Minecraft e fica oculto ate o jogo fechar.

Interface:
- Icone de pasta: abre %LOCALAPPDATA%\MagicWorldLauncher\.minecraft.
- Icone de engrenagem: RAM por slider de 2 GB ate 16 GB, resolucao por dropdown, atalhos para Pasta do jogo, Pasta do launcher e GitHub.
- Icone de perfil: define apenas o usuario local.
- Botao "Jogar": instala/verifica automaticamente e abre o Minecraft.

Perfil:
- Salva apenas o nome de usuario local em modo offline.
- Nao ha campo de senha nem armazenamento de credenciais.

Multiplayer:
- Use a tela Multiplayer do proprio Minecraft para cadastrar servidores.

Desinstalacao:
- Use o atalho "Desinstalar Magic World Launcher" na area de trabalho.
- Ele remove %LOCALAPPDATA%\MagicWorldLauncher e atalhos Magic World.
- Ele nao remove a .minecraft global do Windows.

Release:
MagicWorldLauncherFullInstaller-Stable V1.0.0.2.exe
