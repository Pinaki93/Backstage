package dev.pinaki.backstage

import android.app.Application
import dev.pinaki.backstage.library.Backstage
import dev.pinaki.backstage.library.BackstageDelegate
import dev.pinaki.backstage.library.BasicController

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Backstage.getInstance(BackstageDelegate(this))
            .withSharedPrefExplorer("User Preferences") {
                getSharedPreferences("user_preferences", MODE_PRIVATE)
            }
            .withSharedPrefExplorer("Feature Flags") {
                getSharedPreferences("feature_flags", MODE_PRIVATE)
            }
            .init()
    }
}
