package app.prwheel.desktop.receiver

/**
 * Estatísticas do link: intervalos entre pacotes, perdas e rejeições.
 * Registrar não cria objetos; só o [snapshot], chamado pela tela, aloca.
 */
class LinkStats(capacity: Int = 2048) {
    private val gaps = LongArray(capacity)
    private var gapCount = 0
    private var gapIndex = 0
    private var lastArrival = 0L
    private var maxGap = 0L

    private var received = 0L
    private var rejected = 0L
    private var lost = 0L

    private var windowStart = 0L
    private var windowCount = 0
    private var rate = 0

    @Synchronized
    fun recordAccepted(nowNanos: Long, lostBefore: Int) {
        received++
        lost += lostBefore
        if (lastArrival != 0L) {
            val gap = nowNanos - lastArrival
            gaps[gapIndex] = gap
            gapIndex = (gapIndex + 1) % gaps.size
            if (gapCount < gaps.size) gapCount++
            if (gap > maxGap) maxGap = gap
        }
        lastArrival = nowNanos
        if (windowStart == 0L) windowStart = nowNanos
        windowCount++
        if (nowNanos - windowStart >= ONE_SECOND) {
            rate = windowCount
            windowCount = 0
            windowStart = nowNanos
        }
    }

    @Synchronized
    fun recordRejected() {
        rejected++
    }

    /** Uma sessão nova não deve herdar a pausa entre sessões como "maior falha". */
    @Synchronized
    fun resetSession() {
        lastArrival = 0L
        gapCount = 0
        gapIndex = 0
        maxGap = 0L
        windowStart = 0L
        windowCount = 0
        rate = 0
    }

    @Synchronized
    fun snapshot(nowNanos: Long): LinkStatsSnapshot {
        val sorted = gaps.copyOf(gapCount).also { it.sort() }
        fun percentileMs(p: Double): Double =
            if (sorted.isEmpty()) 0.0 else sorted[((sorted.size - 1) * p).toInt()] / 1_000_000.0
        val stale = lastArrival == 0L || nowNanos - lastArrival > ONE_SECOND
        return LinkStatsSnapshot(
            packetsPerSecond = if (stale) 0 else rate,
            received = received,
            rejected = rejected,
            lost = lost,
            gapP50Ms = percentileMs(0.50),
            gapP95Ms = percentileMs(0.95),
            gapP99Ms = percentileMs(0.99),
            maxGapMs = maxGap / 1_000_000.0,
        )
    }

    private companion object {
        const val ONE_SECOND = 1_000_000_000L
    }
}

data class LinkStatsSnapshot(
    val packetsPerSecond: Int,
    val received: Long,
    val rejected: Long,
    val lost: Long,
    val gapP50Ms: Double,
    val gapP95Ms: Double,
    val gapP99Ms: Double,
    val maxGapMs: Double,
)
