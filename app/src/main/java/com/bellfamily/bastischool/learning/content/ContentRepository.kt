package com.bellfamily.bastischool.learning.content

import com.bellfamily.bastischool.learning.models.*
import java.util.Collections

interface ContentRepository {
    val version: ContentVersion
    /** Stable semantic-ID order, independent of source insertion order. */
    fun all(): List<ContentDefinition>
    fun find(id: ContentId): ContentDefinition?
    /** Monday through Sunday; callers must not infer sequence from all(). */
    fun weekdays(): List<WeekdayDefinition>
    fun seasons(): List<SeasonDefinition>
    fun images(): List<LocalImageAsset>
    fun image(id: AssetId): LocalImageAsset?
    fun previousDay(id: ContentId): WeekdayDefinition
    fun nextDay(id: ContentId): WeekdayDefinition
}

/** Validate once at the boundary. No UI, platform, persistence or network dependency. */
class BundledContentRepository(
    override val version: ContentVersion,
    definitions: List<ContentDefinition>,
    weekdayOrder: List<ContentId>,
    images: List<LocalImageAsset> = emptyList()
) : ContentRepository {
    private val assetRecords = images.toList()
    private val sortedImages: List<LocalImageAsset>
    private val orderedSeasons: List<SeasonDefinition>
    private val records = definitions.toList()
    private val order = weekdayOrder.toList()
    private val byId: Map<ContentId, ContentDefinition>
    private val orderedDays: List<WeekdayDefinition>
    private val sortedRecords: List<ContentDefinition>

    init {
        val errors = ContentValidator.validate(records, order, assetRecords)
        require(errors.isEmpty()) { errors.joinToString("; ") }
        byId = records.associateBy { it.id }
        sortedImages = Collections.unmodifiableList(assetRecords.sortedBy { it.id.value })
        orderedSeasons = Collections.unmodifiableList(SeasonIds.canonicalOrder.mapNotNull { byId[it] as? SeasonDefinition })
        orderedDays = Collections.unmodifiableList(order.map { byId.getValue(it) as WeekdayDefinition })
        sortedRecords = Collections.unmodifiableList(records.sortedBy { it.id.value })
    }
    override fun all(): List<ContentDefinition> = sortedRecords
    override fun find(id: ContentId): ContentDefinition? = byId[id]
    override fun weekdays(): List<WeekdayDefinition> = orderedDays
    override fun seasons(): List<SeasonDefinition> = orderedSeasons
    override fun images(): List<LocalImageAsset> = sortedImages
    override fun image(id: AssetId): LocalImageAsset? = sortedImages.find { it.id == id }
    override fun previousDay(id: ContentId): WeekdayDefinition = neighbour(id, -1)
    override fun nextDay(id: ContentId): WeekdayDefinition = neighbour(id, 1)

    private fun neighbour(id: ContentId, offset: Int): WeekdayDefinition {
        val index = order.indexOf(id)
        require(index >= 0) { "Not a weekday in this repository: ${id.value}" }
        return orderedDays[(index + offset + order.size) % order.size]
    }
}

object ContentValidator {
    /** Deterministic authoring diagnostics; bilingual fields are validated by LocalizedText itself. */
    fun validate(definitions: List<ContentDefinition>, weekdayOrder: List<ContentId>, images: List<LocalImageAsset> = emptyList()): List<String> {
        val errors = mutableListOf<String>()
        definitions.groupBy { it.id }.filterValues { it.size > 1 }.keys.sortedBy { it.value }.forEach {
            errors += "Duplicate content ID: ${it.value}"
        }
        val byId = definitions.associateBy { it.id }
        val days = definitions.filterIsInstance<WeekdayDefinition>()
        if (days.isNotEmpty() || weekdayOrder.isNotEmpty()) {
            if (weekdayOrder != WeekdayIds.canonicalOrder) errors += "Weekday order must be Monday through Sunday, exactly once"
            if (days.map { it.id }.toSet() != WeekdayIds.canonicalOrder.toSet()) {
                errors += "Weekday definitions must contain exactly the seven canonical day IDs"
            }
        }
        weekdayOrder.forEach { id ->
            if (byId[id] !is WeekdayDefinition) errors += "Weekday order reference is missing or not a weekday: ${id.value}"
        }
        days.sortedBy { it.id.value }.forEach { day ->
            if (byId[day.colourCue] !is ColourDefinition) {
                errors += "${day.id.value}: colour cue is missing or not a colour: ${day.colourCue.value}"
            }
        }
        images.groupBy { it.id }.filterValues { it.size > 1 }.keys.sortedBy { it.value }.forEach {
            errors += "Duplicate asset ID: ${it.value}"
        }
        val assetIds = images.map { it.id }.toSet()
        val seasons = definitions.filterIsInstance<SeasonDefinition>()
        if (seasons.isNotEmpty() && seasons.map { it.id }.toSet() != SeasonIds.canonicalOrder.toSet()) {
            errors += "Season definitions must contain exactly the four canonical season IDs"
        }
        seasons.sortedBy { it.id.value }.forEach {
            if (it.illustration !in assetIds) errors += "${it.id.value}: missing image reference: ${it.illustration.value}"
        }
        return errors.toList()
    }
}
