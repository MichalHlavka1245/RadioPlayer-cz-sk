package com.example.radio_player_czsk
import android.util.Log
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    // Overenie, či už je užívateľ prihlásený z minula
    fun getSignedInUser() = auth.currentUser

    // Spustí Google okno a prihlási užívateľa do Firebase databázy
    suspend fun signIn(activityContext: android.content.Context): Boolean {
        return try {
            Log.d("GOOGLE_AUTH", "1. Spúšťam prihlasovanie...")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = activityContext, request = request)
            val credential = result.credential

            Log.d("GOOGLE_AUTH", "2. Google vrátil typ: ${credential.type}")

            // ✨ UNIVERZÁLNE OVERENIE: Skontrolujeme nový aj starší typ balíčka
            val idToken = when {
                credential is GoogleIdTokenCredential -> {
                    Log.d("GOOGLE_AUTH", "3a. Rozpoznaný moderný GoogleIdTokenCredential")
                    credential.idToken
                }
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    Log.d("GOOGLE_AUTH", "3b. Rozpoznaný typ cez stringový identifikátor")
                    // Vytiahneme token priamo z dát structure
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    googleIdTokenCredential.idToken
                }
                else -> {
                    Log.e("GOOGLE_AUTH", "❌ Neznámy typ credentialu: ${credential.type}")
                    null
                }
            }

            if (idToken != null) {
                Log.d("GOOGLE_AUTH", "4. Posielam ID Token do Firebase...")
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                Log.d("GOOGLE_AUTH", "✅ ÚSPECH! Užívateľ úspešne prihlásený: ${authResult.user?.email}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("GOOGLE_AUTH", "❌ CHYBA v bloku catch: ", e)
            false
        }
    }
    // Odhlásenie
    fun signOut() {
        auth.signOut()
    }

}
