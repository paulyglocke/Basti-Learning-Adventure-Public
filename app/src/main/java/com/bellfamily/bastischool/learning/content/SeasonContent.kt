package com.bellfamily.bastischool.learning.content

import com.bellfamily.bastischool.learning.models.*
import java.util.Collections

object SeasonIds {
    val SPRING = ContentId("season.spring")
    val SUMMER = ContentId("season.summer")
    val AUTUMN = ContentId("season.autumn")
    val WINTER = ContentId("season.winter")
    fun next(id: ContentId): ContentId = adjacent(id, 1)
    fun previous(id: ContentId): ContentId = adjacent(id, -1)
    private fun adjacent(id: ContentId, direction: Int): ContentId {
        val index = canonicalOrder.indexOf(id)
        require(index >= 0) { "Unknown season" }
        return canonicalOrder[Math.floorMod(index + direction, canonicalOrder.size)]
    }
    val canonicalOrder: List<ContentId> = Collections.unmodifiableList(listOf(SPRING, SUMMER, AUTUMN, WINTER))
}

/** Authored narration from MASTER_PRODUCT_LEARNING_ROADMAP.md; PNGs remain unchanged. */
internal object SeasonContent {
    private val spring = LocalImageAsset(AssetId("asset.season.spring"), "Seasons/blossoming_lakeside_spring_meadow.png")
    private val summer = LocalImageAsset(AssetId("asset.season.summer"), "Seasons/sunny_summer_lakeside_meadow.png")
    private val autumn = LocalImageAsset(AssetId("asset.season.autumn"), "Seasons/autumn_tree_by_the_lakeside.png")
    private val winter = LocalImageAsset(AssetId("asset.season.winter"), "Seasons/snowy_lakeside_meadow_with_bare_tree.png")
    val images: List<LocalImageAsset> = Collections.unmodifiableList(listOf(spring, summer, autumn, winter))
    val definitions: List<SeasonDefinition> = Collections.unmodifiableList(listOf(
        SeasonDefinition(SeasonIds.SPRING, ContentText.plain("Spring", "Frühling"), LocalizedText(
            "It’s spring. The weather is getting warmer, but it isn’t hot yet. New green leaves are growing on the trees, and lots of flowers are starting to bloom.",
            "Es ist Frühling. Das Wetter wird wärmer, aber es ist noch nicht heiß. Neue grüne Blätter wachsen an den Bäumen, und viele Blumen fangen an zu blühen."
        ), spring.id),
        SeasonDefinition(SeasonIds.SUMMER, ContentText.plain("Summer", "Sommer"), LocalizedText(
            "It’s summer. The weather is warm and sunny. The tree is full of thick green leaves, and the grass and plants are growing everywhere.",
            "Es ist Sommer. Das Wetter ist warm und sonnig. Der Baum ist voller grüner Blätter, und überall wachsen Gras und Pflanzen."
        ), summer.id),
        SeasonDefinition(SeasonIds.AUTUMN, ContentText.plain("Autumn", "Herbst"), LocalizedText(
            "It’s autumn. The weather is getting cooler. The leaves are turning orange, red and yellow, and some are falling from the trees. The days are getting shorter too.",
            "Es ist Herbst. Das Wetter wird kühler. Die Blätter werden orange, rot und gelb, und einige fallen von den Bäumen. Die Tage werden auch kürzer."
        ), autumn.id),
        SeasonDefinition(SeasonIds.WINTER, ContentText.plain("Winter", "Winter"), LocalizedText(
            "It’s winter. The weather is very cold. The ground is covered with snow and ice, and the tree has lost all its leaves. The days are short, and it gets dark early.",
            "Es ist Winter. Das Wetter ist sehr kalt. Der Boden ist mit Schnee und Eis bedeckt, und der Baum hat alle seine Blätter verloren. Die Tage sind kurz, und es wird früh dunkel."
        ), winter.id)
    ))
}
