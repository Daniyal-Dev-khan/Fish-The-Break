package com.cp.fishthebreak.di

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.facebook.FacebookSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        appContext = this
        //FacebookSdk.sdkInitialize(appContext)
    }

    companion object {
        lateinit var appContext: Context
    }
}