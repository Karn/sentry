package io.karn.sentry

import androidx.annotation.IntDef
import androidx.core.content.PermissionChecker


@IntDef(PermissionChecker.PERMISSION_GRANTED, PermissionChecker.PERMISSION_DENIED)
@Retention(AnnotationRetention.SOURCE)
annotation class PermissionResult
