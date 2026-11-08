package app.virtualwheelracing.domain

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Contrato que toda [VirtualWheel] precisa cumprir. Cada implementação estende esta classe
 * e herda os testes (princípio de substituição de Liskov).
 */
abstract class VirtualWheelContract {
    protected abstract fun createWheel(): VirtualWheel

    private fun fullState() = WheelState().apply {
        steering = 1f
        throttle = 1f
        brake = 1f
        buttons = WheelState.BUTTON_MASK
    }

    @Test
    fun `apply e neutral antes de abrir nao lancam excecao`() {
        createWheel().use { wheel ->
            assertDoesNotThrow { wheel.apply(fullState()) }
            assertDoesNotThrow { wheel.neutral() }
        }
    }

    @Test
    fun `abrir duas vezes devolve o mesmo resultado`() {
        createWheel().use { wheel ->
            assertEquals(wheel.open(), wheel.open())
        }
    }

    @Test
    fun `fechar mais de uma vez e depois aplicar nao lanca excecao`() {
        val wheel = createWheel()
        wheel.open()
        wheel.apply(fullState())
        wheel.neutral()
        assertDoesNotThrow { wheel.close() }
        assertDoesNotThrow { wheel.close() }
        assertDoesNotThrow { wheel.apply(fullState()) }
    }
}
