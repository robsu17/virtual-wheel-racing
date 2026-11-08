package app.virtualwheelracing.domain

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SteeringAngleTrackerTest {

    // Matriz celular→mundo: a coluna j é o eixo j do celular escrito no mundo (x leste, y norte, z cima).
    private fun columns(x: DoubleArray, y: DoubleArray, z: DoubleArray) =
        Array(3) { i -> doubleArrayOf(x[i], y[i], z[i]) }

    /** Celular em pé na frente da pessoa, tela virada para ela, em paisagem. */
    private fun landscape(topOnLeft: Boolean) = if (topOnLeft) {
        columns(doubleArrayOf(0.0, 0.0, 1.0), doubleArrayOf(-1.0, 0.0, 0.0), doubleArrayOf(0.0, -1.0, 0.0))
    } else {
        columns(doubleArrayOf(0.0, 0.0, -1.0), doubleArrayOf(1.0, 0.0, 0.0), doubleArrayOf(0.0, -1.0, 0.0))
    }

    private fun rotZ(degrees: Double): Array<DoubleArray> {
        val r = Math.toRadians(degrees)
        return arrayOf(
            doubleArrayOf(cos(r), -sin(r), 0.0),
            doubleArrayOf(sin(r), cos(r), 0.0),
            doubleArrayOf(0.0, 0.0, 1.0),
        )
    }

    private fun mul(a: Array<DoubleArray>, b: Array<DoubleArray>) =
        Array(3) { i -> DoubleArray(3) { j -> (0 until 3).sumOf { k -> a[i][k] * b[k][j] } } }

    /** Girar o volante para a direita = girar no sentido horário visto pela pessoa (−z do celular). */
    private fun turnRight(m: Array<DoubleArray>, degrees: Double) = mul(m, rotZ(-degrees))

    private fun quaternion(m: Array<DoubleArray>): FloatArray {
        val trace = m[0][0] + m[1][1] + m[2][2]
        val (w, x, y, z) = when {
            trace > 0 -> {
                val s = sqrt(trace + 1.0) * 2
                listOf(0.25 * s, (m[2][1] - m[1][2]) / s, (m[0][2] - m[2][0]) / s, (m[1][0] - m[0][1]) / s)
            }
            m[0][0] > m[1][1] && m[0][0] > m[2][2] -> {
                val s = sqrt(1.0 + m[0][0] - m[1][1] - m[2][2]) * 2
                listOf((m[2][1] - m[1][2]) / s, 0.25 * s, (m[0][1] + m[1][0]) / s, (m[0][2] + m[2][0]) / s)
            }
            m[1][1] > m[2][2] -> {
                val s = sqrt(1.0 + m[1][1] - m[0][0] - m[2][2]) * 2
                listOf((m[0][2] - m[2][0]) / s, (m[0][1] + m[1][0]) / s, 0.25 * s, (m[1][2] + m[2][1]) / s)
            }
            else -> {
                val s = sqrt(1.0 + m[2][2] - m[0][0] - m[1][1]) * 2
                listOf((m[1][0] - m[0][1]) / s, (m[0][2] + m[2][0]) / s, (m[1][2] + m[2][1]) / s, 0.25 * s)
            }
        }
        return floatArrayOf(w.toFloat(), x.toFloat(), y.toFloat(), z.toFloat())
    }

    private fun SteeringAngleTracker.feed(m: Array<DoubleArray>): Float {
        val q = quaternion(m)
        return update(q[0], q[1], q[2], q[3])
    }

    @Test
    fun `girar para a direita da angulo positivo nas duas paisagens`() {
        for (topOnLeft in listOf(true, false)) {
            val tracker = SteeringAngleTracker()
            val base = landscape(topOnLeft)
            assertEquals(0f, tracker.feed(base), 0.01f)
            assertEquals(30f, tracker.feed(turnRight(base, 30.0)), 0.01f)
            assertEquals(-45f, tracker.feed(turnRight(base, -45.0)), 0.01f)
        }
    }

    @Test
    fun `deriva de rumo nao muda o angulo`() {
        val tracker = SteeringAngleTracker()
        val base = landscape(topOnLeft = true)
        tracker.feed(base)
        val driftedAndTurned = mul(rotZ(50.0), turnRight(base, 20.0))
        assertEquals(20f, tracker.feed(driftedAndTurned), 0.01f)
    }

    @Test
    fun `recentrar torna a posicao atual o novo centro`() {
        val tracker = SteeringAngleTracker()
        val base = landscape(topOnLeft = true)
        tracker.feed(base)
        tracker.requestRecenter()
        tracker.feed(turnRight(base, 10.0))
        assertEquals(5f, tracker.feed(turnRight(base, 15.0)), 0.01f)
    }

    @Test
    fun `celular deitado mantem o ultimo angulo`() {
        val tracker = SteeringAngleTracker()
        val base = landscape(topOnLeft = true)
        tracker.feed(base)
        tracker.feed(turnRight(base, 12.0))
        val flat = columns(doubleArrayOf(1.0, 0.0, 0.0), doubleArrayOf(0.0, 1.0, 0.0), doubleArrayOf(0.0, 0.0, 1.0))
        assertEquals(12f, tracker.feed(flat), 0.01f)
        assertTrue(tracker.isTilted)
    }
}
