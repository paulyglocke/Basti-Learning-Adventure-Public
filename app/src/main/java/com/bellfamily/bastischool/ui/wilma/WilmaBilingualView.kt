package com.bellfamily.bastischool.ui.wilma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.wilma.WilmaContent

/** Paired rows keep both vertical Wilmas aligned even when one label wraps. Parent owns scrolling. */
@Composable
internal fun WilmaBilingualView(images:Map<String,ImageBitmap>, language:ContentLanguage,
    enabled:Boolean, onDay:(ContentId,ContentLanguage)->Unit) {
    val de=language==ContentLanguage.GERMAN
    Text(if(de) "Tippe auf einen Tag, um ihn auf Deutsch oder Englisch zu hören."
        else "Tap a day to hear it in German or English.")
    // Side assignment is part of the learning material, independent of device layout direction.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(Modifier.fillMaxWidth().testTag("wilma-bilingual").semantics { isTraversalGroup=true }) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                listOf("Deutsch", "English").forEach { label ->
                    Text(label,style=MaterialTheme.typography.titleLarge,textAlign=TextAlign.Center,
                        modifier=Modifier.weight(1f).testTag("wilma-language-$label").semantics { heading() })
                }
            }
            BilingualDecoration(images[WilmaContent.HEAD],"head",112)
            WilmaContent.days.forEach { day ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    listOf(ContentLanguage.GERMAN,ContentLanguage.ENGLISH).forEach { spokenLanguage ->
                        val label=WilmaContent.day(day).text.display[spokenLanguage]
                        val side=if(spokenLanguage==ContentLanguage.GERMAN) "de" else "en"
                        val languageName=if(spokenLanguage==ContentLanguage.GERMAN) {
                            if(de) "Deutsch" else "German"
                        } else if(de) "Englisch" else "English"
                        val cue=WilmaDayColours.background(day,enabled,MaterialTheme.colorScheme.surface)
                        Column(Modifier.weight(1f).fillMaxHeight().heightIn(min=64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled=enabled,role=Role.Button,
                                onClickLabel=if(de) "Auf $languageName anhören" else "Listen in $languageName",
                                onClick={onDay(day,spokenLanguage)})
                            .testTag("wilma-bilingual-$side-${day.value}")
                            .semantics(mergeDescendants=true) { contentDescription="$label, $languageName" },
                            horizontalAlignment=Alignment.CenterHorizontally) {
                            images[WilmaContent.image(day)]?.let { bitmap ->
                                Image(bitmap,null,Modifier.size(88.dp),contentScale=ContentScale.Fit)
                            }
                            Box(Modifier.fillMaxWidth().weight(1f).heightIn(min=64.dp)
                                .background(cue,RoundedCornerShape(12.dp)).padding(horizontal=4.dp,vertical=12.dp),
                                contentAlignment=Alignment.Center) {
                                Text(label,style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center,
                                    color=WilmaDayColours.foreground(cue),modifier=Modifier.clearAndSetSemantics {})
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            BilingualDecoration(images[WilmaContent.TAIL],"tail",48)
        }
    }
}

@Composable
private fun BilingualDecoration(bitmap:ImageBitmap?,part:String,size:Int) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        listOf("de","en").forEach { side ->
            Box(Modifier.weight(1f),contentAlignment=Alignment.Center) {
                bitmap?.let { Image(it,null,Modifier.size(size.dp).testTag("wilma-bilingual-$part-$side"),contentScale=ContentScale.Fit) }
            }
        }
    }
}
