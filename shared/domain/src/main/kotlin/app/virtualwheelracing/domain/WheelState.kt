package app.virtualwheelracing.domain

/**
 * Estado do volante enviado do celular ao PC.
 *
 * É mutável de propósito: o mesmo objeto é reaproveitado a cada amostra do sensor,
 * para o caminho de tempo real não criar objetos (docs/arquitetura.md, seção 7).
 */
class WheelState {
    /** Direção normalizada: -1 = esquerda total, 0 = centro, 1 = direita total. */
    var steering: Float = 0f
        set(value) { field = clampOrZero(value, -1f, 1f) }

    /** Acelerador de 0 (solto) a 1 (no fundo). */
    var throttle: Float = 0f
        set(value) { field = clampOrZero(value, 0f, 1f) }

    /** Freio de 0 (solto) a 1 (no fundo). */
    var brake: Float = 0f
        set(value) { field = clampOrZero(value, 0f, 1f) }

    /** Botões em bits: bit 0 = botão 1, até o botão 16. */
    var buttons: Int = 0
        set(value) { field = value and BUTTON_MASK }

    fun setButton(index: Int, pressed: Boolean) {
        if (index !in 0 until BUTTON_COUNT) return
        val bit = 1 shl index
        buttons = if (pressed) buttons or bit else buttons and bit.inv()
    }

    fun isPressed(index: Int): Boolean =
        index in 0 until BUTTON_COUNT && (buttons and (1 shl index)) != 0

    fun copyFrom(other: WheelState) {
        steering = other.steering
        throttle = other.throttle
        brake = other.brake
        buttons = other.buttons
    }

    /** Volta ao neutro: direção no centro, pedais e botões soltos. */
    fun reset() {
        steering = 0f
        throttle = 0f
        brake = 0f
        buttons = 0
    }

    companion object {
        const val BUTTON_COUNT = 16
        const val BUTTON_MASK = 0xFFFF

        private fun clampOrZero(value: Float, min: Float, max: Float): Float =
            if (value.isNaN()) 0f else value.coerceIn(min, max)
    }
}
