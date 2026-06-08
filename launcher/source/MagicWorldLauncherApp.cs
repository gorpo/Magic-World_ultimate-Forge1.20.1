using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Windows.Forms;
using System.Management.Automation;
using System.Management.Automation.Runspaces;
using System.Threading;

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
            if (args.Length > 0 && string.Equals(args[0], "--install-only", StringComparison.OrdinalIgnoreCase))
            {
                scriptSwitch = " -InstallOnly";
                return RunPowerShellProcess(script, installDir, scriptSwitch);
            }
            else if (args.Length > 0 && string.Equals(args[0], "--launch-only", StringComparison.OrdinalIgnoreCase))
            {
                scriptSwitch = " -LaunchOnly";
                return RunPowerShellProcess(script, installDir, scriptSwitch);
            }

            Directory.SetCurrentDirectory(installDir);
            return RunPowerShellInProcess(script);
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.ToString(), "Magic World Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }

    private static int RunPowerShellProcess(string script, string installDir, string scriptSwitch)
    {
            ProcessStartInfo start = new ProcessStartInfo
            {
                FileName = "powershell.exe",
                Arguments = "-NoProfile -Sta -ExecutionPolicy Bypass -WindowStyle Hidden -File " + Quote(script) + scriptSwitch,
                WorkingDirectory = installDir,
                UseShellExecute = false,
                CreateNoWindow = true,
                WindowStyle = ProcessWindowStyle.Hidden
            };
            using (Process process = Process.Start(start))
            {
                process.WaitForExit();
                return process.ExitCode;
            }
    }

    private static int RunPowerShellInProcess(string script)
    {
        string previousScriptPath = Environment.GetEnvironmentVariable("MAGICWORLD_LAUNCHER_SCRIPT_PATH");
        Environment.SetEnvironmentVariable("MAGICWORLD_LAUNCHER_SCRIPT_PATH", script);
        using (Runspace runspace = RunspaceFactory.CreateRunspace())
        {
            runspace.ApartmentState = ApartmentState.STA;
            runspace.ThreadOptions = PSThreadOptions.ReuseThread;
            runspace.Open();
            using (PowerShell shell = PowerShell.Create())
            {
                shell.Runspace = runspace;
                try
                {
                    shell.AddScript(File.ReadAllText(script, Encoding.UTF8));
                    shell.Invoke();
                    if (shell.HadErrors)
                    {
                        StringBuilder errors = new StringBuilder();
                        foreach (ErrorRecord error in shell.Streams.Error)
                        {
                            errors.AppendLine(error.ToString());
                        }
                        MessageBox.Show(errors.ToString(), "Magic World Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        return 1;
                    }
                }
                finally
                {
                    Environment.SetEnvironmentVariable("MAGICWORLD_LAUNCHER_SCRIPT_PATH", previousScriptPath);
                }
            }
        }
        return 0;
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
        string script = Path.Combine(Path.GetTempPath(), "MagicWorldLauncherUninstall.ps1");
        string content = "$ErrorActionPreference = 'SilentlyContinue'\r\n" +
                         "Start-Sleep -Milliseconds 900\r\n" +
                         "Remove-Item -LiteralPath " + PsQuote(Path.Combine(desktop, "Magic World Launcher.lnk")) + " -Force\r\n" +
                         "Remove-Item -LiteralPath " + PsQuote(Path.Combine(desktop, "Desinstalar Magic World Launcher.lnk")) + " -Force\r\n" +
                         "Remove-Item -LiteralPath " + PsQuote(Path.Combine(desktop, "Magic World Launcher.cmd")) + " -Force\r\n" +
                         "Remove-Item -LiteralPath " + PsQuote(Path.Combine(desktop, "Uninstall Magic World Launcher.cmd")) + " -Force\r\n" +
                         "Remove-Item -LiteralPath " + PsQuote(dataDir) + " -Recurse -Force\r\n" +
                         "Remove-Item -LiteralPath " + PsQuote(installDir) + " -Recurse -Force\r\n" +
                         "Remove-Item -LiteralPath $PSCommandPath -Force\r\n";
        File.WriteAllText(script, content, Encoding.ASCII);
        Process.Start(new ProcessStartInfo
        {
            FileName = "powershell.exe",
            Arguments = "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File " + Quote(script),
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

    private static string PsQuote(string value)
    {
        return "'" + value.Replace("'", "''") + "'";
    }
}
