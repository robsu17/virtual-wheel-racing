<p align="center">
  <img src="desktop/app/icons/icon-256.png" width="120" alt="Virtual Wheel Racing logo: black racing wheel with red grips">
</p>

<h1 align="center">Virtual Wheel Racing</h1>

<p align="center">Turn your Android phone into a steering wheel for PC racing and truck games.</p>

<p align="center">
  <a href="https://github.com/robsu17/virtual-wheel-racing/actions/workflows/ci.yml"><img src="https://github.com/robsu17/virtual-wheel-racing/actions/workflows/ci.yml/badge.svg" alt="CI status"></a>
  <a href="https://github.com/robsu17/virtual-wheel-racing/releases"><img src="https://img.shields.io/github/v/release/robsu17/virtual-wheel-racing?include_prereleases&label=release" alt="Latest release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/robsu17/virtual-wheel-racing" alt="License: GPL-3.0"></a>
  <img src="https://img.shields.io/badge/platform-Android%2010%2B%20%7C%20Windows%2010%2F11-informational" alt="Platforms: Android 10+ and Windows 10/11">
</p>

<p align="center">
  <strong>English</strong> · <a href="README.pt-BR.md">Português</a>
</p>

---

Hold your phone like a steering wheel: it sends steering, pedals and buttons over your local Wi-Fi. The Windows app feeds them into a virtual controller ([vJoy](https://github.com/jshafer817/vJoy)) that games see as a regular wheel. No extra hardware, no account, no internet.

> **Status:** first public release (0.1.1). Validated with rFactor on a Poco X8 Pro (Android 16) and Windows 11. Testing on other games and phones is the most valuable help right now.

## Download

**[⬇ Get the latest release](https://github.com/robsu17/virtual-wheel-racing/releases)** — you need both files:

| File | Install on |
|---|---|
| `virtual-wheel-racing-<version>-windows.msi` | Your PC (Windows 10 or 11). Includes the vJoy driver installer. |
| `virtual-wheel-racing-<version>-android.apk` | Your phone (Android 10 or newer, with a gyroscope). Allow "install unknown apps" for your browser or file manager. |

Each release lists the SHA-256 checksum of every file, so you can confirm your download.

## Quick start

1. **PC:** install the MSI and open Virtual Wheel Racing. In **PC check** (*Verificação do PC*), click the buttons until every item is OK: install vJoy, configure the device, open the firewall. Windows asks for confirmation each time.
2. **Phone:** open the app, tap **Read QR code** (*Ler código QR*) and scan the code in the PC's **Connect phone** (*Conectar celular*) section. The phone remembers the PC.
3. **Phone:** tap **Calibrate** once to test the sensor, then **Start driving** (*Começar a dirigir*) and hold the phone like a wheel.
4. **Game:** in the control options, pick **vJoy Device** and map steering (X axis), throttle (Y), brake (Z) and buttons. The **Assign** buttons (*Atribuir*) on the driving screen move one axis at a time, so the game detects the right one. Set the game's deadzone to 0%.

> The app's interface is in Brazilian Portuguese for now. Translations are welcome.

## Features

- **Steering from the phone's sensors** at up to 200 Hz, using Android's sensor fusion and a One Euro filter: smooth when still, quick when turning.
- **Analog pedals** on the screen edges, controlled by thumb position; choose which side the brake is on.
- **16 buttons** in two pages, mapped freely in each game.
- **QR pairing** with a random 128-bit key; every connection opens a session with its own key.
- **Failsafe:** if the Wi-Fi drops, the PC releases the pedals within 100 ms and centers the steering within 500 ms.
- **Sensor test** that measures your phone's real sample rate and switches to a reduced mode on slow sensors.
- **Universal setup:** one wheel for every game, configured inside the game like a real wheel.

## Supported games

| Game | Status |
|---|---|
| rFactor | ✅ Validated |
| Euro Truck Simulator 2, American Truck Simulator, Assetto Corsa, BeamNG.drive | 🎯 Main targets, not yet tested |
| F1 series, EA WRC, Forza Horizon, GTA V | ⛔ Out of scope for now (anti-cheat or Xbox-only controls) |

The full list, with tips, is in **[docs/games.md](docs/games.md)**. Tested another game? Open an issue with the *game compatibility* template.

## Privacy and permissions

- **Local network only.** Phone and PC talk directly over your Wi-Fi on UDP port 47820. Nothing goes to the internet.
- **No data collection.** No accounts, analytics, ads or telemetry.
- **Administrator rights are asked only for three steps,** each confirmed by you in the Windows prompt: installing vJoy, configuring its device, and adding a firewall rule limited to this app and to private networks.
- **The phone needs no special permission.** QR scanning uses Google's scanner, which opens the camera on its own without giving the app camera access.
- **Every packet is signed.** Devices that did not scan your QR code cannot control the wheel.

## Troubleshooting

| Problem | What to check |
|---|---|
| The phone keeps saying *Procurando o PC* (searching) | Phone and PC on the same network (not a guest network); **Open firewall** is OK in PC check; Virtual Wheel Racing is open on the PC. |
| Windows shows "Windows protected your PC" | The installer is not code-signed yet. Click **More info › Run anyway**. You can compare the file's SHA-256 with the one on the release page. |
| The game does not list vJoy Device | Run **PC check**; all items must be OK. Restart the game after installing vJoy. |
| Steering is reversed or too fast | Use the game's invert option or steering lock. On the phone, adjust **Phone rotation** (*Giro do celular*) in Settings (*Ajustes*). |
| Steering stops when holding the phone flat | The app freezes steering beyond about 70° of tilt. Hold the phone more upright. |
| Lag or stutter | Use 5 GHz Wi-Fi and connect the PC to the router with a cable. Check *Intervalo entre pacotes* in the PC's dashboard. |

## Known limitations

- The Windows installer is not code-signed yet, so Windows shows a SmartScreen warning. The Android APK is signed.
- The MSI does not install vJoy during setup; that happens on first launch, in PC check.
- The pairing key is stored without extra protection (a file on the PC, app preferences on the phone).
- No track vibration or game telemetry yet.
- The QR scanner relies on Google Play Services, which is not free software; replacing it is planned.

## Building from source

Requirements: Windows 10/11, JDK 17, Android SDK platform 36 (set `sdk.dir` in `local.properties`), and vJoy 2.1.9.1 to test with games. Gradle comes with the wrapper.

```bash
gradlew.bat test :android:engine:testDebugUnitTest
```

```bash
gradlew.bat :android:app:assembleDebug :desktop:app:packageMsi
```

```bash
gradlew.bat :desktop:app:run
```

The APK lands in `android/app/build/outputs/apk/debug/` and the MSI in `desktop/app/build/compose/binaries/main/msi/`. `assembleRelease` builds an optimized APK; it is only signed on the maintainer's machine (see [docs/release.md](docs/release.md)). `tools/vjoy-teste.ps1` moves the vJoy axes without a phone, handy for mapping a game.

| Folder | Contents |
|---|---|
| `shared/domain` | Pure rules: steering math, filter, curves, failsafe, sensor statistics |
| `shared/protocol` | 32-byte packet, HMAC, key exchange, QR code |
| `android/engine`, `android/app` | Phone real-time engine and screens (Jetpack Compose) |
| `desktop/receiver`, `desktop/vjoy`, `desktop/app` | PC receiver, vJoy adapter, Windows app and installer (Compose for Desktop) |
| `docs/` | Architecture (in Portuguese) and game compatibility |

## Contributing

Game and phone testing, bug reports, translations, code and docs are all welcome. Read the [contributing guide](CONTRIBUTING.md) and the [code of conduct](CODE_OF_CONDUCT.md) (in Portuguese for now; issues and pull requests in English are welcome). Commits follow [Conventional Commits](https://www.conventionalcommits.org/). Report security issues privately, as described in [SECURITY.md](SECURITY.md).

## Credits

- [vJoy](https://github.com/jshafer817/vJoy), by Shaul Eizikovich and maintained by Justin Shafer, provides the virtual controller.
- Inspired by JD SimWheel, a discontinued Android app that did this years ago.
- Fonts: Barlow Semi Condensed and Inter (SIL Open Font License).

## License

Virtual Wheel Racing is free software under the [GNU General Public License v3.0](LICENSE): you may use, study, modify and share it, including commercially, as long as distributed versions stay open under the same license. Third-party components keep their own licenses, listed in [licenses/THIRD-PARTY.md](licenses/THIRD-PARTY.md).
