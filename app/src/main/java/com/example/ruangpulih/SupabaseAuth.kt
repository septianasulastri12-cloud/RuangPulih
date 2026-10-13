
package com.example.ruangpulih

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


data class AuthResult(
    val berhasil: Boolean,
    val pesan: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userId: String? = null
)


object SupabaseAuth {

    suspend fun daftar(
        email: String,
        password: String,
        namaLengkap: String
    ): AuthResult = withContext(Dispatchers.IO) {

        var koneksi: HttpURLConnection? = null

        try {
            val url = SupabaseConfig.SUPABASE_URL
                .trimEnd('/') + "/auth/v1/signup"

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
                    "Content-Type",
                    "application/json"
                )
            }

            val data = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
                put("data", JSONObject().apply {
                    put("full_name", namaLengkap.trim())
                })
            }

            koneksi.outputStream.use { output ->
                output.write(data.toString().toByteArray(Charsets.UTF_8))
            }

            val kode = koneksi.responseCode
            val stream = if (kode in 200..299) {
                koneksi.inputStream
            } else {
                koneksi.errorStream
            }

            val isiRespons = stream?.bufferedReader()?.use {
                it.readText()
            }.orEmpty()


            if (kode in 200..299) {
                val json = JSONObject(isiRespons)
                val userId = json.optJSONObject("user")
                    ?.optString("id")
                    ?.takeIf { it.isNotBlank() }
                    ?: json.optString("id")
                        .takeIf { it.isNotBlank() }

                AuthResult(
                    berhasil = true,
                    pesan = "Pendaftaran berhasil. Periksa email kamu jika diminta melakukan verifikasi.",
                    userId = userId
                )
            }
                else {
                val pesanError = try {
                    JSONObject(isiRespons).optString(
                        "msg",
                        JSONObject(isiRespons).optString(
                            "message",
                            "Pendaftaran gagal. Periksa email dan password."
                        )
                    )
                } catch (_: Exception) {
                    "Pendaftaran gagal. Periksa koneksi dan konfigurasi Supabase."
                }

                AuthResult(
                    berhasil = false,
                    pesan = pesanError
                )
            }

        } catch (_: Exception) {
            AuthResult(
                berhasil = false,
                pesan = "Tidak dapat terhubung. Periksa koneksi internet dan konfigurasi Supabase."
            )
        } finally {
            koneksi?.disconnect()
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {

        var koneksi: HttpURLConnection? = null

        try {
            val url = SupabaseConfig.SUPABASE_URL
                .trimEnd('/') +
                    "/auth/v1/token?grant_type=password"

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
                    "Content-Type",
                    "application/json"
                )
            }

            val data = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
            }

            koneksi.outputStream.use { output ->
                output.write(
                    data.toString().toByteArray(Charsets.UTF_8)
                )
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

            if (kode in 200..299) {
                val json = JSONObject(respons)

                val accessToken = json.optString("access_token")
                    .takeIf { it.isNotBlank() }

                val refreshToken = json.optString("refresh_token")
                    .takeIf { it.isNotBlank() }

                val userId = json.optJSONObject("user")
                    ?.optString("id")
                    ?.takeIf { it.isNotBlank() }

                if (accessToken != null) {
                    AuthResult(
                        berhasil = true,
                        pesan = "Login berhasil.",
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        userId = userId
                    )
                } else {
                    AuthResult(
                        berhasil = false,
                        pesan = "Token login tidak ditemukan."
                    )
                }
            } else {
                AuthResult(
                    berhasil = false,
                    pesan = try {
                        val json = JSONObject(respons)
                        json.optString(
                            "msg",
                            json.optString(
                                "message",
                                "Login gagal. Periksa email dan password."
                            )
                        )
                    } catch (_: Exception) {
                        "Login gagal. Periksa email dan password."
                    }
                )
            }

        } catch (_: Exception) {
            AuthResult(
                berhasil = false,
                pesan = "Tidak dapat terhubung. Periksa internet kamu."
            )
        } finally {
            koneksi?.disconnect()
        }
    }

    suspend fun ambilRole(
        userId: String,
        accessToken: String
    ): String? = withContext(Dispatchers.IO) {
        var koneksi: HttpURLConnection? = null

        try {
            val url = SupabaseConfig.SUPABASE_URL.trimEnd('/') +
                    "/rest/v1/profiles?id=eq.$userId&select=role"

            koneksi = (URL(url).openConnection()
                    as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000

                setRequestProperty(
                    "apikey",
                    SupabaseConfig.SUPABASE_KEY
                )
                setRequestProperty(
                    "Authorization",
                    "Bearer $accessToken"
                )
            }

            val kode = koneksi.responseCode

            if (kode in 200..299) {
                val respons = koneksi.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                val data = org.json.JSONArray(respons)

                if (data.length() > 0) {
                    data.getJSONObject(0).optString("role")
                        .takeIf { it.isNotBlank() }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            koneksi?.disconnect()
        }
    }

}
