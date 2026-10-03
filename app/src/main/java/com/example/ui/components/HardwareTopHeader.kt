package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberLED
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HardwareTopHeader(
    statusMessage: String,
    bpm: Int,
    isPlaying: Boolean,
    isRecording: Boolean,
    isLooping: Boolean,
    isAudioActive: Boolean,
    onBpmChange: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onToggleRecord: () -> Unit,
    onToggleLoop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberDarkBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Device Title & OLED Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo & Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isAudioActive) MatrixGreen else CrimsonAlert)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AX-100 AI Z-FILE CORE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = NeonCyan
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(CyberSurface, RoundedCornerShape(4.dp))
                        .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "v1.0 DAW",
                        fontSize = 10.sp,
                        color = AmberLED,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Live BPM Display
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BPM:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "$bpm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // OLED Status Panel & Transport Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface, RoundedCornerShape(8.dp))
                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Text
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Status",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusMessage,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Transport Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isPlaying) MatrixGreen.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = if (isPlaying) MatrixGreen else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Record Button
                IconButton(
                    onClick = onToggleRecord,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isRecording) CrimsonAlert.copy(alpha = 0.25f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Record",
                        tint = if (isRecording) CrimsonAlert else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Loop Button
                IconButton(
                    onClick = onToggleLoop,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isLooping) ElectricMagenta.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Loop",
                        tint = if (isLooping) ElectricMagenta else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
