
package com.example.ruangpulih

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class DataKlien(
    val id: String,
    val nama: String
)

object SupabaseKlien {

    suspend fun ambilKlien(
        accessToken: String,
        psikologId: String
    ): List<DataKlien> = withContext(Dispatchers.IO) {

        val hasil = mutableListOf<DataKlien>()
        var koneksi: HttpURLConnection? = null

        try {
            val url = URL(
                "${SupabaseConfig.SUPABASE_URL}/rest/v1/conversations" +
                        "?psychologist_id=eq.$psikologId" +
                        "&select=client_id,profiles!conversations_client_id_fkey(full_name)"
            )

            koneksi = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty(
                    "apikey",
                    SupabaseConfig.SUPABASE_KEY
                )
                setRequestProperty(
                    "Authorization",
                    "Bearer $accessToken"
                )
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15000
                readTimeout = 15000
            }

            if (koneksi.responseCode in 200..299) {
                val isi = koneksi.inputStream.bufferedReader().use {
                    it.readText()
                }
                val data = JSONArray(isi)

                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    val profil = item.optJSONObject("profiles")
                    val id = item.optString("client_id")
                    val nama = profil?.optString("full_name").orEmpty()

                    if (id.isNotBlank() && nama.isNotBlank() &&
                        hasil.none { it.id == id }
                    ) {
                        hasil.add(DataKlien(id, nama))
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Gagal mengambil daftar klien dari Supabase.")
        } finally {
            koneksi?.disconnect()
        }

        hasil
    }
}
