package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.session.SessionId
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SeasonsMatchTest {
    @get:Rule val temp = TemporaryFolder()
    @Test fun deterministicRoundHasFourSeasonsAndOneSelectedCluePerSeason() {
        val a=SeasonsMatch.start(SessionId("match"),42,ContentLanguage.ENGLISH)
        val b=SeasonsMatch.start(SessionId("match"),42,ContentLanguage.ENGLISH)
        assertEquals(a.order,b.order); assertEquals(SeasonIds.canonicalOrder.toSet(),a.rule.items.map {it.id}.toSet())
        assertEquals(4, a.rule.categories.toSet().size)
        assertEquals(4, SeasonsClues.catalogue.map {it.correct}.toSet().size)
        assertEquals(4, SeasonsClues.catalogue.map {it.definition}.toSet().size.coerceAtMost(4))
    }
    @Test fun incorrectMatchRetainsSelectionAndCorrectMatchLocksIt() {
        var s=SeasonsMatch.start(SessionId("match"),1,ContentLanguage.GERMAN)
        val season=s.order.first(); val correct=s.rule.items.first {it.id==season}.category; val wrong=s.rule.categories.first {it!=correct}
        s=SeasonsMatch.reduce(s,SortAction.Select(season)).state
        s=SeasonsMatch.reduce(s,SortAction.Place(s.id,season,wrong,1)).state
        assertFalse(s.placement(season).placed); assertEquals(season,s.selected); assertEquals(1,s.placement(season).attempts)
        s=SeasonsMatch.reduce(s,SortAction.Hint).state
        assertTrue(s.selectedPlacement!!.support.hint)
        s=SeasonsMatch.reduce(s,SortAction.Place(s.id,season,correct,2)).state
        assertTrue(s.placement(season).placed); assertNull(s.selected)
    }
    @Test fun exactPartialStateRestoresThroughSortingJournal() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit); val progress=ProgressFixtures.repository(temp.newFolder())
        fun host()=SortingHost(disk,progress,SeasonsMatch)
        var h=host();h.open(SessionId("match"),42,ContentLanguage.ENGLISH)
        val season=h.state!!.order.first();h.dispatch(SortAction.Select(season)); val clue=h.state!!.rule.items.first {it.id==season}.category
        h.dispatch(SortAction.Place(h.state!!.id,season,clue,1)); val saved=SortingHost.encode(SeasonsMatch,h.state!!,emptyList())
        h=host();assertTrue(h.open(SessionId("other"),0,ContentLanguage.GERMAN).let {it==null}); assertArrayEquals(saved,SortingHost.encode(SeasonsMatch,h.state!!,emptyList()))
    }
    @Test fun languageAndCompletionRemainSupported() {
        var s=SeasonsMatch.start(SessionId("match"),2,ContentLanguage.ENGLISH)
        s=Sorting.reduce(s,SortAction.Language(ContentLanguage.GERMAN)); assertEquals(ContentLanguage.GERMAN,s.language)
        s.order.forEach {season -> s=Sorting.reduce(s,SortAction.Select(season)); s=Sorting.reduce(s,SortAction.Place(s.id,season,s.rule.items.first {it.id==season}.category,1))}
        assertTrue(s.completed)
    }
}
