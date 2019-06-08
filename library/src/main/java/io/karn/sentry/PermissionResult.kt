package io.karn.sentry

import android.content.pm.PackageManager
import androidx.annotation.IntDef


@IntDef(PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_DENIED)
@Retention(AnnotationRetention.SOURCE)
annotation class PermissionResult
