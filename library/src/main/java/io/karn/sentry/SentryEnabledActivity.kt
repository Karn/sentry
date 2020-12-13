package io.karn.sentry

import androidx.appcompat.app.AppCompatActivity

class SentryEnabledActivity : AppCompatActivity() {

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


    }
}