package app.virtualwheelracing.desktop.vjoy

import app.virtualwheelracing.domain.VirtualWheel
import app.virtualwheelracing.domain.VirtualWheelContract
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach

/**
 * Roda o contrato contra o vJoy real. Só executa quando pedido, porque mexe no volante virtual:
 * gradlew :desktop:vjoy:test -Pvwr.vjoy=true
 */
class VJoyAdapterContractTest : VirtualWheelContract() {
    @BeforeEach
    fun onlyWhenRequested() {
        assumeTrue(System.getProperty("vwr.vjoy") == "true", "Teste com vJoy real desligado")
    }

    override fun createWheel(): VirtualWheel = VJoyAdapter()
}
