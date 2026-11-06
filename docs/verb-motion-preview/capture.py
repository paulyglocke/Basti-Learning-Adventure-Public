#!/usr/bin/env python3
"""Record the actual debug Compose preview, preserving emulator motion/rotation settings.

Requires the current debug APK already installed. Does not change app preferences,
clear data or install anything. Videos intentionally contain no captured TTS audio.
"""
import argparse
import json
import pathlib
import re
import shutil
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET

ADB = pathlib.Path.home() / "Library/Android/sdk/platform-tools/adb"
PACKAGE = "com.bellfamily.bastischool"
ACTIVITY = PACKAGE + "/.ui.verbmotion.VerbMotionActivity"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", default="emulator-5554")
    parser.add_argument("--verbs", nargs="+", choices=("jump", "fly", "snap"), default=["jump", "fly", "snap"])
    args = parser.parse_args()
    if not args.serial.startswith("emulator-"):
        parser.error("This capture harness is for an emulator only.")
    output = pathlib.Path(__file__).resolve().parent / "native"
    output.mkdir(exist_ok=True)

    def adb(*values, **kwargs):
        return subprocess.run([str(ADB), "-s", args.serial, *values], check=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE, **kwargs).stdout

    settings = [("global", "animator_duration_scale"), ("system", "accelerometer_rotation"),
                ("system", "user_rotation")]
    original = {(area, key): adb("shell", "settings", "get", area, key).decode().strip()
                for area, key in settings}

    def screenshot(name):
        remote = "/sdcard/basti-verb-preview.png"
        adb("shell", "screencap", "-p", remote)
        adb("pull", remote, str(output / (name + ".png")))

    with tempfile.TemporaryDirectory(prefix="basti-verb-capture-") as scratch:
        def tap_text(label, tap=True):
            deadline = time.monotonic() + 30
            while time.monotonic() < deadline:
                remote = "/sdcard/basti-verb-preview.xml"
                adb("shell", "uiautomator", "dump", remote)
                try:
                    local = pathlib.Path(scratch) / "window.xml"
                    adb("pull", remote, str(local))
                    nodes = ET.parse(local).iter("node")
                    node = next((n for n in nodes if n.get("text") == label and n.get("enabled") == "true"), None)
                    if node is not None:
                        x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
                        if x2 > x1 and y2 > y1:
                            if tap:
                                adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
                            return
                    # Longer German copy can put the picture-step control below the fold.
                    adb("shell", "input", "swipe", "540", "1900", "540", "1400", "350")
                except (subprocess.CalledProcessError, ET.ParseError):
                    pass
                time.sleep(.5)
            raise RuntimeError("Cannot find visible control: " + label)

        try:
            adb("shell", "settings", "put", "global", "animator_duration_scale", "1")
            adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
            adb("shell", "settings", "put", "system", "user_rotation", "0")
            for verb in args.verbs:
                adb("shell", "am", "start", "-S", "-n", ACTIVITY, "--es", "verb", verb)
                tap_text("Watch again", tap=False)
                screenshot(verb)
                # Find the button before recording, so the clip starts with its real activation.
                tap_text("Watch again")
                remote = "/sdcard/basti-verb-" + verb + ".mp4"
                # Keep the real device aspect while avoiding a full-resolution
                # encoder stall on resource-constrained local emulators.
                adb("shell", "screenrecord", "--time-limit", "8", "--size", "720x1600",
                    "--bit-rate", "2000000", remote, timeout=60)
                candidate = pathlib.Path(scratch) / (verb + ".mp4")
                adb("pull", remote, str(candidate))
                info = json.loads(subprocess.check_output(["ffprobe", "-v", "error", "-show_entries",
                                                          "format=duration", "-of", "json", str(candidate)]))
                if float(info.get("format", {}).get("duration", 0)) < 6:
                    raise RuntimeError("Incomplete recording: " + verb)
                shutil.copy2(candidate, output / (verb + ".mp4"))
                print("Recorded", verb, flush=True)
            adb("shell", "am", "start", "-S", "-n", ACTIVITY, "--es", "verb", "snap",
                "--es", "language", "de")
            time.sleep(1.5)
            tap_text("Bildschritte zeigen")
            time.sleep(.5)
            adb("shell", "input", "swipe", "540", "1900", "540", "1100", "350")
            time.sleep(.5)
            screenshot("snap-de-steps")
        finally:
            adb("shell", "am", "force-stop", PACKAGE)
            for (area, key), value in original.items():
                if value == "null":
                    adb("shell", "settings", "delete", area, key)
                else:
                    adb("shell", "settings", "put", area, key, value)


if __name__ == "__main__":
    main()
