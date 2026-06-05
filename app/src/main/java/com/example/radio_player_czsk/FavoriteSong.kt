package com.example.radio_player_czsk

import java.util.UUID

data class FavoriteSong(
    val id: String = UUID.randomUUID().toString(), 
    val userId: String = "",                      
    val songTitle: String = "",                    
    val radioName: String = "",                     
    val timestamp: Long = System.currentTimeMillis() 
)
