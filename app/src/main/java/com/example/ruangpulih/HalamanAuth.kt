
package com.example.ruangpulih

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


@Composable
fun HalamanAuth(
    jenisAkun: String = "client",
    saatBerhasil: () -> Unit
) {

    var modeDaftar by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var namaLengkap by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var konfirmasiPassword by remember { mutableStateOf("") }
    var pesan by remember { mutableStateOf("") }
    var sedangMemproses by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val hijau = Color(0xFF367C68)
    val krem = Color(0xFFF8FAF7)
    val teksGelap = Color(0xFF263B35)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(krem)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌿", fontSize = 54.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "RuangPulih",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = hijau
        )

        Text(
            text = "Ruang aman untuk memahami dirimu.",
            color = teksGelap
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (modeDaftar) "Buat Akun Baru" else "Selamat Datang",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = teksGelap
                )

                Text(
                    text = if (modeDaftar)
                        "Mulai perjalananmu bersama RuangPulih."
                    else
                        "Masuk untuk melanjutkan perjalananmu."
                )


                if (modeDaftar) {
                    OutlinedTextField(
                        value = namaLengkap,
                        onValueChange = { namaLengkap = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }


                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (modeDaftar) {
                    OutlinedTextField(
                        value = konfirmasiPassword,
                        onValueChange = {
                            konfirmasiPassword = it
                        },
                        label = { Text("Konfirmasi Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (pesan.isNotBlank()) {
                    Text(
                        text = pesan,
                        color = if (pesan.contains("berhasil", true))
                            hijau else Color(0xFFB3261E)
                    )
                }

                Button(
                    onClick = {
                        pesan = ""

                        when {
                            email.isBlank() || password.isBlank() -> {
                                pesan = "Email dan password wajib diisi."
                            }

                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(email.trim()).matches() -> {
                                pesan = "Format email belum benar."
                            }

                            password.length < 6 -> {
                                pesan = "Password minimal 6 karakter."
                            }

                            modeDaftar &&
                                    password != konfirmasiPassword -> {
                                pesan = "Konfirmasi password tidak cocok."
                            }

                            modeDaftar -> {
                                scope.launch {
                                    sedangMemproses = true

                                    try {
                                        val hasil = SupabaseAuth.daftar(
                                            email = email,
                                            password = password,
                                            namaLengkap = namaLengkap
                                        )


                                        pesan = hasil.pesan
                                    } catch (_: Exception) {
                                        pesan = "Pendaftaran gagal. Coba lagi."
                                    }

                                    sedangMemproses = false
                                }
                            }



                            else -> {
                                scope.launch {
                                    sedangMemproses = true

                                    try {
                                        val hasil = SupabaseAuth.login(
                                            email = email.trim(),
                                            password = password
                                        )

                                        pesan = hasil.pesan

                                        if (hasil.berhasil && hasil.accessToken != null) {
                                            val userId = hasil.userId

                                            if (userId.isNullOrBlank()) {
                                                pesan = "ID pengguna tidak ditemukan."
                                            } else {
                                                val role = SupabaseAuth.ambilRole(
                                                    userId = userId,
                                                    accessToken = hasil.accessToken
                                                )

                                                if (role == jenisAkun) {
                                                    val prefs = context.getSharedPreferences(
                                                        "RuangPulihSession",
                                                        android.content.Context.MODE_PRIVATE
                                                    )

                                                    prefs.edit()
                                                        .putString("access_token", hasil.accessToken)
                                                        .putString("refresh_token", hasil.refreshToken)
                                                        .putString("user_id", userId)
                                                        .putString("role", role)
                                                        .apply()

                                                    saatBerhasil()
                                                } else if (role == "client" || role == "psychologist") {
                                                    pesan = if (jenisAkun == "psychologist") {
                                                        "Akun ini bukan akun psikolog. Silakan pilih Klien."
                                                    } else {
                                                        "Akun ini bukan akun klien. Silakan pilih Psikolog."
                                                    }
                                                } else {
                                                    pesan = "Role akun belum ditemukan. Periksa profil di Supabase."
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        pesan = "Login gagal. Silakan coba lagi."
                                    }

                                    sedangMemproses = false
                                }
                            }


                        }
                    },
                    enabled = !sedangMemproses,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = hijau
                    )
                ) {
                    Text(
                        if (sedangMemproses) "Memproses..."
                        else if (modeDaftar) "Daftar Sekarang"
                        else "Masuk"
                    )
                }

                TextButton(
                    onClick = {
                        modeDaftar = !modeDaftar
                        pesan = ""
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        if (modeDaftar)
                            "Sudah punya akun? Login"
                        else
                            "Belum punya akun? Daftar",
                        color = hijau
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Kamu berhak mendapatkan ruang untuk bertumbuh 💚",
            color = teksGelap,
            fontSize = 12.sp
        )
    }
}
