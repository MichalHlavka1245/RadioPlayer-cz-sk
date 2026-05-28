package com.example.radio_player_czsk

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RadioStation(
    val stationuuid: String = "",
    val name: String = "",
    @SerialName("url_resolved") val url: String = "",
    @SerialName("favicon") val icon: String = "",
    val tags: String = ""
)