package com.persianvoice.assistant.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Permission Manager مرکزی.
 * فقط زمانی که قابلیت مربوطه نیاز شود، مجوز درخواست می‌شود.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isGranted(type: PermissionType): Boolean {
        val permission = toAndroidPermission(type) ?: return true
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun isAllGranted(types: List<PermissionType>): Boolean =
        types.all { isGranted(it) }

    fun notGrantedTypes(types: List<PermissionType>): List<PermissionType> =
        types.filter { !isGranted(it) }

    fun toAndroidPermission(type: PermissionType): String? = when (type) {
        PermissionType.MICROPHONE -> Manifest.permission.RECORD_AUDIO
        PermissionType.CONTACTS -> Manifest.permission.READ_CONTACTS
        PermissionType.PHONE -> Manifest.permission.CALL_PHONE
        PermissionType.SMS -> Manifest.permission.SEND_SMS
        PermissionType.CALENDAR -> Manifest.permission.READ_CALENDAR
        PermissionType.LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
        PermissionType.NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
        PermissionType.STORAGE -> null // در فاز بعدی
        PermissionType.CALL_LOG -> Manifest.permission.READ_CALL_LOG
        PermissionType.PACKAGE_QUERY -> Manifest.permission.QUERY_ALL_PACKAGES
    }

    fun requiredPermissions(type: PermissionType): List<String> = when (type) {
        PermissionType.PHONE -> listOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )
        PermissionType.SMS -> listOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
        )
        PermissionType.CALENDAR -> listOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
        PermissionType.LOCATION -> listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        else -> listOfNotNull(toAndroidPermission(type))
    }
}