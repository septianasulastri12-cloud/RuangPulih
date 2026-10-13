
package com.example.ruangpulih

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.shape.CircleShape
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField


private val Green = Color(0xFF367C68L)
private val LightGreen = Color(0xFFEAF5EFL)
private val DarkText = Color(0xFF263B35L)
private val MutedText = Color(0xFF718078L)
private val Cream = Color(0xFFF8FAF7L)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RuangPulihApp()
            }
        }
    }
}


private fun saveMood(
    context: Context,
    mood: String,
    note: String
) {
    val prefs = context.getSharedPreferences(
        "ruangpulih_moods",
        Context.MODE_PRIVATE
    )

    val oldHistory = prefs.getString("history", "") ?: ""
    val date = java.text.SimpleDateFormat(
        "dd-MM-yyyy HH:mm",
        java.util.Locale.getDefault()
    ).format(java.util.Date())

    val entry = "$date | $mood | $note"
    val newHistory = if (oldHistory.isEmpty()) {
        entry
    } else {
        "$entry\n$oldHistory"
    }

    prefs.edit()
        .putString("history", newHistory)
        .apply()
}


@Composable
fun RuangPulihHome(
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf("") }
    var moodNote by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Cream
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "RuangPulih 🌿",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ruang aman untuk memahami dirimu.",
                fontSize = 14.sp,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(28.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFE5EFE7),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(22.dp)
            ) {
                Text(
                    text = "RUANG AMANMU 🌿",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Hai, kamu! 💚",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Tidak apa-apa jika hari ini terasa berat. Kamu berhak beristirahat dan memberi ruang bagi dirimu sendiri.",
                    fontSize = 15.sp,
                    color = DarkText,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF7FAF7),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Pelan-pelan, ya. Kamu tidak harus baik-baik saja setiap saat.",
                        fontSize = 13.sp,
                        color = Green,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }


            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Bagaimana perasaanmu hari ini?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Yuk, luangkan waktu untuk mengenali perasaanmu.",
                fontSize = 14.sp,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(18.dp))

            val moods = listOf(
                "😊" to "Senang",
                "😌" to "Tenang",
                "😐" to "Biasa saja",
                "😔" to "Sedih",
                "😣" to "Cemas"
            )

            moods.chunked(3).forEach { rowMoods ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowMoods.forEach { mood ->
                        val isSelected = selectedMood == mood.second


                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) Color(0xFFDCEADF)
                                    else Color.White,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    selectedMood = mood.second
                                }
                                .padding(vertical = 16.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = mood.first,
                                fontSize = 30.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))


                            Text(
                                text = mood.second,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    repeat(3 - rowMoods.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "📝 Ceritakan perasaanmu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = moodNote,
                    onValueChange = { moodNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Apa yang kamu rasakan hari ini?")
                    },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green,
                        unfocusedBorderColor = Color(0xFFDCE5DE),
                        focusedContainerColor = Color(0xFFFCFDFC),
                        unfocusedContainerColor = Color(0xFFFCFDFC)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        saveMood(context, selectedMood, moodNote)
                        moodNote = ""
                        selectedMood = ""
                    },
                    enabled = selectedMood.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        disabledContainerColor = Color(0xFFDCE5DE)
                    )
                ) {
                    Text(
                        text = "Simpan Mood 🌿",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }


            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "✨ Ruang untuk dirimu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Kamu bisa memulai dengan menarik napas perlahan, mengenali perasaanmu, dan beristirahat sejenak.",
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            OutlinedButton(
                onClick = {
                    showHistory = !showHistory
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Green
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFFD5E3D8)
                )
            ) {
                Text(
                    text = if (showHistory) {
                        "Tutup Riwayat Mood ↑"
                    } else {
                        "Lihat Riwayat Mood ↓"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }


            if (showHistory) {
                val history = context
                    .getSharedPreferences(
                        "ruangpulih_moods",
                        Context.MODE_PRIVATE
                    )
                    .getString("history", "")
                    .orEmpty()

                Spacer(modifier = Modifier.height(12.dp))


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "📖 Riwayat Mood",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (history.isBlank()) {
                            Text(
                                text = "Belum ada mood yang tersimpan. Yuk, catat perasaanmu hari ini!",
                                color = MutedText
                            )
                        } else {
                            Text(
                                text = history,
                                color = DarkText,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }


            Text(
                text = "RuangPulih • Bertumbuh dengan caramu sendiri",
                fontSize = 12.sp,
                color = MutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


@Composable
fun RuangPulihApp() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "RuangPulihSession",
            Context.MODE_PRIVATE
        )
    }

    var sudahLogin by remember {
        mutableStateOf(
            !prefs.getString("access_token", null).isNullOrBlank()
        )
    }

    var jenisAkunDipilih by remember {
        mutableStateOf<String?>(null)
    }

    val role = prefs.getString("role", "client") ?: "client"
    val userId = prefs.getString("user_id", null)
    val accessToken = prefs.getString("access_token", null)


    if (!sudahLogin) {
        if (jenisAkunDipilih == null) {
            HalamanPilihAkun(
                onPilihKlien = {
                    jenisAkunDipilih = "client"
                },
                onPilihPsikolog = {
                    jenisAkunDipilih = "psychologist"
                }
            )
        } else {
            HalamanAuth(
                jenisAkun = jenisAkunDipilih!!,
                saatBerhasil = {
                    sudahLogin = true
                    jenisAkunDipilih = null
                }
            )
        }
    } else {
        val tabNames = if (role == "psychologist") {
            listOf("Beranda", "Klien", "Jadwal", "Chat", "Profil")
        } else {
            listOf("Beranda", "Jurnal", "Relaksasi", "Chat", "Profil")
        }

        val tabEmojis = if (role == "psychologist") {
            listOf("🏠", "👥", "📅", "💬", "👤")
        } else {
            listOf("🏠", "📖", "🌿", "💬", "👤")
        }

        var selectedTab by remember {
            mutableIntStateOf(0)
        }

        Scaffold(
            containerColor = Cream,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    tabNames.forEachIndexed { index, name ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Text(
                                    text = tabEmojis[index],
                                    fontSize = 20.sp
                                )
                            },
                            label = {
                                Text(name, fontSize = 11.sp)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Green,
                                selectedTextColor = Green,
                                indicatorColor = LightGreen,
                                unselectedTextColor = MutedText
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> RuangPulihHome()

                    1 -> if (role == "psychologist") {
                        HalamanKlienPsikolog()
                    } else {
                        HalamanJurnal()
                    }

                    2 -> if (role == "psychologist") {
                        HalamanJadwalPsikolog()
                    } else {
                        HalamanRelaksasi()
                    }


                    3 -> HalamanChat(
                        userId = userId,
                        accessToken = accessToken,
                        role = role
                    )


                    4 -> HalamanProfil(
                        saatKeluar = {
                            prefs.edit().clear().apply()
                            selectedTab = 0
                            sudahLogin = false
                            jenisAkunDipilih = null
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun HalamanSederhana(
    judul: String,
    deskripsi: String,
    emoji: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = emoji,
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = judul,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Green,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = deskripsi,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            color = MutedText,
            textAlign = TextAlign.Center
        )
    }
}



@Composable
fun HalamanProfil(
    saatKeluar: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var fotoProfil by rememberSaveable {
        mutableStateOf(
            context.getSharedPreferences(
                "RuangPulih",
                Context.MODE_PRIVATE
            ).getString("foto_profil", null)
        )
    }

    val pilihFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoProfil = it.toString()

            context.getSharedPreferences(
                "RuangPulih",
                Context.MODE_PRIVATE
            ).edit()
                .putString("foto_profil", it.toString())
                .apply()
        }
    }
    val preferences = remember {
        context.getSharedPreferences(
            "RuangPulih",
            Context.MODE_PRIVATE
        )
    }

    var nama by rememberSaveable {
        mutableStateOf(
            preferences.getString("nama_pengguna", "") ?: ""
        )
    }

    var editProfil by rememberSaveable {
        mutableStateOf(false)
    }

    val moodPreferences = remember {
        context.getSharedPreferences(
            "ruangpulih_moods",
            Context.MODE_PRIVATE
        )
    }

    var riwayatMood by remember {
        mutableStateOf(
            moodPreferences.getString("history", "").orEmpty()
        )
    }

    val daftarMood = riwayatMood
        .lineSequence()
        .filter { it.isNotBlank() }
        .toList()

    val moodDominan = daftarMood
        .mapNotNull { entri ->
            entri.split(" | ").getOrNull(1)
        }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key ?: "Belum ada"

    val afirmasi = listOf(
        "Kamu tidak harus selalu kuat. Kamu juga berhak beristirahat. 💚",
        "Langkah kecilmu hari ini tetap berarti. 🌱",
        "Tidak apa-apa berjalan pelan, setiap orang punya waktunya sendiri. 🌿"
    )

    var indeksAfirmasi by rememberSaveable {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Profil Saya 🌿",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Green
        )

        Text(
            text = "Ruang pribadi untuk bertumbuh.",
            fontSize = 14.sp,
            color = MutedText
        )

        // Kartu profil
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE5EFE7)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1E5D5))
                        .clickable {
                            pilihFoto.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoProfil != null) {
                        Image(
                            painter = rememberAsyncImagePainter(fotoProfil),
                            contentDescription = "Foto profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🌿", fontSize = 42.sp)
                    }
                }

                TextButton(
                    onClick = {
                        pilihFoto.launch("image/*")
                    }
                ) {
                    Text("📷 Ganti Foto Profil", color = Green)
                }



                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (nama.isBlank()) {
                        "Teman RuangPulih"
                    } else {
                        "Halo, $nama!"
                    },
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Senang melihatmu terus bertumbuh 💚",
                    fontSize = 13.sp,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        editProfil = !editProfil
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        if (editProfil) "Tutup Edit Profil"
                        else "✎ Edit Profil"
                    )
                }

                if (editProfil) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama kamu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            nama = nama.trim()
                            preferences.edit()
                                .putString("nama_pengguna", nama)
                                .apply()
                            editProfil = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green
                        )
                    ) {
                        Text("Simpan Profil")
                    }
                }
            }
        }

        // Statistik perjalanan emosional
        Text(
            text = "Perjalanan Emosiku 💚",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("📝", fontSize = 25.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = daftarMood.size.toString(),
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green
                    )

                    Text(
                        text = "Mood tercatat",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("💗", fontSize = 25.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = moodDominan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green
                    )

                    Text(
                        text = "Mood dominan",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }
        }

        // Afirmasi positif
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE5EFE7)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🌱 Pesan untuk Dirimu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "“${afirmasi[indeksAfirmasi]}”",
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        indeksAfirmasi =
                            (indeksAfirmasi + 1) % afirmasi.size
                    }
                ) {
                    Text(
                        "Pesan lainnya ↻",
                        color = Green
                    )
                }
            }
        }

        // Pengaturan akun
        Text(
            text = "Pengaturan Akun",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👤", fontSize = 22.sp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "Profil pribadi",
                            fontWeight = FontWeight.SemiBold,
                            color = DarkText
                        )
                        Text(
                            "Kelola nama pengguna",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = { editProfil = true }
                    ) {
                        Text("Ubah", color = Green)
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFE8EEE9)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 22.sp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "Privasi",
                            fontWeight = FontWeight.SemiBold,
                            color = DarkText
                        )
                        Text(
                            "Jaga kerahasiaan ceritamu",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFE8EEE9)
                )

                TextButton(
                    onClick = saatKeluar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Keluar dari Akun",
                        color = Color(0xFFB64C4C),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Text(
            text = "RuangPulih • Bertumbuh dengan caramu sendiri 🌿",
            fontSize = 12.sp,
            color = MutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}


@Composable
fun HalamanJurnal() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val preferencesFoto = remember {
        context.getSharedPreferences("RuangPulih", Context.MODE_PRIVATE)
    }

    var fotoProfil by rememberSaveable {
        mutableStateOf(
            preferencesFoto.getString("foto_profil", null)
        )
    }

    val pilihFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoProfil = it.toString()
            preferencesFoto.edit()
                .putString("foto_profil", it.toString())
                .apply()
        }
    }

    var isiJurnal by rememberSaveable { mutableStateOf("") }
    var rasaSyukur by rememberSaveable { mutableStateOf("") }
    var pesan by remember { mutableStateOf("") }

    val prefs = context.getSharedPreferences(
        "ruangpulih_jurnal",
        android.content.Context.MODE_PRIVATE
    )

    var daftarJurnal by remember {
        mutableStateOf(
            prefs.getString("riwayat", "").orEmpty()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {
        Text(
            text = "Jurnal Harian 📖",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Green
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tidak perlu menyimpan semuanya sendiri. Kamu bisa mulai dengan menuliskannya.",
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Apa yang ingin kamu ceritakan?",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = isiJurnal,
            onValueChange = { isiJurnal = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Ceritakan apa yang kamu rasakan hari ini...")
            },
            minLines = 5
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Apa yang kamu syukuri hari ini? 🌷",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = rasaSyukur,
            onValueChange = { rasaSyukur = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Hal kecil pun boleh kamu syukuri...")
            },
            minLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val cerita = isiJurnal.trim()
                val syukur = rasaSyukur.trim()

                if (cerita.isNotEmpty() || syukur.isNotEmpty()) {
                    val tanggal = java.text.SimpleDateFormat(
                        "dd-MM-yyyy HH:mm",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())

                    val entri = buildString {
                        append("Tanggal: $tanggal\n")
                        if (cerita.isNotEmpty()) {
                            append("Cerita: $cerita\n")
                        }
                        if (syukur.isNotEmpty()) {
                            append("Syukur: $syukur\n")
                        }
                    }

                    val hasil = if (daftarJurnal.isBlank()) {
                        entri
                    } else {
                        "$entri\n--------------------\n$daftarJurnal"
                    }

                    prefs.edit()
                        .putString("riwayat", hasil)
                        .apply()

                    daftarJurnal = hasil
                    isiJurnal = ""
                    rasaSyukur = ""
                    pesan = "Jurnal berhasil disimpan 💚"
                } else {
                    pesan = "Tuliskan ceritamu atau rasa syukurmu dulu, ya."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green
            )
        ) {
            Text("Simpan Jurnal")
        }

        if (pesan.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = pesan, color = Green, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Jurnal Sebelumnya",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (daftarJurnal.isBlank()) {
            Text(
                text = "Belum ada tulisan. Kamu bisa mulai kapan saja 🌿",
                color = MutedText,
                fontSize = 14.sp
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = daftarJurnal,
                    modifier = Modifier.padding(16.dp),
                    color = DarkText,
                    lineHeight = 23.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HalamanRelaksasi() {
    var sedangLatihan by remember { mutableStateOf(false) }
    var tahap by remember { mutableIntStateOf(0) }

    LaunchedEffect(sedangLatihan) {
        while (sedangLatihan) {
            delay(4000)
            tahap = (tahap + 1) % 3
        }
    }


    val instruksi = listOf(
        "Tarik napas perlahan 🌬️",
        "Tahan napas sejenak 🌿",
        "Hembuskan napas dengan lembut 🍃"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Ruang Relaksasi 🌿",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Green
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Luangkan waktu sejenak untuk dirimu.",
            fontSize = 14.sp,
            color = MutedText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Box(
            modifier = Modifier
                .size(190.dp)
                .background(
                    color = LightGreen,
                    shape = RoundedCornerShape(100.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (sedangLatihan) {
                    listOf("🌬️", "🌿", "🍃")[tahap]
                } else {
                    "💚"
                },
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (sedangLatihan) {
                instruksi[tahap]
            } else {
                "Siap menenangkan pikiran?"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (sedangLatihan) {
                "Ikuti panduan dengan nyaman, tanpa memaksakan napas."
            } else {
                "Duduk nyaman dan bernapaslah dengan alami."
            },
            fontSize = 14.sp,
            color = MutedText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                sedangLatihan = !sedangLatihan
                if (sedangLatihan) {
                    tahap = 0
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Green
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (sedangLatihan) "Jeda Latihan"
                else "Mulai Latihan"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (sedangLatihan) {
            OutlinedButton(
                onClick = {
                    sedangLatihan = false
                    tahap = 0
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selesai Latihan")
            }
        }
    }
}


