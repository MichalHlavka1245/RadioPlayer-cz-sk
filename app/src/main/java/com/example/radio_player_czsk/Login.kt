package com.example.radio_player_czsk

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    activityContext: Context,
    googleAuthUiClient: GoogleAuthUiClient,
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            // 🔥 PREKLAD: Názov aplikácie zo strings.xml
            text = stringResource(id = R.string.app_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            // 🔥 PREKLAD: Podnadpis zo strings.xml
            text = stringResource(id = R.string.login_subtitle),
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                scope.launch {
                    val success = googleAuthUiClient.signIn(activityContext = activityContext)
                    if (success) {
                        Toast.makeText(context, "Prihlásenie úspešné!", Toast.LENGTH_LONG).show()
                        onSignInSuccess()
                    } else {
                        // Tieto toasty môžeš neskôr tiež prehodiť do strings.xml, ak budeš chcieť
                        Toast.makeText(context, "Prihlásenie zlyhalo alebo bolo zrušené.", Toast.LENGTH_LONG).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 32.dp)
        ) {
            Text(
                // 🔥 PREKLAD: Text na tlačidle zo strings.xml
                text = stringResource(id = R.string.login_google),
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}