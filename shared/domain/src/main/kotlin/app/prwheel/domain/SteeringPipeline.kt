package app.prwheel.domain

import kotlin.math.PI
import kotlin.math.abs

/**
 * Um estágio do processamento da direção. Filtros, curvas e predição novos entram como
 * classes novas, sem editar o pipeline (princípio aberto/fechado).
 */
interface SteeringStage {
    /** Recebe e devolve o ângulo em graus. Não deve criar objetos: roda a cada amostra do sensor. */
    fun process(degrees: Float, timestampNanos: Long): Float

    /** Esquece o histórico, por exemplo depois de recentrar. */
    fun reset() {}
}

class SteeringPipeline(private val stages: List<SteeringStage>) {
    fun process(degrees: Float, timestampNanos: Long): Float {
        var value = degrees
        for (i in stages.indices) value = stages[i].process(value, timestampNanos)
        return value
    }

    fun reset() {
        for (i in stages.indices) stages[i].reset()
    }
}

/**
 * Filtro One Euro (Casiez et al., 2012): suaviza o tremor com o celular parado sem atrasar
 * movimentos rápidos. Parâmetros iniciais do estudo, em graus do celular.
 */
class OneEuroFilter(
    private val minCutoffHz: Float = 1.5f,
    private val beta: Float = 0.1f,
    private val derivativeCutoffHz: Float = 10f,
) : SteeringStage {
    private var initialized = false
    private var previousValue = 0f
    private var previousDerivative = 0f
    private var previousTimestamp = 0L

    override fun process(degrees: Float, timestampNanos: Long): Float {
        if (!initialized) {
            initialized = true
            previousValue = degrees
            previousDerivative = 0f
            previousTimestamp = timestampNanos
            return degrees
        }
        val dt = (timestampNanos - previousTimestamp) / 1_000_000_000f
        if (dt <= 0f) return previousValue
        previousTimestamp = timestampNanos

        val derivative = (degrees - previousValue) / dt
        previousDerivative = lerp(previousDerivative, derivative, alpha(derivativeCutoffHz, dt))
        val cutoff = minCutoffHz + beta * abs(previousDerivative)
        previousValue = lerp(previousValue, degrees, alpha(cutoff, dt))
        return previousValue
    }

    override fun reset() {
        initialized = false
    }

    private fun alpha(cutoffHz: Float, dt: Float): Float {
        val tau = 1f / (2f * PI.toFloat() * cutoffHz)
        return 1f / (1f + tau / dt)
    }

    private fun lerp(from: Float, to: Float, t: Float): Float = from + t * (to - from)
}
