package com.persianvoice.assistant.tools.calculator

import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tool محاسبات ریاضی ساده.
 * از عبارت‌های ریاضی به فرمت استاندارد پشتیبانی می‌کند:
 * +، -، *، /، ()
 *
 * مثال:
 * - "25 * 47" -> 1175
 * - "(10 + 5) * 2" -> 30
 *
 * اعداد فارسی به‌صورت خودکار به انگلیسی تبدیل می‌شوند.
 */
@Singleton
class CalculatorTool @Inject constructor() : AssistantTool {

    override val id = "calculator.calculate"
    override val name = "ماشین حساب"
    override val description = "محاسبه عبارات ریاضی ساده شامل جمع، تفریق، ضرب، تقسیم و پرانتز"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val expression = arguments["expression"] as? String
            ?: return ToolResult.Failure("پارامتر expression الزامی است")

        return try {
            val normalized = normalizeExpression(expression)
            val result = evaluateExpression(normalized)
            ToolResult.Success(result.toString())
        } catch (e: ArithmeticException) {
            ToolResult.Failure("خطای محاسباتی: ${e.message}")
        } catch (e: Exception) {
            ToolResult.Failure("عبارت ریاضی نامعتبر: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "expression" to stringProp("عبارت ریاضی برای محاسبه، مثل '25 * 47' یا '(10+5)/3'")
        ),
        required = listOf("expression")
    )

    private fun normalizeExpression(input: String): String {
        // تبدیل اعداد فارسی/عربی به انگلیسی
        val persianDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        val arabicDigits = listOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")

        var result = input
        persianDigits.forEachIndexed { i, fa -> result = result.replace(fa, i.toString()) }
        arabicDigits.forEachIndexed { i, ar -> result = result.replace(ar, i.toString()) }
        result = result.replace("×", "*").replace("÷", "/").replace(" ", "")
        return result
    }

    private fun evaluateExpression(expression: String): Double {
        // استفاده از javax.script برای ارزیابی امن عبارات ساده
        val scriptEngineManager = javax.script.ScriptEngineManager()
        val engine = scriptEngineManager.getEngineByName("JavaScript")
            ?: throw IllegalStateException("موتور محاسبه در دسترس نیست")
        val result = engine.eval(expression)
        return when (result) {
            is Number -> result.toDouble()
            else -> throw IllegalStateException("نتیجه نامعتبر")
        }
    }
}