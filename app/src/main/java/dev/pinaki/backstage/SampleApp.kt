package dev.pinaki.backstage

import android.app.Application
import dev.pinaki.backstage.library.Backstage
import dev.pinaki.backstage.library.BackstageDelegate
import dev.pinaki.backstage.library.BasicController

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Backstage.getInstance(BackstageDelegate(this))
            .addController(object : BasicController("/") {
                override fun getHtmlResource() = R.raw.sample
            })
            .init()
    }
}
