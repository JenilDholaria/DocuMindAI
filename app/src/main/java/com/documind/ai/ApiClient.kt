
package com.documind.ai

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class PdfUploadResult(
    val documentId: String,
    val filename: String
)

data class AiSource(
    val page: Int,
    val excerpt: String
)

data class AiChatResult(
    val answer: String,
    val sources: List<AiSource>
)

object ApiClient {

    // Uses ADB reverse port forwarding to reach the Windows backend
    private const val BASE_URL = "http://127.0.0.1:8000"

    fun uploadPdf(
        pdfBytes: ByteArray,
        filename: String
    ): PdfUploadResult {

        val boundary = "DocuMind-${UUID.randomUUID()}"
        val connection = URL("$BASE_URL/documents")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 120000
            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "multipart/form-data; boundary=$boundary"
            )

            connection.outputStream.use { output ->
                output.write(
                    "--$boundary\r\n".toByteArray(Charsets.UTF_8)
                )

                val safeFilename = filename
                    .replace("\"", "")
                    .replace("\r", "")
                    .replace("\n", "")

                val header =
                    "Content-Disposition: form-data; " +
                            "name=\"file\"; filename=\"$safeFilename\"\r\n" +
                            "Content-Type: application/pdf\r\n\r\n"

                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(pdfBytes)
                output.write(
                    "\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8)
                )
            }

            val response = JSONObject(readResponse(connection))

            return PdfUploadResult(
                documentId = response.getString("document_id"),
                filename = response.optString("filename", filename)
            )

        } finally {
            connection.disconnect()
        }
    }

    fun askQuestion(
        documentId: String,
        question: String
    ): AiChatResult {

        val connection = URL("$BASE_URL/chat")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 120000
            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json; charset=utf-8"
            )

            val request = JSONObject()
                .put("document_id", documentId)
                .put("question", question)

            connection.outputStream.use { output ->
                output.write(
                    request.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val response = JSONObject(readResponse(connection))
            val sources = mutableListOf<AiSource>()
            val sourcesJson = response.optJSONArray("sources")

            if (sourcesJson != null) {
                for (i in 0 until sourcesJson.length()) {
                    val item = sourcesJson.getJSONObject(i)

                    sources.add(
                        AiSource(
                            page = item.optInt("page"),
                            excerpt = item.optString("excerpt")
                        )
                    )
                }
            }

            return AiChatResult(
                answer = response.getString("answer"),
                sources = sources
            )

        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(
        connection: HttpURLConnection
    ): String {

        val code = connection.responseCode

        val stream = if (code in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val body = stream?.bufferedReader()?.use {
            it.readText()
        }.orEmpty()

        if (code !in 200..299) {
            val detail = try {
                JSONObject(body).optString("detail", body)
            } catch (_: Exception) {
                body
            }

            throw IllegalStateException(
                "Server error $code: $detail"
            )
        }

        return body
    }
}
