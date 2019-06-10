package io.karn.sentry

import android.content.Context
import androidx.core.content.PermissionChecker

object PermissionHelper : IPermissionHelper {

    override fun hasPermission(context: Context, permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
    }
}
