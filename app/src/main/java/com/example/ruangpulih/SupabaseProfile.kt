
package com.example.ruangpulih

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SupabaseProfile {

    suspend fun simpanProfil(
        userId: String,
        namaLengkap: String,
        accessToken: String
    ): Result<Unit> = withContext(Dispatchers.IO) {

        var koneksi: HttpURLConnection? = null

        try {
            val url = SupabaseConfig.SUPABASE_URL
                .trimEnd('/') + "/rest/v1/profiles"

            koneksi = (URL(url).openConnection()
                    as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true

                setRequestProperty(
                    "apikey",
                    SupabaseConfig.SUPABASE_KEY
                )
                setRequestProperty(
                    "Authorization",
                    "Bearer $accessToken"
                )
                setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
                setRequestProperty(
                    "Prefer",
                    "return=minimal"
                )
            }

            val data = JSONObject().apply {
                put("id", userId)
                put("full_name", namaLengkap)
                put("role", "client")
            }

            koneksi.outputStream.use { output ->
                output.write(
                    data.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val kode = koneksi.responseCode

            if (kode in 200..299) {
                Result.success(Unit)
            } else {
                val detail = koneksi.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()

                Result.failure(
                    Exception("Gagal menyimpan profil ($kode): $detail")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            koneksi?.disconnect()
        }
    }
}
