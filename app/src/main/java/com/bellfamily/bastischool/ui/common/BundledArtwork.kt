package com.bellfamily.bastischool.ui.common

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Shared by Home thumbnails and illustrated sequencing cards; decoded off the UI thread. */
@Composable
fun BundledArtwork(path:String,modifier:Modifier=Modifier,maxDimension:Int=384) {
    val assets=LocalContext.current.applicationContext.assets
    var picture by remember(assets,path,maxDimension) {mutableStateOf<ImageBitmap?>(null)}
    LaunchedEffect(assets,path,maxDimension) {
        picture=withContext(Dispatchers.IO) {BundledArtworkCache.load(assets,path,maxDimension)}
    }
    Box(modifier) {
        picture?.let {Image(it,null,Modifier.fillMaxSize().testTag("bundled-art-$path"),contentScale=ContentScale.Fit)}
    }
}

internal object BundledArtworkCache {
    private val cache=object:LruCache<String,ImageBitmap>(8*1024*1024) {
        override fun sizeOf(key:String,value:ImageBitmap)=value.width*value.height*4
    }
    @Synchronized fun load(assets:AssetManager,path:String,maxDimension:Int):ImageBitmap? {
        require(maxDimension in 32..512)
        val key="$maxDimension:$path"
        cache.get(key)?.let {return it}
        return try {
            val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true}
            assets.open(path).use {BitmapFactory.decodeStream(it,null,bounds)}
            require(bounds.outWidth>0 && bounds.outHeight>0)
            var sample=1
            while((maxOf(bounds.outWidth,bounds.outHeight).toLong()+sample-1)/sample>maxDimension)sample*=2
            assets.open(path).use {BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=sample})}
                ?.asImageBitmap()?.also {cache.put(key,it)}
        } catch(_:IOException) {null} catch(_:IllegalArgumentException) {null}
    }
}
