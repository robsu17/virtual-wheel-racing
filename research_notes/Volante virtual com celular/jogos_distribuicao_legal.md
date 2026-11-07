# Phone-as-wheel: game input compatibility, telemetry, anti-cheat, distribution, legal and effort (as of 2026-11-06)

Reading notes for the report writer:
- Evidence tags used in the matrix: **[A]** official docs, **[B]** community or vendor report, **[C]** inference or not verified.
- These pages blocked direct fetching (403 or timeout): EA forums, help.ea.com, support.forza.net, forums.forza.net (now redirects). Claims citing them rest on search-result excerpts, not on a full read. Re-check them in a browser before quoting them as fact.
- The OpenFFBoard wiki excerpts came through a proxy mirror. The canonical page is linked but was not read in full.
- Legal content is informational, not legal advice.
- No hands-on testing was done. Every game cell needs an in-game test.

## 1. How does each game treat generic DirectInput, vJoy and XInput devices?

### Takeaway
Sims that use free axis binding (ETS2/ATS, AC, ACC, BeamNG, rF2, LMU, AMS2, iRacing) are the natural fit for a vJoy DirectInput wheel, though the evidence is mostly community reports and some cells are unverified. F1, FH5 and GTA V are the problem titles: a list or action-map model in F1, wheel-tab gating plus the EmuWheel workaround in FH5, and pad-only behaviour in GTA V. The XInput path is the safer default for those three.

### Cited Findings

**Current title status**
- No standalone "F1 26" was found. The current product is F1 25 with a 2026 Season Pack/Edition, released June 3, with patches up to at least v1.25 and a new game teased for 2027 — [Box This Lap](https://boxthislap.org/f1-25-update-1-25-f2-2026-sports-update-and-hardware-support/); [EA v1.24 notes](https://www.ea.com/de/games/f1/f1-25/news/f1-25-2026-season-pack-patch-notes-v124).
- FH6 is released (May 19 launch; the source does not state the year in that sentence) — [FinalBoss](https://finalboss.io/forza-horizon-6-wheel-support-is-messy-at).

**Compatibility matrix**

| Game | Generic DirectInput / vJoy | XInput pad | Binding and known problems | Evidence |
|---|---|---|---|---|
| **F1 23/24/25 (+2026 pack)** | Risky. EA adds wheels by patch: T98 and RS50 in [v1.13](https://racinggames.gg/article/f1-25-v113-patch-adds-major-wheel-support-fixes-game-breaking-career-save-bug), [Cammus in v1.20](https://forums.ea.com/blog/f1-games-game-info-hub-en/f1%C2%AE-25-2026---v1-20---patch-notes/13453211), [Asetek in v1.24](https://www.bsimracing.com/f1-25-2026-season-pack-update-v1-24/). Users [request official support](https://forums.ea.com/idea/f1-25-bug-reports-en/please-add-official-support-for-pxn-vd10--w-cs-r2-direct-drive-wheel/13561906) for unlisted wheels. An EA forum reply on F1 23 says [vJoy use is unsupported](https://forums.ea.com/discussions/f1-23-en/f1-23-crash-to-desktop-during-specific-race-events/8421646/replies/8421648). | Official controller path. | Unlisted wheel read as controller/keyboard, limiting steering; fixed by a [custom actionmap that tells the game the device is a wheel](https://forums.ea.com/discussions/f1-24-technical-issues-en/asetek-wheel-is-not-working--recognized-as-controllerkeyboard/8441232). Older titles: vJoy worked in [F1 2021 (Steam user)](https://steamcommunity.com/app/1134570/discussions/0/5190945662882743122). Two-device problem: [wheel base plus pedals both on USB often goes unrecognised](https://forums.ea.com/discussions/f1-24-technical-issues-en/thrustmaster-tx-wheel-not-being-recognized/8426254). [G920 joy.cpl error appears only while F1 25 runs](https://forums.ea.com/idea/f1-25-bug-reports-en/bug---g920/13816361). [Bitdefender can block inputs](https://forums.ea.com/discussions/f1-25-general-discussion-en/logitech-pro-wheel-issue-on-f1-25/12800594/replies/12836224). | [B] |
| **ETS2 / ATS** | Yes. Several phone apps use vJoy for ETS2: [Engine](https://github.com/whitescent/Engine), [Steering Wheel Emulator](https://mrsomebody02.itch.io/steering-wheel-emulator). ATS uses the same engine family but was not separately tested in these sources. | Not checked. | [Wheel subtype, sensitivity 100%, non-linearity 0-10%, deadzone 0](https://steamcommunity.com/app/227300/discussions/0/3033726413401664299) (one user's advice; thread attribution approximate). [Stick axis behaving like velocity](https://steamcommunity.com/app/227300/discussions/0/3805027459318826439) reported (thread attribution approximate). | [B] |
| **Assetto Corsa** | Yes: [AndroidSteering tested AC](https://www.github.com/teamclouday/AndroidSteering); [MouseToVJoy built for AC, with FFB](https://github.com/R1PeR/MouseToVJoy). | Gamepad mode exists; CSP [GamepadFX](https://www.overtake.gg/downloads/csp-gamepadfx-falloff.50177/) tunes it. | Manual binding. | [B] |
| **ACC** | Probably. vJoy is mentioned for [axis splitting, and analogue sticks bind without a tool](https://steamcommunity.com/app/805550/discussions/0/3879344844062595156). No direct proof of vJoy steering. SimHub user says [ACC, iRacing, AC and rF2 accept vJoy buttons](https://simhubdash.com/community-2/simhub-support/control-mapper-unable-to-use-the-same-target-vjoy-device-in-simhub-and-stream-deck-iraceit). | Yes, with filters (see section 2). | Thread-to-claim mapping for ACC comes from excerpts. | [B/C] |
| **AC EVO** | A third-party hub lists [DirectInput with vJoy axis assignment in-game](https://github.com/Barden-dev/VirtualWheel-Hub/wiki). | Not checked. | Steam Input is [mentioned as suspect in FFB issues](https://steamcommunity.com/app/3058630/discussions/0/756142145462659151?l=german). Early-access behaviour may change. | [B, single vendor] |
| **FH5** | Partial. [FH5 only showed a G923 when vJoy and EmuWheel ran](https://forums.forza.net/t/g923-wheel-not-detected-without-vjoy-and-emuwheel-does-yours-work-without-any-extra-software/537410). [Wheel tab missing](https://forums.forza.net/t/why-are-both-forza-horizon-4-and-5-for-pc-missing-the-wheel-tab-option-in-control-settings-tab/725173) complaints. [PC Gamer on the workaround](https://www.pcgamer.com/uk/forza-horizon-5-wheel-disconnect-fix-force-feedback-working). [vJoy driver crash at launch](https://www.github.com/shauleiz/vJoy/issues/60) reported. | Yes. Likely the default route. | [EmuWheel setup needs a registry file and repeats after remapping](https://steamcommunity.com/app/1293830/discussions/0/3049485912455199727). [No soft lock](https://briankoponen.com/forza-horizon-5-logitech-g29-g920-settings). [Pad: reported 5-10% centre dead range even at 0/100](https://forums.forza.net/t/why-is-there-a-5-10-centre-deadzone-on-controller/557532). | [B] |
| **FH6** | Unverified for vJoy. [Unlisted brands fall into "compatibility mode", may appear as generic DirectInput with uneven mapping/FFB](https://finalboss.io/forza-horizon-6-wheel-support-is-messy-at). | Yes (assumed; not separately sourced). | [Official list exists; FFB goes only to "Device 1"](https://support.forza.net/hc/en-us/articles/51674028831251-FH6-Supported-Wheels-and-Devices). | [B/C] |
| **Forza Motorsport (2023)** | Yes in principle: [DirectInput API, wider hardware, manual setup](https://racinggames.gg/article/forza-motorsport-steering-wheel-support-list-revealed). [No EmuWheel needed](https://community.granitedevices.com/t/forza-and-simucube-2/3948/141). No vJoy-specific report found. | Yes. | Manual binding. | [A/B, vJoy C] |
| **BeamNG.drive** | Yes. [vJoy guide; Automatic filter resolves to wheel (direct) for analogue input; newer vJoy failed for that author](https://forum.beammp.com/t/sim-racing-full-mouse-control-and-vehicle-dynamics/1336720). | Yes. | [Official: use Direct filter for wheels](https://documentation.beamng.com/support/hardware/steering_wheel_common_problems/). | [A/B] |
| **iRacing** | Yes in practice: [irFFB uses vJoy; calibration raw range 0-65535 identifies the vJoy device; HidGuardian hides the real wheel](https://github.com/nlp80/irFFB/wiki/Configuration). | Not checked. | Calibration step required. Policy: see section 4. | [B] |
| **rFactor 2** | Unverified. [vWheel Hub says basic controls work](https://github.com/Barden-dev/VirtualWheel-Hub/wiki/Game-Compatibility-and-Plugins). | Not checked. | No direct evidence found. | [C] |
| **Automobilista 2** | Mixed. [Custom wheel in control scheme](https://steamcommunity.com/app/1066890/discussions/0/3408678077959702310) (thread attribution approximate). A forum user calls [vJoy a hack that crashes some games](https://forum.reizastudios.com/goto/post?id=267341). | Not checked. | Reset by renaming the profile's controller settings file. | [B] |
| **Le Mans Ultimate** | Probably. Listed by [vWheel Hub, with an input fix for high-frequency input issues](https://github.com/Barden-dev/VirtualWheel-Hub/wiki/Game-Compatibility-and-Plugins). [Official guide has a gamepad tab](https://guide.lemansultimate.com/hc/en-gb/articles/13182838936975). | Yes. | Relevant warning: high-frequency input can cause issues, so rate-limit output. | [B] |
| **Dirt Rally 2 / EA WRC** | Action-map model. [OpenFFBoard EA WRC map binds steering to DirectInput X](https://github.com/Ultrawipf/OpenFFBoard/wiki/Games-setup). [Dirt Rally disables some FFB and H-shifter features unless wheel-detected](https://code-monet.github.io/sim-gamer-kit/game_guides/issues/). | Yes. | [Asetek publishes a WRC input guide](https://www.asetek.com/simsports/wp-content/uploads/2024/05/WRC-Input-guide.pdf). vJoy itself not confirmed. Javelin applies to EA WRC. | [B/C] |
| **GTA V** | Not native. [Hackster: only official gamepads accepted, used X360CE](https://www.hackster.io/news/the-30-arduino-hack-every-gta-v-fan-should-build-b7a6d9901ade). | Yes. Required path. | [x360ce wheel worked but with no FFB](https://steamcommunity.com/app/271590/discussions/0/611703999964693971). [ScriptHook/LSPDFR mods broke detection](https://www.lcpdfr.com/profile/298128-chuckdelta/). | [B] |
| **Gran Turismo** | Not on PC, per the brief. Excluded. | n/a | n/a | n/a |

**vJoy mechanics**
- A specific vJoy configuration enabling Accelerator, Brake and Steering axes [lets vJoy be detected as a wheel by DirectInput](https://code-monet.github.io/sim-gamer-kit/fffsake/vjoy_configuration/). It needs a patched `vJoyConf.exe`, DirectInput sees only the first 8 axes, and it was verified only on DirectInput8 games.
- [Dirt Rally/Crew 2 disable some features unless a wheel is detected](https://code-monet.github.io/sim-gamer-kit/game_guides/issues/); that page recommends vJoy-as-wheel.
- Phone apps claim [900 degrees (SimWheel)](https://apkcombo.com/sim-wheel-controller-for-pc/com.ik.simwheel/) and [up to 2000 degrees (Virtual Steering Controller)](https://apps.apple.com/app/id6757863011).

### Inferences
- Rank titles for launch: Tier 1 (vJoy wheel) ETS2, ATS, AC, BeamNG, Forza Motorsport. Tier 2 (test first) ACC, EVO, LMU, AMS2, iRacing, rF2, EA WRC, DR2. Tier 3 (XInput default) F1, FH5, FH6, GTA V.
- Ship per-game setup profiles (rotation, deadzone, recommended filter) rather than a single global mapping.

### Gaps
- No primary source for current F1 25 vJoy behaviour, and no F1 action-map schema was read.
- PCGamingWiki pages were not retrieved. No FH6 or rF2 vJoy test report found.
- Rotation-setting behaviour per game is largely undocumented in these sources. Needs testing.

## 2. Emulate an Xbox pad, a DirectInput wheel, or spoof a known wheel's VID/PID?

### Takeaway
Support both output modes, per game: DirectInput wheel (vJoy) by default for sims, XInput for the list-gated and pad-only titles. Do not spoof VID/PID by default. I found no evidence that spoofing is needed or that any named game whitelists by VID/PID, though F1's patch-added wheel lists point to a list of some kind.

### Cited Findings
- Pad treatment is real and game-specific:
  - AndroidSteering warns that [most modern games apply stabilization to steering input, causing lag or wrong speed](https://www.github.com/teamclouday/AndroidSteering).
  - ACC has a [steering filter and speed sensitivity; one shared pad setup uses filter 40, speed steering 95, gamma 3.0](https://steamcommunity.com/app/805550/discussions/0/3050611812305884123) (thread attribution approximate).
  - BeamNG's [Key (smooth) filter cuts steering speed and authority to 70% while driving; Wheel (direct) bypasses it](https://awesome.ecosyste.ms/projects/github.com%2Fadam10603%2FBeamNG-Advanced-Steering).
  - x360ce-style emulation [feels more like a standard controller, with no clutch, shifter or FFB](https://steamcommunity.com/app/271590/discussions/0/611703999964693971).
- Double-device problem: [a virtual pad alongside a real one causes double input; HidHide is the usual fix; games may cache device lists](https://github.com/jskrzypek/stadia-vigem) ([troubleshooting source](https://gpad.pages.dev/guides/ds4windows-pc-mapping-guide)). A phone has no physical pad, so this mainly affects users with another controller or wheel plugged in.
- Spoofing data:
  - Nefarius documents [VID 0x7331 as a fake unused ID used to avoid spoofing real hardware, with PID 0x000A as vJoy-compatible emulation](https://docs.nefarius.at/research/Virtual-VID-PID-used-by-NSS/).
  - [G29 is VID 0x046D, PID 0xC24F or 0xC260, and SDL dropped it from its mapping list because Steam then creates a virtual Xbox pad that breaks racing games](https://skia.googlesource.com/third_party/sdl/+/44703d54ac44d2b0d822512c153229b214d12180%5E%21/).
- Forza EmuWheel is the community precedent for wheel emulation. The [guide found describes no VID/PID spoofing](https://steamcommunity.com/app/1293830/discussions/0/3049485912455199727).
- Android Steering added an [Xbox emulator mode in v2.3.0](https://www.github.com/teamclouday/AndroidSteering), so dual-mode exists among competitors.

### Inferences
- Spoofing risks (my reasoning, not sourced):
  - A game seeing a "G29" may call vendor SDKs or send device-specific FFB that the virtual device cannot honour.
  - Vendor software may try to claim the device.
  - Anti-cheat may treat a false hardware identity as evasion.
  - Using another company's USB vendor ID in a commercial product adds trademark and USB-IF licensing exposure.
- If tested at all, keep spoofing as a private spike, not a shipped feature. Prefer the F1 action-map route for unlisted devices if the files can legally be shipped as user-installed config (needs review; those files are EA's format).
- Compensate for pad filters on the phone side only for tuning (curves), and avoid anything resembling a driving aid (see section 4).

### Gaps
- No source confirms VID/PID whitelisting in any listed game, nor the mechanism behind F1 action maps or EmuWheel's registry file. A one-day test with a HID sniffer (or API monitor) on F1 25 and FH5 would settle this.
- vJoy's default VID/PID (from memory 0x1234/0xBEAD) is unverified here.
- No measurement exists of pad filter latency per game.

## 3. Can game telemetry drive phone vibration, on-screen dash and RPM LEDs?

### Takeaway
Yes for most listed games via official or community feeds, and it is a strong differentiator. The PC receiver should read telemetry and relay it to the phone. Phone vibration can only approximate FFB (road and event cues), not torque.

### Cited Findings

| Game | Feed | Details |
|---|---|---|
| F1 25 / 2026 | UDP | [EA publishes the spec; new installs default to the 2026 format; existing saves stay on F1 25 format](https://forums.ea.com/blog/f1-games-game-info-hub-en/ea-sports%E2%84%A2-f1%C2%AE25-2026-season-pack-udp-specification/12187347). [Port 20777; format 2024 recommended by a third party for compatibility](https://www.f1laps.com/faqs/f1-25-udp-telemetry-settings/) (a conflict to resolve: parse by header format). [29-byte header](https://docs.rs/f1-game-library-models-25/latest/f1_game_library_models_25/). [Packet 0 Motion, 6 CarTelemetry](https://github.com/Addzyyy/open-f1-parser) (third party). |
| ETS2/ATS | SDK plugin | [RenCloud plugin: MIT, memory-mapped `Local\SCSTelemetry`; build from source; its listed compatibility ends at 1.45-1.46 test builds](https://github.com/RenCloud/scs-sdk-plugin). Newer game versions are unverified. |
| AC / ACC | Shared memory | [Community library](https://github-redirect.dependabot.com/mdjarv/assettocorsasharedmemory); [`acpmf_physics` mapping](https://spacefreak18.github.io/simapi/simd_usage). No official Kunos spec found. |
| AC EVO | Shared memory | [Third-party claim of native buffers up to 1000 Hz](https://github.com/Barden-dev/VirtualWheel-Hub/releases). Unconfirmed by Kunos. |
| iRacing | Memory-mapped | [`Local\IRSDKMemMapFileName`, usually 60 Hz, variable table read at runtime](https://docs.rs/crate/iracing/latest). |
| Forza Motorsport | UDP Data Out | [Sled and Dash formats; Sled lacks gear and pedals; 60 packets/s; localhost supported](https://support.forza.net/hc/en-us/articles/21742934024211-Forza-Motorsport-Data-Out-Documentation). |
| FH6 | UDP Data Out | [Single 324-byte packet; avoid ports 5200-5300](https://support.forza.net/hc/en-us/articles/51744149102611-Forza-Horizon-6-Data-Out-Documentation). |
| BeamNG | UDP | [Official OutGauge (speed, rpm, pedals, lights) and MotionSim ("BNG1" marker, motion fields); no default port or rate stated](https://docs.beamng.com/modding/protocols/). Third parties use [4444](https://github.com/baddo-baddo/OpenShaker/wiki/BeamNG-Drive) and others. |
| LMU | Built-in shared memory | [Needs "Enable Plugins" plus restart; author claims it works online under EAC](https://github.com/Swizzjack/lmu-pitwall). [TinyPedal: no plugin needed](https://awesome.ecosyste.ms/projects/github.com%2FTinyPedal%2FTinyPedal). |
| rF2 | Plugin | [TheIronWolf shared memory plugin still in use](https://github.com/NikMusy/le-mans-ultimate-telemetry). |
| AMS2 | Shared memory | [Enable Project CARS 2 shared memory in options](https://github.com/Barden-dev/VirtualWheel-Hub/wiki/Game-Compatibility-and-Plugins). |
| DR2, EA WRC, FH4/5 | Unconfirmed | [vWheel Hub offers game detection only](https://github.com/Barden-dev/VirtualWheel-Hub/wiki/Game-Compatibility-and-Plugins). That reflects its product, not necessarily the games. |

- FFB capture is a separate route: [irFFB uses vJoy as a virtual FFB device to read iRacing's DirectInput FFB output](https://github.com/nlp80/irFFB/wiki/Configuration). [MouseToVJoy reads FFB on AC](https://github.com/R1PeR/MouseToVJoy). On vJoy, [Win11 FFB depends on a signed driver](https://github.com/BrunnerInnovation/vJoy).
- Competitor precedent: Virtual Steering Controller markets [real-time haptic feedback for supported games](https://apps.apple.com/app/id6757863011). vWheel Hub [gates telemetry helpers behind Pro](https://github.com/Barden-dev/vWheel-Hub).
- SimHub: licence is [pay-what-you-want](https://www.simhubdash.com/get-a-license/). It has a [Custom Serial Device plugin; can use Bluetooth serial ports like a COM port](https://dev.to/nmwilk/simhub-without-the-wires-18ij). Users map [buttons to vJoy through its control mapper](https://simhubdash.com/community-2/simhub-support/control-mapper-unable-to-use-the-same-target-vjoy-device-in-simhub-and-stream-deck-iraceit).
- Forza has [no ABS signal in Data Out](https://github.com/baddo-baddo/OpenShaker/wiki/Forza-Motorsport) (third party).

### Inferences
- Architecture: PC receiver owns all game adapters and sends one normalised stream (rpm, max rpm, gear, speed, G forces, slip, ABS/TC where available) to the phone. Android and iOS then share one protocol.
- Suggested order: F1 UDP, Forza UDP, BeamNG, then shared-memory titles, then the SCS plugin (which requires placing a DLL in the game folder, so it needs an explicit consent step).
- Offer a SimHub integration as an optional path: a custom serial or network device could reuse its game coverage instead of writing adapters for all games. The feasibility of a network path to a phone is unverified.
- Haptics design: phone vibration is coarse and has its own lag, so favour discrete events (kerbs, ABS, gear shift, rev limit) over continuous rumble.

### Gaps
- EA's spec text was not read (403), so packet sizes, send rates and the rev-light fields are unconfirmed. Get the PDF from the EA post.
- SimHub's plugin SDK and licence terms for third-party integration were not found.
- FH5 Data Out details, its localhost behaviour under the Store build, and the DR2/EA WRC UDP feeds were not verified.
- Phone haptic latency figures were not researched here.

## 4. Anti-cheat and competition rules relating to virtual devices

### Takeaway
I found no documented ban for vJoy or a virtual pad in any listed game, and no statement that they are permitted. EA Javelin is the highest-risk environment. Treat online and competitive use as "at the user's own risk, check the rules", and never add input automation.

### Cited Findings
- EA Javelin is [kernel-level, runs only with protected games, and covers F1 and EA SPORTS WRC, among others](https://www.ea.com/news/anticheat-progress-report).
- EA help warns that [Javelin may cause connection issues with controllers, wheels and VR headsets](https://help.ea.com/pt-br/articles/f1/f1-25/controllers-wheels-and-vr-support/).
- User-written thread: [AutoHotkey, DS4Windows, iCUE and Keys2XInput listed as falsely flagged](https://forums.ea.com/idea/f1-25-bug-reports-en/ea-javelin-anti-cheat-is-garbage-/12221320). It does not mention vJoy.
- [reWASD reported as detected by EA anticheat, F1 25 included; vendor said it was working on a fix](https://forum.rewasd.com/forum/rewasd/technical-questions-aa/246126-ea-security-violation?p=246145). This is the closest analogue found. Outcome unknown.
- A [petition alleges false-positive bans with little recourse](https://www.change.org/p/demand-transparency-and-fairness-in-ea-s-javelin-anti-cheat-enforcement). Advocacy source, not evidence of specific cases.
- iRacing: [terms bar methods that intercept, emulate or redirect its proprietary communication protocols](https://ir-core-sites.iracing.com/members/pdfs/20251106_iRacing_Commercial_Terms_dated_Nov_06_2025.pdf) (excerpt only). That wording concerns protocols, not HID devices. [Driving aids are disabled in series requiring D licence and above](https://support.iracing.com/support/solutions/articles/31000133499) (attribution from excerpt). Community tool irFFB [uses vJoy with iRacing](https://github.com/nlp80/irFFB/wiki/Configuration), and the [iRacing Sporting Code](https://www.iracing.com/?p=258355) governs sanctioned events.
- LMU: a third-party tool [reports telemetry reads work under EAC](https://github.com/Swizzjack/lmu-pitwall). There is no official Studio 397 statement.

### Inferences
- Product rules to reduce risk: no process injection, no game memory writes, read only documented feeds, no macros, no auto-countersteer, no traction-style input shaping tied to telemetry. A steering curve is ordinary configuration. A telemetry-driven input modifier could reasonably be read as a driving aid.
- Warn users against VID/PID spoofing online. Include an in-app note that league and esports rules may require approved hardware.
- A phone wheel has a fairness angle in leagues: gyro plus filtering could be seen as an assist, so being transparent about processing helps.

### Gaps
- Not found: explicit positions on vJoy or ViGEmBus from EA, Kunos (ACC/AC EVO), iRacing, Studio 397 or Reiza.
- Unverified from memory: iRacing's move to EOS anti-cheat. I could not confirm ACC's or AMS2's anti-cheat status.
- Esports rulebooks (F1 Esports, [Ferrari Esports Series](https://esportsseries.ferrari.com/rules)) were not read.

## 5. Distribution: stores, signing and driver redistribution

### Takeaway
The total direct cost is modest: about USD 344-446 in the first year (Play USD 25, Apple USD 99, OV USD 220-322), plus trademark fees. Brazil is excluded from Azure Artifact Signing. The main blockers are the driver install flow and SmartScreen reputation, not fees. Use vJoy and ViGEmBus rather than writing a kernel driver.

### Cited Findings

**Android**
- Verification is [live since September 30, 2026 for users in Brazil, Indonesia, Singapore and Thailand, covering installs from Play and six OEM stores; global rollout 2027](https://developer.android.com/developer-verification/guides).
- [Direct sideloads are not yet enforced in that phase, per a FAQ entry updated July 15, 2026](https://developer.android.com/developer-verification/guides/faq?hl=en). Some secondary sources overstate this.
- Fees: [USD 25 for an Android Developer Console Full Distribution account, described as similar to Play's USD 25 fee; Limited Distribution is free, up to 20 devices, no government ID](https://developer.android.com/developer-verification/guides/faq?hl=en).
- Play developers are [registered automatically and use Play Console](https://developer.android.com/developer-verification/guides).
- [Unregistered apps need the advanced flow: developer mode, one-day wait, re-authentication; ADB is unaffected](https://developer.android.com/developer-verification/guides/faq?hl=en).
- Losing the signing key [blocks package registration](https://developer.android.com/developer-verification/guides/faq?hl=en).
- Target API: [from August 31, 2026, new apps and updates must target API 36; existing apps need at least API 35 to stay available to new users](https://developer.android.com/google/play/requirements/target-sdk). A [November 1 extension](https://www.appsonair.com/blogs/android-16-api-level-36-target-sdk-requirement-what-you-need-to-do-before-august-31) is claimed by one blog and unverified.
- Foreground services: [`connectedDevice` needs `FOREGROUND_SERVICE_CONNECTED_DEVICE` plus a prerequisite such as `CHANGE_NETWORK_STATE`/`CHANGE_WIFI_STATE` or a Bluetooth runtime permission; `specialUse` is reviewed in Play Console; types must be declared in App content](https://developer.android.com/develop/background-work/services/fgs/service-types).

**iOS**
- [USD 99 per year; waivers exclude individuals and paid apps/IAP](https://developer.apple.com/br/support/fee-waiver/).
- [TestFlight: 10,000 external testers, 100 internal, builds valid 90 days, first external build reviewed](https://developer.apple.com/help/app-store-connect/test-a-beta-version/testflight-overview/).
- [Free personal team: 3 devices, 7-day profiles](https://developer.apple.com/support/compare-memberships/).

**Windows signing** ([Microsoft Learn, August 2026](https://learn.microsoft.com/en-us/windows/apps/package-and-deploy/code-signing-options))

| Option | Cost | Availability | SmartScreen |
|---|---|---|---|
| Store MSIX | Free, free account, Microsoft re-signs | Worldwide | No warnings |
| Store MSI/EXE | Own certificate | Worldwide | No prompts during Store install |
| Azure Artifact Signing | ~USD 9.99/month | Organisations: US, Canada, EU, UK. Individuals: US, Canada. **Brazil excluded** | Builds over time |
| OV | USD 150-300/year | Worldwide | Builds over time |
| EV | USD 400+/year | Worldwide | Same as OV since 2024 |
| SignPath Foundation | Free for qualifying open source | Check terms | OV-level |

- Reseller prices: [Sectigo OV ~USD 220-322; DigiCert OV ~USD 380-439; Sectigo EV USD 290-500; DigiCert EV USD 519-685](https://sslinsights.com/digicert-vs-sectigo-code-signing/) ([also here](https://www.whtop.com/plans/gogetssl.com/153115)). Indicative only.
- [Validity is capped at 460 days from March 1, 2026; many CAs will not issue to individuals without business documents](https://codenote.net/en/posts/ov-ev-code-signing-certificate-pricing-japan-2026/) (secondary). OV keys [must be on HSM or token](https://learn.microsoft.com/en-us/windows/apps/package-and-deploy/code-signing-options).
- Reputation: [can take several weeks and hundreds of clean installs; consistent identity helps; no manual consumer submission; Smart App Control may block unsigned files](https://learn.microsoft.com/en-us/windows/apps/package-and-deploy/smartscreen-reputation).
- MSIX: [cannot install kernel drivers, must split them into a separate installer](https://www.powershellgallery.com/packages/msix/0.71.0/Content/MSIX.Limitations.ps1) (third party). A [Store Driver Catalog workaround](https://techcommunity.microsoft.com/discussions/msix-discussions/packaging-usb-drivers-as-msix/3822133) is described by a community post.

**Drivers**
- vJoy: [MIT licence, copyright Shaul Eizikovich](https://raw.githubusercontent.com/BrunnerInnovation/vJoy/master/LICENSE.txt). The [Brunner fork supports Win10/11, has limited maintenance, an EV certificate is required to build the public driver, and re-signing is only a "might"](https://github.com/BrunnerInnovation/vJoy). A [signed v2.2.2.0 is reportedly available](https://github.com/njz3/vJoy/issues/12) (verify the asset). Version reliability varies: [BeamNG author says newer versions failed](https://forum.beammp.com/t/sim-racing-full-mouse-control-and-vehicle-dynamics/1336720).
- ViGEmBus: [BSD-3-Clause](https://github.com/nefarius/ViGEmBus/blob/master/README.md), [retired with no further updates; old updaters contact a domain the author no longer controls](https://docs.nefarius.at/projects/ViGEm/End-of-Life/). [v1.22.0 last release, November 2023](https://gitpedia.pages.dev/wiki/nefarius/ViGEmBus).
- VirtualPad: [commercial framework for Nefarius business partners](https://docs.nefarius.at/projects/VirtualPad/). A [DS4Windows discussion comment](https://github.com/Ryochan7/DS4Windows/discussions/3124) calls it proprietary, with installer redistribution limits (excerpt only; the page returned 404 on fetch).
- Own driver: [attestation signing needs an EV certificate](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/driver-signing-offerings). A [moderator reply says Azure Trusted Signing cannot replace it for registration](https://learn.microsoft.com/en-us/answers/questions/5866910/hardware-program-verification-using-azures-trusted). [Attestation offers no HLK compatibility assurance and no Windows Update retail publishing](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/driver-signing-offerings).

### Inferences
- Recommended path for a Brazilian solo developer:
  - Publish on Play with verification from day one.
  - Keep the APK signing key in a backup.
  - Use `connectedDevice` rather than `specialUse` to avoid review friction.
  - Direct signed installer (OV via cloud HSM, or SignPath if open source) that chain-installs driver setups with consent.
  - Add a Store listing later, because Store MSIX cannot itself carry the driver.
- ViGEmBus being archived is both a stability and a supply-chain risk. Do not bundle its updater. Pin the installer hash, and plan a fallback if Windows ever blocks it.
- Long term, a VirtualPad partnership is the commercial route for XInput if ViGEmBus breaks. Pricing is unknown.

### Gaps
- Unverified: whether a Brazilian individual can get OV, IV or EV from each CA (CPF vs CNPJ/MEI documents); Brazilian BRL Apple pricing; whether Play's USD 25 is still one-time; HidHide licence (LICENSE URL 404; MIT from memory); redistribution rights for Nefarius-signed ViGEmBus binaries; Store policy on apps requiring external drivers; Artifact Signing roadmap for Brazil.

## 6. Legal: names, game trademarks, LGPD, licences and clean-room

### Takeaway
"JD SimWheel" could not be found in web search, so its trademark status is unknown. Check INPI, WIPO and app stores directly, and pick a distinct name regardless. Avoid decompiling the original app, because Brazilian law is not clearly protective of reverse engineering.

### Cited Findings
- Search for ["JD SimWheel"](https://apkcombo.com/sim-wheel-controller-for-pc/com.ik.simwheel/) returned nothing exact. The closest is SimWheel Connect (`com.ik.simwheel`), which uses [vJoy and a receiver from simwheel.netlify.app](https://simwheel.netlify.app/installation). This is not confirmed to be the same product.
- Trademark precedent in this niche: [ViGEm had to retire its name after a conflict with ViGEM GmbH, found in May 2023](https://docs.nefarius.at/projects/ViGEm/End-of-Life/).
- INPI: [new fee table in force since December 20, 2025 (Portaria GM/MDIC 110/2025); discounts for MEI, ME, EPP and ESI, plus low-income CadÚnico individuals and persons with disabilities](https://www.gov.br/inpi/pt-br/servicos/custos-e-pagamento/NovaTabeladeRetribuiesINPI_MARCAS_Final_20_dez_25.pdf). The [old 2019 values (R$ 355/142 application per class) are not current](https://www.contabeis.com.br/legislacao/5052387/portaria-me-516-2019/). The [reported average increase is 24.1%](https://www.gov.br/inpi/pt-br/inpi-data/precificacao-dos-servicos/PolticadePreos.pdf) (excerpt).
- Software law: [Lei 9.609/98 protection does not depend on registration](https://www.wipo.int/wipolex/en/legislation/details/513). Commentators say [Brazilian law is silent on software reverse engineering](https://www.sedep.com.br/artigos/limitacoes-ao-direito-do-autor-copia-privada-e-engenharia-reversa/), and a [UFRJ dissertation concludes it cannot be said to be always lawful](https://www.ie.ufrj.br/images/IE/PPED/Dissertacao/2015/Luca%20Schirru.pdf). An [express interoperability exception was only a reform proposal](https://gedai.ufpr.br/wp-content/uploads/2017/11/data_xi_codaip_heloisa-2.pdf) in that source.
- LGPD: [IP treated as personal data](https://www.pontoisp.com.br/wp-content/uploads/2020/08/LGPD-Provedores-cartilha.pdf) (secondary). [Legitimate interest needs a documented balancing test per ANPD, according to a law firm](https://www.bmalaw.com.br/conteudo/direito-digital-protecao-de-dados-e-cybersecurity/artigos-e-noticias/o-legitimo-interesse-na-visao-da-anpd/pdf). [Art. 37 requires records of processing](https://www.dataprivacybr.org/wp-content/uploads/2021/10/O-legitimo-interesse-na-LGPD.pdf).
- Competitor signal: Virtual Steering Controller's [privacy label reports identifiers and usage data used for tracking](https://apps.apple.com/app/id6757863011), the ad-model cost.

### Inferences
- Clean-room practice:
  - Build only from public docs (vJoy SDK, Android sensors, game specs).
  - Keep notes on the origin of the design.
  - Do not decompile the APK or copy its assets, protocol constants or UI.
  - Observing publicly visible features for competitive analysis is lower risk than disassembly.
- Game marks: text-only compatibility statements ("works with...") with a non-affiliation disclaimer, no logos or screenshots of F1/Forza in store art. F1 marks are heavily policed. General legal reasoning, not sourced here; ask counsel.
- LGPD: keep game telemetry on the local network, make analytics opt-in or minimal, anonymise crash reports, publish a privacy policy, and name a contact. Local telemetry that never leaves the user's devices likely does not reach the developer as a controller, which simplifies compliance.
- Licence hygiene: include the MIT and BSD notices. Avoid "ViGEm" in product naming beyond factual dependency attribution, and link the scs plugin's MIT notice if it is bundled.

### Gaps
- INPI, USPTO and WIPO database searches for the name were not run (interactive systems). Current INPI BRL values could not be extracted from the PDF.
- Art. 6 of Lei 9.609 could not be read in full (fetch failed). Verify at planalto.gov.br.
- ANPD's small-agent regulation (from memory: Resolução CD/ANPD 2/2022) is unverified here.
- No source was read on nominative trademark use under Brazilian law, or on the app's business entity form (MEI vs ME).

## 7. Business model observations and effort estimates

### Takeaway
The niche is crowded with small, low-traction apps, so polish and game coverage are the differentiators. A freemium model (free steering, paid telemetry and dash) fits the market evidence. The phase estimates below are my judgement, because no project publishes actual effort data.

### Cited Findings

| Project | Model | Tech | Signal |
|---|---|---|---|
| [AndroidSteering](https://www.github.com/teamclouday/AndroidSteering) | MIT, free | vJoy, Wi-Fi/BT | 101 commits, 51 stars, tested AC, FH4 |
| [Engine](https://github.com/whitescent/Engine) | Open source, archived | vJoy | Abandoned risk |
| [Virtual Steering Controller](https://apps.apple.com/app/id6757863011) | Ads, "fuel" limit, USD 1 and USD 9.99 IAP | Own receiver | 4.1 from 8 ratings; reviews criticise fuel |
| [SimWheel Connect](https://apkcombo.com/sim-wheel-controller-for-pc/com.ik.simwheel/) | Details not retrieved | vJoy, Wi-Fi | Tested ETS2, Forza Horizon |
| [vWheel Hub](https://github.com/Barden-dev/vWheel-Hub) (mouse, not phone) | Free plus Boosty Pro subscription, all rights reserved | vJoy | 33 commits, v2.0.3; Pro gates telemetry |
| [Steering Wheel Emulator](https://mrsomebody02.itch.io/steering-wheel-emulator) | itch.io | vJoy | ETS2 focus |
| [SimHub](https://www.simhubdash.com/get-a-license/) | Pay-what-you-want | n/a | Community expects affordability |

### Inferences
- Pricing direction: one-time unlock near the USD 9.99 competitor price point, avoiding playtime limits and intrusive ads. Subscriptions may be poorly received given the SimHub norm (speculative). BRL pricing is an opportunity in Brazil.
- Open source trade-off: an open PC receiver earns trust for a driver-installing app and enables SignPath, while keeping the mobile premium features closed. Note SignPath's eligibility may not fit a mostly commercial product.

**Solo developer plan (author's estimates, part-time ranges; not sourced):**

| Phase | Scope | Estimate |
|---|---|---|
| 0. Spike | vJoy and ViGEm output, UDP link, latency test, test Tier 1 and F1/FH5 | 2-4 weeks |
| 1. Android MVP | Gyro steering, pedals, buttons, pairing, signed receiver, per-game guides | 6-10 weeks |
| 2. Beta | Play closed test, 5-8 games validated, installer with driver flow, reputation building | 4-8 weeks |
| 3. Telemetry | Normalised stream; F1, Forza, BeamNG first; haptics, dash, RPM LEDs | 6-10 weeks |
| 4. iOS | Port plus TestFlight | 6-12 weeks (faster with a cross-platform core) |
| 5. Extended | Shared-memory titles, SCS plugin, SimHub, freemium billing | 4-8 weeks |

- Total to Android-with-telemetry: roughly 4.5-7.5 months part-time (sum of phases 0 through 3: 18-32 weeks). Phases 4 and 5 add 10-20 weeks.

### Gaps
- No revenue or install data for competitors was found. RojX, Tilt Racer, HandyGamePad and WheelApp pricing could not be retrieved ([Tilt Racer article](https://www.droidgamers.com/news/tilt-racer-turns-your-phone-into-a-steering-wheel-for-pc-racing-games/) returned 403).
- No commit-time or effort data is available from comparable projects beyond commit counts.
