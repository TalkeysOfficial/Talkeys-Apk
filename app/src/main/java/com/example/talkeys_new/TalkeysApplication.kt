package com.example.talkeys_new

import android.app.Application
import com.talkeys.shared.initKoin
import org.koin.android.ext.koin.androidContext

class TalkeysApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize KMP shared module — must happen in Application, not Activity,
        // to avoid KoinApplicationAlreadyStartedException on Activity recreation.
        initKoin {
            androidContext(this@TalkeysApplication)
        }
    }
}
