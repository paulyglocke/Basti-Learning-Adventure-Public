"""Authoring-boundary fixtures; no Android, image decoding or third-party packages."""
import hashlib
import json
import tempfile
import unittest
from pathlib import Path
import generate_scene_descriptions as content


REVISION_7_PURPOSES = {
    'scene.ocean.reef_actions.01': 'prediction',
    'scene.jungle.actions.01': 'story',
    'scene.woodland.actions.02': 'cause',
    'scene.sky.actions.01': 'properties',
    'scene.mountains.actions.02': 'story',
    'scene.classroom.tasks.01': 'story',
    'scene.zoo.actions.02': 'prediction',
}


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

    def scene_before_example_revision(self, scene):
        # Historical checkpoints retain their exact bundles. The new regression
        # below verifies every explicit child-example-only successor first.
        revision = json.loads((content.REPO / 'docs/tellme-variety/CHILD_EXAMPLE_CORRECTIONS.json').read_text())
        entry = next(s for s in revision['scenes'] if s['scene_id'] == scene['id'])
        restored = json.loads(json.dumps(scene))
        restored['prompts'] = entry['before_prompts']
        return restored

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

    def test_english_identity_references_and_order_preserved_except_documented_visual_corrections(self):
        # Fingerprint of the normalized English catalogue at 296d6ef. No Git needed at test time.
        def english(value):
            if isinstance(value, dict):
                if set(value) == {'en', 'de'}: return english(value['en'])
                return {k: english(v) for k, v in value.items()}
            if isinstance(value, (list, tuple)): return [english(v) for v in value]
            return value
        restored = json.loads(json.dumps(self.pack[:2]))
        # Assert the opt-in conversation bundles, then remove this new field to preserve
        # the original English identity/order fingerprint and historical visual ledger.
        variety = json.loads((content.REPO / 'docs/tellme-variety/CONTENT_CHANGES.json').read_text())
        expected = {s['scene_id']: s['prompts'] for s in variety['scenes']}
        for scene in restored[1]:
            self.assertEqual(expected.get(scene['id'], []), scene.pop('prompts'))
        # Revision 6: assert and reverse only the documented editorial corrections.
        corrections = json.loads((content.REPO / 'docs/tellme-variety/EDITORIAL_CORRECTIONS.json').read_text())
        self.assertEqual(6, corrections['content_revision'])
        self.assertEqual({'scene.woodland.compare_groups.06', 'scene.classroom.art_sorting.06'},
                         {entry['scene_id'] for entry in corrections['scenes']})
        for entry in corrections['scenes']:
            scene = next(s for s in restored[1] if s['id'] == entry['scene_id'])
            for change in entry['changes']:
                container = scene
                for key in change['path'][:-1]:
                    container = container[key]
                key = change['path'][-1]
                self.assertEqual(change['after'], container[key], (entry['scene_id'], change['path']))
                container[key] = change['before']
        # Physical acceptance review: assert and reverse the field-level bilingual ledger.
        # Preserve the original fingerprint rather than replacing it with a new blessing hash.
        ledger = json.loads((content.REPO / 'docs/tellme-visual-qa/CONTENT_CHANGES.json').read_text())
        for entry in ledger:
            scene = next(s for s in restored[1] if s['id'] == entry['scene_id'])
            for change in entry['changes']:
                container = scene
                for key in change['path'][:-1]: container = container[key]
                key = change['path'][-1]
                self.assertEqual(change['after'], container[key], (entry['scene_id'], change['path']))
                container[key] = change['before']
        projection = english(restored)
        park = next(s for s in projection[1] if s['id'] == 'scene.park.big_small_counting.05')
        self.assertEqual('There are four yellow buckets.', park['targets'][3][1][3])
        self.assertEqual('Can you make a sentence with four yellow buckets?', park['groups'][1][1][1])
        # Reverse only documented corrections, asserting their replacement wording first.
        park['targets'][3][1][3] = 'There are three yellow buckets.'
        park['groups'][1][1][1] = 'Can you make a sentence with three yellow buckets?'
        reviewed = {
            'scene.park.playground_actions.07': [
                (('targets', 1, 1, 2), 'climbing', 'standing'),
                (('targets', 3, 1, 2), 'The child is climbing.', 'The child is standing on the rope bridge.'),
                (('groups', 0, 1, 2), 'Who is climbing?', 'Who is on the rope bridge?'),
            ],
            'scene.mountains.journey_story.07': [
                (('targets', 1, 1, 1), 'climbing', 'hiking'),
                (('targets', 3, 1, 3), 'They are going up the mountain.', 'They are walking together on the path.'),
                (('groups', 0, 1, 3), 'Are they going up or down?', 'Who is on the path?'),
                (('groups', 1, 1, 0), 'Tell me the journey from the bottom of the path upward.', 'Tell me about their journey along the path.'),
            ],
        }
        for scene_id, changes in reviewed.items():
            scene = next(s for s in projection[1] if s['id'] == scene_id)
            for keys, original, corrected in changes:
                container = scene
                for key in keys[:-1]: container = container[key]
                self.assertEqual(corrected, container[keys[-1]])
                container[keys[-1]] = original
        digest = hashlib.sha256(json.dumps(projection, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
        self.assertEqual('ae45295414d87446f053b4cb2e09667bc3dfe4f192734bb6b0315338c33acd05', digest)

    def test_visual_review_covers_all_production_images_without_claiming_automatic_semantic_proof(self):
        audit = json.loads((content.REPO / 'docs/tellme-visual-qa/AUDIT.json').read_text())
        scenes = self.pack[1]
        self.assertEqual([s['id'] for s in scenes], [s['scene_id'] for s in audit['scenes']])
        for scene, review in zip(scenes, audit['scenes']):
            self.assertEqual(scene['asset'], review['asset'])
            path = content.REPO / 'app/src/main/assets' / scene['asset']
            self.assertEqual(hashlib.sha256(path.read_bytes()).hexdigest(), review['image_sha256'])
            self.assertTrue(review['visual_evidence'])
            self.assertEqual(scene['id'] == 'scene.park.turn_taking.09', review['production_artwork_changed'])
            groups = dict(scene['groups'])
            for lang in ('en', 'de'):
                prompt = next(groups[k][lang][0] for k in ('QUESTIONS', 'STARTER_PROMPTS', 'EXPANSION_PROMPTS') if groups.get(k, {}).get(lang))
                self.assertEqual(prompt, review['after'][lang]['prompt'])
        park = next(s for s in audit['scenes'] if s['scene_id'] == 'scene.park.turn_taking.09')
        self.assertEqual('PASS', park['classification'])
        self.assertEqual('OWNER_REPLACEMENT_VALIDATED_PENDING_S24', park['resolution'])
        self.assertEqual('BOTH', park['prior_review']['classification'])
        self.assertFalse(park['production_content_changed'])

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

    def test_curated_conversation_bundles_are_bilingual_and_bounded(self):
        selected = [s for s in self.pack[1] if s['prompts']]
        self.assertEqual(81, len(selected))
        self.assertEqual([s['id'] for s in self.pack[1]], [s['id'] for s in selected])
        self.assertEqual(78, sum(len(s['prompts']) == 2 for s in selected))
        self.assertEqual(3, sum(len(s['prompts']) == 3 for s in selected))
        for scene in selected:
            for prompt in scene['prompts']:
                for lang in ('en', 'de'):
                    self.assertTrue(prompt['prompt'][lang])
                    self.assertTrue(prompt['starter'][lang])
                    self.assertTrue(prompt['model'][lang])
                    self.assertTrue(prompt['childExample'][lang])
                    self.assertTrue(prompt['guidance'][lang])
                    self.assertLessEqual(len(prompt['words'][lang]), 3)

    def test_malformed_curated_bundles_are_rejected(self):
        source = next(s for s in self.pack[1] if s['prompts'])
        path = source['metadata'].removeprefix('SceneDescriptions/')
        original = (self.root / path).read_text()
        mutations = [
            lambda d: d.update(tellMePrompts='not a list'),
            lambda d: d.update(tellMePrompts=d['tellMePrompts'][:1]),
            lambda d: d['tellMePrompts'][0]['prompt'].pop('de'),
            lambda d: d['tellMePrompts'][0]['starter'].update(de=''),
            lambda d: d['tellMePrompts'][0]['childExample'].pop('de'),
            lambda d: d['tellMePrompts'][0]['guidance'].update(de=''),
            lambda d: d['tellMePrompts'][0].pop('model'),
            lambda d: d['tellMePrompts'][0].update(unknown='value'),
            lambda d: d['tellMePrompts'][0]['words'].update(en=['one', 'two', 'three', 'four']),
            lambda d: d['tellMePrompts'].__setitem__(1, d['tellMePrompts'][0]),
        ]
        for mutate in mutations:
            with self.subTest(mutation=mutate):
                (self.root / path).write_text(original)
                self.change(path, mutate)
                self.rejected()

    def test_all_rotating_prompt_slots_are_distinct_in_each_language(self):
        for lang in ('en', 'de'):
            prompts = [p['prompt'][lang] for s in self.pack[1] for p in s['prompts']]
            self.assertEqual(165, len(prompts))
            self.assertEqual(165, len(set(prompts)))

    def test_second_visit_revision_preserves_first_bundles_references_and_picture_identity(self):
        # Editorial intent is reviewed by humans. These checks protect the bounded
        # scope and original first-visit/reference content through regeneration.
        revision = json.loads((content.REPO / 'docs/tellme-variety/QUESTION_VARIETY_CORRECTIONS.json').read_text())
        variety = json.loads((content.REPO / 'docs/tellme-variety/CONTENT_CHANGES.json').read_text())
        summary = json.loads((content.REPO / 'docs/tellme-variety/SUMMARY.json').read_text())
        self.assertEqual(7, revision['content_revision'])
        self.assertEqual(11, variety['content_revision'])
        self.assertEqual(11, summary['content_revision'])
        self.assertEqual(7, len(revision['scenes']))
        self.assertEqual(set(REVISION_7_PURPOSES), {entry['scene_id'] for entry in revision['scenes']})
        for entry in revision['scenes']:
            with self.subTest(scene=entry['scene_id']):
                scene = next(s for s in self.pack[1] if s['id'] == entry['scene_id'])
                scene = self.scene_before_example_revision(scene)
                record = next(s for s in variety['scenes'] if s['scene_id'] == scene['id'])
                self.assertEqual(entry['unchanged_first'], scene['prompts'][0])
                self.assertEqual(entry['after'], scene['prompts'][1])
                self.assertNotEqual(entry['before'], entry['after'])
                self.assertEqual(['actions', REVISION_7_PURPOSES[scene['id']]], record['learning_purposes'])
                self.assertEqual(REVISION_7_PURPOSES[scene['id']], entry['learning_purpose'])
                self.assertEqual(scene['asset'], entry['asset'])
                self.assertEqual('app/src/main/assets/' + scene['metadata'], entry['metadata'])
                self.assertEqual(record['image_sha256'], entry['image_sha256'])
                metadata = json.loads((content.REPO / entry['metadata']).read_text())
                metadata.pop('tellMePrompts')
                digest = hashlib.sha256(json.dumps(metadata, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
                self.assertEqual(entry['reference_metadata_sha256'], digest)
        # Keep reported purpose counts tied to all 81 records, not a hand-edited total.
        from collections import Counter
        initial = Counter(s['learning_purposes'][0] for s in variety['scenes'])
        all_slots = Counter(p for s in variety['scenes'] for p in s['learning_purposes'])
        self.assertEqual(dict(initial), summary['initial_purposes'])
        self.assertEqual(dict(all_slots), summary['all_slot_purposes'])
        self.assertEqual(165, sum(all_slots.values()))
        self.assertEqual(33, summary['targeted_desktop_review_scenes'])

    def test_spatial_revision_preserves_other_slots_references_and_support(self):
        revision = json.loads((content.REPO / 'docs/tellme-variety/SPATIAL_PROMPT_CORRECTIONS.json').read_text())
        expected = {
            'scene.ocean.hiding_places.03': [1], 'scene.ocean.reef_routines.06': [1],
            'scene.jungle.positions.03': [1], 'scene.woodland.positions.03': [1],
            'scene.park.positions.03': [1], 'scene.park.routines_positions.06': [1],
            'scene.sky.positions.03': [0, 1], 'scene.mountains.positions.03': [0, 1],
            'scene.mountains.spatial_clues.09': [1], 'scene.farm.positions.03': [1],
            'scene.farm.garden_food.06': [1], 'scene.classroom.positions.03': [1],
            'scene.classroom.objects_places.04': [1], 'scene.zoo.positions.03': [0, 1],
        }
        self.assertEqual(8, revision['content_revision'])
        self.assertEqual(set(expected), {s['scene_id'] for s in revision['scenes']})
        self.assertEqual(17, sum(len(s['changes']) for s in revision['scenes']))
        for entry in revision['scenes']:
            with self.subTest(scene=entry['scene_id']):
                scene = next(s for s in self.pack[1] if s['id'] == entry['scene_id'])
                scene = self.scene_before_example_revision(scene)
                self.assertEqual(expected[scene['id']], [c['slot'] for c in entry['changes']])
                self.assertEqual({0, 1}, {c['slot'] for c in entry['changes']} | {s['slot'] for s in entry['unchanged_slots']})
                for unchanged in entry['unchanged_slots']:
                    if scene['id'] == 'scene.farm.garden_food.06' and unchanged['slot'] == 0:
                        # Revision 9 intentionally improves A; preserve the exact
                        # revision-8 snapshot and verify its explicit successor.
                        later = json.loads((content.REPO / 'docs/tellme-variety/CLUE_STORY_CORRECTIONS.json').read_text())
                        change = next(s for s in later['scenes'] if s['scene_id'] == scene['id'])['changes'][0]
                        self.assertEqual(0, change['slot'])
                        self.assertEqual(unchanged['bundle'], change['before'])
                        self.assertEqual(change['after'], scene['prompts'][0])
                    else:
                        self.assertEqual(unchanged['bundle'], scene['prompts'][unchanged['slot']])
                for change in entry['changes']:
                    self.assertEqual(change['after'], scene['prompts'][change['slot']])
                    for field in ('starter', 'childExample'):
                        self.assertEqual(change['before'][field], change['after'][field])
                    for lang in ('en', 'de'):
                        self.assertNotEqual(change['before']['prompt'][lang], change['after']['prompt'][lang])
                        self.assertNotEqual(change['before']['guidance'][lang], change['after']['guidance'][lang])
                    for field in ('words', 'model'):
                        if scene['id'] == 'scene.park.routines_positions.06':
                            self.assertEqual(change['before'][field]['en'], change['after'][field]['en'])
                        else:
                            self.assertEqual(change['before'][field], change['after'][field])
                self.assertEqual(scene['asset'], entry['asset'])
                self.assertEqual('app/src/main/assets/' + scene['metadata'], entry['metadata'])
                metadata = json.loads((content.REPO / entry['metadata']).read_text())
                metadata.pop('tellMePrompts')
                digest = hashlib.sha256(json.dumps(metadata, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
                self.assertEqual(entry['reference_metadata_sha256'], digest)
                self.assertEqual(entry['image_sha256'], hashlib.sha256((content.REPO / 'app/src/main/assets' / entry['asset']).read_bytes()).hexdigest())

    def test_clue_story_revision_preserves_other_slots_references_and_picture_identity(self):
        revision = json.loads((content.REPO / 'docs/tellme-variety/CLUE_STORY_CORRECTIONS.json').read_text())
        variety = json.loads((content.REPO / 'docs/tellme-variety/CONTENT_CHANGES.json').read_text())
        expected = {
            'scene.ocean.story_actions.04': [1], 'scene.ocean.helping_story.07': [1],
            'scene.ocean.find_and_follow.08': [1], 'scene.jungle.clue_finding.08': [0],
            'scene.jungle.actions_directions.09': [0], 'scene.woodland.find_animals.09': [0, 1],
            'scene.zoo.habitats_story.07': [0], 'scene.zoo.feature_clues.08': [0],
            'scene.classroom.routines.02': [0], 'scene.farm.animal_actions.04': [1],
            'scene.farm.garden_food.06': [0],
        }
        self.assertEqual(8, revision['previous_content_revision'])
        self.assertEqual(9, revision['content_revision'])
        self.assertEqual(set(expected), {s['scene_id'] for s in revision['scenes']})
        self.assertEqual(12, sum(len(s['changes']) for s in revision['scenes']))
        for entry in revision['scenes']:
            with self.subTest(scene=entry['scene_id']):
                scene = next(s for s in self.pack[1] if s['id'] == entry['scene_id'])
                scene = self.scene_before_example_revision(scene)
                record = next(s for s in variety['scenes'] if s['scene_id'] == scene['id'])
                self.assertEqual(expected[scene['id']], [c['slot'] for c in entry['changes']])
                self.assertEqual({0, 1}, {c['slot'] for c in entry['changes']} | {s['slot'] for s in entry['unchanged_slots']})
                for unchanged in entry['unchanged_slots']:
                    self.assertEqual(unchanged['bundle'], scene['prompts'][unchanged['slot']])
                for change in entry['changes']:
                    self.assertEqual(change['after'], scene['prompts'][change['slot']])
                    self.assertNotEqual(change['before'], change['after'])
                    self.assertEqual(change['after_learning_purpose'], record['learning_purposes'][change['slot']])
                self.assertEqual(scene['asset'], entry['asset'])
                self.assertEqual('app/src/main/assets/' + scene['metadata'], entry['metadata'])
                metadata = json.loads((content.REPO / entry['metadata']).read_text())
                metadata.pop('tellMePrompts')
                digest = hashlib.sha256(json.dumps(metadata, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
                self.assertEqual(entry['reference_metadata_sha256'], digest)
                self.assertEqual(entry['image_sha256'], hashlib.sha256((content.REPO / 'app/src/main/assets' / entry['asset']).read_bytes()).hexdigest())

    def test_short_examples_only_revision_preserves_all_other_content(self):
        revision = json.loads((content.REPO / 'docs/tellme-variety/CHILD_EXAMPLE_CORRECTIONS.json').read_text())
        self.assertEqual(9, revision['previous_content_revision'])
        self.assertEqual(10, revision['content_revision'])
        self.assertEqual(121, revision['changed_examples'])
        self.assertEqual(73, revision['changed_scenes'])
        self.assertEqual([s['id'] for s in self.pack[1]], [s['scene_id'] for s in revision['scenes']])
        self.assertEqual(121, sum(len(s['changes']) for s in revision['scenes']))
        self.assertEqual(73, sum(bool(s['changes']) for s in revision['scenes']))
        for scene, entry in zip(self.pack[1], revision['scenes']):
            with self.subTest(scene=scene['id']):
                expected = json.loads(json.dumps(entry['before_prompts']))
                self.assertEqual(len(entry['changes']), len({c['slot'] for c in entry['changes']}))
                for change in entry['changes']:
                    slot = change['slot']
                    self.assertEqual(change['before'], expected[slot]['childExample'])
                    for lang in ('en', 'de'):
                        self.assertNotEqual(change['before'][lang], change['after'][lang])
                    expected[slot]['childExample'] = change['after']
                # Compares questions, starters, words, models, guidance and all
                # unchanged examples across the full 81-scene pack.
                self.assertEqual(expected, scene['prompts'][:2])
                self.assertEqual(scene['asset'], entry['asset'])
                self.assertEqual('app/src/main/assets/' + scene['metadata'], entry['metadata'])
                metadata = json.loads((content.REPO / entry['metadata']).read_text())
                metadata.pop('tellMePrompts')
                digest = hashlib.sha256(json.dumps(metadata, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
                self.assertEqual(entry['reference_metadata_sha256'], digest)
                self.assertEqual(entry['image_sha256'], hashlib.sha256((content.REPO / 'app/src/main/assets' / entry['asset']).read_bytes()).hexdigest())

    def test_third_question_pilot_preserves_first_two_bundles_and_reference_content(self):
        pilot = json.loads((content.REPO / 'docs/tellme-variety/THIRD_QUESTION_PILOT.json').read_text())
        expected = {'scene.ocean.hiding_places.03', 'scene.classroom.routines.02', 'scene.zoo.feature_clues.08'}
        self.assertEqual(10, pilot['previous_content_revision'])
        self.assertEqual(11, pilot['content_revision'])
        self.assertEqual(expected, {s['scene_id'] for s in pilot['scenes']})
        self.assertEqual(expected, {s['id'] for s in self.pack[1] if len(s['prompts']) == 3})
        for entry in pilot['scenes']:
            scene = next(s for s in self.pack[1] if s['id'] == entry['scene_id'])
            self.assertEqual(entry['unchanged_first_two'], scene['prompts'][:2])
            self.assertEqual(entry['third'], scene['prompts'][2])
            self.assertEqual(scene['asset'], entry['asset'])
            self.assertEqual('app/src/main/assets/' + scene['metadata'], entry['metadata'])
            metadata = json.loads((content.REPO / entry['metadata']).read_text())
            metadata.pop('tellMePrompts')
            digest = hashlib.sha256(json.dumps(metadata, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
            self.assertEqual(entry['reference_metadata_sha256'], digest)
            self.assertEqual(entry['image_sha256'], hashlib.sha256((content.REPO / 'app/src/main/assets' / entry['asset']).read_bytes()).hexdigest())

    def test_variety_ledger_preserves_picture_identity_and_complete_scene_order(self):
        variety = json.loads((content.REPO / 'docs/tellme-variety/CONTENT_CHANGES.json').read_text())
        historical = json.loads((content.REPO / 'docs/tellme-visual-qa/AUDIT.json').read_text())
        self.assertEqual([s['id'] for s in self.pack[1]], [s['scene_id'] for s in variety['scenes']])
        for scene, change, review in zip(self.pack[1], variety['scenes'], historical['scenes']):
            self.assertEqual(scene['asset'], change['asset'])
            self.assertEqual(review['image_sha256'], change['image_sha256'])
            self.assertEqual('NATIVE_SPEAKER_REVIEW_PENDING', change['language_acceptance'])
            targeted = {'scene.woodland.compare_groups.06', 'scene.classroom.art_sorting.06'} | set(REVISION_7_PURPOSES)
            spatial = json.loads((content.REPO / 'docs/tellme-variety/SPATIAL_PROMPT_CORRECTIONS.json').read_text())
            targeted |= {entry['scene_id'] for entry in spatial['scenes']}
            clue_story = json.loads((content.REPO / 'docs/tellme-variety/CLUE_STORY_CORRECTIONS.json').read_text())
            targeted |= {entry['scene_id'] for entry in clue_story['scenes']}
            expected = 'TARGETED_DESKTOP_REVIEW_NOT_DEVICE_ACCEPTANCE' if scene['id'] in targeted else 'DRAFT_BASED_ON_PRIOR_AUDIT_NOT_NEW_IMAGE_ACCEPTANCE'
            self.assertEqual(expected, change['visual_acceptance'])

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
