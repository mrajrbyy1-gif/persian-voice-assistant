package com.persianvoice.assistant.tools.search

import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.domain.model.SearchResult
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface Search Provider - قابل تعویض.
 */
interface SearchProvider {
    suspend fun search(query: String): List<SearchResult>
}

/**
 * پیاده‌سازی ساده با DuckDuckGo HTML (نیاز به API Key ندارد).
 * در فاز 76 با Searx یا Brave Search جایگزین می‌شود.
 */
class DuckDuckGoSearchProvider : SearchProvider {

    private val httpClient = okhttp3.OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    override suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val url = "https://html.duckduckgo.com/html/?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
        val request = okhttp3.Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 PersianVoiceAssistant")
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList()

        parseResults(body)
    }

    private fun parseResults(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val pattern = Regex(
            "<a[^>]*class=\"result__a\"[^>]*href=\"([^\"]+)\"[^>]*>([^<]+)</a>",
            RegexOption.IGNORE_CASE
        )
        val snippetPattern = Regex(
            "<a[^>]*class=\"result__snippet\"[^>]*>([^<]+)</a>",
            RegexOption.IGNORE_CASE
        )

        val matches = pattern.findAll(html).take(5).toList()
        val snippets = snippetPattern.findAll(html).take(5).toList()

        matches.forEachIndexed { index, match ->
            val url = match.groupValues[1]
            val title = match.groupValues[2]
            val snippet = snippets.getOrNull(index)?.groupValues?.get(1) ?: ""
            results.add(SearchResult(title, snippet, url))
        }

        return results
    }
}

/**
 * Tool جستجوی وب.
 *
 * مثال:
 * - "قیمت طلا امروز چنده؟"
 */
@Singleton
class WebSearchTool @Inject constructor() : AssistantTool {

    private val provider: SearchProvider = DuckDuckGoSearchProvider()

    override val id = "web.search"
    override val name = "جستجوی وب"
    override val description = "جستجو در وب با استفاده از موتور جستجوی DuckDuckGo و برگرداندن نتایج"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val query = arguments["query"] as? String
            ?: return ToolResult.Failure("پارامتر query الزامی است")

        return try {
            val results = provider.search(query)
            if (results.isEmpty()) {
                ToolResult.Success("نتیجه‌ای برای '$query' یافت نشد")
            } else {
                val text = results.take(3).joinToString("\n") { result ->
                    "${result.title}: ${result.snippet}"
                }
                ToolResult.Success(text)
            }
        } catch (e: Exception) {
            ToolResult.Failure("خطا در جستجو: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "query" to stringProp("عبارت جستجو")
        ),
        required = listOf("query")
    )
}