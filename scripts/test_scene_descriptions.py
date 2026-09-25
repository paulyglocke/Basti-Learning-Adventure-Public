"""Authoring-boundary fixtures; no Android, image decoding or third-party packages."""
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
        self.assertEqual(27, sum(s['title']['de'] is not None for s in scenes))
        self.assertTrue(all(s['purpose'] is None for s in scenes if s['wave'] == 1))
        self.assertTrue(all(s['title']['de'] is None for s in scenes if s['wave'] > 1))

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
