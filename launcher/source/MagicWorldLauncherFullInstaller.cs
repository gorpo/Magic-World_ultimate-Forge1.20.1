using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Linq;
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

                    Directory.CreateDirectory(installDir);
                    ExtractPayloadZip(installDir);

                    status.Text = "Criando atalho de abertura...";
                    progress.Value = 80;
                    Application.DoEvents();

                    CreateDesktopShortcut(installDir);

                    progress.Value = 100;
                    status.Text = "Magic World Launcher instalado. Abra pelo atalho da area de trabalho e clique em Instalar Magic World.";
                    close.Enabled = true;

                    string launcherCmd = Path.Combine(installDir, "Abrir Magic World Launcher.cmd");
                    if (File.Exists(launcherCmd))
                    {
                        Process.Start(new ProcessStartInfo
                        {
                            FileName = launcherCmd,
                            WorkingDirectory = installDir,
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

    private static void CreateDesktopShortcut(string installDir)
    {
        string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
        string shortcut = Path.Combine(desktop, "Magic World Launcher.cmd");
        string launcher = Path.Combine(installDir, "Abrir Magic World Launcher.cmd");
        File.WriteAllText(
            shortcut,
            "@echo off\r\ncd /d \"" + installDir + "\"\r\ncall \"" + launcher + "\"\r\n",
            Encoding.ASCII
        );
    }
}
