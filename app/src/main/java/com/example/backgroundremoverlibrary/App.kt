package com.example.backgroundremoverlibrary

import android.app.Application
import com.theapache64.removebg.RemoveBg

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        RemoveBg.init("kTuWVRY3g7W6HHJwHTpbzNx9")
    }
}