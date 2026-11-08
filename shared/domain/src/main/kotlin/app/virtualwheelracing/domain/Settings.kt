package app.virtualwheelracing.domain

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

/** Curva de resposta da direção: Suave é menos sensível no centro; Firme, mais. */
enum class ResponseCurve(private val exponent: Float) {
    LINEAR(1f),
    SOFT(1.5f),
    FIRM(0.75f);

    fun apply(normalized: Float): Float = if (exponent == 1f) normalized else normalized.pow(exponent)
}

/** Ajustes do volante. Valem para todos os jogos (configuração universal, sem perfis). */
data class GlobalSettings(
    /** Quanto girar o celular, para cada lado, até a direção chegar ao fim. */
    val arcDegrees: Float = 90f,
    val deadzoneDegrees: Float = 0.8f,
    val curve: ResponseCurve = ResponseCurve.LINEAR,
    val brakeOnLeft: Boolean = true,
    /** Fração do curso do polegar em que o pedal já conta como no fundo. */
    val pedalFullAt: Float = 0.9f,
) {
    init {
        require(arcDegrees in 30f..180f) { "Giro fora da faixa: $arcDegrees" }
        require(deadzoneDegrees >= 0f && deadzoneDegrees < arcDegrees) { "Zona morta inválida: $deadzoneDegrees" }
        require(pedalFullAt in 0.3f..1f) { "Curso do pedal inválido: $pedalFullAt" }
    }
}

/** Converte graus do celular no eixo de direção, de -1 a 1, aplicando zona morta e curva. */
class AxisMapper(private val settings: GlobalSettings) {
    fun toAxis(degrees: Float): Float {
        val span = settings.arcDegrees - settings.deadzoneDegrees
        val normalized = ((abs(degrees) - settings.deadzoneDegrees) / span).coerceIn(0f, 1f)
        return sign(degrees) * settings.curve.apply(normalized)
    }
}

/** Converte a posição do polegar (0 = embaixo, 1 = em cima) no valor do pedal. */
class PedalMapper(private val settings: GlobalSettings) {
    fun toPedal(position: Float): Float = (position / settings.pedalFullAt).coerceIn(0f, 1f)
}
