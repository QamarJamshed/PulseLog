package com.example.pulselog

import android.app.Application
import com.example.pulselog.di.initKoin
import org.koin.android.ext.koin.androidContext

class PulseLogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@PulseLogApplication)
        }
    }
}
