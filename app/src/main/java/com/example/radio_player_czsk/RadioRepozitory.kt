package com.example.radio_player_czsk

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.Normalizer

object RadioRepository {

    private const val TAG = "RadioDebug"

    // Singleton cache — načíta sa raz pri štarte z Firebase
    var cachedStations: List<RadioStation> = emptyList()
        private set

    // ⚡ Bleskové načítanie spoločného zoznamu z Firebase Firestore
    suspend fun fetchSlovakAndCzechRadios(): List<RadioStation> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "⚡ Načítavam rádiá z Firebase Firestore...")

            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("radios").get().await()

            // Firebase automaticky namapuje dokumenty na objekty RadioStation
            val finalRadioList = snapshot.toObjects(RadioStation::class.java)

            cachedStations = finalRadioList
            Log.d(TAG, "✅ Celkový zoznam z Firebase: ${finalRadioList.size} rádií")
            return@withContext finalRadioList

        } catch (e: Exception) {
            Log.e(TAG, "❌ Chyba pri čítaní z Firebase: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    // Normalizácia: odstráni diakritiku, medzery, prevedie na lowercase
    fun normalize(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("\\s+"), "")
            .lowercase()
    }

    // Vyhľadávanie nad nacachovanými dátami z Firebase
    fun searchStations(query: String): List<RadioStation> {
        if (query.isBlank()) return emptyList()
        val normalizedQuery = normalize(query)
        return cachedStations.filter { station ->
            normalize(station.name).contains(normalizedQuery)
        }
    }
}