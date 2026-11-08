package app.virtualwheelracing.engine

import android.os.Handler
import app.virtualwheelracing.domain.WheelState
import java.io.IOException
import java.net.SocketAddress

/** Recebe a orientação na thread do motor. Parâmetros primitivos: nada é criado por amostra. */
interface OrientationListener {
    fun onOrientation(w: Float, x: Float, y: Float, z: Float, timestampNanos: Long)
}

/** Fonte de orientação do celular. Em produção, o sensor; nos testes, uma gravação. */
interface OrientationSource {
    /** Começa a entregar amostras em [handler]. Devolve false se o sensor não existir. */
    fun start(listener: OrientationListener, handler: Handler): Boolean
    fun stop()
}

/** Pedais e botões, lidos a cada pacote. */
interface ControlInput {
    fun readInto(state: WheelState)
}

/**
 * Canal de rede com o PC. Só transporte: formato, assinatura e sessão ficam no protocolo.
 * Enquanto procura o PC, envia para todos os endereços do QR; depois, só para o que respondeu.
 */
interface PacketLink : AutoCloseable {
    @Throws(IOException::class)
    fun open()

    /** Envia para todos os endereços candidatos (usado no HELLO). */
    @Throws(IOException::class)
    fun sendToCandidates(buffer: ByteArray, length: Int)

    /** Passa a enviar só para este endereço (o PC que respondeu). */
    fun selectPeer(address: SocketAddress)

    @Throws(IOException::class)
    fun send(buffer: ByteArray, length: Int)

    /** Espera um pacote por um curto intervalo. Devolve o tamanho, ou -1 se nada chegou. */
    @Throws(IOException::class)
    fun receive(buffer: ByteArray): Int

    /** Quem enviou o último pacote recebido. */
    val lastSender: SocketAddress?
}

/** Mantém a rede em modo de baixa latência enquanto se dirige. */
interface NetworkLock {
    fun acquire()
    fun release()
}
