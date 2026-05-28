package com.example.radio_player_czsk

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    station: RadioStation,
    googleAuthUiClient: GoogleAuthUiClient,
    onNavigateToHome: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onLogOutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSignOutDialog by remember { mutableStateOf(false) }

    val currentUser = remember { googleAuthUiClient.getSignedInUser() }
    val userPhotoUrl = currentUser?.photoUrl
    val userDisplayName = currentUser?.displayName ?: context.getString(R.string.user)
    val firstLetter = (currentUser?.displayName ?: currentUser?.email ?: "R").take(1).uppercase()

    var trackTitle by remember { mutableStateOf("Živé vysielanie") }
    var playStartTime by remember { mutableStateOf<Long?>(null) }
    var isPlaying by remember { mutableStateOf(false) } // Presunuté vyššie kvôli prístupu v ExoPlayeri

    val exoPlayer = remember(station.url) {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("EXOPLAYER", "Chyba: ${error.message}")
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        Log.d("EXOPLAYER", "Stav: $state")
                    }
                    override fun onIsPlayingChanged(playing: Boolean) {
                        Log.d("EXOPLAYER", "Prehráva: $playing")
                        isPlaying = playing // 🔥 AUTOMATICKÁ SYNCHRONIZÁCIA: Ak rádio začne reálne hrať/pauzovať, stav sa zmení sám
                    }
                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                        val title = mediaMetadata.title
                        val artist = mediaMetadata.artist

                        trackTitle = when {
                            !title.isNullOrBlank() && !artist.isNullOrBlank() -> "$artist - $title"
                            !title.isNullOrBlank() -> title.toString()
                            else -> "Živé vysielanie"
                        }
                        Log.d("EXOPLAYER", "Aktuálna skladba: $trackTitle")
                    }
                })
                val mediaItem = MediaItem.fromUri(station.url)
                setMediaItem(mediaItem)
                playWhenReady = false
                prepare()
            }
    }

    // Funkcia na výpočet a bezpečné odoslanie nazbieraných sekúnd do Firebase
    val stopListeningAndSave = {
        if (playStartTime != null) {
            val elapsedMillis = System.currentTimeMillis() - playStartTime!!
            val elapsedSeconds = elapsedMillis / 1000
            if (elapsedSeconds > 0) {
                StatisticsManager.saveListeningTime(station.name, elapsedSeconds)
                Log.d("STATISTIKY", "Uložené z stopListeningAndSave: $elapsedSeconds sekúnd")
            }
            playStartTime = null
        }
    }

    // 💡 REAKTÍVNY ČASOVAČ: Riadi pridelenie času do premennej playStartTime a priebežné ukladanie
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // Rádio začalo hrať -> nastavíme štartovací bod na aktuálny čas v milisekundách
            playStartTime = System.currentTimeMillis()

            // Nekonečná slučka bežiaca na pozadí počas prehrávania
            while (true) {
                delay(30000L) // Počkáme 30 sekúnd
                val now = System.currentTimeMillis()
                val diff = (now - (playStartTime ?: now)) / 1000

                if (diff > 0) {
                    StatisticsManager.saveListeningTime(station.name, diff)
                    playStartTime = now // Posunieme štartovací bod na "teraz", aby sa čas nepočítal duplicitne
                }
            }
        } else {
            // Používateľ stlačil pauzu -> okamžite uložíme doterajší čas
            stopListeningAndSave()
        }
    }

    // Správa životného cyklu prehrávača a uloženie pri opustení obrazovky (Tlačidlo späť, domov...)
    DisposableEffect(station.url) {
        onDispose {
            stopListeningAndSave()
            exoPlayer.release()
        }
    }

    val backgroundColor = if (isDarkMode) FigmaDarkBg else FigmaLightBg
    val primaryColor = if (isDarkMode) FigmaDarkCyan else FigmaDarkBlue
    val textColor = if (isDarkMode) Color.White else Color.Black
    val bottomIconColor = if (isDarkMode) Color.White else Heart_Light
    val subTextColor = if (isDarkMode) Color.LightGray else Color.Gray

    val isLive = FavoritesManager.isLiveBroadcast(trackTitle, station.name)

    // 🛑 DIALÓG NA ODHLÁSENIE
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(text = context.getString(R.string.logout_title)) },
            text = { Text(text = context.getString(R.string.logout_confirmation, userDisplayName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        googleAuthUiClient.signOut()
                        Toast.makeText(context, "Boli ste odhlásený", Toast.LENGTH_LONG).show()
                        onLogOutSuccess()
                    }
                ) {
                    Text(context.getString(R.string.logout_button), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(context.getString(R.string.cancel), color = Color.Black)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* --- TOP HEADER --- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .border(BorderStroke(2.dp, primaryColor), RoundedCornerShape(30.dp))
                        .clickable { onToggleDarkMode() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDarkMode) stringResource(R.string.light_mode) else stringResource(R.string.dark_mode),
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))



                /* 👤 DYNAMICKÝ PROFILOVÝ AVATAR */
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(ProfileBlue)
                        .clickable { showSignOutDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userPhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profilová fotka",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = firstLetter,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /* --- FAVICON ALEBO DEFAULT OBRÁZOK --- */
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (station.icon.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(station.icon)
                            .crossfade(true)
                            .build(),
                        contentDescription = station.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(188.dp)
                            .clip(RoundedCornerShape(32.dp)),
                        error = painterResource(id = R.drawable.default_radio),
                        placeholder = painterResource(id = R.drawable.default_radio)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_radio),
                        contentDescription = "Logo Radia",
                        modifier = Modifier
                            .size(188.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            /* --- NÁZOV RÁDIA A SKLADBY --- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = station.name.ifBlank { "Rádio" },
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .border(BorderStroke(2.dp, if (isLive) Color.Gray else textColor), RoundedCornerShape(4.dp))
                            .alpha(if (isLive) 0.5f else 1.0f)
                            .clickable {
                                if (isLive) {
                                    Toast.makeText(context, context.getString(R.string.cannot_save_live), Toast.LENGTH_SHORT).show()
                                } else {
                                    scope.launch {
                                        val result = FavoritesManager.addSongToFavorites(trackTitle, station.name)
                                        if (result.isSuccess) {
                                            Toast.makeText(context, context.getString(R.string.song_saved), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Chyba: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pridať",
                            tint = if (isLive) Color.Gray else textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = trackTitle,
                    color = subTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            /* --- PLAY / PAUSE TLAČIDLO --- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(35.dp))
                    .border(BorderStroke(3.dp, primaryColor), RoundedCornerShape(35.dp))
                    .background(if (isDarkMode) Color.Black else Color.White)
                    .clickable {
                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pozastaviť" else "Prehrať",
                    tint = textColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            /* --- SPODNÁ NAVIGÁCIA --- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Obľúbené",
                    tint = bottomIconColor,
                    modifier = Modifier.size(40.dp).clickable { onNavigateToFavorites() }
                )
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Domov",
                    tint = bottomIconColor,
                    modifier = Modifier.size(40.dp).clickable { onNavigateToHome() }
                )
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Štatistiky",
                    tint = bottomIconColor,
                    modifier = Modifier.size(40.dp).clickable { onNavigateToStatistics() }
                )
            }
        }
    }
}