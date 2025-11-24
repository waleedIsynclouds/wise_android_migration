package com.example.wiseman

import android.app.Application
import android.content.Context

class HxjApp : Application() {
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        private lateinit var instance: HxjApp
        @JvmStatic
        fun getAppContext(): Context = instance.applicationContext
    }
}
