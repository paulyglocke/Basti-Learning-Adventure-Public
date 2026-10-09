package com.bellfamily.bastischool

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.prepositions.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.wilma.*
import com.bellfamily.bastischool.learning.vocabulary.*
import com.bellfamily.bastischool.ui.prepositions.PrepositionsScreen
import com.bellfamily.bastischool.ui.seasons.SeasonsScreen
import com.bellfamily.bastischool.ui.wilma.WilmaScreen
import com.bellfamily.bastischool.ui.vocabulary.VocabularyScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CompactTextVisibilityTest(private val language: ContentLanguage, private val scale: Float) {
    @get:Rule val compose=createComposeRule()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}-{1}") fun cases() = ContentLanguage.entries.flatMap { l -> listOf(1f,1.5f).map {arrayOf<Any>(l,it)} }
    }
    private fun check(tag:String) {
        val node=compose.onNodeWithTag(tag).performScrollTo()
        val layouts=mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(layouts)}
        assertTrue("No text layout for $tag",layouts.isNotEmpty())
        layouts.forEach { layout ->
            assertFalse("Truncated $tag: ${layout.layoutInput.text}",layout.multiParagraph.didExceedMaxLines)
            assertTrue("Height $tag",layout.multiParagraph.height<=layout.size.height+1)
            repeat(layout.lineCount) { line ->
                assertFalse(layout.isLineEllipsized(line))
                assertTrue("Right edge $tag: ${layout.layoutInput.text}",layout.getLineRight(line)<=layout.size.width+1)
                assertTrue(layout.getLineLeft(line)>=-1)
            }
            val text=layout.layoutInput.text.text
            for(line in 1 until layout.lineCount) {
                val start=layout.getLineStart(line)
                val end=layout.getLineEnd(line,visibleEnd=true)
                if(start>0 && start<text.length && text[start-1].isLetter() && text[start].isLetter()) {
                    assertTrue("Orphaned word fragment $tag: $text",text.substring(start,end).trim().length>2)
                }
            }
        }
    }
    @Test fun compactActivityControlsShowCompleteReadableWords() {
        var page by mutableIntStateOf(0)
        val pPlan=(PrepositionsContent.generate(SessionId("text-p"),RoundLength.FIVE,42) as GenerationResult.Generated).plan
        val p=SessionReducer.start(pPlan,language,PrepositionsContent.repository).state
        val sPlan=(SeasonsContent.generate(SessionId("text-s"),RoundLength.FIVE,42) as GenerationResult.Generated).plan
        val s=SessionReducer.start(sPlan,language,SeasonsContent.repository).state
        compose.setContent { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,scale)) {
            MaterialTheme { Box(Modifier.width(320.dp)) { when(page) {
                0 -> PrepositionsScreen(p,language,false,false,false,{}, {}, {}, {}, {}, {}, {})
                1 -> SeasonsScreen(SeasonsSelection(phase=SeasonsPhase.PRACTICE),s,language,null,false,false,false,false,{}, {}, {}, {}, {}, {}, {}, {})
                2 -> WilmaScreen(WilmaSelection(),null,null,language,emptyMap(),false,false,false,false,{}, {}, {}, {}, {}, {}, {}, {}, {})
                3 -> VocabularyScreen(VocabularySelection(),null,language,false,false,false,{}, {}, {}, {}, {}, {}, {}, {}, {})
            } } }
        } }
        p.task.question.choices.forEach { check("answer-${it.value}"); check("speaker-${it.value}") }
        compose.runOnIdle {page=1}
        SeasonsPhase.entries.forEach {check(it.tag)}
        s.task.question.choices.forEach {check("answer-${it.value}")}
        compose.runOnIdle {page=2}
        WilmaPhase.entries.forEach {check("wilma-phase-${it.name}")}
        WilmaContent.days.forEach {check("day-${it.value}")}
        compose.runOnIdle {page=3}
        VocabularyContent.items.forEach {check("word-${it.id.value}")}
    }
}
