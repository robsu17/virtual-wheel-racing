package app.prwheel.domain

/** Relógio monotônico em nanossegundos, trocável nos testes. */
fun interface MonotonicClock {
    fun nanos(): Long
}

/**
 * Volante virtual no PC. Toda implementação cumpre o mesmo contrato, verificado pela
 * bateria `VirtualWheelContract` (princípio de substituição de Liskov):
 * - `apply` e `neutral` nunca lançam exceção, mesmo antes de `open` ou depois de `close`;
 * - `open` repetido devolve o mesmo resultado;
 * - `close` pode ser chamado mais de uma vez.
 */
interface VirtualWheel : AutoCloseable {
    fun open(): OpenResult

    fun apply(state: WheelState)

    /** Pedais soltos, direção no centro e botões soltos. */
    fun neutral()
}

sealed interface OpenResult {
    data object Ok : OpenResult
    data object DriverMissing : OpenResult
    data object DriverDisabled : OpenResult
    data object DeviceMissing : OpenResult
    data object DeviceBusy : OpenResult
    data class Failed(val reason: String) : OpenResult
}

/** Diagnóstico do driver, usado só pela tela de verificação (segregação de interfaces). */
interface DriverHealth {
    fun check(): DriverStatus
}

data class DriverStatus(
    val driverLoaded: Boolean,
    val driverEnabled: Boolean,
    val deviceId: Int,
    val deviceState: String,
    val hasSteeringAxis: Boolean,
    val hasThrottleAxis: Boolean,
    val hasBrakeAxis: Boolean,
    val buttonCount: Int,
)
