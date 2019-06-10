package io.karn.sentry

import android.content.Context

interface IPermissionHelper {

    fun hasPermission(context: Context, permission: String): Boolean
}
