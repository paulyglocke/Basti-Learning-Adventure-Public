package com.bellfamily.bastischool.learning.content

import com.bellfamily.bastischool.learning.models.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SeasonContentTest {
    private val repository = CoreContent.repository()
    private fun repo(records: List<ContentDefinition> = repository.all(), images: List<LocalImageAsset> = repository.images()) =
        BundledContentRepository(CoreContent.version, records, WeekdayIds.canonicalOrder, images)
    private fun projectFile(path: String): File = File(path).takeIf { it.exists() } ?: File("../$path")

    @Test fun seasonsHaveCanonicalIdsAndSeparateBilingualNamesAndDescriptions() {
        val seasons = repository.seasons()
        assertEquals(listOf("season.spring", "season.summer", "season.autumn", "season.winter"), seasons.map { it.id.value })
        assertEquals(listOf("Spring", "Summer", "Autumn", "Winter"), seasons.map { it.text.display.en })
        assertEquals(listOf("Frühling", "Sommer", "Herbst", "Winter"), seasons.map { it.text.display.de })
        seasons.forEach {
            assertEquals(it.text.display, it.text.speech)
            assertNotEquals(it.text.speech.en, it.spokenDescription.en)
            assertNotEquals(it.text.speech.de, it.spokenDescription.de)
        }
    }

    @Test fun allEightCanonicalDescriptionsRemainExactlyAsAuthored() {
        // The consolidated roadmap delegates authored content to the content layer. Retain the
        // reviewed narration as a fixed regression fixture rather than requiring prose in that doc.
        val expected = listOf(
            LocalizedText("It’s spring. The weather is getting warmer, but it isn’t hot yet. New green leaves are growing on the trees, and lots of flowers are starting to bloom.",
                "Es ist Frühling. Das Wetter wird wärmer, aber es ist noch nicht heiß. Neue grüne Blätter wachsen an den Bäumen, und viele Blumen fangen an zu blühen."),
            LocalizedText("It’s summer. The weather is warm and sunny. The tree is full of thick green leaves, and the grass and plants are growing everywhere.",
                "Es ist Sommer. Das Wetter ist warm und sonnig. Der Baum ist voller grüner Blätter, und überall wachsen Gras und Pflanzen."),
            LocalizedText("It’s autumn. The weather is getting cooler. The leaves are turning orange, red and yellow, and some are falling from the trees. The days are getting shorter too.",
                "Es ist Herbst. Das Wetter wird kühler. Die Blätter werden orange, rot und gelb, und einige fallen von den Bäumen. Die Tage werden auch kürzer."),
            LocalizedText("It’s winter. The weather is very cold. The ground is covered with snow and ice, and the tree has lost all its leaves. The days are short, and it gets dark early.",
                "Es ist Winter. Das Wetter ist sehr kalt. Der Boden ist mit Schnee und Eis bedeckt, und der Baum hat alle seine Blätter verloren. Die Tage sind kurz, und es wird früh dunkel.")
        )
        assertEquals(expected, repository.seasons().map {it.spokenDescription})
        val spring = repository.seasons()[0].spokenDescription
        val summer = repository.seasons()[1].spokenDescription
        assertTrue(spring.en.contains("New green leaves")); assertTrue(spring.en.contains("bloom"))
        assertTrue(summer.en.contains("thick green leaves"))
        assertTrue(spring.de.contains("Blumen")); assertTrue(summer.de.contains("voller grüner Blätter"))
    }

    @Test fun canonicalIllustrationsResolveToExistingUnmodifiedPngFiles() {
        val paths = repository.seasons().map { repository.image(it.illustration)!!.path }
        assertEquals(listOf(
            "Seasons/blossoming_lakeside_spring_meadow.png",
            "Seasons/sunny_summer_lakeside_meadow.png",
            "Seasons/autumn_tree_by_the_lakeside.png",
            "Seasons/snowy_lakeside_meadow_with_bare_tree.png"
        ), paths)
        paths.forEach { path ->
            val file = projectFile("app/src/main/assets/$path")
            assertTrue("Missing local asset: $path", file.isFile)
            val signature = file.inputStream().use { it.readNBytes(8) }
            assertArrayEquals(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10), signature)
        }
    }

    @Test fun missingAndDuplicateAssetReferencesAreRejected() {
        val missing = ContentValidator.validate(repository.all(), WeekdayIds.canonicalOrder, emptyList())
        assertTrue(missing.any { "season.spring" in it && "asset.season.spring" in it })
        assertThrows(IllegalArgumentException::class.java) { repo(images = emptyList()) }
        assertThrows(IllegalArgumentException::class.java) { repo(images = repository.images() + repository.images().first()) }
        assertNull(repository.image(AssetId("asset.unknown")))
    }

    @Test fun incompleteOrUnknownSeasonSetsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { repo(records = repository.all().filterNot { it.id == SeasonIds.WINTER }) }
        val extra = repository.seasons().first().copy(id = ContentId("season.rainy"))
        assertThrows(IllegalArgumentException::class.java) { repo(records = repository.all() + extra) }
    }

    @Test fun assetIdentityAndPathsCannotMasqueradeAsRemoteOrParentFiles() {
        assertThrows(IllegalArgumentException::class.java) { AssetId("Seasons/spring.png") }
        for (path in listOf("", "/spring.png", "../spring.png", "Seasons/../spring.png", "Seasons//spring.png", "https://example.com/spring.png", "Seasons\\spring.png")) {
            assertThrows(IllegalArgumentException::class.java) { LocalImageAsset(AssetId("asset.season.spring"), path) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            repository.seasons().first().copy(spokenDescription = LocalizedText("Spring", " "))
        }
    }

    @Test fun assetReplacementDoesNotChangeSeasonIdentityAndLookupIsDeterministic() {
        val reversed = repo(repository.all().reversed(), repository.images().reversed())
        assertEquals(repository.seasons(), reversed.seasons())
        assertEquals(repository.images(), reversed.images())
        val changedAssets = repository.images().map { it.copy(path = "replacement/${it.id.value}.png") }
        val replaced = repo(images = changedAssets)
        assertEquals(repository.seasons(), replaced.seasons())
        assertEquals(SeasonIds.SPRING, replaced.find(SeasonIds.SPRING)!!.id)
    }

    @Test fun assetAndSeasonCollectionsAreImmutableSnapshots() {
        val images = repository.images().toMutableList()
        val snapshot = repo(images = images)
        images.clear()
        assertEquals(4, snapshot.images().size)
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.images() as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.seasons() as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (SeasonIds.canonicalOrder as MutableList).clear() }
    }
}
