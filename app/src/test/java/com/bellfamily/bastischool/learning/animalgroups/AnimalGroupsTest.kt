package com.bellfamily.bastischool.learning.animalgroups

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import com.bellfamily.bastischool.learning.session.SessionId

class AnimalGroupsTest {
    private fun start(seed:Long=1)=AnimalGroups.start(SessionId("animals"),seed,ContentLanguage.ENGLISH)
    @Test fun exactMembershipBilingualNamesHelpAndCanonicalPngs() {
        assertEquals(listOf("animal.whale","animal.dolphin","animal.horse","animal.rabbit"),AnimalGroups.objects.map {it.id.value})
        assertEquals(listOf(AnimalGroups.water,AnimalGroups.water,AnimalGroups.land,AnimalGroups.land),AnimalGroups.objects.map {it.category})
        assertEquals(listOf("Whale","Dolphin","Horse","Rabbit"),AnimalGroups.objects.map {it.text.display[ContentLanguage.ENGLISH]})
        assertEquals(listOf("Wal","Delfin","Pferd","Kaninchen"),AnimalGroups.objects.map {it.text.display[ContentLanguage.GERMAN]})
        assertEquals(listOf("The whale lives in water.","The dolphin lives in water.","The horse lives on land.","The rabbit lives on land."),AnimalGroups.objects.map {it.help.display[ContentLanguage.ENGLISH]})
        assertEquals(listOf("Der Wal lebt im Wasser.","Der Delfin lebt im Wasser.","Das Pferd lebt an Land.","Das Kaninchen lebt an Land."),AnimalGroups.objects.map {it.help.display[ContentLanguage.GERMAN]})
        assertEquals("Put each animal in the right group.",AnimalGroups.instruction.display[ContentLanguage.ENGLISH])
        assertEquals("Ordne jedes Tier der richtigen Gruppe zu.",AnimalGroups.instruction.display[ContentLanguage.GERMAN])
        assertEquals(listOf("Water","Land"),AnimalGroups.categories.map {AnimalGroups.category(it).display[ContentLanguage.ENGLISH]})
        assertEquals(listOf("Wasser","Land"),AnimalGroups.categories.map {AnimalGroups.category(it).display[ContentLanguage.GERMAN]})
        AnimalGroups.objects.forEach {
            assertEquals("Animals/canonical/${it.id.value.substringAfter('.')}.png",it.image)
            val file=File("src/main/assets",it.image).let {f->if(f.exists()) f else File("app/src/main/assets",it.image)}
            assertTrue(file.isFile)
            assertArrayEquals(byteArrayOf(137.toByte(),80,78,71,13,10,26,10),file.inputStream().use { stream -> ByteArray(8).also { assertEquals(8,stream.read(it)) } })
        }
    }
    @Test fun deterministicOrderAndLanguageNeverChangeMembership() {
        for(seed in 0L..30L){val a=start(seed);val b=start(seed)
            assertEquals(a.order,b.order);assertEquals(4,a.order.toSet().size)
            val de=AnimalGroups.reduce(a,SortAction.Language(ContentLanguage.GERMAN)).state
            assertEquals(a.order,de.order);assertEquals(a.id,de.id)
        }
    }
    @Test fun incorrectPlacementImmediateCorrectionSupportAndFinalCompletion() {
        var s=start();val first=AnimalGroups.objects.first()
        s=AnimalGroups.reduce(s,SortAction.Select(first.id)).state
        val wrong=AnimalGroups.reduce(s,SortAction.Place(s.id,first.id,AnimalGroups.land,1));s=wrong.state
        assertFalse(s.placement(first.id).placed);assertEquals(first.id,s.selected)
        val replay=AnimalGroups.reduce(s,SortAction.Replay);assertTrue(replay.events.isEmpty());s=replay.state
        val help=AnimalGroups.reduce(s,SortAction.Hint);assertTrue(help.events.isEmpty());s=help.state
        assertEquals(1,s.placement(first.id).attempts);assertTrue(s.placement(first.id).support.hint)
        val corrected=AnimalGroups.reduce(s,SortAction.Place(s.id,first.id,first.category,2));s=corrected.state
        assertEquals(AttemptOutcome.CORRECT,(corrected.events.single() as AttemptEvent).outcome)
        assertFalse(s.completed)
        AnimalGroups.objects.drop(1).forEachIndexed {i,o->
            s=AnimalGroups.reduce(s,SortAction.Select(o.id)).state
            val next=AnimalGroups.reduce(s,SortAction.Place(s.id,o.id,o.category,1));s=next.state
            assertEquals(i==2,s.completed)
            if(i==2)assertEquals(1,next.events.filterIsInstance<CompletionEvent>().size)
        }
        assertEquals(2,s.placements[0].attempts);assertTrue(s.placements[0].support.hint)
    }
    @Test fun staleUnknownDuplicateAndCorruptStateAreRejected() {
        var s=start();val item=AnimalGroups.objects.first();s=AnimalGroups.reduce(s,SortAction.Select(item.id)).state
        for(action in listOf(SortAction.Place(SessionId("old"),item.id,item.category,1),
            SortAction.Place(s.id,item.id,ContentId("unknown.item"),1),SortAction.Place(s.id,item.id,item.category,9),SortAction.Select(ContentId("unknown.item"))))
            assertSame(s,AnimalGroups.reduce(s,action).state)
        val action=SortAction.Place(s.id,item.id,item.category,1);s=AnimalGroups.reduce(s,action).state
        assertSame(s,AnimalGroups.reduce(s,action).state)
        assertThrows(IllegalArgumentException::class.java){SortState(s.id,s.rule,s.language,List(4){item.id},s.placements)}
        assertThrows(IllegalArgumentException::class.java){SortState(s.id,s.rule,s.language,s.order,s.placements.drop(1))}
    }
}
