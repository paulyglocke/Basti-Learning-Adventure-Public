#!/usr/bin/env python3
"""Inspect original GitHub Android report ZIPs without extracting or modifying them.

This validates report structure/counts; a successfully inspected report can contain
failed tests. The JSON retains failures explicitly and is not new test execution.
"""
import argparse
import hashlib
import json
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile


def inspect(archive: Path, expected_tests: int) -> dict:
    cases = []
    suites = []
    with zipfile.ZipFile(archive) as bundle:
        names = sorted(n for n in bundle.namelist()
                       if n.startswith("outputs/androidTest-results/connected/")
                       and Path(n).name.startswith("TEST-") and n.endswith(".xml"))
        if not names:
            raise ValueError("No original Android JUnit result suites in archive")
        for name in names:
            if bundle.getinfo(name).file_size > 16 * 1024 * 1024:
                raise ValueError("Result suite exceeds inspection bound")
            suite = ET.fromstring(bundle.read(name))
            if suite.tag != "testsuite":
                raise ValueError(f"Unexpected result root: {name}")
            records = suite.findall("testcase")
            if len(records) != int(suite.attrib["tests"]):
                raise ValueError(f"Suite count differs from testcase records: {name}")
            counts = dict(tests=len(records), failures=0, errors=0, skipped=0)
            for case in records:
                failure, error, skipped = (case.find(tag) for tag in ("failure", "error", "skipped"))
                issue = failure if failure is not None else error
                outcome = ("failure" if failure is not None else "error" if error is not None
                           else "skipped" if skipped is not None else "passed")
                if outcome != "passed":
                    counts[{"failure": "failures", "error": "errors", "skipped": "skipped"}[outcome]] += 1
                cases.append(dict(suite=name, classname=case.attrib["classname"],
                                  name=case.attrib["name"], outcome=outcome,
                                  stack=issue.text if issue is not None else None))
            for key in ("failures", "errors", "skipped"):
                if int(suite.get(key, "0")) != counts[key]:
                    raise ValueError(f"Suite {key} differs from testcase records: {name}")
            suites.append(dict(path=name, **counts))
    if len(cases) != expected_tests:
        raise ValueError(f"Expected {expected_tests} cases, found {len(cases)}")
    totals = {key: sum(s[key] for s in suites) for key in ("tests", "failures", "errors", "skipped")}
    return dict(source=archive.name, sha256=hashlib.sha256(archive.read_bytes()).hexdigest(),
                format="Derived from original hosted Android JUnit XML; not new execution",
                **totals, suites=suites, cases=cases)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--expected-tests", type=int, required=True)
    args = parser.parse_args()
    result = inspect(args.archive, args.expected_tests)
    args.output.write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps({k: result[k] for k in ("source", "tests", "failures", "errors", "skipped")}))
