package dev.pinaki.backstage

import android.app.Application
import dev.pinaki.backstage.library.Backstage
import dev.pinaki.backstage.library.BackstageDelegate

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Backstage.getInstance(BackstageDelegate()).init()
    }
}
