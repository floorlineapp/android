package com.thefloor.app

import android.app.Application
import com.thefloor.app.referral.InstallReferrerHandler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class FloorApplication : Application() {

    @Inject lateinit var installReferrerHandler: InstallReferrerHandler

    @Inject lateinit var demoStore: com.thefloor.app.core.demo.DemoStore

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Demo mode must be known before the first API call leaves the app.
        demoStore.restoreBlocking()
        // Referral attribution capture — async, once per install, never blocks launch.
        installReferrerHandler.captureOnce()
    }
}
