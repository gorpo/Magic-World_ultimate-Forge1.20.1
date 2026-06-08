@echo off
setlocal
set "INSTALL_DIR=%LOCALAPPDATA%\MagicWorldLauncher"
set "DATA_DIR=%APPDATA%\MagicWorldLauncher"
set "DESKTOP=%USERPROFILE%\Desktop"
echo Removendo Magic World Launcher...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Start-Sleep -Seconds 1; Get-Process java,javaw -ErrorAction SilentlyContinue | Where-Object { $_.Path -like '*MagicWorldLauncher*' } | Stop-Process -Force -ErrorAction SilentlyContinue; Remove-Item -LiteralPath '%DESKTOP%\Magic World Launcher.cmd' -Force -ErrorAction SilentlyContinue; Remove-Item -LiteralPath '%DESKTOP%\Uninstall Magic World Launcher.cmd' -Force -ErrorAction SilentlyContinue; Remove-Item -LiteralPath '%DATA_DIR%' -Recurse -Force -ErrorAction SilentlyContinue; Remove-Item -LiteralPath '%INSTALL_DIR%' -Recurse -Force -ErrorAction SilentlyContinue"
echo Magic World Launcher removido.
pause
