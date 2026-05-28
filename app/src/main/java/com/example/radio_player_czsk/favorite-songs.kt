package com.example.radio_player_czsk

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    googleAuthUiClient: GoogleAuthUiClient, // 🚀 PRIDANÝ PARAMETER PRE FIREBASE/GOOGLE ÚČET
    onNavigateToHome: () -> Unit,
    isDarkMode: Boolean,                // 👈 Pridaj sem
    onToggleDarkMode: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onLogOutSuccess: () -> Unit // 🚀 CALLBACK PRE ODHLÁSENIE
) {

    val localizedContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showSignOutDialog by remember { mutableStateOf(false) } // Stav pre odhlasovací dialóg

    // Získame dáta prihláseného usera (identicky ako v Homescreen a PlayerScreen)
    val currentUser = remember { googleAuthUiClient.getSignedInUser() }
    val userPhotoUrl = currentUser?.photoUrl
    val userDisplayName = currentUser?.displayName ?: localizedContext.getString(R.string.user)
    val firstLetter = (currentUser?.displayName ?: currentUser?.email ?: "R").take(1).uppercase()

    // ✨ ODBERANIE LIVE DÁT Z FIREBASE MANAŽÉRA
    val favoriteSongs by FavoritesManager.observeFavoriteSongs().collectAsState(initial = emptyList())

    val backgroundColor = if (isDarkMode) FigmaDarkBg else FigmaLightBg
    val primaryColor = if (isDarkMode) FigmaDarkCyan else FigmaDarkBlue
    val textColor = if (isDarkMode) Color.White else Color.Black
    val bottomIconColor = if (isDarkMode) Color.White else Heart_Light

    // 🛑 DIALÓG NA ODHLÁSENIE
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(text = localizedContext.getString(R.string.logout_title)) },
            text = { Text(text = localizedContext.getString(R.string.logout_confirmation, userDisplayName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        googleAuthUiClient.signOut()
                        Toast.makeText(context, "Boli ste odhlásený", Toast.LENGTH_LONG).show()
                        onLogOutSuccess() // Skočí späť na Login screen
                    }
                ) {
                    Text(localizedContext.getString(R.string.logout_button), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(localizedContext.getString(R.string.cancel), color = Color.Black)
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

            /* --- HORIZONTÁLNA LIŠTA (Top Header) --- */
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
                        .clickable { onToggleDarkMode()  }
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
                        .clickable { showSignOutDialog = true }, // Po kliknutí otvorí ponuku na odhlásenie
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

            Spacer(modifier = Modifier.height(40.dp))

            /* --- NADPIS (Obľúbené skladby) --- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .border(BorderStroke(3.dp, primaryColor), RoundedCornerShape(30.dp))
                    .background(if (isDarkMode) Color.Black else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.favorites),
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            /* --- KARTA S PESNIČKAMI (ZMENENÁ NA DYNAMICKÝ LAZYCOLUMN) --- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(40.dp))
                    .border(BorderStroke(3.dp, primaryColor), RoundedCornerShape(40.dp))
                    .background(if (isDarkMode) Color.Black else Color.White)
                    .padding(24.dp)
            ) {
                if (favoriteSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Žiadne uložené skladby",
                            color = if (isDarkMode) Color.Gray else Color.LightGray,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(favoriteSongs, key = { it.id }) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Kliknutím na skladbu ju vymažeme z databázy
                                        scope.launch {
                                            FavoritesManager.removeSongFromFavorites(song.id)
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = song.songTitle,
                                    color = textColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            /* --- SPODNÁ NAVIGAČNÁ LIŠTA (Domov a Graf) --- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Domov",
                    tint = bottomIconColor,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onNavigateToHome() }
                )

                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Štatistiky",
                    tint = if (isDarkMode) Color.White else bottomIconColor,
                    modifier = Modifier.size(40.dp)
                        .clickable { onNavigateToStatistics() }
                )
            }
        }
    }
}