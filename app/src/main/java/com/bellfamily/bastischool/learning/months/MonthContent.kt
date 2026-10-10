package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import java.util.Collections
import kotlin.math.atan2
import kotlin.math.hypot

object MonthIds {
    val JANUARY = ContentId("month.january")
    val FEBRUARY = ContentId("month.february")
    val MARCH = ContentId("month.march")
    val APRIL = ContentId("month.april")
    val MAY = ContentId("month.may")
    val JUNE = ContentId("month.june")
    val JULY = ContentId("month.july")
    val AUGUST = ContentId("month.august")
    val SEPTEMBER = ContentId("month.september")
    val OCTOBER = ContentId("month.october")
    val NOVEMBER = ContentId("month.november")
    val DECEMBER = ContentId("month.december")
    val canonicalOrder: List<ContentId> = Collections.unmodifiableList(listOf(JANUARY, FEBRUARY, MARCH,
        APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER))
    fun next(id: ContentId) = adjacent(id, 1)
    fun previous(id: ContentId) = adjacent(id, -1)
    private fun adjacent(id: ContentId, delta: Int): ContentId {
        val index = canonicalOrder.indexOf(id)
        require(index >= 0)
        return canonicalOrder[Math.floorMod(index + delta, 12)]
    }
}

object MonthContent {
    const val ACTIVITY = "activity.months"
    private fun month(id: ContentId, en: String, de: String, shortEn: String, shortDe: String, season: ContentId) =
        MonthDefinition(id, ContentText.plain(en, de), LocalizedText(shortEn, shortDe), season)
    val definitions: List<MonthDefinition> = Collections.unmodifiableList(listOf(
        month(MonthIds.JANUARY, "January", "Januar", "Jan", "Jan", SeasonIds.WINTER),
        month(MonthIds.FEBRUARY, "February", "Februar", "Feb", "Feb", SeasonIds.WINTER),
        month(MonthIds.MARCH, "March", "März", "Mar", "Mär", SeasonIds.SPRING),
        month(MonthIds.APRIL, "April", "April", "Apr", "Apr", SeasonIds.SPRING),
        month(MonthIds.MAY, "May", "Mai", "May", "Mai", SeasonIds.SPRING),
        month(MonthIds.JUNE, "June", "Juni", "Jun", "Jun", SeasonIds.SUMMER),
        month(MonthIds.JULY, "July", "Juli", "Jul", "Jul", SeasonIds.SUMMER),
        month(MonthIds.AUGUST, "August", "August", "Aug", "Aug", SeasonIds.SUMMER),
        month(MonthIds.SEPTEMBER, "September", "September", "Sep", "Sep", SeasonIds.AUTUMN),
        month(MonthIds.OCTOBER, "October", "Oktober", "Oct", "Okt", SeasonIds.AUTUMN),
        month(MonthIds.NOVEMBER, "November", "November", "Nov", "Nov", SeasonIds.AUTUMN),
        month(MonthIds.DECEMBER, "December", "Dezember", "Dec", "Dez", SeasonIds.WINTER)
    ))
    val repository = BundledContentRepository(ContentVersion(1, 1), definitions, emptyList())
    private val seasons = CoreContent.repository()
    fun month(id: ContentId) = definitions.single { it.id == id }
    fun season(id: ContentId) = seasons.find(id) as SeasonDefinition
    val title = ContentText.plain("Months", "Monate")
    val instruction = ContentText.plain("Choose a month on the wheel or below. Listen to its name.",
        "Wähle einen Monat im Jahresrad oder unten. Höre dir seinen Namen an.")
    val convention = ContentText.plain(
        "For grown-ups: we use meteorological seasons in Germany, with three months in each season. Weather can be different.",
        "Für Erwachsene: Wir verwenden die meteorologischen Jahreszeiten in Deutschland mit jeweils drei Monaten. Das Wetter kann anders sein.")
}

data class MonthsSelection(val selected: ContentId = MonthIds.JANUARY) {
    init { MonthContent.month(selected) }
}

/** Screen coordinates: January centred at twelve, clockwise. No stored visual-angle state. */
object YearWheelGeometry {
    fun angle(id: ContentId): Float {
        val index = MonthIds.canonicalOrder.indexOf(id)
        require(index >= 0)
        return -90f + index * 30f
    }
    fun hit(x: Float, y: Float, radius: Float): ContentId? {
        if (!x.isFinite() || !y.isFinite() || !radius.isFinite() || radius <= 0) return null
        val distance = hypot(x, y)
        if (distance < radius * .48f || distance > radius) return null
        val angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()))
        val index = Math.floorMod(kotlin.math.floor((angle + 105) / 30).toInt(), 12)
        return MonthIds.canonicalOrder[index]
    }
}
