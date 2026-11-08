<p align="center">
  <img src="desktop/app/icons/icon-256.png" width="120" alt="Virtual Wheel Racing logo: black racing wheel with red grips">
</p>

<h1 align="center">Virtual Wheel Racing</h1>

<p align="center">Turn your Android phone into a steering wheel for PC racing and truck games.</p>

<p align="center">
  <strong>English</strong> · <a href="README.pt-BR.md">Português</a> · <a href="LICENSE">GPL-3.0 license</a> · <a href="CONTRIBUTING.md">Contributing</a>
</p>

---

Hold your phone like a steering wheel: it sends steering, pedals and buttons over Wi-Fi. The Windows app receives them and feeds a virtual joystick ([vJoy](https://github.com/jshafer817/vJoy)) that games see as a regular controller. You map the controls inside each game, just like with a real wheel.

> **Status:** working prototype (version 0.1.1). Validated with rFactor 1 on a Poco X8 Pro (Android 16) and Windows 11 with vJoy 2.1.9.1. Open source under GPL-3.0 — testing on other games and phones is the most valuable contribution right now.

## How it works

```
 Phone (Android)                                     PC (Windows)
 rotation sensor ─┐                            ┌─► authenticated session ─► vJoy ─► game
 pedals, buttons ─┴─► signed UDP packet ───────┘     failsafe: releases pedals and
                      up to 200 times per second     centers steering if the link drops
```

- **Steering:** the angle comes from Android's own sensor fusion at 200 Hz, smoothed by a One Euro Filter.
- **Pedals:** brake and throttle are strips on the screen edges, driven by thumb position.
- **Pairing:** the phone scans a QR code shown on the PC, carrying a random 128-bit key. Each connection opens a session with its own key.
- **Track safety:** after 100 ms without packets the PC releases the pedals; after 500 ms it centers the steering.

The full design (in Portuguese) is in [docs/arquitetura.md](docs/arquitetura.md).

## Repository layout

| Folder | Contents |
|---|---|
| `shared/domain` | Pure rules: angle math, filter, curves, failsafe, sensor statistics |
| `shared/protocol` | 32-byte packet format, HMAC, key exchange and QR code |
| `android/engine` | Phone real-time engine: sensor, network, session |
| `android/app` | Android screens (Jetpack Compose) |
| `desktop/receiver` | UDP receiver, session, watchdog and statistics on the PC |
| `desktop/vjoy` | The only module that talks to vJoy (via JNA) |
| `desktop/app` | Windows app (Compose for Desktop), PC check and installer |
| `docs/` | Architecture and technical decisions |
| `reports/`, `research_notes/` | Feasibility study behind the project |
| `spikes/` | Throwaway experiments (such as the vJoy test script) |
| `licenses/` | Licenses of fonts and third-party components |

## Development requirements

- **Windows 10 or 11** (the receiver and vJoy are Windows-only)
- **JDK 17** (`JAVA_HOME` pointing to it)
- **Android SDK** with platform 36, set in `local.properties` (`sdk.dir=...`)
- **vJoy 2.1.9.1** to test with games (the app can also install it from the PC check screen)
- An **Android 10+** phone with a gyroscope, with USB debugging enabled to install over a cable

Gradle comes with the wrapper (`gradlew`); no install needed.

## Commands

Run all JVM tests (domain, protocol, receiver):

```bash
gradlew.bat test
```

Also run the contract test against the real vJoy driver (moves the virtual wheel):

```bash
gradlew.bat :desktop:vjoy:test -Pvwr.vjoy=true
```

Run the Windows app in development mode:

```bash
gradlew.bat :desktop:app:run
```

Build and install the app on a phone connected over USB:

```bash
gradlew.bat :android:app:assembleDebug
```

```bash
adb install -r android\app\build\outputs\apk\debug\app-debug.apk
```

Build the Windows installer (WiX is downloaded automatically the first time):

```bash
gradlew.bat :desktop:app:packageMsi
```

| Output | Path |
|---|---|
| Debug APK | `android/app/build/outputs/apk/debug/app-debug.apk` |
| Windows installer | `desktop/app/build/compose/binaries/main/msi/Virtual Wheel Racing-0.1.1.msi` |

## Using it

1. **On the PC:** install `Virtual Wheel Racing-0.1.1.msi` and open it. In **PC check** (Verificação do PC), use the buttons to install vJoy, configure the device and open the firewall until every item is OK.
2. **On the phone:** open the app, tap **Read QR code** (Ler código QR) and point it at the code in the PC's **Connect phone** section. The phone remembers the PC afterwards.
3. **On the phone:** tap **Calibrate** once to test the sensor, then **Start driving**.
4. **In the game:** pick **vJoy Device** in the control options. Use the **Assign** buttons on the driving screen to move one axis at a time while the game asks for each control (X axis = steering, Y = throttle, Z = brake, buttons 1 to 16). Set the game's deadzone to 0%.

Phone and PC must be on the same network, ideally 5 GHz Wi-Fi with the PC on a cable. The app uses **UDP port 47820**.

> The app's interface is in Brazilian Portuguese for now. Translations are welcome.

## Games

| Game | Status |
|---|---|
| rFactor 1 | Validated: no axis inversion, no noticeable lag |
| Euro Truck Simulator 2, American Truck Simulator, Assetto Corsa, BeamNG.drive | Main targets, not yet tested with Virtual Wheel Racing |
| F1, EA WRC, Forza Horizon, GTA V | Out of scope for now (anti-cheat, or Xbox controller only) |

Tested another game? Open an issue with the **Game compatibility** template (Compatibilidade de jogo), whether it worked or not — that's how the list grows.

## Known limitations

- The installer and the app are **not code-signed** yet; Windows shows a SmartScreen warning and the APK is a debug build.
- The installer does not install vJoy or open the firewall during setup; that happens on first launch, in the PC check screen.
- The pairing key is stored without extra protection (a file on the PC, preferences on the phone).
- No track vibration or game telemetry yet.
- The QR scanner uses Google Play Services, which is not free software; replacing it with a free scanner is planned.

## Contributing

Contributions are welcome: game and phone testing, bug reports, code and documentation. Read the [contributing guide](CONTRIBUTING.md) and the [code of conduct](CODE_OF_CONDUCT.md) (both in Portuguese for now). Report security issues privately, as described in [SECURITY.md](SECURITY.md). Issues and pull requests in English are welcome.

## License

Virtual Wheel Racing is free software, released under the [GNU General Public License v3.0](LICENSE). You may use, study, modify and redistribute it, including commercially, as long as distributed versions stay open source under the same license.

Third-party components keep their own licenses, listed in [licenses/THIRD-PARTY.md](licenses/THIRD-PARTY.md): the vJoy installer (MIT), the fonts (OFL) and Apache 2.0 libraries, all compatible with GPL-3.0.
