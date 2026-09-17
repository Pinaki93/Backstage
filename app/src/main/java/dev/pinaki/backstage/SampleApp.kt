package dev.pinaki.backstage

import android.app.Application
import dev.pinaki.backstage.library.Backstage

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Backstage().init()
    }
}
