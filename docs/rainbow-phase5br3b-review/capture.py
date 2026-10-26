#!/usr/bin/env python3
"""Capture real Rainbow UI states from the explicit API 35 review emulator.

The script uses only Android ADB input and screencap. It never injects Compose
state. State progression is performed through visible production controls.
"""

from __future__ import annotations

import argparse
import json
import re
import struct
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

SERIAL = "emulator-5554"
ADB = "/Users/paulbell/Library/Android/sdk/platform-tools/adb"
ROOT = Path(__file__).resolve().parent
SHOTS = ROOT / "screenshots"
sys.stdout.reconfigure(line_buffering=True)


def adb(*args: str, timeout: int = 30) -> bytes:
    result = subprocess.run(
        [ADB, "-s", SERIAL, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
    )
    return result.stdout


def shell(*args: str, timeout: int = 30) -> str:
    return adb("shell", *args, timeout=timeout).decode("utf-8", "replace").strip()


def bounds(node: ET.Element) -> tuple[int, int, int, int]:
    match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
    if not match:
        raise ValueError(f"Invalid bounds: {node.get('bounds')!r}")
    return tuple(map(int, match.groups()))  # type: ignore[return-value]


def hierarchy() -> ET.Element:
    shell("uiautomator", "dump", "/sdcard/rainbow-review-window.xml", timeout=30)
    data = shell("cat", "/sdcard/rainbow-review-window.xml")
    return ET.fromstring(data)


def visible_nodes(root: ET.Element, query: str) -> list[tuple[ET.Element, ET.Element]]:
    found: list[tuple[ET.Element, ET.Element]] = []

    def visit(node: ET.Element, clickable_ancestor: ET.Element | None) -> None:
        active = node if node.get("clickable") == "true" else clickable_ancestor
        text = node.get("text", "")
        description = node.get("content-desc", "")
        if query.casefold() in text.casefold() or query.casefold() in description.casefold():
            found.append((node, active if active is not None else node))
        for child in node:
            visit(child, active)

    visit(root, None)
    return found


def tap_query(query: str, *, scroll: bool = True, occurrence: int = 0) -> None:
    for attempt in range(8 if scroll else 1):
        root = hierarchy()
        candidates = visible_nodes(root, query)
        exact = [(node, click_node) for node, click_node in candidates
                 if node.get("text", "").casefold() == query.casefold()
                 or node.get("content-desc", "").casefold() == query.casefold()]
        if exact:
            candidates = exact
        if candidates:
            node, click_node = candidates[occurrence]
            nx0, ny0, nx1, ny1 = bounds(node)
            x0, y0, x1, y1 = bounds(click_node)
            _, _, _, screen_h = bounds(root[0])
            if scroll and attempt < 7 and (ny1 - ny0 < 24 or y1 > screen_h - 160):
                shell("input", "swipe", str((x0 + x1) // 2), str(screen_h * 4 // 5),
                      str((x0 + x1) // 2), str(screen_h // 3), "450")
                time.sleep(0.35)
                continue
            x, y = (x0 + x1) // 2, (y0 + y1) // 2
            y = min(max(y, 100), screen_h - 100)
            print(f"TAP {query!r} at {x},{y} via {click_node.get('class')} bounds={click_node.get('bounds')}")
            shell("input", "tap", str(x), str(y))
            time.sleep(0.7)
            return
        if not scroll or attempt == 7:
            break
        _, _, screen_w, screen_h = bounds(root[0])
        shell("input", "swipe", str(screen_w // 2), str(screen_h * 4 // 5),
              str(screen_w // 2), str(screen_h // 3), "450")
        time.sleep(0.35)
    raise RuntimeError(f"Could not find visible UI text/description containing {query!r}")


def back() -> None:
    shell("input", "keyevent", "KEYCODE_BACK")
    time.sleep(0.6)


def screenshot(name: str, *, screen: str, language: str, state: str,
               viewport: str, font_scale: float) -> dict[str, object]:
    SHOTS.mkdir(parents=True, exist_ok=True)
    path = SHOTS / f"{name}.png"
    image = adb("exec-out", "screencap", "-p", timeout=30)
    path.write_bytes(image)
    if image[:8] != b"\x89PNG\r\n\x1a\n":
        raise RuntimeError(f"{path.name}: ADB did not return a PNG")
    width, height = struct.unpack(">II", image[16:24])
    item = {
        "file": f"screenshots/{path.name}",
        "screen": screen,
        "language": language,
        "state": state,
        "viewport": viewport,
        "fontScale": font_scale,
        "width": width,
        "height": height,
    }
    print(json.dumps(item, ensure_ascii=False))
    return item


def set_font_scale(scale: float) -> None:
    shell("settings", "put", "system", "font_scale", str(scale))
    time.sleep(0.7)


def set_viewport(width: int, height: int, rotation: int, density: int = 420) -> None:
    shell("settings", "put", "system", "accelerometer_rotation", "0")
    shell("settings", "put", "system", "user_rotation", str(rotation))
    shell("wm", "density", str(density))
    shell("wm", "size", f"{width}x{height}")
    time.sleep(1.2)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--basic", action="store_true", help="capture chooser and initial Explore views")
    parser.add_argument("--inspect", help="print text-node ancestry for a visible label")
    parser.add_argument("--order-initial", action="store_true", help="open and capture production Starter")
    parser.add_argument("--matrix", action="store_true", help="capture bilingual, interaction, restoration and viewport states")
    parser.add_argument("--finish", action="store_true", help="continue the matrix from the current Full screen")
    parser.add_argument("--responsive", action="store_true", help="capture restored Full state and responsive layouts")
    parser.add_argument("--landscape-fix", action="store_true", help="recapture landscape viewports with verified dimensions")
    parser.add_argument("--explore-responsive", action="store_true", help="capture Explore across responsive viewports")
    parser.add_argument("--starter-en", action="store_true", help="capture a fresh English Starter flow")
    parser.add_argument("--short-order", action="store_true", help="capture the compact-height Starter/Full prompt")
    parser.add_argument("--starter-de", action="store_true", help="capture fresh German Starter states")
    parser.add_argument("--current-screen", action="store_true", help="capture the current emulator screen without navigation")
    args = parser.parse_args()
    if args.current_screen:
        screenshot("current-screen-debug", screen="Current screen", language="unknown", state="current",
                   viewport="current emulator viewport", font_scale=1.0)
        return
    if args.landscape_fix:
        landscape_capture()
        return
    if args.responsive:
        responsive_capture()
        return
    if args.finish:
        finish_matrix()
        return
    shell("am", "force-stop", "com.bellfamily.bastischool")
    shell("am", "start", "-n", "com.bellfamily.bastischool/.MainActivity")
    time.sleep(2)
    root = hierarchy()
    if not (visible_nodes(root, "Practise") or visible_nodes(root, "Üben")):
        if any(click.get("clickable") == "true" for _, click in visible_nodes(root, "Colours")):
            tap_query("Colours")
        elif any(click.get("clickable") == "true" for _, click in visible_nodes(root, "Farben")):
            tap_query("Farben")
        time.sleep(1)
    root = hierarchy()
    if visible_nodes(root, "Üben"):
        tap_query("Optionen")
        tap_query("English")
        back()
        time.sleep(1)
    if args.inspect:
        root = hierarchy()
        for node, _ in visible_nodes(root, args.inspect):
            print("MATCH", node.attrib)
            for parent in root.iter():
                if node in list(parent):
                    print("PARENT", parent.attrib)
                    for grandparent in root.iter():
                        if parent in list(grandparent):
                            print("GRANDPARENT", grandparent.attrib)
                            break
                    break
    if args.basic:
        screenshot("chooser-en-phone", screen="Colours chooser", language="English", state="initial",
                   viewport="phone portrait 1080x2400", font_scale=1.0)
        tap_query("The Rainbow")
        screenshot("explore-en-phone-initial", screen="Rainbow Explore", language="English", state="initial",
                   viewport="phone portrait 1080x2400", font_scale=1.0)
        root = hierarchy()
        for node, _ in visible_nodes(root, "Seven-band rainbow"):
            print("RAINBOW_CANVAS_BOUNDS", node.get("bounds"), node.get("content-desc"))
    if args.order_initial:
        tap_query("Rainbow Order")
        time.sleep(1.2)
        screenshot("order-starter-en-initial-phone", screen="Rainbow Order — Starter", language="English",
                   state="initial", viewport="phone portrait 1080x2400", font_scale=1.0)
    if args.matrix:
        matrix_capture()
    if args.explore_responsive:
        explore_responsive_capture()
    if args.starter_en:
        starter_english_capture()
    if args.short_order:
        short_order_capture()
    if args.starter_de:
        starter_german_capture()


def starter_german_capture() -> None:
    set_font_scale(1.0)
    set_viewport(1080, 2400, 0, 420)
    root = hierarchy()
    if not (visible_nodes(root, "Üben") or visible_nodes(root, "Practise")):
        if visible_nodes(root, "Discover the rainbow") or visible_nodes(root, "Den Regenbogen entdecken"):
            pass
        else:
            tap_query("Farben" if visible_nodes(root, "Farben") else "Colours")
            time.sleep(0.7)
    root = hierarchy()
    if not visible_nodes(root, "Üben"):
        tap_query("Optionen" if visible_nodes(root, "Optionen") else "Options")
        tap_query("Deutsch")
        back()
        time.sleep(0.8)
    tap_query("Regenbogen ordnen")
    time.sleep(0.8)
    tap_query("Mit Beispiel")
    if visible_nodes(hierarchy(), "Noch einmal"):
        tap_query("Noch einmal")
    screenshot("starter-de-initial", screen="Rainbow Order — Starter", language="German", state="fresh initial",
               viewport="phone portrait 1080x2400", font_scale=1.0)
    tap_query("Rot", occurrence=-1)
    screenshot("starter-de-partial", screen="Rainbow Order — Starter", language="German", state="one placed",
               viewport="phone portrait 1080x2400", font_scale=1.0)
    for name in ["Gelb", "Blau", "Violett"]:
        tap_query(name, occurrence=-1)
    screenshot("starter-de-completed", screen="Rainbow Order — Starter", language="German", state="completed",
               viewport="phone portrait 1080x2400", font_scale=1.0)


def matrix_capture() -> None:
    records: list[dict[str, object]] = []

    def cap(name: str, screen: str, language: str, state: str, viewport: str, scale: float) -> None:
        records.append(screenshot(name, screen=screen, language=language, state=state,
                                  viewport=viewport, font_scale=scale))

    def enter_colours() -> None:
        for _ in range(5):
            root = hierarchy()
            if (visible_nodes(root, "Practise") or visible_nodes(root, "Üben") or
                    visible_nodes(root, "Discover the rainbow") or visible_nodes(root, "Den Regenbogen entdecken")):
                return
            candidates = visible_nodes(root, "Colours")
            if any(click.get("clickable") == "true" for _, click in candidates):
                tap_query("Colours")
                time.sleep(1)
                continue
            if visible_nodes(root, "Farben"):
                # The exact Colours header is non-clickable on the chooser; its activity card is only on Home.
                candidates = visible_nodes(root, "Farben")
                if any(click.get("clickable") == "true" for _, click in candidates):
                    tap_query("Farben")
                    time.sleep(1)
                    continue
            back()
        raise RuntimeError("Could not reach the Colours chooser from the current app route")

    def set_language(language: str) -> None:
        root = hierarchy()
        expected = "Deutsch" if language == "German" else "English"
        if visible_nodes(root, expected):
            return
        tap_query("Options")
        tap_query(expected)
        back()
        time.sleep(1)

    def open_explore() -> None:
        enter_colours()
        tap_query("The Rainbow" if not visible_nodes(hierarchy(), "Den Regenbogen entdecken")
                  else "Der Regenbogen")
        time.sleep(1)

    def open_order() -> None:
        enter_colours()
        tap_query("Rainbow Order" if visible_nodes(hierarchy(), "Rainbow Order") else "Regenbogen ordnen")
        time.sleep(1)

    en = ["Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"]
    de = ["Rot", "Orange", "Gelb", "Grün", "Blau", "Indigo", "Violett"]
    # The caller has already started the app and opened the Colours chooser.
    enter_colours()
    cap("chooser-en-phone", "Colours chooser", "English", "initial", "phone portrait 1080x2400", 1.0)
    tap_query("The Rainbow"); time.sleep(1)
    cap("explore-en-phone-initial", "Rainbow Explore", "English", "initial", "phone portrait 1080x2400", 1.0)
    for index in [0, 1, 5, 6]:
        tap_query(en[index], occurrence=-1)
        cap(f"explore-en-{en[index].lower()}-selected", "Rainbow Explore", "English",
            f"selected {en[index]}", "phone portrait 1080x2400", 1.0)
    tap_query("Listen again")
    # Capture German Explore and chooser through actual Options language control.
    tap_query("Options"); tap_query("Deutsch"); back(); time.sleep(1)
    cap("explore-de-phone-initial", "Rainbow Explore", "German", "initial", "phone portrait 1080x2400", 1.0)
    tap_query("Orange", occurrence=-1)
    cap("explore-de-orange-selected", "Rainbow Explore", "German", "selected Orange",
        "phone portrait 1080x2400", 1.0)
    back(); time.sleep(1)
    cap("chooser-de-phone", "Colours chooser", "German", "initial", "phone portrait 1080x2400", 1.0)

    # Starter: initial, partial, wrong-answer feedback/retry, then completion in both languages.
    tap_query("Regenbogen ordnen"); time.sleep(1)
    cap("starter-de-initial", "Rainbow Order — Starter", "German", "initial", "phone portrait 1080x2400", 1.0)
    tap_query("Rot", occurrence=-1)
    cap("starter-de-partial", "Rainbow Order — Starter", "German", "one placed", "phone portrait 1080x2400", 1.0)
    for name in ["Gelb", "Blau", "Violett"]: tap_query(name, occurrence=-1)
    cap("starter-de-completed", "Rainbow Order — Starter", "German", "completed", "phone portrait 1080x2400", 1.0)
    tap_query("Noch einmal"); time.sleep(0.6)
    for name in ["Gelb", "Rot", "Blau", "Violett"]: tap_query(name, occurrence=-1)
    cap("starter-de-wrong-feedback", "Rainbow Order — Starter", "German", "wrong answer feedback", "phone portrait 1080x2400", 1.0)
    tap_query("Nochmal versuchen")
    tap_query("Mit Beispiel")
    for name in ["Rot", "Gelb", "Blau", "Violett"]: tap_query(name, occurrence=-1)
    cap("starter-de-completed-retry", "Rainbow Order — Starter", "German", "completed after retry", "phone portrait 1080x2400", 1.0)

    # Restore a live partial Starter through a forced process stop and a fresh app/ViewModel.
    tap_query("Noch einmal"); tap_query("Rot", occurrence=-1)
    cap("starter-en-before-restore", "Rainbow Order — Starter", "German", "one placed before force-stop", "phone portrait 1080x2400", 1.0)
    shell("am", "force-stop", "com.bellfamily.bastischool")
    shell("am", "start", "-n", "com.bellfamily.bastischool/.MainActivity"); time.sleep(2)
    open_order()
    cap("starter-en-after-restore", "Rainbow Order — Starter", "German", "one placed after force-stop/reopen",
        "phone portrait 1080x2400", 1.0)
    # Complete and restore the completed durable session as well.
    for name in ["Gelb", "Blau", "Violett"]: tap_query(name, occurrence=-1)
    shell("am", "force-stop", "com.bellfamily.bastischool")
    shell("am", "start", "-n", "com.bellfamily.bastischool/.MainActivity"); time.sleep(2)
    open_order()
    cap("starter-completed-after-restore", "Rainbow Order — Starter", "German", "completed after force-stop/reopen",
        "phone portrait 1080x2400", 1.0)

    # Full challenge: all seven pieces, discoverable help/replay, wrong feedback and completion.
    tap_query("Alle sieben"); time.sleep(1)
    cap("full-de-initial", "Rainbow Order — Full", "German", "initial", "phone portrait 1080x2400", 1.0)
    tap_query("Rot", occurrence=-1)
    cap("full-de-partial", "Rainbow Order — Full", "German", "one placed", "phone portrait 1080x2400", 1.0)
    tap_query("Hilfe"); cap("full-de-help", "Rainbow Order — Full", "German", "help model visible", "phone portrait 1080x2400", 1.0)
    tap_query("Noch einmal hören")
    # Continue from the already placed Red with a deliberately incorrect next choice.
    for name in ["Violett", "Orange", "Gelb", "Grün", "Blau", "Indigo"]:
        tap_query(name, occurrence=-1)
    if visible_nodes(hierarchy(), "Nochmal"):
        cap("full-de-wrong-feedback", "Rainbow Order — Full", "German", "wrong answer feedback", "phone portrait 1080x2400", 1.0)
        tap_query("Nochmal versuchen")
    for name in de: tap_query(name, occurrence=-1)
    cap("full-de-completed", "Rainbow Order — Full", "German", "completed", "phone portrait 1080x2400", 1.0)

    # Switch to English via Options, return to the active screen, capture bilingual states.
    tap_query("Optionen"); tap_query("English"); back(); time.sleep(1)
    enter_colours(); tap_query("Rainbow Order"); time.sleep(1)
    cap("full-en-initial", "Rainbow Order — Full", "English", "initial", "phone portrait 1080x2400", 1.0)
    for name in en[:3]: tap_query(name, occurrence=-1)
    cap("full-en-partial", "Rainbow Order — Full", "English", "three placed", "phone portrait 1080x2400", 1.0)
    tap_query("Help"); cap("full-en-help", "Rainbow Order — Full", "English", "help model visible", "phone portrait 1080x2400", 1.0)
    tap_query("Listen again")
    for name in en[3:]: tap_query(name, occurrence=-1)
    cap("full-en-completed", "Rainbow Order — Full", "English", "completed", "phone portrait 1080x2400", 1.0)

    # Full level durable restoration after process stop, preserving app data.
    tap_query("Play again"); time.sleep(0.7)
    tap_query("Red", occurrence=-1); tap_query("Orange", occurrence=-1)
    cap("full-en-before-restore", "Rainbow Order — Full", "English", "two placed before force-stop", "phone portrait 1080x2400", 1.0)
    shell("am", "force-stop", "com.bellfamily.bastischool")
    shell("am", "start", "-n", "com.bellfamily.bastischool/.MainActivity"); time.sleep(2)
    open_order()
    cap("full-en-after-restore", "Rainbow Order — Full", "English", "two placed after force-stop/reopen",
        "phone portrait 1080x2400", 1.0)

    # Larger text plus responsive emulator viewports; re-open the real activity per viewport.
    for label, width, height, rotation, density, scale in [
        ("phone-landscape", 2400, 1080, 1, 420, 1.0),
        ("short-landscape", 1280, 720, 1, 420, 1.0),
        ("tablet-portrait-emulator", 1600, 2560, 0, 280, 1.0),
        ("tablet-landscape-emulator", 2560, 1600, 1, 280, 1.0),
        ("phone-150-text", 1080, 2400, 0, 420, 1.5),
        ("german-200-text", 1080, 2400, 0, 420, 2.0),
    ]:
        set_viewport(width, height, rotation, density); set_font_scale(scale)
        shell("am", "force-stop", "com.bellfamily.bastischool")
        shell("am", "start", "-n", "com.bellfamily.bastischool/.MainActivity"); time.sleep(2)
        # Restore German before the German enlarged-text case.
        target_lang = "German" if label == "german-200-text" else "English"
        root = hierarchy()
        current_de = bool(visible_nodes(root, "Startseite"))
        if (target_lang == "German") != current_de:
            tap_query("Options"); tap_query("Deutsch" if target_lang == "German" else "English"); back(); time.sleep(1)
        enter_colours(); tap_query("Rainbow Order" if target_lang == "English" else "Regenbogen ordnen"); time.sleep(1)
        cap(f"order-{label}", "Rainbow Order — Full", target_lang, "responsive partial state",
            f"{label} emulator viewport {width}x{height} density {density} dpi", scale)

    # Restore normal phone configuration and finish on Options return in German.
    set_font_scale(1.0); set_viewport(1080, 2400, 0, 420)
    shell("am", "force-stop", "com.bellfamily.bastischool")
    shell("am", "start", "-n", "com.bellfamily.bastischool/.MainActivity"); time.sleep(2)
    tap_query("Options")
    cap("options-return-de", "Options", "German", "language and Sound Off controls", "phone portrait 1080x2400", 1.0)
    tap_query("Sound off")
    cap("options-return-sound-off", "Options", "German", "Sound Off selected", "phone portrait 1080x2400", 1.0)
    back(); time.sleep(1)
    tap_query("Colours")
    time.sleep(1)
    cap("options-return-chooser-de", "Colours chooser", "German", "returned from Options", "phone portrait 1080x2400", 1.0)

    (ROOT / "screenshots.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n")


def finish_matrix() -> None:
    records: list[dict[str, object]] = []

    def cap(name: str, screen: str, language: str, state: str, viewport: str, scale: float) -> None:
        records.append(screenshot(name, screen=screen, language=language, state=state,
                                  viewport=viewport, font_scale=scale))

    # Continue from the completed German Full state left on screen by --matrix.
    tap_query("Optionen")
    tap_query("English")
    back(); time.sleep(1)
    if visible_nodes(hierarchy(), "Play again"):
        tap_query("Play again")
    cap("full-en-initial", "Rainbow Order — Full", "English", "initial", "phone portrait 1080x2400", 1.0)
    for name in ["Red", "Orange", "Yellow"]: tap_query(name, occurrence=-1)
    cap("full-en-partial", "Rainbow Order — Full", "English", "three placed", "phone portrait 1080x2400", 1.0)
    tap_query("Help")
    cap("full-en-help", "Rainbow Order — Full", "English", "help model visible", "phone portrait 1080x2400", 1.0)
    tap_query("Listen again")
    for name in ["Green", "Blue", "Indigo", "Violet"]: tap_query(name, occurrence=-1)
    cap("full-en-completed", "Rainbow Order — Full", "English", "completed", "phone portrait 1080x2400", 1.0)

    tap_query("Play again"); time.sleep(0.7)
    tap_query("Red", occurrence=-1); tap_query("Orange", occurrence=-1)
    cap("full-en-before-restore", "Rainbow Order — Full", "English", "two placed before force-stop", "phone portrait 1080x2400", 1.0)
    shell("am", "force-stop", "com.bellfamily.bastischool")
    shell("am", "start", "-n", "com.bellfamily.bastischool/.MainActivity"); time.sleep(2)
    enter_colours_after_restart()
    cap("full-en-after-restore", "Rainbow Order — Full", "English", "two placed after force-stop/reopen",
        "phone portrait 1080x2400", 1.0)

    for label, width, height, rotation, density, scale, lang in [
        ("phone-landscape", 2400, 1080, 1, 420, 1.0, "English"),
        ("short-landscape", 1280, 720, 1, 420, 1.0, "English"),
        ("tablet-portrait-emulator", 1600, 2560, 0, 280, 1.0, "English"),
        ("tablet-landscape-emulator", 2560, 1600, 1, 280, 1.0, "English"),
        ("phone-150-text", 1080, 2400, 0, 420, 1.5, "English"),
        ("german-200-text", 1080, 2400, 0, 420, 2.0, "German"),
    ]:
        set_viewport(width, height, rotation, density); set_font_scale(scale)
        root = hierarchy()
        current_de = bool(visible_nodes(root, "Optionen") or visible_nodes(root, "Startseite"))
        if (lang == "German") != current_de:
            tap_query("Optionen" if visible_nodes(root, "Optionen") else "Options")
            tap_query("Deutsch" if lang == "German" else "English")
            back(); time.sleep(1)
        cap(f"order-{label}", "Rainbow Order — Full", lang, "responsive restored partial",
            f"{label} emulator viewport {width}x{height} density {density} dpi", scale)

    set_font_scale(1.0); set_viewport(1080, 2400, 0, 420)
    root = hierarchy()
    tap_query("Optionen" if visible_nodes(root, "Optionen") else "Options")
    cap("options-return-de", "Options", "German", "language and audio controls", "phone portrait 1080x2400", 1.0)
    if visible_nodes(hierarchy(), "Ton aus"):
        tap_query("Ton aus")
    else:
        tap_query("Sound off")
    cap("options-return-sound-off", "Options", "German", "Sound Off selected", "phone portrait 1080x2400", 1.0)
    back(); time.sleep(1)
    tap_query("Startseite")
    tap_query("Farben")
    cap("options-return-chooser-de", "Colours chooser", "German", "returned from Options", "phone portrait 1080x2400", 1.0)
    (ROOT / "screenshots.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n")


def enter_colours_after_restart() -> None:
    for _ in range(5):
        root = hierarchy()
        if visible_nodes(root, "Practise") or visible_nodes(root, "Üben"):
            break
        candidates = visible_nodes(root, "Colours") or visible_nodes(root, "Farben")
        if any(click.get("clickable") == "true" for _, click in candidates):
            node_label = "Colours" if visible_nodes(root, "Colours") else "Farben"
            tap_query(node_label)
            time.sleep(1)
            continue
        back()
    label = "Regenbogen ordnen" if visible_nodes(hierarchy(), "Üben") else "Rainbow Order"
    tap_query(label); time.sleep(1)


def responsive_capture() -> None:
    records: list[dict[str, object]] = []

    def cap(name: str, screen: str, language: str, state: str, viewport: str, scale: float) -> None:
        records.append(screenshot(name, screen=screen, language=language, state=state,
                                  viewport=viewport, font_scale=scale))

    label = "Regenbogen ordnen" if visible_nodes(hierarchy(), "Üben") else "Rainbow Order"
    tap_query(label); time.sleep(1)
    cap("full-en-after-restore", "Rainbow Order — Full", "English", "two placed after force-stop/reopen",
        "phone portrait 1080x2400", 1.0)

    for label, width, height, rotation, density, scale, lang in [
        ("phone-landscape", 2400, 1080, 1, 420, 1.0, "English"),
        ("short-landscape", 1280, 720, 1, 420, 1.0, "English"),
        ("tablet-portrait-emulator", 1600, 2560, 0, 280, 1.0, "English"),
        ("tablet-landscape-emulator", 2560, 1600, 1, 280, 1.0, "English"),
        ("phone-150-text", 1080, 2400, 0, 420, 1.5, "English"),
        ("german-200-text", 1080, 2400, 0, 420, 2.0, "German"),
    ]:
        set_viewport(width, height, rotation, density); set_font_scale(scale)
        root = hierarchy()
        current_de = visible_nodes(root, "Optionen") or visible_nodes(root, "Startseite")
        if (lang == "German") != bool(current_de):
            tap_query("Optionen" if current_de else "Options")
            tap_query("Deutsch" if lang == "German" else "English")
            back(); time.sleep(1)
        cap(f"order-{label}", "Rainbow Order — Full", lang, "responsive restored partial",
            f"{label} emulator viewport {width}x{height} density {density} dpi", scale)

    set_font_scale(1.0); set_viewport(1080, 2400, 0, 420)
    root = hierarchy()
    tap_query("Optionen" if visible_nodes(root, "Optionen") else "Options")
    cap("options-return-de", "Options", "German", "language and audio controls", "phone portrait 1080x2400", 1.0)
    tap_query("Ton aus" if visible_nodes(hierarchy(), "Ton aus") else "Sound off")
    cap("options-return-sound-off", "Options", "German", "Sound Off selected", "phone portrait 1080x2400", 1.0)
    back(); time.sleep(1)
    tap_query("Startseite" if visible_nodes(hierarchy(), "Startseite") else "Home")
    root = hierarchy()
    tap_query("Farben" if visible_nodes(root, "Farben") else "Colours")
    cap("options-return-chooser-de", "Colours chooser", "German", "returned from Options", "phone portrait 1080x2400", 1.0)
    (ROOT / "screenshots.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n")


def landscape_capture() -> None:
    records: list[dict[str, object]] = []
    if visible_nodes(hierarchy(), "Üben"):
        tap_query("Optionen"); tap_query("English"); back(); time.sleep(1)
    tap_query("Rainbow Order"); time.sleep(1)
    for name, size_w, size_h, density, description in [
        ("order-phone-landscape", 1080, 2400, 420, "phone landscape 2400x1080"),
        ("order-short-landscape", 720, 1280, 420, "short landscape 1280x720"),
        ("order-tablet-landscape-emulator", 1600, 2560, 280, "tablet landscape emulator viewport 2560x1600"),
    ]:
        set_viewport(size_w, size_h, 1, density)
        time.sleep(1)
        records.append(screenshot(name, screen="Rainbow Order — Full", language="English",
            state="responsive restored partial", viewport=description, font_scale=1.0))
    set_font_scale(1.0); set_viewport(1080, 2400, 0, 420)
    (ROOT / "screenshots-landscape.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n")


def explore_responsive_capture() -> None:
    records: list[dict[str, object]] = []

    def cap(name: str, language: str, viewport: str, scale: float) -> None:
        records.append(screenshot(name, screen="Rainbow Explore", language=language,
            state="initial", viewport=viewport, font_scale=scale))

    tap_query("The Rainbow"); time.sleep(1)
    cap("explore-en-phone-initial", "English", "phone portrait 1080x2400", 1.0)
    for name, size_w, size_h, rotation, density, description in [
        ("explore-phone-landscape", 1080, 2400, 1, 420, "phone landscape 2400x1080"),
        ("explore-short-landscape", 720, 1280, 1, 420, "short landscape 1280x720"),
        ("explore-tablet-portrait-emulator", 1600, 2560, 0, 280, "tablet portrait emulator viewport 1600x2560"),
        ("explore-tablet-landscape-emulator", 1600, 2560, 1, 280, "tablet landscape emulator viewport 2560x1600"),
    ]:
        set_viewport(size_w, size_h, rotation, density)
        cap(name, "English", description, 1.0)
    set_viewport(1080, 2400, 0, 420); set_font_scale(1.5)
    cap("explore-phone-150-text", "English", "phone portrait 1080x2400", 1.5)
    tap_query("Options"); tap_query("Deutsch"); back(); time.sleep(1)
    set_font_scale(2.0)
    cap("explore-german-200-text", "German", "phone portrait 1080x2400", 2.0)
    set_font_scale(1.0); set_viewport(1080, 2400, 0, 420)
    (ROOT / "explore-responsive.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n")


def starter_english_capture() -> None:
    records: list[dict[str, object]] = []
    tap_query("Rainbow Order"); time.sleep(1)
    tap_query("With example"); time.sleep(1)
    if visible_nodes(hierarchy(), "Play again"):
        tap_query("Play again"); time.sleep(1)
    records.append(screenshot("starter-en-initial", screen="Rainbow Order — Starter", language="English",
        state="fresh initial", viewport="phone portrait 1080x2400", font_scale=1.0))
    tap_query("Red", occurrence=-1)
    records.append(screenshot("starter-en-partial", screen="Rainbow Order — Starter", language="English",
        state="one placed", viewport="phone portrait 1080x2400", font_scale=1.0))
    for name in ["Yellow", "Blue", "Violet"]: tap_query(name, occurrence=-1)
    records.append(screenshot("starter-en-completed", screen="Rainbow Order — Starter", language="English",
        state="completed", viewport="phone portrait 1080x2400", font_scale=1.0))
    (ROOT / "starter-en.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n")


def short_order_capture() -> None:
    records: list[dict[str, object]] = []
    tap_query("Rainbow Order"); time.sleep(1)
    if visible_nodes(hierarchy(), "All seven"):
        tap_query("All seven"); time.sleep(1)
    # Finish through selectable colour pieces, then use Play again for a genuine fresh task.
    for _ in range(2):
        if visible_nodes(hierarchy(), "Try again"):
            tap_query("Try again")
        for name in ["Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"]:
            root = hierarchy()
            if visible_nodes(root, "Play again"):
                break
            if visible_nodes(root, "Try again"):
                break
            candidates = visible_nodes(root, name)
            exact = [(node, click) for node, click in candidates
                if node.get("text", "").casefold() == name.casefold() or
                   node.get("content-desc", "").casefold() == name.casefold()]
            if any(click.get("clickable") == "true" for _, click in exact):
                tap_query(name, occurrence=-1)
                time.sleep(1)
        if visible_nodes(hierarchy(), "Try again"):
            tap_query("Try again")
            continue
        break
    if visible_nodes(hierarchy(), "Play again"):
        tap_query("Play again"); time.sleep(1)
    set_viewport(720, 1280, 1, 420)
    records.append(screenshot("order-short-landscape", screen="Rainbow Order — Full", language="English",
        state="fresh initial; compact prompt visible", viewport="short landscape 1280x720", font_scale=1.0))
    set_viewport(1080, 2400, 1, 420)
    records.append(screenshot("order-phone-landscape", screen="Rainbow Order — Full", language="English",
        state="fresh initial", viewport="phone landscape 2400x1080", font_scale=1.0))
    set_viewport(1080, 2400, 0, 420)
    (ROOT / "short-order.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    main()
