package app.prwheel.desktop.vjoy

import app.prwheel.domain.DriverHealth
import app.prwheel.domain.DriverStatus
import app.prwheel.domain.OpenResult
import app.prwheel.domain.VirtualWheel
import app.prwheel.domain.WheelState
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.ptr.IntByReference
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.roundToInt

/** Funções da vJoyInterface.dll usadas pelo app (os nomes precisam ser idênticos aos da DLL). */
@Suppress("FunctionName")
internal interface VJoyLibrary : Library {
    fun vJoyEnabled(): Boolean
    fun GetVJDStatus(rID: Int): Int
    fun AcquireVJD(rID: Int): Boolean
    fun RelinquishVJD(rID: Int)
    fun GetVJDAxisExist(rID: Int, axis: Int): Boolean
    fun GetVJDAxisMax(rID: Int, axis: Int, max: IntByReference): Boolean
    fun GetVJDButtonNumber(rID: Int): Int
    fun SetAxis(value: Int, rID: Int, axis: Int): Boolean
    fun SetBtn(value: Boolean, rID: Int, nBtn: Byte): Boolean
}

/**
 * Único ponto do código que sabe que o vJoy existe. Eixos: X = direção, Y = acelerador,
 * Z = freio; botões 1 a 16. Testado com o vJoy 2.1.9.1.
 */
class VJoyAdapter(
    private val dllPath: Path = defaultDllPath(),
    private val deviceId: Int = 1,
) : VirtualWheel, DriverHealth {
    private var library: VJoyLibrary? = null
    private var acquired = false
    private val axisMax = IntArray(AXIS_COUNT) // 0 = eixo não habilitado
    private val lastAxisValue = IntArray(AXIS_COUNT) { -1 }
    private var buttonCount = 0
    private var lastButtons = 0
    private var lastOpenResult: OpenResult? = null
    private val neutralState = WheelState()

    @Synchronized
    override fun open(): OpenResult {
        if (acquired) return OpenResult.Ok
        val result = tryOpen()
        lastOpenResult = result
        return result
    }

    private fun tryOpen(): OpenResult {
        val lib = load() ?: return OpenResult.DriverMissing
        return try {
            when {
                !lib.vJoyEnabled() -> OpenResult.DriverDisabled
                else -> when (lib.GetVJDStatus(deviceId)) {
                    STATUS_OWN, STATUS_FREE -> acquire(lib)
                    STATUS_BUSY -> OpenResult.DeviceBusy
                    else -> OpenResult.DeviceMissing
                }
            }
        } catch (e: LinkageError) {
            OpenResult.Failed(e.message ?: "Erro ao chamar o vJoy")
        } catch (e: RuntimeException) {
            OpenResult.Failed(e.message ?: "Erro ao chamar o vJoy")
        }
    }

    private fun acquire(lib: VJoyLibrary): OpenResult {
        readCapabilities(lib)
        if (axisMax[AXIS_STEERING] == 0) {
            return OpenResult.Failed("O eixo X (direção) não está habilitado no Configure vJoy.")
        }
        if (!lib.AcquireVJD(deviceId)) return OpenResult.DeviceBusy
        acquired = true
        lastButtons = -1
        lastAxisValue.fill(-1)
        write(lib, neutralState)
        return OpenResult.Ok
    }

    @Synchronized
    override fun apply(state: WheelState) {
        val lib = library ?: return
        if (!acquired) return
        try {
            write(lib, state)
        } catch (e: RuntimeException) {
            // Contrato: nunca lança exceção no laço de recepção.
        } catch (e: LinkageError) {
        }
    }

    @Synchronized
    override fun neutral() = apply(neutralState)

    @Synchronized
    override fun close() {
        val lib = library ?: return
        if (!acquired) return
        try {
            write(lib, neutralState)
            lib.RelinquishVJD(deviceId)
        } catch (e: RuntimeException) {
        } catch (e: LinkageError) {
        }
        acquired = false
    }

    @Synchronized
    override fun check(): DriverStatus {
        val lib = load()
        if (lib == null) {
            return DriverStatus(false, false, deviceId, "vJoyInterface.dll não encontrada em $dllPath", false, false, false, 0)
        }
        return try {
            val enabled = lib.vJoyEnabled()
            val stateText = when (lib.GetVJDStatus(deviceId)) {
                STATUS_OWN -> "em uso pelo PR Wheel"
                STATUS_FREE -> "livre"
                STATUS_BUSY -> "ocupado por outro programa"
                STATUS_MISSING -> "não existe (habilite-o no Configure vJoy)"
                else -> "desconhecido"
            }
            DriverStatus(
                driverLoaded = true,
                driverEnabled = enabled,
                deviceId = deviceId,
                deviceState = stateText,
                hasSteeringAxis = lib.GetVJDAxisExist(deviceId, USAGES[AXIS_STEERING]),
                hasThrottleAxis = lib.GetVJDAxisExist(deviceId, USAGES[AXIS_THROTTLE]),
                hasBrakeAxis = lib.GetVJDAxisExist(deviceId, USAGES[AXIS_BRAKE]),
                buttonCount = lib.GetVJDButtonNumber(deviceId),
            )
        } catch (e: LinkageError) {
            DriverStatus(true, false, deviceId, e.message ?: "erro", false, false, false, 0)
        }
    }

    private fun write(lib: VJoyLibrary, state: WheelState) {
        setAxis(lib, AXIS_STEERING, (state.steering + 1f) / 2f)
        setAxis(lib, AXIS_THROTTLE, state.throttle)
        setAxis(lib, AXIS_BRAKE, state.brake)
        val changed = state.buttons xor lastButtons
        if (changed != 0) {
            val count = minOf(buttonCount, WheelState.BUTTON_COUNT)
            for (i in 0 until count) {
                val bit = 1 shl i
                if (changed and bit != 0) lib.SetBtn(state.buttons and bit != 0, deviceId, (i + 1).toByte())
            }
            lastButtons = state.buttons
        }
    }

    private fun setAxis(lib: VJoyLibrary, axis: Int, fraction: Float) {
        val max = axisMax[axis]
        if (max == 0) return
        val value = 1 + (fraction.coerceIn(0f, 1f) * (max - 1)).roundToInt()
        if (value == lastAxisValue[axis]) return
        lib.SetAxis(value, deviceId, USAGES[axis])
        lastAxisValue[axis] = value
    }

    private fun readCapabilities(lib: VJoyLibrary) {
        for (axis in 0 until AXIS_COUNT) {
            axisMax[axis] = if (lib.GetVJDAxisExist(deviceId, USAGES[axis])) {
                val ref = IntByReference()
                if (lib.GetVJDAxisMax(deviceId, USAGES[axis], ref) && ref.value > 1) ref.value else DEFAULT_AXIS_MAX
            } else {
                0
            }
        }
        buttonCount = lib.GetVJDButtonNumber(deviceId)
    }

    private fun load(): VJoyLibrary? {
        library?.let { return it }
        if (!Files.exists(dllPath)) return null
        return try {
            Native.load(dllPath.toString(), VJoyLibrary::class.java).also { library = it }
        } catch (e: UnsatisfiedLinkError) {
            null
        }
    }

    companion object {
        private const val STATUS_OWN = 0
        private const val STATUS_FREE = 1
        private const val STATUS_BUSY = 2
        private const val STATUS_MISSING = 3

        private const val AXIS_STEERING = 0
        private const val AXIS_THROTTLE = 1
        private const val AXIS_BRAKE = 2
        private const val AXIS_COUNT = 3
        private val USAGES = intArrayOf(0x30, 0x31, 0x32) // HID X, Y, Z
        private const val DEFAULT_AXIS_MAX = 32767

        fun defaultDllPath(): Path =
            Paths.get(System.getenv("ProgramFiles") ?: "C:\\Program Files", "vJoy", "x64", "vJoyInterface.dll")
    }
}
