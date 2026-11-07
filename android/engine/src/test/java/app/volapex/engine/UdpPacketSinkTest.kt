package app.volapex.engine

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UdpPacketSinkTest {
    @Test
    fun `envia para a porta do PC e nao para a porta do proprio socket`() {
        DatagramSocket(0, InetAddress.getLoopbackAddress()).use { pc ->
            pc.soTimeout = 2000
            val sink = UdpPacketSink("127.0.0.1", pc.localPort)
            sink.open()
            val payload = ByteArray(32) { it.toByte() }
            sink.send(payload, payload.size)
            sink.send(payload, payload.size)

            val received = DatagramPacket(ByteArray(64), 64)
            pc.receive(received)
            assertEquals(32, received.length)
            assertContentEquals(payload, received.data.copyOf(received.length))
            sink.close()
        }
    }
}
