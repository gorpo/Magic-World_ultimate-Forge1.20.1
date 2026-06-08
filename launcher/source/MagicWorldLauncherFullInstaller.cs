using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;

internal static class MagicWorldLauncherFullInstaller
{
    private static readonly byte[] PayloadMarker = Encoding.ASCII.GetBytes("MAGICWORLD_LAUNCHER_PAYLOAD_V1");

    [STAThread]
    private static int Main()
    {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);

        using (Form form = new Form())
        using (Label status = new Label())
        using (ProgressBar progress = new ProgressBar())
        using (Button close = new Button())
        {
            form.Text = "Magic World Launcher FULL";
            form.Width = 520;
            form.Height = 220;
            form.StartPosition = FormStartPosition.CenterScreen;
            form.FormBorderStyle = FormBorderStyle.FixedDialog;
            form.MaximizeBox = false;
            form.BackColor = System.Drawing.Color.FromArgb(12, 18, 30);
            form.Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath);

            status.Left = 24;
            status.Top = 24;
            status.Width = 460;
            status.Height = 72;
            status.ForeColor = System.Drawing.Color.White;
            status.Font = new System.Drawing.Font("Segoe UI", 10);
            status.Text = "Instalando Magic World Launcher FULL...";
            form.Controls.Add(status);

            progress.Left = 24;
            progress.Top = 105;
            progress.Width = 460;
            progress.Height = 22;
            progress.Minimum = 0;
            progress.Maximum = 100;
            form.Controls.Add(progress);

            close.Left = 334;
            close.Top = 145;
            close.Width = 150;
            close.Height = 30;
            close.Text = "Fechar";
            close.Enabled = false;
            close.Click += delegate { form.Close(); };
            form.Controls.Add(close);

            form.Shown += delegate
            {
                try
                {
                    string installDir = Path.Combine(
                        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                        "MagicWorldLauncher"
                    );

                    status.Text = "Extraindo launcher e pacote FULL...";
                    progress.Value = 20;
                    Application.DoEvents();

                    if (Directory.Exists(installDir))
                    {
                        Directory.Delete(installDir, true);
                    }
                    Directory.CreateDirectory(installDir);
                    ExtractPayloadZip(installDir);
                    ConfigureFolderIcon(installDir);

                    status.Text = "Instalando Minecraft, Forge e pacote Magic World...";
                    progress.Value = 55;
                    Application.DoEvents();

                    RunInternalInstall(installDir);

                    status.Text = "Criando atalhos com icones...";
                    progress.Value = 90;
                    Application.DoEvents();

                    CreateDesktopShortcuts(installDir);

                    progress.Value = 100;
                    status.Text = "Magic World Launcher instalado. Abra pelo atalho da area de trabalho e clique em Jogar Magic World.";
                    close.Enabled = true;

                    string launcherExe = Path.Combine(installDir, "MagicWorldLauncher.exe");
                    if (File.Exists(launcherExe))
                    {
                        Process.Start(new ProcessStartInfo
                        {
                            FileName = launcherExe,
                            WorkingDirectory = installDir,
                            CreateNoWindow = true,
                            WindowStyle = ProcessWindowStyle.Hidden,
                            UseShellExecute = true
                        });
                    }
                }
                catch (Exception ex)
                {
                    progress.Value = 0;
                    status.Text = "Erro: " + ex.Message;
                    close.Enabled = true;
                    MessageBox.Show(form, ex.ToString(), "Erro no installer", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            };

            Application.Run(form);
        }

        return 0;
    }

    private static void ExtractPayloadZip(string installDir)
    {
        string tempZip = Path.Combine(Path.GetTempPath(), "MagicWorldLauncherPayload.zip");
        using (FileStream executable = File.OpenRead(Application.ExecutablePath))
        {
            if (executable.Length < PayloadMarker.Length + sizeof(long))
            {
                throw new InvalidDataException("Payload do launcher nao encontrado.");
            }

            executable.Position = executable.Length - PayloadMarker.Length;
            byte[] marker = new byte[PayloadMarker.Length];
            executable.Read(marker, 0, marker.Length);
            if (!marker.SequenceEqual(PayloadMarker))
            {
                throw new InvalidDataException("Payload do launcher invalido.");
            }

            executable.Position = executable.Length - PayloadMarker.Length - sizeof(long);
            byte[] lengthBytes = new byte[sizeof(long)];
            executable.Read(lengthBytes, 0, lengthBytes.Length);
            long payloadLength = BitConverter.ToInt64(lengthBytes, 0);
            long payloadStart = executable.Length - PayloadMarker.Length - sizeof(long) - payloadLength;
            if (payloadLength <= 0 || payloadStart < 0)
            {
                throw new InvalidDataException("Tamanho do payload invalido.");
            }

            executable.Position = payloadStart;
            using (FileStream output = File.Create(tempZip))
            {
                CopyBytes(executable, output, payloadLength);
            }
        }

        ZipFile.ExtractToDirectory(tempZip, installDir);
    }

    private static void CopyBytes(Stream input, Stream output, long bytes)
    {
        byte[] buffer = new byte[1024 * 1024];
        long remaining = bytes;
        while (remaining > 0)
        {
            int read = input.Read(buffer, 0, (int)Math.Min(buffer.Length, remaining));
            if (read <= 0)
            {
                throw new EndOfStreamException("Fim inesperado ao extrair payload.");
            }
            output.Write(buffer, 0, read);
            remaining -= read;
        }
    }

    private static void RunInternalInstall(string installDir)
    {
        string launcherExe = Path.Combine(installDir, "MagicWorldLauncher.exe");
        string script = Path.Combine(installDir, "MagicWorldLauncher.ps1");
        ProcessStartInfo start;
        if (File.Exists(launcherExe))
        {
            start = new ProcessStartInfo
            {
                FileName = launcherExe,
                Arguments = "--install-only",
                WorkingDirectory = installDir,
                UseShellExecute = false,
                CreateNoWindow = true,
                WindowStyle = ProcessWindowStyle.Hidden
            };
        }
        else
        {
            start = new ProcessStartInfo
            {
                FileName = "powershell.exe",
                Arguments = "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File " + Quote(script) + " -InstallOnly",
                WorkingDirectory = installDir,
                UseShellExecute = false,
                CreateNoWindow = true,
                WindowStyle = ProcessWindowStyle.Hidden
            };
        }

        using (Process process = Process.Start(start))
        {
            process.WaitForExit();
            if (process.ExitCode != 0)
            {
                throw new InvalidOperationException("Falha ao instalar Minecraft/Forge pelo launcher interno. Codigo: " + process.ExitCode);
            }
        }
    }

    private static void CreateDesktopShortcuts(string installDir)
    {
        string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
        string launcherExe = Path.Combine(installDir, "MagicWorldLauncher.exe");
        string icon = LauncherIconPath(installDir);
        CreateShortcut(
            Path.Combine(desktop, "Magic World Launcher.lnk"),
            launcherExe,
            "",
            installDir,
            icon,
            "Abrir Magic World Launcher"
        );
        CreateShortcut(
            Path.Combine(desktop, "Desinstalar Magic World Launcher.lnk"),
            launcherExe,
            "--uninstall",
            installDir,
            icon,
            "Remover Magic World Launcher"
        );
        DeleteOldCmdShortcut(desktop, "Magic World Launcher.cmd");
        DeleteOldCmdShortcut(desktop, "Uninstall Magic World Launcher.cmd");
    }

    private static void DeleteOldCmdShortcut(string desktop, string name)
    {
        string path = Path.Combine(desktop, name);
        if (File.Exists(path))
        {
            File.Delete(path);
        }
    }

    private static void ConfigureFolderIcon(string installDir)
    {
        string icon = LauncherIconPath(installDir);
        if (!File.Exists(icon))
        {
            return;
        }

        string desktopIni = Path.Combine(installDir, "desktop.ini");
        File.WriteAllText(
            desktopIni,
            "[.ShellClassInfo]\r\nIconResource=MagicWorldLauncher.ico,0\r\n",
            Encoding.ASCII
        );
        File.SetAttributes(desktopIni, FileAttributes.Hidden | FileAttributes.System);
        File.SetAttributes(installDir, File.GetAttributes(installDir) | FileAttributes.System);
    }

    private static string LauncherIconPath(string installDir)
    {
        string icon = Path.Combine(installDir, "MagicWorldLauncher.ico");
        if (File.Exists(icon))
        {
            return icon;
        }
        return Path.Combine(installDir, "assets", "magicworld.ico");
    }

    private static string Quote(string value)
    {
        return "\"" + value.Replace("\"", "\\\"") + "\"";
    }

    private static void CreateShortcut(string shortcutPath, string targetPath, string arguments, string workingDirectory, string iconPath, string description)
    {
        IShellLinkW link = (IShellLinkW)new CShellLink();
        link.SetPath(targetPath);
        link.SetArguments(arguments);
        link.SetWorkingDirectory(workingDirectory);
        link.SetDescription(description);
        if (File.Exists(iconPath))
        {
            link.SetIconLocation(iconPath, 0);
        }

        IPersistFile file = (IPersistFile)link;
        file.Save(shortcutPath, true);
    }

    [ComImport]
    [Guid("00021401-0000-0000-C000-000000000046")]
    private class CShellLink
    {
    }

    [ComImport]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    [Guid("000214F9-0000-0000-C000-000000000046")]
    private interface IShellLinkW
    {
        void GetPath([Out, MarshalAs(UnmanagedType.LPWStr)] StringBuilder pszFile, int cchMaxPath, IntPtr pfd, uint fFlags);
        void GetIDList(out IntPtr ppidl);
        void SetIDList(IntPtr pidl);
        void GetDescription([Out, MarshalAs(UnmanagedType.LPWStr)] StringBuilder pszName, int cchMaxName);
        void SetDescription([MarshalAs(UnmanagedType.LPWStr)] string pszName);
        void GetWorkingDirectory([Out, MarshalAs(UnmanagedType.LPWStr)] StringBuilder pszDir, int cchMaxPath);
        void SetWorkingDirectory([MarshalAs(UnmanagedType.LPWStr)] string pszDir);
        void GetArguments([Out, MarshalAs(UnmanagedType.LPWStr)] StringBuilder pszArgs, int cchMaxPath);
        void SetArguments([MarshalAs(UnmanagedType.LPWStr)] string pszArgs);
        void GetHotkey(out short pwHotkey);
        void SetHotkey(short wHotkey);
        void GetShowCmd(out int piShowCmd);
        void SetShowCmd(int iShowCmd);
        void GetIconLocation([Out, MarshalAs(UnmanagedType.LPWStr)] StringBuilder pszIconPath, int cchIconPath, out int piIcon);
        void SetIconLocation([MarshalAs(UnmanagedType.LPWStr)] string pszIconPath, int iIcon);
        void SetRelativePath([MarshalAs(UnmanagedType.LPWStr)] string pszPathRel, uint dwReserved);
        void Resolve(IntPtr hwnd, uint fFlags);
        void SetPath([MarshalAs(UnmanagedType.LPWStr)] string pszFile);
    }

    [ComImport]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    [Guid("0000010b-0000-0000-C000-000000000046")]
    private interface IPersistFile
    {
        void GetClassID(out Guid pClassID);
        void IsDirty();
        void Load([MarshalAs(UnmanagedType.LPWStr)] string pszFileName, uint dwMode);
        void Save([MarshalAs(UnmanagedType.LPWStr)] string pszFileName, bool fRemember);
        void SaveCompleted([MarshalAs(UnmanagedType.LPWStr)] string pszFileName);
        void GetCurFile([MarshalAs(UnmanagedType.LPWStr)] out string ppszFileName);
    }
}
