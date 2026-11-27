import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import Mock

from run_android_instrumentation import DEVICE_DIRECTORY, run_instrumentation


class AndroidInstrumentationRunnerTest(unittest.TestCase):
    def run_with(self, outcomes):
        calls = []

        def invoke(args, **kwargs):
            calls.append((args, kwargs))
            if args == ["adb", "devices"]:
                return subprocess.CompletedProcess(args, 0, stdout="List of devices attached\nemulator-5554\tdevice\n")
            result = next(outcomes)
            if isinstance(result, Exception):
                raise result
            return subprocess.CompletedProcess(args, result)

        with tempfile.TemporaryDirectory() as directory:
            destination = Path(directory) / "diagnostics"
            code = run_instrumentation("C.first,C.second", serial="emulator-5554",
                                       destination=destination, invoke=invoke)
            report = destination / "collection.json"
            data = json.loads(report.read_text()) if report.exists() else None
        return code, calls, data

    def test_failed_gradle_stays_failed_after_successful_collection(self):
        code, calls, data = self.run_with(iter([7, 0, 0]))
        self.assertEqual(7, code)
        self.assertEqual(7, data["gradleExit"])
        self.assertEqual(0, data["pullExit"])
        self.assertEqual("-Pandroid.testInstrumentationRunnerArguments.class=C.first,C.second", calls[1][0][2])
        self.assertEqual(["adb", "-s", "emulator-5554", "pull", DEVICE_DIRECTORY], calls[3][0][:5])
        self.assertEqual(15, calls[2][1]["timeout"])
        self.assertEqual(30, calls[3][1]["timeout"])

    def test_absent_diagnostics_do_not_require_a_pull(self):
        code, calls, data = self.run_with(iter([0, 1]))
        self.assertEqual(0, code)
        self.assertEqual(3, len(calls))
        self.assertFalse(data["directoryPresent"])

    def test_collection_timeout_cannot_hide_gradle_failure(self):
        code, _, data = self.run_with(iter([1, subprocess.TimeoutExpired("adb", 15)]))
        self.assertEqual(1, code)
        self.assertIsNone(data)

    def test_failed_pull_preserves_original_success_or_failure(self):
        for original in (0, 1):
            with self.subTest(original=original):
                code, _, data = self.run_with(iter([original, 0, 1]))
                self.assertEqual(original, code)
                self.assertEqual(1, data["pullExit"])

    def test_collection_filesystem_failure_cannot_replace_gradle_result(self):
        destination = Mock()
        destination.mkdir.side_effect = OSError("cannot save diagnostics")
        invoke = Mock(side_effect=[subprocess.CompletedProcess([], 0, stdout="List of devices attached\nemulator-5554\tdevice\n"), subprocess.CompletedProcess([], 2)])
        code = run_instrumentation("C", serial="emulator-5554", destination=destination, invoke=invoke)
        self.assertEqual(2, code)

    def test_additional_physical_device_is_rejected_before_gradle(self):
        invoke = Mock(return_value=subprocess.CompletedProcess([], 0,
            stdout="List of devices attached\nemulator-5554\tdevice\nphysical-phone\tdevice\n"))
        code = run_instrumentation("C", serial="emulator-5554", destination=Path("unused"), invoke=invoke)
        self.assertEqual(2, code)
        invoke.assert_called_once()

    def test_missing_or_offline_emulator_is_rejected_before_gradle(self):
        for state in ("", "emulator-5554\toffline\n"):
            with self.subTest(state=state):
                invoke = Mock(return_value=subprocess.CompletedProcess([], 0,
                    stdout="List of devices attached\n" + state))
                code = run_instrumentation("C", serial="emulator-5554", destination=Path("unused"), invoke=invoke)
                self.assertEqual(2, code)
                invoke.assert_called_once()


if __name__ == "__main__":
    unittest.main()
