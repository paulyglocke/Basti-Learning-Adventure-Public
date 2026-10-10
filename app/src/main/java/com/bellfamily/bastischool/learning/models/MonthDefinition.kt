package com.bellfamily.bastischool.learning.models

/** Calendar identity and bilingual words; the season is a meteorological teaching convention. */
data class MonthDefinition(override val id: ContentId, override val text: ContentText,
    val shortLabel: LocalizedText, val season: ContentId) : ContentDefinition
