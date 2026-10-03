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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import com.example.data.db.PatchPresetEntity
import com.example.data.db.ZFileEntity
import com.example.ui.AX100ViewModel
import com.example.ui.theme.AmberLED
import com.example.ui.theme.CrimsonAlert
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
fun PresetsHistoryScreen(
    viewModel: AX100ViewModel
) {
    val presets by viewModel.savedPresets.collectAsState()
    val zFiles by viewModel.zFiles.collectAsState()

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Patch Presets, 1: Sonified Files History, 2: Device Memory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .padding(12.dp)
    ) {
        // Sub-Tab Row
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = CyberSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = NeonCyan
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("PATCH PRESETS (${presets.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Z-FILES (${zFiles.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("HARDWARE MEMORY", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedSubTab) {
            0 -> {
                if (presets.isEmpty()) {
                    EmptyStatePlaceholder("No saved patch presets found. Create and save patches in Synthesizer.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets) { preset ->
                            PresetCard(
                                preset = preset,
                                onLoad = {
                                    viewModel.applyPresetToSynth(preset)
                                    viewModel.setSelectedTab(1) // jump to synth
                                },
                                onDelete = { viewModel.deletePreset(preset.id) }
                            )
                        }
                    }
                }
            }
            1 -> {
                if (zFiles.isEmpty()) {
                    EmptyStatePlaceholder("No analyzed Z-Files yet. Import files in Z-File Studio.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(zFiles) { zFile ->
                            ZFileHistoryCard(
                                zFile = zFile,
                                onDelete = { viewModel.deleteZFile(zFile.id) }
                            )
                        }
                    }
                }
            }
            2 -> {
                HardwareMemoryPanel()
            }
        }
    }
}

@Composable
fun PresetCard(
    preset: PatchPresetEntity,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(ElectricMagenta.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = preset.category, fontSize = 9.sp, color = ElectricMagenta, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "OSC: ${preset.oscillatorType} | Cutoff: ${preset.filterCutoffHz.toInt()}Hz | Res: ${String.format("%.2f", preset.filterResonance)}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberDarkBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LOAD", fontSize = 10.sp, color = CyberDarkBg, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert)
                }
            }
        }
    }
}

@Composable
fun ZFileHistoryCard(
    zFile: ZFileEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = AmberLED)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = zFile.fileName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = zFile.aiSummary,
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Entropy: ${String.format("%.2f", zFile.entropyScore)}", fontSize = 10.sp, color = NeonCyan, fontFamily = FontFamily.Monospace)
                Text(text = "Key: ${zFile.detectedMusicalKey}", fontSize = 10.sp, color = AmberLED, fontFamily = FontFamily.Monospace)
                Text(text = "Tempo: ${zFile.suggestedTempoBpm} BPM", fontSize = 10.sp, color = MatrixGreen, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun HardwareMemoryPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberSurface, RoundedCornerShape(10.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = NeonCyan)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "AX-100 HARDWARE KNOWLEDGE MEMORY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        MemoryRow("Device Serial Number", "AX100-ZCORE-88219")
        MemoryRow("Firmware Version", "v1.0.4-DAW-HYBRID")
        MemoryRow("Local AI Model", "On-Device Neural File Indexer (100% Offline)")
        MemoryRow("Vector Database", "Local SQLite Vector Memory Active")
        MemoryRow("Audio Buffer Latency", "1024 Samples (~23ms @ 44.1kHz)")
        MemoryRow("USB-MIDI Engine", "Class-Compliant USB & Virtual DAW Bus")
    }
}

@Composable
fun MemoryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun EmptyStatePlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(CyberSurface, RoundedCornerShape(10.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(16.dp)
        )
    }
}
