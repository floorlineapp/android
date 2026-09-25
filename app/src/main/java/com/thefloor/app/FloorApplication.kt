package com.thefloor.app

import android.app.Application
import com.thefloor.app.core.demo.DemoStore
import com.thefloor.app.referral.InstallReferrerHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class FloorApplication : Application() {
    @Inject lateinit var installReferrerHandler: InstallReferrerHandler

    @Inject lateinit var demoStore: DemoStore

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        demoStore.restoreBlocking()
        installReferrerHandler.captureOnce()
    }
}
