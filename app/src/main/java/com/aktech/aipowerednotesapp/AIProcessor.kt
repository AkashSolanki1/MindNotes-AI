package com.aktech.aipowerednotesapp

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AIProcessor {

    private val client = OkHttpClient()

    private val API_KEY = "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"
    private val API_URL = "https://openrouter.ai/api/v1/chat/completions"

    interface AIResponseCallback {
        fun onSuccess(improvedText: String)
        fun onFailure(error: String)
    }


    fun improveNoteText(originalText: String, callback: AIResponseCallback) {

        if (originalText.isBlank()) {
            callback.onFailure("Text cannot be empty")
            return
        }

        val prompt = """
            You are a professional writing assistant.
            Improve the following text by:
            - Fixing grammar and spelling
            - Improving clarity and flow
            - Keeping the original meaning
            - Making it sound natural and professional
            - Do NOT add new information

            Text:
            "$originalText"

            Return ONLY valid JSON in this format:
            {
              "improved_text": "..."
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("model", "mistralai/mistral-7b-instruct")
            put("temperature", 0.3)
            put("max_tokens", 500)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "Return valid JSON only.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val requestBody =
            jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string() ?: ""

                    if (!it.isSuccessful) {
                        callback.onFailure("API Error ${it.code}: $body")
                        return
                    }

                    try {
                        val content = JSONObject(body)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")

                        val start = content.indexOf('{')
                        val end = content.lastIndexOf('}')

                        if (start == -1 || end == -1) {
                            callback.onFailure("Invalid AI response")
                            return
                        }

                        val json = JSONObject(content.substring(start, end + 1))
                        val improvedText = json.optString("improved_text", originalText)

                        callback.onSuccess(improvedText)

                    } catch (e: Exception) {
                        callback.onFailure("Parsing error: ${e.message}")
                    }
                }
            }
        })
    }
}
