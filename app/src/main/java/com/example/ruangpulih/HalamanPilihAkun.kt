
package com.example.ruangpulih

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HijauRuangPulih = Color(0xFF19765F)
private val BiruPsikolog = Color(0xFF285DA8)
private val LatarRuangPulih = Color(0xFFF6FAF8)

@Composable
fun HalamanPilihAkun(
    onPilihKlien: () -> Unit,
    onPilihPsikolog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LatarRuangPulih)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "💚",
            fontSize = 58.sp
        )

        Text(
            text = "RuangPulih",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = HijauRuangPulih
        )

        Text(
            text = "Teman Sehat, Bersama",
            fontSize = 16.sp,
            color = Color(0xFF496B69)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Pilih jenis akun untuk melanjutkan",
            fontSize = 18.sp,
            color = Color(0xFF426663),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Kartu Klien
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPilihKlien() },
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0F1E8)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌿", fontSize = 48.sp)
                    Text("🤍", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = "Saya Klien",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = HijauRuangPulih
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Masuk untuk konsultasi, mengisi jurnal, " +
                                "dan menggunakan fitur relaksasi.",
                        fontSize = 14.sp,
                        color = Color(0xFF416762),
                        lineHeight = 21.sp
                    )
                }

                Text(
                    text = "›",
                    fontSize = 38.sp,
                    color = HijauRuangPulih
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Kartu Psikolog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPilihPsikolog() },
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE2ECFC)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🩺", fontSize = 48.sp)
                    Text("📋", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = "Saya Psikolog",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiruPsikolog
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Masuk untuk mengelola klien, melihat " +
                                "jadwal konsultasi, dan menggunakan fitur chat.",
                        fontSize = 14.sp,
                        color = Color(0xFF3D5F88),
                        lineHeight = 21.sp
                    )
                }

                Text(
                    text = "›",
                    fontSize = 38.sp,
                    color = BiruPsikolog
                )
            }
        }

        Spacer(modifier = Modifier.height(38.dp))

        Text(
            text = "♥   Kesehatan mental itu penting   ♥",
            fontSize = 14.sp,
            color = HijauRuangPulih,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
