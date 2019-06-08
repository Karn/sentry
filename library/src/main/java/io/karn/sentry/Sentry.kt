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
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference


/**
 * A lightweight class which makes requesting permissions a breeze.
 */
class Sentry internal constructor(activity: AppCompatActivity, private val permissionHelper: IPermissionHelper) : ActivityCompat.OnRequestPermissionsResultCallback by activity {

    companion object {
        /**
         * A fluent API to create and instance of the Sentry object.
         *
         * @param activity  A reference to an activity.
         * @return An instance of the Sentry object.
         */
        fun with(activity: AppCompatActivity): Sentry {
            return Sentry(activity, PermissionHelper)
        }
    }

    private val requestCode: Int = this.hashCode()
    lateinit var callback: ((Boolean) -> Unit)
    private val activity: WeakReference<AppCompatActivity> = WeakReference(activity)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) {
            return
        }

        when (requestCode) {
            this.requestCode -> {
                when (grantResults[0]) {
                    PackageManager.PERMISSION_GRANTED -> callback(true)
                    else -> callback(false)
                }
            }
            else -> Unit
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
            this ?: return@with

            when (permissionHelper.hasPermission(this, permission)) {
                PackageManager.PERMISSION_GRANTED -> callback(true)
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        this.requestPermissions(arrayOf(permission), requestCode)
                    } else {
                        callback(true)
                    }
                }
            }
        }
    }
}
