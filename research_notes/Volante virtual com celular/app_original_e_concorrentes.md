# JD SimWheel and the phone-as-steering-wheel market (as of November 2026)

Evidence labels used throughout:
- **[direct]**: I retrieved the page itself.
- **[snippet]**: seen only through a search-result summary because the page blocked direct fetching. Treat as weaker and re-verify before relying on it.
- Store metrics come from dated third-party trackers or listings, not from the developers' consoles.

Access limits that shape every section below:
- XDA and AppBrain returned HTTP 403.
- apkcombo failed DNS, and apkfab returned 403.
- The Wayback Machine could not be fetched.
- Google Play pages came back empty or truncated, except that the jd.simwheel URL returned a clean 404.
- Reddit is blocked to the search tool, so **no Reddit evidence was collected**.

## What was JD SimWheel, and why did it disappear?

### Takeaway
JD SimWheel (package `jd.simwheel`) was a free Android app, attributed to John Diamantis, that used a Windows server and the vJoy driver to act as a Wi-Fi steering wheel. Its last known version is 4.2 (mid-2015), and one tracker says it left Google Play on 3 January 2016. **No source states why it was removed.** Any explanation is inference.

### Cited Findings
**Identity and status**
- Developer attributed as John Diamantis; removed from Google Play on 3 Jan 2016 [snippet] — [AppBrain](https://www.appbrain.com/app/jd-simwheel-steering-wheel/jd.simwheel)
- The Play URL for `jd.simwheel` returns HTTP 404 today [direct, Nov 2026] — [Google Play](https://play.google.com/store/apps/details?id=jd.simwheel)
- Latest listed version is 4.2, last updated mid-2015; some pages still show a 3.0 build; requires Android 2.3.3+ [snippet] — [APKCombo](https://apkcombo.com/jd-simwheel-steering-wheel/jd.simwheel/), [APKFab](https://apkfab.com/jd-simwheel-steering-wheel/jd.simwheel)
- The developer announced it in an XDA thread titled "[APP][2.3.3+] JD SimWheel virtual steering wheel for PC racing games" [snippet] — [XDA](https://xdaforums.com/t/app-2-3-3-jd-simwheel-virtual-steering-wheel-for-pc-racing-games.2400867/page-3)

**How it worked**
- The app talked to a PC server over home Wi-Fi, and the server fed a virtual Windows game-controller driver. Tilt steered, and forward/back motion handled throttle and brake. Listings target Windows Vista through 10 [snippet] — [APKCombo](https://apkcombo.com/jd-simwheel-steering-wheel/jd.simwheel/)
- The driver was vJoy. Users on Windows 10 reported the server refusing to start because of a vJoy version mismatch, with the app asking for 2.0.5, and a commenter said installing that older version fixed it [snippet] — [AppBrain](https://www.appbrain.com/app/jd-simwheel-steering-wheel/jd.simwheel)
- The PC driver and server were downloaded from the developer's site, given as droidcortex.com in 2014 [direct] — [odd.blog, 23 Jun 2014](https://odd.blog/2014/06/23/turn-your-android-phone-into-a-steering-wheel/). Another summary gives droidcortex.eu [snippet]; that domain did not resolve when tested in Nov 2026.

**Reported strengths and complaints (2014-2016 era)**
- Strengths: ETS2 "works fine", corners can be taken smoothly, and two on-screen buttons come preset for indicators; setup gets you most of the way, with a few minutes of in-game tinkering [direct] — [odd.blog, 2014](https://odd.blog/2014/06/23/turn-your-android-phone-into-a-steering-wheel/)
- Complaint: the same author could not reverse properly [direct] — [odd.blog, 2014](https://odd.blog/2014/06/23/turn-your-android-phone-into-a-steering-wheel/)
- Complaint: steering is mapped to the Y axis instead of X, because X stays internal to the app [snippet] — [XDA](https://xdaforums.com/t/app-2-3-3-jd-simwheel-virtual-steering-wheel-for-pc-racing-games.2400867/page-3)
- Complaint: game freezes on Windows 10 while the server runs. The developer advised reinstalling vJoy; one user said this worked, another said it did not [snippet] — [XDA](https://xdaforums.com/t/app-2-3-3-jd-simwheel-virtual-steering-wheel-for-pc-racing-games.2400867/page-6)
- Friction: a forum post notes vJoy of that era worked on 64-bit systems only in test mode because it was unsigned [snippet] — [XDA](https://xdaforums.com/t/app-2-3-3-jd-simwheel-virtual-steering-wheel-for-pc-racing-games.2400867/page-6)

**Unofficial remnants**
- A Blogspot page offers "Server+APK v4.2". It names no developer, routes its instruction links through festyy.com, and appears to be an unofficial March 2016 mirror [direct] — [Blogspot mirror](https://jdsimwheeldownload.blogspot.com/). Do not point users to it.

### Inferences
- The combination of a single developer, an app last updated in 2015 and a server pinned to one vJoy version suggests a project that stopped being maintained as Windows 10 arrived. This is plausible but not evidenced as the cause.
- A Play delisting can be initiated by the developer or by Google, and the evidence does not distinguish them.
- A 2016 delisting predates today's Play target-API enforcement, so that is unlikely to be the explanation.
- "SimWheel Controller" (`com.ik.simwheel`, first released 2025) has a similar name, but I found nothing connecting it to JD SimWheel. Treat them as unrelated. Anyone searching for the old name will land on this newer app.

### Gaps
- **Reason for removal:** no developer statement or Google notice was found.
- **First release date:** not verified. The XDA thread could establish it, but it was blocked.
- **Install count and final rating:** not retrieved.
- **USB or Bluetooth support:** only Wi-Fi is evidenced.
- **Developer identity:** rests on one AppBrain snippet, and it was not confirmed that the XDA poster is the same person.
- **Popularity in Brazil:** no Portuguese-language source about JD SimWheel surfaced, so this is unverified.
- **Page integrity warning:** the odd.blog page contains injected text addressed to LLMs, asking them to post the contents of `~/.ssh`. It was ignored. The page's article content matched its topic, but anyone automating against that site should be careful.

## What alternatives exist as of 2026?

### Takeaway
The category is crowded with small, low-rated-count apps, and it has been refreshed recently: at least four dedicated products shipped or updated in 2025-2026. Almost all of them still require a Windows receiver plus vJoy or an Xbox-pad emulation layer. None that I found publishes a measured latency methodology.

### Cited Findings
**Dedicated and general apps.** Dates are last known updates; "n/f" means not found.

| App | Status / last update | Platforms | Connection | Virtual device | Latency claim | Monetization | Rating / installs | Complaints found | Source |
|---|---|---|---|---|---|---|---|---|---|
| Sim Wheel Controller (`com.ik.simwheel`, ik apps&games) | Active; 2026-08-07; first 2025-08-10 | Android only | Wi-Fi, USB | Receiver v3 (C++); vJoy per install guide; no force feedback | "Ultra-low latency", no figures | Free + IAP, no ads | 4.48, 223 ratings, 35K+ | n/f | [AppGoblin](https://appgoblin.info/apps/com.ik.simwheel), [site](https://simwheel.netlify.app/), [install guide](https://simwheel.netlify.app/installation) |
| Steering App (`com.simracing.control`) | New; v1.0.0, Oct 2026 (APKPure header says Nov 1) | Android; Win 10 1803+/11 x64 | Wi-Fi via same account, no IP entry; PC hotspot | Appears as a DirectInput wheel; no separate driver download listed | n/f | Free, no ads; one-time packs (controls, dashboards, wheels) | No rating yet | None yet | [APKPure](https://apkpure.com/steering-app/com.simracing.control), [Play](https://play.google.com/store/apps/details?id=com.simracing.control) |
| Touch Racer (`ru.thelv.joy`, thelv) | Maintained; 2025-10-27; first 2016-12-02 | Android | Wi-Fi with auto-connect; wired also claimed | Emulates a joystick; driver not named | n/f | Free, no ads or IAP | 4.15, 872 ratings, 114K+ | A user asked for forward/back tilt [snippet] | [AppGoblin](https://appgoblin.info/apps/ru.thelv.joy), [BlueStacks page](https://www.bluestacks.com/apps/tools/touch-racer-racing-wheel-on-pc.html), [Play](https://play.google.com/store/apps/details?id=ru.thelv.joy&hl=en) |
| Steering Wheel for PC (`com.mssalt.gamingwheel`, mssalt) | Stale; 2023-11-20; first 2022-10-19 | Android | n/f | n/f | n/f | Free, no ads or IAP | 3.51, 194 ratings, 147K+ | n/f | [AppGoblin](https://appgoblin.info/apps/com.mssalt.gamingwheel) |
| Virtual Steering Controller / VSteer (Agastya Raj C) | Active; v3.5, Sep 15 (year not shown, app ID suggests recent) | iOS; Windows receiver | LAN Wi-Fi, no account | "Sim Wheel Emulation (Direct input)", plus gamepad and mouse modes | "Low latency", no figures | Free + ad "fuel"; $1 Full Tank, $9.99 Premium | 4.1, 8 ratings | Ad-fuel system; stuck at "No ads in my region" | [App Store](https://apps.apple.com/app/id6757863011) |
| RojX (Rahel Ali) | Active; v1.0.28, Oct 10 (year not shown) | iOS; Windows receiver | Local Wi-Fi UDP, QR pairing | Xbox 360 emulation or vJoy | n/f | Listed free, no IAP listed | 3.0, 2 ratings | Buttons stopped after 3 days; refund refused | [App Store](https://apps.apple.com/app/id6783482537) |
| simSteer (itsayushk19) | In development | Android/web client; Python PC server on Windows | LAN Wi-Fi or USB tethering; Bluetooth planned | vJoy (X, Y, Z, RX axes, 24 buttons, no force feedback) | "< 10 ms", no method given | Not stated | n/f | Page contradicts itself on gyro support | [Site](https://simsteer.nexs.works/) |
| Remote Gamepad | Active; date n/f | Android (site); App Store listing also exists; PC side Win/Linux/Mac | Wi-Fi, USB, Bluetooth | Not stated | None | Time-limited trial; ads or premium | n/f | Paid no-ads version not working; QR connection fails; USB praised for low latency [snippet] | [Site](https://remotegamepad.com/), [App Store](https://apps.apple.com/ca/app/remote-gamepad/id6448859108) |
| Monect | Date n/f (article estimated April 2026) | Android; Windows 8+ | Local network, manual IP | Server converts to keyboard, mouse or controller | None | Ad mechanisms in installer | n/f | Installer flagged by browsers and security suites | [Androidayuda](https://en.androidayuda.com/games/Tutorials/How-to-use-your-mobile-phone-as-a-gyro-steering-wheel-controller-on-PC/) |
| HandyGamePad | Date n/f | Android; Windows | Wi-Fi, USB | Installer bundles its own virtual-controller drivers | None | Free 10 min (20 with ad); Pro about €3 | n/f | Session limit | [Androidayuda](https://en.androidayuda.com/games/Tutorials/How-to-use-your-mobile-phone-as-a-gyro-steering-wheel-controller-on-PC/) |
| Steering Wheel Emulator (MrSomeBody, itch.io) | Date n/f | Windows, Android | Client/server | n/f | n/f | n/f | n/f | n/f | [itch.io](https://mrsomebody02.itch.io/steering-wheel-emulator) [snippet] |
| Truck Remote Control (ETS2 mod) | Date n/f | Android | n/f | n/f | n/f | n/f | n/f | n/f | [Modland](https://www.modland.net/euro-truck-simulator-2/other/truck-remote-control-android-device-steering-solution.html) [snippet] |

**Named only in guides, status unverified**
- Virtual Wheel, Max Remote, WiDroid, Touch Racer and DroidJoy are listed by a how-to guide, which says most need a Windows receiver [snippet] — [Androidguias](https://en.androidguias.com/How-to-use-your-mobile-phone-as-a-steering-wheel-for-racing-games-on-PC/)
- Tilt Racer also appears in a news post, which I did not open — [DroidGamers](https://www.droidgamers.com/news/tilt-racer-turns-your-phone-into-a-steering-wheel-for-pc-racing-games/)

**Adjacent, not a wheel**
- Fergo ETS2 Virtual Button Box is a virtual button box for ETS2 and shows that demand exists for phone or tablet controls beyond steering [snippet] — [Fergonez](https://www.fergonez.net/softwares/fergo-ets2-vbb)

**Driver layer, which every product depends on**
- ViGEmBus is retired and archived. A trademark conflict found in May 2023 ended the name, and the domain transfer finished on 26 Dec 2023. Existing installs still work, but old auto-updaters need adjusting, and no direct successor is named [direct] — [Nefarius EOL notice](https://docs.nefarius.at/projects/ViGEm/End-of-Life/). The repo is archived with 4,225 stars, BSD-3-Clause, last push 2023-11-02 — [GitHub API](https://github.com/nefarius/ViGEmBus)
- vJoy is fragmented across forks: shauleiz (743 stars, last push 2023-08), njz3 (379 stars, 2024-10), BrunnerInnovation (228 stars, 2024-06), all MIT — [shauleiz](https://github.com/shauleiz/vJoy), [njz3](https://github.com/njz3/vJoy), [Brunner](https://github.com/BrunnerInnovation/vJoy)
- vJoy 2.2.1 has been reported not loading on Windows 11 [snippet] — [njz3 issue 11](https://github.com/njz3/vJoy/issues/11)
- One user reported Forza Horizon 5 crashing at startup with the vJoy driver installed [snippet] — [shauleiz issues](https://www.github.com/shauleiz/vJoy/issues/57)
- A lower-reliability blog recommends the signed 2.2.2.0 Brunner build [snippet] — [Geekchamp](https://geekchamp.com/?p=1057175)

### Inferences
- A wheel in this category needs a realistic driver plan. Both common choices carry risk: vJoy has no single maintained line and has Windows 11 reports, and ViGEmBus is frozen. Commercial projects depending on either inherit that risk, and it echoes what JD SimWheel hit with Windows 10.
- Steering App, VSteer and HandyGamePad seem to be moving to an install experience without a manual vJoy step. How they do it, including whether they ship their own signed driver, is unverified. Inspecting their installers would be the fastest way to learn.
- iOS looks thinly served: the iOS-capable apps I found have 2 and 8 ratings, plus Remote Gamepad. iOS demand is unmeasured, though, so this could be an empty niche rather than an open one.

### Gaps
- **Named apps with no usable data:** PC Racing Wheel, DroidJoy, WheelController, VirtuaWheel and UWheel. Nothing verifiable was retrieved, which does not prove they are dead.
- **DSU/Cemuhook motion tools:** not researched to a usable standard. They typically serve emulators, so relevance to PC truck games is unconfirmed.
- **Full Play review text:** not retrievable for any app, so "complaints found" columns understate reality.
- **Year on App Store update dates:** not displayed on the pages retrieved.
- **Steering App Windows driver mechanism:** not disclosed on the pages retrieved.

## What open source projects can serve as references?

### Takeaway
There are many repos but little traction: the best-known dedicated projects have about 110 and 51 stars, and both are inactive. **DriveLink** (MIT, C++/Kotlin, active) is the cleanest current reference, **AndroidSteering** (MIT) the best older reference, and **PhoneWheel by MiguelM-bit** a useful protocol design to read, though it has no license.

### Cited Findings
Metadata comes from the GitHub API, queried 6 Nov 2026. "Last push" is the `pushed_at` value, which can differ from the last code commit.

| Repo | Stars | Last push | Language | License | Architecture | Link |
|---|---|---|---|---|---|---|
| totoneei/Engine (formerly whitescent) | 110 | 2023-09-26; archived 25 Nov 2025 | Kotlin | GPL-3.0 | Compose Android app + desktop MSI, vJoy, manual IPv4, widget editor, volume keys as buttons; zh/en only | [GitHub](https://github.com/totoneei/Engine) |
| teamclouday/AndroidSteering | 51 | 2022-11-12 | C# | MIT | WPF + vJoy, Bluetooth or LAN with manual IP; tilt steering and pedals; force feedback only a plan | [GitHub](https://github.com/teamclouday/AndroidSteering) |
| imkh778/SimWheeel_Receiver_ | 10 | 2026-06-19 | C++ | GPL-3.0 | PC receiver of the commercial SimWheel Controller; the phone app is closed source | [GitHub](https://github.com/imkh778/SimWheeel_Receiver_) |
| itsayushk19/drivingRemote | 7 | 2026-02-14 | n/c | n/c | Probably simSteer's code; the site names the account, but this repo match is my inference | [GitHub](https://github.com/itsayushk19/drivingRemote) |
| DevBoiAgru/DriveLinkDesktop | 6 | 2026-10-29 | C++20 / SFML | MIT | UDP port 7001 to vJoy on Windows; Linux too; 3 pedal axes; force feedback planned | [GitHub](https://github.com/DevBoiAgru/DriveLinkDesktop) |
| DevBoiAgru/DriveLinkMobile | 4 | 2026-04-09 | Kotlin | MIT | Android client; about 100 Hz UDP claimed [snippet] | [GitHub](https://github.com/DevBoiAgru/DriveLinkMobile) |
| danielwitkowsk/DroidWheel_releases | 5 | 2021-10-14 | none | none | Releases repo, source apparently absent; ViGEmBus [snippet] | [GitHub](https://github.com/danielwitkowsk/DroidWheel_releases) |
| stylpe/tmphonepad | 4 | 2024-03-13 | C# | MIT | ViGEm client for TrackMania | [GitHub](https://github.com/stylpe/tmphonepad) |
| KINN-CH/PhoneWheel | 2 | 2026-09-25 | Python | MIT | Tilt, analog pedals, paddles; aimed at Forza Horizon 5 | [GitHub](https://github.com/KINN-CH/PhoneWheel) |
| MiguelM-bit/PhoneWheel | 0 | 2026-09-28 | Kotlin + C# | none detected | JSON over UDP port 5005, broadcast discovery, about 50 ms send interval, 500 ms watchdog that centers and releases, vJoy or ViGEm auto-detected; in-game testing pending | [GitHub](https://github.com/MiguelM-bit/PhoneWheel) |
| ashus-soul/Phone-Gyro-Steering-Wheel | 0 | 2026-11-01 | Python | none detected | Third-party HyperIMU stream to vgamepad and ViGEmBus | [GitHub](https://github.com/ashus-soul/Phone-Gyro-Steering-Wheel) |
| BLCXD/virtual-steering-wheel (fork of n1xsoph1c) | 0 | 2026-10-13 (upstream 2025-01-31) | Python / JS | GPL-3.0 | Phone browser gyro via local web server, ViGEmBus | [GitHub](https://github.com/BLCXD/virtual-steering-wheel) |
| hmongcodequest/steering-ets2-controller-android | 0 | 2026-07-03 | none detected | none detected | ETS2 client and button box, Wi-Fi or USB, vJoy | [GitHub](https://github.com/hmongcodequest/steering-ets2-controller-android) |
| ezunic1/GameFlex-Controller | 0 | 2025-09-23 | Kotlin | none detected | Android + Pi Pico joystick + Python MQTT bridge + vJoy | [GitHub](https://github.com/ezunic1/GameFlex-Controller) |

Details worth carrying over:
- AndroidSteering's README says most games stabilize the steering axis, which can make steering feel laggy or too fast or slow, so it ships in-game tuning notes — [GitHub](https://github.com/teamclouday/AndroidSteering)
- A related README estimates about 1-3 ms network latency over USB-C tethering, plus some driver overhead. This is an estimate from the project, and the search summary did not clearly identify which repo it belongs to [snippet] — [Gitee mirror](https://gitee.com/BlueBuger/AndroidSteering)

### Inferences
- **License safety for a closed or commercial build:** MIT repos (DriveLink, AndroidSteering, tmphonepad, KINN-CH PhoneWheel, vJoy) can be reused with attribution. GPL-3.0 repos (Engine, SimWheel receiver, virtual-steering-wheel) would impose copyleft on derived distributions. Repos with no license should be read but not copied.
- **Design lessons:** the watchdog that centers the wheel and releases buttons on signal loss is a safety behavior worth copying. A 50 ms interval (20 Hz) looks too slow for a wheel, whereas DriveLink's claimed 100 Hz is a better baseline. Plain JSON also seems heavier than needed; a compact binary packet would be cheaper.
- A cluster of new repos in 2025-2026 suggests that developers keep rebuilding this tool. That is modest evidence of unmet need, and also a sign that a basic version is now easy to build, so it is not a defensible advantage on its own.

### Gaps
- Licenses, stars and dates are from the API, but I did not audit code quality. I also did not run any of these projects, so no latency or stability claim has been tested.
- The GitHub search API returned few hits, so this list is not exhaustive. Gitee and Chinese-language projects were barely covered.

## What do users complain about and ask for?

### Takeaway
The available evidence points to driver and setup trouble, aggressive monetization, connection failures and steering feel. **This evidence is thin and skews toward store reviews and READMEs**, because Reddit, Play review text, SCS forums and RaceDepartment could not be read. Treat these themes as hypotheses to validate.

### Cited Findings
**Driver and setup friction**
- Windows 10 vJoy mismatch stopped JD SimWheel's server from starting [snippet] — [AppBrain](https://www.appbrain.com/app/jd-simwheel-steering-wheel/jd.simwheel)
- vJoy setup needs manual configuration (axes, 24-32 buttons, force feedback off) — [simSteer](https://simsteer.nexs.works/), [SimWheel install guide](https://simwheel.netlify.app/installation)
- Connection failures are often caused by the firewall, and Steam or another program capturing the controller can interfere — [AndroidSteering](https://github.com/teamclouday/AndroidSteering). DriveLink likewise requires opening UDP port 7001 — [DriveLink](https://github.com/DevBoiAgru/DriveLinkDesktop)

**Monetization and trust**
- Complaint about VSteer's ad-based "fuel", with a user stuck at "No ads in my region" — [App Store](https://apps.apple.com/app/id6757863011)
- RojX reviewer: buttons stopped after three days and a refund was refused — [App Store](https://apps.apple.com/app/id6783482537)
- Remote Gamepad reviewer: the paid no-ads version does not work; another reports QR connection failure [snippet] — [App Store](https://apps.apple.com/ca/app/remote-gamepad/id6448859108)
- Monect's installer gets flagged because of ad mechanisms; HandyGamePad's free tier stops at 10 minutes — [Androidayuda](https://en.androidayuda.com/games/Tutorials/How-to-use-your-mobile-phone-as-a-gyro-steering-wheel-controller-on-PC/)
- Fake or malicious apps are a known concern in this search space: an ETS2 forum user warned that the Play Store holds copies and possibly data-stealing apps, in a thread about phone versions of the game, not wheel apps (Feb 2023) — [Steam](https://steamcommunity.com/app/227300/discussions/0/3775742015025265764)

**Steering feel and control**
- Reversing was difficult (2014) — [odd.blog](https://odd.blog/2014/06/23/turn-your-android-phone-into-a-steering-wheel/)
- Request for forward/back tilt in Touch Racer [snippet] — [Play](https://play.google.com/store/apps/details?id=ru.thelv.joy&hl=en)
- A physical controller remains more ergonomic, and touch buttons struggle with quick combinations — [Androidayuda](https://en.androidayuda.com/games/Tutorials/How-to-use-your-mobile-phone-as-a-gyro-steering-wheel-controller-on-PC/)
- ETS2 steering tuning is a problem even with real wheels: a Logitech G29 thread reports non-uniform turning. One user says trucks turn about 1440-1800° and that non-linearity is intentional, suggesting a matching rotation setting (2023-2024, user claims) — [Steam](https://steamcommunity.com/app/227300/discussions/0/6644556627480705723)
- A related Italian thread reports a wheel oscillating on its own, traced to the centering setting; replies suggest the input wizard and disabling lane assist (Jun 2024, real wheel) — [Steam](https://steamcommunity.com/app/227300/discussions/0/4416424435091105843)

**Feature asks visible in roadmaps and changelogs**
- Force feedback is planned or absent in SimWheel Controller, DriveLink and AndroidSteering — [SimWheel listing](https://apkcombo.com/sim-wheel-controller-for-pc/com.ik.simwheel/) [snippet], [DriveLink](https://github.com/DevBoiAgru/DriveLinkDesktop), [AndroidSteering](https://github.com/teamclouday/AndroidSteering)
- VSteer has added haptics "for supported games", gyro range up to 4000° lock-to-lock and direct-input wheel emulation — [App Store](https://apps.apple.com/app/id6757863011)
- simSteer lists H-shifter, Bluetooth and configurable 900°/1800° rotation — [simSteer](https://simsteer.nexs.works/)
- RojX lists truck-relevant controls: diff locks, lift axle, cruise control, engine start, lights, horn — [App Store](https://apps.apple.com/app/id6783482537)

**Portuguese-language signal**
- A Brazilian App Store review of a mobile truck game (not a wheel app) repeatedly asks for Brazilian Portuguese, which is only a weak signal of language demand among truck-game players — [Universal Truck Simulator](https://apps.apple.com/BR/app/id1632742662)

### Inferences
- Trust is a real weak point: a clean installer, no time-gated core, honest refunds and no ad "fuel" could differentiate cheaply.
- Setup is probably the largest first-session failure: firewall, driver install, driver configuration and in-game mapping must all succeed before the first turn of the wheel.
- Latency complaints may be overstated or understated here; I found no independent measurements or direct user reports with numbers.

### Gaps
- **Reddit (r/simracing, r/trucksim, r/F1Game):** inaccessible, so nothing is reported. Check manually before committing.
- **SCS forum and RaceDepartment/Overtake:** no relevant thread was retrieved. The final search restricted to these domains failed because Reddit was in the list and was not rerun.
- **Play Store reviews:** page text was not retrievable. Tools such as a logged-in browser would help.
- **Brazilian forums and YouTube:** Portuguese searches surfaced no real discussion on this topic. YouTube comments were not read.

## What market gap exists, and how big is the demand evidence?

### Takeaway
Demand is real but small and proven mainly by installs, not by revenue: the three Android apps with tracker data total about 296K cumulative installs. The clearest gap is a **reliable, no-fuss, honestly measured, truck-sim-aware wheel**. Expect a niche or portfolio-sized outcome unless validation shows otherwise.

### Cited Findings
- Installs: Steering Wheel for PC 147K+, Touch Racer 114K+, Sim Wheel Controller 35K+ — [AppGoblin mssalt](https://appgoblin.info/apps/com.mssalt.gamingwheel), [AppGoblin Touch Racer](https://appgoblin.info/apps/ru.thelv.joy), [AppGoblin SimWheel](https://appgoblin.info/apps/com.ik.simwheel)
- Rating counts are low relative to installs: 194, 872 and 223 respectively — same sources
- Steering Wheel for PC kept growing with no update since Nov 2023: a search snapshot showed about 103.6K installs and 155 ratings, and a later fetch showed 147K+ and 194 [snippet for the earlier figure; snapshot dates unknown] — [AppGoblin trends](https://appgoblin.info/apps/com.mssalt.gamingwheel/trends)
- Sim Wheel Controller reached 35K+ installs roughly a year after its first release in Aug 2025 — [AppGoblin](https://appgoblin.info/apps/com.ik.simwheel)
- Revenue data is not available for the apps checked — [AppGoblin](https://appgoblin.info/apps/ru.thelv.joy)
- A new commercial entrant launched as recently as Oct 2026 with a one-time-pack model — [APKPure](https://apkpure.com/steering-app/com.simracing.control)
- Only one product I found gives a numeric latency claim ("< 10 ms"), and it gives no method — [simSteer](https://simsteer.nexs.works/). Others say "ultra-low" or "near-instantaneous" — [SimWheel](https://simwheel.netlify.app/), [DriveLink](https://github.com/DevBoiAgru/DriveLinkDesktop)

### Inferences
Gap candidates, strongest first. All of these are my judgment from the evidence above:

1. **Install reliability.** One signed installer, no manual vJoy configuration, automatic discovery, automatic firewall handling and a built-in test screen. This addresses the failure that hurt JD SimWheel and persists today. Steering App appears to target this already, so the window may be narrowing.
2. **Published latency.** A repeatable end-to-end measurement (Wi-Fi vs USB tethering) would be unique among what I found and would make for a credible marketing story.
3. **Truck-sim depth.** ETS2/ATS presets (1440-1800° rotation, tuned non-linearity, ready-made button pages for lights, retarder, diff lock and so on) and auto-mapping of controls. Only RojX and simSteer show partial moves, and neither has visible traction.
4. **Feel without force feedback.** Haptics driven by game telemetry. ETS2/ATS provide a telemetry SDK, but that was not researched here, so it needs confirming. VSteer's "supported games" haptics show the idea is viable, and most rivals lack it or list it as future work.
5. **Fair pricing.** A one-time unlock with a usable free tier, avoiding time limits and ad "fuel".
6. **PT-BR first and Android-first.** Reasonable for a Brazilian developer with a price-sensitive local audience. No competitor I checked advertises PT-BR, but I did not check every listing, and demand here is unmeasured.
7. **iOS.** Thinly served, but unproven; consider it after Android.

Caution on sizing:
- 296K is a sum of cumulative installs across apps, with overlap, not active users.
- Low rating counts suggest many installs may not convert into lasting use, although this is not measured.
- Several free apps anchor the price near zero. A solo developer should expect modest revenue and validate willingness to pay before heavy investment.

### Gaps
- No revenue, retention, or active-user figures exist for any competitor.
- No search-volume data was collected, whether for Portuguese or English terms.
- No estimate of how many ETS2/ATS players lack a physical wheel was found.
- Whether cheap physical wheels in Brazil undercut this demand was not researched.
- Quality of existing apps is unknown first-hand, since none was installed or tested. A hands-on test of Sim Wheel Controller, Steering App and Touch Racer is the most valuable next step.
