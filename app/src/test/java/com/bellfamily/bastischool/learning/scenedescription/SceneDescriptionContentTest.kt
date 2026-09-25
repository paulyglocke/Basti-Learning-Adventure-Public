package com.bellfamily.bastischool.learning.scenedescription

import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class SceneDescriptionContentTest {
    private val repository = BundledSceneDescriptions.repository()
    private fun asset(path: String): File = File("app/src/main/assets/$path").takeIf { it.exists() }
        ?: File("../app/src/main/assets/$path")

    @Test fun nineCategoriesAndEightyOneApprovedScenesKeepAuthoredOrder() {
        assertEquals(listOf("ocean_underwater", "jungle_rainforest", "woodland_forest", "park_playground",
            "sky_flying", "mountains_alpine", "countryside_farm", "classroom_school", "zoo_wildlife"),
            repository.categories().map { it.id.value })
        assertEquals(81, repository.all().size)
        assertEquals(81, repository.all().map { it.id }.distinct().size)
        repository.categories().forEach { category ->
            val scenes = repository.scenes(category.id)
            assertEquals(9, scenes.size)
            assertEquals(List(3) { SceneWave.ONE } + List(3) { SceneWave.TWO } + List(3) { SceneWave.THREE }, scenes.map { it.wave })
            assertTrue(scenes.all { it.status == SceneProductionStatus.APPROVED })
        }
        assertEquals(repository.all().map { it.id }, BundledSceneDescriptions.repository().all().map { it.id })
        assertEquals(repository.all(), repository.categories().flatMap { repository.scenes(it.id) })
    }

    @Test fun everyCanonicalImageAndMetadataReferenceResolvesWithoutAuthoringAssets() {
        repository.all().forEach { scene ->
            assertSame(scene.image, repository.image(scene.id))
            assertEquals(scene.id.imageId, scene.image.id)
            assertTrue(scene.image.path.contains("/canonical/"))
            assertFalse(scene.image.path.contains("/prompts/"))
            assertFalse(scene.image.path.contains("/reference/"))
            assertTrue(asset(scene.metadataPath).isFile)
            assertArrayEquals(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10), asset(scene.image.path).inputStream().use { it.readNBytes(8) })
        }
    }

    @Test fun generatedDataFingerprintMatchesEveryManifestAndMetadataSource() {
        val paths = BundledSceneDescriptions.sourcePaths
        assertEquals(91, paths.size) // root + nine category manifests + 81 sidecars
        assertEquals(91, paths.distinct().size)
        val digest = MessageDigest.getInstance("SHA-256")
        paths.forEach { path ->
            digest.update(path.toByteArray(Charsets.UTF_8)); digest.update(0.toByte())
            digest.update(asset("SceneDescriptions/$path").readBytes()); digest.update(0.toByte())
        }
        assertEquals(BundledSceneDescriptions.SOURCE_SHA256, digest.digest().joinToString("") { "%02x".format(it) })
    }

    @Test fun unknownIdsAreSafeAndIdentityDoesNotDependOnFilename() {
        assertNull(repository.find(SceneId("scene.unknown.item.99")))
        assertNull(repository.image(SceneId("scene.unknown.item.99")))
        assertTrue(repository.scenes(SceneCategoryId("unknown_category")).isEmpty())
        val first = repository.all().first()
        assertEquals("scene.ocean.reef_actions.01", first.id.value)
        assertEquals("asset.scene.ocean.reef_actions.n01", first.image.id.value)
        assertNotEquals(first.id.value, first.image.path.substringAfterLast('/').removeSuffix(".png"))
    }

    @Test fun everyApprovedSceneHasGermanForEveryAuthoredRuntimeText() {
        assertEquals(2, repository.version.revision)
        fun bilingual(text: SceneText) {
            assertFalse(text[ContentLanguage.ENGLISH].isNullOrBlank())
            assertFalse(text[ContentLanguage.GERMAN].isNullOrBlank())
        }
        fun bilingualLines(lines: SceneLines) {
            ContentLanguage.entries.forEach { language ->
                assertFalse(lines[language].isNullOrEmpty())
                assertTrue(lines[language]!!.all { it.isNotBlank() })
            }
        }
        repository.all().forEach { scene ->
            bilingual(scene.title)
            scene.purpose?.let(::bilingual)
            scene.reviewCaution?.let(::bilingual)
            scene.targets.forEach { group ->
                group.terms.forEach(::bilingual)
                assertNotNull(scene.terms(group.kind, ContentLanguage.GERMAN))
            }
            scene.examples?.let(::bilingualLines)
            scene.adultSupport.principle?.let(::bilingual)
            scene.adultSupport.focus?.let(::bilingual)
            scene.adultSupport.groups.forEach { bilingualLines(it.lines) }
            scene.adultSupport.expansions.forEach { bilingual(it.child); bilingual(it.adult) }
            if (scene.wave == SceneWave.ONE) {
                assertNull(scene.purpose)
                assertNotNull(scene.examples)
                assertTrue(scene.primaryFocus.isNotEmpty()) // technical tags, not localized text
            } else {
                assertNotNull(scene.purpose)
                assertNull(scene.examples) // do not manufacture new examples
            }
        }
    }

    @Test fun germanSpatialAndSequencingPhrasesAreAuthoredNotEnglishFallback() {
        val ocean = repository.find(SceneId("scene.ocean.find_and_follow.08"))!!
        assertEquals("Finde die Krabbe unter dem Stein.", ocean.terms(SceneTargetKind.SENTENCE_MODELS, ContentLanguage.GERMAN)!!.first())
        val sky = repository.find(SceneId("scene.sky.weather_sequence.07"))!!
        assertEquals(listOf("Zuerst ist es sonnig.", "Dann kommen die Wolken.", "Danach regnet es.", "Der Regenbogen kommt nach dem Regen."),
            sky.terms(SceneTargetKind.SENTENCE_MODELS, ContentLanguage.GERMAN))
        // Optional model remains honest outside the complete production pack; no fallback is introduced.
        assertNull(SceneText("English only")[ContentLanguage.GERMAN])
    }

    @Test fun parkCountingUsesFourVisibleBucketsInBothLanguages() {
        val park = repository.find(SceneId("scene.park.big_small_counting.05"))!!
        assertEquals("There are four yellow buckets.", park.terms(SceneTargetKind.SENTENCE_MODELS, ContentLanguage.ENGLISH)!![3])
        assertEquals("Da sind vier gelbe Eimer.", park.terms(SceneTargetKind.SENTENCE_MODELS, ContentLanguage.GERMAN)!![3])
        assertEquals("Kannst du einen Satz mit „vier gelbe Eimer“ sagen?",
            park.adultSupport.lines(SceneSupportKind.EXPANSION_PROMPTS, ContentLanguage.GERMAN)!![1])
    }

    @Test fun separatelyAuthoredLanguageListsAreNotZippedOrExpanded() {
        val lines = SceneLines(listOf("one", "two"), listOf("eins"))
        assertEquals(listOf("one", "two"), lines[ContentLanguage.ENGLISH])
        assertEquals(listOf("eins"), lines[ContentLanguage.GERMAN])
        assertNull(SceneLines(listOf("one"))[ContentLanguage.GERMAN])
    }

    @Test fun loggedFarmReviewLimitationIsRetainedAsAdultData() {
        val farm = repository.find(SceneId("scene.farm.tasks.01"))!!
        assertTrue(farm.reviewCaution!!.en.contains("four chickens"))
        assertTrue(farm.reviewCaution.de!!.contains("vier Hühner"))
        assertTrue(farm.adultSupport.groups.isNotEmpty())
    }

    @Test fun malformedIdsAndBlankAuthoredValuesAreRejected() {
        listOf("", "scene.ocean.01.png", "canonical/ocean.png", "scene.ocean.item").forEach {
            assertThrows(IllegalArgumentException::class.java) { SceneId(it) }
        }
        assertThrows(IllegalArgumentException::class.java) { SceneCategoryId("../ocean") }
        assertThrows(IllegalArgumentException::class.java) { SceneText(" ") }
        assertThrows(IllegalArgumentException::class.java) { SceneText("English", " ") }
        assertThrows(IllegalArgumentException::class.java) { SceneLines(emptyList()) }
        assertThrows(IllegalArgumentException::class.java) { SceneLines(listOf("English"), listOf("")) }
        assertThrows(IllegalArgumentException::class.java) { SceneTargetGroup(SceneTargetKind.NOUNS, emptyList()) }
    }

    @Test fun duplicateIdsAndMissingCategoryReferencesFailAtRepositoryBoundary() {
        val categories = repository.categories()
        val scenes = repository.all()
        assertThrows(IllegalArgumentException::class.java) { SceneDescriptionRepository(categories + categories.first(), scenes) }
        assertThrows(IllegalArgumentException::class.java) { SceneDescriptionRepository(categories, scenes + scenes.first()) }
        assertThrows(IllegalArgumentException::class.java) { SceneDescriptionRepository(categories.drop(1), scenes) }
        assertThrows(IllegalArgumentException::class.java) { SceneDescriptionRepository(categories, scenes.filter { it.categoryId != categories.first().id }) }
    }

    @Test fun repositoryAndNestedCollectionsAreImmutableSnapshots() {
        val categories = repository.categories().toMutableList()
        val scenes = repository.all().toMutableList()
        val snapshot = SceneDescriptionRepository(categories, scenes)
        categories.clear(); scenes.clear()
        assertEquals(81, snapshot.all().size)
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.all() as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.categories() as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.all().first().targets as MutableList).clear() }
        val input = mutableListOf("word")
        val lines = SceneLines(input)
        input.clear()
        assertEquals(listOf("word"), lines.en)
        assertThrows(UnsupportedOperationException::class.java) { (lines.en as MutableList).clear() }
    }
}
