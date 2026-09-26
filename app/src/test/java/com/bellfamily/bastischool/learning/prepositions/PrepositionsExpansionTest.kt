package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import org.junit.Assert.*
import org.junit.Test

class PrepositionsExpansionTest {
    @Test fun catalogueExactlyMatchesManifestPathsDimensionsAndHashes() {
        val assets = File("src/main/assets")
        val manifest = File(assets, "Prepositions/metadata/prepositions_art_manifest.json").readText()
        val records = Regex("\\{\\s*\"scene_id\".*?\\}", RegexOption.DOT_MATCHES_ALL).findAll(manifest).map { it.value }.toList()
        fun field(record: String, key: String) = Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"").find(record)!!.groupValues[1]
        assertEquals(52, records.size)
        assertEquals(52, PrepositionsContent.scenes.map { it.taskId }.toSet().size)
        assertEquals(records.map { field(it,"scene_id") }, PrepositionsContent.scenes.map { it.id.value })
        records.forEach { record ->
            val id = ContentId(field(record,"scene_id"))
            val asset = PrepositionsArtwork.image(id)
            assertEquals(field(record,"asset_path"), asset.path)
            val bytes = File(assets,asset.path).readBytes()
            assertArrayEquals(byteArrayOf(-119,80,78,71,13,10,26,10),bytes.copyOfRange(0,8))
            val dimensions = ByteBuffer.wrap(bytes,16,8)
            assertEquals(1448,dimensions.int); assertEquals(1086,dimensions.int)
            assertEquals(2,bytes[25].toInt()) // RGB, not indexed or RGBA.
            assertEquals(field(record,"sha256"), MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) })
        }
        assertEquals(records.map { field(it,"asset_path") }.toSet(), File(assets,"Prepositions/scenes").listFiles()!!.map { "Prepositions/scenes/${it.name}" }.toSet())
        assertThrows(IllegalArgumentException::class.java) { PrepositionsArtwork.image(ContentId("scene.prepositions.bee.in")) }
    }

    @Test fun exactNonCartesianAnimalSetsAndNewSubjects() {
        val land=setOf("snake","dinosaur","dragon","crocodile")
        val flying=setOf("bird","bee","butterfly","dragon")
        val sea=setOf("fish","turtle","octopus","seahorse")
        PositionRelation.entries.forEach { relation ->
            val expected=when(relation) {
                PositionRelation.ABOVE,PositionRelation.BELOW -> flying
                PositionRelation.INSIDE,PositionRelation.OUTSIDE -> sea
                else -> land
            }
            assertEquals(expected,PrepositionsContent.scenes.filter {it.relation==relation}.map {it.animal.key}.toSet())
        }
        val subjects=listOf("bird|the bird|der Vogel","bee|the bee|die Biene","butterfly|the butterfly|der Schmetterling",
            "fish|the fish|der Fisch","turtle|the turtle|die Schildkröte","octopus|the octopus|der Oktopus","seahorse|the seahorse|das Seepferdchen")
        subjects.forEach { val (key,en,de)=it.split('|'); assertEquals(LocalizedText(en,de),PositionAnimal.entries.single {a->a.key==key}.subject) }
        assertThrows(IllegalArgumentException::class.java) {
            PrepositionsContent.question(PositionScene(PositionAnimal.BEE,PositionRelation.IN),listOf("in","on","under","behind").map {ContentId("position.$it")})
        }
    }

    @Test fun allBilingualLabelsPhrasesAndNewSentencesAreAuthoredExactly() {
        val rows=listOf(
            "on|on|auf|on the rock|auf dem Stein", "under|under|unter|under the table|unter dem Tisch",
            "behind|behind|hinter|behind the rock|hinter dem Stein", "next_to|next to|neben|next to the rock|neben dem Stein",
            "in|in|in|in the box|in der Kiste", "between|between|zwischen|between the two rocks|zwischen den beiden Steinen",
            "above|above|über|above the cloud|über der Wolke", "below|below|unterhalb|below the cloud|unterhalb der Wolke",
            "inside|inside|drinnen|inside the cave|in der Höhle", "outside|outside|draußen|outside the cave|außerhalb der Höhle",
            "in_front_of|in front of|vor|in front of the rock|vor dem Stein", "near|near|in der Nähe|near the rock|in der Nähe des Steins",
            "far_from|far from|weit weg|far from the rock|weit weg vom Stein")
        rows.forEach { val r=it.split('|'); val relation=PositionRelation.entries.single {v->v.key==r[0]}
            assertEquals(LocalizedText(r[1],r[2]),relation.label);assertEquals(LocalizedText(r[3],r[4]),relation.phrase)
        }
        val sentences=listOf(
            "bird.above|The bird is above the cloud.|Der Vogel ist über der Wolke.",
            "bee.below|The bee is below the cloud.|Die Biene ist unterhalb der Wolke.",
            "fish.inside|The fish is inside the cave.|Der Fisch ist in der Höhle.",
            "turtle.outside|The turtle is outside the cave.|Die Schildkröte ist außerhalb der Höhle.",
            "dinosaur.in_front_of|The dinosaur is in front of the rock.|Der Dinosaurier ist vor dem Stein.",
            "dragon.near|The dragon is near the rock.|Der Drache ist in der Nähe des Steins.",
            "crocodile.far_from|The crocodile is far from the rock.|Das Krokodil ist weit weg vom Stein.")
        sentences.forEach {val (key,en,de)=it.split('|');assertEquals(LocalizedText(en,de),PrepositionsContent.scenes.single {s->s.id.value=="scene.prepositions.$key"}.description)}
    }

    @Test fun seededRoundsReachEveryRelationWithDistinctBilingualNonCompetingChoices() {
        val reached=mutableSetOf<ContentId>()
        for(round in RoundLength.entries) for(seed in 0L..199L) {
            val plan=PrepositionsContentTest.plan(round,seed)
            assertEquals(2,plan.activityRevision);assertEquals(ContentVersion(1,2),plan.contentVersion)
            assertEquals(round.count,plan.tasks.map {it.question.definition}.toSet().size)
            val again=PrepositionsContentTest.plan(round,seed)
            assertArrayEquals(SessionCheckpoint.encode(SessionReducer.start(plan,ContentLanguage.ENGLISH,PrepositionsContent.repository).state),
                SessionCheckpoint.encode(SessionReducer.start(again,ContentLanguage.ENGLISH,PrepositionsContent.repository).state))
            plan.tasks.forEach {task ->
                val ids=task.question.choices;assertEquals(4,ids.toSet().size);assertTrue(task.question.correct in ids)
                reached+=task.question.correct
                val relations=ids.map {id->PositionRelation.entries.single {it.id==id}}
                assertEquals(4,relations.map {it.label.en}.toSet().size);assertEquals(4,relations.map {it.label.de}.toSet().size)
                relations.forEachIndexed {i,a->relations.drop(i+1).forEach {b->assertTrue(PrepositionsContent.compatible(a,b))}}
            }
        }
        assertEquals(PositionRelation.entries.map {it.id}.toSet(),reached)
    }
}
