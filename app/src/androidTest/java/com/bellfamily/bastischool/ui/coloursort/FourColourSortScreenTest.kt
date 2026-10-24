package com.bellfamily.bastischool.ui.coloursort

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.bellfamily.bastischool.learning.coloursort.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.SessionId
import com.bellfamily.bastischool.learning.sorting.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FourColourSortScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun fourGroupsSelectionCorrectionAndCompletionAreAccessible() {
        var state by mutableStateOf(FourColourSort.start(SessionId("four-ui"), 5L, ContentLanguage.ENGLISH))
        compose.setContent {
            MaterialTheme { FourColourSortScreen(state, ContentLanguage.ENGLISH, false, false, false,
                { action -> state = FourColourSort.reduce(state, action).state }, {}, {}, {}, Modifier) }
        }
        compose.onNodeWithTag("sort-four-title").assertIsDisplayed()
        assertEquals(8, state.rule.items.size)
        FourColourSort.categories.forEach { category -> compose.onNodeWithTag("sort-four-category-${category.value}").performScrollTo().assertIsDisplayed() }
        val first = FourColourSort.item(state.order.first())
        compose.onNodeWithTag("sort-four-item-${first.id.value}").performScrollTo().assertIsDisplayed().assertHasClickAction().performClick()
        val wrong = FourColourSort.categories.first { it != first.category }
        compose.onNodeWithTag("sort-four-category-${wrong.value}").performScrollTo().performClick()
        assertEquals(first.id, state.selected)
        val correct = state.selected!!
        compose.onNodeWithTag("sort-four-category-${first.category.value}").performScrollTo().performClick()
        assertTrue(state.placement(correct).placed)
        FourColourSort.objects.filter { it.id != correct }.forEach { item ->
            state = FourColourSort.reduce(state, SortAction.Select(item.id)).state
            state = FourColourSort.reduce(state, SortAction.Place(state.id, item.id, item.category, state.placement(item.id).attempts + 1)).state
        }
        compose.waitForIdle()
        compose.onNodeWithText(FourColourSort.completion.display.en).assertIsDisplayed()
    }
}
