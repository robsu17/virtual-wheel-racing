# Virtual game controller / steering wheel on Windows 10/11 (status as of November 2026)

Evidence-quality note for the report writer: Microsoft Learn, Android Developers and project READMEs are primary sources. GitHub issues and forum threads are single-user reports. "Project claims" means the project's own documentation, with no independent verification found. EA forum pages returned HTTP 403 when fetched, so those items rest on search-result titles or snippets only. Nothing here was tested on real hardware.

## Comparison and recommendation: what should a solo developer use?

### Takeaway
No option is clean in November 2026. Ship behind a swappable backend interface, and do not write a kernel driver at this stage. Use ViGEmBus 1.22.0 (Xbox 360 emulation) as the broad-compatibility default. Run an immediate spike on HIDMaestro, the only candidate offering real wheel identity, PID force feedback and no EV certificate, and promote it to the wheel backend only if it passes on Secure Boot + Memory Integrity machines. Keep vJoy as an optional fallback and Android Bluetooth HID as a secondary no-driver mode.

### Cited Findings
Comparison (details and sources in the sections below):

| Path | Status Nov 2026 | Licence | PC install | Looks like a wheel? | Receives FFB? | Main risk |
|---|---|---|---|---|---|---|
| ViGEmBus 1.22.0 | Retired, archived, still works | BSD-3-Clause | Admin; kernel driver | No: X360 or DS4 only | Rumble only | Frozen; future signing policy |
| vJoy 2.2.2.0 (Brunner) | Minimal maintenance | MIT | Admin; kernel driver | Generic "vJoy Device" | Yes (PID, callbacks) | Reported install failures on Win 11 |
| vJoy 2.1.9.1 (Shafer) | Old, reported to work | MIT | Admin; kernel driver | Generic | Effect handling weaker (FFB fixes are in 2.2.x) | Old |
| HIDMaestro 2.0.1 | Active, released Oct 2026 | MIT | Admin; self-signed cert in machine Root store; no reboot claimed | Yes: custom or cloned VID/PID | Yes (HID PID 1.0) | Young; Secure Boot behaviour undocumented |
| VIIPER + usbip-win2 | Active | GPL-3.0 server, MIT clients | usbip-win2 signed kernel driver | No wheels listed | Not documented | No wheel support |
| WinUHid | Active, small | MIT | Installer present; test-signed per a third party | Raw descriptor possible | Not documented | Signing unclear |
| VirtualPad (Nefarius) | Commercial/proprietary | Proprietary | Unknown | Unknown | Unknown | Access and terms |
| Own KMDF + VHF driver | Do-it-yourself | Yours | Admin | Yes | Yes | EV cert, entity, ongoing cost |
| Own UMDF2 HID minidriver | Do-it-yourself | Yours | Admin | Yes | Yes | Signing path for distribution |
| Android BluetoothHidDevice | Android 9+ API | n/a | None; just pairing | Own descriptor | Callbacks exist; unproven | Foreground only; OEM variance; no iOS |

- HIDMaestro lists ViGEmBus and vJoy as needing a kernel driver and an EV certificate, and itself as needing neither — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro) (project claim).

### Inferences
- Recommended architecture: a `VirtualDeviceBackend` abstraction in the PC app (create, submit state, output/FFB event, destroy), so backends can be swapped without touching the phone protocol.
- Recommended order:
  1. **Week 1 spike:** test HIDMaestro on clean Windows 10 and 11 machines with Secure Boot and Memory Integrity on, including Windows 11 Home, and with F1, Assetto Corsa and BeamNG-class titles.
  2. **MVP default:** ViGEmBus 1.22.0 X360 target, because XInput is what most racing games accept without a wheel profile.
  3. **Wheel mode:** HIDMaestro if the spike passes; otherwise vJoy, offered as an advanced option with a clear warning.
  4. **Bluetooth mode:** later, Android only, marketed as "no install, no force feedback, keep the app open".
  5. **Own driver:** only with revenue and a legal entity, and only if third-party backends break.
- HIDMaestro's .NET 10 SDK favours a C#/.NET PC companion; a Rust or C++ companion would need interop or a small .NET sidecar.
- The biggest product risk is not technical: kernel anti-cheat titles, including the F1 games the product likely targets, may refuse any virtual device. Marketing should not promise F1 support until it is tested.
- Create the virtual device only while a session is active and remove it on exit. This reduces anti-cheat conflicts and "ghost controller" problems. It is design reasoning, not a documented guarantee.

### Gaps
- No independent benchmark compares latency across these backends.
- No hands-on evidence of any backend with current F1 builds was found.

## vJoy: status, forks, Windows 11, signing, capabilities, bindings

### Takeaway
vJoy is usable but fragile: the only widely signed build with proper FFB (2.2.2.0, December 2023) has reports of failing on Windows 11 as recently as April 2026, while the older 2.1.9.1 works on 25H2 but lacks the FFB fixes. It is MIT-licensed and has good bindings, but effectively has no active maintainer.

### Cited Findings
**Forks**
- shauleiz/vJoy (original): README says Windows 7 to Windows 10 1803 and points to the jshafer817 fork; MIT — [shauleiz/vJoy](https://github.com/shauleiz/vJoy).
- jshafer817/vJoy: MIT; building requires buying driver-signing rights; its 2020 note points to njz3 for a signed version and newer FFB — [jshafer817/vJoy](https://github.com/jshafer817/vJoy).
- njz3/vJoy: MIT; development fork; names 2.2.2.0 at BrunnerInnovation as the latest signed Windows 10/11 build — [njz3/vJoy](https://github.com/njz3/vJoy).
- BrunnerInnovation/vJoy: supports only Windows 10 and 11; maintainers sponsored the signing, do not intend to invest much, and will publish new signed binaries only for important contributions — [BrunnerInnovation/vJoy](https://github.com/BrunnerInnovation/vJoy).
- v2.2.2.0 is marked Latest and is EV-signed plus Microsoft attestation-signed — [release notes](https://github.com/BrunnerInnovation/vJoy/releases/tag/v2.2.2.0). It was announced 19 December 2023 — [shauleiz issue #57](https://www.github.com/shauleiz/vJoy/issues/57).

**Windows 11 compatibility (conflicting)**
- Failure with `WdfCollectionCreate` status `0xC000009A` reported on Windows 11 in December 2023. On 22 April 2026 a user said the 2.2.2.0 fork still fails, with the installer freezing. The issue remains open — [njz3 issue #17](https://github.com/njz3/vJoy/issues/17).
- 2.1.9.1 reported working on Windows 11 25H2 on 5 May 2026 — [shauleiz issue #57](https://www.github.com/shauleiz/vJoy/issues/57).
- Nimbus Adaptive Controller chose to bundle 2.1.9.1 rather than 2.2.x for this reason, and pins its hash and signer. It says 2.1.9.1 is signed by Microsoft Windows Hardware Compatibility Publisher — [Nimbus PR #5](https://github.com/owenpkent/Nimbus-Adaptive-Controller/pull/5).
- Contrary view: 2.2.2.0 is stated to work on Windows 10 and 11 — [release notes](https://github.com/BrunnerInnovation/vJoy/releases/tag/v2.2.2.0).

**Secure Boot / test signing / HVCI**
- Test-signed builds need `bcdedit -set TESTSIGNING ON` and UEFI Secure Boot disabled, and are for testing only — [njz3/vJoy](https://github.com/njz3/vJoy).
- With a test-signed vJoy, iRacing's anti-cheat blocked races while test mode was on — [shauleiz issue #57](https://www.github.com/shauleiz/vJoy/issues/57).
- Evidence on Memory Integrity is thin. Search summaries (pages not opened) said a forum user reported an HVCI-compliant attestation-signed build installing with it on, without naming the version, and that one unofficial site says no vJoy build publishes an HVCI guarantee — likely [Windows forum](https://forum-windows.com/d/1348-vjoy-windows-11) and [vjoy.pro (unofficial)](https://vjoy.pro/); confirm before quoting.

**Capabilities**
- Up to 16 devices; axis maximum value `0x7FFF`, so 15-bit (0–32767) — [SDK public.h](https://raw.githubusercontent.com/njz3/vJoy/master/SDK/inc/public.h).
- Eight axes: X, Y, Z, Rx, Ry, Rz, Slider, Dial/Slider2 — [vjoy-sys source](https://docs.rs/crate/vjoy-sys/latest/source/src/lib.rs).
- Identity problem: vJoy presents as its own device, not a known wheel. A user could not get F1 2020 to treat it as a G29 because the identifier did not match. A registry `OEMData` tweak for `VID_1234&PID_BEAD` worked for some titles and failed for others — [vJoy forum](https://vjoy.freeforums.net/thread/197/make-vjoy-game-steering-wheel). HIDMaestro's comparison also lists a fixed "vJoy Device" identity — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).

**FFB and bindings**
- Python: `pyvjoyffb` 0.4 (27 March 2025, MIT, Python 3.8+) requires vJoy 2.2.x for the effect block index, recommends the Brunner fork, and says the original project lacks proper FFB. It offers constant, periodic, ramp, condition, envelope and gain callback hooks — [pyvjoyffb](https://pypi.org/project/pyvjoyffb/).
- Rust: `vjoy-sys` 0.5.2, built against 2.2.2.0, Windows only, loads the DLL at runtime; exposes `FfbRegisterGenCB`, `FfbStart` and `IsDeviceFfb` — [vjoy-sys](https://docs.rs/crate/vjoy-sys/latest); [build script 0.4.0](https://docs.rs/crate/vjoy-sys/0.4.0/source/build.rs).
- C/C++: SDK headers `SDK/inc/vjoyinterface.h` and `public.h` are in the repo — [njz3/vJoy](https://github.com/njz3/vJoy).
- If FFB fixes are unnecessary, a maintainer says staying on 2.1.9 is acceptable — [shauleiz issue #57](https://www.github.com/shauleiz/vJoy/issues/57).

### Inferences
- A practical split: 2.2.x for FFB, 2.1.9.1 for install reliability. Neither alone gives both, on the available reports.
- The 15-bit range is more than enough for phone-gyro steering, since sensor noise will dominate.
- 2.2.2.0's attestation signature carries some long-term policy risk (see signing section).

### Gaps
- A search result mentioned 128 buttons and 4 hats; I could not confirm these in the header and they are widely repeated rather than verified here.
- C# wrapper: I recall the vJoy SDK shipping one, but did not verify it this session.
- No systematic HVCI test of any specific vJoy build was found.
- jshafer817's and njz3's last commit dates were not visible.

## ViGEmBus: retirement, successor, licensing, emulation limits, XInput vs DirectInput

### Takeaway
ViGEmBus is officially retired and frozen at 1.22.0, yet remains widely shipped and functional. It is BSD-3-Clause, so bundling is permissible, but it can only look like an Xbox 360 pad or DualShock 4, not a wheel. Its named successor, VirtualPad, appears commercial, and I could not read its licence directly.

### Cited Findings
- The README declares the project retired; the repo was archived read-only on 2 November 2023; licence BSD-3-Clause; 1.17+ targets Windows 10/11 on x86, amd64 and ARM64 — [nefarius/ViGEmBus](https://github.com/nefarius/ViGEmBus).
- The official statement says a trademark conflict with ViGEM GmbH (May 2023), and the maintainer's lack of time, led to retirement. There will be no further updates, existing installs are unaffected apart from updater domains, and it names no successor — [End of Life statement](https://docs.nefarius.at/projects/ViGEm/End-of-Life/).
- Last release 1.22.0 was reported as dated 2 November 2023 and still signed — [Coda guide (secondary)](https://coda-apps.com/guides/what-is-vigembus).
- A fan-style site says VirtualPad is in development — [vigembusdriver.com (not clearly official)](https://vigembusdriver.com/). A community member describes VirtualPad's licence as proprietary with limits on distributing the installer — [DS4Windows discussion, search snippet](https://github.com/Ryochan7/DS4Windows/discussions/3124). Note this URL returned 404 on direct fetch.
- The DSX "Virtual DualSense" DLC uses Nefarius technology under an exclusive partnership — [Steam](https://store.steampowered.com/app/2345650/DSX__Virtual_DualSense_Emulation?l=german); [DekuDeals](https://www.dekudeals.com/items/dsx-virtual-dualsense-emulation).
- Sunshine's maintainer said in 2025 that no open-source alternative existed, and considered a fork. He noted that distributing a signed kernel driver needs a legal business entity — [Sunshine #3527](https://github.com/LizardByte/Sunshine/issues/3527). Tracking continues at [roadmap #10](https://github.com/LizardByte/roadmap/issues/10).
- A 2026 project bundles ViGEmBus 1.22.0 and installs it silently, checking for an attached device because stale service entries can survive uninstall until reboot — [Nimbus PR #5](https://github.com/owenpkent/Nimbus-Adaptive-Controller/pull/5).
- Report formats: X360 uses signed 16-bit sticks and 8-bit triggers; DS4 uses 8-bit sticks and triggers — [ViGEmClient Common.h](https://raw.githubusercontent.com/nefarius/ViGEmClient/master/include/ViGEm/Common.h). XInput documents thumbsticks as -32768..32767 and triggers as 0..255 — [XINPUT_GAMEPAD](https://learn.microsoft.com/en-us/windows/win32/api/xinput/ns-xinput-xinput_gamepad).
- Newer alternatives: VIIPER runs in userspace over USB/IP, emulates Xbox 360, DS4, DualSense and Switch 2 Pro at up to 1000 Hz, depends on usbip-win2's signed kernel driver, is GPL-3.0 with MIT client libraries, and lists no wheels or FFB — [VIIPER](https://github.com/Alia5/VIIPER).
- Forum posts surfaced by a search say gamepad steering in some games behaves as a rate-of-steer control rather than direct angle, and that a game's steering model can be tied to whether input is XInput or DirectInput. Anecdotal, and the search output did not tie the claim firmly to one URL; candidates are [Focus forum](https://forums.focus-entmt.com/post/73159) and [BeamMP forum](https://forum.beammp.com/t/sim-racing-full-mouse-control-and-vehicle-dynamics/1336720/3). Another older guide warns a game may prioritise an XInput device over a vJoy one — [F1-22 mouse steering](https://github.com/deep-patel21/F1-22-Mouse-Steering-Clone).

### Inferences
- XInput pad vs DirectInput wheel trade-off:
  - **XInput:** almost every game accepts it with no setup, but games apply gamepad steering filters and speed-sensitive assists. The wheel angle gets 16-bit precision that is partly wasted, and triggers are only 8-bit. Output is limited to rumble, so no real FFB.
  - **DirectInput/HID wheel:** direct steering and FFB potential, but games must recognise the device, and some only offer profiles for known wheels.
- BSD-3-Clause allows redistribution with notices, so bundling the signed installer appears fine. Do not use the retired ViGEm name for branding, given the trademark settlement. Have counsel check if commercial.
- A VirtualPad licence is worth one email to Nefarius if budget exists, but it should not be a dependency in the plan.

### Gaps
- I could not retrieve VirtualPad's official licence or price; the official EOL page does not mention it.
- ViGEm rumble: I recall client notification callbacks for X360 motor values, but did not re-verify them this session.
- A search result titled "CRITICAL Security Update for ViGEmBus" ([BetterJoy #1127](https://github.com/Davidobot/BetterJoy/issues/1127)) was not read. Check it before bundling, and confirm 1.22.0 is the patched version.
- HidHide's current maintenance status could not be established; only an undated release note surfaced.

## Writing your own driver: VHF, UMDF, KMDF, signing, cost, and Brazil

### Takeaway
A distributable kernel driver means an EV certificate ($349–$669/yr), Partner Center hardware registration built around an organisation, and attestation, which Microsoft's 2026 docs now file under testing scenarios. WHCP/HLK is the recommended route but heavier. It is not viable as a first step for a solo developer. Using a UMDF2 HID minidriver avoids kernel risk, but its signing story for wide distribution is not settled by the sources.

### Cited Findings
**Technical**
- VHF needs a KMDF or WDM HID source driver linking `vhfkm.lib`, with `Vhf.sys` as lower filter via the INF; the current release supports kernel-mode source drivers only. Microsoft's own example is a phone acting as a game controller, sending accelerometer data wirelessly — [Microsoft Learn: VHF](https://learn.microsoft.com/en-us/windows-hardware/drivers/hid/virtual-hid-framework--vhf-) (dated 22 April 2025).
- VHF APIs: `VhfCreate`, `VhfStart`, `VhfReadReportSubmit`, `VhfDelete`. If registered, callbacks handle GetFeature, SetFeature, WriteReport and GetInputReport; otherwise those return `STATUS_NOT_SUPPORTED` — [same source](https://learn.microsoft.com/en-us/windows-hardware/drivers/hid/virtual-hid-framework--vhf-).
- `vhidmini2` compiles as KMDF or UMDF 2.x, and is the recommended starting point; both shims (`MsHidUmdf.sys`, `MsHidKmdf.sys`) ship in the OS — [Microsoft Learn (Russian locale)](https://learn.microsoft.com/ru-ru/windows-hardware/drivers/wdf/creating-umdf-hid-minidrivers).
- UMDF drivers run in `Wudfhost.exe`, usually as LocalService, isolated from applications — [UMDF host process](https://learn.microsoft.com/en-us/windows-hardware/drivers/wdf/umdf-driver-host-process).
- Conflict: HIDMaestro's table describes WinUHid as "UMDF2 on VHF", while Microsoft's page says VHF source drivers are kernel-only — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro); [Microsoft Learn: VHF](https://learn.microsoft.com/en-us/windows-hardware/drivers/hid/virtual-hid-framework--vhf-).

**Signing today**
- HLK-tested dashboard signing is Microsoft's recommended method. Attestation is now headed as being for testing scenarios and described as for testing purposes only. It does not need HLK, works on Windows 10 Desktop and later, covers kernel and user mode, requires an EV certificate for submission, and yields a driver that is trusted but not Windows Certified — [Driver Signing Options](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/driver-signing-offerings) (dated 23 March 2026).
- Attestation-signed drivers cannot reach retail audiences through Windows Update — [Attestation signing](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/code-signing-attestation). The page's table still shows attestation as accepted on Windows 10 — [Driver Signing Options](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/driver-signing-offerings).
- Kernel trust change: from the April 2026 update, Windows 11 24H2, 25H2, 26H1 and Server 2025 remove trust for the cross-signed program, allowing WHCP-passed drivers and an allow list by default. It starts in evaluation mode (250 hours since the June 2026 update, plus 3 restarts on Windows 11). The post does not mention attestation or user-mode drivers — [Microsoft Tech Community](https://techcommunity.microsoft.com/blog/windows-itpro-blog/advancing-windows-driver-security-removing-trust-for-the-cross-signed-driver-pro/4504818). The community noted the same silence on attestation — [OSR thread](https://community.osr.com/t/advancing-windows-driver-security-the-windows-driver-policy/60086).
- Nimbus's maintainers treat a signed build as good for now and budget for WHCP as fallback, without claiming attestation will stop loading — [Nimbus PR #5](https://github.com/owenpkent/Nimbus-Adaptive-Controller/pull/5).
- Preproduction signing supports testing with Secure Boot and HVCI on, but only on explicitly provisioned machines — [Driver Signing Options](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/driver-signing-offerings).
- Test signing: drivers install only with test signing on, in which configuration Secure Boot and code integrity are off — [preproduction signing post](https://techcommunity.microsoft.com/blog/hardwaredevcenter/introducing-preproduction-signing-level/3483336).

**Costs and eligibility**
- Registration needs an EV certificate, an Entra ID global administrator (a free directory can be created), a legal contact, company details or manual entry without D-U-N-S, and a business questionnaire. The EV certificate is needed to register, not to sign each driver — [Register](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/hardware-program-register) (dated 11 August 2026). The page lists no program fee.
- Accepted EV CAs: Certum, DigiCert, GlobalSign, IdenTrust, Sectigo, SSL.com — [Code signing requirements](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/code-signing-reqs).
- Prices: published one-year EV prices of $349–$669 (18 October 2026) — [My-SSL](https://my-ssl.com/learn/code-signing-certificate-price). Resellers advertise from about $249–$409/yr, often undated — [SSL Dragon](https://www.ssldragon.com/ssl-certificates/code-signing/extended-validation/); [SignMyCode](https://signmycode.com/ev-code-signing).
- SSL.com sells EV to individuals and sole proprietors without a registered business: $359 for one year, down to about $201/yr on a five-year term. YubiKey is $379 and may not satisfy EV kernel-signing needs. Cloud HSM attestation is $75–$1,275. The page does not list eligible countries — [SSL.com sole proprietor EV](https://www.ssl.com/products/software-integrity/code-signing/ev-sole-proprietor/).
- A March 2026 thread reports an individual Partner Center enrolment failing on a hidden required field (unconfirmed) — [Microsoft Q&A](https://learn.microsoft.com/en-us/answers/questions/5842350/individual-developer-enrollment-fails-alternativeb). Another thread reports being unable to complete Hardware program registration — [Microsoft Q&A](https://learn.microsoft.com/en-my/answers/questions/5898771/unable-to-complete-hardware-developer-program-regi).

### Inferences
- Rough first-year cash cost for attestation: about $350–$700 for the certificate, plus possibly token or HSM fees; realistically one to several weeks for validation and registration, plus driver development and testing. The time estimate is my judgement, not sourced.
- Brazil: nothing found excludes a Brazilian developer, and the listed CAs sell internationally, but nothing confirms it either. A Brazilian MEI or small company with CNPJ would probably fit the organisation-oriented registration better than a natural person. Verify with SSL.com or Certum before paying.
- Attestation is now a business risk even if it loads today: Microsoft labels it as testing, and the trust post is silent on it.
- A self-written driver would still need solving FFB (PID descriptor) and testing across games, so it saves little relative to adopting HIDMaestro or vJoy.

### Gaps
- Whether attestation-signed kernel drivers are affected by the 2026 policy: no Microsoft source answers this.
- I recall attestation-signed drivers also showing the Microsoft Windows Hardware Compatibility Publisher name, which would make Nimbus's signer check inconclusive about WHCP status. Not verified this session.
- HLK/WHCP cost and effort for a virtual HID device with no physical hardware, including whether it can pass at all: no source found.
- Current rules for a self-signed or OV-signed UMDF driver with Secure Boot on: no official English doc retrieved.
- No confirmed account of a Brazilian individual obtaining Hardware program access.

## HIDMaestro and other user-mode approaches (new since most older guides)

### Takeaway
HIDMaestro is the most interesting finding: an MIT-licensed UMDF2 SDK claiming wheel descriptors, HID PID force feedback and no EV certificate. However, it is young, its claims are self-reported, its docs say nothing about Secure Boot or Memory Integrity, and it installs a self-signed certificate into the machine Root store. Test it before depending on it.

### Cited Findings
- Claims: UMDF2 in user mode, self-signed certificate, no test-signing boot mode, no purchased certificate, no reboot; visible to DirectInput, XInput, SDL3, browser Gamepad and WGI/GameInput; HID PID 1.0 FFB; custom racing wheel profiles with arbitrary VID/PID, axis count and resolution; capture of a real device's descriptor; MIT — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).
- Five Thrustmaster wheels and OpenFFBoard are named among catalogue profiles; Logitech G29 was not found in the README — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).
- Tested by the author on Windows 11 IoT Enterprise LTSC 2024 (build 26200) and Windows 10 IoT Enterprise LTSC (19044), with the certificate in Root and TrustedPublisher — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).
- Requires elevation: non-elevated processes fail on first create and cleanup does nothing. Primary SDK is .NET 10, with NativeAOT supported; x64 and ARM64 — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).
- FFB writes are delivered to the consumer through an event; routing them onward is the app's job — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).
- It states virtual devices are detectable by kernel anti-cheat and that it neither hides this nor bypasses anti-cheat — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).
- v2.0.1 was published 21 October 2026, adding `HIDMaestroUninstall.exe`, which also removes the signing certificate and its private key; composite personas can install usbip-win2's kernel host controller — [v2.0.1 release](https://github.com/hifihedgehog/HIDMaestro/releases/tag/v2.0.1). Version 1.x programs can remove other programs' devices — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).
- Used by PadForge and described by its developer as a ViGEmBus/vJoy replacement — [LaunchBox forum](https://forums.launchbox-app.com/topic/93722-hidmaestro-and-padforge/).
- A text search of the README and INTERNALS found no mention of Secure Boot, HVCI, Memory Integrity or Smart App Control — [README](https://github.com/hifihedgehog/HIDMaestro); [INTERNALS](https://github.com/hifihedgehog/HIDMaestro/blob/master/docs/INTERNALS.md).
- WinUHid: MIT, © Cameron Gutman 2025, UMDF-tagged UHID-like framework; its README page as fetched did not state signing or FFB — [WinUHid](https://github.com/cgutman/WinUHid). HIDMaestro says it ships test-signed and needs an OV certificate on x64 — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro) (competitor's claim).

### Inferences
- Adding a root certificate on each user's PC is a real trust cost. Antivirus, enterprise policy and reviewers may object, and a compromised private key could be serious. Prefer a per-machine generated key with immediate disposal of signing capability if the SDK allows, and remove it fully on uninstall.
- Spoofing a real wheel's VID/PID could get better game recognition, but raises trademark and terms questions and could look like evasion to anti-cheat systems. Start with own or generic identity and an honest device name; use cloned identities only after legal consideration.

### Gaps
- Whether HIDMaestro loads on retail Windows 11 Home/Pro with Secure Boot, Memory Integrity and Smart App Control on: undocumented. This is the single most important test.
- No independent review or anti-cheat report for HIDMaestro was found.
- Whether an MIT consumer can freely redistribute it inside a paid product without certificate issues appears fine but was not legally reviewed.

## Other routes: GameInput, WGI Custom, GIP, Bluetooth HID, USB gadget, Linux, Steam Input, macOS

### Takeaway
GameInput and Windows.Gaming.Input are consumer APIs, not ways to create devices. Android's `BluetoothHidDevice` is a genuine no-PC-driver path, but it is Classic Bluetooth, foreground-only, OEM-dependent and absent on iOS. USB gadget needs root. Linux `uinput` is the easy path for Linux and Steam Deck.

### Cited Findings
**Microsoft APIs**
- GameInput is an API for games to read devices, supports force feedback and is available via NuGet back to Windows 10 19H1; doc updated 29 October 2026 — [GameInput overview](https://learn.microsoft.com/en-us/gaming/gdk/docs/features/common/input/overviews/input-overview). The overview describes no device-creation function.
- `Windows.Gaming.Input.Custom` targets equipment makers writing WinRT libraries and is not meant for direct app use; providers represent connected devices — [namespace docs](https://learn.microsoft.com/en-us/uwp/api/windows.gaming.input.custom). `GipGameControllerProvider` represents a physical controller on GIP.SYS — [provider docs](https://learn.microsoft.com/uwp/api/windows.gaming.input.custom.gipgamecontrollerprovider).

**Android Bluetooth HID**
- `BluetoothHidDevice` was added in API 28. Methods include `registerApp`, `sendReport`, `replyReport`, `reportError`; only one app can be registered at a time; the host service is disabled during registration; and **an app is automatically unregistered if not in the foreground**. Android 12+ needs `BLUETOOTH_CONNECT`. Subclass constants include gamepad and joystick — [Android docs](https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice).
- It is Classic BR/EDR rather than BLE HOGP — [TabletHID](https://github.com/realpoka5-coder/TabletHID).
- PocketPad (MIT) needs Android 9+ and an OEM stack implementing the HID Device profile. It is confirmed only on a Samsung Tizen TV, and Windows 10/11 is unverified. Windows treats the phone as a phone, so the connection must start from the app after pairing. XInput-only games will not see it. Hosts cache descriptors, requiring re-pairing after changes. No latency has been measured — [PocketPad](https://github.com/Dathaze20/PocketPad).
- A Pixel 11 Pro on Android 17 was reported showing a phone icon and failing reconnection in a firmware PR — [WMR_Bridge PR #89](https://github.com/nightcinder/WMR_Bridge/pull/89). It concerns a BLE HID gamepad talking to Android, so relevance is partial.
- Claims that Chinese OEMs disable HOGP by default come from low-quality guides and could not be verified — [Alibaba guide](https://electronics.alibaba.com/question/android-game-controller-guide-what-works-how-to-choose).

**iOS**
- Developers report CoreBluetooth rejecting 0x1812 publication with a not-allowed error, and no app-level HID peripheral support, going back years — [Apple forum](https://developer.apple.com/forums/thread/725238); [Apple forum](https://developer.apple.com/forums/thread/695844); [Nordic DevZone](https://devzone.nordicsemi.com/f/nordic-q-a/598/ios-7-not-seeing-service-for-uuid-1812-hid). A thread mentions AccessorySetupKit BLE HID in iOS 18.4, which looks aimed at setting up accessories rather than making the phone one — [Apple forum](https://developer.apple.com/forums/thread/852603).

**USB gadget**
- USB Gadget Tool requires root and ConfigFS kernel support, exposes `/dev/hidg` endpoints, and is keyboard/mouse oriented — [android-usb-gadget](https://github.com/tejado/android-usb-gadget). ADB may have to be disabled and results vary by kernel — [issue](https://www.github.com/tejado/android-usb-gadget/issues/12).

**Linux and macOS**
- `uinput` supports FFB: declare `ff_effects_max` and `UI_SET_FFBIT`, then service `UI_FF_UPLOAD` and `UI_FF_ERASE` in a userspace loop with `UI_BEGIN_FF_UPLOAD`/`UI_END_FF_UPLOAD` — [uinput.h](https://git.zx2c4.com/wireguard-linux/tree/include/uapi/linux/uinput.h).
- VIIPER also runs on Linux — [VIIPER](https://github.com/Alia5/VIIPER). Wheels under Proton are described as hit and miss by game — [GamingOnLinux](https://www.gamingonlinux.com/forum/topic/6042/).
- macOS virtual HID needs Apple-granted entitlements. Developers report no clear way to request the DriverKit one, the portal pointing to a CoreHID capability, and a gamepad app's request stuck about 2.5 months — [Apple forum](https://developer.apple.com/forums/thread/840311); [Apple forum](https://developer.apple.com/forums/thread/845599). Karabiner is the known open reference — [Karabiner-DriverKit-VirtualHIDDevice](https://github.com/pqrs-org/Karabiner-DriverKit-VirtualHIDDevice).

### Inferences
- Foreground-only registration actually suits a steering app, which is on screen and active, but screen lock or switching apps will drop the device. Test this.
- A custom descriptor with 16-bit axes and the Simulation Controls steering usage should be possible, since the API takes your SDP/descriptor settings, but I found no field report on Windows game detection.
- Since XInput-only games will not see Bluetooth HID, this mode cannot be the sole path on Windows.
- Steam on the PC could potentially map a generic HID device for Steam-launched games, reducing driver need in some cases, but this is unverified.

### Gaps
- Real Bluetooth HID latency and report rate on phones with Windows hosts: no measurements found.
- A device-support list by OEM: none exists officially.
- Steam Input's handling of wheels or virtual devices: searches returned nothing authoritative.
- Non-root Android USB accessory mode as HID: no source found.
- Xbox GIP device creation: no public route found.
- Steam Deck-specific `uinput` permissions under SteamOS: not verified.

## Force feedback: requirements and conversion to phone vibration

### Takeaway
To receive real wheel FFB, the virtual device must declare a HID PID descriptor (or an equivalent such as vJoy's), so DirectInput games send effects. The app must then simulate effects and map them to one crude phone motor. Expect "road feel" haptics, not torque. XInput yields only rumble.

### Cited Findings
- DirectInput-recognised FFB is tied to the Physical Interface Device usage page (0x0F), and few controllers use it — [SDL forum (informal)](https://discourse.libsdl.org/t/ble-hid-haptic-custom-hardware-support/28575).
- vJoy path: `FfbRegisterGenCB`, with Python decoding of packets into effect types via `ffb_packet_to_dict` — [pyvjoyffb](https://pypi.org/project/pyvjoyffb/); [vjoy-sys](https://docs.rs/crate/vjoy-sys/0.4.0/source/build.rs).
- HIDMaestro path: PID 1.0 responses and output events — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).
- Own-driver path: VHF `WriteReport` and `SetFeature`/`GetFeature` callbacks — [Microsoft Learn: VHF](https://learn.microsoft.com/en-us/windows-hardware/drivers/hid/virtual-hid-framework--vhf-).
- Bluetooth path: output and feature report types plus `onSetReport`, `onGetReport`, `onInterruptData` callbacks exist — [Android docs](https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice).
- Audio-derived haptics are a known alternative for games without FFB: PadForge uses low-frequency audio to drive rumble — [LaunchBox forum](https://forums.launchbox-app.com/topic/93722-hidmaestro-and-padforge/).
- Android: `createOneShot` amplitude 1–255; `createWaveform` amplitude 0–255 from API 26; predefined effects API 29; `startComposition` API 30; `createRepeatingEffect` API 36; a newer envelope and Builder API; primitive durations vary by device, with estimates from 5 to 500 ms — [VibrationEffect](https://developer.android.com/reference/android/os/VibrationEffect); [Custom haptic effects](https://developer.android.com/develop/ui/views/haptics/custom-haptic-effects).
- On devices without amplitude control, non-zero amplitudes round up to full strength, so apps should check `hasAmplitudeControl()` and fall back to on/off patterns. `areAllPrimitivesSupported()` checks primitives. Envelope effects arrived in Android 16 (API 36) and are gated by `areEnvelopeEffectsSupported()`. `VibrationEffect.Builder` is the preferred composition API from Android 16 (26Q4) — [Custom haptic effects](https://developer.android.com/develop/ui/views/haptics/custom-haptic-effects).
- Output on some wheels and games can be inconsistent even on real hardware: reports of FFB not working at race start in F1 25 — [EA forum](https://forums.ea.com/discussions/ea-app-technical-issues-en/f1-25-%e2%80%93-force-feedback-not-working-at-race-start--every-session/12505604).

### Inferences
- Suggested mapping on the PC, sending a low-rate summary rather than raw effects:
  - Constant force: amplitude proportional to magnitude, with a dead zone so steady cornering load does not buzz constantly.
  - Periodic (sine/square): short waveform, and because phone motors cannot follow high frequencies, map frequency to pulse rate.
  - Spring and damper: ignore or reduce; they depend on wheel position and speed, and a phone has no actuator to resist.
  - Rumble and collision spikes: `EFFECT_HEAVY_CLICK` or short strong one-shots.
- Transport: UDP over Wi-Fi at around 20–60 Hz, with short overlapping waveforms to avoid start/stop gaps; requires experimentation.
- Vibrating a phone held as a wheel also adds noise to gyro/accelerometer readings, so steering filters need to account for it. This is a reasoned risk, not measured.
- Battery, heat and motor quality will limit continuous use; offer an intensity slider and an off switch.

### Gaps
- Android vibration latency figures and the share of devices with amplitude control: not obtained.
- Which games actually send PID effects to a non-whitelisted virtual wheel: no list found.
- UDP Telemetry-based FFB as an alternative (F1 and others publish telemetry) was out of scope for this note.

## Anti-cheat: EAC, EA Javelin, Vanguard, BattlEye

### Takeaway
There is no official statement from any vendor about vJoy or ViGEm, but multiple user reports show EA titles refusing virtual controller software and Vanguard conflicting with one. Treat EA games as high risk and test them. I found no evidence that a user was banned for using vJoy or ViGEm, though the search coverage is limited.

### Cited Findings
- EA FC 25 message naming "VirtualController"; a third-party guide says EA's anti-cheat treats virtual driver layers as potential cheats and names vJoy Device and Virtual HID Device as things to disable — [Appuals, 17 Oct 2025](https://appuals.com/ea-fc-25-error-this-software-cannot-be-used-at-the-same-time-as-the-game-virtualcontroller/). This is not an EA statement.
- An F1 25 EA forum thread is titled to the effect that the software cannot be used at the same time as the game, naming reWASD — [EA forum, title only, page returned 403](https://forums.ea.com/discussions/f1-25-pt/este-software-n%C3%A3o-pode-ser-usado-ao-mesmo-tempo-que-o-jogo-rewasd/12320914).
- An EA FC 24 user reports the anti-cheat calling a software virtual controller incompatible, fixed by removing a DS4 app and using Steam's controller support — [Steam](https://steamcommunity.com/app/2195250/discussions/0/4434443557913362145).
- A post on EA forums reports launching only after deleting ViGEmBus, DS4Windows and related drivers, with an "Interception" error — [EA forum, snippet only](https://forums.ea.com/discussions/apex-legends-technical-issues-en/interception/12261311).
- F1 25 Javelin launch failures have many reported causes, including AutoHotkey and TPM/Secure Boot — [Steam thread](https://steamcommunity.com/app/3059520/discussions/0/685239622575361912/); [Steam thread](https://steamcommunity.com/app/3059520/discussions/0/684112192552972599/). None here mention vJoy.
- EA anti-cheat is described as kernel level and used across the F1 series — [Players.com.ua](https://players.com.ua/en/ea-anticheat-a-beginner-s-guidea-brief-look-at-the-world-of-cheats-and-anti-cheats-using-f1-as-an-example/).
- Vanguard: a user's VAN 1067 appeared only when reWASD created a virtual controller — [reWASD forum](https://forum.rewasd.com/forum/rewasd/technical-questions-aa/238606-rewasd-causing-valorant-van-1067-errors?p=238613). Vanguard can block incompatible software — [One Esports](https://www.oneesports.gg/valorant/riot-will-give-players-more-control-over-valorants-anti-cheat-program). A FaceIt representative said the Interception driver is blocked because cheats use it — [PC Gamer](https://www.pcgamer.com/the-controversy-over-riots-vanguard-anti-cheat-software-explained).
- BattlEye: general guidance on blocked files, with no vJoy-specific entry found — [BattlEye FAQ](https://battleye.com/support/faq).
- EAC: a joystick-related DLL was reported untrusted in an EA title, with vJoy plus mapper named as a workaround by that user — [EA forum](https://forums.ea.com/discussions/star-wars-games-discussion-en/anti-cheat-doesnt-trust-joystick-system-file/10608616).
- iRacing's anti-cheat blocked races with test signing on — [shauleiz issue #57](https://www.github.com/shauleiz/vJoy/issues/57).
- HIDMaestro acknowledges detectability by kernel anti-cheat — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro).

### Inferences
- Blocking appears to be per-title and possibly per-driver or per-process, rather than universal. A driver being installed but inactive can matter, per the FC 25 guide about drivers staying active.
- Bluetooth HID may be the least likely to trigger driver-based checks because no third-party PC driver exists, but this is untested and a generic HID device could still be ignored by the game.
- Product stance: never attempt to hide or evade. Provide a "disable virtual device" switch, publish a compatibility list, and avoid promising multiplayer use.

### Gaps
- No official EA, Riot, Epic or BattlEye policy on virtual wheels was found.
- No confirmed test of vJoy, ViGEmBus or HIDMaestro against F1 25 or any later F1 title (2026 season pack included). Run this test directly.
- EAC-specific reports (non-EA games) remain thin.

## Installation UX: user-level vs admin, reboot, Secure Boot changes

### Takeaway
Every Windows virtual-device path needs admin at least once, except Bluetooth HID, which needs only pairing. Secure Boot changes should never be asked of end users, because test signing conflicts with anti-cheat and security.

### Cited Findings
- Bluetooth HID: nothing to install on the receiving device; the user pairs and connects from the phone app — [PocketPad](https://github.com/Dathaze20/PocketPad).
- HIDMaestro: elevation required for create and cleanup; no reboot and no test-signing mode claimed; certificate placed in machine stores; removal tool available — [HIDMaestro README](https://github.com/hifihedgehog/HIDMaestro); [v2.0.1](https://github.com/hifihedgehog/HIDMaestro/releases/tag/v2.0.1).
- ViGEmBus: can be silently installed by a bundling installer, but stale service entries may persist until reboot — [Nimbus PR #5](https://github.com/owenpkent/Nimbus-Adaptive-Controller/pull/5). Old updater components needed reconfiguration after domain retirement — [End of Life statement](https://docs.nefarius.at/projects/ViGEm/End-of-Life/).
- vJoy: installer problems documented, including a greyed-out config app and failed device creation — [njz3 issue #17](https://github.com/njz3/vJoy/issues/17).
- Test signing requires `bcdedit` and Secure Boot off — [njz3/vJoy](https://github.com/njz3/vJoy).
- App with signed installer: Nimbus verifies via `/kp` kernel-policy check plus thumbprint pinning — [Nimbus PR #5](https://github.com/owenpkent/Nimbus-Adaptive-Controller/pull/5).

### Inferences
- Suggested UX: install the app per-user; on first enabling a virtual device, show one UAC prompt for the backend; run a self-test that confirms a device actually appears (for example, a query of HID devices), not just that a service exists; offer "Repair" and a full uninstall.
- Keep the elevated portion in a small helper or service, so the main UI and network listener run unprivileged. This matters because the app accepts network input from a phone: pair with a code and authenticate, since an unauthenticated listener would be a remote input injection route.
- Sign the app itself with at least an ordinary code-signing certificate to reduce SmartScreen friction; this is separate from driver signing. Specific reputation behaviour was not researched here.

### Gaps
- Whether any route supports a truly non-admin virtual gamepad on Windows: none found.
- Smart App Control's effect on these installers: not documented in the sources read.
- Reboot needs for vJoy 2.1.9.1 and ViGEmBus on a clean machine: not systematically documented.
