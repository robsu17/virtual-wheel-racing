package app.volapex.domain

import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Funções puras para tirar o ângulo de direção da orientação do celular.
 *
 * O quaternion (w, x, y, z) leva coordenadas do celular para o mundo. O vetor "para cima"
 * do mundo, visto pelo celular, é a terceira linha da matriz de rotação. Como a deriva do
 * sensor gira em torno do eixo vertical, ela não muda esse vetor nem o ângulo calculado.
 */
object SteeringMath {
    private const val RAD_TO_DEG = (180.0 / Math.PI).toFloat()

    fun upX(w: Float, x: Float, y: Float, z: Float): Float = 2f * (x * z - w * y)

    fun upY(w: Float, x: Float, y: Float, z: Float): Float = 2f * (y * z + w * x)

    /** Ângulo, em graus, do vetor "para cima" no plano da tela. Positivo = girou para a direita. */
    fun planeAngleDegrees(upX: Float, upY: Float, sign: Float): Float =
        atan2(sign * upY, sign * upX) * RAD_TO_DEG

    fun wrap180(degrees: Float): Float {
        var d = degrees % 360f
        if (d > 180f) d -= 360f
        if (d < -180f) d += 360f
        return d
    }
}

/**
 * Guarda a calibração (centro e lado da paisagem) e protege contra inclinação excessiva:
 * com o celular deitado demais, o ângulo deixa de ser confiável e o último valor é mantido.
 */
class SteeringAngleTracker(
    private val freezeNorm: Float = 0.35f,
    private val resumeNorm: Float = 0.40f,
) {
    private var sign = 1f
    private var zeroDegrees = 0f
    private var recenterPending = true
    private var lastDegrees = 0f

    /** Verdadeiro enquanto o celular está inclinado demais para medir a direção. */
    var isTilted: Boolean = false
        private set

    /** O próximo ângulo válido passa a ser o centro. */
    fun requestRecenter() {
        recenterPending = true
    }

    /** Recebe o quaternion (w, x, y, z) celular→mundo e devolve o ângulo relativo ao centro, em graus. */
    fun update(w: Float, x: Float, y: Float, z: Float): Float {
        val ux = SteeringMath.upX(w, x, y, z)
        val uy = SteeringMath.upY(w, x, y, z)
        val norm = sqrt(ux * ux + uy * uy)
        isTilted = if (isTilted) norm < resumeNorm else norm < freezeNorm
        if (isTilted) return lastDegrees

        if (recenterPending) {
            sign = if (ux >= 0f) 1f else -1f
            zeroDegrees = SteeringMath.planeAngleDegrees(ux, uy, sign)
            recenterPending = false
        }
        lastDegrees = SteeringMath.wrap180(SteeringMath.planeAngleDegrees(ux, uy, sign) - zeroDegrees)
        return lastDegrees
    }
}
