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

import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import com.nhaarman.mockitokotlin2.mock as mockFrom

internal class SentryTest {

    companion object {
        private const val EMPTY_PERMISSION = "      "
        private const val ARBITRARY_PERMISSION = "io.karn.sentry.permission.ARBITRARY_PERMISSION"

        /**
         * Fix issues that Mockito has with Kotlin.
         * {@link https://stackoverflow.com/a/48805160}
         */
        fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

        /**
         * Allow modification of static final fields.
         */
        @Throws(Exception::class)
        fun setFinalStatic(field: Field, newValue: Any) {
            field.isAccessible = true

            val modifiersField = Field::class.java.getDeclaredField("modifiers")
            modifiersField.isAccessible = true
            modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())

            field.set(null, newValue)
        }

        /**
         * Configure the default state for a given permission.
         */
        fun setupPermissionHelper(permissionHelper: IPermissionHelper, @PermissionResult initialPermissionResult: Int) {
            `when`(permissionHelper.hasPermission(any(AppCompatActivity::class.java), anyString()))
                    .thenReturn(initialPermissionResult)
        }

        /**
         * Configure the result of a given permission request.
         */
        fun setupPermissionResult(activity: AppCompatActivity, sentry: Sentry, @PermissionResult permissionResult: Int, requestCode: Int? = null) {
            `when`(activity.requestPermissions(any(), anyInt())).then {
                val permissions = it.getArgument<Array<String>>(0)
                val code = requestCode ?: it.getArgument<Int>(1)

                // Set the permission result to DENIED to validate the flow.
                sentry.onRequestPermissionsResult(code, permissions, intArrayOf(permissionResult))
            }
        }
    }

    private val activity = mock(AppCompatActivity::class.java)!!
    private val permissionHelper = mock(IPermissionHelper::class.java)!!
    private val callback = mockFrom<(Boolean) -> Unit>()

    @Before
    fun before() {
        setFinalStatic(Build.VERSION::class.java.getField("SDK_INT"), Build.VERSION_CODES.M)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Expect error when empty permission is specified`() {

        setupPermissionHelper(permissionHelper, PERMISSION_GRANTED)

        Sentry(activity, permissionHelper).requestPermission(EMPTY_PERMISSION, callback)
    }

    @Test
    fun `Verify granted permission`() {
        setupPermissionHelper(permissionHelper, PERMISSION_GRANTED)

        Sentry(activity, permissionHelper).requestPermission(ARBITRARY_PERMISSION, callback)

        verify(activity, never()).requestPermissions(any(), anyInt())
        verify(callback, times(1)).invoke(eq(true))

        verifyNoMoreInteractions(activity)
    }

    @Test
    fun `Ignore results from unknown requestCode`() {
        // Create test object
        val sentry = Sentry(activity, permissionHelper)

        // Initialize mocked responses
        setupPermissionHelper(permissionHelper, PERMISSION_DENIED)

        setupPermissionResult(activity, sentry, PERMISSION_GRANTED, -1)

        // Perform action
        sentry.requestPermission(ARBITRARY_PERMISSION, callback)

        // Assert
        verify(activity, times(1)).requestPermissions(any(), eq(sentry.hashCode()))
        verify(callback, never()).invoke(any(Boolean::class.java))

        verifyNoMoreInteractions(activity)
    }

    @Test
    fun `Always return granted permission for API less than M`() {
        // Set the the version to be less than M.
        setFinalStatic(Build.VERSION::class.java.getField("SDK_INT"), Build.VERSION_CODES.M - 2)

        // Create test object
        val sentry = Sentry(activity, permissionHelper)

        // Initialize mocked responses
        setupPermissionHelper(permissionHelper, PERMISSION_DENIED)

        setupPermissionResult(activity, sentry, PERMISSION_DENIED)

        // Perform action
        sentry.requestPermission(ARBITRARY_PERMISSION, callback)

        // Assert
        verify(activity, never()).requestPermissions(any(), eq(sentry.hashCode()))
        verify(callback, times(1)).invoke(eq(true))

        verifyNoMoreInteractions(activity)
    }

    @Test
    fun `Single permission granted`() {
        // Create test object
        val sentry = Sentry(activity, permissionHelper)

        // Initialize mocked responses
        setupPermissionHelper(permissionHelper, PERMISSION_DENIED)

        setupPermissionResult(activity, sentry, PERMISSION_GRANTED)

        // Perform action
        sentry.requestPermission(ARBITRARY_PERMISSION, callback)

        // Assert
        verify(activity, times(1)).requestPermissions(any(), eq(sentry.hashCode()))
        verify(callback, times(1)).invoke(eq(true))

        verifyNoMoreInteractions(activity)
    }

    @Test
    @Ignore
    fun `Single permission denied`() {

    }

    @Test
    @Ignore
    fun `Sequential permissions mixed grants`() {

    }

    @Test
    @Ignore
    fun `Sequential permissions granted`() {

    }

    @Test
    @Ignore
    fun `Sequential permissions denied`() {

    }
}
