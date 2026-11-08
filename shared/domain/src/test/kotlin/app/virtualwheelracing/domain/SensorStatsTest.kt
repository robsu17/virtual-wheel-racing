package app.virtualwheelracing.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SensorStatsTest {
    @Test
    fun `200 Hz regulares dao modo completo`() {
        val timestamps = LongArray(1000) { it * 5_000_000L }
        val stats = SensorStats.from(timestamps, 1000, repeated = 0)!!
        assertEquals(200, stats.rateHz)
        assertEquals(5f, stats.p95IntervalMs, 0.001f)
        assertTrue(stats.fullMode)
    }

    @Test
    fun `50 Hz com valores repetidos dao modo reduzido`() {
        val timestamps = LongArray(500) { it * 20_000_000L }
        val stats = SensorStats.from(timestamps, 500, repeated = 250)!!
        assertEquals(50, stats.rateHz)
        assertEquals(50f, stats.repeatedPercent, 0.001f)
        assertFalse(stats.fullMode)
    }

    @Test
    fun `poucas leituras nao dao resultado`() {
        assertNull(SensorStats.from(LongArray(2), 2, 0))
    }
}
