package com.example.camera.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity

class CrashDisplayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashInfo = intent.getStringExtra(EXTRA_CRASH_INFO) ?: "No error details available"

        setContent {
            CrashScreen(
                crashInfo = crashInfo,
                onRestart = {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                    finish()
                }
            )
        }
    }

    companion object {
        const val EXTRA_CRASH_INFO = "extra_crash_info"
    }
}

@Composable
private fun CrashScreen(crashInfo: String, onRestart: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D0F))
            .padding(20.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "Camera Recovered",
            color = Color(0xFFF6E8DC),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "An unexpected exception was caught safely by the camera recovery engine.",
            color = Color(0xFFA5A9AF),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181A1D)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = crashInfo,
                    color = Color(0xFFE2E4E8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Crash Log", crashInfo))
                    Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Copy Log")
            }

            Button(
                onClick = onRestart,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6E8DC), contentColor = Color(0xFF141618))
            ) {
                Text("Restart App", fontWeight = FontWeight.Bold)
            }
        }
    }
}
