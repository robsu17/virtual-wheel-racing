package app.virtualwheelracing.desktop.ui

import java.util.Locale

/**
 * Textos do programa do Windows. Inglês é o padrão; português quando o Windows estiver em português.
 * Para testar outro idioma: `-Dvwr.lang=en` ou `-Dvwr.lang=pt`.
 */
abstract class Strings {
    // Comum
    abstract val appNamePrefix: String
    abstract val technicalDetails: String
    abstract val backToSimple: String

    // Tela simples
    abstract val stepPcReady: String
    abstract val stepPcReadyDone: String
    abstract val stepPcReadyTodo: String
    abstract val stepConnect: String
    abstract val stepConnected: String
    abstract val stepNow: String
    abstract val stepLater: String
    abstract val stepPlay: String
    abstract val stepPlaySubtitle: String
    abstract val noAppTitle: String
    abstract val noAppBody: String
    abstract val howToDownload: String
    abstract val notConnectingTitle: String
    abstract val notConnectingTips: List<String>
    abstract val prepareTitle: String
    abstract val prepareBody: String
    abstract val taskVJoy: String
    abstract val taskFirewall: String
    abstract val taskPort: String
    abstract val prepareButton: String
    abstract val confirmWindowsPrompt: String
    abstract val seeTechnicalDetails: String
    abstract val installing: String
    abstract val configuring: String
    abstract val openingFirewall: String
    abstract fun cancelled(what: String): String
    abstract fun failed(what: String, reason: String): String
    abstract val whatInstallVJoy: String
    abstract val whatConfigureVJoy: String
    abstract val whatFirewall: String
    abstract val vjoyNeedsRestart: String
    abstract val vjoyInUse: String
    abstract val pointPhoneHere: String
    abstract val openAppAndTapStart: String
    abstract val scanButtonName: String
    abstract val noNetworkSimple: String
    abstract val waitingPhone: String
    abstract val pairedBefore: String
    abstract val connectedTitle: String
    abstract val openYourGame: String
    abstract fun phone(name: String): String
    abstract val firstTimeHint: String

    // Detalhes técnicos
    abstract val sectionDashboard: String
    abstract val sectionConnect: String
    abstract val sectionCheck: String
    abstract val phonePanel: String
    abstract fun pairedWaiting(peer: String): String
    abstract val waitingForPhone: String
    abstract fun connectedTo(peer: String?): String
    abstract val unstableSignal: String
    abstract val noSignal: String
    abstract val dashboardPhoneHint: String
    abstract val virtualWheel: String
    abstract val vjoyActive: String
    abstract val vjoyUnavailable: String
    abstract val axesSummary: String
    abstract val configureInGames: String
    abstract val liveInputs: String
    abstract val axisSteering: String
    abstract val axisThrottle: String
    abstract val axisBrake: String
    abstract fun buttonsPressed(list: String?): String
    abstract val packetGaps: String
    abstract val packetsPerSecond: String
    abstract val typical: String
    abstract val biggestGap: String
    abstract val lost: String
    abstract val rejected: String
    abstract val gapsNote: String
    abstract val connectTitle: String
    abstract val connectSubtitle: String
    abstract val qrCode: String
    abstract val noNetwork: String
    abstract val thisPc: String
    abstract val name: String
    abstract val localAddresses: String
    abstract fun portNote(port: Int): String
    abstract val rememberNote: String
    abstract val newCode: String
    abstract fun fixDone(label: String): String
    abstract fun fixCancelled(label: String): String
    abstract val checkTitle: String
    abstract val checkSubtitle: String
    abstract val items: String
    abstract val checkDriver: String
    abstract val fixInstallVJoy: String
    abstract val checkAxes: String
    abstract val fixConfigure: String
    abstract val labelConfigure: String
    abstract val checkDevice: String
    abstract val fixRetry: String
    abstract val labelReopen: String
    abstract val checkFirewall: String
    abstract val fixFirewall: String
    abstract fun receivingOnPort(port: Int): String
    abstract val adminNote: String
    abstract fun busyConfirm(label: String): String
    abstract val qrDescription: String

    // Mensagens do Windows
    abstract val errNoBundledVJoy: String
    abstract val errNoVJoyConfig: String
    abstract val errNoProgramPath: String
    abstract val errTimeout: String
    abstract fun errExitCode(code: Int): String
    abstract val errElevation: String
    abstract fun errPort(port: Int, message: String?): String

    companion object {
        val current: Strings by lazy {
            val lang = System.getProperty("vwr.lang") ?: Locale.getDefault().language
            if (lang == "pt") PtStrings else EnStrings
        }
    }
}

object EnStrings : Strings() {
    override val appNamePrefix = "Virtual Wheel "
    override val technicalDetails = "Technical details"
    override val backToSimple = "Back to simple view"

    override val stepPcReady = "PC ready"
    override val stepPcReadyDone = "Everything installed"
    override val stepPcReadyTodo = "Needs setup"
    override val stepConnect = "Connect your phone"
    override val stepConnected = "Connected"
    override val stepNow = "Now"
    override val stepLater = "Next"
    override val stepPlay = "Play"
    override val stepPlaySubtitle = "Open your game"
    override val noAppTitle = "Don't have the app yet?"
    override val noAppBody = "Get Virtual Wheel Racing on your Android phone and come back to this screen."
    override val howToDownload = "How to get it on your phone"
    override val notConnectingTitle = "Not connecting?"
    override val notConnectingTips = listOf(
        "Phone and PC must be on the same Wi-Fi.",
        "Keep this program open while you play.",
        "If Windows shows a prompt, click Yes or Allow.",
    )
    override val prepareTitle = "Let's set up your PC"
    override val prepareBody = "One click installs the virtual wheel and allows the phone to connect. Windows will ask for your permission."
    override val taskVJoy = "Virtual wheel (vJoy)"
    override val taskFirewall = "Connection allowed in the firewall"
    override val taskPort = "Connection port available"
    override val prepareButton = "Set up my PC"
    override val confirmWindowsPrompt = "Confirm the Windows prompt if it appears."
    override val seeTechnicalDetails = "See technical details"
    override val installing = "Installing the virtual wheel…"
    override val configuring = "Configuring the virtual wheel…"
    override val openingFirewall = "Allowing the connection…"
    override fun cancelled(what: String) = "$what was cancelled in the Windows prompt. Try again and click Yes."
    override fun failed(what: String, reason: String) = "$what didn't work: $reason"
    override val whatInstallVJoy = "Installing vJoy"
    override val whatConfigureVJoy = "Configuring vJoy"
    override val whatFirewall = "Allowing the firewall"
    override val vjoyNeedsRestart = "vJoy was installed but isn't responding yet. Restart the PC and open the program again."
    override val vjoyInUse = "The virtual wheel is in use by another program. Close it and try again."
    override val pointPhoneHere = "Point your phone here"
    override val openAppAndTapStart = "Open the Virtual Wheel Racing app and tap "
    override val scanButtonName = "Scan the PC code"
    override val noNetworkSimple = "This PC isn't on any network. Connect it to Wi-Fi or a network cable and open the program again."
    override val waitingPhone = "Waiting for the phone…"
    override val pairedBefore = "Paired before? Just open the app and tap Play."
    override val connectedTitle = "Connected!"
    override val openYourGame = "Open your game and drive."
    override fun phone(name: String) = "Phone: $name"
    override val firstTimeHint = "First time in this game? On the phone, tap Set up my game."

    override val sectionDashboard = "Dashboard"
    override val sectionConnect = "Connect phone"
    override val sectionCheck = "PC check"
    override val phonePanel = "Phone"
    override fun pairedWaiting(peer: String) = "Paired ($peer), waiting for data"
    override val waitingForPhone = "Waiting for the phone"
    override fun connectedTo(peer: String?) = "Connected to ${peer ?: "phone"}"
    override val unstableSignal = "Unstable signal"
    override val noSignal = "No signal · failsafe active"
    override val dashboardPhoneHint = "Open Virtual Wheel Racing on the phone and tap Play."
    override val virtualWheel = "Virtual wheel"
    override val vjoyActive = "vJoy Device · active"
    override val vjoyUnavailable = "vJoy unavailable"
    override val axesSummary = "X axis steering · Y throttle · Z brake · 16 buttons"
    override val configureInGames = "Set up the controls in each game's options."
    override val liveInputs = "Live inputs"
    override val axisSteering = "Steering"
    override val axisThrottle = "Throttle"
    override val axisBrake = "Brake"
    override fun buttonsPressed(list: String?) = "Buttons pressed: ${list ?: "none"}"
    override val packetGaps = "Packet intervals"
    override val packetsPerSecond = "Packets/s"
    override val typical = "Typical (p50)"
    override val biggestGap = "Biggest gap"
    override val lost = "Lost"
    override val rejected = "Rejected"
    override val gapsNote = "Measures how regularly packets arrive, not the total delay from phone to game."
    override val connectTitle = "Connect a phone"
    override val connectSubtitle = "In the Virtual Wheel Racing Android app, tap Scan the PC code (or Settings > Change PC) and point the camera at this code."
    override val qrCode = "QR code"
    override val noNetwork = "No local network found. Connect the PC to Wi-Fi or a network cable."
    override val thisPc = "This PC"
    override val name = "Name"
    override val localAddresses = "Local network addresses"
    override fun portNote(port: Int) = "Port $port · phone and PC on the same Wi-Fi"
    override val rememberNote = "The phone remembers this PC. Creating a new code disconnects paired phones."
    override val newCode = "Create new code"
    override fun fixDone(label: String) = "$label: done."
    override fun fixCancelled(label: String) = "$label: cancelled in the Windows prompt."
    override val checkTitle = "PC check"
    override val checkSubtitle = "Everything must be OK for the virtual wheel to work in games."
    override val items = "Items"
    override val checkDriver = "vJoy driver installed"
    override val fixInstallVJoy = "Install vJoy"
    override val checkAxes = "Device 1 with X, Y, Z axes and 16 buttons"
    override val fixConfigure = "Configure automatically"
    override val labelConfigure = "Configure vJoy"
    override val checkDevice = "Virtual wheel ready for Virtual Wheel Racing"
    override val fixRetry = "Try again"
    override val labelReopen = "Reopen the virtual wheel"
    override val checkFirewall = "Firewall allowed for the private network"
    override val fixFirewall = "Allow in firewall"
    override fun receivingOnPort(port: Int) = "Receiving on port $port"
    override val adminNote = "Fixes ask for administrator permission through the Windows prompt."
    override fun busyConfirm(label: String) = "$label… confirm the Windows prompt."
    override val qrDescription = "Pairing QR code"

    override val errNoBundledVJoy = "The vJoy installer was not bundled with Virtual Wheel Racing."
    override val errNoVJoyConfig = "vJoyConfig.exe not found. Install vJoy first."
    override val errNoProgramPath = "Could not find the program's path."
    override val errTimeout = "The operation took too long."
    override fun errExitCode(code: Int) = "Windows returned code $code."
    override val errElevation = "Error asking for administrator permission."
    override fun errPort(port: Int, message: String?) = "Could not open port $port: $message"
}

object PtStrings : Strings() {
    override val appNamePrefix = "Virtual Wheel "
    override val technicalDetails = "Detalhes técnicos"
    override val backToSimple = "Voltar à tela simples"

    override val stepPcReady = "PC pronto"
    override val stepPcReadyDone = "Tudo instalado"
    override val stepPcReadyTodo = "Falta preparar"
    override val stepConnect = "Conectar o celular"
    override val stepConnected = "Conectado"
    override val stepNow = "Agora"
    override val stepLater = "Depois"
    override val stepPlay = "Jogar"
    override val stepPlaySubtitle = "Abrir seu jogo"
    override val noAppTitle = "Ainda não tem o app?"
    override val noAppBody = "Baixe o Virtual Wheel Racing no celular Android e volte para esta tela."
    override val howToDownload = "Como baixar no celular"
    override val notConnectingTitle = "Não está conectando?"
    override val notConnectingTips = listOf(
        "O celular e o PC precisam estar no mesmo Wi-Fi.",
        "Deixe este programa aberto enquanto joga.",
        "Se aparecer um aviso do Windows, clique em Sim ou Permitir.",
    )
    override val prepareTitle = "Vamos preparar seu PC"
    override val prepareBody = "Um clique instala o volante virtual e libera a conexão com o celular. O Windows vai pedir sua permissão."
    override val taskVJoy = "Volante virtual (vJoy)"
    override val taskFirewall = "Conexão liberada no firewall"
    override val taskPort = "Porta de conexão livre"
    override val prepareButton = "Preparar meu PC"
    override val confirmWindowsPrompt = "Confirme no aviso do Windows, se aparecer."
    override val seeTechnicalDetails = "Ver detalhes técnicos"
    override val installing = "Instalando o volante virtual…"
    override val configuring = "Configurando o volante virtual…"
    override val openingFirewall = "Liberando a conexão…"
    override fun cancelled(what: String) = "$what foi cancelado no aviso do Windows. Tente de novo e clique em Sim."
    override fun failed(what: String, reason: String) = "$what não deu certo: $reason"
    override val whatInstallVJoy = "A instalação do vJoy"
    override val whatConfigureVJoy = "A configuração do vJoy"
    override val whatFirewall = "A liberação no firewall"
    override val vjoyNeedsRestart = "O vJoy foi instalado, mas ainda não respondeu. Reinicie o PC e abra o programa de novo."
    override val vjoyInUse = "O volante virtual está em uso por outro programa. Feche-o e tente de novo."
    override val pointPhoneHere = "Aponte o celular para cá"
    override val openAppAndTapStart = "Abra o app Virtual Wheel Racing e toque em "
    override val scanButtonName = "Escanear o código do PC"
    override val noNetworkSimple = "Este PC não está em nenhuma rede. Conecte-o ao Wi-Fi ou ao cabo de rede e abra o programa de novo."
    override val waitingPhone = "Esperando o celular…"
    override val pairedBefore = "Já pareou antes? É só abrir o app e tocar em Jogar."
    override val connectedTitle = "Conectado!"
    override val openYourGame = "Abra seu jogo e acelere."
    override fun phone(name: String) = "Celular: $name"
    override val firstTimeHint = "Primeira vez nesse jogo? No celular, toque em Configurar meu jogo."

    override val sectionDashboard = "Painel"
    override val sectionConnect = "Conectar celular"
    override val sectionCheck = "Verificação do PC"
    override val phonePanel = "Celular"
    override fun pairedWaiting(peer: String) = "Pareado ($peer), aguardando dados"
    override val waitingForPhone = "Aguardando o celular"
    override fun connectedTo(peer: String?) = "Conectado a ${peer ?: "celular"}"
    override val unstableSignal = "Sinal instável"
    override val noSignal = "Sem sinal · failsafe ativo"
    override val dashboardPhoneHint = "Abra o Virtual Wheel Racing no celular e toque em Jogar."
    override val virtualWheel = "Volante virtual"
    override val vjoyActive = "vJoy Device · ativo"
    override val vjoyUnavailable = "vJoy indisponível"
    override val axesSummary = "Eixo X direção · Y acelerador · Z freio · 16 botões"
    override val configureInGames = "Configure os controles nas opções de cada jogo."
    override val liveInputs = "Entradas ao vivo"
    override val axisSteering = "Direção"
    override val axisThrottle = "Acelerador"
    override val axisBrake = "Freio"
    override fun buttonsPressed(list: String?) = "Botões pressionados: ${list ?: "nenhum"}"
    override val packetGaps = "Intervalo entre pacotes"
    override val packetsPerSecond = "Pacotes/s"
    override val typical = "Típico (p50)"
    override val biggestGap = "Maior falha"
    override val lost = "Perdidos"
    override val rejected = "Rejeitados"
    override val gapsNote = "Mede a regularidade da chegada, não o atraso total do celular até o jogo."
    override val connectTitle = "Conectar um celular"
    override val connectSubtitle = "No app Virtual Wheel Racing do Android, toque em Escanear o código do PC (ou em Ajustes > Trocar de PC) e aponte a câmera para este código."
    override val qrCode = "Código QR"
    override val noNetwork = "Nenhuma rede local encontrada. Conecte o PC ao Wi-Fi ou ao cabo de rede."
    override val thisPc = "Este PC"
    override val name = "Nome"
    override val localAddresses = "Endereços na rede local"
    override fun portNote(port: Int) = "Porta $port · celular e PC na mesma rede Wi-Fi"
    override val rememberNote = "O celular lembra deste PC. Gerar outro código desconecta os celulares pareados."
    override val newCode = "Gerar novo código"
    override fun fixDone(label: String) = "$label: concluído."
    override fun fixCancelled(label: String) = "$label: cancelado no aviso do Windows."
    override val checkTitle = "Verificação do PC"
    override val checkSubtitle = "Tudo precisa estar OK para o volante virtual funcionar nos jogos."
    override val items = "Itens"
    override val checkDriver = "Driver vJoy instalado"
    override val fixInstallVJoy = "Instalar vJoy"
    override val checkAxes = "Dispositivo 1 com eixos X, Y, Z e 16 botões"
    override val fixConfigure = "Configurar automaticamente"
    override val labelConfigure = "Configurar o vJoy"
    override val checkDevice = "Volante virtual pronto para o Virtual Wheel Racing"
    override val fixRetry = "Tentar de novo"
    override val labelReopen = "Reabrir o volante virtual"
    override val checkFirewall = "Firewall liberado para a rede privada"
    override val fixFirewall = "Liberar no firewall"
    override fun receivingOnPort(port: Int) = "Recebendo na porta $port"
    override val adminNote = "As correções pedem permissão de administrador pelo aviso do Windows."
    override fun busyConfirm(label: String) = "$label… confirme no aviso do Windows."
    override val qrDescription = "Código QR de pareamento"

    override val errNoBundledVJoy = "O instalador do vJoy não veio junto com o Virtual Wheel Racing."
    override val errNoVJoyConfig = "vJoyConfig.exe não encontrado. Instale o vJoy primeiro."
    override val errNoProgramPath = "Não foi possível descobrir o caminho do programa."
    override val errTimeout = "A operação demorou demais."
    override fun errExitCode(code: Int) = "O Windows devolveu o código $code."
    override val errElevation = "Erro ao pedir permissão de administrador."
    override fun errPort(port: Int, message: String?) = "Não foi possível abrir a porta $port: $message"
}
