package app.virtualwheelracing.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WheelStateTest {
    @Test
    fun `valores fora da faixa sao limitados e NaN vira zero`() {
        val state = WheelState()
        state.steering = 3f
        state.throttle = -1f
        state.brake = Float.NaN
        assertEquals(1f, state.steering)
        assertEquals(0f, state.throttle)
        assertEquals(0f, state.brake)
    }

    @Test
    fun `botoes ligam e desligam por indice`() {
        val state = WheelState()
        state.setButton(0, true)
        state.setButton(15, true)
        state.setButton(16, true)
        assertEquals(0x8001, state.buttons)
        state.setButton(0, false)
        assertFalse(state.isPressed(0))
        assertTrue(state.isPressed(15))
    }
}

class OneEuroFilterTest {
    @Test
    fun `primeira amostra passa direto e valor constante se mantem`() {
        val filter = OneEuroFilter()
        assertEquals(10f, filter.process(10f, 0L))
        var t = 0L
        repeat(100) {
            t += 5_000_000L
            assertEquals(10f, filter.process(10f, t), 0.0001f)
        }
    }

    @Test
    fun `degrau e alcancado em menos de meio segundo`() {
        val filter = OneEuroFilter()
        filter.process(0f, 0L)
        var t = 0L
        var out = 0f
        repeat(100) {
            t += 5_000_000L
            out = filter.process(30f, t)
        }
        assertTrue(out > 29.5f, "saída $out")
    }

    @Test
    fun `timestamp repetido nao gera NaN`() {
        val filter = OneEuroFilter()
        filter.process(1f, 100L)
        assertEquals(1f, filter.process(5f, 100L))
    }
}

class AxisMapperTest {
    private val mapper = AxisMapper(GlobalSettings(arcDegrees = 90f, deadzoneDegrees = 1f))

    @Test
    fun `zona morta zera e o fim do giro satura`() {
        assertEquals(0f, mapper.toAxis(0.5f))
        assertEquals(1f, mapper.toAxis(90f))
        assertEquals(-1f, mapper.toAxis(-120f))
    }

    @Test
    fun `meio do giro e linear e simetrico`() {
        assertEquals(0.5f, mapper.toAxis(45.5f), 0.001f)
        assertEquals(-0.5f, mapper.toAxis(-45.5f), 0.001f)
    }

    @Test
    fun `curva suave e menos sensivel no centro`() {
        val soft = AxisMapper(GlobalSettings(deadzoneDegrees = 0f, curve = ResponseCurve.SOFT))
        assertTrue(soft.toAxis(45f) < 0.5f)
    }

    @Test
    fun `pedal chega ao fundo no curso configurado`() {
        val pedal = PedalMapper(GlobalSettings(pedalFullAt = 0.8f))
        assertEquals(1f, pedal.toPedal(0.8f))
        assertEquals(0.5f, pedal.toPedal(0.4f), 0.0001f)
    }
}

class FailsafePolicyTest {
    @Test
    fun `saude segue a tabela de silencio`() {
        assertEquals(LinkHealth.OK, FailsafePolicy.health(10_000_000L))
        assertEquals(LinkHealth.WARNING, FailsafePolicy.health(60_000_000L))
        assertEquals(LinkHealth.PEDALS_RELEASED, FailsafePolicy.health(150_000_000L))
        assertEquals(LinkHealth.CENTERING, FailsafePolicy.health(600_000_000L))
        assertEquals(LinkHealth.DISCONNECTED, FailsafePolicy.health(3_000_000_000L))
    }

    @Test
    fun `apos 100 ms solta pedais e mantem a direcao`() {
        val state = WheelState().apply { steering = 0.6f; throttle = 1f; brake = 0.5f; buttons = 1 }
        assertTrue(FailsafePolicy.apply(state, 150_000_000L, 0.01f))
        assertEquals(0.8f, state.throttle, 0.0001f)
        assertEquals(0.3f, state.brake, 0.0001f)
        assertEquals(0.6f, state.steering)
        assertEquals(1, state.buttons)
    }

    @Test
    fun `apos 500 ms centraliza e solta botoes`() {
        val state = WheelState().apply { steering = -0.6f; buttons = 3 }
        FailsafePolicy.apply(state, 600_000_000L, 0.05f)
        assertEquals(-0.35f, state.steering, 0.0001f)
        assertEquals(0, state.buttons)
    }

    @Test
    fun `sem silencio nada muda`() {
        val state = WheelState().apply { throttle = 1f }
        assertFalse(FailsafePolicy.apply(state, 20_000_000L, 0.01f))
        assertEquals(1f, state.throttle)
    }
}

class AxisAssignmentTest {
    @Test
    fun `so o eixo escolhido se mexe`() {
        val state = WheelState().apply { steering = 0.3f; brake = 1f; buttons = 5 }
        AxisAssignment.THROTTLE.applyTo(state, 1f)
        assertEquals(1f, state.throttle, 0.0001f)
        assertEquals(0f, state.steering)
        assertEquals(0f, state.brake)
        assertEquals(0, state.buttons)
    }
}

class FakeWheelContractTest : VirtualWheelContract() {
    override fun createWheel(): VirtualWheel = FakeWheel()
}
