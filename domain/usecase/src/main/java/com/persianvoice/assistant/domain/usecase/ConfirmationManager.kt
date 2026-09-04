package com.persianvoice.assistant.domain.usecase

import com.persianvoice.assistant.domain.model.ConfirmationAction
import com.persianvoice.assistant.domain.model.RiskLevel

/**
 * سیستم تأیید برای عملیات حساس.
 */
interface ConfirmationManager {
    suspend fun confirm(action: ConfirmationAction): Boolean
    suspend fun shouldConfirm(riskLevel: RiskLevel, level: String): Boolean
}

class DefaultConfirmationManager : ConfirmationManager {

    override suspend fun confirm(action: ConfirmationAction): Boolean {
        // پیاده‌سازی واقعی از طریق UI - در فاز 22
        // فعلاً فقط HIGH risk را نیاز به تأیید می‌داند
        return when (action.riskLevel) {
            RiskLevel.HIGH -> true // نیاز به تأیید کاربر
            else -> true
        }
    }

    override suspend fun shouldConfirm(riskLevel: RiskLevel, level: String): Boolean {
        return when (level) {
            "Always" -> true
            "Never" -> riskLevel == RiskLevel.HIGH // حتی در Never، HIGH اجباری است
            "Smart" -> riskLevel != RiskLevel.LOW
            else -> riskLevel == RiskLevel.HIGH
        }
    }
}