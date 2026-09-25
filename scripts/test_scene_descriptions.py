"""Authoring-boundary fixtures; no Android, image decoding or third-party packages."""
import hashlib
import json
import tempfile
import unittest
from pathlib import Path
import generate_scene_descriptions as content


class SceneDescriptionBoundaryTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.pack = content.load()
        for relative in self.pack[2]:
            path = self.root / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes((content.ROOT / relative).read_bytes())
        for scene in self.pack[1]:
            path = self.root / scene['asset'].removeprefix('SceneDescriptions/')
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(b'\x89PNG\r\n\x1a\n')
        self.category = self.pack[0][0]['id']
        self.cm = f'{self.category}/manifest.json'
        self.meta = self.pack[1][0]['metadata'].removeprefix('SceneDescriptions/')

    def change(self, path, mutate):
        file = self.root / path
        data = json.loads(file.read_text())
        mutate(data)
        file.write_text(json.dumps(data, ensure_ascii=False))

    def rejected(self):
        with self.assertRaises(ValueError):
            content.load(self.root)

    def test_production_counts_and_references(self):
        cats, scenes, paths, _ = content.load(self.root)
        self.assertEqual(9, len(cats))
        self.assertEqual(81, len(scenes))
        self.assertEqual(81, len({s['id'] for s in scenes}))
        self.assertEqual(91, len(paths))
        for cat in cats:
            entries = json.loads((self.root / cat['id'] / 'manifest.json').read_text())['approvedScenes']
            actual = [s for s in scenes if s['category'] == cat['id']]
            self.assertEqual(9, len(actual))
            self.assertEqual([e['sceneId'] for e in entries], [s['id'] for s in actual])
            self.assertEqual([f"SceneDescriptions/{cat['id']}/{e['asset']}" for e in entries], [s['asset'] for s in actual])

    def test_generation_is_deterministic_and_checked_in(self):
        self.assertEqual(content.render(self.pack), content.render(content.load()))
        self.assertEqual(content.OUTPUT.read_text(), content.render(self.pack))

    def test_language_availability_is_not_fabricated(self):
        scenes = self.pack[1]
        self.assertEqual({1: 27, 2: 27, 3: 27}, {w: sum(s['wave'] == w for s in scenes) for w in (1, 2, 3)})
        self.assertEqual(81, sum(s['title']['de'] is not None for s in scenes))
        self.assertTrue(all(s['purpose'] is None for s in scenes if s['wave'] == 1))
        self.assertTrue(all(s['purpose']['de'] for s in scenes if s['wave'] > 1))

    def test_every_runtime_text_has_authored_german(self):
        def check(value):
            if isinstance(value, dict):
                if set(value) == {'en', 'de'}:
                    for locale in ('en', 'de'):
                        authored = value[locale]
                        self.assertIsNotNone(authored)
                        self.assertTrue(authored)
                        self.assertTrue(all(v.strip() for v in authored) if isinstance(authored, list) else authored.strip())
                else:
                    for v in value.values(): check(v)
            elif isinstance(value, (list, tuple)):
                for v in value: check(v)
        check(self.pack[:2])

    def test_english_identity_references_and_order_preserved_except_reviewed_bucket_fix(self):
        # Fingerprint of the normalized English catalogue at 296d6ef. No Git needed at test time.
        def english(value):
            if isinstance(value, dict):
                if set(value) == {'en', 'de'}: return english(value['en'])
                return {k: english(v) for k, v in value.items()}
            if isinstance(value, (list, tuple)): return [english(v) for v in value]
            return value
        projection = english(self.pack[:2])
        park = next(s for s in projection[1] if s['id'] == 'scene.park.big_small_counting.05')
        self.assertEqual('There are four yellow buckets.', park['targets'][3][1][3])
        self.assertEqual('Can you make a sentence with four yellow buckets?', park['groups'][1][1][1])
        # Reverse ONLY these two documented source-error corrections for the preservation comparison.
        park['targets'][3][1][3] = 'There are three yellow buckets.'
        park['groups'][1][1][1] = 'Can you make a sentence with three yellow buckets?'
        digest = hashlib.sha256(json.dumps(projection, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
        self.assertEqual('ae45295414d87446f053b4cb2e09667bc3dfe4f192734bb6b0315338c33acd05', digest)

    def test_wave_two_and_three_missing_or_blank_german_is_rejected(self):
        for wave in (2, 3):
            scene = next(s for s in self.pack[1] if s['wave'] == wave)
            path = self.root / scene['metadata'].removeprefix('SceneDescriptions/')
            original = path.read_text()
            for keys in (('title',), ('purpose',), ('targetLanguage', 'nouns', 0),
                         ('targetLanguage', 'verbs', 0), ('targetLanguage', 'adjectives', 0),
                         ('targetLanguage', 'sentenceModels', 0), ('adultSupport', 'focus'),
                         ('adultSupport', 'starterPrompts'), ('adultSupport', 'expansionPrompts')):
                for replacement in (None, '', []):
                    with self.subTest(wave=wave, field=keys, value=replacement):
                        data = json.loads(original)
                        value = data
                        for key in keys: value = value[key]
                        if replacement is None: value.pop('de')
                        else: value['de'] = replacement
                        path.write_text(json.dumps(data))
                        self.rejected()
            path.write_text(original)

    def test_authoring_and_unlisted_material_never_enters_pack(self):
        for relative in ('reference/extra.json', 'prompts/extra.json', 'metadata/unlisted.json'):
            path = self.root / self.category / relative
            path.parent.mkdir(exist_ok=True)
            path.write_text('not even valid JSON')
        self.assertEqual(content.render(self.pack), content.render(content.load(self.root)))

    def test_malformed_json(self):
        (self.root / self.meta).write_text('{')
        self.rejected()

    def test_nonobject_json(self):
        (self.root / 'manifest.json').write_text('[]')
        self.rejected()

    def test_duplicate_json_key(self):
        (self.root / 'manifest.json').write_text('{"schemaVersion":1,"schemaVersion":1}')
        self.rejected()

    def test_duplicate_category(self):
        self.change('manifest.json', lambda d: d['categories'].__setitem__(1, d['categories'][0]))
        self.rejected()

    def test_duplicate_scene(self):
        self.change(self.cm, lambda d: d['approvedScenes'].__setitem__(1, d['approvedScenes'][0]))
        self.rejected()

    def test_missing_metadata(self):
        (self.root / self.meta).unlink()
        self.rejected()

    def test_missing_image(self):
        (self.root / self.pack[1][0]['asset'].removeprefix('SceneDescriptions/')).unlink()
        self.rejected()

    def test_unapproved_scene(self):
        self.change(self.meta, lambda d: d.update(status='draft'))
        self.rejected()

    def test_metadata_identity_mismatch(self):
        self.change(self.meta, lambda d: d.update(sceneId='scene.ocean.other.01'))
        self.rejected()

    def test_authoring_asset_reference_rejected(self):
        self.change(self.cm, lambda d: d['approvedScenes'][0].update(asset='reference/extra.png'))
        self.rejected()

    def test_missing_bilingual_field(self):
        self.change(self.meta, lambda d: d['title'].pop('de'))
        self.rejected()

    def test_unknown_teaching_field(self):
        self.change(self.meta, lambda d: d['targetLanguage'].update(unrecognised=['test']))
        self.rejected()

    def test_unsupported_schema(self):
        self.change(self.meta, lambda d: d.update(schemaVersion=2))
        self.rejected()

    def test_wave_mismatch(self):
        self.change(self.cm, lambda d: d['approvedScenes'][0].update(wave=3))
        self.rejected()

    def test_missing_required_field(self):
        self.change(self.meta, lambda d: d.pop('adultSupport'))
        self.rejected()


if __name__ == '__main__':
    unittest.main()
