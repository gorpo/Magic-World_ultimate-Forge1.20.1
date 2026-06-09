using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Collections.Concurrent;
using Microsoft.Win32;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
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
            bool canClose = false;
            form.Text = "Magic World Launcher FULL";
            form.Width = 560;
            form.Height = 240;
            form.StartPosition = FormStartPosition.CenterScreen;
            form.FormBorderStyle = FormBorderStyle.FixedDialog;
            form.MaximizeBox = false;
            form.BackColor = System.Drawing.Color.FromArgb(12, 18, 30);
            form.Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath);

            status.Left = 26;
            status.Top = 24;
            status.Width = 500;
            status.Height = 82;
            status.ForeColor = System.Drawing.Color.White;
            status.Font = new System.Drawing.Font("Segoe UI", 10, FontStyle.Bold);
            status.Text = "0% - Instalando Magic World Launcher FULL...";
            form.Controls.Add(status);

            progress.Left = 26;
            progress.Top = 112;
            progress.Width = 500;
            progress.Height = 22;
            progress.Minimum = 0;
            progress.Maximum = 100;
            form.Controls.Add(progress);

            close.Left = 376;
            close.Top = 158;
            close.Width = 150;
            close.Height = 34;
            close.Text = "Aguarde";
            close.Enabled = true;
            close.ForeColor = System.Drawing.Color.White;
            close.BackColor = System.Drawing.Color.FromArgb(16, 24, 38);
            close.FlatStyle = FlatStyle.Flat;
            close.FlatAppearance.BorderColor = System.Drawing.Color.FromArgb(218, 165, 32);
            close.FlatAppearance.BorderSize = 1;
            close.Click += delegate
            {
                if (canClose)
                {
                    form.Close();
                }
            };
            form.Controls.Add(close);
            form.FormClosing += delegate(object sender, FormClosingEventArgs e)
            {
                if (!canClose)
                {
                    e.Cancel = true;
                }
            };

            form.Shown += delegate
            {
                try
                {
                    string installDir = Path.Combine(
                        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                        "MagicWorldLauncher"
                    );

                    SetProgress(status, progress, 12, "Preparando instalacao FULL...");

                    PrepareInstallDirectory(installDir);
                    SetProgress(status, progress, 22, "Extraindo launcher e pacote FULL...");
                    ExtractPayloadZip(installDir);
                    ConfigureFolderIcon(installDir);

                    SetProgress(status, progress, 40, "Instalando Minecraft, Forge e pacote Magic World...");
                    RunInternalInstall(installDir, status, progress);

                    SetProgress(status, progress, 94, "Criando atalhos com icones...");

                    CreateDesktopShortcuts(installDir);
                    RegisterWindowsUninstallEntry(installDir);
                    NotifyShellChanged();

                    SetProgress(status, progress, 100, "Magic World Launcher instalado. Abra pelo atalho da area de trabalho.");
                    canClose = true;
                    close.Text = "Fechar";

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
                    canClose = true;
                    close.Text = "Fechar";
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

    private static void PrepareInstallDirectory(string installDir)
    {
        Directory.CreateDirectory(installDir);

        string[] filesToReplace = new string[]
        {
            "MagicWorldLauncher.exe",
            "MagicWorldLauncher.ps1",
            "MagicWorldInstaller.exe",
            "MagicWorldPayload.bin",
            "install-magicworld-forge.ps1",
            "README.txt",
            "MagicWorldLauncher.ico",
            "desktop.ini",
            "Abrir Magic World Launcher.cmd",
            "Uninstall Magic World Launcher.cmd"
        };

        foreach (string file in filesToReplace)
        {
            string path = Path.Combine(installDir, file);
            if (File.Exists(path))
            {
                File.SetAttributes(path, FileAttributes.Normal);
                File.Delete(path);
            }
        }

        string assets = Path.Combine(installDir, "assets");
        if (Directory.Exists(assets))
        {
            Directory.Delete(assets, true);
        }
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

    private static void SetProgress(Label status, ProgressBar progress, int percent, string text)
    {
        percent = Math.Max(0, Math.Min(100, percent));
        progress.Value = percent;
        status.Text = percent + "% - " + text;
        Application.DoEvents();
    }

    private static void RunInternalInstall(string installDir, Label status, ProgressBar progress)
    {
        string script = Path.Combine(installDir, "MagicWorldLauncher.ps1");
        if (!File.Exists(script))
        {
            throw new FileNotFoundException("Script do launcher nao encontrado.", script);
        }

        ProcessStartInfo start = new ProcessStartInfo
        {
            FileName = "powershell.exe",
            Arguments = "-NoProfile -Sta -ExecutionPolicy Bypass -WindowStyle Hidden -File " + Quote(script) + " -InstallOnly",
            WorkingDirectory = installDir,
            UseShellExecute = false,
            CreateNoWindow = true,
            WindowStyle = ProcessWindowStyle.Hidden,
            RedirectStandardOutput = true,
            RedirectStandardError = true
        };

        ConcurrentQueue<string> lines = new ConcurrentQueue<string>();
        ConcurrentQueue<string> errors = new ConcurrentQueue<string>();
        int percent = 40;
        DateTime lastPulse = DateTime.UtcNow;
        string lastError = "";

        using (Process process = Process.Start(start))
        {
            process.OutputDataReceived += delegate(object sender, DataReceivedEventArgs e)
            {
                if (!string.IsNullOrWhiteSpace(e.Data))
                {
                    lines.Enqueue(e.Data);
                }
            };
            process.ErrorDataReceived += delegate(object sender, DataReceivedEventArgs e)
            {
                if (!string.IsNullOrWhiteSpace(e.Data))
                {
                    errors.Enqueue(e.Data);
                }
            };
            process.BeginOutputReadLine();
            process.BeginErrorReadLine();

            while (!process.HasExited || !lines.IsEmpty || !errors.IsEmpty)
            {
                string line;
                while (lines.TryDequeue(out line))
                {
                    int parsedPercent;
                    string parsedText = ParseProgressLine(line, out parsedPercent);
                    if (parsedPercent >= 0)
                    {
                        percent = Math.Max(percent, Math.Min(92, parsedPercent));
                    }
                    else
                    {
                        percent = Math.Min(92, percent + 1);
                    }
                    SetProgress(status, progress, percent, parsedText);
                    lastPulse = DateTime.UtcNow;
                }

                string errorLine;
                while (errors.TryDequeue(out errorLine))
                {
                    lastError = errorLine;
                    percent = Math.Min(92, percent + 1);
                    SetProgress(status, progress, percent, TrimForStatus(errorLine));
                    lastPulse = DateTime.UtcNow;
                }

                if (!process.HasExited && (DateTime.UtcNow - lastPulse).TotalMilliseconds >= 1200)
                {
                    percent = Math.Min(92, percent + 1);
                    SetProgress(status, progress, percent, "Instalando Minecraft, Forge e pacote Magic World...");
                    lastPulse = DateTime.UtcNow;
                }

                Application.DoEvents();
                Thread.Sleep(120);
            }

            if (process.ExitCode != 0)
            {
                if (string.IsNullOrWhiteSpace(lastError))
                {
                    lastError = "Veja o log em %TEMP%\\magicworld-forge-installer.log";
                }
                throw new InvalidOperationException("Falha ao instalar Minecraft/Forge pelo launcher interno. Codigo: " + process.ExitCode + ". " + lastError);
            }
        }
    }

    private static string ParseProgressLine(string line, out int percent)
    {
        percent = -1;
        if (line.StartsWith("PROGRESS:", StringComparison.OrdinalIgnoreCase))
        {
            string[] parts = line.Split(new char[] { ':' }, 3);
            if (parts.Length >= 3 && int.TryParse(parts[1], out percent))
            {
                return TrimForStatus(parts[2]);
            }
        }
        if (line.StartsWith("STATUS:", StringComparison.OrdinalIgnoreCase))
        {
            return TrimForStatus(line.Substring("STATUS:".Length));
        }
        return TrimForStatus(line);
    }

    private static string TrimForStatus(string text)
    {
        if (string.IsNullOrWhiteSpace(text))
        {
            return "Instalando Magic World...";
        }
        text = text.Trim();
        if (text.Length > 130)
        {
            return text.Substring(0, 127) + "...";
        }
        return text;
    }

    private static void CreateDesktopShortcuts(string installDir)
    {
        string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
        string launcherExe = Path.Combine(installDir, "MagicWorldLauncher.exe");
        string icon = File.Exists(launcherExe) ? launcherExe : LauncherIconPath(installDir);
        DeleteOldShortcut(Path.Combine(desktop, "Magic World Launcher.lnk"));
        DeleteOldShortcut(Path.Combine(desktop, "Desinstalar Magic World Launcher.lnk"));
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

    private static void RegisterWindowsUninstallEntry(string installDir)
    {
        string launcherExe = Path.Combine(installDir, "MagicWorldLauncher.exe");
        using (RegistryKey key = Registry.CurrentUser.CreateSubKey(@"Software\Microsoft\Windows\CurrentVersion\Uninstall\MagicWorldLauncher"))
        {
            key.SetValue("DisplayName", "Magic World Launcher");
            key.SetValue("DisplayVersion", "V1.0.0.2");
            key.SetValue("Publisher", "GuiPaluch - (Gorpo) - TCXS Project");
            key.SetValue("InstallLocation", installDir);
            key.SetValue("DisplayIcon", launcherExe + ",0");
            key.SetValue("UninstallString", Quote(launcherExe) + " --uninstall");
            key.SetValue("QuietUninstallString", Quote(launcherExe) + " --uninstall");
            key.SetValue("NoModify", 1, RegistryValueKind.DWord);
            key.SetValue("NoRepair", 1, RegistryValueKind.DWord);
            key.SetValue("EstimatedSize", EstimateDirectorySizeKb(installDir), RegistryValueKind.DWord);
            key.SetValue("InstallDate", DateTime.Now.ToString("yyyyMMdd"));
        }
    }

    private static int EstimateDirectorySizeKb(string installDir)
    {
        try
        {
            long total = Directory.EnumerateFiles(installDir, "*", SearchOption.AllDirectories)
                .Sum(path => new FileInfo(path).Length);
            return (int)Math.Min(int.MaxValue, Math.Max(1, total / 1024));
        }
        catch
        {
            return 1;
        }
    }

    private static void NotifyShellChanged()
    {
        SHChangeNotify(0x08000000, 0x0000, IntPtr.Zero, IntPtr.Zero);
    }

    private static void DeleteOldShortcut(string path)
    {
        if (File.Exists(path))
        {
            File.Delete(path);
        }
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

    [DllImport("shell32.dll")]
    private static extern void SHChangeNotify(int wEventId, uint uFlags, IntPtr dwItem1, IntPtr dwItem2);
}
