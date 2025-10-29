package com.example.o2scratch

import android.app.Application
import com.example.o2scratch.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class O2ScratchApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@O2ScratchApplication)
            modules(appModule)
        }
    }
}

