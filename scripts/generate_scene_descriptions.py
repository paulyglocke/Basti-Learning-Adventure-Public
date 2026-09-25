"""Validate manifest-only production data and emit pure Kotlin. No runtime JSON dependency."""
import argparse
import hashlib
import json
import re
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
ROOT = REPO / "app/src/main/assets/SceneDescriptions"
OUTPUT = REPO / "app/src/main/java/com/bellfamily/bastischool/learning/scenedescription/BundledSceneDescriptions.kt"
TARGETS = {
    "nouns": "NOUNS", "verbs": "VERBS", "adjectives": "ADJECTIVES",
    "optionalAdjectives": "OPTIONAL_ADJECTIVES", "spatialLanguage": "SPATIAL_LANGUAGE",
    "optionalVerbs": "OPTIONAL_VERBS", "comparisonLanguage": "COMPARISON_LANGUAGE",
    "countingLanguage": "COUNTING_LANGUAGE", "featureLanguage": "FEATURE_LANGUAGE",
    "socialLanguage": "SOCIAL_LANGUAGE", "functionalLanguage": "FUNCTIONAL_LANGUAGE",
    "sentenceModels": "SENTENCE_MODELS",
}
SUPPORT = {
    "wordsToModel": "WORDS_TO_MODEL", "questions": "QUESTIONS",
    "sentenceStarters": "SENTENCE_STARTERS", "modellingExamples": "MODELLING_EXAMPLES",
    "functionalFollowUps": "FUNCTIONAL_FOLLOW_UPS", "instructionExamples": "INSTRUCTION_EXAMPLES",
    "starterPrompts": "STARTER_PROMPTS", "expansionPrompts": "EXPANSION_PROMPTS",
}


def require(ok, message):
    if not ok:
        raise ValueError(message)


def pairs(items):
    result = {}
    for key, value in items:
        require(key not in result, f"Duplicate JSON key: {key}")
        result[key] = value
    return result


def string(value):
    require(isinstance(value, str) and bool(value.strip()), "Expected nonblank authored string")
    return value


def strings(value):
    require(isinstance(value, list) and bool(value), "Expected nonempty authored list")
    return [string(item) for item in value]


def text(value, bilingual=False):
    if isinstance(value, str):
        require(not bilingual, "Required bilingual text")
        return {"en": string(value), "de": None}
    require(isinstance(value, dict) and set(value) == {"en", "de"}, "Expected authored EN/DE text")
    return {"en": string(value["en"]), "de": string(value["de"])}


def lines(value, bilingual=False):
    if isinstance(value, list):
        require(not bilingual, "Required bilingual lines")
        return {"en": strings(value), "de": None}
    require(isinstance(value, dict) and set(value) == {"en", "de"}, "Expected authored EN/DE lines")
    return {"en": strings(value["en"]), "de": strings(value["de"])}


def wave(value):
    if type(value) is int and value in (1, 2, 3):
        return value
    require(value in ("wave_1", "wave_2", "wave_3"), "Unsupported or missing wave")
    return int(value[-1])


def _load(root=ROOT):
    root = Path(root)
    source_paths = []
    digest = hashlib.sha256()

    def read(relative):
        path = root / relative
        require(path.is_file() and not path.is_symlink(), f"Missing/invalid source: {relative}")
        data = path.read_bytes()
        require(len(data) <= 100_000, f"Oversized metadata: {relative}")
        source_paths.append(relative)
        digest.update(relative.encode() + b"\0" + data + b"\0")
        try:
            value = json.loads(data, object_pairs_hook=pairs,
                              parse_constant=lambda _: require(False, "Non-finite JSON value"))
            require(isinstance(value, dict), f"Expected JSON object: {relative}")
            return value
        except (ValueError, UnicodeError) as error:
            raise ValueError(f"{relative}: {error}") from error

    manifest = read("manifest.json")
    require(manifest.get("schemaVersion") == 1 and manifest.get("assetFamily") == "scene_descriptions", "Unsupported root manifest")
    require(isinstance(manifest.get("categories"), list) and len(manifest["categories"]) == 9, "Expected nine scene categories")
    categories, scenes, ids, assets, metadata_refs = [], [], set(), set(), set()
    for category in manifest["categories"]:
        cid = category["id"]
        require(isinstance(cid, str) and re.fullmatch(r"[a-z]+(?:_[a-z]+)*", cid), "Invalid category ID")
        require(cid not in [c["id"] for c in categories], f"Duplicate category: {cid}")
        categories.append({"id": cid, "display": text(category["display"], True)})
        cm = read(f"{cid}/manifest.json")
        require(cm.get("schemaVersion") == 1 and cm.get("categoryId") == cid, f"Category manifest mismatch: {cid}")
        require(isinstance(cm.get("approvedScenes"), list) and len(cm["approvedScenes"]) == 9, f"Expected nine scenes: {cid}")
        for entry in cm["approvedScenes"]:
            sid = entry["sceneId"]
            require(isinstance(sid, str) and re.fullmatch(r"scene\.[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)*\.[0-9]{2}", sid), "Invalid semantic scene ID")
            require(sid not in ids, f"Duplicate scene ID: {sid}")
            ids.add(sid)
            asset, metadata = entry["asset"], entry["metadata"]
            require(isinstance(asset, str) and re.fullmatch(r"canonical/[a-z0-9_]+\.png", asset), f"Noncanonical asset: {sid}")
            require(metadata == "metadata/" + asset.split('/')[1][:-4] + ".json", f"Metadata pairing mismatch: {sid}")
            full_asset, full_metadata = f"{cid}/{asset}", f"{cid}/{metadata}"
            require(full_asset not in assets and full_metadata not in metadata_refs, f"Duplicate scene reference: {sid}")
            assets.add(full_asset); metadata_refs.add(full_metadata)
            path = root / full_asset
            require(path.is_file() and not path.is_symlink(), f"Missing canonical asset: {sid}")
            with path.open("rb") as stream:
                require(stream.read(8) == b"\x89PNG\r\n\x1a\n", f"Invalid canonical PNG: {sid}")
            data = read(full_metadata)
            require(data.get("sceneId") == sid and data.get("categoryId") == cid and data.get("asset") == asset, f"Manifest/metadata mismatch: {sid}")
            require(data.get("status") == "approved", f"Unapproved production scene: {sid}")
            w = wave(data.get("wave"))
            require("wave" not in entry or wave(entry["wave"]) == w, f"Wave mismatch: {sid}")
            require(data.get("schemaVersion", 1 if w > 1 else None) == 1, f"Unsupported metadata schema: {sid}")
            bilingual = w == 1
            title = text(data["title"], bilingual)
            require("title" not in entry or entry["title"] == title["en"], f"Title mismatch: {sid}")
            target = data["targetLanguage"]
            require(isinstance(target, dict) and target and set(target) <= TARGETS.keys(), f"Unknown/empty target language: {sid}")
            targets = []
            for kind, values in target.items():
                require(isinstance(values, list) and bool(values), f"Invalid target list: {sid}/{kind}")
                targets.append((TARGETS[kind], [text(v, bilingual) for v in values]))
            adult = data["adultSupport"]
            require(isinstance(adult, dict) and set(adult) <= set(SUPPORT) | {"principle", "focus", "expansionExamples"}, f"Unknown support field: {sid}")
            groups = [(SUPPORT[k], lines(v, bilingual)) for k, v in adult.items() if k in SUPPORT]
            require(bool(groups), f"Missing adult support: {sid}")
            expansions = []
            if "expansionExamples" in adult:
                require(isinstance(adult["expansionExamples"], list), "Invalid expansions")
                for example in adult["expansionExamples"]:
                    require(isinstance(example, dict) and set(example) == {"child", "adult"}, "Invalid expansion pair")
                    expansions.append((text(example["child"], bilingual), text(example["adult"], bilingual)))
            focus = data.get("learningFocus")
            require(focus is None or isinstance(focus, dict) and set(focus) == {"primary", "secondary"}, "Invalid learning focus")
            scenes.append(dict(id=sid, category=cid, asset="SceneDescriptions/" + full_asset,
                metadata="SceneDescriptions/" + full_metadata, wave=w, title=title,
                purpose=text(data["purpose"]) if "purpose" in data else None,
                primary=strings(focus["primary"]) if focus else [], secondary=strings(focus["secondary"]) if focus else [],
                targets=targets, examples=lines(data["exampleChildDescriptions"], True) if "exampleChildDescriptions" in data else None,
                principle=text(adult["principle"], bilingual) if "principle" in adult else None,
                focus=text(adult["focus"]) if "focus" in adult else None, groups=groups, expansions=expansions,
                caution=text(data["strictReview"]["reason"]) if "strictReview" in data else None))
    return categories, scenes, source_paths, digest.hexdigest()


def load(root=ROOT):
    """Reject invalid authoring input before any generated output can be written."""
    try:
        return _load(root)
    except (KeyError, TypeError, AttributeError, OSError) as error:
        raise ValueError(f"Invalid scene source: {error}") from error


def quote(value):
    return json.dumps(value, ensure_ascii=False).replace("$", "\\$")


def listing(values):
    return "listOf(" + ", ".join(values) + ")"


def kt_text(value):
    return "null" if value is None else "SceneText(" + quote(value["en"]) + (", " + quote(value["de"]) if value["de"] is not None else "") + ")"


def kt_lines(value):
    return "null" if value is None else "SceneLines(" + listing(map(quote, value["en"])) + (", " + listing(map(quote, value["de"])) if value["de"] is not None else "") + ")"


def render(pack):
    categories, scenes, paths, digest = pack
    out = ["// Generated by scripts/generate_scene_descriptions.py; edit manifest/metadata, then regenerate.",
           "package com.bellfamily.bastischool.learning.scenedescription", "",
           "import com.bellfamily.bastischool.learning.models.*", "",
           "object BundledSceneDescriptions {", f"    const val SOURCE_SHA256 = {quote(digest)}",
           "    val sourcePaths: List<String> = java.util.Collections.unmodifiableList(" + listing(map(quote, paths)) + ")",
           "    fun repository(): SceneDescriptionRepository = SceneDescriptionRepository(",
           "        " + listing("SceneCategory(SceneCategoryId(" + quote(c['id']) + "), LocalizedText(" + quote(c['display']['en']) + ", " + quote(c['display']['de']) + "))" for c in categories) + ",",
           "        " + listing(f"scene{i}()" for i in range(len(scenes))) + ",", "    )", ""]
    for i, s in enumerate(scenes):
        out += [f"    private fun scene{i}(): SceneDescription = SceneDescription(",
                f"        id = SceneId({quote(s['id'])}), categoryId = SceneCategoryId({quote(s['category'])}),",
                f"        image = LocalImageAsset(SceneId({quote(s['id'])}).imageId, {quote(s['asset'])}),",
                f"        metadataPath = {quote(s['metadata'])}, wave = SceneWave.{['ONE', 'TWO', 'THREE'][s['wave']-1]},",
                f"        title = {kt_text(s['title'])}, purpose = {kt_text(s['purpose'])},",
                f"        primaryFocus = {listing(map(quote, s['primary']))}, secondaryFocus = {listing(map(quote, s['secondary']))},",
                "        targets = listOf("]
        out += ["            SceneTargetGroup(SceneTargetKind." + k + ", " + listing(map(kt_text, v)) + ")," for k, v in s['targets']]
        out += ["        ),", "        examples = " + kt_lines(s['examples']) + ",",
                "        adultSupport = SceneAdultSupport(",
                f"            principle = {kt_text(s['principle'])}, focus = {kt_text(s['focus'])},", "            groups = listOf("]
        out += [f"                SceneSupportGroup(SceneSupportKind.{k}, {kt_lines(v)})," for k, v in s['groups']]
        out += ["            ),", "            expansions = " + listing("SceneExpansion(" + kt_text(c) + ", " + kt_text(a) + ")" for c, a in s['expansions']) + ",",
                "        ),", "        reviewCaution = " + kt_text(s['caution']) + ",", "    )", ""]
    return "\n".join(out + ["}", ""])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    try:
        generated = render(load())
        if args.check:
            require(OUTPUT.is_file() and OUTPUT.read_text() == generated, "Generated scene data is stale; run scripts/generate_scene_descriptions.py")
        else:
            OUTPUT.parent.mkdir(parents=True, exist_ok=True)
            OUTPUT.write_text(generated)
        print("Validated 9 categories / 81 approved scenes; generated Kotlin " + ("current" if args.check else "written"))
    except (ValueError, KeyError, TypeError, OSError) as error:
        raise SystemExit(f"Scene content rejected: {error}") from error


if __name__ == "__main__":
    main()
