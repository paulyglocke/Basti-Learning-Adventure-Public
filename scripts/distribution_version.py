"""One version stream for this workflow; attempts 1..99 have distinct codes."""
import os


def version(run_number: str, run_attempt: str) -> tuple[int, str]:
    if not run_number.isascii() or not run_number.isdecimal() or not run_attempt.isascii() or not run_attempt.isdecimal():
        raise ValueError("CI run number and attempt must be positive decimal integers")
    run, attempt = int(run_number), int(run_attempt)
    code = 1_000_000 + run * 100 + attempt
    if run < 1 or not 1 <= attempt <= 99 or code > 2_100_000_000:
        raise ValueError("CI version range exhausted; choose an explicit higher base/stream before distributing")
    return code, f"1.1.{run}.{attempt}"


if __name__ == "__main__":
    code, name = version(os.environ["GITHUB_RUN_NUMBER"], os.environ["GITHUB_RUN_ATTEMPT"])
    print(f"BASTI_VERSION_CODE={code}")
    print(f"BASTI_VERSION_NAME={name}")
