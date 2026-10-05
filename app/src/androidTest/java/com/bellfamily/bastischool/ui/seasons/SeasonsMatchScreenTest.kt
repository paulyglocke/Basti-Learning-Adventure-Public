package com.bellfamily.bastischool.ui.seasons

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sorting.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SeasonsMatchScreenTest {
    @get:Rule val compose=createComposeRule()
    @Test fun germanNarrowLayoutMatchesAllAfterWrongRetryAndHelp() {
        val state=mutableStateOf(SeasonsMatch.start(SessionId("ui-match"),42,ContentLanguage.GERMAN))
        compose.setContent { MaterialTheme { SeasonsScreen(SeasonsSelection(phase=SeasonsPhase.MATCH),null,ContentLanguage.GERMAN,null,false,false,false,false,{}, {}, {}, {}, {}, {}, {}, {}, modifier=Modifier.width(320.dp), matching=state.value, onMatch={state.value=SeasonsMatch.reduce(state.value,it).state}) } }
        compose.onNodeWithTag("seasons-match-prompt").assertIsDisplayed()
        val first=state.value.order.first();val correct=state.value.rule.items.first {it.id==first}.category;val wrong=state.value.rule.categories.first {it!=correct}
        compose.onNodeWithTag("match-season-${first.value}").performClick(); compose.waitForIdle()
        compose.onNodeWithTag("match-clue-${wrong.value}").performClick(); compose.waitForIdle()
        state.value = SeasonsMatch.reduce(state.value, SortAction.Hint).state
        compose.waitForIdle(); assertTrue(state.value.selectedPlacement!!.support.hint)
        state.value = SeasonsMatch.reduce(state.value, SortAction.Place(state.value.id, first, correct, 2)).state
        compose.waitForIdle(); assertTrue(state.value.placement(first).placed)
        state.value.order.drop(1).forEach { season ->
            state.value = SeasonsMatch.reduce(state.value, SortAction.Select(season)).state
            val clue = state.value.rule.items.first { it.id == season }.category
            state.value = SeasonsMatch.reduce(state.value, SortAction.Place(state.value.id, season, clue, 1)).state
        }
        compose.waitForIdle()
        compose.onNodeWithTag("completion-celebration").assertExists()
    }
}
