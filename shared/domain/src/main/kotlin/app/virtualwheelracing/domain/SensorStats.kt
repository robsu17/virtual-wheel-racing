package app.virtualwheelracing.domain

/** Resultado do teste de sensores da calibração (RF-06 e RF-07). */
data class SensorStats(
    /** Leituras por segundo, pela mediana dos intervalos. */
    val rateHz: Int,
    /** 95% dos intervalos entre leituras ficam abaixo deste valor. */
    val p95IntervalMs: Float,
    /** Leituras idênticas à anterior: sinal de sensor que repete valores para parecer mais rápido. */
    val repeatedPercent: Float,
) {
    /** Abaixo de 90 Hz, o app avisa e trabalha no modo reduzido. */
    val fullMode: Boolean get() = rateHz >= FULL_MODE_MIN_HZ

    companion object {
        const val FULL_MODE_MIN_HZ = 90

        /** Calcula a partir dos timestamps (em ns) das primeiras [count] leituras. Null se houver poucas. */
        fun from(timestamps: LongArray, count: Int, repeated: Int): SensorStats? {
            if (count < 3) return null
            val intervals = (1 until count)
                .map { timestamps[it] - timestamps[it - 1] }
                .filter { it > 0 }
                .sorted()
            if (intervals.isEmpty()) return null
            val median = intervals[intervals.size / 2]
            val p95 = intervals[((intervals.size - 1) * 0.95).toInt()]
            return SensorStats(
                rateHz = (1_000_000_000.0 / median).toInt(),
                p95IntervalMs = p95 / 1_000_000f,
                repeatedPercent = repeated * 100f / count,
            )
        }
    }
}
