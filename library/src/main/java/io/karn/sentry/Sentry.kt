/*
 * MIT License
 *
 * Copyright (c) 2019 Karn Saheb
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.karn.sentry

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import java.lang.ref.WeakReference
import kotlin.random.Random

/**
 * Provides a typealias for aesthetic purposes.
 */
typealias Permissions = ActivityCompat.OnRequestPermissionsResultCallback

/**
 * A lightweight class which makes requesting permissions a breeze.
 */
class Sentry internal constructor(activity: Activity, private val permissionHelper: IPermissionHelper) {

    companion object {
        // Tracks the requests that are made and their callbacks
        internal val receivers = HashMap<Int, (granted: Boolean) -> Unit>()

        /**
         * A fluent API to create an instance of the Sentry object.
         *
         * @param activity  A reference to an activity.
         * @return An instance of the Sentry object.
         */
        fun with(activity: Activity): Sentry {
            return Sentry(activity, PermissionHelper)
        }
    }

    // Stores a reference to the activity
    private val activity: WeakReference<Activity> = WeakReference(activity)

    /**
     * Request a [permission] and return the result of the request through the [callback] receiver.
     *
     * @param permission    One of the many Android permissions. See: [Manifest.permission]
     * @param callback      A receiver for the result of the permission request.
     * @return The request code associated with the request
     */
    fun requestPermission(permission: String, callback: (granted: Boolean) -> Unit): Int {
        if (permission.isBlank()) {
            throw IllegalArgumentException("Invalid permission specified.")
        }

        // Generate a request code for the request
        var requestCode: Int
        do {
            requestCode = Random.nextInt(1, 65535)
        } while (receivers.containsKey(requestCode))

        with(activity.get()) {
            this ?: return@with

            // We can immediately resolve if we've been granted the permission
            if (permissionHelper.hasPermission(this, permission)) {
                return@with callback(true)
            }

            // Track the request
            receivers[requestCode] = callback

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return@with ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }

            callback(true)
        }

        return requestCode
    }
}

/**
 * A delegated receiver for the onRequestPermissionsResult, this allows the activity's permissions
 * results to be intercepted by Sentry and forwarded to the defined receiver.
 */
object SentryPermissionHandler : Permissions {
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) {
            return
        }

        // Ensure that there is a pending request code available and remove it once its been tracked
        val callback = Sentry.receivers.remove(requestCode)
                ?: return // No handler for request code

        callback.invoke(grantResults[0] == PermissionChecker.PERMISSION_GRANTED)
    }
}
