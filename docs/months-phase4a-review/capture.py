"""Capture the Phase 4A Months review gallery on the API 35 AVD.

Run from the repository root after installing the debug APK. The script saves
the emulator display/font settings and restores them even when a capture fails.
"""
import html
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent
GALLERY = ROOT / "gallery"
ADB = ["/Users/paulbell/Library/Android/sdk/platform-tools/adb", "-s", "emulator-5554"]
PACKAGE = "com.bellfamily.bastischool"
captures = []
RECAPTURE_PRIMARY_STATES = True
LANDSCAPE_ONLY = "--landscape-only" in sys.argv


def call(*args):
    return subprocess.check_output(ADB + list(map(str, args)))


def shell(*args):
    return call("shell", *args).decode().strip()


def tree():
    shell("uiautomator", "dump", "/sdcard/months-phase4a-window.xml")
    return ET.fromstring(shell("cat", "/sdcard/months-phase4a-window.xml"))


def click(label, attempts=10):
    for _ in range(attempts):
        for node in tree().iter("node"):
            if node.get("text") == label or node.get("content-desc") == label:
                x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds", "")))
                shell("input", "tap", (x1 + x2) // 2, (y1 + y2) // 2)
                time.sleep(.8)
                return
        w, h = dimensions()
        shell("input", "swipe", w // 2, int(h * .82), w // 2, int(h * .36), 400)
        time.sleep(.4)
    raise RuntimeError(f"Cannot find visible label: {label}")


def dimensions():
    values = re.findall(r"(\d+)x(\d+)", shell("wm", "size"))
    return tuple(map(int, values[-1]))


def back():
    shell("input", "keyevent", "4")
    time.sleep(.8)


def start():
    shell("am", "force-stop", PACKAGE)
    shell("am", "start", "-W", "-n", PACKAGE + "/.MainActivity")
    time.sleep(1.3)


def set_viewport(size, density="480", scale="1"):
    shell("wm", "size", size)
    shell("wm", "density", density)
    shell("settings", "put", "system", "font_scale", scale)
    time.sleep(.8)


def set_language(lang):
    start()
    labels = {n.get("text") for n in tree().iter("node")}
    click("Erwachsene" if "Erwachsene" in labels else "Grown-ups")
    click("🇩🇪 Deutsch" if lang == "de" else "🇬🇧 English")
    back()
    time.sleep(.8)


def chooser(lang):
    start()
    click("Alle Aktivitäten" if lang == "de" else "All Activities")
    click("Tage & Jahreszeiten" if lang == "de" else "Days & Seasons")
    click("Monate" if lang == "de" else "Months")
    time.sleep(.5)


def capture(name, description):
    (GALLERY / f"{name}.png").write_bytes(call("exec-out", "screencap", "-p"))
    captures.append((name, description))
    print(name, flush=True)


def restore_setting(name, value):
    if value == "null":
        shell("settings", "delete", "system", name)
    else:
        shell("settings", "put", "system", name, value)


GALLERY.mkdir(parents=True, exist_ok=True)
saved = {key: shell("settings", "get", "system", key)
         for key in ("font_scale", "accelerometer_rotation", "user_rotation")}
saved["size"] = shell("wm", "size")
saved["density"] = shell("wm", "density")
try:
    prefs = ET.fromstring(call("exec-out", "run-as", PACKAGE, "cat", "shared_prefs/basti_shell.xml"))
    old_language = next((n.text for n in prefs if n.get("name") == "lang"), "en")
except (subprocess.CalledProcessError, ET.ParseError):
    old_language = "en"
old_language = "de" if old_language == "de" else "en"

try:
    shell("settings", "put", "system", "accelerometer_rotation", "0")
    shell("settings", "put", "system", "user_rotation", "0")

    if LANDSCAPE_ONLY:
        set_viewport("2400x1080")
        set_language("de")
        chooser("de")
        click("Das Jahresrad entdecken")
        capture("06-explore-phone-landscape", "German Year Wheel — short phone landscape")

    if RECAPTURE_PRIMARY_STATES and not LANDSCAPE_ONLY:
        set_viewport("1080x2400")
        set_language("en")
        chooser("en")
        capture("01-chooser-en", "English Months chooser — phone portrait, 1×")

        set_language("de")
        chooser("de")
        capture("02-chooser-de", "German Months chooser — phone portrait, 1×")

        set_viewport("1080x2400", scale="2")
        chooser("de")
        capture("03-chooser-de-2x", "German Months chooser — phone portrait, 2× text")

        set_viewport("1080x2400", scale="1")
        set_language("en")
        chooser("en")
        click("Explore the Year Wheel")
        capture("04-explore-en", "English content-first Year Wheel — phone portrait")

        set_language("de")
        chooser("de")
        click("Das Jahresrad entdecken")
        capture("05-explore-de", "German content-first Year Wheel — phone portrait")

        set_viewport("2400x1080")
        chooser("de")
        click("Das Jahresrad entdecken")
        capture("06-explore-phone-landscape", "German Year Wheel — short phone landscape")

    if not LANDSCAPE_ONLY:
        set_viewport("1600x2560", density="320")
        set_language("en")
        chooser("en")
        click("Explore the Year Wheel")
        capture("07-explore-tablet-portrait", "English Year Wheel — tablet portrait")

        set_viewport("2560x1600", density="320")
        set_language("de")
        chooser("de")
        click("Das Jahresrad entdecken")
        capture("08-explore-tablet-landscape", "German Year Wheel — tablet landscape")

        set_viewport("1080x2400", density="480")
        set_language("en")
        chooser("en")
        click("Between")
        capture("09-between-question", "Between question with contextual learning aid — English")
        click("Learn with the year wheel")
        capture("10-between-contextual-wheel", "Between contextual Year Wheel with return-to-question control")

        back()
        back()
        chooser("en")
        click("Build the Year")
        capture("11-build-year-levels", "Build the Year three-/four-month level selection")

        back()
        chooser("en")
        click("Seasons & Months Wheel")
        capture("12-seasons-wheel-entry", "Seasons Wheel Explore/Match entry and access controls")
        click("Explore")
        capture("13-seasons-wheel-explore", "Seasons Wheel Explore mode")
        click("Match")
        capture("13-seasons-wheel-match", "Seasons Wheel Match mode")
finally:
    for key in ("size", "density"):
        match = re.search(r"Override (?:size|density): (.+)", saved[key])
        shell("wm", key, match.group(1) if match else "reset")
    for key in ("font_scale", "accelerometer_rotation", "user_rotation"):
        restore_setting(key, saved[key])
    set_language(old_language)
    if not LANDSCAPE_ONLY:
        figures = "".join(
            f'<figure><a href="gallery/{name}.png"><img src="gallery/{name}.png"></a>'
            f"<figcaption>{html.escape(description)}</figcaption></figure>"
            for name, description in captures
        )
        (ROOT / "index.html").write_text(
            "<!doctype html><meta charset='utf-8'><title>Months Phase 4A visual review</title>"
            "<style>body{font:16px system-ui;background:#f2f0ea;color:#253449;margin:24px}"
            "main{display:flex;flex-wrap:wrap;gap:20px}figure{margin:0;width:320px}"
            "img{width:100%;height:560px;object-fit:contain;background:white}"
            "figcaption{padding:10px}</style><h1>Months Phase 4A — API 35 emulator review</h1>"
            "<p>Actual MainActivity screenshots. Physical-device, TalkBack and switch-access acceptance remain deferred.</p>"
            f"<main>{figures}</main>"
        )
