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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference


/**
 * A lightweight class which makes requesting permissions a breeze.
 */
class Sentry<T : FragmentActivity> internal constructor(activity: T, private val permissionHelper: IPermissionHelper) : ActivityCompat.OnRequestPermissionsResultCallback by activity {

    companion object {
        /**
         * A fluent API to create an instance of the Sentry object.
         *
         * @param activity  A reference to an activity.
         * @return An instance of the Sentry object.
         */
        fun <T : AppCompatActivity> with(activity: T): Sentry<T> {
            return Sentry(activity, PermissionHelper)
        }
    }

    private val requestCode: Int = this.hashCode()
    private lateinit var callback: ((Boolean) -> Unit)
    private val activity: WeakReference<T> = WeakReference(activity)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) {
            return
        }

        if (requestCode == this.requestCode) {
            callback(grantResults[0] == PermissionChecker.PERMISSION_GRANTED)
        }
    }

    /**
     * Request a [permission] and return the result of the request through the [callback] receiver.
     *
     * @param permission    One of the many Android permissions. See: [Manifest.permission]
     * @param callback      A receiver for the result of the permission request.
     */
    fun requestPermission(permission: String, callback: (granted: Boolean) -> Unit) {
        if (permission.isBlank()) {
            throw IllegalArgumentException("Invalid permission specified.")
        }

        this.callback = callback

        with(activity.get()) {
            this ?: return

            if (permissionHelper.hasPermission(this, permission)) {
                return@with callback(true)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return@with this.requestPermissions(arrayOf(permission), requestCode)
            }

            callback(true)
        }
    }
}
