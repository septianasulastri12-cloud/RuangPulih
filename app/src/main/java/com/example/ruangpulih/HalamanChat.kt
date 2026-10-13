
package com.example.ruangpulih


import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import android.media.MediaPlayer
import androidx.compose.runtime.DisposableEffect
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat
import java.io.File
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class PesanTampilan(
    val teks: String,
    val dariSaya: Boolean
)


@Composable
fun HalamanChat(
    userId: String?,
    accessToken: String?,
    role: String,
    namaPsikolog: String = "Psikolog RuangPulih"
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var gambarDiperbesar by remember {
        mutableStateOf<String?>(null)
    }
    var sedangUploadGambar by remember {
        mutableStateOf(false)
    }


    var conversationId by remember {
        mutableStateOf<String?>(null)
    }

    var sedangMemuat by remember {
        mutableStateOf(false)
    }

    var pesanError by remember {
        mutableStateOf<String?>(null)
    }


    var perekamSuara by remember { mutableStateOf<MediaRecorder?>(null) }
    var fileRekaman by remember { mutableStateOf<File?>(null) }
    var sedangMerekam by remember { mutableStateOf(false) }
    var pesanSuara by remember { mutableStateOf("") }
    var sedangMemutarAudio by remember {
        mutableStateOf(false)
    }
    var pemutarAudio by remember {
        mutableStateOf<android.media.MediaPlayer?>(null)
    }

    val pemilihGambar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val idUser = userId
            val token = accessToken
            val idPercakapan = conversationId

            if (idUser != null && token != null && idPercakapan != null) {
                scope.launch {
                    sedangUploadGambar = true

                    val hasil = SupabaseChat.unggahGambar(
                        context = context,
                        uri = uri,
                        conversationId = idPercakapan,
                        accessToken = token
                    )

                    hasil.onSuccess { path ->
                        val hasilKirim = SupabaseChat.kirimPesan(
                            conversationId = idPercakapan,
                            senderId = idUser,
                            content = "image:$path",
                            accessToken = token
                        )

                        hasilKirim.onSuccess {
                            pesanError = "Gambar berhasil dikirim."
                        }.onFailure { error ->
                            pesanError = "Gambar terunggah, tetapi gagal dikirim: ${error.message}"
                        }
                    }.onFailure { error ->
                        pesanError = "Gagal mengunggah gambar: ${error.message}"
                    }

                    sedangUploadGambar = false
                }
            } else {
                pesanError = "Data login atau percakapan belum tersedia."
            }
        }
    }


    fun mulaiRekamSuara() {
        try {
            val file = File(
                context.cacheDir,
                "voice_${System.currentTimeMillis()}.m4a"
            )


            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(file.absolutePath)

            recorder.prepare()
            recorder.start()

            perekamSuara = recorder
            fileRekaman = file
            sedangMerekam = true
            pesanSuara = "Sedang merekam suara..."
        } catch (e: Exception) {
            perekamSuara?.release()
            perekamSuara = null
            sedangMerekam = false
            pesanSuara = "Gagal merekam: ${e.message}"
        }
    }

    fun berhentiRekamSuara(): File? {
        val recorder = perekamSuara
        val file = fileRekaman

        return try {
            if (recorder != null) {
                recorder.stop()
                recorder.release()
            }

            perekamSuara = null
            sedangMerekam = false

            if (file != null && file.exists() && file.length() > 0) {
                pesanSuara = "Rekaman selesai."
                file
            } else {
                pesanSuara = "File rekaman kosong."
                null
            }
        } catch (e: Exception) {
            recorder?.release()
            perekamSuara = null
            sedangMerekam = false
            pesanSuara = "Rekaman terlalu singkat atau gagal disimpan."
            null
        }
    }

    val izinMikrofon = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { diberikan ->
        if (diberikan) {
            mulaiRekamSuara()
        } else {
            pesanSuara = "Izin mikrofon diperlukan untuk merekam suara."
        }
    }



    var teksPesan by remember { mutableStateOf("") }
    var menuLampiranTerbuka by remember { mutableStateOf(false) }

    val daftarPesan = remember {
        mutableStateListOf(
            PesanTampilan(
                "Halo, selamat datang di RuangPulih. Apa yang sedang kamu rasakan hari ini?",
                false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF7))
    ) {

        LaunchedEffect(userId, accessToken, role) {
            android.util.Log.d(
                "RuangPulihChat",
                "userId=$userId, role=$role, tokenAda=${accessToken != null}"
            )
            if (userId != null && accessToken != null) {
                sedangMemuat = true
                pesanError = null

                val hasilId = SupabaseChat.ambilConversationId(
                    userId = userId,
                    role = role,
                    accessToken = accessToken
                )
                hasilId.onFailure { error ->
                    pesanError = "Gagal mencari percakapan: ${error.message}"
                    android.util.Log.e(
                        "RuangPulihChat",
                        "Gagal mencari percakapan",
                        error
                    )
                }

                hasilId.onSuccess { id ->
                    conversationId = id

                    val hasilPesan = SupabaseChat.ambilPesan(
                        conversationId = id,
                        accessToken = accessToken
                    )

                    hasilPesan.onSuccess { pesanSupabase ->
                        daftarPesan.clear()

                        pesanSupabase.forEach { pesan ->
                            daftarPesan.add(
                                PesanTampilan(
                                    teks = pesan.content,
                                    dariSaya = pesan.senderId == userId
                                )
                            )
                        }
                    }.onFailure { error ->
                        pesanError = error.message ?: "Gagal memuat pesan."
                    }
                }.onFailure { error ->
                    pesanError = error.message ?: "Percakapan tidak ditemukan."
                }

                sedangMemuat = false
            } else {
                pesanError = "Silakan login kembali untuk membuka chat."
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F8F4))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE5EFE7))
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color(0xFFDCE9DF),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SR",
                            color = Color(0xFF365B45),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }


                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = namaPsikolog,
                            color = Color(0xFF254735),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Konsultasi kesehatan mental",
                            color = Color(0xFF66806D),
                            fontSize = 13.sp
                        )
                    }
                }
            }


        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(daftarPesan) { pesan ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (pesan.dariSaya) 40.dp else 0.dp,
                            end = if (pesan.dariSaya) 0.dp else 40.dp
                        ),
                    horizontalArrangement = if (pesan.dariSaya) {
                        Arrangement.End
                    } else {
                        Arrangement.Start
                    },
                    verticalAlignment = Alignment.Bottom
                ) {

                    if (pesan.teks.startsWith("image:")) {
                        val pathGambar = pesan.teks.removePrefix("image:")

                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(SupabaseChat.urlGambar(pathGambar))
                                .addHeader("Authorization", "Bearer ${accessToken.orEmpty()}")
                                .addHeader("apikey", SupabaseConfig.SUPABASE_KEY)
                                .build(),
                            contentDescription = "Gambar dalam chat",
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .heightIn(max = 300.dp)
                                .clickable {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Gambar berhasil diklik!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    gambarDiperbesar = pathGambar
                                },
                            contentScale = ContentScale.Fit
                        )

                    } else if (pesan.teks.startsWith("audio:")) {
                        val pathAudio = pesan.teks.removePrefix("audio:")

                        Row(
                            modifier = Modifier
                                .background(
                                    color = if (pesan.dariSaya) Color(0xFFD8EBDD) else Color.White,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val token = accessToken

                                    if (token.isNullOrBlank()) {
                                        pesanError = "Token login tidak tersedia."
                                        return@IconButton
                                    }

                                    try {
                                        pemutarAudio?.release()

                                        val player = MediaPlayer()
                                        pemutarAudio = player
                                        sedangMemutarAudio = true

                                        player.setDataSource(
                                            context,
                                            android.net.Uri.parse(
                                                SupabaseChat.urlAudio(pathAudio)
                                            ),
                                            mapOf(
                                                "Authorization" to "Bearer $token",
                                                "apikey" to SupabaseConfig.SUPABASE_KEY
                                            )
                                        )

                                        player.setOnPreparedListener {
                                            it.start()
                                        }

                                        player.setOnCompletionListener {
                                            it.release()
                                            pemutarAudio = null
                                            sedangMemutarAudio = false
                                        }

                                        player.setOnErrorListener { mp, _, _ ->
                                            mp.release()
                                            pemutarAudio = null
                                            sedangMemutarAudio = false
                                            pesanError = "Gagal memutar pesan suara."
                                            true
                                        }

                                        player.prepareAsync()
                                    } catch (e: Exception) {
                                        pemutarAudio?.release()
                                        pemutarAudio = null
                                        sedangMemutarAudio = false
                                        pesanError = "Gagal memutar pesan suara."
                                    }
                                }
                            ) {
                                Text("▶️")
                            }

                            Text(
                                text = "Pesan suara",
                                color = Color(0xFF26352B),
                                fontSize = 14.sp
                            )
                        }
                    } else {

                        Text(
                            text = pesan.teks,
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .background(
                                    color = if (pesan.dariSaya) {
                                        Color(0xFFD8EBDD)
                                    } else {
                                        Color.White
                                    },
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomEnd = if (pesan.dariSaya) 6.dp else 18.dp,
                                        bottomStart = if (pesan.dariSaya) 18.dp else 6.dp
                                    )
                                )
                                .padding(12.dp),
                            color = Color(0xFF26352B),
                            fontSize = 14.sp
                        )

                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = teksPesan,
                onValueChange = { teksPesan = it },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        Color(0xFFF0F4F0),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(14.dp),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (teksPesan.isEmpty()) {
                            Text(
                                "Tulis pesan...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Box {
                IconButton(
                    onClick = {
                        menuLampiranTerbuka = true
                    },
                    enabled = !sedangUploadGambar
                ) {


                    DropdownMenu(
                        expanded = menuLampiranTerbuka,
                        onDismissRequest = {
                            menuLampiranTerbuka = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Foto") },
                            onClick = {
                                menuLampiranTerbuka = false
                                pemilihGambar.launch("image/*")
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (sedangMerekam) "Hentikan rekaman"
                                    else "Pesan suara"
                                )
                            },
                            onClick = {
                                menuLampiranTerbuka = false

                                if (sedangMerekam) {
                                    val file = berhentiRekamSuara()
                                    val idUser = userId
                                    val token = accessToken
                                    val idPercakapan = conversationId

                                    if (
                                        file != null &&
                                        idUser != null &&
                                        token != null &&
                                        idPercakapan != null
                                    ) {
                                        scope.launch {
                                            val hasilUpload =
                                                SupabaseChat.unggahVoiceNote(
                                                    file = file,
                                                    conversationId = idPercakapan,
                                                    accessToken = token
                                                )

                                            hasilUpload.onSuccess { path ->
                                                val hasilKirim =
                                                    SupabaseChat.kirimPesan(
                                                        conversationId = idPercakapan,
                                                        senderId = idUser,
                                                        content = "audio:$path",
                                                        accessToken = token
                                                    )

                                                hasilKirim.onSuccess {
                                                    pesanSuara =
                                                        "Pesan suara berhasil dikirim."

                                                    val hasilPesan =
                                                        SupabaseChat.ambilPesan(
                                                            conversationId = idPercakapan,
                                                            accessToken = token
                                                        )

                                                    hasilPesan.onSuccess { daftar ->
                                                        daftarPesan.clear()
                                                        daftar.forEach { pesan ->
                                                            daftarPesan.add(
                                                                PesanTampilan(
                                                                    teks = pesan.content,
                                                                    dariSaya =
                                                                    pesan.senderId == idUser
                                                                )
                                                            )
                                                        }
                                                    }.onFailure { error ->
                                                        pesanError =
                                                            error.message
                                                                ?: "Gagal memuat ulang chat."
                                                    }
                                                }.onFailure { error ->
                                                    pesanError =
                                                        error.message
                                                            ?: "Gagal mengirim pesan suara."
                                                }
                                            }.onFailure { error ->
                                                pesanError =
                                                    error.message
                                                        ?: "Gagal mengunggah rekaman."
                                            }
                                        }
                                    } else {
                                        pesanError =
                                            "Data login atau percakapan belum tersedia."
                                    }
                                } else {
                                    val izinDiberikan =
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED

                                    if (izinDiberikan) {
                                        mulaiRekamSuara()
                                    } else {
                                        izinMikrofon.launch(
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                Text(
                    text = "＋",
                    color = Color(0xFF477A60),
                    fontSize = 28.sp
                )
            }



            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val isiPesan = teksPesan.trim()
                    val idUser = userId
                    val token = accessToken
                    val idPercakapan = conversationId

                    if (
                        isiPesan.isNotBlank() &&
                        idUser != null &&
                        token != null &&
                        idPercakapan != null
                    ) {
                        scope.launch {
                            val hasil = SupabaseChat.kirimPesan(
                                conversationId = idPercakapan,
                                senderId = idUser,
                                content = isiPesan,
                                accessToken = token
                            )

                            hasil.onSuccess {
                                teksPesan = ""

                                val pesanTerbaru = SupabaseChat.ambilPesan(
                                    conversationId = idPercakapan,
                                    accessToken = token
                                )

                                pesanTerbaru.onSuccess { daftar ->
                                    daftarPesan.clear()
                                    daftar.forEach { pesan ->
                                        daftarPesan.add(
                                            PesanTampilan(
                                                teks = pesan.content,
                                                dariSaya = pesan.senderId == idUser
                                            )
                                        )
                                    }
                                }.onFailure { error ->
                                    pesanError = error.message
                                        ?: "Pesan terkirim, tetapi gagal memuat ulang chat."
                                }
                            }.onFailure { error ->
                                pesanError = error.message ?: "Pesan gagal dikirim."
                            }
                        }
                    } else {
                        pesanError = when {
                            idUser == null -> "User ID belum terbaca. Silakan login ulang."
                            token == null -> "Token login belum terbaca. Silakan login ulang."
                            idPercakapan == null -> "ID percakapan belum ditemukan."
                            isiPesan.isBlank() -> "Pesan masih kosong."
                            else -> "Pesan belum dapat dikirim."
                        }
                    }
                },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF477A60),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Kirim",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (pesanSuara.isNotEmpty()) {
            Text(
                text = pesanSuara,
                color = Color(0xFF477A60),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        pesanError?.let { pesan ->
            Text(
                text = pesan,
                color = Color.Red,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
        }



