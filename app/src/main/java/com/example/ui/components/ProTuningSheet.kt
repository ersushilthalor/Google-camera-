package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CinemaLut
import com.example.model.ProSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProTuningSheet(
    proSettings: ProSettings,
    isCinemaMode: Boolean,
    onUpdateSettings: (ProSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF181A1D),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Pro Controls",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Exposure Value (EV) Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Exposure (EV)", fontSize = 14.sp, color = Color(0xFFB0B3B8))
                Text(
                    text = "${if (proSettings.exposureCompensation > 0) "+" else ""}${proSettings.exposureCompensation}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PeachActivePill
                )
            }

            Slider(
                value = proSettings.exposureCompensation.toFloat(),
                onValueChange = { onUpdateSettings(proSettings.copy(exposureCompensation = it.toInt())) },
                valueRange = -2f..2f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = PeachActivePill,
                    activeTrackColor = PeachActivePill,
                    inactiveTrackColor = Color(0xFF333538)
                ),
                modifier = Modifier.testTag("slider_ev")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cinema LUTs / Color Profiles
            Text(
                text = if (isCinemaMode) "Cinema Color Grade (LUT)" else "Color Profile",
                fontSize = 14.sp,
                color = Color(0xFFB0B3B8)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CinemaLut.entries) { lut ->
                    val isSelected = proSettings.cinemaLut == lut
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PeachActivePill else Color(0xFF26282D))
                            .clickable { onUpdateSettings(proSettings.copy(cinemaLut = lut)) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = lut.displayName,
                            color = if (isSelected) Color(0xFF121315) else Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // White Balance Presets
            Text(text = "White Balance", fontSize = 14.sp, color = Color(0xFFB0B3B8))
            Spacer(modifier = Modifier.height(8.dp))

            val wbList = listOf("Auto", "Daylight", "Cloudy", "Tungsten", "Fluorescent")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(wbList) { wb ->
                    val isSelected = proSettings.whiteBalance == wb
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PeachActivePill else Color(0xFF26282D))
                            .clickable { onUpdateSettings(proSettings.copy(whiteBalance = wb)) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = wb,
                            color = if (isSelected) Color(0xFF121315) else Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
