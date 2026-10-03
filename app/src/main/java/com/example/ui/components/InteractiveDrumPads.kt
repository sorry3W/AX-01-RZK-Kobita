package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberLED
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InteractiveDrumPads(
    onDrumTrigger: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val drumNames = listOf(
        "01 KICK", "02 SNARE", "03 CHAT", "04 OHAT",
        "05 LOW TOM", "06 MID TOM", "07 CRASH", "08 RIDE"
    )

    val padColors = listOf(
        ElectricMagenta, MatrixGreen, NeonCyan, AmberLED,
        ElectricMagenta, MatrixGreen, NeonCyan, AmberLED
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberDarkBg, RoundedCornerShape(8.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "AX-100 DRUM PADS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0..3) {
                val isPressed = remember { mutableStateOf(false) }
                val baseColor = padColors[i]

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPressed.value) baseColor else CyberSurfaceVariant)
                        .border(1.dp, if (isPressed.value) baseColor else CyberBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            isPressed.value = true
                            onDrumTrigger(i)
                            scope.launch {
                                delay(120)
                                isPressed.value = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = drumNames[i],
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPressed.value) CyberDarkBg else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 4..7) {
                val isPressed = remember { mutableStateOf(false) }
                val baseColor = padColors[i]

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPressed.value) baseColor else CyberSurfaceVariant)
                        .border(1.dp, if (isPressed.value) baseColor else CyberBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            isPressed.value = true
                            onDrumTrigger(i)
                            scope.launch {
                                delay(120)
                                isPressed.value = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = drumNames[i],
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPressed.value) CyberDarkBg else TextPrimary
                    )
                }
            }
        }
    }
}
