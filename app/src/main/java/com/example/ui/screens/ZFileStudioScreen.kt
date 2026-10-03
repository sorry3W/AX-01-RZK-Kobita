package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
fun ZFileStudioScreen(
    viewModel: AX100ViewModel
) {
    val isProcessing by viewModel.isProcessingFile.collectAsState()
    val lastResult by viewModel.lastSonifiedResult.collectAsState()

    var customText by remember { mutableStateOf("") }
    var customFileName by remember { mutableStateOf("Project_Specs.pdf") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Hardware Device Art Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_ax100_device_1785143433395),
                        contentDescription = "AX-100 Hardware Unit",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        CyberDarkBg.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "AUTOMATIC FILE SONIFICATION ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Transform PDF, Code, Images & Data into Synth Patches",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Quick File Samples Selection Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "SELECT FILE FOR SONIFICATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberLED,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FileSampleCard(
                            title = "PDF Spec",
                            subtitle = "AX-100_Manual.pdf",
                            icon = Icons.Default.Description,
                            accentColor = NeonCyan,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.sonifySampleFile(
                                "AX-100_Manual.pdf",
                                "PDF",
                                "AX-100 AI Z File Device specification manual. Includes real-time sound synthesis, automated MIDI mapping, and semantic vector database memory."
                            )
                        }

                        FileSampleCard(
                            title = "Source Code",
                            subtitle = "SynthKernel.kt",
                            icon = Icons.Default.Code,
                            accentColor = MatrixGreen,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.sonifySampleFile(
                                "SynthKernel.kt",
                                "CODE",
                                "class AudioSynthEngine { val sampleRate = 44100; fun processBuffer() { ... } }"
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FileSampleCard(
                            title = "Data Matrix",
                            subtitle = "AudioData.json",
                            icon = Icons.Default.TableChart,
                            accentColor = AmberLED,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.sonifySampleFile(
                                "AudioData.json",
                                "JSON",
                                "{ \"biquadFilter\": { \"type\": \"LPF\", \"freq\": 3200, \"q\": 0.707 }, \"oscCount\": 4 }"
                            )
                        }

                        FileSampleCard(
                            title = "Archive ZIP",
                            subtitle = "Patches.zip",
                            icon = Icons.Default.FolderZip,
                            accentColor = ElectricMagenta,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.sonifySampleFile(
                                "Patches.zip",
                                "ZIP",
                                "Compressed archive containing 128 wavetables and high-entropy binary DSP algorithms."
                            )
                        }
                    }
                }
            }
        }

        // Custom File Content / Prompt Input
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "SONIFY CUSTOM TEXT / FILE DATA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customFileName,
                    onValueChange = { customFileName = it },
                    label = { Text("File Name", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    label = { Text("Paste document content, prompt or audio metadata...", fontSize = 11.sp) },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val textToProcess = if (customText.isBlank()) "Default custom sonification file data" else customText
                        viewModel.sonifySampleFile(
                            if (customFileName.isBlank()) "CustomData.txt" else customFileName,
                            "TXT",
                            textToProcess
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = CyberDarkBg,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Sonifying File...", color = CyberDarkBg, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CyberDarkBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ANALYZE & GENERATE Z-PATCH", color = CyberDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sonification Results Panel
        if (lastResult != null) {
            val z = lastResult!!.zFileEntity
            val p = lastResult!!.patchPreset

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant, RoundedCornerShape(10.dp))
                        .border(1.dp, MatrixGreen, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MatrixGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Z-FILE ANALYSIS COMPLETE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreen
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(MatrixGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = z.fileType, fontSize = 10.sp, color = MatrixGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = z.fileName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = z.aiSummary,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Extracted Sound Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricChip("ENTROPY", String.format("%.2f", z.entropyScore), NeonCyan)
                        MetricChip("KEY/SCALE", z.detectedMusicalKey, AmberLED)
                        MetricChip("TEMPO", "${z.suggestedTempoBpm} BPM", MatrixGreen)
                        MetricChip("OSCILLATOR", p.oscillatorType, ElectricMagenta)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.applyPresetToSynth(p)
                            viewModel.setSelectedTab(1) // Jump to Synthesizer tab
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberDarkBg)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LOAD Z-PATCH INTO SYNTHESIZER & PLAY", color = CyberDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FileSampleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
