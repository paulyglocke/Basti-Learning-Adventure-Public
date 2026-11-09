package com.bellfamily.bastischool

/** Explicit current policy wins; older installations may only have the sound Boolean. */
internal fun shellAudioMode(explicit:String?,legacySound:Boolean):String =
    explicit ?: if(legacySound) "all" else "off"
