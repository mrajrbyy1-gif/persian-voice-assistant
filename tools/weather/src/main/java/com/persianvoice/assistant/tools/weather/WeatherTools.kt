package com.persianvoice.assistant.tools.weather

import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.domain.model.WeatherData
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface Weather Provider - قابل تعویض.
 */
interface WeatherProvider {
    suspend fun current(latitude: Double, longitude: Double): WeatherData
    suspend fun currentByCity(city: String): WeatherData
}

/**
 * پیاده‌سازی پیش‌فرض با Open-Meteo (رایگان، نیاز به API Key ندارد).
 */
class OpenMeteoWeatherProvider : WeatherProvider {

    private val httpClient = okhttp3.OkHttpClient()

    override suspend fun current(latitude: Double, longitude: Double): WeatherData = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"

        val request = okhttp3.Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: throw IllegalStateException("پاسخ خالی")

        val json = Json.parseToJsonElement(body).jsonObject
        val current = json["current"]?.jsonObject
            ?: throw IllegalStateException("ساختار پاسخ نامعتبر")

        val temp = current["temperature_2m"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
        val humidity = current["relative_humidity_2m"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
        val wind = current["wind_speed_10m"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
        val weatherCode = current["weather_code"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0

        WeatherData(
            temperature = temp,
            description = weatherCodeToDescription(weatherCode),
            humidity = humidity,
            windSpeed = wind,
            city = ""
        )
    }

    override suspend fun currentByCity(city: String): WeatherData = withContext(Dispatchers.IO) {
        // استفاده از Open-Meteo Geocoding API
        val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${city.trim()}&count=1"
        val geoRequest = okhttp3.Request.Builder().url(geoUrl).build()
        val geoResponse = httpClient.newCall(geoRequest).execute()
        val geoBody = geoResponse.body?.string() ?: throw IllegalStateException("پاسخ جغرافیا خالی")

        val json = Json.parseToJsonElement(geoBody).jsonObject
        val results = json["results"]?.let { it as? kotlinx.serialization.json.JsonArray }
        val first = results?.firstOrNull()?.jsonObject
            ?: throw IllegalStateException("شهر '$city' پیدا نشد")

        val lat = first["latitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
            ?: throw IllegalStateException("عرض جغرافیایی نامعتبر")
        val lon = first["longitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
            ?: throw IllegalStateException("طول جغرافیایی نامعتبر")
        val cityName = first["name"]?.jsonPrimitive?.content ?: city

        val data = current(lat, lon)
        data.copy(city = cityName)
    }

    private fun weatherCodeToDescription(code: Int): String = when (code) {
        0 -> "صاف"
        1, 2 -> "نیمه ابری"
        3 -> "ابری"
        45, 48 -> "مه"
        51, 53, 55 -> "باران ریز"
        61, 63, 65 -> "باران"
        71, 73, 75 -> "برف"
        80, 81, 82 -> "رگبار"
        95 -> "رعد و برق"
        else -> "نامشخص"
    }
}

/**
 * Tool وضعیت هوا.
 *
 * مثال:
 * - "هوا چطوره؟"
 * - "هوای تهران چطوره؟"
 */
@Singleton
class WeatherCurrentTool @Inject constructor() : AssistantTool {

    private val provider: WeatherProvider = OpenMeteoWeatherProvider()

    override val id = "weather.current"
    override val name = "وضعیت هوا"
    override val description = "دریافت وضعیت فعلی آب و هوا برای یک شهر یا موقعیت"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val city = arguments["city"] as? String
            ?: "تهران" // پیش‌فرض

        return try {
            val weather = provider.currentByCity(city)
            val description = "هوای ${weather.city}: ${weather.description}، دما ${weather.temperature}°C، رطوبت ${weather.humidity}٪"
            ToolResult.Success(description)
        } catch (e: Exception) {
            ToolResult.Failure("خطا در دریافت وضعیت هوا: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "city" to stringProp("نام شهر (مثل 'تهران'، 'اصفهان')")
        ),
        required = emptyList()
    )
}