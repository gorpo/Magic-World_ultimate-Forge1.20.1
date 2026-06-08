using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Windows.Forms;

internal static class MagicWorldLauncherApp
{
    [STAThread]
    private static int Main(string[] args)
    {
        try
        {
            string installDir = AppDomain.CurrentDomain.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
            if (args.Length > 0 && string.Equals(args[0], "--uninstall", StringComparison.OrdinalIgnoreCase))
            {
                return Uninstall(installDir);
            }

            string script = Path.Combine(installDir, "MagicWorldLauncher.ps1");
            if (!File.Exists(script))
            {
                MessageBox.Show("MagicWorldLauncher.ps1 nao encontrado.", "Magic World Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return 1;
            }

            string scriptSwitch = "";
            bool wait = false;
            if (args.Length > 0 && string.Equals(args[0], "--install-only", StringComparison.OrdinalIgnoreCase))
            {
                scriptSwitch = " -InstallOnly";
                wait = true;
            }
            else if (args.Length > 0 && string.Equals(args[0], "--launch-only", StringComparison.OrdinalIgnoreCase))
            {
                scriptSwitch = " -LaunchOnly";
                wait = true;
            }

            ProcessStartInfo start = new ProcessStartInfo
            {
                FileName = "powershell.exe",
                Arguments = "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File " + Quote(script) + scriptSwitch,
                WorkingDirectory = installDir,
                UseShellExecute = false,
                CreateNoWindow = true,
                WindowStyle = ProcessWindowStyle.Hidden
            };
            using (Process process = Process.Start(start))
            {
                if (wait)
                {
                    process.WaitForExit();
                    return process.ExitCode;
                }
            }
            return 0;
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.ToString(), "Magic World Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }

    private static int Uninstall(string installDir)
    {
        DialogResult confirm = MessageBox.Show(
            "Remover Magic World Launcher e dados locais?",
            "Magic World Launcher",
            MessageBoxButtons.YesNo,
            MessageBoxIcon.Question
        );
        if (confirm != DialogResult.Yes)
        {
            return 0;
        }

        string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
        string dataDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MagicWorldLauncher");
        string script = Path.Combine(Path.GetTempPath(), "MagicWorldLauncherUninstall.cmd");
        string content = "@echo off\r\n" +
                         "timeout /t 1 /nobreak >nul\r\n" +
                         "del /f /q " + Quote(Path.Combine(desktop, "Magic World Launcher.lnk")) + " >nul 2>nul\r\n" +
                         "del /f /q " + Quote(Path.Combine(desktop, "Desinstalar Magic World Launcher.lnk")) + " >nul 2>nul\r\n" +
                         "del /f /q " + Quote(Path.Combine(desktop, "Magic World Launcher.cmd")) + " >nul 2>nul\r\n" +
                         "del /f /q " + Quote(Path.Combine(desktop, "Uninstall Magic World Launcher.cmd")) + " >nul 2>nul\r\n" +
                         "rmdir /s /q " + Quote(dataDir) + " >nul 2>nul\r\n" +
                         "rmdir /s /q " + Quote(installDir) + " >nul 2>nul\r\n" +
                         "del /f /q \"%~f0\" >nul 2>nul\r\n";
        File.WriteAllText(script, content, Encoding.ASCII);
        Process.Start(new ProcessStartInfo
        {
            FileName = script,
            UseShellExecute = false,
            CreateNoWindow = true,
            WindowStyle = ProcessWindowStyle.Hidden
        });
        return 0;
    }

    private static string Quote(string value)
    {
        return "\"" + value.Replace("\"", "\\\"") + "\"";
    }
}
