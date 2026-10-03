package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary

@Composable
fun InteractivePianoKeyboard(
    onNoteOn: (Int) -> Unit,
    onNoteOff: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // White Keys: C3 (60), D3 (62), E3 (64), F3 (65), G3 (67), A3 (69), B3 (71), C4 (72), D4 (74), E4 (76), F4 (77)
    val whiteNotes = listOf(
        Pair(60, "C3"), Pair(62, "D3"), Pair(64, "E3"),
        Pair(65, "F3"), Pair(67, "G3"), Pair(69, "A3"),
        Pair(71, "B3"), Pair(72, "C4"), Pair(74, "D4"),
        Pair(76, "E4"), Pair(77, "F4")
    )

    // Black Keys: (Note, Index position after white key)
    val blackNotes = listOf(
        Pair(61, 0), // C#3
        Pair(63, 1), // D#3
        Pair(66, 3), // F#3
        Pair(68, 4), // G#3
        Pair(70, 5), // A#3
        Pair(73, 7), // C#4
        Pair(75, 8)  // D#4
    )

    val activeNotes = remember { mutableStateListOf<Int>() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(CyberDarkBg, RoundedCornerShape(8.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        // White Keys Row
        Row(
            modifier = Modifier.matchParentSize()
        ) {
            whiteNotes.forEach { (midiNote, noteLabel) ->
                val isActive = activeNotes.contains(midiNote)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(if (isActive) NeonCyan else Color(0xFFE2E8F0))
                        .border(1.dp, CyberBorder, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isActive) {
                                activeNotes.remove(midiNote)
                                onNoteOff(midiNote)
                            } else {
                                activeNotes.add(midiNote)
                                onNoteOn(midiNote)
                            }
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = noteLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) CyberDarkBg else Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }

        // Overlay Black Keys
        // We render black keys positioned over white key boundaries
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 4.dp)
        ) {
            val keyWidth = (320.dp / whiteNotes.size) // approximate
            whiteNotes.forEachIndexed { index, _ ->
                val blackKey = blackNotes.find { it.second == index }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    if (blackKey != null) {
                        val isActive = activeNotes.contains(blackKey.first)
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(0.6f)
                                .offset(x = 11.dp)
                                .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                                .background(if (isActive) ElectricMagenta else Color(0xFF0F172A))
                                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (isActive) {
                                        activeNotes.remove(blackKey.first)
                                        onNoteOff(blackKey.first)
                                    } else {
                                        activeNotes.add(blackKey.first)
                                        onNoteOn(blackKey.first)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
