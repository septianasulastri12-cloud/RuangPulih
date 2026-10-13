
package com.example.ruangpulih

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HijauPsikolog = Color(0xFF367C68)
private val LatarPsikolog = Color(0xFFF8FAF7)


@Composable
fun HalamanKlienPsikolog() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var daftarKlien by remember {
        mutableStateOf<List<DataKlien>>(emptyList())
    }
    var sedangMemuat by remember { mutableStateOf(false) }
    var pesan by remember { mutableStateOf("") }

    val prefs = remember {
        context.getSharedPreferences(
            "RuangPulihSession",
            android.content.Context.MODE_PRIVATE
        )
    }

    val token = prefs.getString("access_token", null)
    val psikologId = prefs.getString("user_id", null)

    LaunchedEffect(token, psikologId) {
        if (token.isNullOrBlank() || psikologId.isNullOrBlank()) {
            pesan = "Sesi login tidak ditemukan. Silakan login kembali."
        } else {
            sedangMemuat = true
            pesan = ""

            try {
                daftarKlien = SupabaseKlien.ambilKlien(
                    accessToken = token,
                    psikologId = psikologId
                )

                if (daftarKlien.isEmpty()) {
                    pesan = "Belum ada klien yang terhubung."
                }
            } catch (e: Exception) {
                pesan = e.message ?: "Gagal memuat daftar klien."
            } finally {
                sedangMemuat = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LatarPsikolog)
            .padding(24.dp)
    ) {
        Text(
            text = "Manajemen Klien 👥",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = HijauPsikolog
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Kelola daftar klien yang terhubung denganmu.")

        Spacer(modifier = Modifier.height(24.dp))

        when {
            sedangMemuat -> {
                CircularProgressIndicator(color = HijauPsikolog)
            }

            pesan.isNotBlank() -> {
                Text(text = pesan, color = HijauPsikolog)
            }

            else -> {
                daftarKlien.forEach { klien ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = klien.nama,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Klien RuangPulih")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HalamanJadwalPsikolog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LatarPsikolog)
            .padding(24.dp)
    ) {
        Text(
            text = "Jadwal Konsultasi 📅",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = HijauPsikolog
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Atur jadwal konsultasi dengan klien.")

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Jadwal Saya",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Jadwal konsultasi akan ditampilkan di sini setelah fitur jadwal dibuat."
                )
            }
        }
    }
}
