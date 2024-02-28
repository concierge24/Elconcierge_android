package com.trava.utilities

import android.app.Application

class AppController: Application(){
    override fun onCreate() {
        super.onCreate()
        SharedPrefs.with(applicationContext)
    }
}