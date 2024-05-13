package dev.josejordan

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File

fun main() {
    // Definir constantes para el script
    val chunkSize = 1024
    val xiApiKey = "<xi-api-key>"
    val voiceId = "<voice-id>"
    val textToSpeak = "<text>"
    val outputPath = "output.mp3"

    // Construir la URL para la solicitud de Texto a Voz
    val ttsUrl = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId/stream"

    // Preparar los encabezados y el cuerpo de la solicitud
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val jsonData = """
    {
        "text": "$textToSpeak",
        "model_id": "eleven_multilingual_v2",
        "voice_settings": {
            "stability": 0.5,
            "similarity_boost": 0.8,
            "style": 0.0,
            "use_speaker_boost": true
        }
    }
    """.trimIndent()

    val requestBody = jsonData.toRequestBody(mediaType)

    // Crear la solicitud
    val request = Request.Builder()
        .url(ttsUrl)
        .addHeader("Accept", "application/json")
        .addHeader("xi-api-key", xiApiKey)
        .post(requestBody)
        .build()

    // Crear el cliente HTTP y ejecutar la solicitud
    val client = OkHttpClient()
    client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            // Abrir el archivo de salida en modo de escritura binaria
            val file = File(outputPath).outputStream()
            response.body?.byteStream()?.use { inputStream ->
                val buffer = ByteArray(chunkSize)
                while (true) {
                    val readBytes = inputStream.read(buffer)
                    if (readBytes == -1) break
                    file.write(buffer, 0, readBytes)
                }
            }
            file.close()
            println("Audio stream saved successfully.")
        } else {
            println("Error during request: ${response.message}")
        }
    }
}
