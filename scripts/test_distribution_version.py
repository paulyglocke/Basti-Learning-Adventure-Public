import unittest
from distribution_version import version


class DistributionVersionTest(unittest.TestCase):
    def test_first_code_and_readable_name(self):
        self.assertEqual((1000101, "1.1.1.1"), version("1", "1"))

    def test_new_run_exceeds_every_previous_attempt(self):
        self.assertGreater(version("43", "1")[0], version("42", "99")[0])

    def test_rerun_gets_a_higher_code(self):
        self.assertGreater(version("42", "2")[0], version("42", "1")[0])

    def test_invalid_values_reject_instead_of_reusing_a_code(self):
        for run, attempt in [("0", "1"), ("1", "0"), ("1", "100"), ("-1", "1"), ("oops", "1"), ("1", "１")]:
            with self.subTest(run=run, attempt=attempt), self.assertRaises(ValueError):
                version(run, attempt)

    def test_android_limit(self):
        self.assertEqual(2099999999, version("20989999", "99")[0])
        with self.assertRaises(ValueError):
            version("20990000", "1")
