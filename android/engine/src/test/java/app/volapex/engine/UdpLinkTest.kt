package app.volapex.engine

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UdpLinkTest {
    @Test
    fun `procura em todos os enderecos e depois fala so com quem respondeu`() {
        DatagramSocket(0, InetAddress.getLoopbackAddress()).use { pc ->
            pc.soTimeout = 2000
            val link = UdpLink(listOf("127.0.0.1"), pc.localPort)
            link.open()

            val hello = ByteArray(32) { 1 }
            link.sendToCandidates(hello, hello.size)
            val received = DatagramPacket(ByteArray(64), 64)
            pc.receive(received)
            assertContentEquals(hello, received.data.copyOf(received.length))

            // O PC responde para quem enviou; o link registra o remetente.
            pc.send(DatagramPacket(ByteArray(32) { 2 }, 32, received.socketAddress))
            val buffer = ByteArray(64)
            assertEquals(32, link.receive(buffer))
            link.selectPeer(assertNotNull(link.lastSender))

            val state = ByteArray(32) { 3 }
            link.send(state, state.size)
            pc.receive(received)
            assertContentEquals(state, received.data.copyOf(received.length))
            link.close()
        }
    }

    @Test
    fun `receber sem nada chegando devolve -1`() {
        DatagramSocket(0, InetAddress.getLoopbackAddress()).use { pc ->
            val link = UdpLink(listOf("127.0.0.1"), pc.localPort)
            link.open()
            assertEquals(-1, link.receive(ByteArray(64)))
            link.close()
        }
    }
}
