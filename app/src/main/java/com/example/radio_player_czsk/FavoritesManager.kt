package com.example.radio_player_czsk

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose

object FavoritesManager {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Názov kolekcie vo Firestore
    private const val COLLECTION_NAME = "favorite_songs"

    // Pomocná funkcia na zistenie, či ide o živé vysielanie / neplatný názov skladby
    fun isLiveBroadcast(title: String, radioName: String): Boolean {
        val cleaned = title.lowercase().trim()
        val radioCleaned = radioName.lowercase().trim()

        return cleaned.isEmpty() ||
                cleaned == "živé vysielanie" ||
                cleaned == "zive vysielanie" ||
                cleaned == "live" ||
                cleaned == "live stream" ||
                cleaned == "studio" ||
                cleaned == radioCleaned ||
                cleaned.contains("reklama") ||
                cleaned.contains("správy")
    }

    // 💾 PRIDANIE PESNIČKY DO FIREBASE
    suspend fun addSongToFavorites(songTitle: String, radioName: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Užívateľ nie je prihlásený"))

        // Kontrola na živé vysielanie
        if (isLiveBroadcast(songTitle, radioName)) {
            return Result.failure(Exception("Živé vysielanie alebo prázdny názov nie je možné uložiť."))
        }

        return try {
            // 🔥 OVERENIE DUPLICITY: Skontrolujeme, či táto kombinácia už v databáze neexistuje
            val existingSongs = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("songTitle", songTitle)
                .whereEqualTo("radioName", radioName)
                .get()
                .await()

            // Ak výsledok nie je prázdny, pesnička už je uložená
            if (!existingSongs.isEmpty) {
                return Result.failure(Exception("Táto skladba už je vo vašich obľúbených."))
            }

            val favoriteSong = FavoriteSong(
                userId = userId,
                songTitle = songTitle,
                radioName = radioName
            )

            // Ak prešla kontrolou, uložíme ju do Firestore pod vlastným ID
            firestore.collection(COLLECTION_NAME)
                .document(favoriteSong.id)
                .set(favoriteSong)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ❌ VYMAZANIE PESNIČKY Z FIREBASE
    suspend fun removeSongFromFavorites(songId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(songId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 📻 REÁLNOTASOVÉ NAČÍTANIE (FLOW) – Načítá iba pesničky prihláseného užívateľa
    fun observeFavoriteSongs(): Flow<List<FavoriteSong>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Hľadáme dokumenty, kde userId odpovedá prihlásenému človeku, zoradené od najnovších
        val listener = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val songs = snapshot?.toObjects(FavoriteSong::class.java) ?: emptyList()
                trySend(songs)
            }

        // Automatické zrušenie listenera, keď užívateľ odíde z obrazovky
        awaitClose { listener.remove() }
    }
}