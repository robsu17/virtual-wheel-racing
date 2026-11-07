package app.prwheel.domain

import kotlin.math.PI
import kotlin.math.sin

/**
 * Modo "mexer só um eixo": ajuda a atribuir os controles nas opções de cada jogo.
 * Enquanto ativo, o estado real é trocado por um movimento lento em um único eixo.
 */
enum class AxisAssignment {
    STEERING,
    THROTTLE,
    BRAKE;

    fun applyTo(state: WheelState, elapsedSeconds: Float) {
        val wave = sin(2f * PI.toFloat() * elapsedSeconds / PERIOD_SECONDS)
        state.reset()
        when (this) {
            STEERING -> state.steering = wave
            THROTTLE -> state.throttle = (wave + 1f) / 2f
            BRAKE -> state.brake = (wave + 1f) / 2f
        }
    }

    companion object {
        const val PERIOD_SECONDS = 4f
    }
}
