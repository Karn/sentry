package io.karn.sentry

import android.content.Context

interface IPermissionHelper {

    @PermissionResult
    fun hasPermission(context: Context, permission: String): Int
}
