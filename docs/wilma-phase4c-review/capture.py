"""Capture Wilma Phase 4C screens from the API 35 Android emulator."""
import html
import re
import subprocess
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
    shell("uiautomator", "dump", "/sdcard/wilma-phase4c-window.xml")
    return ET.fromstring(shell("cat", "/sdcard/wilma-phase4c-window.xml"))


def click(label, attempts=14):
    for _ in range(attempts):
        for node in tree().iter("node"):
            if node.get("text") == label or node.get("content-desc") == label:
                x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds", "")))
                shell("input", "tap", (x1 + x2) // 2, (y1 + y2) // 2)
                time.sleep(.9)
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
    time.sleep(1)


def language(lang):
    start()
    labels = {n.get("text") for n in tree().iter("node")}
    click("Erwachsene" if "Erwachsene" in labels else "Grown-ups")
    click("🇩🇪 Deutsch" if lang == "de" else "🇬🇧 English")
    back()


def chooser(lang):
    start()
    click("Zeit & Jahreszeiten" if lang == "de" else "Time & Seasons")
    click("Tage & Jahreszeiten" if lang == "de" else "Days & Seasons")
    click("Wilmas Woche" if lang == "de" else "Wilma’s Week")


def capture(name, caption):
    (GALLERY / f"{name}.png").write_bytes(call("exec-out", "screencap", "-p"))
    captures.append((name, caption))
    print(name, flush=True)


def set_setting(key, value):
    shell("settings", "delete" if value == "null" else "put", "system", key,
          *([] if value == "null" else [value]))


def landscape():
    shell("settings", "put", "system", "accelerometer_rotation", "0")
    shell("settings", "put", "system", "user_rotation", "1")
    shell("cmd", "window", "user-rotation", "lock", "1")
    time.sleep(2)


def portrait():
    shell("settings", "put", "system", "accelerometer_rotation", "0")
    shell("settings", "put", "system", "user_rotation", "0")
    shell("cmd", "window", "user-rotation", "lock", "0")
    time.sleep(1.5)


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

try:
    portrait()
    viewport("1080x2400")
    language("en"); chooser("en")
    capture("01-chooser-en-phone", "Chooser — English, phone portrait")
    language("de"); chooser("de")
    capture("02-chooser-de-phone", "Chooser — German, phone portrait")
    viewport("1080x2400", scale="2")
    chooser("de")
    capture("03-chooser-de-2x", "Chooser — German, 2× text")

    viewport("1080x2400")
    language("en"); chooser("en"); click("Explore the week")
    capture("04-explore-en-phone", "Explore — English, phone portrait")
    language("de"); chooser("de"); click("Die Woche entdecken")
    capture("05-explore-de-phone", "Explore — German, phone portrait")

    back(); click("Finde den Tag")
    capture("06-practice-de-phone", "Practice — Find the day")
    back()

    chooser("de")
    click("⚙ Optionen"); click("🇬🇧 English"); back()
    capture("11-options-return", "Options language change returns to Wilma chooser")
    language("de"); chooser("de")
    click("Finde den Tag"); back()
    capture("12-practice-back-to-chooser", "Back from Find returns to Wilma chooser")

    viewport("1080x2400", density="420")
    chooser("de"); landscape()
    capture("07-chooser-short-landscape", "Chooser — short landscape phone")
    # Navigate in portrait, then rotate without UIAutomator calls.
    portrait(); chooser("de"); click("Die Woche entdecken"); landscape()
    capture("08-explore-short-landscape", "Explore — short landscape phone")

    portrait(); viewport("1600x2560", density="320")
    language("en"); chooser("en"); capture("09-chooser-tablet-portrait", "Chooser — tablet portrait")
    click("Explore the week"); capture("10-explore-tablet-portrait", "Explore — tablet portrait")
    language("de"); chooser("de"); landscape(); capture("13-chooser-tablet-landscape", "Chooser — tablet landscape")
    portrait(); chooser("de"); click("Die Woche entdecken"); landscape()
    capture("14-explore-tablet-landscape", "Explore — tablet landscape")
finally:
    for key in ("size", "density"):
        match = re.search(r"Override (?:size|density): (.+)", saved[key])
        shell("wm", key, match.group(1) if match else "reset")
    for key in ("font_scale", "accelerometer_rotation", "user_rotation"):
        set_setting(key, saved[key])
    shell("cmd", "window", "user-rotation", "free")
    language(old_language)
    figures = "".join(
        f'<figure><a href="gallery/{name}.png"><img src="gallery/{name}.png"></a>'
        f"<figcaption>{html.escape(caption)}</figcaption></figure>"
        for name, caption in captures)
    (ROOT / "index.html").write_text(
        "<!doctype html><meta charset='utf-8'><title>Wilma Phase 4C visual review</title>"
        "<style>body{font:16px system-ui;background:#f2f0ea;color:#253449;margin:24px}"
        "main{display:flex;flex-wrap:wrap;gap:20px}figure{margin:0;width:320px}"
        "img{width:100%;height:560px;object-fit:contain;background:white}"
        "figcaption{padding:10px}</style><h1>Wilma Phase 4C — API 35 visual review</h1>"
        "<p>Actual MainActivity captures; see REVIEW.md for dimensions, findings and acceptance limits.</p>"
        f"<main>{figures}</main>")
