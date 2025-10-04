package com.example.labelhighlighter

import android.graphics.Bitmap
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

object OpenAIHelper {

    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun analyzeImage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val base64Image = bitmapToBase64(bitmap)
        val apiKey = BuildConfig.OPENAI_API_KEY

        if (apiKey.isEmpty()) {
            return@withContext "Error: API key is not set in local.properties"
        }

        val url = "https://api.openai.com/v1/chat/completions"

        val requestPayload = mapOf(
            "model" to "gpt-4-turbo",
            "messages" to listOf(
                mapOf(
                    "role" to "user",
                    "content" to listOf(
                        mapOf(
                            "type" to "text",
                            "text" to "Extract all nutritional values (Fat, Saturates, Carbohydrate, Sugars, Protein, Salt) from this food label image. Respond only with a single, clean JSON object like {\"Fat\": \"0g\", \"Salt\": \"0g\"}. Do not include any extra text or markdown formatting."
                        ),
                        mapOf(
                            "type" to "image_url",
                            "image_url" to mapOf(
                                "url" to "data:image/jpeg;base64,$base64Image"
                            )
                        )
                    )
                )
            ),
            "max_tokens" to 300
        )

        val requestBodyJson = gson.toJson(requestPayload)
        val body = requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@use "API Error: ${response.code} ${response.message}\n${response.body?.string()}"
                }

                val responseBody = response.body?.string() ?: return@use "Error: Empty response body"

                val jsonObject = JsonParser.parseString(responseBody).asJsonObject
                val choices = jsonObject.getAsJsonArray("choices")
                if (choices.isEmpty()) {
                    return@use "Error: No choices returned from API."
                }

                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message")
                val content = message.get("content").asString

                content ?: "Error: No content in API response."
            }
        } catch (e: Exception) {
            "Network or other error: ${e.message}"
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
