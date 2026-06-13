# Lockscreen PIN Entry (q25pininput)

A tiny Android app that lets you type your **lockscreen PIN on a physical keyboard** — for BlackBerry-style QWERTY devices where the stock lockscreen ignores the hardware keys.

It listens for hardware key presses via an accessibility service and taps the corresponding buttons on the SystemUI PIN pad.

> Built and tested on a BlackBerry physical-keyboard device on LineageOS (Android 14/15). The digit mapping matches a BlackBerry Q20-style layout; adjust `keyCodeToDigit()` for other keyboards.

> ⚠️ **Disclaimer:** This app was "vibecoded" — essentially all of the code was written by **Claude Opus 4.8** through conversational prompting. It has been **tested on-device and works**, but the **code has not been reviewed or audited**. Use at your own risk; review the source before installing if that matters to you.

## What it does

- On the lockscreen, maps physical-keyboard presses to the SystemUI PIN pad.
- Digits map phone-dialpad style onto QWERTY: `W E R = 1 2 3`, `S D F = 4 5 6`, `Z X C = 7 8 9`, `Q = 0`. Standard digit and numpad keys also work.
- **Enter** confirms, **Backspace/Delete** deletes.
- Only acts while the device is locked.

**No root required** — it uses the accessibility service only (key-event filtering + clicking PIN-pad nodes).

> Looking for more KEY2 tweaks (nav-button lock, audio EQ)? See the companion project **[key2-tweaks](https://github.com/nozerorma/key2-tweaks)**, which also includes this PIN feature.

## Build

No Gradle — a raw `aapt2` / `d8` / `zipalign` / `apksigner` pipeline:

```bash
./build.sh        # outputs lockscreenpin.apk
```

Edit the `SDK`, `BT`, `PLATFORM` and `KS` (keystore) paths at the top of `build.sh` for your environment. The signing keystore is **not** included in this repo — supply your own.

## Install & enable

```bash
adb install -r lockscreenpin.apk
```

Then open the app, tap **Open Accessibility Settings**, and enable **Lockscreen PIN Entry**. (Reinstalling resets this — re-enable after each install.)

## License

[MIT](LICENSE)
