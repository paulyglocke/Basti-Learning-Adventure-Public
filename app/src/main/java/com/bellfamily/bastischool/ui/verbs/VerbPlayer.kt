package com.bellfamily.bastischool.ui.verbs

import android.database.ContentObserver
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.verbs.*
import com.bellfamily.bastischool.ui.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun VerbPlayer(lesson:VerbLesson,language:ContentLanguage,onInterrupt:()->Unit) {
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    val assets=LocalContext.current.applicationContext.assets
    var retry by remember(lesson.id){mutableIntStateOf(0)}
    var result by remember(lesson.id) {mutableStateOf<Result<android.graphics.Bitmap>?>(null)}
    LaunchedEffect(lesson.id,retry) {
        result=null
        result=withContext(Dispatchers.IO) {runCatching {
            assets.open(lesson.motion.asset).use {requireNotNull(BitmapFactory.decodeStream(it))}.also {
                require(it.hasAlpha() && it.width%lesson.motion.columns==0 && it.height%lesson.motion.rows==0 && maxOf(it.width,it.height)<=2048)
            }
        }}
    }
    var playing by remember(lesson.id){mutableStateOf(false)}
    var autoPending by rememberSaveable(lesson.id.value){mutableStateOf(true)}
    var elapsed by rememberSaveable(lesson.id.value){mutableLongStateOf(0)}
    var pictures by rememberSaveable(lesson.id.value){mutableStateOf(false)}
    val resolver=LocalContext.current.contentResolver
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    fun motionAllowed()=Settings.Global.getFloat(resolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0
    var allowed by remember {mutableStateOf(motionAllowed())}
    var resumed by remember {mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
    val interrupt by rememberUpdatedState(onInterrupt)
    DisposableEffect(lifecycle,resolver) {
        val observer=LifecycleEventObserver {_,_->
            resumed=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            allowed=motionAllowed()
            if(!resumed){playing=false;autoPending=false;interrupt()}
        }
        val settings=object:ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange:Boolean){allowed=motionAllowed();if(!allowed){playing=false;autoPending=false;interrupt()}}
        }
        lifecycle.addObserver(observer);resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),false,settings)
        onDispose {playing=false;autoPending=false;lifecycle.removeObserver(observer);resolver.unregisterContentObserver(settings);interrupt()}
    }
    LaunchedEffect(language){playing=false;interrupt()}
    val bitmap=result?.getOrNull();val showPictures=pictures||!allowed
    LaunchedEffect(bitmap) {
        if(bitmap!=null && autoPending){autoPending=false;if(resumed&&allowed&&!showPictures)playing=true}
    }
    LaunchedEffect(playing,resumed,allowed,lesson.id) {
        if(playing&&resumed&&allowed&&bitmap!=null&&!showPictures) {
            var previous:Long?=null
            while(playing){val now=withFrameNanos{it};previous?.let{elapsed=(elapsed+(now-it)/1_000_000).coerceAtMost(lesson.motion.duration-1L)};previous=now
                if(elapsed>=lesson.motion.duration-1L)playing=false}
        }
    }
    if(bitmap!=null) {
        if(showPictures) {
            Text(t("Picture steps","Bildschritte"),style=MaterialTheme.typography.titleLarge)
            NativeChoiceRows((0..3).toList()) { i ->
                Column(Modifier.weight(1f)) {
                    Canvas(Modifier.fillMaxWidth().aspectRatio(16f/9).testTag("native-verb-picture-$i").semantics {contentDescription=lesson.stages[i][language]}) {
                        drawIntoCanvas {VerbScene(it.nativeCanvas).draw(lesson,bitmap,size.width,size.height,VerbTimeline.stillTimes(lesson.motion)[i],lesson.motion.still[i])}
                    }
                    Text("${i+1}. ${lesson.stages[i][language]}")
                }
            }
        } else Canvas(Modifier.fillMaxWidth().aspectRatio(16f/9).heightIn(min=160.dp).testTag("native-verb-stage").semantics {
            contentDescription=lesson.example.display[language];stateDescription=if(playing)t("Playing","Wird abgespielt") else t("Paused","Pausiert")
        }) {drawIntoCanvas {VerbScene(it.nativeCanvas).draw(lesson,bitmap,size.width,size.height,elapsed.toFloat()/lesson.motion.duration)}}
        NativeChoiceRows(listOf("play","pause","pictures")) { action ->
            NativeActionButton(when(action){"play"->t("Watch again","Noch einmal ansehen");"pause"->t("Pause","Pause");else->if(showPictures)t("Show movement","Bewegung zeigen") else t("Picture steps","Bildschritte")},
                NativeActionRole.SECONDARY,{
                    autoPending=false;interrupt()
                    when(action){"play"->{elapsed=0;playing=true};"pause"->playing=false;else->{playing=false;pictures=!pictures}}
                },Modifier.weight(1f).testTag("native-verb-$action"),when(action){"play"->allowed&&!showPictures;"pause"->playing;else->allowed})
        }
    } else {
        Text(if(result==null)t("Opening pictures…","Bilder werden geöffnet…") else t("The pictures could not open. Try again or go back.","Die Bilder konnten nicht geöffnet werden. Versuche es erneut oder gehe zurück."))
        if(result!=null)NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,{retry++})
    }
}
