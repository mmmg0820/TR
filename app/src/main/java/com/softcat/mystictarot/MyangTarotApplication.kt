package com.softcat.mystictarot

import android.app.Application
import android.content.ComponentCallbacks2
import com.softcat.mystictarot.ui.components.clearTarotBitmapCache

class MyangTarotApplication : Application() {
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            clearTarotBitmapCache()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        clearTarotBitmapCache()
    }
}
