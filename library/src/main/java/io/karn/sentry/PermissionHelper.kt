package io.karn.sentry

import android.content.Context
import androidx.core.content.ContextCompat

object PermissionHelper : IPermissionHelper {

    @PermissionResult
    override fun hasPermission(context: Context, permission: String): Int {
        return ContextCompat.checkSelfPermission(context, permission)
    }
}
