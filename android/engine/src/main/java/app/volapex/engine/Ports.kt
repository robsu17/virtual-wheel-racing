package app.volapex.engine

import android.os.Handler
import app.volapex.domain.WheelState
import java.io.IOException

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

/** Para onde os pacotes vão. Só transporte: formato e assinatura ficam no protocolo. */
interface PacketSink : AutoCloseable {
    @Throws(IOException::class)
    fun open()

    @Throws(IOException::class)
    fun send(buffer: ByteArray, length: Int)
}

/** Mantém a rede em modo de baixa latência enquanto se dirige. */
interface NetworkLock {
    fun acquire()
    fun release()
}
