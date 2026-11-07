package app.prwheel.desktop.vjoy

import app.prwheel.domain.VirtualWheel
import app.prwheel.domain.VirtualWheelContract
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach

/**
 * Roda o contrato contra o vJoy real. Só executa quando pedido, porque mexe no volante virtual:
 * gradlew :desktop:vjoy:test -Pprwheel.vjoy=true
 */
class VJoyAdapterContractTest : VirtualWheelContract() {
    @BeforeEach
    fun onlyWhenRequested() {
        assumeTrue(System.getProperty("prwheel.vjoy") == "true", "Teste com vJoy real desligado")
    }

    override fun createWheel(): VirtualWheel = VJoyAdapter()
}
