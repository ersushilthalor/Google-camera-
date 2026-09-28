package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    proSettings: ProSettings,
    onUpdateSettings: (ProSettings) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E2024),
            modifier = Modifier.fillMaxWidth().testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Camera Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Horizon Level toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Framing Level", color = Color.White, fontSize = 15.sp)
                        Text(text = "Shows 0° horizon line", color = Color(0xFF9E9FA3), fontSize = 12.sp)
                    }
                    Switch(
                        checked = proSettings.levelEnabled,
                        onCheckedChange = { onUpdateSettings(proSettings.copy(levelEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1E2024),
                            checkedTrackColor = PeachActivePill
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rule of Thirds Grid toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Grid Lines", color = Color.White, fontSize = 15.sp)
                        Text(text = "3x3 alignment grid", color = Color(0xFF9E9FA3), fontSize = 12.sp)
                    }
                    Switch(
                        checked = proSettings.gridEnabled,
                        onCheckedChange = { onUpdateSettings(proSettings.copy(gridEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1E2024),
                            checkedTrackColor = PeachActivePill
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4K Ultra HD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "High Resolution (4K / Raw)", color = Color.White, fontSize = 15.sp)
                        Text(text = "Maximum sensor output", color = Color(0xFF9E9FA3), fontSize = 12.sp)
                    }
                    Switch(
                        checked = proSettings.resolution4k,
                        onCheckedChange = { onUpdateSettings(proSettings.copy(resolution4k = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1E2024),
                            checkedTrackColor = PeachActivePill
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Done", color = PeachActivePill, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
