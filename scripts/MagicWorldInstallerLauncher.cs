using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Windows.Forms;

internal static class MagicWorldInstallerLauncher
{
    [STAThread]
    private static int Main(string[] args)
    {
        string scriptPath = ExtractEmbeddedInstaller();
        if (scriptPath == null)
        {
            scriptPath = FindInstallerScript();
        }

        if (scriptPath == null)
        {
            MessageBox.Show(
                "Nao encontrei o instalador embutido nem scripts\\install-magicworld-tlauncher.ps1 ao lado do executavel.",
                "Magic World Installer",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error
            );
            return 1;
        }

        string forwardedArgs = string.Join(" ", args.Select(QuoteArgument));
        string powershellArgs = "-NoProfile -ExecutionPolicy Bypass -File " + QuoteArgument(scriptPath);
        if (!string.IsNullOrWhiteSpace(forwardedArgs))
        {
            powershellArgs += " " + forwardedArgs;
        }

        try
        {
            using (Process process = Process.Start(new ProcessStartInfo
            {
                FileName = "powershell.exe",
                Arguments = powershellArgs,
                UseShellExecute = false,
                WorkingDirectory = Path.GetDirectoryName(scriptPath)
            }))
            {
                process.WaitForExit();
                return process.ExitCode;
            }
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message, "Magic World Installer", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }

    private static string ExtractEmbeddedInstaller()
    {
        Assembly assembly = Assembly.GetExecutingAssembly();
        if (assembly.GetManifestResourceStream("install-magicworld-tlauncher.ps1") == null)
        {
            return null;
        }

        string extractionRoot = Path.Combine(
            Path.GetTempPath(),
            "MagicWorldInstaller",
            assembly.GetName().Version.ToString()
        );

        Directory.CreateDirectory(extractionRoot);
        Directory.CreateDirectory(Path.Combine(extractionRoot, "screenshots"));

        ExtractResource(assembly, "install-magicworld-tlauncher.ps1", Path.Combine(extractionRoot, "install-magicworld-tlauncher.ps1"));
        ExtractResource(assembly, "banner_installer.png", Path.Combine(extractionRoot, "screenshots", "banner_installer.png"));
        ExtractResource(assembly, "neoforge-installer.jar", Path.Combine(extractionRoot, "neoforge-26.1.2.65-beta-installer.jar"));

        return Path.Combine(extractionRoot, "install-magicworld-tlauncher.ps1");
    }

    private static void ExtractResource(Assembly assembly, string resourceName, string destination)
    {
        using (Stream input = assembly.GetManifestResourceStream(resourceName))
        {
            if (input == null)
            {
                throw new FileNotFoundException("Recurso embutido ausente: " + resourceName);
            }

            using (FileStream output = File.Create(destination))
            {
                input.CopyTo(output);
            }
        }
    }

    private static string FindInstallerScript()
    {
        string exeDir = AppDomain.CurrentDomain.BaseDirectory;
        string[] candidates =
        {
            Path.Combine(exeDir, "install-magicworld-tlauncher.ps1"),
            Path.Combine(exeDir, "scripts", "install-magicworld-tlauncher.ps1"),
            Path.Combine(exeDir, "..", "scripts", "install-magicworld-tlauncher.ps1"),
            Path.Combine(Environment.CurrentDirectory, "scripts", "install-magicworld-tlauncher.ps1")
        };

        return candidates
            .Select(Path.GetFullPath)
            .FirstOrDefault(File.Exists);
    }

    private static string QuoteArgument(string value)
    {
        if (string.IsNullOrEmpty(value))
        {
            return "\"\"";
        }

        return "\"" + value.Replace("\"", "\\\"") + "\"";
    }
}
