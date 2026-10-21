"""Capture Vocabulary Phase 4D production routes on the API 35 emulator."""
import html
import re
import struct
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
    shell("uiautomator", "dump", "/sdcard/vocabulary-phase4d-window.xml")
    return ET.fromstring(shell("cat", "/sdcard/vocabulary-phase4d-window.xml"))


def click(label, attempts=16):
    for _ in range(attempts):
        for node in tree().iter("node"):
            if node.get("text") == label or node.get("content-desc") == label:
                x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds", "")))
                shell("input", "tap", (x1 + x2) // 2, (y1 + y2) // 2)
                time.sleep(.8)
                return
        w, h = dimensions()
        shell("input", "swipe", w // 2, int(h * .82), w // 2, int(h * .36), 400)
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


def rotate(orientation):
    rotation = "0" if orientation == "portrait" else "1"
    shell("settings", "put", "system", "accelerometer_rotation", "0")
    shell("settings", "put", "system", "user_rotation", rotation)
    shell("cmd", "window", "user-rotation", "lock", rotation)
    time.sleep(1.5)


def language(lang):
    global current_language
    start()
    click("Erwachsene" if current_language == "de" else "Grown-ups")
    click("🇩🇪 Deutsch" if lang == "de" else "🇬🇧 English")
    back()
    current_language = lang


def chooser(lang):
    if current_language != lang:
        language(lang)
    start()
    click("Wörter & Erzählen" if lang == "de" else "Words & Talking")
    click("Wortschatz" if lang == "de" else "Vocabulary Booster")
    time.sleep(1)


def open_explore(lang):
    click("Wörter entdecken" if lang == "de" else "Explore words")
    time.sleep(1.5)


def capture(name, caption, expected):
    data = call("exec-out", "screencap", "-p")
    width, height = struct.unpack(">II", data[16:24])
    if (width, height) != expected:
        raise RuntimeError(f"{name}: got {width}x{height}, expected {expected[0]}x{expected[1]}")
    (GALLERY / f"{name}.png").write_bytes(data)
    captures.append((name, caption, width, height))
    print(f"{name}: {width}x{height}", flush=True)


def mode_and_back(mode, label):
    click("Finde das Wort" if mode == "find" and label == "de" else
          "Find the word" if mode == "find" else
          "Was ist das?" if label == "de" else "What is it?")
    time.sleep(1.2)


def restore(name, value):
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
current_language = old_language

try:
    rotate("portrait")
    viewport("1080x2400")
    language("en"); chooser("en")
    capture("01-chooser-en-phone", "Chooser — English, phone", (1080, 2400))
    language("de"); chooser("de")
    capture("02-chooser-de-phone", "Chooser — German, phone", (1080, 2400))

    viewport("1080x2400", scale="2")
    chooser("de")
    capture("03-chooser-de-2x", "Chooser — German, 2× text", (1080, 2400))

    viewport("1080x2400")
    language("en"); chooser("en"); open_explore("en")
    capture("04-explore-en-phone", "Explore — English, phone", (1080, 2400))
    language("de"); chooser("de"); open_explore("de")
    capture("05-explore-de-phone", "Explore — German, phone", (1080, 2400))

    back(); mode_and_back("find", "de")
    capture("06-find-practice-de", "Find the word — German practice", (1080, 2400))
    back(); mode_and_back("name", "de")
    capture("07-name-practice-de", "What is it? — German picture-to-word practice", (1080, 2400))

    viewport("1080x2400", density="420")
    rotate("landscape"); chooser("en"); open_explore("en")
    capture("08-explore-phone-landscape", "Explore — phone landscape", (2400, 1080))

    viewport("1080x1920", density="420")
    chooser("de"); open_explore("de")
    capture("09-explore-short-landscape", "Explore — short landscape", (1920, 1080))

    rotate("portrait"); viewport("1600x2560", density="320")
    language("en"); chooser("en")
    capture("10-chooser-tablet-portrait", "Chooser — tablet portrait", (1600, 2560))
    open_explore("en")
    capture("11-explore-tablet-portrait", "Explore — tablet portrait", (1600, 2560))

    rotate("landscape"); chooser("de"); open_explore("de")
    capture("12-explore-tablet-landscape", "Explore — tablet landscape", (2560, 1600))

    rotate("portrait"); viewport("1080x2400", density="420")
    chooser("de"); click("⚙ Optionen"); click("🇬🇧 English"); back()
    current_language = "en"
    capture("13-options-return", "Vocabulary chooser after language change in Options", (1080, 2400))

    chooser("en"); click("Find the word")
    # Rotate away and back to force a real Activity configuration recreation;
    # the durable quiz checkpoint remains owned by VocabularyViewModel.
    rotate("landscape"); rotate("portrait")
    capture("14-find-session-restored", "Find session after Activity recreation", (1080, 2400))
finally:
    shell("cmd", "window", "user-rotation", "free")
    for key in ("size", "density"):
        match = re.search(r"Override (?:size|density): (.+)", saved[key])
        shell("wm", key, match.group(1) if match else "reset")
    for key in ("font_scale", "accelerometer_rotation", "user_rotation"):
        restore(key, saved[key])
    language(old_language)
    figures = "".join(
        f'<figure><a href="gallery/{name}.png"><img src="gallery/{name}.png"></a>'
        f"<figcaption>{html.escape(caption)}<small>{width}×{height}</small></figcaption></figure>"
        for name, caption, width, height in captures)
    before_after = """<section class="compare"><h2>Before / after</h2>
      <article><h3>Before — baseline implementation</h3>
      <p>All mode chips and six word choices preceded the selected animal image,
      word and listening controls. The picture used a fixed 160 dp height.</p></article>
      <article><h3>After — Phase 4D</h3><p>The primary animal picture and spoken-language
      controls appear before the six-word selector. Explore and each practice mode
      launch directly from the compact chooser.</p></article></section>"""
    (ROOT / "index.html").write_text(
        "<!doctype html><meta charset='utf-8'><title>Vocabulary Phase 4D visual review</title>"
        "<style>body{font:16px system-ui;background:#f2f0ea;color:#253449;margin:24px}"
        "main{display:flex;flex-wrap:wrap;gap:18px}figure{margin:0;width:320px}"
        "img{width:100%;height:560px;object-fit:contain;background:white}"
        "figcaption{padding:10px}small{display:block;color:#65756e}.compare{display:flex;gap:20px}"
        ".compare article{max-width:420px;background:white;padding:16px;border-radius:16px}</style>"
        "<h1>Vocabulary Booster Phase 4D — API 35 visual review</h1>"
        "<p>Actual production MainActivity captures; see REVIEW.md for inventory, tests and acceptance limits.</p>"
        f"{before_after}<main>{figures}</main>")
