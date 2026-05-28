package com.example.radio_player_czsk

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val TAG = "RADIO_MAIN_ACTIVITY"

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(context = applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Spustenie MainActivity")

        setContent {
            // 🔥 ZMENA: Chytíme si skutočný Activity kontext hneď na začiatku, kým ho neprepíšeme nižšie
            val realActivityContext = LocalContext.current

            val context = LocalContext.current

            // 💾 Načítanie uloženého jazyka z SharedPreferences pri štarte
            val sharedPrefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
            var currentLanguageTag by rememberSaveable {
                mutableStateOf(sharedPrefs.getString("locale_tag", "sk") ?: "sk")
            }

            // 🌍 Dynamické vytvorenie lokalizovaného kontextu naživo v Compose
            val localizedContext = remember(currentLanguageTag) {
                val locale = Locale.forLanguageTag(currentLanguageTag)
                Locale.setDefault(locale)
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                context.createConfigurationContext(config)
            }

            LaunchedEffect(Unit) {
                RadioRepository.fetchSlovakAndCzechRadios()
            }

            // Použijeme rememberSaveable, aby dark mode prežil otočenie displeja
            var isDarkMode by rememberSaveable { mutableStateOf(false) }

            // 🔥 KĽÚČOVÝ FIX: Vstrekneme lokalizovaný kontext do celého Compose stromu.
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                val navController = rememberNavController()
                val startDestination = if (googleAuthUiClient.getSignedInUser() != null) "home" else "login"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // 🔐 OBRAZOVKA PRIHLÁSENIA
                    composable("login") {
                        LoginScreen(
                            // 🔥 ZMENA: Posielame čistý realActivityContext na spustenie Google okna
                            activityContext = realActivityContext,
                            googleAuthUiClient = googleAuthUiClient,
                            onSignInSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 🏠 DOMÁCA OBRAZOVKA
                    composable("home") {
                        Homescreen(
                            googleAuthUiClient = googleAuthUiClient,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            onLanguageChange = { newLocale ->
                                sharedPrefs.edit().putString("locale_tag", newLocale).apply()
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                                currentLanguageTag = newLocale
                            },
                            onNavigateToPlayer = { station ->
                                navController.currentBackStackEntry?.savedStateHandle?.set("station_name", station.name)
                                navController.currentBackStackEntry?.savedStateHandle?.set("station_url", station.url)
                                navController.currentBackStackEntry?.savedStateHandle?.set("station_icon", station.icon)
                                navController.navigate("player")
                            },
                            onNavigateToFavorites = { navController.navigate("favorites") },
                            onNavigateToStatistics = { navController.navigate("statistics") },
                            onLogOutSuccess = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 📻 PREHRÁVAČ RÁDIA
                    composable("player") {
                        val stationName = navController.previousBackStackEntry?.savedStateHandle?.get<String>("station_name") ?: ""
                        val stationUrl = navController.previousBackStackEntry?.savedStateHandle?.get<String>("station_url") ?: ""
                        val stationIcon = navController.previousBackStackEntry?.savedStateHandle?.get<String>("station_icon") ?: ""
                        val station = RadioStation(name = stationName, url = stationUrl, icon = stationIcon)

                        PlayerScreen(
                            station = station,
                            googleAuthUiClient = googleAuthUiClient,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            onNavigateToHome = { navController.navigate("home") },
                            onNavigateToFavorites = { navController.navigate("favorites") },
                            onNavigateToStatistics = { navController.navigate("statistics") },
                            onLogOutSuccess = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    // ❤️ OBĽÚBENÉ SKLADBY
                    composable("favorites") {
                        FavoritesScreen(
                            googleAuthUiClient = googleAuthUiClient,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            onNavigateToHome = { navController.navigate("home") },
                            onNavigateToStatistics = { navController.navigate("statistics") },
                            onLogOutSuccess = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 📊 ŠTATISTIKY
                    composable("statistics") {
                        StatisticsScreen(
                            googleAuthUiClient = googleAuthUiClient,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            onNavigateToHome = { navController.navigate("home") },
                            onNavigateToFavorites = { navController.navigate("favorites") },
                            onLogOutSuccess = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}