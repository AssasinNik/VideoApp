package com.cherenkov.videoapp

import android.app.Application
import com.cherenkov.videoapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class VideoApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VideoApp)
            androidLogger()
            modules(appModule)
        }
    }
}