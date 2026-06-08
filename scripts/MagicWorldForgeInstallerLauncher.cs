using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Windows.Forms;

internal static class MagicWorldForgeInstallerLauncher
{
    private const string ScriptName = "install-magicworld-forge.ps1";
    private const string ForgeInstallerName = "forge-1.20.1-47.4.10-installer.jar";
    private static readonly byte[] FullPayloadMarker = Encoding.ASCII.GetBytes("MAGICWORLD_FULL_PAYLOAD_V1");

    [STAThread]
    private static int Main(string[] args)
    {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        return ShowInstallerGui(args);
    }

    private static int ShowInstallerGui(string[] args)
    {
        int exitCode = 0;
        Form form = new Form();
        form.Text = "Magic World Forge Installer";
        form.Width = 760;
        form.Height = 540;
        form.StartPosition = FormStartPosition.CenterScreen;
        form.BackColor = Color.FromArgb(9, 13, 29);
        Icon icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath);
        if (icon != null)
        {
            form.Icon = icon;
        }

        Image banner = LoadEmbeddedImage("banner_installer.png");
        if (banner != null)
        {
            PictureBox picture = new PictureBox();
            picture.Image = banner;
            picture.SizeMode = PictureBoxSizeMode.Zoom;
            picture.Left = 18;
            picture.Top = 16;
            picture.Width = 706;
            picture.Height = 170;
            form.Controls.Add(picture);
        }

        Label label = new Label();
        label.ForeColor = Color.White;
        label.BackColor = Color.Transparent;
        label.Left = 24;
        label.Top = 205;
        label.Width = 690;
        label.Height = 54;
        label.Font = new Font("Segoe UI", 10);
        label.Text = "Instalador FULL Magic World Ultimate Forge 1.20.1: instala/atualiza Forge 47.4.10 e distribui mods, resourcepacks, shaderpacks e configuracoes embutidos para a .minecraft usada pelo TLauncher.";
        form.Controls.Add(label);

        TextBox pathBox = new TextBox();
        pathBox.Left = 24;
        pathBox.Top = 278;
        pathBox.Width = 560;
        pathBox.Text = FindMinecraftDir();
        form.Controls.Add(pathBox);

        Button browse = CreateButton("Escolher pasta", 596, 276, 128, 28, Color.FromArgb(43, 68, 118));
        browse.Click += delegate
        {
            using (FolderBrowserDialog dialog = new FolderBrowserDialog())
            {
                dialog.Description = "Escolha a pasta .minecraft usada pelo TLauncher";
                if (dialog.ShowDialog(form) == DialogResult.OK)
                {
                    pathBox.Text = dialog.SelectedPath;
                }
            }
        };
        form.Controls.Add(browse);

        TextBox output = new TextBox();
        output.Left = 24;
        output.Top = 324;
        output.Width = 700;
        output.Height = 120;
        output.Multiline = true;
        output.ScrollBars = ScrollBars.Vertical;
        output.ReadOnly = true;
        output.BackColor = Color.FromArgb(236, 242, 252);
        output.ForeColor = Color.FromArgb(14, 20, 32);
        output.Font = new Font("Consolas", 9);
        output.Text = InstallerOverview();
        form.Controls.Add(output);

        Button install = CreateButton("Instalar / Atualizar", 24, 460, 210, 34, Color.FromArgb(68, 91, 180));
        Button openFolder = CreateButton("Abrir pasta", 248, 460, 160, 34, Color.FromArgb(43, 68, 118));
        Button close = CreateButton("Fechar", 564, 460, 160, 34, Color.FromArgb(43, 68, 118));

        install.Click += delegate
        {
            install.Enabled = false;
            browse.Enabled = false;
            AppendOutput(output, "Iniciando instalacao Forge Magic World. Aguarde.\r\n");
            string minecraftDir = pathBox.Text;
            ThreadPool.QueueUserWorkItem(delegate
            {
                try
                {
                    int code = RunPowerShellInstall(minecraftDir, args, output);
                    exitCode = code;
                    AppendOutput(output, "Processo finalizado com codigo: " + code + "\r\n");
                    form.BeginInvoke((MethodInvoker)delegate
                    {
                        MessageBox.Show(form, "Magic World Forge instalado. No TLauncher, selecione Forge 1.20.1-47.4.10.", "Magic World Forge Installer");
                        install.Enabled = true;
                        browse.Enabled = true;
                    });
                }
                catch (Exception ex)
                {
                    exitCode = 1;
                    AppendOutput(output, "Erro: " + ex.Message + "\r\n");
                    form.BeginInvoke((MethodInvoker)delegate
                    {
                        MessageBox.Show(form, ex.Message, "Erro no instalador", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        install.Enabled = true;
                        browse.Enabled = true;
                    });
                }
            });
        };
        form.Controls.Add(install);

        openFolder.Click += delegate
        {
            if (Directory.Exists(pathBox.Text))
            {
                Process.Start("explorer.exe", pathBox.Text);
            }
        };
        form.Controls.Add(openFolder);

        close.Click += delegate { form.Close(); };
        form.Controls.Add(close);

        Application.Run(form);
        return exitCode;
    }

    private static int RunPowerShellInstall(string minecraftDir, string[] args, TextBox output)
    {
        string scriptPath = ExtractEmbeddedInstaller();
        if (scriptPath == null)
        {
            scriptPath = FindInstallerScript();
        }

        if (scriptPath == null)
        {
            throw new FileNotFoundException("Nao encontrei o instalador embutido nem scripts\\" + ScriptName + ".");
        }

        string powershellArgs = "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File "
                + QuoteArgument(scriptPath)
                + " -NoGui -MinecraftDir "
                + QuoteArgument(minecraftDir);

        string packageMinecraftDir = FindPackageMinecraftDir(Path.GetDirectoryName(scriptPath));
        if (!string.IsNullOrEmpty(packageMinecraftDir))
        {
            powershellArgs += " -PackageMinecraftDir " + QuoteArgument(packageMinecraftDir);
        }

        string extractedForge = Path.Combine(Path.GetDirectoryName(scriptPath), "payload", "forge", ForgeInstallerName);
        if (File.Exists(extractedForge))
        {
            powershellArgs += " -ForgeInstallerPath " + QuoteArgument(extractedForge);
        }

        string forwardedArgs = string.Join(" ", args.Select(QuoteArgument).ToArray());
        if (!string.IsNullOrWhiteSpace(forwardedArgs))
        {
            powershellArgs += " " + forwardedArgs;
        }

        using (Process process = Process.Start(new ProcessStartInfo
        {
            FileName = "powershell.exe",
            Arguments = powershellArgs,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            WindowStyle = ProcessWindowStyle.Hidden,
            WorkingDirectory = Path.GetDirectoryName(scriptPath)
        }))
        {
            process.OutputDataReceived += delegate(object sender, DataReceivedEventArgs eventArgs)
            {
                if (eventArgs.Data != null)
                {
                    AppendOutput(output, eventArgs.Data + "\r\n");
                }
            };
            process.ErrorDataReceived += delegate(object sender, DataReceivedEventArgs eventArgs)
            {
                if (eventArgs.Data != null)
                {
                    AppendOutput(output, eventArgs.Data + "\r\n");
                }
            };
            process.BeginOutputReadLine();
            process.BeginErrorReadLine();
            process.WaitForExit();
            return process.ExitCode;
        }
    }

    private static Button CreateButton(string text, int left, int top, int width, int height, Color backColor)
    {
        Button button = new Button();
        button.Left = left;
        button.Top = top;
        button.Width = width;
        button.Height = height;
        button.Text = text;
        button.FlatStyle = FlatStyle.Flat;
        button.BackColor = backColor;
        button.ForeColor = Color.White;
        return button;
    }

    private static void AppendOutput(TextBox output, string text)
    {
        if (output.IsDisposed)
        {
            return;
        }

        if (output.InvokeRequired)
        {
            output.BeginInvoke((MethodInvoker)delegate { AppendOutput(output, text); });
            return;
        }

        output.AppendText(text);
    }

    private static Image LoadEmbeddedImage(string resourceName)
    {
        Assembly assembly = Assembly.GetExecutingAssembly();
        using (Stream input = assembly.GetManifestResourceStream(resourceName))
        {
            if (input == null)
            {
                return null;
            }

            MemoryStream copy = new MemoryStream();
            input.CopyTo(copy);
            copy.Position = 0;
            return Image.FromStream(copy);
        }
    }

    private static string FindMinecraftDir()
    {
        string appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        string userProfile = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        string[] candidates =
        {
            Path.Combine(appData, ".minecraft"),
            Path.Combine(appData, "TLauncher", ".minecraft"),
            Path.Combine(appData, ".tlauncher", ".minecraft"),
            Path.Combine(userProfile, "AppData", "Roaming", ".minecraft")
        };

        foreach (string candidate in candidates.Distinct())
        {
            if (Directory.Exists(candidate))
            {
                return Path.GetFullPath(candidate);
            }
        }

        string defaultPath = Path.Combine(appData, ".minecraft");
        Directory.CreateDirectory(defaultPath);
        return Path.GetFullPath(defaultPath);
    }

    private static string InstallerOverview()
    {
        return "O que sera instalado:\r\n"
                + "- Forge 1.20.1-47.4.10.\r\n"
                + "- Magic World all-in-one em .minecraft\\mods.\r\n"
                + "- Mods externos necessarios que ainda nao podem ficar dentro do all-in-one.\r\n"
                + "- Resource Packs e Shader Packs do pacote distribuivel.\r\n"
                + "- JourneyMap configurado sem beacons/linhas 3D visiveis.\r\n\r\n"
                + "Modo FULL: o pacote .minecraft vai embutido dentro deste EXE.\r\n"
                + "Remove apenas Magic World antigo e conflitos conhecidos: TL Cape, Controllable, EMF/ETF, Fusion, CIT, ModernFix e FerriteCore.\r\n";
    }

    private static string ExtractEmbeddedInstaller()
    {
        Assembly assembly = Assembly.GetExecutingAssembly();
        if (assembly.GetManifestResourceStream(ScriptName) == null)
        {
            return null;
        }

        FileInfo exeInfo = new FileInfo(Application.ExecutablePath);
        string extractionRoot = Path.Combine(
                Path.GetTempPath(),
                "MagicWorldForgeInstaller",
                assembly.GetName().Version + "-" + exeInfo.Length + "-" + exeInfo.LastWriteTimeUtc.Ticks
        );

        Directory.CreateDirectory(extractionRoot);
        Directory.CreateDirectory(Path.Combine(extractionRoot, "screenshots"));
        Directory.CreateDirectory(Path.Combine(extractionRoot, "payload", "forge"));

        ExtractResource(assembly, ScriptName, Path.Combine(extractionRoot, ScriptName));
        ExtractResourceIfPresent(assembly, "banner_installer.png", Path.Combine(extractionRoot, "screenshots", "banner_installer.png"));
        ExtractResourceIfPresent(assembly, "forge-installer.jar", Path.Combine(extractionRoot, "payload", "forge", ForgeInstallerName));
        ExtractAppendedPayloadIfPresent(extractionRoot);
        return Path.Combine(extractionRoot, ScriptName);
    }

    private static void ExtractAppendedPayloadIfPresent(string extractionRoot)
    {
        string packageDir = Path.Combine(extractionRoot, "payload", ".minecraft");
        string forgeInstaller = Path.Combine(extractionRoot, "payload", "forge", ForgeInstallerName);
        if (Directory.Exists(packageDir) && File.Exists(forgeInstaller))
        {
            return;
        }

        using (FileStream executable = File.OpenRead(Application.ExecutablePath))
        {
            if (executable.Length < FullPayloadMarker.Length + sizeof(long))
            {
                return;
            }

            executable.Position = executable.Length - FullPayloadMarker.Length;
            byte[] marker = new byte[FullPayloadMarker.Length];
            executable.Read(marker, 0, marker.Length);
            if (!marker.SequenceEqual(FullPayloadMarker))
            {
                return;
            }

            executable.Position = executable.Length - FullPayloadMarker.Length - sizeof(long);
            byte[] lengthBytes = new byte[sizeof(long)];
            executable.Read(lengthBytes, 0, lengthBytes.Length);
            long payloadLength = BitConverter.ToInt64(lengthBytes, 0);
            long payloadStart = executable.Length - FullPayloadMarker.Length - sizeof(long) - payloadLength;
            if (payloadLength <= 0 || payloadStart < 0)
            {
                throw new InvalidDataException("Payload FULL embutido invalido.");
            }

            string payloadZip = Path.Combine(extractionRoot, "magicworld-full-payload.zip");
            if (!File.Exists(payloadZip) || new FileInfo(payloadZip).Length != payloadLength)
            {
                executable.Position = payloadStart;
                using (FileStream output = File.Create(payloadZip))
                {
                    CopyBytes(executable, output, payloadLength);
                }
            }

            ZipFile.ExtractToDirectory(payloadZip, extractionRoot);
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
                throw new EndOfStreamException("Fim inesperado ao extrair payload FULL.");
            }
            output.Write(buffer, 0, read);
            remaining -= read;
        }
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

    private static void ExtractResourceIfPresent(Assembly assembly, string resourceName, string destination)
    {
        if (assembly.GetManifestResourceStream(resourceName) == null)
        {
            return;
        }

        ExtractResource(assembly, resourceName, destination);
    }

    private static string FindInstallerScript()
    {
        string exeDir = AppDomain.CurrentDomain.BaseDirectory;
        string[] candidates =
        {
            Path.Combine(exeDir, ScriptName),
            Path.Combine(exeDir, "scripts", ScriptName),
            Path.Combine(exeDir, "..", "scripts", ScriptName),
            Path.Combine(Environment.CurrentDirectory, "scripts", ScriptName)
        };

        return candidates.Select(Path.GetFullPath).FirstOrDefault(File.Exists);
    }

    private static string FindPackageMinecraftDir(string extractionRoot)
    {
        if (!string.IsNullOrEmpty(extractionRoot))
        {
            string embeddedPackage = Path.Combine(extractionRoot, "payload", ".minecraft");
            if (Directory.Exists(embeddedPackage))
            {
                return Path.GetFullPath(embeddedPackage);
            }
        }

        string exeDir = AppDomain.CurrentDomain.BaseDirectory;
        string[] candidates =
        {
            Path.Combine(exeDir, "pacote_distribuivel", ".minecraft"),
            Path.Combine(exeDir, "..", "pacote_distribuivel", ".minecraft"),
            Path.Combine(Environment.CurrentDirectory, "pacote_distribuivel", ".minecraft")
        };

        return candidates.Select(Path.GetFullPath).FirstOrDefault(Directory.Exists);
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
