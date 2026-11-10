package app.virtualwheelracing.desktop.setup

import app.virtualwheelracing.desktop.ui.Strings
import app.virtualwheelracing.protocol.ProtocolDefaults
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64
import java.util.concurrent.TimeUnit

/** Resultado de uma ação de configuração do Windows. */
sealed interface SetupResult {
    data object Done : SetupResult
    data object Cancelled : SetupResult
    data class Failed(val reason: String) : SetupResult
}

/**
 * Prepara o Windows para o Virtual Wheel Racing: instala o vJoy que vem junto, configura o dispositivo 1
 * (eixos X, Y, Z e 16 botões) e libera a porta UDP no firewall para a rede privada.
 * Cada ação pede permissão de administrador pelo aviso do Windows (UAC), com consentimento do usuário.
 */
class WindowsSetup(
    private val resourcesDir: Path? = System.getProperty("compose.application.resources.dir")?.let(Paths::get),
    private val vjoyDir: Path = Paths.get(System.getenv("ProgramFiles") ?: "C:\\Program Files", "vJoy", "x64"),
) {
    val bundledVJoyInstaller: Path?
        get() = resourcesDir?.resolve("vJoySetup.exe")?.takeIf(Files::exists)

    fun installVJoy(): SetupResult {
        val installer = bundledVJoyInstaller ?: return SetupResult.Failed(Strings.current.errNoBundledVJoy)
        return runElevated(installer.toString(), "")
    }

    /** Recria o dispositivo 1 com os eixos e botões que o Virtual Wheel Racing usa. */
    fun configureVJoyDevice(): SetupResult {
        val config = vjoyDir.resolve("vJoyConfig.exe")
        if (!Files.exists(config)) return SetupResult.Failed(Strings.current.errNoVJoyConfig)
        return runElevated(config.toString(), "1 -f -a x y z -b 16")
    }

    fun firewallRuleExists(): Boolean = try {
        val process = ProcessBuilder("netsh", "advfirewall", "firewall", "show", "rule", "name=$RULE_NAME")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0 && output.contains(RULE_NAME)
    } catch (e: Exception) {
        false
    }

    /** Libera a porta só para redes privadas e de domínio, e só para este programa. */
    fun addFirewallRule(): SetupResult {
        val program = ProcessHandle.current().info().command().orElse(null)
            ?: return SetupResult.Failed(Strings.current.errNoProgramPath)
        val args = "advfirewall firewall add rule name=$RULE_NAME dir=in action=allow protocol=UDP " +
            "localport=${ProtocolDefaults.PORT} profile=private,domain program=\"$program\""
        return runElevated("netsh.exe", args)
    }

    private fun runElevated(file: String, arguments: String): SetupResult {
        val argumentList = if (arguments.isBlank()) "" else " -ArgumentList ${psQuote(arguments)}"
        val script = "try { \$p = Start-Process -FilePath ${psQuote(file)}$argumentList -Verb RunAs -Wait -PassThru " +
            "-WindowStyle Hidden; exit \$p.ExitCode } catch { exit $CANCELLED_EXIT }"
        // O script vai codificado (UTF-16LE em base64): aspas e espaços no caminho do programa
        // não se perdem na linha de comando, como acontecia ao passá-lo com -Command.
        val encoded = Base64.getEncoder().encodeToString(script.toByteArray(Charsets.UTF_16LE))
        return try {
            val process = ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand", encoded)
                .redirectErrorStream(true)
                .start()
            process.inputStream.bufferedReader().readText()
            if (!process.waitFor(10, TimeUnit.MINUTES)) {
                process.destroy()
                return SetupResult.Failed(Strings.current.errTimeout)
            }
            when (process.exitValue()) {
                0 -> SetupResult.Done
                CANCELLED_EXIT -> SetupResult.Cancelled
                else -> SetupResult.Failed(Strings.current.errExitCode(process.exitValue()))
            }
        } catch (e: Exception) {
            SetupResult.Failed(e.message ?: Strings.current.errElevation)
        }
    }

    private fun psQuote(value: String) = "'" + value.replace("'", "''") + "'"

    private companion object {
        const val RULE_NAME = "VirtualWheelRacing" // sem espaços: o netsh não aceita nome com espaço sem aspas
        const val CANCELLED_EXIT = 1223 // mesmo código do Windows para "cancelado pelo usuário"
    }
}
