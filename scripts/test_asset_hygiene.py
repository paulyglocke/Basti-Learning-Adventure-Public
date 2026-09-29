"""Synthetic boundary checks for the read-only raster validator."""
import tempfile
import io
import json
import hashlib
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
                self.assertIn('1280x960',rows[4]['error'])


    def test_reviewed_prepositions_size_mode_and_ratio_boundaries(self):
        def check(size, mode='RGB', fmt='PNG'):
            stream=io.BytesIO()
            Image.new(mode,size).save(stream,format=fmt)
            stream.seek(0)
            with Image.open(stream) as image:
                image.load()
                return audit.prepositions_image_error(image)
        for mode in ('RGB','RGBA'):
            for size in ((1448,1086),(1383,1086),(1280,960),(1368,1080),(1512,1080)):
                self.assertEqual('',check(size,mode),(size,mode))
        for size in ((1279,960),(1280,959),(1367,1080),(1513,1080),(1400,1400)):
            self.assertTrue(check(size),size)
        for mode in ('L','P'):
            self.assertTrue(check((1448,1086),mode))
        self.assertTrue(check((1448,1086),fmt='JPEG'))

    def test_all_prepositions_decode_match_manifest_and_have_no_separator(self):
        manifest=json.loads((audit.ASSETS/'Prepositions/metadata/prepositions_art_manifest.json').read_text())
        self.assertEqual(audit.PREPOSITIONS_IMAGE_CONTRACT,manifest['image_contract'])
        records=manifest['scenes']
        self.assertEqual(52,len(records))
        projection=[{k:r[k] for k in ('scene_id','animal','relation','reference','asset_path')} for r in records]
        # Frozen ordered semantic/path projection from fbc7082 before crop-only cleanup.
        self.assertEqual('477a12f410fffd6d6f17abc42def0909c71d32a1d6235f539fdb96a842c841fd',hashlib.sha256(json.dumps(projection,sort_keys=True,separators=(',',':')).encode()).hexdigest())
        self.assertEqual(52,len({r['scene_id'] for r in records}))
        self.assertEqual(52,len({r['asset_path'] for r in records}))
        paths=[audit.ASSETS/r['asset_path'] for r in records]
        self.assertEqual(set(paths),set((audit.ASSETS/'Prepositions/scenes').glob('*.png')))
        for record,result in zip(records,audit.scan(paths)):
            with self.subTest(scene=record['scene_id']):
                self.assertFalse(result['error'])
                self.assertEqual('[]',result['separator_signatures'])
                self.assertEqual(record['width'],result['width'])
                self.assertEqual(record['height'],result['height'])
                self.assertEqual(record['mode'],result['mode'])
                self.assertEqual(record['sha256'],result['sha256'])

if __name__=='__main__': unittest.main()
