"""Capture Seasons Phase 4B screens from the API 35 emulator.

Run from the repository root after installing the debug APK. Device viewport,
font scale, orientation, and app language are restored after capture.
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


def call(*args):
    return subprocess.check_output(ADB + list(map(str, args)))


def shell(*args):
    return call("shell", *args).decode().strip()


def tree():
    shell("uiautomator", "dump", "/sdcard/seasons-phase4b-window.xml")
    return ET.fromstring(shell("cat", "/sdcard/seasons-phase4b-window.xml"))


def click(label, attempts=12):
    for _ in range(attempts):
        for node in tree().iter("node"):
            if node.get("text") == label or node.get("content-desc") == label:
                x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds", "")))
                shell("input", "tap", (x1 + x2) // 2, (y1 + y2) // 2)
                time.sleep(.8)
                return
        w, h = dimensions()
        shell("input", "swipe", w // 2, int(h * .82), w // 2, int(h * .38), 400)
        time.sleep(.3)
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
    time.sleep(1.2)


def viewport(size, density="420", scale="1"):
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
    time.sleep(.5)


def chooser(lang):
    start()
    click("Zeit & Jahreszeiten" if lang == "de" else "Time & Seasons")
    click("Tage & Jahreszeiten" if lang == "de" else "Days & Seasons")
    click("Jahreszeiten – Lernen und Üben" if lang == "de" else "Seasons – Learn and practise")


def capture(name, caption):
    (GALLERY / f"{name}.png").write_bytes(call("exec-out", "screencap", "-p"))
    captures.append((name, caption))
    print(name, flush=True)


def restore(key, value):
    shell("settings", "delete" if value == "null" else "put", "system", key, *([] if value == "null" else [value]))


GALLERY.mkdir(parents=True, exist_ok=True)
saved = {key: shell("settings", "get", "system", key)
         for key in ("font_scale", "accelerometer_rotation", "user_rotation")}
saved["size"] = shell("wm", "size")
saved["density"] = shell("wm", "density")
try:
    prefs = ET.fromstring(call("exec-out", "run-as", PACKAGE, "cat", "shared_prefs/basti_shell.xml"))
    old_language = next((node.text for node in prefs if node.get("name") == "lang"), "en")
except (subprocess.CalledProcessError, ET.ParseError):
    old_language = "en"
old_language = "de" if old_language == "de" else "en"

if "--landscape-only" in sys.argv:
    try:
        set_language("de")
        chooser("de")
        click("Jahreszeiten entdecken")
        shell("settings", "put", "system", "accelerometer_rotation", "0")
        viewport("2400x1080", density="480")
        shell("settings", "put", "system", "user_rotation", "1")
        time.sleep(1)
        capture("08-explore-short-landscape", "Explore — short landscape phone")
    finally:
        for key in ("size", "density"):
            match = re.search(r"Override (?:size|density): (.+)", saved[key])
            shell("wm", key, match.group(1) if match else "reset")
        for key in ("font_scale", "accelerometer_rotation", "user_rotation"):
            restore(key, saved[key])
        set_language(old_language)
    raise SystemExit(0)

try:
    shell("settings", "put", "system", "accelerometer_rotation", "0")
    shell("settings", "put", "system", "user_rotation", "0")
    viewport("1080x2400")
    set_language("en")
    chooser("en")
    capture("01-chooser-en-phone", "Seasons chooser — English, phone portrait")

    set_language("de")
    chooser("de")
    capture("02-chooser-de-phone", "Seasons chooser — German, phone portrait")
    viewport("1080x2400", scale="2")
    chooser("de")
    capture("03-chooser-de-2x", "Seasons chooser — German, phone portrait, 2× text")

    viewport("1080x2400")
    set_language("en")
    chooser("en")
    click("Explore the seasons")
    capture("04-explore-en-phone", "Explore — English, phone portrait")
    set_language("de")
    chooser("de")
    click("Jahreszeiten entdecken")
    capture("05-explore-de-phone", "Explore — German, phone portrait")

    back()
    click("Üben")
    capture("06-practice-de-phone", "Practice — German recognition activity")

    viewport("2400x1080", density="480")
    shell("settings", "put", "system", "user_rotation", "1")
    set_language("de")
    chooser("de")
    capture("07-chooser-short-landscape", "Chooser — short landscape phone")
    click("Jahreszeiten entdecken")
    capture("08-explore-short-landscape", "Explore — short landscape phone")

    viewport("1600x2560", density="320")
    shell("settings", "put", "system", "user_rotation", "0")
    set_language("en")
    chooser("en")
    capture("09-chooser-tablet-portrait", "Chooser — tablet portrait")
    click("Explore the seasons")
    capture("10-explore-tablet-portrait", "Explore — tablet portrait")

    viewport("2560x1600", density="320")
    shell("settings", "put", "system", "user_rotation", "1")
    set_language("de")
    chooser("de")
    capture("11-chooser-tablet-landscape", "Chooser — tablet landscape")
    click("Jahreszeiten entdecken")
    capture("12-explore-tablet-landscape", "Explore — tablet landscape")

    viewport("1080x2400")
    set_language("en")
    chooser("en")
    click("⚙ Options")
    click("🇩🇪 Deutsch")
    back()
    capture("13-options-return", "Options language change returns to the Seasons chooser")

    back()
    click("Monate")
    click("Monate den Jahreszeiten zuordnen")
    capture("14-shared-months-seasons", "Existing shared Months/Seasons activity")
finally:
    for key in ("size", "density"):
        match = re.search(r"Override (?:size|density): (.+)", saved[key])
        shell("wm", key, match.group(1) if match else "reset")
    for key in ("font_scale", "accelerometer_rotation", "user_rotation"):
        restore(key, saved[key])
    set_language(old_language)
    figures = "".join(
        f'<figure><a href="gallery/{name}.png"><img src="gallery/{name}.png"></a>'
        f"<figcaption>{html.escape(caption)}</figcaption></figure>"
        for name, caption in captures)
    (ROOT / "index.html").write_text(
        "<!doctype html><meta charset='utf-8'><title>Seasons Phase 4B visual review</title>"
        "<style>body{font:16px system-ui;background:#f2f0ea;color:#253449;margin:24px}"
        "main{display:flex;flex-wrap:wrap;gap:20px}figure{margin:0;width:320px}"
        "img{width:100%;height:560px;object-fit:contain;background:white}"
        "figcaption{padding:10px}</style><h1>Seasons Phase 4B — API 35 emulator review</h1>"
        "<p>Actual MainActivity captures. Physical-device, TalkBack, switch-access, child-usability and offline-voice acceptance remain owner-deferred.</p>"
        f"<main>{figures}</main>")
