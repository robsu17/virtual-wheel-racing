# Smartphone IMU to steering-wheel angle: sensors, algorithms, limits and latency (Android and iOS, November 2026)

Evidence labels used throughout:
- "Cited" items come from the linked source.
- "Own calculation" and "own simulation" mean Python runs made for these notes. They are synthetic, not device measurements.
- Anything estimated is labelled as an estimate. No device was bench-tested.

## Recommended pipeline and latency budget

### Takeaway
Take the OS-fused orientation at about 200 Hz on Android and at least 100 Hz on iOS. Derive the "up" vector in the device frame and compute the angle with `atan2` in the screen plane. Apply a One Euro filter and a short gyro prediction. Keep this loop native and off the UI thread. An estimated 15-30 ms phone-side latency is plausible on good hardware, but it has to be measured per device.

### Cited Findings
- `registerListener()` is capped at 200 Hz for apps targeting Android 12+, and direct channel is capped at `RATE_NORMAL` (about 50 Hz) unless `HIGH_SAMPLING_RATE_SENSORS` is declared — [Android sensors overview](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview)
- iOS device-motion maximum rate is "hardware dependent, but is usually at least 100 Hz", and higher requests are clamped — [Apple: processed device-motion data](https://developer.apple.com/tutorials/data/documentation/coremotion/getting-processed-device-motion-data.md)
- Android 16 CDD limits for delivery: general sensors report within 100 ms + 2 × sample_time, while high-fidelity sensors deliver to apps within 5 ms — [Android 16 CDD 7.3](https://source.android.com/docs/compatibility/16/android-16-cdd)
- Pantel and Wolf treat racing as the most sensitive class of game: 50 ms was acceptable and delays above 100 ms should be avoided — [summary of Pantel & Wolf](https://cs.ubc.ca/~krasic/cpsc538a/summaries/52)

### Inferences
Proposed defaults:

| Stage | Android | iOS |
|---|---|---|
| Primary input | `TYPE_GAME_ROTATION_VECTOR`, up vector from the third row of R | `CMDeviceMotion.gravity`, `xArbitraryZVertical` |
| Secondary input | `TYPE_GYROSCOPE` z-axis, for prediction and the high-tilt fallback | `rotationRate.z` |
| Requested rate | 5,000 µs (nominal 200 Hz), no batching. Measure the real rate from timestamps | `deviceMotionUpdateInterval = 1/100`. Measure the real rate from timestamps |
| Fallback | `TYPE_GRAVITY` plus gyro, then a custom complementary filter on accel plus gyro | None needed on current iPhones (not verified across models) |
| Filter (phone degrees, deg/s) | 1€: fcmin 1.5 Hz, β 0.1, dcutoff 10 Hz | Same |
| Prediction | H = measured sample age + transport estimate, clamped to ≤ 20-30 ms | Same |
| Tilt limit | Freeze gravity below in-plane norm 0.35 (about 70° tilt), resume at 0.40 | Same |

Latency budget. **These are estimates from the sampling arithmetic plus assumed OS and filter costs, not measurements:**

| Stage | Flagship, 200 Hz | Mid-range, 100 Hz | Low-end, 50 Hz |
|---|---|---|---|
| Sampling wait, mean T/2 (arithmetic) | 2.5 ms | 5 ms | 10 ms |
| Chip output filter (assumed) | 2 ms | 3 ms | 5 ms |
| HAL and framework delivery (assumed; CDD hi-fi limit is 5 ms) | 3 ms | 6 ms | 15 ms |
| Angle and mapping math | < 0.5 ms | < 0.5 ms | 1 ms |
| One Euro during motion (own simulation) | 10 ms | 15 ms | 20 ms |
| Handing off to the network socket (assumed) | 0.5 ms | 0.5 ms | 1 ms |
| **Phone-side total** | **about 18 ms** | **about 30 ms** | **about 52 ms** |
| After 10-20 ms of prediction | about 5-10 ms effective | about 15-20 ms | about 35-40 ms |

- Network, PC driver, game and display latency come on top of this table.
- Treat 50 Hz devices as degraded mode: one sample period is already 20 ms.
- Bench test to add: rotate the phone on a servo or against a high-speed camera, and log `timestamp` age at the callback (see Android section).

### Gaps
- No published end-to-end measurement of phone IMU to PC input was found. The table above must be replaced with bench data.

## Android: sensor choice, rates, limits, power

### Takeaway
Use `TYPE_GAME_ROTATION_VECTOR` as the default, because its heading drift does not affect the up vector, and it needs no magnetometer. Do not use the direct channel unless you also declare the high-rate permission. The 200 Hz cap is sufficient for this use. Real rates vary so much by device that the app must measure them.

### Cited Findings
Sensor types:
- `TYPE_GAME_ROTATION_VECTOR` uses accelerometer and gyroscope with no magnetometer. Its Y axis does not point north, and its reference may drift by the same order as gyro drift around Z. No numeric drift limit is given — [AOSP sensor types](https://source.android.com/docs/core/interaction/sensors/sensor-types)
- `TYPE_ROTATION_VECTOR` adds the magnetometer and is referenced to East-North-Up; `data[4]` holds estimated heading accuracy — [AOSP sensor types](https://source.android.com/docs/core/interaction/sensors/sensor-types)
- `TYPE_GRAVITY` uses accelerometer and gyro when a gyro is present, otherwise accelerometer and magnetometer; at rest it matches the accelerometer — [AOSP sensor types](https://source.android.com/docs/core/interaction/sensors/sensor-types)
- `TYPE_GYROSCOPE` output is corrected, with temperature compensation, scale compensation and online bias calibration; it cannot be emulated from accelerometer and magnetometer — [AOSP sensor types](https://source.android.com/docs/core/interaction/sensors/sensor-types)
- Timestamps are in nanoseconds, synchronized with `SystemClock.elapsedRealtimeNano()` — [Android 16 CDD 7.3](https://source.android.com/docs/compatibility/16/android-16-cdd)

Rate limits:
- `SENSOR_DELAY_GAME` is 20,000 µs, `UI` 60,000 µs, `NORMAL` 200,000 µs, `FASTEST` 0 µs. The delay is "only a suggested delay", and there is no public method for the delivered rate, so derive it from timestamps — [Android sensors overview](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview)
- Direct channel nominal rates are 50, 200 and 800 Hz, with actual rates expected within 55-220% of nominal: 27.5-110, 110-440 and 440-1760 Hz. Events go into shared memory in 104-byte elements — [SensorDirectChannel](https://developer.android.com/reference/android/hardware/SensorDirectChannel)
- The AOSP change throttles only accelerometer, gyroscope and magnetometer, on both direct and non-direct connections — [AOSP commit](https://android.googlesource.com/platform/frameworks/native/+/af91a91c5c)
- The permission is granted at install time without a prompt — [Qt Android compatibility notes](https://doc.qt.io/qt-6/compatmap.html)
- If the user turns off microphone access with the device toggle, sensors stay rate-limited even with the permission — [Android sensors overview](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview)
- Conflict on over-limit requests: Android docs say a `SecurityException` occurs without the permission ([overview](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview)), while the AOSP commit says rates are throttled at 200 Hz and the exception is thrown in debug mode ([commit](https://android.googlesource.com/platform/frameworks/native/+/af91a91c5c)).

Android 16 CDD numbers ([CDD 7.3, 2.2.1](https://source.android.com/docs/compatibility/16/android-16-cdd)):

| Requirement | General | Handheld | High fidelity (`hifi_sensors`) |
|---|---|---|---|
| Gyro rate | ≥ 50 Hz, SHOULD reach 200 Hz | ≥ 100 Hz | 12.5-400 Hz |
| Gyro range | n/a in extract | 1000 dps | ±1000 dps |
| Gyro resolution | ≥ 12 bits, 16 strongly recommended | n/a | ≥ 16 LSB/dps |
| Gyro noise | Variance ≤ 1e-7 rad²/s²/Hz | n/a | ≤ 0.014 dps/√Hz, SHOULD ≤ 0.007 |
| Gyro calibration | Strongly recommended < 0.01 rad/s at rest | n/a | SHOULD < 0.002 rad/s (10-40 °C) |
| Bias vs temperature | Not numeric | n/a | SHOULD ≤ ±0.05 dps/°C |
| Accel rate | ≥ 50 Hz, SHOULD reach 200 Hz | ≥ 100 Hz | 12.5-400 Hz |
| Accel noise | SD ≤ 0.05 m/s² per axis | n/a | ≤ 400 µg/√Hz |
| Report latency | ≤ 100 ms + 2 × sample_time at 0 ms requested latency | n/a | Delivery to apps ≤ 5 ms |
| First sample | ≤ 400 ms + 2 × sample_time | n/a | n/a |
| Jitter | SHOULD be < 3% | n/a | n/a |
| Timestamp sync | Strongly recommended < 100 ms, SHOULD be < 1 ms | n/a | Accel/gyro/mag within 2.5 ms; accel-gyro SHOULD be within 0.25 ms |
| Batch buffer | n/a | n/a | 3000 accel events |

Real-world rates (mostly anecdotal, small samples):
- Pixel 6a about 440 Hz via Java API, but a developer report has native capped at 200 Hz; Motorola G Power 500 Hz on both paths — [developer thread](https://groups.google.com/g/android-ndk/c/Iqi0nwLQnzE)
- Android 10 phone jumped between 500 and 333.33 Hz at `FASTEST` — [UniBw ISS 2021](https://www.unibw.de/lrt9/lrt-9.2/preprints/2021_iss_paper_mohamedbochkati.pdf)
- A phone sold with a 400 Hz gyro actually ran at 50 Hz with repeated values; a Fairphone 4 did reach 400 Hz — [arXiv 2501.04886](https://arxiv.org/pdf/2501.04886)
- Nexus 1, very old, about 95 Hz with one run at 37.68 Hz — [slide deck](https://www.slideshare.net/slideshow/motion-recognition-with-android-devices/9596036)

MEMS noise (secondary mirrors of datasheets, not original PDFs):

| Part | Gyro noise density | Other | Source |
|---|---|---|---|
| Bosch BMI270 | 7 mdps/√Hz | ZRO ±0.5 dps, ±0.015 dps/K | [datasheet mirror](https://wiki.adom.inc/kyle/bmi270) |
| TDK ICM-42688-P/V | 2.8 mdps/√Hz (source typo omits √) | ±5 mdps/°C offset stability | [TDK comparison](https://adm.invensense.tdk.com/comment/13838); [ICM-42688-V](https://en.sekorm.com/doc/3797376.html) |
| ST LSM6DSM | 3.8 mdps/√Hz | n/a | [alldatasheet](https://www.alldatasheet.net/html-pdf/796183/STMICROELECTRONICS/LSM6DSMTR/40986/21/LSM6DSMTR.html) |
| ST LSM6DST | 3.4 mdps/√Hz | n/a | [alldatasheet](https://www.alldatasheet.net/html-pdf/1447359/STMICROELECTRONICS/LSM6DSTTR/65997/29/LSM6DSTTR.html) |
| ST LSM6DSL | 4 mdps/√Hz | n/a | [alldatasheet](https://www.alldatasheet.net/html-pdf/880700/STMICROELECTRONICS/LSM6DSL/44844/23/LSM6DSL.html) |

Background and power:
- From Android 9, background apps receive no events from continuous, on-change or one-shot sensors; use the foreground or a foreground service. Unregister in `onPause`, and do not block `onSensorChanged` — [Android sensors overview](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview)
- Android 14+ requires a declared service type. `connectedDevice` covers external devices over Bluetooth, NFC, IR, USB or network connection, with a prerequisite such as `CHANGE_NETWORK_STATE` or `CHANGE_WIFI_STATE`. `specialUse` is reviewed in Play Console. `shortService` lasts about 3 minutes — [FGS types](https://developer.android.com/develop/background-work/services/fgs/service-types)
- `FLAG_KEEP_SCREEN_ON` keeps the screen on and bright while the window is visible — [WindowManager.LayoutParams](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#FLAG_KEEP_SCREEN_ON)
- `getThermalHeadroom` returns 0.0-1.0 toward `THERMAL_STATUS_SEVERE`. Call it at most once per 10 s or it may return NaN. Available from Android 11 — [Thermal API](https://developer.android.com/games/optimize/adpf/thermal)

### Inferences
- Own calculations from the CDD numbers:
  - Noise limit 1e-7 rad²/s²/Hz = 0.0181 dps/√Hz.
  - RMS over 100 Hz bandwidth = density × √(1.57 × 100): BMI270 0.088 dps, ICM-42688 0.035 dps, CDD hi-fi limit 0.175 dps, general limit 0.227 dps.
  - Unbounded integration of residual calibration error: 0.01 rad/s = 0.57 dps = 34°/min, 0.002 rad/s = 6.9°/min. At wheel gain 5 these become 172°/min and 34°/min, so **pure gyro integration is not acceptable without gravity correction.**
  - Angle random walk after 60 s from noise alone: 0.022° (2.8 mdps/√Hz) to 0.054° (7 mdps/√Hz). Bias dominates, not white noise.
- 200 Hz is enough. It gives a 5 ms period and 2.5 ms mean wait; going to 400 Hz saves only 1.25 ms more on average and needs a permission that the microphone toggle can defeat.
- The direct channel only helps if combined with the permission and native code. Its value is avoiding Binder and callback overhead, which was not quantified by any source found.
- The AOSP throttle list does not mention virtual sensors. So a game rotation vector may not be literally capped, but it is still limited by its inputs. Measure instead of assuming.
- Measure sample age per callback: `age = SystemClock.elapsedRealtimeNanos() - event.timestamp`.
- Set up per device at first launch: log 10 s of events per candidate sensor, record median rate, p95 interval and repeated values. The repeated-value check catches the "fake 400 Hz" case above.
- Foreground activity is the natural mode, because the screen carries the pedal touch zones. A `connectedDevice` foreground service is only a fallback for keeping the link alive.

### Gaps
- No systematic low-end vs flagship rate, latency or noise comparison was found, and no mapping from phone models to IMU parts. Only the CDD floor and anecdotes above are available.
- ST LSM6DSV and LSM6DSO noise values were not confirmed from primary datasheets. The original BMI270 and TDK PDFs were not read.
- The Android 16 CDD was read; a later CDD may exist in November 2026 and was not checked.
- No source found for the effect of Battery Saver or thermal throttling on sensor rates. Sensor hubs may be unaffected, while the app thread and radio may not be. Test this.
- Internal latency of `TYPE_GAME_ROTATION_VECTOR` compared with raw gyro is not published. Measure by cross-correlating its angle with integrated gyro.
- Exact `registerListener` max-report-latency semantics were not read from the API reference. Pass 0 (or use the overload without latency) and verify delivery timing.

## iOS: CoreMotion

### Takeaway
Use `CMDeviceMotion` with `xArbitraryZVertical` at the maximum supported rate, expected around 100 Hz. Treat motion as foreground-only. Do not plan on higher rates via `CMBatchedSensorManager` on iPhone without testing.

### Cited Findings
- Device motion provides attitude, unbiased rotation rate, gravity vector, gravity-free acceleration and magnetic field — [Apple: processed device-motion data](https://developer.apple.com/tutorials/data/documentation/coremotion/getting-processed-device-motion-data.md)
- Reference frames: `xArbitraryZVertical` is the default (z perpendicular to ground, x/y from starting orientation), and it or `xArbitraryCorrectedZVertical` is used to measure change relative to the starting position; `xMagneticNorthZVertical` and `xTrueNorthZVertical` are for compass use. Roll, pitch and yaw range from -π to π — [same Apple page](https://developer.apple.com/tutorials/data/documentation/coremotion/getting-processed-device-motion-data.md)
- Maximum rate is hardware dependent, "usually at least 100 Hz", and clamped. Each sample has a `timestamp` for checking timeliness — [same Apple page](https://developer.apple.com/tutorials/data/documentation/coremotion/getting-processed-device-motion-data.md)
- Apple says to stop services when the app deactivates or enters background — [same Apple page](https://developer.apple.com/tutorials/data/documentation/coremotion/getting-processed-device-motion-data.md)
- A developer reports `CMDeviceMotion` events stop after backgrounding — [Apple forums](https://developer.apple.com/forums/thread/126045)
- A workaround keeps the app alive via another background mode — [Stack Overflow mirror](https://kaweah.freedombox.rocks/kiwix/content/stackoverflow.com_en_all_2023-11/questions/20729668/continuous-accelerometer-updates-while-in-the-background-on-ios)
- A single test on iOS 26.5.2 with Core Location active received about 99.5% of samples in background — [Apple forums](https://developer.apple.com/forums/thread/841001)
- Measured 100 Hz on iPhone in a hobbyist 2023 test — [gist](https://Gist.github.com/bhupiister/b95aba7e5f7b385478ea761d0ed064cc)
- `CMBatchedSensorManager` is listed as a device-motion service — [Apple page](https://developer.apple.com/tutorials/data/documentation/coremotion/getting-processed-device-motion-data.md); the only rate figures found (800 Hz accel, 200 Hz gyro) describe Apple Watch and come from a third party — [HackerNoon](https://hackernoon.com/getting-hiqh-frequency-tennis-motion-data-from-apple-watch). `isDeviceMotionSupported` returned true in one iOS 17 report — [Apple forums](https://developer.apple.com/forums/thread/731467).

### Inferences
- At 100 Hz, the period is 10 ms and the mean wait 5 ms, about 2.5 ms worse than Android at 200 Hz. Prediction can absorb this difference.
- Use `gravity`, not `attitude.roll/pitch/yaw`, for the reason shown in the math section (Euler coupling).
- Staying alive using location or audio modes solely to read sensors carries App Store review risk (my judgment, not a sourced policy). The screen is in use anyway, so it should not be necessary.

### Gaps
- No Apple statement of an exact iPhone maximum rate by model, nor a documented ban on background delivery, was found.
- `CMBatchedSensorManager` iPhone rates are unconfirmed.
- CoreMotion's internal latency is not published. Measure against `CMLogItem.timestamp`; its time base was not verified in this research.
- Not verified from a source this session: `gravity` sign convention (expected to point toward Earth, opposite of the Android-style up vector) and `UIApplication.isIdleTimerDisabled` for keeping the screen awake. Validate both on device.

## Math: computing steering angle

### Takeaway
Compute the angle from the projected "up" vector taken from fused orientation, not Euler angles and not raw accelerometer. It ignores heading drift and yaw of the torso, and it is exact when the wheel plane tilts around a horizontal axis. It degrades as tilt nears 90°, and it cross-couples under some tilt axes, so include a gyro fallback and limit tilt.

### Cited Findings
- `TYPE_GAME_ROTATION_VECTOR` reference drift occurs around the Z axis (vertical) — [AOSP sensor types](https://source.android.com/docs/core/interaction/sensors/sensor-types)
- Android sensor axes stay fixed to the device's natural orientation (x right, y up, z out of screen) and do not swap on screen rotation; do not assume the natural orientation is portrait — [Android sensors overview](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview)
- CDD accelerometer limit is SD ≤ 0.05 m/s² per axis, used below as noise input — [CDD 7.3.1](https://source.android.com/docs/compatibility/16/android-16-cdd)

### Inferences
All formulas below were derived here and checked numerically in Python.

**1. Up vector from a quaternion (w, x, y, z), device to world, world Z up:**
```
u = q⁻¹ ẑ q = ( 2(xz − wy),  2(yz + wx),  1 − 2(x² + y²) )
```
This is the third row of the rotation matrix. If heading drifts, q' = D·q with D about ẑ. Since D fixes ẑ, u is unchanged. **So GRV heading drift cannot reach the steering angle by this route.**

**2. Angle, landscape.** With s = sign(u_x) captured at calibration:
```
θ = atan2(s·u_y, s·u_x)     // positive = clockwise facing the screen = right turn
θ_rel = wrap180(θ − θ₀)
```
Numeric check with both landscape orientations, with tilt 0° and 40°: inputs of +10, −30 and +75 returned exactly +10, −30, +75.

**3. Twist-swing.** With q_rel = q₀⁻¹ q and twist about device z:
```
twist = 2·atan2(q_rel.z, q_rel.w)      // singular only when w = z = 0 (swing 180°)
```

**4. Comparison, own numeric test** (true phone rotation r, tilt or yaw a; neutral up along device y):

| True r | Motion | Gravity atan2 | Twist of q | Euler Z from ZYX |
|---|---|---|---|---|
| 30° | World pitch 30° then roll | 30.00 | 30.00 | 26.57 |
| 30° | World pitch 60° then roll | 30.00 | 30.00 | 16.10 |
| 30° | World pitch 85° then roll | 30.00 | 30.00 | **2.88** |
| 30° | Roll then device-axis pitch 30° | 33.69 | 30.00 | 30.00 |
| 30° | Roll then device-axis pitch 60° | **49.11** | 30.00 | 30.00 |
| 30° | Roll then device-axis pitch 85° | **81.42** | 30.00 | 30.00 |
| 30° | Roll then device-axis yaw 30° | 26.57 | 30.00 | 30.00 |
| 30° | World yaw 30° + world pitch 40°, then roll | 30.00 | **18.86** (absolute) | n/a |
| 60° | Calibrated at pitch 40°, then pitch to 70° | 60.00 | 60.00 (relative) | n/a |
| 60° | Calibrated at pitch 40°, then torso yaw 30° | 60.00 | **40.46** (relative) | n/a |

Conclusions:
- **Euler angles fail** near tilt, and that is the practical gimbal-lock problem: at 85° tilt the reading drops from 30° to 2.88°. Avoid `getOrientation` and `CMAttitude.roll/pitch/yaw`.
- **Relative twist is exact for pitching but is sensitive to torso yaw** (19.5° phone error, about 98° wheel at gain 5) and inherits heading drift.
- **Gravity projection ignores world yaw and world-axis pitch, but couples under device-axis pitch:** tan θ = tan r / cos p. This matches the table; for example tan 30° / cos 60° gives 49.11°. At r = 90° all the methods agree.

**5. Tilt conditioning.** The in-plane part of up has norm |u_xy| = cos p. Error ≈ atan(σ / (g·cos p)) with σ = 0.05 m/s² on raw accel:

| Tilt p | 0° | 30° | 60° | 80° | 85° | 88° |
|---|---|---|---|---|---|---|
| Error | 0.29° | 0.34° | 0.58° | 1.68° | 3.35° | 8.31° |

Threshold |u_xy| < 0.35 is about 70° tilt. Below it, freeze gravity correction and integrate gyro z. In an ideal tilted hold gyro z has this coupling: ω_z = −sin(p)·ω_yaw from world-yaw motion, and none from pitching about an in-plane axis.

**6. Why raw accelerometer is not enough.** Hand motion adds a tangential acceleration a_t = R·α at lever arm R from the rotation centre:

| R | α | a_t | Angle error |
|---|---|---|---|
| 0.05 m | 500 °/s² | 0.44 m/s² | 2.5° |
| 0.10 m | 1000 °/s² | 1.75 m/s² | 10.1° |
| 0.15 m | 2000 °/s² | 5.24 m/s² | 28.1° |

Centripetal adds a further 0.99 m/s² at 0.1 m and 180 °/s. Hence the need for fused gravity, not raw accel.

**7. Mapping (design proposal).**
```
gain: phone ±90° → wheel ±450° is 5.0; ±60° → ±450° is 7.5
norm n = clamp((|θ_rel| − dz) / (θmax − dz), 0, 1)     // continuous after deadzone
curve c = (1 − e)·n + e·n³                             // e in [0, 0.5]; or n^γ
wheel  = sign(θ_rel) · c · 450°
```
Check with θmax = 90°, dz = 1°:

| Phone angle | 1° | 2° | 5° | 10° | 30° | 60° | 90° |
|---|---|---|---|---|---|---|---|
| Wheel at γ = 1 | 0° | 5.1° | 20.2° | 45.5° | 146.6° | 298.3° | 450° |
| Wheel at γ = 1.5 | 0° | 0.5° | 4.3° | 14.5° | 83.7° | 242.9° | 450° |

- γ = 1.5 has slope 0.335 at 5% input; γ = 2 has slope 0.1. Default linear (γ = 1) with dz of 0.5-1°, because the One Euro filter already handles rest jitter and the deadzone is a dead spot of up to 5° wheel at gain 5.
- Clamp at ±θmax and do not wrap, so a phone passing ±180° cannot flip the output. Use sequential unwrapping of the gyro-integrated state.
- Recenter: hold button or tap, set θ₀ = current θ. Gravity is absolute, so θ₀ does not drift, and no auto-recenter is needed in normal use.
- Speed-sensitive steering, only if the game or telemetry gives speed v: G(v) = G_min + (G_max − G_min) / (1 + (v/v₀)²), as a starting shape with v₀ tuned by testing. Off by default for simulators, because they usually apply their own steering model (my judgment).

**8. Pseudo-code:**
```
onSensorEvent(q, gyro_z, t):
  u = upVector(q)
  norm = hypot(u.x, u.y)
  if norm > 0.40: valid = true
  if norm < 0.35: valid = false
  dt = t - t_prev
  if valid:
    θ = atan2(s * u.y, s * u.x)
  else:
    θ = θ_prev − gyro_z_deg * dt         // right turn = clockwise = negative ω_z, in both landscapes
  θ_rel = clampUnwrap(wrap180(θ − θ0))
  θ_f = oneEuro(θ_rel, t)                // fcmin 1.5, β 0.1, dcut 10
  H = clamp(age(t) + transportEst, 0, 25 ms)
  θ_p = θ_f − rateFilt(gyro_z_deg) * H   // same sign rule; fades to 0 at low |ω|
  send(mapToAxis(θ_p), t, seq)
```

### Gaps
- Which tilt axis real users follow (world vs device, per the table) is unknown and decides whether the coupling is a real problem. Log during playtests.
- Common racing-game wheel conventions were not surveyed; check sign on each OS.
- In-hand lever-arm distance and angular-acceleration figures above are assumed inputs, not measured human data.

## Filtering and prediction

### Takeaway
Do not build Madgwick, Mahony or a Kalman filter when OS fusion exists; use them only as a fallback. A One Euro filter on the final angle with a higher dcutoff than its default (1 Hz) gives far lower jitter than fixed low-pass at the same reaction time. Prediction with gyro rate cuts most lag error at normal steering frequencies for 10-20 ms horizons.

### Cited Findings
- 1€ filter is a speed-based low-pass; decreasing fcmin reduces jitter but increases lag, and raising β reduces high-speed lag. Tuning: start with β = 0, fcmin about 1 Hz; tune fcmin while still or slow; raise β during fast movement, by powers of ten — [Casiez 1€ page](https://gery.casiez.net/1euro/)
- Adaptive cutoff is mincutoff + β × |filtered derivative| — [libavg implementation](https://sources.debian.org/data/main/liba/libavg/1.8.1-1/src/python/filter.py)
- dcutoff defaults to 1 Hz — [npm 1eurofilter](https://www.npmjs.com/package/1eurofilter)
- β order of magnitude depends on the data's units; suggested trial values are 0.001 or 0.0001 — [Casiez lab page](https://cristal.univ-lille.fr/~casiez/1euro)
- The authors report lower lag than other filters at a reference jitter reduction — [HAL record](https://hal-lara.archives-ouvertes.fr/CRISTAL-MINT/hal-00670496v1)
- Madgwick reports accuracy matching a Kalman approach with < 0.8° static and < 1.7° dynamic RMS — [Madgwick ICORR 2011 record](https://pure.ul.ie/en/publications/estimation-of-imu-and-marg-orientation-using-a-gradient-descent-a/); an earlier report states < 0.6° and < 0.8° — [Madgwick internal report (Scribd copy)](https://www.scribd.com/doc/183997331/Madgwick-Internal-Report)
- Mahony has a PI controller with an integral term for gyro bias, while Madgwick has proportional only. Higher β converges faster but noisier. Tuned example: β = 0.03311, kp = 0.13, ki = 0.3 on foot-mounted data, where Madgwick beat Mahony for heading — [Ludwig et al.](https://web.cs.ndsu.nodak.edu/~siludwig/Publish/papers/SPIE20181.pdf); on quadcopter data Mahony was better — [Ludwig & Burnham](https://web.cs.ndsu.nodak.edu/~siludwig/Publish/papers/ICUAS2018.pdf)
- Library defaults differ, for example Madgwick β 0.1 and Mahony kp 2.0 — [bb_estimator_ahrs docs](https://hexdocs.pm/bb_estimator_ahrs/bb_estimator_ahrs.epub)
- A DriveLink phone wheel uses a complementary filter at about 100 Hz UDP — [DriveLink Mobile](https://gh.r-mj.com/DevBoiAgru/DriveLinkMobile)

### Inferences
**Formulas (standard first-order form, and verified in the simulation):**
```
τ = 1/(2π·fc);  α = 1/(1 + τ/Te);  y[k] = α·x[k] + (1−α)·y[k−1]
1€: fc = fcmin + β·|dx̂|, with dx̂ low-passed at dcutoff
Complementary: θ̂ = a(θ̂ + ω·dt) + (1−a)·θ_grav;  a = τc/(τc + dt)
```
A low-pass's delay ≈ τ for slow signals: 1 Hz 159 ms, 2 Hz 80 ms, 5 Hz 32 ms, 10 Hz 16 ms, 20 Hz 8 ms, 30 Hz 5 ms.

Complementary values: 200 Hz: τc 0.5 s → a 0.9901, 1 s → 0.9950, 2 s → 0.9975. 100 Hz: 0.5 s → 0.9804, 1 s → 0.9901, 2 s → 0.9950. Steady-state error from gyro bias b ≈ b·τc, for example 0.1 dps at τc = 1 s is 0.1° phone, 0.5° wheel.

Gyro-integrating filters (complementary, Madgwick, Mahony) add essentially no delay for fast motion because the gyro path passes straight through; their tuning affects drift correction instead.

**One Euro simulation** (own; 200 Hz; units phone degrees and deg/s; rest input 0.1° at 9 Hz assumed tremor + 0.03° noise, raw SD 0.077°; lag quantized at 5 ms; onset = lag behind a ramp starting from rest, at the 10° mark):

| fcmin | β | dcutoff | Rest SD | Onset at 100 °/s | Onset at 400 °/s |
|---|---|---|---|---|---|
| 1 | 0 | 1 | 0.0085° | 120 ms | 75 ms |
| 1 | 0.007 | 1 | 0.0086° | 95 ms | 50 ms |
| 1 | 0.05 | 1 | 0.0088° | 45 ms | 25 ms |
| 1 | 0.1 | 1 | 0.0089° | 30 ms | 20 ms |
| 1 | 0.1 | 5 | 0.0104° | 20 ms | 10 ms |
| 1 | 0.1 | 10 | 0.0110° | 15 ms | 10 ms |
| **1.5** | **0.1** | **10** | **0.0147°** | **15 ms** | **10 ms** |
| 2 | 0.1 | 10 | 0.0180° | 15 ms | 5 ms |
| 3 | 0.1 | 10 | 0.0248° | 15 ms | 5 ms |
| 10 (fixed) | 0 | n/a | 0.0506° | 20 ms | 15 ms |
| 20 (fixed) | 0 | n/a | 0.0630° | 10 ms | 10 ms |

- Recommended start: **fcmin 1.5 Hz, β 0.1, dcutoff 10 Hz.** Rest SD falls 81%, to 0.015° phone or 0.07° wheel at gain 5. Compared to fixed 10 Hz, that is 3.4 times less rest jitter at similar onset lag.
- At 100 Hz the same parameters gave a rest SD of 0.0153°.
- The default dcutoff of 1 Hz costs about 15 ms more onset lag here. That is the most useful single change for this use case.
- **β is unit-dependent:** rad/s input needs β × 57.3 (5.73). Wheel-degree input at gain 5 needs β/5 (0.02). Filter in phone degrees before gain.
- Tune on device; real tremor, grip and vibration differ from the synthetic input.

**Own-fusion simulation** (120 s, 200 Hz; lever arm 0.12 m, sine steering at 0.4 and 1.7 Hz, RMS / max error in phone degrees):

| Scenario | Raw accel atan2 | Gyro only | Gated complementary |
|---|---|---|---|
| Tilt 25°, bias 0.1 dps | 9.65 / 25.3 | 7.16 / 12.9 | 3.46 / 5.3 |
| Tilt 25°, bias 0.5 dps | 9.65 / 25.3 | 34.8 / 60.9 | 17.0 / 24.5 |
| Tilt sweep 20-85°, bias 0.1 dps | 27.1 / 132 | 7.14 / 12.9 | 4.75 / 7.5 |
| Tilt sweep 20-85°, bias 0.5 dps | 27.1 / 132 | 34.8 / 60.9 | 23.3 / 35.3 |

- Gate: in-plane ≥ 0.35 g, |a| within 3% of g, |ω| < 30 °/s, |α| < 120 °/s², τc 1.5 s. The angular-acceleration gate is essential: a sinusoid has zero rate at peak acceleration, so a rate-only gate corrects at the worst moment. In this simulation |α| used the true value; a real system must estimate it from noisy gyro differences.
- **A hand-written gated filter was still weak under continuous motion, since it rarely gets to correct.** This is the argument for OS fusion and for estimating bias at rest. If building the fallback, add a rest-state detector and a bias estimate (the Mahony integral role).
- Keep custom fusion to a fallback for devices lacking a usable rotation vector.

**Prediction simulation** (own; θ_p = θ_delayed + ω·H; rate noise 0.2 dps; RMS error, phone degrees, H = delay):

| Motion | H = 10 ms: none / predicted | H = 20 ms | H = 30 ms |
|---|---|---|---|
| 0.5 Hz, ±30° | 0.67 / 0.01 | 1.33 / 0.04 | 2.00 / 0.09 |
| 1 Hz, ±45° | 2.00 / 0.06 | 3.99 / 0.25 | 5.98 / 0.57 |
| 2 Hz, ±45° | 3.99 / 0.25 | 7.97 / 1.00 | 11.9 / 2.25 |
| 3 Hz, ±30° | 3.99 / 0.38 | 7.95 / 1.50 | 11.8 / 3.36 |

- These assume a clean delay and do not model the transient on reversals or overshoot at a sudden stop.
- Limit H to 20-30 ms, low-pass the rate lightly, and fade prediction to zero when |ω| is small, so that it does not re-inject tremor.

### Gaps
- The original 1€ paper's numeric lag and jitter data could not be read (403 on the PDF). The α formula is cited from implementation and standard form, not read from the paper itself.
- Madgwick full text could not be retrieved, so these remain unverified: β = √(3/4)·ω̃ and the commonly quoted optima of 0.033 (IMU) and 0.041 (MARG).
- No study was found measuring filter parameters specifically for phone-as-wheel use, nor Kalman filter latency for this case.
- The tremor input is an assumed test signal, not cited physiology.

## Human factors: latency, resolution, ergonomics

### Takeaway
Aim for under 50 ms end to end, with phone-side contribution of 20-30 ms. Effective phone resolution at gain 5 is roughly 10.5-13 bits per revolution, well below direct-drive encoders but plausibly enough for casual and mid-level use. Transport with 16 bits so that transport is not the limiting factor. Sustained arm fatigue, rather than phone mass alone, is likely the main ergonomic limit.

### Cited Findings
Latency:
- Racing: 50 ms acceptable, above 100 ms should be avoided; treated as a worst case — [summary of Pantel & Wolf](https://cs.ubc.ca/~krasic/cpsc538a/summaries/52)
- Secondary sources say Claypool & Claypool suggest a 100 ms maximum for fast, high-precision games such as shooters and racing — [latency survey](https://ntnu.no/ojs/index.php/nikt/article/view/5252)
- Survey warning: thresholds are inconsistent between studies, and research is too sparse for strong conclusions — [latency survey](https://ntnu.no/ojs/index.php/nikt/article/view/5252)
- Reported thresholds range from 25 to 1000 ms depending on task and skill — [Regensburg dissertation](https://epub.uni-regensburg.de/75259/)
- A button-to-flash study found median motor-visual delay detection of 51-90 ms, with some people below 40 ms, and earlier work found a 39 ms median. The search summary tied these to the game-latency results, including [this MMSys 2016 paper](https://mmsys2016.itec.aau.at/papers/MMSYS/a28-westerdals.pdf) and [this Simula paper](https://web-backend.simula.no/sites/default/files/publications/files/paper_5.pdf), but **I could not confirm which paper states which number** because the PDF would not parse. Check before quoting.

Wheel encoders:

| Wheel | Resolution stated | Source quality |
|---|---|---|
| Thrustmaster T300 | 16-bit, 65,536 values (H.E.A.R.T. Hall sensor) | [retailer listing](https://www.pbtech.com/product/GAMTTM4109417/reg) |
| Logitech G923 | 16-bit | [retail blog, unverified](https://simracerzone.com/blogs/knowledge-base/thrustmaster-t248-vs-logitech-g923) |
| Logitech G29 | Not found; plastic optical encoder wheel reported | [forum](https://www.overtake.gg/threads/logitech-g25-optical-encoder-problem-g29-questions.166692/) |
| Fanatec CSL DD | Disputed: 12-bit hardware, interpolated to 16-bit data, per users | [Fanatec forum](https://forum.fanatec.com/discussion/comment/100092) |
| Moza R12 V2 | 21-bit (2,097,152 PPR) | [Moza official](https://mozaracing.com/products/r12-wheelbase) |
| Simucube 2 Pro | 22-bit | [Granite Devices forum](https://community.granitedevices.com/t/simucube-2-pro-vs-simucube-2-ultimate/11016) |

Tilt as a control:
- A Beach Buggy Blitz study reported higher immersion and better performance with tilt than touch, attributed to the natural wheel mapping. Seen via a search summary of the tilt literature; the exact paper was not confirmed — [York/MacKenzie tilt work](https://www.yorku.ca/mack/gi2014.html)
- A two-player 2018 comparison found no significant effect of tilt vs touch — [Suhaib et al.](https://www.researchgate.net/profile/Muhammad-Suhaib-6/publication/328127724_Tilt_or_Touch_An_Evaluation_of_Steering_Control_of_Racing_Game_on_Tablet_or_Smartphone/links/5bd95e1492851c6b279b9df9/Tilt-or-Touch-An-Evaluation-of-Steering-Control-of-Racing-Game-on-Tablet-or-Smartphone.pdf)
- For children in a 2D racer, a joystick outperformed the alternatives including tilt and steering schemes — [Rocha et al.](https://estudogeral.uc.pt/handle/10316/87771?locale=en)
- Position control was about twice as fast as velocity control in a tilt-control study — [Teather & MacKenzie](https://www.csit.carleton.ca/~rteather/pdfs/GI2014.pdf)

Ergonomics:
- 24-person study: phone holding with one or two hands raises neck flexion and muscle fatigue, and posture and grip matter — [Human Factors](https://api.crossref.org/works/10.1177%2F00187208221087760)
- An arm support prototype reduced neck and shoulder load and fatigue over 20 minutes of gaming (24 people). From a search summary; confirm the link is the Applied Ergonomics paper before quoting — [Scinapse record](https://www.scinapse.io/papers/3163390806)
- Heavier phones were associated with hand discomfort after prolonged use (BMC Public Health, seen only through a consumer summary) — [summary](https://digitpatrox.com/yes-your-smartphone-can-cause-hand-pain/)

### Inferences
- Own calculations, degrees per step:
  - 8 bits over 900°: 3.52°
  - 10 bits: 0.88°
  - 12 bits: 0.22°
  - 14 bits: 0.055°
  - 16 bits: 0.0137°
  - Per revolution, 16-bit is 0.0055°, 21-bit 0.00017°, 22-bit 0.000086°.
- Phone noise translated to wheel at gain 5: 0.01° → 0.05°, 0.05° → 0.25°, 0.1° → 0.5°. As counts per 360° of wheel: 0.05° ≈ 12.8 bits, 0.25° ≈ 10.5 bits. **Proposed specification: about 0.05-0.25° wheel-equivalent; transmit 16 bits.** Lower gain (a longer physical arc) directly improves this.
- Direct-drive encoder bits are not the relevant comparison on their own: a 16-bit base already exceeds what a phone sensor stream can resolve at gain 5. The phone's limits are noise, drift and lack of force feedback.
- Wheel-holder ergonomics: a 200 g phone is about 2 N; at about 0.3 m from the elbow, about 0.6 N·m across both arms. This is small compared with holding the forearms up, so **resting elbows or a supporting surface likely matters more than phone mass** (physics estimate, in line with the arm-support study). Playtest sessions of at least 20 minutes, matching the study above, and consider a mount or rim that allows elbow support.
- A 3D-printed rim can make the motion more wheel-like and encourages rotation about the phone's centre, reducing R and thus a_t. It adds mass and must leave the screen edges reachable.

### Gaps
- No steering-wheel or sim-rig latency threshold study with ms data was found; 50 and 100 ms come from network-delay game research.
- G29 encoder bits, and official Logitech and Fanatec resolutions, were not found. An AliExpress "24-slot" claim for G29 was rejected as unreliable.
- No gram-level threshold for phone fatigue, and no sourced data on 3D-printed phone wheel holders.
- Minimum wheel resolution needed for lap-time neutrality: no study found. The 0.05-0.25° target is an engineering proposal.

## Throttle, brake and framework impact

### Takeaway
Use thumb-position sliders at the screen edges for analog pedals. Avoid pressure as a primary axis because it is not portable, and avoid tilt pedals because they couple into steering. Keep sensor and touch handling in native code whichever cross-platform UI is chosen.

### Cited Findings
Android:
- `ACTION_MOVE` batches multiple samples: read historical coordinates first, then current. Multi-touch reports all active pointers. Pointer index can change, so track pointer ID with `findPointerIndex`. `getEventTime` is in `uptimeMillis` base — [MotionEvent](https://developer.android.com/reference/android/view/MotionEvent)
- `requestUnbufferedDispatch` is not meant for most apps: side effects include increased latency, jittery scrolls and loss of system resampling — [AOSP framework mirror](https://gitlab.e.foundation/e/os/android_frameworks_base/-/commit/2a89b08eb1d48208f52eeaf12cb8b687abe81e1c)
- Buffering aligns touches to frames and resamples them; extrapolation horizon is capped to 8 ms — [Google design doc](https://docs.google.com/document/d/1L2JTgYMksmXgujKxxhyV45xL8jNhbCh60NQHoueKyS4/export?format=pdf)

iOS:
- `UITouch.force` is available on devices supporting 3D Touch or Apple Pencil; check `forceTouchCapability`. A value of 1.0 is an average touch — [Apple UITouch.force](https://developer.apple.com/tutorials/data/documentation/uikit/uitouch/force.md)

Touch rates:
- 120 Hz = 8.3 ms, 240 Hz = 4.2 ms, 480 Hz = 2.1 ms — [touch rate explainer](https://gadgetstouse.com/blog/2020/03/06/what-is-touch-sampling-rate)
- ROG Phone 6 reported at 720 Hz touch, 165 Hz display; refresh and touch rates are separate specs — [DXOMARK](https://www.dxomark.com/?p=74683); [explainer](https://gadgetstouse.com/blog/2021/08/03/check-touch-sampling-rate-response-rate-display-android)

Frameworks:
- Flutter `sensors_plus`: sampling rate on Android is not guaranteed, and it varies by version, hardware and vendor — [sensors_plus](https://pub.dev/documentation/sensors_plus/latest/)
- `expo-sensors` needs the high-rate permission above 200 Hz — [Expo gyroscope](https://docs.expo.dev/versions/v57.0.0/sdk/gyroscope.md)
- Hobbyist Expo test: about 100 Hz iPhone, about 160 Hz Android — [gist, 2023](https://Gist.github.com/bhupiister/b95aba7e5f7b385478ea761d0ed064cc)
- Flutter maintains platform-channel benchmarks, but the results found have no figures — [Flutter benchmarks](https://flutter.googlesource.com/mirrors/flutter/+/58499c29ea46b66216cc4053476ae44cf1adfa95/dev/benchmarks/platform_channels_benchmarks/)

Open-source references:
- GyroDrive: gyro phone wheel with UDP and ViGEm, archived — [GyroDrive](https://github.com/devsuju/GyroDrive-PC-Server/releases)
- DriveLink: mobile and desktop, about 100 Hz UDP, vJoy — [DriveLink Desktop](https://gh.r-mj.com/DevBoiAgru/DriveLinkDesktop)
- tmphonepad: web app, ViGEm, claims 1-3 ms latency over USB tethering — [tmphonepad](https://www.Github.com/stylpe/tmphonepad)
- Virtual Gamepad: Flutter and .NET, ViGEmBus — [Virtual Gamepad](https://github.com/KaifCoding/virtual-gamepad)

### Inferences
Pedal options:

| Option | Precision | Added latency | Issues |
|---|---|---|---|
| **Thumb position slider (recommended)** | Analog | Touch wait of T/2, plus OS input path | Thumbs are also holding the phone; provide a generous zone and calibration |
| On/off touch zones | Digital | Same | Simple; poor for trail braking |
| Pressure | Device-dependent | Same | iOS: 3D Touch hardware only. Android: `getPressure` semantics vary (unverified). Optional extra only |
| Tilt forward/back | Analog | IMU path | **Couples into steering.** At 25° device-axis tilt, 30° reads as 32.5°, a 12.5° wheel error at gain 5 |
| Second phone | Analog, by tilt or touch | Second radio link | Needs clock sync and a foot or hand mount |
| Physical pedals or Bluetooth device | Best analog | Separate path to the PC is cleanest | Costs money and removes the phone-only appeal |
| Volume keys | Digital | Unknown | Possibly usable on Android for shift; iOS behaviour unverified |

- Pedal mapping suggestion: y = clamp((pos − p₀)/(p₁ − p₀), 0, 1), an adjustable curve and a 2-3% end deadzone. These are design proposals.
- Touch event age: compare `getEventTime` against `uptimeMillis` for an estimate. Do not use the same subtraction with sensor timestamps, which use the `elapsedRealtime` base.
- Do not use unbuffered dispatch by default. If used for pedals, test on real devices, given the warnings. Send touch samples on the same packet as the steering sample, without waiting for a frame. Consume historical samples.
- Framework recommendation, mechanism reasoning rather than measured figures:
  - **Native (Kotlin + Swift):** baseline.
  - **Kotlin Multiplatform:** shared math, no bridge on Android; platform interop for iOS sensors. Expected to be the best shared-code option for filter and protocol logic.
  - **Flutter or React Native UI:** acceptable only if a native module owns sensor registration, filtering and UDP socket on its own thread. Passing each sample through EventChannel, a JS thread or JSI to reach the network adds queueing and garbage-collection jitter.
- Android: register the listener with a dedicated `HandlerThread`, and perform little work in `onSensorChanged`, per guidance above.

### Gaps
- No trustworthy measured touch-to-app numbers for specific phones; DXOMARK's chart is an image. iPhone touch sampling was not verified.
- Apple's coalesced-touch documentation did not load, so `coalescedTouches`, `predictedTouches` and ProMotion rates are unverified here; do not treat as sourced.
- No benchmark data for Flutter channels, React Native JSI/TurboModules or Kotlin Multiplatform sensor latency. Build a small test app to measure them.
- GyroDrive's materials list ViGEm as archived. That status was not checked at ViGEm's own project page, and PC-side details are outside this note's scope.

## Haptics as an FFB substitute

### Takeaway
Haptics cannot replace force feedback, since they cannot apply torque to resist or centre the hand. They are feasible as cue channels: kerbs, ABS, wheel slip, gear shifts and impacts. Use Android envelopes or presets with fallback, and Core Haptics on iOS. Latency is unpublished and must be measured.

### Cited Findings
Android:
- `hasAmplitudeControl`: without it, all amplitudes map to on or off. Non-zero values round up to 100% — [Android haptics APIs](https://developer.android.com/develop/ui/views/haptics/haptics-apis)
- `VibrationEffect.Composition` is on Android 11+ and has no automatic fallback: one unsupported primitive and nothing plays; per-primitive checks are fully available from Android 12 — [Android haptics APIs](https://developer.android.com/develop/ui/views/haptics/haptics-apis)
- From Android 16 (26Q4), `VibrationEffect.Builder` is preferred, with timeline scheduling, presets, envelopes and automatic fallback, except for `WaveformEnvelopeBuilder` — [Android haptics APIs](https://developer.android.com/develop/ui/views/haptics/haptics-apis)
- Envelopes: devices with support must handle a minimum of 20 ms between control points and at least 16 points; envelope intensity starts and ends at 0. Frequencies out of the FOAM range do not play. Check `areEnvelopeEffectsSupported` and query limits with `getMinControlPointDurationMillis`, `getMaxSize` etc. — [Android haptics APIs](https://developer.android.com/develop/ui/views/haptics/haptics-apis)
- A 5-10 ms gap between primitives is too short to detect; use about 50 ms. If an element is still playing, the next one shifts later, so close scheduling produces drift — [Android custom haptics](https://developer.android.com/develop/ui/views/haptics/custom-haptic-effects)
- Buzzy on/off patterns are discouraged — [Android haptics APIs](https://developer.android.com/develop/ui/views/haptics/haptics-apis)

iOS:
- `CHHapticEngine` is a connection to the haptic server, and the OS can override requests with system haptics. There are `stoppedHandler` and `resetHandler` callbacks, `capabilitiesForHardware()`, `CHHapticTimeImmediate`, advanced players and `isAutoShutdownEnabled` — [Apple CHHapticEngine](https://developer.apple.com/tutorials/data/documentation/corehaptics/chhapticengine.md)
- Intensity and sharpness each range 0-1 — [HackerNoon guide](https://hackernoon.com/the-ios-guide-to-haptic-feedback)
- Starting the engine early avoids a first-haptic delay — [react-native-haptic-feedback docs](https://mkuczera.github.io/react-native-haptic-feedback/guide/ios.html)
- UIKit generators are said to have about 10-20 ms delay, reduced by `prepare()`. Low-quality source, not about Core Haptics — [skills.lc guide](https://www.skills.lc/megastep/codex-skills/megastep-codex-skills-axiom-haptics-skill-md)

### Inferences
- Mapping proposal, using game telemetry over a return UDP stream:

| Game event | Android | iOS |
|---|---|---|
| Kerb, rumble strip | Envelope or low-sharpness pulses at about 20-50 Hz repetition | Continuous event with sharpness and intensity curves |
| ABS or wheelspin | Repeated preset ticks ≥ 50 ms apart | Repeated transients |
| Impact | Strong preset or `PRIMITIVE_THUD` where supported | Strong transient |
| Gear shift | `PRESET_CLICK`/`CLICK` | Sharp transient |

- Effective update rate is limited to about 20-50 Hz (20 ms envelope segment floor; 50 ms suggested gap). Do not resend per physics tick; send state changes and pre-built short effects.
- Capability tiers: (A) envelope support or Core Haptics; (B) amplitude control; (C) on/off, cues only, or off by default. Provide an intensity slider.
- **Self-interference risk:** vibration couples into the IMU, the same phone being sensed. Test IMU noise with haptics on and off; if it matters, lower intensity and keep the rest-state detector from reading vibration as motion. This risk is reasoned, not measured.

### Gaps
- No measured haptic actuation latency for `VibrationEffect` or Core Haptics was found.
- No primary numbers for specific LRA or ERM response time, or primitive durations (query `getPrimitiveDurations` at runtime).
- No user study was found about phone haptics as an FFB substitute in racing. Effect on IMU data is unmeasured.
