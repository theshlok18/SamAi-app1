package dev.shlok.samai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
