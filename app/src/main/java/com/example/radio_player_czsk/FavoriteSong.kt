package com.example.radio_player_czsk

import java.util.UUID

data class FavoriteSong(
    val id: String = UUID.randomUUID().toString(), // Unikátne ID záznamu
    val userId: String = "",                       // ID prihláseného užívateľa z Firebase Auth
    val songTitle: String = "",                    // Názov pesničky (Interpret - Skladba)
    val radioName: String = "",                     // Z ktorej stanice bola pesnička uložená
    val timestamp: Long = System.currentTimeMillis() // Kedy si ju užívateľ uložil
)