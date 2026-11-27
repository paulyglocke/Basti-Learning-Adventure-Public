#!/usr/bin/env python3
"""Run the focused Gradle selection, retaining optional diagnostics before AVD teardown."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import sys

DEVICE_DIRECTORY = "/sdcard/Android/data/com.bellfamily.bastischool/files/ci-diagnostics"


def run_instrumentation(classes, *, serial, destination, invoke=subprocess.run):
    # This CI entry point must never install/run tests on additional attached devices.
    devices = invoke(["adb", "devices"], check=False, capture_output=True, text=True, timeout=15)
    attached = [line.split() for line in devices.stdout.splitlines()[1:] if line.strip()]
    if devices.returncode != 0 or attached != [[serial, "device"]]:
        print(f"Expected only {serial} ready; refusing connected-device tests: {attached}", file=sys.stderr)
        return 2
    # Keep Gradle output streaming into the workflow log and preserve its actual exit.
    result = invoke(["./gradlew", "connectedDebugAndroidTest",
                     "-Pandroid.testInstrumentationRunnerArguments.class=" + classes,
                     "--console=plain"], check=False)
    try:
        destination.mkdir(parents=True, exist_ok=True)
        probe = invoke(["adb", "-s", serial, "shell", "test", "-d", DEVICE_DIRECTORY],
                       check=False, capture_output=True, text=True, timeout=15)
        collection = dict(serial=serial, gradleExit=result.returncode,
                          source=DEVICE_DIRECTORY, directoryPresent=probe.returncode == 0)
        if probe.returncode == 0:
            pulled = invoke(["adb", "-s", serial, "pull", DEVICE_DIRECTORY, str(destination)],
                            check=False, timeout=30)
            collection["pullExit"] = pulled.returncode
        (destination / "collection.json").write_text(json.dumps(collection, indent=2) + "\n")
    except Exception as error:
        # A missing/unresponsive device or diagnostics filesystem cannot turn a failed
        # test run green, nor replace the original Gradle result with a collection error.
        print(f"Supplementary Android diagnostics unavailable: {error}", file=sys.stderr)
    return result.returncode


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--classes", required=True)
    parser.add_argument("--serial", default=os.environ.get("ANDROID_SERIAL", "emulator-5554"))
    args = parser.parse_args()
    sys.exit(run_instrumentation(args.classes, serial=args.serial,
                                destination=Path("app/build/outputs/ci-diagnostics")))
