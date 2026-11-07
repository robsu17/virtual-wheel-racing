package app.volapex.engine

import android.os.Handler
import android.os.HandlerThread
import app.volapex.domain.MonotonicClock
import app.volapex.domain.SensorStats
import app.volapex.domain.SteeringAngleTracker

/**
 * Caso de uso "calibrar": mede o sensor por alguns segundos, sem rede, e mostra o giro ao vivo.
 * Detecta celulares com sensor lento ou que repetem valores (RF-06).
 */
class SensorDiagnostics(
    private val source: OrientationSource,
    private val clock: MonotonicClock,
    private val durationNanos: Long = 10_000_000_000L,
) : OrientationListener {
    private val tracker = SteeringAngleTracker()
    private val timestamps = LongArray(MAX_SAMPLES)
    private var count = 0
    private var repeated = 0
    private var lastW = Float.NaN
    private var lastX = Float.NaN
    private var lastY = Float.NaN
    private var lastZ = Float.NaN
    private var startNanos = 0L

    private var thread: HandlerThread? = null
    @Volatile private var handler: Handler? = null
    @Volatile private var started = 0L
    @Volatile private var available = true
    @Volatile private var liveDegrees = 0f
    @Volatile private var tilted = false
    @Volatile private var result: SensorStats? = null
    @Volatile private var finished = false

    fun start() {
        check(thread == null) { "Diagnóstico já iniciado" }
        val t = HandlerThread("volapex-diagnostics").apply { start() }
        val h = Handler(t.looper)
        thread = t
        handler = h
        h.post {
            startNanos = clock.nanos()
            started = startNanos
            available = source.start(this, h)
        }
    }

    fun recenter() {
        handler?.post { tracker.requestRecenter() }
    }

    fun stop() {
        val h = handler ?: return
        val t = thread
        handler = null
        thread = null
        h.post {
            source.stop()
            t?.quitSafely()
        }
    }

    fun snapshot(): DiagnosticsSnapshot {
        val elapsed = if (started == 0L) 0f else ((clock.nanos() - started) / 1_000_000_000f)
        return DiagnosticsSnapshot(
            sensorAvailable = available,
            progress = (elapsed / (durationNanos / 1_000_000_000f)).coerceIn(0f, 1f),
            finished = finished,
            stats = result,
            liveDegrees = liveDegrees,
            tilted = tilted,
        )
    }

    override fun onOrientation(w: Float, x: Float, y: Float, z: Float, timestampNanos: Long) {
        liveDegrees = tracker.update(w, x, y, z)
        tilted = tracker.isTilted
        if (finished) return
        if (w == lastW && x == lastX && y == lastY && z == lastZ) repeated++
        lastW = w
        lastX = x
        lastY = y
        lastZ = z
        if (count < MAX_SAMPLES) timestamps[count++] = timestampNanos
        if (clock.nanos() - startNanos >= durationNanos) {
            result = SensorStats.from(timestamps, count, repeated)
            finished = true
        }
    }

    private companion object {
        const val MAX_SAMPLES = 4000
    }
}

data class DiagnosticsSnapshot(
    val sensorAvailable: Boolean,
    val progress: Float,
    val finished: Boolean,
    val stats: SensorStats?,
    val liveDegrees: Float,
    val tilted: Boolean,
)
