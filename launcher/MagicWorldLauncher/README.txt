Magic World Launcher proprio

Este launcher NAO depende do TLauncher instalado localmente.
O login TLauncher deve ser feito pelo botao "Login TLauncher API", usando o endpoint oficial configurado em:
MAGICWORLD_TLAUNCHER_AUTH_API_URL
O launcher nao salva senha; ele guarda apenas usuario/token retornados pela API em %APPDATA%\MagicWorldLauncher\accounts.json.
O Minecraft do Magic World roda em pasta propria:
%APPDATA%\MagicWorldLauncher\.minecraft

Fluxo:
1. Abra "Abrir Magic World Launcher.cmd".
2. Se precisar entrar/trocar conta, configure a API oficial e clique em "Login TLauncher API".
3. Clique em "Instalar Magic World".
4. O launcher usa o pacote FULL local ou baixa o MagicWorldInstaller.exe do release oficial do repositorio.
5. O EXE e salvo em uma pasta temporaria.
6. O launcher extrai o payload FULL embutido no EXE sem abrir a tela do installer.
7. O script de instalacao roda em modo -NoGui e instala Forge 1.20.1, mods, resourcepacks e shaderpacks na pasta propria do Magic World.
8. O launcher cria/verifica as pastas necessarias, baixa Java 17 proprio se precisar, e baixa bibliotecas/assets faltantes.
9. Ao final, clique em "Jogar Magic World" para abrir o Minecraft direto por este launcher.

Desinstalacao:
Abra "Uninstall Magic World Launcher.cmd".
Ele remove %LOCALAPPDATA%\MagicWorldLauncher, %APPDATA%\MagicWorldLauncher e os atalhos do Magic World Launcher na area de trabalho.
Ele NAO remove o TLauncher nem a .minecraft original, se existirem.

Release usado:
https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1/releases/tag/installer-upload-manual-forge-1.20.1-v1.0.0.1-main

Repositorio:
https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1
