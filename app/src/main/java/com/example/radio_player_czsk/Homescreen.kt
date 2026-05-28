package com.example.radio_player_czsk

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalConfiguration

// 💡 Pomocná funkcia na nájdenie Activity v Jetpack Compose prostredí
fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun LanguageSelector(onLanguageChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    // 💡 Čítame aktuálny jazyk priamo z Compose konfigurácie
    val config = LocalConfiguration.current
    val currentLang = config.locales[0].language

    val currentFlag = when (currentLang) {
        "cs" -> "🇨🇿"
        "en" -> "🇬🇧"
        else -> "🇸🇰"
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        Text(
            text = currentFlag,
            fontSize = 28.sp,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("🇸🇰 Slovenčina", fontSize = 18.sp) }, onClick = { expanded = false; onLanguageChange("sk") })
            DropdownMenuItem(text = { Text("🇨🇿 Čeština", fontSize = 18.sp) }, onClick = { expanded = false; onLanguageChange("cs") })
            DropdownMenuItem(text = { Text("🇬🇧 English", fontSize = 18.sp) }, onClick = { expanded = false; onLanguageChange("en") })
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homescreen(
    googleAuthUiClient: GoogleAuthUiClient,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateToPlayer: (RadioStation) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onLogOutSuccess: () -> Unit
) {
    val localizedContext = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val currentUser = remember { googleAuthUiClient.getSignedInUser() }
    val userPhotoUrl = currentUser?.photoUrl
    val userDisplayName = currentUser?.displayName ?: localizedContext.getString(R.string.user)
    val firstLetter = (currentUser?.displayName ?: currentUser?.email ?: "R").take(1).uppercase()

    val backgroundColor = if (isDarkMode) FigmaDarkBg else FigmaLightBg
    val primaryColor = if (isDarkMode) FigmaDarkCyan else FigmaDarkBlue
    val textColor = if (isDarkMode) Color.White else Color.Black
    val iconBorderColor = if (isDarkMode) FigmaDarkCyan else FigmaDarkBlue
    val iconTintColor = if (isDarkMode) Heart_Dark else Heart_Light

    LaunchedEffect(searchText) {
        searchResults = RadioRepository.searchStations(searchText)
    }

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
                        onLogOutSuccess()
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

                LanguageSelector(onLanguageChange = onLanguageChange)

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
                            contentDescription = stringResource(R.string.profile_photo),
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

            Spacer(modifier = Modifier.height(32.dp))

            /* --- IKONY SRDCE A GRAF --- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(65.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(3.dp, iconBorderColor), CircleShape)
                        .background(if (isDarkMode) Color.Black else Color.White)
                        .clickable { onNavigateToFavorites() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.favorites),
                        tint = iconTintColor,
                        modifier = Modifier.size(35.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(65.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(3.dp, iconBorderColor), CircleShape)
                        .background(if (isDarkMode) Color.Black else Color.White)
                        .clickable { onNavigateToStatistics() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = stringResource(R.string.statistics),
                        tint = iconTintColor,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            /* --- SEARCH BAR --- */
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                ),
                placeholder = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.enter_radio_name),
                            color = if (isDarkMode) Color.Gray else Heart_Light,
                            fontSize = 20.sp
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = primaryColor,
                    cursorColor = primaryColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            /* --- VÝSLEDKY VYHĽADÁVANIA --- */
            if (searchText.isNotBlank()) {
                if (searchResults.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_radio_found),
                        color = if (isDarkMode) Color.Gray else Heart_Light,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults.take(20)) { station ->
                            RadioSearchResultItem(
                                station = station,
                                isDarkMode = isDarkMode,
                                primaryColor = primaryColor,
                                textColor = textColor,
                                onClick = { onNavigateToPlayer(station) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadioSearchResultItem(
    station: RadioStation,
    isDarkMode: Boolean,
    primaryColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(2.dp, primaryColor), RoundedCornerShape(16.dp))
            .background(if (isDarkMode) Color.Black else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (station.icon.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(station.icon)
                        .crossfade(true)
                        .build(),
                    contentDescription = station.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    error = painterResource(id = R.drawable.default_radio),
                    placeholder = painterResource(id = R.drawable.default_radio)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_radio),
                    contentDescription = station.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = station.name,
                color = textColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (station.tags.isNotBlank()) {
                Text(
                    text = station.tags.split(",").take(2).joinToString(" · "),
                    color = if (isDarkMode) Color.Gray else Heart_Light,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}