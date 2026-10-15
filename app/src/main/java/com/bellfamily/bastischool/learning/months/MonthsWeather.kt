package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentText
import java.util.Collections

/** Educational examples for Germany/Central Europe, never a forecast or an exclusive rule. */
enum class MonthsWeather(val phrase: ContentText) {
    SNOW(ContentText.plain("It's snowing!", "Es schneit!")),
    FROST(ContentText.plain("It's frosty!", "Es ist frostig!")),
    LIGHT_RAIN(ContentText.plain("It's raining gently!", "Es regnet leicht!")),
    RAIN(ContentText.plain("It's raining!", "Es regnet!")),
    SUNSHINE(ContentText.plain("It's sunny!", "Es ist sonnig!")),
    WIND(ContentText.plain("It's windy!", "Es ist windig!")),
    CLOUDS(ContentText.plain("It's cloudy!", "Es ist bewölkt!")),
    SUN_AND_CLOUDS(ContentText.plain("Sunshine and clouds!", "Sonne und Wolken!"))
}

object MonthsWeatherContent {
    private fun choices(vararg values: MonthsWeather): List<MonthsWeather> =
        Collections.unmodifiableList(values.toList())

    private val byMonth = mapOf(
        MonthIds.JANUARY to choices(MonthsWeather.SNOW, MonthsWeather.FROST, MonthsWeather.CLOUDS, MonthsWeather.RAIN),
        MonthIds.FEBRUARY to choices(MonthsWeather.FROST, MonthsWeather.SNOW, MonthsWeather.SUNSHINE, MonthsWeather.RAIN),
        MonthIds.MARCH to choices(MonthsWeather.WIND, MonthsWeather.LIGHT_RAIN, MonthsWeather.CLOUDS, MonthsWeather.SUNSHINE),
        MonthIds.APRIL to choices(MonthsWeather.RAIN, MonthsWeather.SUNSHINE, MonthsWeather.CLOUDS, MonthsWeather.WIND, MonthsWeather.LIGHT_RAIN),
        MonthIds.MAY to choices(MonthsWeather.SUNSHINE, MonthsWeather.LIGHT_RAIN, MonthsWeather.CLOUDS, MonthsWeather.WIND),
        MonthIds.JUNE to choices(MonthsWeather.SUNSHINE, MonthsWeather.CLOUDS, MonthsWeather.RAIN),
        MonthIds.JULY to choices(MonthsWeather.SUNSHINE, MonthsWeather.CLOUDS, MonthsWeather.RAIN),
        MonthIds.AUGUST to choices(MonthsWeather.SUNSHINE, MonthsWeather.RAIN, MonthsWeather.CLOUDS),
        MonthIds.SEPTEMBER to choices(MonthsWeather.SUN_AND_CLOUDS, MonthsWeather.LIGHT_RAIN, MonthsWeather.WIND, MonthsWeather.CLOUDS),
        MonthIds.OCTOBER to choices(MonthsWeather.WIND, MonthsWeather.RAIN, MonthsWeather.CLOUDS, MonthsWeather.SUNSHINE),
        MonthIds.NOVEMBER to choices(MonthsWeather.RAIN, MonthsWeather.CLOUDS, MonthsWeather.FROST, MonthsWeather.WIND),
        MonthIds.DECEMBER to choices(MonthsWeather.SNOW, MonthsWeather.FROST, MonthsWeather.RAIN, MonthsWeather.CLOUDS)
    )

    val variability = ContentText.plain(
        "Weather can change in every month. Try another kind of weather!",
        "Das Wetter kann sich in jedem Monat ändern. Probiere ein anderes Wetter aus!")

    fun choices(month: ContentId): List<MonthsWeather> = byMonth.getValue(month)
    fun default(month: ContentId) = choices(month).first()
    fun next(month: ContentId, current: MonthsWeather): MonthsWeather {
        val options = choices(month)
        require(current in options)
        return options[(options.indexOf(current) + 1) % options.size]
    }

    fun narration(month: ContentId, weather: MonthsWeather): ContentText {
        require(weather in choices(month))
        val definition = MonthContent.month(month)
        val season = MonthContent.season(definition.season)
        return ContentText.plain(
            "${definition.text.speech.en}. ${season.text.speech.en}. ${weather.phrase.speech.en} ${variability.speech.en}",
            "${definition.text.speech.de}. ${season.text.speech.de}. ${weather.phrase.speech.de} ${variability.speech.de}")
    }
}
