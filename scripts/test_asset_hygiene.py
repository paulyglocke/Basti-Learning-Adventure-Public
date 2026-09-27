"""Synthetic boundary checks for the read-only raster validator."""
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
from PIL import Image
import audit_asset_hygiene as audit


class AssetHygieneTest(unittest.TestCase):
    def test_solid_scene_is_clean(self):
        self.assertEqual('CLEAN', audit.analyse(Image.new('RGB', (160, 160), '#35734a'))['classification'])

    def test_pale_scenery_only_warns(self):
        self.assertEqual('AMBIGUOUS_REVIEW_REQUIRED', audit.analyse(Image.new('RGB', (160, 160), 'white'))['classification'])

    def test_transparent_white_does_not_fail(self):
        self.assertNotEqual('CONFIRMED_SEPARATOR_SLIVER', audit.analyse(Image.new('RGBA', (160, 160), (255,255,255,0)))['classification'])

    def test_all_four_separator_edges(self):
        for edge, box in [('top',(0,10,160,14)), ('bottom',(0,146,160,150)), ('left',(10,0,14,160)), ('right',(146,0,150,160))]:
            with self.subTest(edge=edge):
                image=Image.new('RGB',(160,160),'green'); image.paste('white',box)
                signatures=audit.analyse(image)['separator_signatures']
                self.assertIn('"edge": "'+edge+'"',signatures)

    def test_outer_gutter_has_no_sliver(self):
        image=Image.new('RGB',(160,160),'green'); image.paste('white',(0,0,160,4))
        self.assertEqual('AMBIGUOUS_REVIEW_REQUIRED',audit.analyse(image)['classification'])

    def test_corrupt_empty_duplicate_and_dimension_checks(self):
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory)
            with patch.object(audit,'ROOT',root):
                empty=root/'empty.png'; empty.write_bytes(b'')
                corrupt=root/'bad.png'; corrupt.write_bytes(b'not PNG')
                valid=root/'valid.png'; Image.new('RGB',(160,160),'green').save(valid)
                wrong=root/'Prepositions/scenes/wrong.png'; wrong.parent.mkdir(parents=True); Image.new('RGB',(20,20)).save(wrong)
                rows=audit.scan([empty,corrupt,valid,valid,wrong])
                self.assertTrue(rows[0]['error']); self.assertTrue(rows[1]['error'])
                self.assertFalse(rows[2]['error']); self.assertEqual('duplicate path',rows[3]['error'])
                self.assertIn('1448x1086',rows[4]['error'])

if __name__=='__main__': unittest.main()
