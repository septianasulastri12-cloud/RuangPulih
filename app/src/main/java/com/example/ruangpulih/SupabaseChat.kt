
package com.example.ruangpulih

import android.media.MediaRecorder
import java.io.File
import android.content.Context
import android.net.Uri
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class PesanChat(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val createdAt: String
)

object SupabaseChat {

    private fun encode(nilai: String): String =
        URLEncoder.encode(nilai, "UTF-8")


    suspend fun ambilConversationId(
        userId: String,
        role: String,
        accessToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var koneksi: HttpURLConnection? = null

        try {
            val kolom = if (role == "psychologist") {
                "psychologist_id"
            } else {
                "client_id"
            }

            val url = SupabaseConfig.SUPABASE_URL.trimEnd('/') +
                    "/rest/v1/conversations" +
                    "?select=id&$kolom=eq.${encode(userId)}&limit=1"

            koneksi = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000

                setRequestProperty("apikey", SupabaseConfig.SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Accept", "application/json")
            }

            val kode = koneksi.responseCode
            val stream = if (kode in 200..299) {
                koneksi.inputStream
            } else {
                koneksi.errorStream
            }

            val respons = stream?.bufferedReader()?.use {
                it.readText()
            }.orEmpty()

            if (kode !in 200..299) {
                return@withContext Result.failure(
                    Exception("Gagal mencari percakapan. Kode: $kode")
                )
            }

            val array = JSONArray(respons)

            if (array.length() == 0) {
                return@withContext Result.failure(
                    Exception("Percakapan belum tersedia untuk akun ini.")
                )
            }

            Result.success(array.getJSONObject(0).getString("id"))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            koneksi?.disconnect()
        }
    }


    suspend fun ambilPesan(
        conversationId: String,
        accessToken: String
    ): Result<List<PesanChat>> = withContext(Dispatchers.IO) {
        var koneksi: HttpURLConnection? = null

        try {
            val url = SupabaseConfig.SUPABASE_URL.trimEnd('/') +
                    "/rest/v1/messages" +
                    "?select=id,conversation_id,sender_id,content,created_at" +
                    "&conversation_id=eq.${encode(conversationId)}" +
                    "&order=created_at.asc"

            koneksi = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000

                setRequestProperty("apikey", SupabaseConfig.SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Accept", "application/json")
            }

            val kode = koneksi.responseCode
            val stream = if (kode in 200..299) {
                koneksi.inputStream
            } else {
                koneksi.errorStream
            }

            val respons = stream?.bufferedReader()?.use { it.readText() }
                .orEmpty()

            if (kode !in 200..299) {
                return@withContext Result.failure(
                    Exception(
                        "Gagal mencari percakapan. Kode: $kode. Detail: $respons"
                    )
                )
            }

            val array = JSONArray(respons)
            val daftarPesan = mutableListOf<PesanChat>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                daftarPesan.add(
                    PesanChat(
                        id = item.optString("id"),
                        conversationId = item.optString("conversation_id"),
                        senderId = item.optString("sender_id"),
                        content = item.optString("content"),
                        createdAt = item.optString("created_at")
                    )
                )
            }

            Result.success(daftarPesan)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            koneksi?.disconnect()
        }
    }

    suspend fun kirimPesan(
        conversationId: String,
        senderId: String,
        content: String,
        accessToken: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var koneksi: HttpURLConnection? = null

        try {
            if (content.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Pesan tidak boleh kosong.")
                )
            }

            val url = SupabaseConfig.SUPABASE_URL.trimEnd('/') +
                    "/rest/v1/messages"

            koneksi = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true

                setRequestProperty("apikey", SupabaseConfig.SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "return=minimal")
            }

            val data = JSONObject().apply {
                put("conversation_id", conversationId)
                put("sender_id", senderId)
                put("content", content.trim())
            }

            koneksi.outputStream.use { output ->
                output.write(data.toString().toByteArray(Charsets.UTF_8))
            }

            val kode = koneksi.responseCode

            if (kode in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Gagal mengirim pesan. Kode: $kode")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            koneksi?.disconnect()
        }
    }

    suspend fun unggahGambar(
        context: Context,
        uri: Uri,
        conversationId: String,
        accessToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var koneksi: HttpURLConnection? = null

        try {
            val mimeType = context.contentResolver.getType(uri)
                ?: "image/jpeg"

            if (!mimeType.startsWith("image/")) {
                return@withContext Result.failure(
                    IllegalArgumentException("File yang dipilih bukan gambar.")
                )
            }

            val namaFile = "${UUID.randomUUID()}.jpg"
            val path = "$conversationId/$namaFile"

            val bytes = context.contentResolver
                .openInputStream(uri)
                ?.use { it.readBytes() }
                ?: throw Exception("Gambar tidak dapat dibaca.")

            val url = SupabaseConfig.SUPABASE_URL.trimEnd('/') +
                    "/storage/v1/object/chat-images/$path"

            koneksi = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20000
                readTimeout = 20000
                doOutput = true

                setRequestProperty("apikey", SupabaseConfig.SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", mimeType)
                setRequestProperty("x-upsert", "false")
            }

            koneksi.outputStream.use { output ->
                output.write(bytes)
            }

            val kode = koneksi.responseCode

            if (kode !in 200..299) {
                val detail = koneksi.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()

                throw Exception("Upload gambar gagal ($kode): $detail")
            }

            Result.success(path)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            koneksi?.disconnect()
        }
    }


    suspend fun unggahVoiceNote(
        file: File,
        conversationId: String,
        accessToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var koneksi: HttpURLConnection? = null

        try {
            val namaFile = "${UUID.randomUUID()}.m4a"
            val path = "$conversationId/$namaFile"

            val url = SupabaseConfig.SUPABASE_URL.trimEnd('/') +
                    "/storage/v1/object/chat-audio/$path"

            koneksi = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20000
                readTimeout = 20000
                doOutput = true

                setRequestProperty("apikey", SupabaseConfig.SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "audio/mp4")
                setRequestProperty("x-upsert", "false")
            }

            file.inputStream().use { input ->
                koneksi.outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val kode = koneksi.responseCode

            if (kode !in 200..299) {
                val detail = koneksi.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()

                throw Exception("Upload audio gagal ($kode): $detail")
            }

            Result.success(path)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            koneksi?.disconnect()
        }
    }


    fun urlGambar(path: String): String {
        return "${SupabaseConfig.SUPABASE_URL.trimEnd('/')}/storage/v1/object/authenticated/chat-images/$path"
    }
    fun urlAudio(path: String): String {
        return "${SupabaseConfig.SUPABASE_URL.trimEnd('/')}/storage/v1/object/authenticated/chat-audio/$path"
    }

}
