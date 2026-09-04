package com.persianvoice.assistant.core.permissions

/**
 * نوع مجوز - برای درخواست‌های مختلف
 */
enum class PermissionType {
    MICROPHONE,
    CONTACTS,
    PHONE,
    SMS,
    CALENDAR,
    LOCATION,
    NOTIFICATIONS,
    STORAGE,
    CALL_LOG,
    PACKAGE_QUERY
}

data class PermissionRequest(
    val type: PermissionType,
    val rationale: String,
    val required: Boolean = false
)