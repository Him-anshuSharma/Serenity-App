package com.serenity

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.com.serenity.BuildConfig

@HiltAndroidApp
class SerenityApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
} 