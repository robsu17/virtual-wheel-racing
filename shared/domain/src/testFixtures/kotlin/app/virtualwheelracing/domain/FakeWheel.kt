package app.virtualwheelracing.domain

/** Volante virtual de mentira, que só registra o que recebeu. Seguro para ler de outra thread. */
class FakeWheel : VirtualWheel {
    private val applied = WheelState()
    private var opened = false

    var applyCount = 0
        @Synchronized get
        private set

    var neutralCount = 0
        @Synchronized get
        private set

    @Synchronized
    override fun open(): OpenResult {
        opened = true
        return OpenResult.Ok
    }

    @Synchronized
    override fun apply(state: WheelState) {
        applied.copyFrom(state)
        applyCount++
    }

    @Synchronized
    override fun neutral() {
        applied.reset()
        neutralCount++
    }

    @Synchronized
    override fun close() {
        if (opened) neutral()
        opened = false
    }

    /** Cópia do último estado aplicado. */
    @Synchronized
    fun current(): WheelState = WheelState().also { it.copyFrom(applied) }
}
