package com.persianvoice.assistant.tools.contacts

import android.content.Context
import android.provider.ContactsContract
import com.persianvoice.assistant.domain.model.Contact
import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tool جستجوی مخاطبین.
 *
 * مثال:
 * - "شماره علی رو پیدا کن"
 */
@Singleton
class ContactsTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "contacts.search"
    override val name = "جستجوی مخاطب"
    override val description = "جستجو در مخاطبین دستگاه بر اساس نام"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val name = arguments["name"] as? String
            ?: return ToolResult.Failure("پارامتر name الزامی است")

        val contacts = searchContacts(name)
        if (contacts.isEmpty()) {
            return ToolResult.Failure("مخاطبی با نام '$name' پیدا نشد")
        }

        return if (contacts.size == 1) {
            val c = contacts.first()
            val phones = c.phoneNumbers.joinToString("، ")
            ToolResult.Success("$phones: ${c.name} - شماره $phones")
        } else {
            val list = contacts.joinToString("\n") { "${it.name}: ${it.phoneNumbers.joinToString("، ")}" }
            ToolResult.Success("$list\nچند مخاطب پیدا شد. لطفاً مشخص کنید کدام را می‌خواهید.")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "name" to stringProp("نام مخاطب برای جستجو (مثل 'علی')")
        ),
        required = listOf("name")
    )

    private fun searchContacts(query: String): List<Contact> {
        val results = mutableListOf<Contact>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$query%"),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC LIMIT 5"
        )

        cursor?.use { c ->
            val idIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            val grouped = mutableMapOf<Long, MutableList<String>>()
            val names = mutableMapOf<Long, String>()

            while (c.moveToNext()) {
                val id = c.getLong(idIndex)
                val name = c.getString(nameIndex) ?: continue
                val number = c.getString(numberIndex)?.let { normalizeNumber(it) } ?: continue

                grouped.getOrPut(id) { mutableListOf() }.add(number)
                names[id] = name
            }

            grouped.forEach { (id, numbers) ->
                results.add(
                    Contact(
                        id = id,
                        name = names[id] ?: "",
                        phoneNumbers = numbers.distinct()
                    )
                )
            }
        }

        return results
    }

    private fun normalizeNumber(number: String): String {
        val persianDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        var result = number
        persianDigits.forEachIndexed { i, fa -> result = result.replace(fa, i.toString()) }
        return result.replace("[^0-9+]".toRegex(), "")
    }
}