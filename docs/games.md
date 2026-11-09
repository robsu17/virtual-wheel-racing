# Game compatibility

Virtual Wheel Racing shows up in games as **vJoy Device**, a generic DirectInput controller: X axis = steering, Y = throttle, Z = brake, buttons 1 to 16. Any game that lets you map a generic controller should work; games that only accept Xbox controllers or that run kernel anti-cheat may not.

**Tested a game?** Open an issue with the *Compatibilidade de jogo* (game compatibility) template, whether it worked or not. That is how this list grows.

## Status

| Game | Status | Notes |
|---|---|---|
| rFactor | ✅ Validated | No axis inversion needed, no noticeable lag (Poco X8 Pro, Windows 11, vJoy 2.1.9.1) |
| Euro Truck Simulator 2 | 🎯 Main target, untested | Community reports show vJoy working; choose the wheel input type and map steering, throttle and brake |
| American Truck Simulator | 🎯 Main target, untested | Same engine as ETS2 |
| Assetto Corsa | 🎯 Main target, untested | Other phone-to-vJoy projects report it working; manual binding |
| BeamNG.drive | 🎯 Main target, untested | Use the "Direct" steering filter for a wheel-like feel |
| Assetto Corsa Competizione, Le Mans Ultimate, Automobilista 2, rFactor 2, iRacing | ❓ Likely, untested | Accept generic DirectInput devices; check each game's online rules before racing online |
| Forza Motorsport | ❓ Likely, untested | Supports DirectInput devices with manual setup |
| Forza Horizon 5 and 6, GTA V | ⚠️ Out of scope for now | Work best with Xbox controllers; vJoy support is partial or needs extra tools |
| F1 series, EA WRC | ⛔ Out of scope for now | EA's kernel anti-cheat may refuse virtual controller software |

## Tips for every game

- Set the game's **deadzone to 0%**: Virtual Wheel Racing already applies its own.
- Turn **force feedback off** for vJoy Device: the phone cannot push back.
- Use the **setup mode** (*Configurar meu jogo > Abrir modo configuração*): it sends only one control at a time, so the game detects the right one.
- If a pedal acts reversed (pressed when released), use the game's "invert axis" option and tell us in an issue.
- Online and competitive play: virtual controllers are allowed in most sims, but rules vary. Check each game's rules first.
