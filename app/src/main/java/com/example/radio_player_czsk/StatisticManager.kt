package com.example.radio_player_czsk

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object StatisticsManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    
    fun saveListeningTime(stationName: String, seconds: Long) {
        if (seconds <= 0) return

        val userDocRef = db.collection("statistics").document(userId)

    
        userDocRef.set(
            mapOf(stationName to FieldValue.increment(seconds)),
            com.google.firebase.firestore.SetOptions.merge()
        ).addOnSuccessListener {
            Log.d("STATISTIKY", "Úspešne uložené: $stationName +$seconds sekúnd")
        }.addOnFailureListener { e ->
            Log.e("STATISTIKY", "Chyba pri ukladaní štatistík", e)
        }
    }

    
    fun observeStatistics(): Flow<Map<String, Long>> = callbackFlow {
        val docRef = db.collection("statistics").document(userId)

        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val statsMap = mutableMapOf<String, Long>()
            if (snapshot != null && snapshot.exists()) {
                snapshot.data?.forEach { (key, value) ->
                    if (value is Long) {
                        statsMap[key] = value
                    }
                }
            }
            trySend(statsMap)
        }
        awaitClose { subscription.remove() }
    }

   
    fun formatListeningTime(totalSeconds: Long): String {
        return when {
            totalSeconds < 60 -> {
                "$totalSeconds sekúnd"
            }
            totalSeconds < 3600 -> {
                val minutes = totalSeconds / 60
                "$minutes minút"
            }
            else -> {
                val hours = totalSeconds / 3600
                val remainingMinutes = (totalSeconds % 3600) / 60
                if (remainingMinutes > 0) {
                    "$hours hodín $remainingMinutes minút"
                } else {
                    "$hours hodín"
                }
            }
        }
    }
}
