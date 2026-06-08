Magic World Launcher proprio

Este launcher instala e executa o Magic World em pastas exclusivas.
Ele NAO depende da instalacao local do TLauncher, do launcher oficial ou da .minecraft global.

Pastas usadas:
- Instalacao do launcher: %LOCALAPPDATA%\MagicWorldLauncher
- Minecraft interno do Magic World: %APPDATA%\MagicWorldLauncher\.minecraft
- Contas e configuracoes: %APPDATA%\MagicWorldLauncher

Assim, voce pode manter TLauncher, launcher oficial e outros launchers instalados em paralelo.
Eles nao misturam mods, versoes, assets, saves ou configuracoes com o Magic World.

Fluxo:
1. Execute MagicWorldLauncherFullInstaller.exe.
2. O instalador extrai o launcher, instala/verifica Forge, Minecraft, mods, resourcepacks, shaderpacks e Java proprio.
3. Depois da instalacao, voce pode apagar o instalador.
4. Abra "Magic World Launcher" pela area de trabalho.
5. Clique em "Jogar Magic World".
6. O launcher verifica o que faltar, abre o Minecraft e fica oculto ate o jogo fechar.

Interface:
- "Login": usuario/senha e opcao de salvar senha.
- "Servidores": cadastra IP/dominio:porta e gera servers.dat na .minecraft exclusiva.
- ".minecraft": abre a pasta interna exclusiva do Magic World.
- "Configuracoes": RAM por slider de 2 GB ate 16 GB, resolucao personalizada e opcao de ocultar o launcher durante o jogo.
- "Jogar Magic World": instala/verifica automaticamente e abre o Minecraft.

Login:
- Login online TLauncher depende de API oficial configurada em MAGICWORLD_TLAUNCHER_AUTH_API_URL.
- Sem API oficial configurada, o launcher salva o usuario em modo offline para manter o jogo funcional.
- A senha salva fica em %APPDATA%\MagicWorldLauncher\tlauncher-password.xml.

Servidores:
- Conta offline entra apenas em servidores que aceitam modo offline/cracked.
- Servidores premium online-mode=true exigem autenticacao valida.
- O launcher nao faz bypass de autenticacao premium.
- Mesmo PC: 127.0.0.1:25565.
- Outro computador da rede: IP_DA_MAQUINA:25565, por exemplo 192.168.0.25:25565.
- Servidor de amigo: dominio/IP e porta informados por ele.

Desinstalacao:
- Use o atalho "Desinstalar Magic World Launcher" na area de trabalho.
- Ele remove %LOCALAPPDATA%\MagicWorldLauncher, %APPDATA%\MagicWorldLauncher e atalhos Magic World.
- Ele NAO remove TLauncher, launcher oficial nem a .minecraft global.

Release:
MagicWorldLauncherFullInstaller-Stable V1.0.0.2.exe
