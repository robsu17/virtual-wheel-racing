package app.prwheel.domain

import kotlin.math.max
import kotlin.math.min

enum class LinkHealth { OK, WARNING, PEDALS_RELEASED, CENTERING, DISCONNECTED }

/**
 * O que fazer quando os pacotes param de chegar (tabela de failsafe do relatório).
 * Pedais soltam primeiro; a direção só volta ao centro depois, para evitar guinada.
 */
object FailsafePolicy {
    const val WARNING_NANOS = 50_000_000L
    const val RELEASE_PEDALS_NANOS = 100_000_000L
    const val CENTER_NANOS = 500_000_000L
    const val DISCONNECT_NANOS = 3_000_000_000L

    private const val PEDAL_RAMP_SECONDS = 0.05f
    private const val STEERING_RAMP_SECONDS = 0.2f

    fun health(silenceNanos: Long): LinkHealth = when {
        silenceNanos >= DISCONNECT_NANOS -> LinkHealth.DISCONNECTED
        silenceNanos >= CENTER_NANOS -> LinkHealth.CENTERING
        silenceNanos >= RELEASE_PEDALS_NANOS -> LinkHealth.PEDALS_RELEASED
        silenceNanos >= WARNING_NANOS -> LinkHealth.WARNING
        else -> LinkHealth.OK
    }

    /** Aplica as rampas ao estado e diz se ele mudou. [dtSeconds] é o tempo desde a última chamada. */
    fun apply(state: WheelState, silenceNanos: Long, dtSeconds: Float): Boolean {
        var changed = false
        if (silenceNanos >= RELEASE_PEDALS_NANOS) {
            val step = dtSeconds / PEDAL_RAMP_SECONDS
            if (state.throttle > 0f) {
                state.throttle = max(0f, state.throttle - step)
                changed = true
            }
            if (state.brake > 0f) {
                state.brake = max(0f, state.brake - step)
                changed = true
            }
        }
        if (silenceNanos >= CENTER_NANOS) {
            val step = dtSeconds / STEERING_RAMP_SECONDS
            val steering = state.steering
            if (steering != 0f) {
                state.steering = if (steering > 0f) max(0f, steering - step) else min(0f, steering + step)
                changed = true
            }
            if (state.buttons != 0) {
                state.buttons = 0
                changed = true
            }
        }
        return changed
    }
}
