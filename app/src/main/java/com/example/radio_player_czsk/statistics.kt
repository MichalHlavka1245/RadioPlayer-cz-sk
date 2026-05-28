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
import androidx.compose.material.icons.filled.Favorite
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

@Composable
fun StatisticsScreen(
    googleAuthUiClient: GoogleAuthUiClient,
    isDarkMode: Boolean,
    onNavigateToHome: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onLogOutSuccess: () -> Unit
) {

    var showSignOutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Načítanie dát používateľa
    val currentUser = remember { googleAuthUiClient.getSignedInUser() }
    val userPhotoUrl = currentUser?.photoUrl
    val userDisplayName = currentUser?.displayName ?: context.getString(R.string.user)
    val firstLetter = (currentUser?.displayName ?: currentUser?.email ?: "R").take(1).uppercase()

    // 🚀 ODBERANIE LIVE ŠTATISTÍK Z FIREBASE
    val statistics by StatisticsManager.observeStatistics().collectAsState(initial = emptyMap())

    val backgroundColor = if (isDarkMode) FigmaDarkBg else FigmaLightBg
    val primaryColor = if (isDarkMode) FigmaDarkCyan else FigmaDarkBlue
    val textColor = if (isDarkMode) Color.White else Color.Black
    val bottomIconColor = if (isDarkMode) Color.White else Heart_Light

    // 🛑 Dialóg na odhlásenie
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

                //LanguageSelector(onLanguageChanged = onLanguageChanged)

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

            Spacer(modifier = Modifier.height(40.dp))

            /* --- NADPIS (Štatistiky) --- */
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
                    text = stringResource(R.string.statistics),
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            /* --- KARTA SÚHRNU ŠTATISTÍK (DYNAMICKÁ) --- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(40.dp))
                    .border(BorderStroke(3.dp, primaryColor), RoundedCornerShape(40.dp))
                    .background(if (isDarkMode) Color.Black else Color.White)
                    .padding(24.dp)
            ) {
                if (statistics.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.no_stats_yet),
                            color = if (isDarkMode) Color.Gray else Color.LightGray,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        items(statistics.toList()) { (stationName, totalSeconds) ->
                            Text(
                                text = "$stationName : ${StatisticsManager.formatListeningTime(totalSeconds)}",
                                color = textColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            /* --- SPODNÁ NAVIGAČNÁ LIŠTA --- */
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
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onNavigateToFavorites() }
                )

                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Domov",
                    tint = bottomIconColor,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onNavigateToHome() }
                )
            }
        }
    }
}