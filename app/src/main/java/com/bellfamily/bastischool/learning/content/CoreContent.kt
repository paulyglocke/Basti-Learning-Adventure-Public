package com.bellfamily.bastischool.learning.content

import com.bellfamily.bastischool.learning.models.*
import java.util.Collections

object WeekdayIds {
    val MONDAY = ContentId("day.monday")
    val TUESDAY = ContentId("day.tuesday")
    val WEDNESDAY = ContentId("day.wednesday")
    val THURSDAY = ContentId("day.thursday")
    val FRIDAY = ContentId("day.friday")
    val SATURDAY = ContentId("day.saturday")
    val SUNDAY = ContentId("day.sunday")
    // Explicit semantic order; never enum ordinal, label sort, colour or device locale.
    val canonicalOrder: List<ContentId> = Collections.unmodifiableList(listOf(
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    ))
}

object CoreContent {
    val version = ContentVersion(schema = 1, revision = 1)

    /** Fresh validated snapshots; no mutable singleton registry or platform initialization. */
    fun repository(): ContentRepository {
        val green = ColourDefinition(ContentId("colour.green"), ContentText.plain("green", "grün"))
        val red = ColourDefinition(ContentId("colour.red"), ContentText.plain("red", "rot"))
        val yellow = ColourDefinition(ContentId("colour.yellow"), ContentText.plain("yellow", "gelb"))
        val blue = ColourDefinition(ContentId("colour.blue"), ContentText.plain("blue", "blau"))
        val purple = ColourDefinition(ContentId("colour.purple"), ContentText.plain("purple", "lila"))
        val orange = ColourDefinition(ContentId("colour.orange"), ContentText.plain("orange", "orange"))
        val pink = ColourDefinition(ContentId("colour.pink"), ContentText.plain("pink", "rosa"))
        val definitions = listOf(
            green, red, yellow, blue, purple, orange, pink,
            WeekdayDefinition(WeekdayIds.MONDAY, ContentText.plain("Monday", "Montag"), green.id),
            WeekdayDefinition(WeekdayIds.TUESDAY, ContentText.plain("Tuesday", "Dienstag"), red.id),
            WeekdayDefinition(WeekdayIds.WEDNESDAY, ContentText.plain("Wednesday", "Mittwoch"), yellow.id),
            WeekdayDefinition(WeekdayIds.THURSDAY, ContentText.plain("Thursday", "Donnerstag"), blue.id),
            WeekdayDefinition(WeekdayIds.FRIDAY, ContentText.plain("Friday", "Freitag"), purple.id),
            WeekdayDefinition(WeekdayIds.SATURDAY, ContentText.plain("Saturday", "Samstag"), orange.id),
            WeekdayDefinition(WeekdayIds.SUNDAY, ContentText.plain("Sunday", "Sonntag"), pink.id)
        )
        return BundledContentRepository(version, definitions + SeasonContent.definitions, WeekdayIds.canonicalOrder, SeasonContent.images)
    }
}
