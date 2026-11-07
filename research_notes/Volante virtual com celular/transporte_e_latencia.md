# Phone-to-PC transport and latency for a phone-as-steering-wheel app (as of November 2026)

Evidence labels: **[V]** = primary page was read directly. **[S]** = taken from search-result excerpts only, page not read in full. **ESTIMATE** = my own engineering estimate, not a measurement.

Overall evidence warning: I found almost no controlled, phone-specific, one-way latency measurements for any of these transports. Most "measured" figures are campus-scale studies, forum captures or self-reports. Every per-transport millisecond figure in the comparison table is an ESTIMATE and should be replaced by the benchmark described in the Gaps of the first section before it is used to justify a decision.

## 1. Transport comparison: latency and jitter

### Takeaway
Wi-Fi UDP on 5 GHz or 6 GHz with a wired PC is the best default, because it needs no cable or developer mode and its typical delay is probably a few milliseconds, but its tail is the real risk: the best large study found a 99th percentile near 250 ms. USB is the best low-jitter option but no direct measurements were found. Bluetooth looks worse and more variable in the little evidence available.

### Cited Findings

**Wi-Fi: measured or reported**
- A study of more than 47,000 devices on the Tsinghua campus reports a long-tailed Wi-Fi latency distribution, with the 90th percentile around 20 ms and the 99th around 250 ms. This is a busy, enterprise-style network and not a home LAN — [Sui et al., MobiSys 2016, Microsoft Research](https://www.microsoft.com/en-us/research/?p=635400) [S]
- Tencent's IEEE 802.11 contribution reports that about 60% of mobile gamers connect by Wi-Fi, that 20-30% encounter in-game lag, and that 60-70% of that lag comes from the client-to-gateway hop — [IEEE 802.11-18/1419](https://mentor-ieee-org.bibliotheek.ehb.be/802.11/dcn/18/11-18-1419-02-0000-real-time-mobile-game-network-problem-highlight.docx) [S]
- A 2025 comparison of Wi-Fi 4 through 7 measured RTT and jitter and concluded that Wi-Fi 6/6E perform best overall, especially on low-interference 6 GHz. The page returned HTTP 403 to me, so no numbers were retrieved — [MDPI Telecom 6(2):40](https://mdpi.com/2673-4001/6/2/40) [S]
- Anecdote, Moonlight on Wi-Fi 6 at 5 GHz: network latency of about 7-9 ms, jumping to 20+ ms without an obvious cause — [NetSpot community](https://community.netspotapp.com/t/would-a-dedicated-router-or-ap-fix-moonlight-sunshine-latency-spikes/583) [S]
- Anecdote, Steam Deck on Wi-Fi with a wired PC: about 1 ms of network latency, self-reported with no method — [Reddit mirror](https://reddit.birdcat.cafe/r/SteamDeck/comments/1jsiexg/tips_for_game_streamers/mlonzu2/?context=3) [S]
- Anecdote, phone ping over 5 GHz showing min/avg/max 2.5/50.6/115.7 ms, in a thread about phone power saving. The excerpt is ambiguous about the exact power-save state during that run — [OpenWrt forum](https://forum.openwrt.org/t/high-ping-latency-via-5ghz-wifi/159399) [S]
- A generic article claims 5-20 ms wired and 20-100 ms Wi-Fi for streaming. These are unmeasured numbers and I would not rely on them — [Joltfly](https://joltfly.com/optimize-moonlight-game-streaming-for-ultra-low-latency) [S]

**Wi-Fi power save mechanics**
- A station that sleeps straight after communicating has its RTT effectively rounded up to the nearest beacon interval, even when the true RTT is much shorter — [Iowa State, 2015 TVT](https://www.engineering.iastate.edu/~daji/papers/2015tvt.pdf) [S]
- With a 100 ms beacon interval and a listen interval of 5, detecting buffered frames can take up to 500 ms in the worst case. A listen interval of 1 is common for lower data-path latency — [US patent 9210656](https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/9210656) [S]
- Power save adds latency to incoming packets, so some devices stay in constant-active mode during steady traffic (older Maemo devices, not Android) — [Maemo wiki](https://wiki.maemo.org/Wifi_PSM) [S]

**USB**
- scrcpy claimed 35-70 ms in an old README. That is a full video mirror pipeline including encode and decode, so it says nothing direct about the tunnel — [scrcpy v1.17 README](https://git.blob42.xyz/Archives/scrcpy/raw/commit/dca11f6c513e3b63e5494fce5bfffdc5c29be002/README.md) [S]

**Bluetooth LE**
- Citation caution: the Nordic DevZone pages below returned HTTP 403 when I tried to verify them. The figures come from search excerpts, and the excerpts did not tie each figure to a specific URL, so the figure-to-link mapping for those three bullets is my best match and is unconfirmed. Check them before quoting.
- AOSP sets `CONNECTION_PRIORITY_HIGH` to min 9 and max 12 units of 1.25 ms, which is 11.25-15 ms — [Flutter BLE docs](https://pub.dev/documentation/flutter_splendid_ble/1.2.0/central_models_ble_connection_priority/BleConnectionPriority.html); [AOSP GATT config commit](https://android.googlesource.com/platform/packages/apps/Bluetooth/+/0ec1e3f%5E%21/) [S]
- The priority is a request and not a guarantee. A Nexus 9 on Android 6 measured 48.75 ms although 15 ms or less was requested (old device) — [android-midi group](https://groups.google.com/g/android-midi/c/awtanDwxaCQ/m/7mkT2BojBgAJ) [S]
- A developer measured about 80 ms average with a 7.5 ms interval, then attributed it to how Windows handles HID over BLE and not to the link. In a separate test with an LLPM-enabled central (Nordic hardware, not a phone) the average was about 1.5 ms — [Nordic DevZone thread](https://devzone.nordicsemi.com/f/nordic-q-a/119571/unusually-high-ble-connection-latency/526410) [S]
- A BLE HID mouse capture showed a packet about every 7.5 ms — [Nordic DevZone](https://devzone.nordicsemi.com/f/nordic-q-a/46902/understanding-connection-interval-nrf-sniffer-and-transfer-speed) [S]
- Android notifications at a 10 ms interval kept up with the sample rate over a minute or more, but per-packet spacing varied from 16 to 38 ms — [Nordic DevZone](https://devzone.nordicsemi.com/f/nordic-q-a/84802/how-to-shorten-the-data-transmission-cycle-using-sd_ble_gatts_hvx) [S]

**Bluetooth Classic**
- Vendor figures conflict: BLE is listed at under 6 ms against 100 ms for Classic in one source, while another lists Classic data latency at about 2.5 ms and 100 ms for connection setup. The 100 ms figure is probably connection setup — [Ezurio](https://ezurio.com/resources/blog/bluetooth-low-energy-vs-bluetooth-classic-what-s-the-difference); [RF Wireless World](https://www.rfwireless-world.com/terminology/classic-bluetooth-vs-ble) [S]
- iOS does not support Classic SPP outside MFi-certified accessories, while Android supports it natively (vendor source) — [RF Essentials](https://rfessentials.com/resources/rf-glossary/bluetooth-classic/) [S]

**WebRTC and WebSocket**
- Results were vendor blogs without LAN measurements. Ably says no general winner can be declared and that WebRTC over UDP suits cases where speed matters more than losing some packets — [Ably](https://ably.com/topic/webrtc-vs-websocket) [S]

### Inferences

**Comparison table. All latency figures are ESTIMATES of one-way application-to-application delay on a healthy link. They are not measurements.**

| Transport | Typical | Jitter / tail | Setup friction | Verdict |
|---|---|---|---|---|
| Wi-Fi 5/6, 5 or 6 GHz, PC wired to router | 2-5 ms | Low jitter, occasional spikes of 20-100+ ms | Firewall, permission | **Default** |
| Wi-Fi 5/6, PC also on Wi-Fi | 4-10 ms | Moderate, two wireless hops | Same | Acceptable, warn the user |
| Wi-Fi 2.4 GHz | 5-20 ms | High in crowded bands | Same | Warn the user |
| Phone hotspot, PC as client | 2-8 ms | Unknown, see section 2 | Cellular data side effects | Fallback for AP isolation |
| Wi-Fi Direct | Unknown | Unknown | High, poor Windows UX expected | Do not build first |
| USB via ADB reverse/forward | 1-3 ms | Very low, but TCP | Developer mode | Power-user option |
| USB tethering (RNDIS/NCM) | 1-2 ms | Very low, real UDP | One Android toggle, no dev mode | **Recommended wired mode** |
| Android Open Accessory | About 1 ms | Very low | Custom PC USB driver | Not worth it |
| BLE, 11.25-15 ms interval | 10-30 ms | High | Pairing | Niche |
| Bluetooth Classic SPP | 5-30 ms | Moderate to high, unmeasured | Pairing, no iOS | Niche |
| WebRTC data channel in a browser | 5-15 ms | Similar to Wi-Fi plus browser scheduling | Signalling, HTTPS page | Try-before-install |
| WebSocket in a browser | 5-20 ms, spikes on loss | TCP head-of-line stalls | HTTPS/WSS certificate | Weakest option |

- The steering stream is almost entirely uplink, phone to PC. Power save buffering at the AP hits downlink hardest, so it should affect echo packets, clock sync and rumble more than steering input itself. A steady 120 Hz stream also gives the radio little idle time. This is reasoning from the mechanism and needs testing, but it suggests the low-latency lock is more important for downlink and for scan or roam spikes.
- Because power save inflates echoes, a measured RTT may overstate uplink latency. Do not simply halve an RTT measured without the lock.
- ADB tunnels are, to my knowledge, TCP only (see Gaps). Over a clean USB link TCP stalls should be rare, so set `TCP_NODELAY` and send latest-only.
- The campus 250 ms p99 is a pessimistic bound for a home network, but it demonstrates that the failsafe must assume outages of hundreds of milliseconds.

### Gaps
- **No controlled phone measurements found** for UDP on 2.4 GHz vs 5 GHz vs Wi-Fi 6, router vs hotspot vs Wi-Fi Direct, or lock on vs off. Android's own validation guide uses ping histograms and publishes no numbers (section 2).
- **No measured figures found** for ADB tunnels, RNDIS/NCM tethering, AOA, RFCOMM on Android, or LAN WebRTC vs WebSocket. No Steam Link controller input data or scrcpy control-channel latency found.
- A claim that more than 50% of TCP packets exceed 20 ms on the Wi-Fi hop (47 APs) appeared in results as a mirror of the MobiSys paper. It conflicts with the abstract's 90th percentile of 20 ms, may be a different paper, and I could not pin the source, so I excluded it. The Microsoft PDF was fetched but could not be parsed here, so per-band figures from it are not included.
- Unverified from my background knowledge: `adb reverse/forward` supports TCP but not UDP; Android's `BluetoothHidDevice` (API 28+) is Classic HID only.
- **Recommended benchmark (2-3 days):** stream timestamped 60-byte UDP at 120 Hz for 30 minutes per configuration, across 3 or more phones (a Pixel, a Samsung, a budget device), with lock on and off, and report p50/p95/p99/p99.9, one-way delay variation and the longest gap between packets.

## 2. Android, iOS and Windows platform constraints

### Takeaway
Hold `WIFI_MODE_FULL_LOW_LATENCY` in a foreground, screen-on app. A correction to the brief: local network protection is opt-in only on Android 16, and it becomes mandatory on Android 17 for apps targeting SDK 37, so the app must plan for `ACCESS_LOCAL_NETWORK`. On Windows, add the firewall rule from the installer, because a dismissed prompt creates block rules that override allow rules.

### Cited Findings

**Wi-Fi locks**
- `WIFI_MODE_FULL_LOW_LATENCY` was added in API 29. It is active only when connected to an access point, with the screen on and the acquiring app in the foreground. It may reduce battery life and throughput and reduce scan frequency, so the device may not roam to the best AP — [WifiManager](https://developer.android.com/reference/android/net/wifi/WifiManager) [V]
- `WIFI_MODE_FULL_HIGH_PERF` was added in API 12 and deprecated in API 34, and requests for it are now silently converted to a low-latency lock with its restrictions. The cited reason is power dissipation — [WifiManager](https://developer.android.com/reference/android/net/wifi/WifiManager) [V]
- AOSP lists an additional activation condition: the device must have internet access. The framework disables 802.11 power save while the mode is active. Driver and firmware tuning for scanning and roaming is implementation-specific — [AOSP Wi-Fi low latency](https://source.android.com/docs/core/connect/wifi-low-latency) [V]
- AOSP sets no numeric target. The pass criterion is only that average ping is lower with the mode on, based on 3-hour runs. It can be forced with `adb shell cmd wifi force-low-latency-mode` — [AOSP](https://source.android.com/docs/core/connect/wifi-low-latency) [V]
- `MulticastLock` allows an app to receive Wi-Fi multicast packets — [WifiManager](https://developer.android.com/reference/android/net/wifi/WifiManager) [V]

**Android local network protection**
- Android 16 offered opt-in only, through a compat toggle. Android 17 enforces it for apps targeting SDK 37+, which are blocked by default. Apps targeting below 37 keep implicit access through `INTERNET`, and Google says this is temporary — [Android docs](https://developer.android.com/privacy-and-security/local-network-permission) [V]
- It covers outgoing and incoming TCP and all UDP unicast, multicast and broadcast, across all networking APIs. Blocked UDP typically returns `EPERM`, and TCP typically times out — [Android docs](https://developer.android.com/privacy-and-security/local-network-permission) [V]
- `ACCESS_LOCAL_NETWORK` is a runtime permission in the `NEARBY_DEVICES` group. If the user has already granted another permission in the group, such as Bluetooth, there is no new prompt — [Android docs](https://developer.android.com/privacy-and-security/local-network-permission) [V]
- `NsdManager` with `FLAG_SHOW_PICKER` shows a system picker, and connections to the returned addresses need no permission — [Android docs](https://developer.android.com/privacy-and-security/local-network-permission) [V]

**Sensors**
- Reading accelerometer, gyroscope or magnetometer above 200 Hz requires the `HIGH_SAMPLING_RATE_SENSORS` permission from Android 12. It is a normal permission granted at install. Without it, rates are capped at 200 Hz — [SensorManager](https://developer.android.com/reference/android/hardware/SensorManager?hl=tr); [Qt compatibility map](https://doc.qt.io/qt-6.12/compatmap.html); [AOSP commit](https://android.googlesource.com/platform/frameworks/native/+/af91a91c5c) [S]

**iOS**
- Apple's reference is [TN3179](https://developer.apple.com/documentation/technotes/tn3179-understanding-local-network-privacy). Only its title was retrievable, so I could not verify its contents (see Gaps).

**Windows Firewall**
- With no allow rule, a prompt appears on first launch or first network use. If an admin answers No or cancels, block rules are created. A non-admin user gets block rules whichever option they choose. The rules must be deleted before the prompt can appear again — [Microsoft Learn](https://learn.microsoft.com/windows/security/operating-system-security/network-security/windows-firewall/rules) [V]
- Explicit block rules take precedence over conflicting allow rules — [Microsoft Learn](https://learn.microsoft.com/windows/security/operating-system-security/network-security/windows-firewall/rules) [V]
- Microsoft recommends providing rules before first launch, creating rules in all three profiles and enabling only those needed, and using a Local Subnet remote-address restriction for home-network apps. Application rules require a full path, with no wildcards — [Microsoft Learn](https://learn.microsoft.com/windows/security/operating-system-security/network-security/windows-firewall/rules) [V]

### Inferences
- **Hotspot and offline-router risk.** The lock is documented as active only when connected to an AP, and AOSP also requires internet access. So the low-latency lock probably gives nothing when the phone is itself the hotspot, and may not engage on a router without internet. Wi-Fi Direct is likewise uncertain. Test all three.
- **No background operation.** A locked phone or background app loses the low-latency lock, and `HIGH_PERF` no longer works around this since API 34. Keep the screen on with the app in the foreground, which fits the use case. Treat onPause as a failsafe event.
- **Reduced scanning is a feature here,** since scan-related spikes should drop, but the phone may stay on a weak AP. Show a signal or link-quality warning.
- **Android permission strategy.** QR or manual IP requires `ACCESS_LOCAL_NETWORK` when targeting SDK 37. Requesting it with a clear explanation on the connect screen is the simplest route. The NSD picker avoids the prompt but requires working mDNS and adds a system dialog. Offer the picker as an alternative when the permission is denied. Handle `EPERM` explicitly with a message rather than a silent timeout.
- **Windows.** Add a UDP rule scoped to the app's full path and Local Subnet at install, enabled for Private. Handle networks marked Public by asking the user first, because Windows profiles on home Wi-Fi are often Public (from my experience, not sourced). Include a diagnostic that looks for block rules on the executable, since these would defeat the allow rule, and offers to remove them with elevation.
- **USB debugging drawbacks (judgement).** It requires enabling Developer Options, an RSA authorisation prompt, and bundling or requiring ADB on the PC, and may be restricted on managed phones. USB tethering avoids this, which is why I rank it first among the wired modes. However, it may change the phone's network path, so the PC must use the tether interface's IP.

### Gaps
- iOS Local Network Privacy details could not be verified from Apple's page. The following is unverified background knowledge: a one-time prompt, sticky denial, `NSLocalNetworkUsageDescription`, `NSBonjourServices`, and a restricted multicast entitlement.
- Not found: whether loopback traffic for `adb reverse` or a tethering interface counts as "local network" under Android 17. Test before relying on it.
- Not found: the Google Play deadline for target SDK 37, which determines when enforcement becomes unavoidable.
- Not found: background network restriction details such as Doze and App Standby for this use case. Likely moot with a foreground-only design.
- Android's actual power-save timeout per device: nothing found.

## 3. Discovery and pairing

### Takeaway
Make QR-code pairing the primary route, since it delivers address, port and key in one step, and keep manual IP as the fallback. mDNS is a convenience for reconnecting, and it should not be the only route, given the Windows firewall and multi-stack problems.

### Cited Findings
- mDNS and `.local` resolution fall under Android local network protection, and so do multicast and broadcast sends — [Android docs](https://developer.android.com/privacy-and-security/local-network-permission) [V]
- Bonjour uses UDP 5353, and a manual inbound and outbound rule is sometimes suggested. Some rules may prevent mDNS even with the firewall off — [Microsoft Q&A](https://learn.microsoft.com/en-us/answers/a/7919376) [S]
- In block-by-default enterprise setups, firewall logs misattributed blocked port 5353 traffic to the wrong process, which makes diagnosis harder — [Microsoft Tech Community](https://techcommunity.microsoft.com/blog/networkingblog/mdns-in-the-enterprise/3275777/replies/3970102) [S]
- Several programs and services use mDNS on Windows: Apple's Bonjour is bundled by apps such as iTunes, and disabling it does not stop mDNS traffic, because other services use it too (lower-quality sources) — [sslinsights](https://sslinsights.com/what-is-port-5353/); [Hunting for mDNS Vulnerabilities](https://danielloosec.github.io/blog/jekyll/update/2018/04/18/mDNS.html) [S]
- Precedent: GyroBridge pairs a phone by having the user scan a QR code that opens a page in Chrome, and defaults to port 26760 — [PhoneGyro v1.1.2](https://github.com/MrHoustonOff/PhoneGyro/releases/tag/v1.1.2) [S]

### Inferences

| Method | Friction | Reliability | Role |
|---|---|---|---|
| QR code | Scan once | High, needs no multicast, but needs correct IP choice on multi-NIC PCs | **Primary** |
| Manual IP + PIN | High | High | Fallback |
| mDNS / NSD | None when working | Medium: firewall, AP isolation, multicast filtering | Reconnect convenience |
| UDP broadcast | None when working | Medium: same permission, may be filtered | Optional secondary, simple on both sides |

- **QR contents:** protocol version, every candidate IPv4 address, UDP port, 128-bit or larger random pairing key, PC name and a key fingerprint. Listing all addresses matters on PCs with VPN, Hyper-V or WSL adapters, where choosing one address can pick the wrong NIC. Have the phone probe all of them and use the first reply.
- **Reconnects:** store the key and the last IP. On failure, retry the last IP, then mDNS or broadcast, and only then ask for a rescan. DHCP changes then cost nothing visible.
- **On Windows,** prefer to advertise with a small in-process mDNS responder or the OS API rather than requiring Bonjour. Bind with address reuse, since other processes share 5353. This is background judgement; test against Bonjour installed and not installed.
- **Failure detection:** if the QR handshake gets no reply, show targeted guidance covering the firewall, guest network or AP isolation, and different subnet, rather than a generic error.

### Gaps
- Not found: a primary, reliable source on the Windows `DnsServiceRegister` API behaviour and its coexistence with Bonjour. The search turned up nothing usable. Read the Microsoft Learn page and test.
- Not found: data on how common AP isolation or multicast filtering is on consumer routers, including Brazilian ISP-supplied routers.

## 4. Protocol design

### Takeaway
Use a custom 32-byte binary UDP packet with latest-wins semantics, 120 Hz by default and up to 200 Hz, truncated HMAC, and a two-stage watchdog. Do not use DSU as the primary protocol, since it has no authentication and only CRC32, 8-bit sticks and a 100-byte message, and it assumes the phone is the server. It remains useful as an optional secondary output for emulators.

### Cited Findings
- DSU/Cemuhook runs over UDP, with default port 26760 and protocol version 1001. It has a 16-byte header (magic `DSUS`/`DSUC`, version, length, CRC32, ID) followed by a message type, and all numbers are little-endian — [cemuhook-protocol](https://v1993.github.io/cemuhook-protocol/) [V]
- The controller data message is 100 bytes including the header. It has a per-client 32-bit packet counter, 8-bit analog sticks and buttons, a 64-bit motion timestamp in microseconds, and accelerometer in g and gyroscope in deg/s as 32-bit floats — [cemuhook-protocol](https://v1993.github.io/cemuhook-protocol/) [V]
- The document specifies no controller data rate and no fixed subscription timeout, only that servers should implement a timeout because client disconnects cannot be detected. It suggests zeroing rumble after about 5 seconds without packets. It has no authentication or encryption — [cemuhook-protocol](https://v1993.github.io/cemuhook-protocol/) [V]
- DSU is in active use for phones: GyroBridge bridges iOS and Android motion through it, and cemu-no-gyro uses a local web page as a motion source — [PhoneGyro](https://github.com/MrHoustonOff/PhoneGyro/releases/tag/v1.1.2); [cemu-no-gyro](https://github.com/nostlan/cemu-no-gyro) [S]
- Android sensors above 200 Hz need an additional normal permission — [Qt compatibility map](https://doc.qt.io/qt-6.12/compatmap.html) [S]

### Inferences

These are all design recommendations from my own reasoning, not measured results.

**Packet (32 bytes, little-endian)**

| Offset | Size | Field | Notes |
|---|---|---|---|
| 0 | 2 | magic + version | Reject foreign traffic cheaply |
| 2 | 1 | type | Input, ping, pong, hello, bye |
| 3 | 1 | flags | Touch active, calibrating, low battery |
| 4 | 4 | session_id | Set at handshake |
| 8 | 4 | seq | Monotonic; drop if not newer |
| 12 | 4 | t_send_us | Wraps every 71 minutes; handle with wrap-around subtraction |
| 16 | 2 | steering (i16) | Normalised, -32767..32767; 16 bits, versus DSU's 8-bit sticks |
| 18 | 2 | throttle (u16) | |
| 20 | 2 | brake (u16) | |
| 22 | 2 | buttons (u16) | |
| 24 | 8 | HMAC tag | HMAC-SHA256 truncated to 64 bits |

Size on the wire is 60 bytes with IP and UDP headers, which is 58 kbit/s at 120 Hz, 96 kbit/s at 200 Hz, and 120 kbit/s at 250 Hz. Bandwidth is irrelevant. Packet rate and airtime are the real costs. Send the computed steering angle instead of raw IMU, so that calibration stays on the phone. Include raw IMU in an optional debug message type only.

**Send rate.** Sample-and-hold adds a mean delay of half the period:

| Rate | Period | Mean added | Worst added | Assessment |
|---|---|---|---|---|
| 60 Hz | 16.7 ms | 8.3 ms | 16.7 ms | Too slow, beats against a 60 fps game |
| **120 Hz** | 8.3 ms | 4.2 ms | 8.3 ms | **Default** |
| 200 Hz | 5 ms | 2.5 ms | 5 ms | Optional, top of the no-permission sensor rate |
| 250 Hz | 4 ms | 2 ms | 4 ms | Needs sensors above 200 Hz or duplicates |
| 500 Hz | 2 ms | 1 ms | 2 ms | Needs capable hardware, and burns battery and airtime for a 1 ms mean gain over 250 Hz |

Going from 120 Hz to 500 Hz saves about 3 ms mean, against a 60 fps game frame of 16.7 ms and a Wi-Fi tail that dwarfs it. Send when a new sensor sample arrives instead of on a separate timer, so that no second sampling delay stacks up. Back off from 200 to 120 or 60 Hz if loss or queueing grows, because a sender that overruns the radio queue creates latency.

**Packet loss.** Use latest-wins with no retransmission and no jitter buffer. Each packet carries the absolute state. Discard any `seq` not newer than the last accepted. Do not reorder or interpolate by default. Optional short extrapolation is risky near the limit of the input and should stay off in v1. Critical button edges such as gear shift are absolute state too, so a held button survives single-packet loss, given a minimum hold of about 3 packets on the phone.

**Heartbeat and failsafe.** The input stream is the heartbeat, with PC-to-phone pings at 1-2 Hz for status and clock sync.

| Silence | Action |
|---|---|
| Under 50 ms | Hold last value |
| 50-100 ms | Hold last value, flag link warning in UI |
| Over 100 ms | Ramp throttle and brake to 0 over about 50 ms, and hold steering |
| Over 500 ms | Ramp steering to centre over about 200 ms, and release buttons |
| Over 3 s | Show disconnected; the virtual device stays attached and neutral |

Holding steering during the first stage avoids a sudden swerve on a short Wi-Fi spike. Thresholds should be configurable and tuned from benchmark data. Also send an explicit `bye` on onPause, on screen off, and on app exit for an instant, clean release. On reconnect, apply a brief steering slew limit of about 100 ms to prevent a jump. Keep the virtual device plugged in across dropouts so that the game does not lose its bound controller.

**Clock sync and measurement.** Use NTP-style ping/pong with timestamps t1 (phone send), t2 (PC receive), t3 (PC send), t4 (phone receive):
- `RTT = (t4 - t1) - (t3 - t2)` and `offset = ((t2 - t1) + (t3 - t4)) / 2`.
- Run at 1-2 Hz, and keep the sample with the lowest RTT from a sliding window of about 30 s. A low RTT bounds the error and rejects power-save-inflated samples.
- Estimate drift as a slope over minutes.
- Jitter requires no clock sync: compare `(arrival - t_send)` across packets to get one-way delay variation.
- Asymmetry from an uplink/downlink difference is unobservable with this method, so report the result as an estimate. For ground truth, use a photodiode or a high-speed camera at least once.
- Display p50/p95/p99, loss percentage and the longest gap in the app's diagnostics. It is valuable for support and for the Android benchmark above.

**Security.** The threat is another LAN device injecting or replaying inputs, and the data itself is not confidential.
- Exchange a random key of at least 128 bits by QR, derive a session key in an authenticated hello with nonces from both sides, and put a truncated HMAC on each packet. Reject any `seq` not newer than the last, which also blocks replay within a session. A fresh session key per connection prevents replay across sessions.
- DTLS is **not needed** for v1 because there is nothing secret in the stream, it adds a handshake and library weight, and the HMAC covers integrity and authenticity. If the channel ever carries secrets, use a vetted AEAD or DTLS, and do not design custom encryption.
- HMAC-SHA256 on this packet size should be negligible on current phones, though I did not benchmark it.
- A pure unauthenticated mode should not exist, since a spoofed "full throttle" is a nuisance on shared networks. The Windows rule should be limited to Local Subnet as well.

**Reuse.**
- **DSU:** implement it optionally on the PC as a local bridge to `127.0.0.1:26760`, so that emulators can see motion. This is cheap and gains a feature, with the custom authenticated protocol still on the wireless link. This is more secure than exposing DSU to the LAN.
- **OSC:** unsuitable as the wire format, in my judgement. Text addresses add overhead and there is no built-in authentication. It may be useful only for a hobbyist output plugin.

### Gaps
- OSC, and alternatives such as vJoy feeder and Open Sound Control tooling, were not researched from primary sources. The OSC conclusion above is background judgement.
- No measured data was found for the throughput or battery cost of different send rates on phones. Battery testing should be part of the benchmark.
- Failsafe thresholds are unvalidated estimates with no usability data.
- Whether a wheel-like use needs a filter on the send side belongs to the sensor research track, and is not covered here.

## 5. Windows receiver and desktop stack

### Takeaway
Use a blocking UDP receive on a dedicated thread with MMCSS registration, so that packet arrival drives the wake-up. Do not poll with a timer, since Windows 11 no longer guarantees a fine timer for hidden window-owning processes, which is exactly what a tray app is. Rust or C++ fits the receiver best, but my stack comparison is judgement and not sourced research.

### Cited Findings
- From Windows 10 2004, `timeBeginPeriod` no longer affects global timer resolution. Windows uses the finest value requested by any process that called it for those processes — [Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/api/timeapi/nf-timeapi-timebeginperiod) [S]
- From Windows 11, if a window-owning process becomes fully occluded, minimized or otherwise invisible or inaudible, Windows does not guarantee a resolution higher than the system default — [Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/api/timeapi/nf-timeapi-timebeginperiod) [S]
- A finer resolution costs scheduler overhead and may prevent CPU power saving. It does not improve the performance counter — [Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/api/timeapi/nf-timeapi-timebeginperiod) [S]
- `CREATE_WAITABLE_TIMER_HIGH_RESOLUTION` is available from Windows 10 1803 for time-critical work where delays of a few milliseconds are unacceptable — [Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/api/synchapi/nf-synchapi-createwaitabletimerexw) [V]
- MMCSS: threads join a task through `AvSetMmThreadCharacteristics` or `AvSetMmMaxThreadCharacteristics`. Default tasks include Audio, Capture, Distribution, Games, Playback, Pro Audio and Window Manager — [Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/procthread/multimedia-class-scheduler-service) [V]
- MMCSS priorities: High category is 23-26 and designed for Pro Audio; Medium is 16-22 for foreground application threads; Low is 8-15; exhausted threads drop to 1-7. `SystemResponsiveness` reserves CPU for low-priority work, and a value of 100 disables MMCSS. The clock rate guarantee was removed in Windows 7 — [Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/procthread/multimedia-class-scheduler-service) [V]
- Input comparison: gamepadla lists about 125 Hz (8 ms) maximum polling for both DualSense and Xbox Series in its comparison page, which is flagged as a module in testing that may contain errors — [gamepadla](https://gamepadla.com/versus/sony-dualsense_vs_xbox-core-controller.html) [V]
- Community wheel latency figures from a July 2019 forum post: Simucube 1 5.008 ms, Simucube 2 Pro 5.969 ms, Fanatec Podium DD1 10.996 ms. The post itself does not describe the method, and another poster in the thread calls the conditions unscientific and the results unreliable. A search excerpt described this kind of test as a force-feedback spring effect, so treat these as FFB-path numbers and not input-only ones, and as weak evidence — [Granite Devices forum](https://community.granitedevices.com/t/simucube-firmware-0-50-x-feedback-thread/2924?page=7) [V]

### Inferences
- **Receiver loop.** Use a dedicated thread with a blocking `recvfrom` or `WSARecvMsg`, or an I/O completion port, so the kernel wakes it when a packet arrives. There is no timer in this path. Drain the socket until `WSAEWOULDBLOCK`, keeping only the newest packet, then update the virtual device immediately. Keep `SO_RCVBUF` small enough that stale data cannot accumulate, such as a few KB.
- **Thread priority.** The "Games" MMCSS task is a sensible start, and "Pro Audio" only if testing shows starvation under a heavy game, since the High category is explicitly designed for audio. The thread must never spin, since a busy thread at that priority hurts the game. Do not raise the process-wide priority.
- **Watchdog.** This needs a timer, but 10-15 ms accuracy is fine since its thresholds are 50-500 ms. Use a high-resolution waitable timer if finer resolution is ever needed, instead of depending on `timeBeginPeriod`. Avoid `timeBeginPeriod` for a tray app, given the occlusion rule and its power cost.
- **Games poll per frame**, so at 60 fps the game samples only every 16.7 ms, and at 120 fps every 8.3 ms. Delivery faster than a few hundred Hz does not reduce the game's input wait, and only slightly narrows staleness.
- **Stack (judgement, not researched in this pass):**

| Option | Latency fit | UX fit | Comment |
|---|---|---|---|
| **Rust core + Tauri tray/settings UI** | Excellent, no GC | Good, small install, web UI | **Recommended** if the team knows Rust. Needs FFI for driver and MMCSS, which is straightforward. |
| C++ core + small UI | Excellent | Laborious | Natural if the virtual driver is also C++ |
| C# (.NET) + WPF or WinUI | Good with care; avoid allocation on the receive path, as GC pauses are the concern | Excellent native Windows UX | **Fastest to build**; a reasonable MVP choice, with the hot loop tested for GC pauses |
| Electron | Keep the hot path out of it, in a native helper | Good, heavy | No advantage over Tauri for a tray app |

  Whichever is used, keep the receiver in a separate native thread or process from the UI, so that a UI stall cannot affect input.

### Gaps
- Not researched from primary sources in this pass: Rust vs C# vs C++ latency, Tauri vs Electron vs WPF vs WinUI footprint and tray support, and whether the receiver should be separate from the driver. The table above is judgement, and the search budget went to transport data.
- Not found: a primary citation for per-frame XInput or DirectInput polling or specific wheel USB polling rates. 1000 Hz wheels are commonly stated, and the search returned only low-quality product claims (0.3-0.4 ms for Fanatec and Moza with no method), which I am excluding. Per-frame polling varies by game.
- Not found: measurements of MMCSS benefit for UDP receive, or of Windows UDP receive wake-up latency. Test under a real CPU-heavy game load.
- `SO_RCVBUF` guidance and Winsock details are background knowledge.

## 6. End-to-end latency budget

### Takeaway
ESTIMATE: the phone-specific portion (motion to virtual driver output) is about 14 ms in the typical case over 5 GHz Wi-Fi, against an end-to-end motion-to-photon total of about 55 ms at 60 fps. That is about 8-12 ms more than a wired wheel, which is under one 60 fps frame and in the same range as Bluetooth pads. The game and display contribute most of the total. Network spikes, and not the average, are the main perceptible difference.

### Cited Findings

Measured comparison data for wireless and wired pads:

| Device and connection | Button latency | Stick latency | LatScore |
|---|---|---|---|
| DualSense, cable | 7.8 ms | 12.6 ms | 11.9 ms |
| DualSense, dongle | 6.6 ms | 6.5 ms | 7.8 ms |
| DualSense, Bluetooth | 9.0 ms | 13.6 ms | 23.3 ms |
| Xbox Series, cable | 6.5 ms | 8.3 ms | 10.4 ms |
| Xbox Series, dongle | 7.2 ms | 8.9 ms | 11.1 ms |
| Xbox Series, Bluetooth | 12.2 ms | 13.0 ms | 16.7 ms |

- All of the above are from [gamepadla's comparison](https://gamepadla.com/versus/sony-dualsense_vs_xbox-core-controller.html) [V]. LatScore combines button and stick latency with a jitter penalty, with an undisclosed formula.
- Caveats: the comparison module says it "may contain errors." The page does not describe the latency equipment or procedure. Another gamepadla page says its tests rely on polling rate and do not represent actual input lag — [gamepadla Elite Core](https://gamepadla.com/xbox-elite-series-2-core.html) [S]. Other gamepadla pages show different values for the same pads — [gamepadla](https://gamepadla.com/versus/xbox-core-controller_vs_xbox-one-controller.html) [S].
- Force feedback loop figures for wheels are in section 5. They measure a different thing, and they are not comparable to the table above.

### Inferences

**ESTIMATE. Stage budget in milliseconds for 5 GHz Wi-Fi, a healthy home network, 200 Hz sensor, 120 Hz timer send in the typical column, and a 60 fps game. None of these rows are measurements.**

| Stage | Best | Typical | Worst | Basis |
|---|---|---|---|---|
| Sensor sampling and delivery | 1 | 3 | 10 | 200 Hz period is 5 ms, plus OS delivery |
| Filtering (group delay) | 0 | 2 | 10 | Depends on filter; see sensor track |
| App processing | 0.2 | 0.5 | 5 | Worst includes a scheduling or GC stall |
| Send wait | 0 | 4 | 8.3 | 120 Hz timer; goes to about 0 if send-on-sample |
| Network, Wi-Fi | 1 | 3 | 20 | Excludes outages; p99 spikes can be far larger |
| PC receive | 0.1 | 0.3 | 2 | Blocking receive wake-up |
| Virtual driver and report path | 0.1 | 1 | 4 | Driver-dependent |
| **Subtotal: phone-specific** | **2.4** | **13.8** | **59.3** | |
| Game input wait | 0 | 8.3 | 16.7 | Half or full frame at 60 fps |
| Game simulation and render queue | 16.7 | 25 | 50 | Engine and vsync-dependent |
| Display | 3 | 8 | 20 | Panel and mode-dependent |
| **Total** | **22.1** | **55.1** | **146** | |

**Comparison. ESTIMATE, using the same game and display rows (typical 41.3 ms).**

| Input device | Input portion | Typical total | Extra versus wired wheel |
|---|---|---|---|
| Wired USB wheel | About 1-2 ms | About 43 ms | Baseline |
| Phone, USB tethering, send-on-sample | About 8 ms | About 49 ms | About 6 ms |
| Phone, 5 GHz Wi-Fi, send-on-sample | About 10 ms | About 51 ms | About 8 ms |
| Phone, 5 GHz Wi-Fi, 120 Hz timer | About 14 ms | About 55 ms | About 12 ms |
| Xbox or DualSense on dongle (gamepadla, stick 6.5-8.9 ms) | 6.5-8.9 ms | About 48-50 ms | About 5-7 ms |
| Xbox or DualSense on Bluetooth (gamepadla, stick 13-13.6 ms) | 13-13.6 ms | About 54-55 ms | About 11-12 ms |
| Phone, BLE | About 25-45 ms | About 66-86 ms | About 23-43 ms |

- These compare loosely, since gamepadla's method and start and end points are undisclosed and may differ from mine.
- **Practical reading:** the phone's added average latency is about one Bluetooth pad's worth, and under a frame at 60 fps. So reducing the game's frame time will often save more than optimising the transport. Going from 60 to 120 fps saves about 4 ms on input wait and a further amount in the render chain.
- **Priorities, in order of expected return:**
  1. Eliminate spikes (5 GHz, wired PC, low-latency lock), since jitter is what the player will perceive.
  2. Use send-on-sample rather than a free-running timer, which saves about 4 ms at no cost.
  3. Keep filter delay low, which is potentially a larger stage than the network.
  4. Treat rates above 200 Hz as low priority.
- **Target for the MVP:** a phone-specific p50 under 15 ms and p99 under 40 ms on a good network, and a gap over 100 ms less than about once per 10 minutes. These are proposed targets, not established thresholds.

### Gaps
- Not found: a controlled study of perceptible steering latency in sim racing, so I cannot state that the 8-12 ms difference is imperceptible.
- Not found: measured Android sensor-to-app delivery latency. The 200 Hz cap is documented, but no latency data was retrieved. This is a major uncertainty in the budget. Measure with a physical rig and a photodiode or a high-speed camera.
- Not found: reliable input-only latency for wired wheels, and the game and display rows are generic estimates rather than measurements from specific titles. A [GTPlanet thread](https://www.gtplanet.net/forum/threads/measure-your-total-input-lag.394212) covers total input-lag measurement and was not read; it may be a useful method reference.

## 7. Web-only route: PWA with DeviceOrientation and WebRTC or WebSocket

### Takeaway
A PWA is technically feasible and has precedent, but the 60 Hz sensor cap in Chromium and the HTTPS requirement against a PC on a LAN make it the weaker product. Use it as a zero-install demo or fallback, and keep the native app as the main route. WebRTC's data channel is the better transport if a browser route is built.

### Cited Findings
- Chromium caps Generic Sensor frequency at 60 Hz, using `min(requested, min(platform max, 60 Hz))` — [Chromium commit](https://crrev.com/9d5b8269d088402d1d9c261d1c12585d81a4ac78); [W3C sensors issue 367](https://www.github.com/w3c/sensors/issues/367) [S]
- Chromium's device motion and orientation event pump targets 60 Hz, chosen to match the typical `requestAnimationFrame` rate, and is limited to the platform maximum when that is lower — [Chromium review 604483003](https://codereview.chromium.org/604483003); [Chromium commit](https://crrev.com/b29ba0d062280291845dd8a5bd9dc052e439bbb5) [S]
- For iOS, the only statement found is an unverified comment that about 60 Hz is what iOS Safari does — [Mozilla bug 1515780](https://bugzilla.mozilla.org/show_bug.cgi?id=1515780) [S]
- Device orientation events are limited to secure contexts in some or all supporting browsers — [MDN](https://developer.mozilla.org/docs/Web/API/Device_orientation_events) [S]
- iOS 13+ requires `DeviceMotionEvent.requestPermission()`, called from a user gesture, and auto-declines when the page is not on HTTPS. Android has no such method (as of the 2020 source) and can register a listener directly — [yal.cc](https://yal.cc/js-device-motion/); [WebKit bug 203287](https://bugs.webkit.org/show_bug.cgi?id=203287) [S]
- Chrome on Android reports the sensor API as unavailable on insecure origins — [device-motions-js](https://www.npmjs.com/package/device-motions-js) [S]
- Chrome's Local Network Access permission applies to public-to-local requests and is restricted to secure contexts. Requests are exempt from mixed-content checks if Chrome knows the target is local, as with a private IP literal, a `.local` name, or `targetAddressSpace: "local"` — [Chrome blog, June 2025](https://developer.chrome.com/blog/local-network-access) [V]
- As of that post, the permission gated `fetch()`, subresources and subframe navigations, while WebSockets, WebTransport and WebRTC were not yet gated and were planned "soon" — [Chrome blog](https://developer.chrome.com/blog/local-network-access) [V]
- Chrome 142 enabled the prompt by default, and Chrome 145 split the permission into `local-network` and `loopback-network`. A third-party source reports that Chrome 156 removes the enterprise opt-out, scheduled for October 2026 — [SimCapture FAQ](https://simcapture.helpjuice.com/en_US/faq-chrome-local-network-access-lna-prompt); [SuperchargeBrowser](https://www.superchargebrowser.com/library/chrome-156-local-network-access/) [S]
- Precedent: GyroBridge on Android and cemu-no-gyro use a browser page as a motion source — [PhoneGyro](https://github.com/MrHoustonOff/PhoneGyro/releases/tag/v1.1.2); [cemu-no-gyro](https://github.com/nostlan/cemu-no-gyro) [S]

### Inferences

| Aspect | PWA | Native Android |
|---|---|---|
| Install friction | **None** | Store install |
| Sensor rate | About 60 Hz cap | 200 Hz without extra permission |
| Mean sampling delay | About 8.3 ms | About 2.5 ms |
| Transport | WebRTC or WSS | Raw UDP |
| Low-latency Wi-Fi lock | **Not available** to a page | Yes |
| Certificate problem | **Yes** | No |
| Permission | iOS gesture prompt, Chrome prompts possibly | `ACCESS_LOCAL_NETWORK` prompt |

- **The certificate problem is the crux.** Sensors need a secure context, so the page must be on HTTPS, but the PC on a LAN has no trusted certificate. There are three routes:
  1. Serve the page from the PC with a self-signed certificate. This gives a scary browser warning, and it is a poor experience.
  2. Host the page on a public HTTPS site and connect to the PC by WebRTC, carrying the signalling (SDP) through a QR code, a relay, or a local HTTP call. The data channel has its own DTLS, so no web PKI certificate is needed for the PC. **This is the best browser design.** Use `ordered: false, maxRetransmits: 0`.
  3. Public site calling a local `ws://` or HTTP endpoint under the LNA mixed-content exemption. This depends on Chrome behaviour, which is still evolving, and may not work in Safari.
- **A public page brings an operational dependency,** since the page must be hosted and any signalling server maintained, though an installed, cached PWA can keep working if signalling stays local.
- **WebSocket should be avoided** for live input, because on loss TCP stalls newer data behind older data, which defeats latest-wins.
- **Expected penalty, ESTIMATE:** about 6 ms more from sampling at 60 Hz rather than 200 Hz, and a few ms from browser scheduling and event-loop delivery, with no Wi-Fi lock and with a higher risk of spikes. Total roughly 10-15 ms over native. Also, a 60 Hz sensor sampling against a 60 fps game can beat visibly, so consider this in testing.
- **Recommended position:** native Android as the main product. A PWA is a valuable zero-install "try it now" and a route to iOS without App Store work, built after the native protocol is settled. The PC side would need a WebRTC stack too (for instance, `webrtc-rs` or `libdatachannel`), which adds meaningful complexity, so it should not be in the MVP.

### Gaps
- Not confirmed: whether LNA now covers WebSockets or WebRTC in November 2026. The official post dates from June 2025, and a third-party source says WebRTC is out of scope. Check the current Chrome status page.
- Not confirmed: iOS Safari sensor rate, or current `webkit` behaviour on WebRTC to LAN peers and mDNS-obfuscated ICE candidates. Test on a device.
- Not found: measured LAN WebRTC data-channel or WebSocket latency from a phone browser.
- Not researched: Screen Wake Lock availability, background tab throttling, and PWA behaviour on Android vs iOS. All three matter for a product that must keep the screen on.
- The Generic Sensor limit is sourced from older Chromium commits, and I did not confirm it is unchanged in 2026 Chromium. Verify with a quick test page logging event intervals on real devices.
