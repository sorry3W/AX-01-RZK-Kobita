package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MidiMappingEntity
import com.example.midi.MidiEngine
import com.example.ui.AX100ViewModel
import com.example.ui.theme.AmberLED
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MidiMatrixScreen(
    viewModel: AX100ViewModel
) {
    val midiEngine = viewModel.midiEngine
    val selectedDaw by midiEngine.selectedDawProfile.collectAsState()
    val mappings by viewModel.midiMappings.collectAsState()
    val logs by midiEngine.midiLogs.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // DAW Profile Selector Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = MatrixGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DAW INTEGRATION PROFILE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen
                        )
                    }

                    Button(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = CyberDarkBg, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXPORT DAW SCRIPT", fontSize = 10.sp, color = CyberDarkBg, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MidiEngine.DawProfile.values().take(3).forEach { profile ->
                        val isSelected = selectedDaw == profile
                        FilterChip(
                            selected = isSelected,
                            onClick = { midiEngine.setDawProfile(profile) },
                            label = { Text(profile.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MatrixGreen,
                                selectedLabelColor = CyberDarkBg,
                                containerColor = CyberDarkBg,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MidiEngine.DawProfile.values().drop(3).forEach { profile ->
                        val isSelected = selectedDaw == profile
                        FilterChip(
                            selected = isSelected,
                            onClick = { midiEngine.setDawProfile(profile) },
                            label = { Text(profile.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MatrixGreen,
                                selectedLabelColor = CyberDarkBg,
                                containerColor = CyberDarkBg,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = selectedDaw.description,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // MIDI Mapping Table Header
        item {
            Text(
                text = "AUTOMATED MIDI CC CONTROL MATRIX",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                letterSpacing = 1.sp
            )
        }

        // MIDI Mappings List
        items(mappings) { map ->
            MidiMappingCard(
                mapping = map,
                onCcChange = { newCcVal ->
                    midiEngine.sendMidiCc(map.ccNumber, newCcVal, map.parameterName)
                }
            )
        }

        // Live MIDI Monitor Feed Log
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(CyberSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = AmberLED)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE MIDI SIGNAL STREAM FEED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberLED
                        )
                    }

                    IconButton(
                        onClick = { midiEngine.clearLogs() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Logs", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CyberDarkBg, RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    items(logs) { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "[${log.timestamp}] ${log.type} ${log.parameter}",
                                fontSize = 10.sp,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "HEX: ${log.hexPayload}",
                                fontSize = 10.sp,
                                color = MatrixGreen,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    // Export DAW Script Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export DAW Script / MIDI Map", color = MatrixGreen) },
            text = {
                Column {
                    Text(
                        text = "Script generated for ${selectedDaw.displayName}:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberDarkBg, RoundedCornerShape(6.dp))
                            .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = """
                                {
                                  "daw": "${selectedDaw.displayName}",
                                  "device": "AX-100 Z-File Core",
                                  "mappings": [
                                    ${mappings.take(4).joinToString(",\n    ") { "{ \"cc\": ${it.ccNumber}, \"param\": \"${it.parameterName}\", \"src\": \"${it.sourceAttribute}\" }" }}
                                  ]
                                }
                            """.trimIndent(),
                            fontSize = 10.sp,
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen)
                ) {
                    Text("DOWNLOAD SCRIPT", color = CyberDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("CLOSE", color = TextMuted)
                }
            },
            containerColor = CyberSurface
        )
    }
}

@Composable
fun MidiMappingCard(
    mapping: MidiMappingEntity,
    onCcChange: (Int) -> Unit
) {
    var ccVal by remember { mutableStateOf(mapping.currentCcValue.toFloat()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CC#${mapping.ccNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mapping.parameterName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "SRC: ${mapping.sourceAttribute}",
                    fontSize = 10.sp,
                    color = AmberLED,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VALUE: ${ccVal.toInt()}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(70.dp)
                )

                Slider(
                    value = ccVal,
                    onValueChange = {
                        ccVal = it
                        onCcChange(it.toInt())
                    },
                    valueRange = 0f..127f,
                    colors = SliderDefaults.colors(
                        thumbColor = MatrixGreen,
                        activeTrackColor = MatrixGreen,
                        inactiveTrackColor = CyberBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
