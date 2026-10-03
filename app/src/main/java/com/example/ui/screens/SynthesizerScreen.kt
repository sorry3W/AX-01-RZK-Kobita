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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSynthEngine
import com.example.ui.AX100ViewModel
import com.example.ui.components.InteractiveDrumPads
import com.example.ui.components.InteractivePianoKeyboard
import com.example.ui.components.LiveWaveformVisualizer
import com.example.ui.theme.AmberLED
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
fun SynthesizerScreen(
    viewModel: AX100ViewModel
) {
    val synth = viewModel.synthEngine
    val vizBuffer by synth.visualizerBuffer.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("Z-Custom Patch") }

    // Live state triggers for UI slider redraws
    var cutoffVal by remember { mutableStateOf(synth.filterCutoffHz) }
    var resVal by remember { mutableStateOf(synth.filterResonance) }
    var lfoRateVal by remember { mutableStateOf(synth.lfoRateHz) }
    var lfoDepthVal by remember { mutableStateOf(synth.lfoDepth) }
    var reverbVal by remember { mutableStateOf(synth.reverbLevel) }
    var delayVal by remember { mutableStateOf(synth.delayLevel) }
    var distortionVal by remember { mutableStateOf(synth.distortionLevel) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Live Waveform / Spectrum Visualizer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REAL-TIME SPECTRUM & OSCILLOSCOPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Text(
                        text = "44.1kHz PCM",
                        fontSize = 10.sp,
                        color = MatrixGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LiveWaveformVisualizer(buffer = vizBuffer)
            }
        }

        // Oscillator Selection Rack
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "OSCILLATOR WAVEFORM ARCHITECTURE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberLED,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AudioSynthEngine.OscillatorType.values().take(4).forEach { oscType ->
                        val isSelected = synth.oscillatorType == oscType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateSynthOscillator(oscType)
                            },
                            label = { Text(oscType.name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = CyberDarkBg,
                                containerColor = CyberDarkBg,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AudioSynthEngine.OscillatorType.values().drop(4).forEach { oscType ->
                        val isSelected = synth.oscillatorType == oscType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateSynthOscillator(oscType)
                            },
                            label = { Text(oscType.name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricMagenta,
                                selectedLabelColor = CyberDarkBg,
                                containerColor = CyberDarkBg,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Sound Synthesis Parameters Rack (Filter, LFO, ADSR, Effects)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYNTH SOUND PARAMETERS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Button(
                        onClick = { showSaveDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = CyberDarkBg, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAVE PATCH", fontSize = 11.sp, color = CyberDarkBg, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cutoff Slider
                ParamSlider(
                    label = "Filter Cutoff",
                    valueText = "${cutoffVal.toInt()} Hz",
                    value = cutoffVal,
                    valueRange = 200f..12000f,
                    activeColor = NeonCyan,
                    onValueChange = {
                        cutoffVal = it
                        viewModel.updateFilterCutoff(it)
                    }
                )

                // Resonance Slider
                ParamSlider(
                    label = "Resonance",
                    valueText = String.format("%.2f", resVal),
                    value = resVal,
                    valueRange = 0.05f..0.95f,
                    activeColor = MatrixGreen,
                    onValueChange = {
                        resVal = it
                        viewModel.updateFilterResonance(it)
                    }
                )

                // LFO Rate Slider
                ParamSlider(
                    label = "LFO Rate",
                    valueText = "${String.format("%.1f", lfoRateVal)} Hz",
                    value = lfoRateVal,
                    valueRange = 0.2f..15.0f,
                    activeColor = AmberLED,
                    onValueChange = {
                        lfoRateVal = it
                        viewModel.updateLfoRate(it)
                    }
                )

                // LFO Depth Slider
                ParamSlider(
                    label = "LFO Depth / Modulation",
                    valueText = String.format("%.2f", lfoDepthVal),
                    value = lfoDepthVal,
                    valueRange = 0.0f..1.0f,
                    activeColor = AmberLED,
                    onValueChange = {
                        lfoDepthVal = it
                        viewModel.updateLfoDepth(it)
                    }
                )

                // Reverb Slider
                ParamSlider(
                    label = "Reverb Space Level",
                    valueText = "${(reverbVal * 100).toInt()}%",
                    value = reverbVal,
                    valueRange = 0.0f..1.0f,
                    activeColor = ElectricMagenta,
                    onValueChange = {
                        reverbVal = it
                        viewModel.updateReverb(it)
                    }
                )

                // Delay Slider
                ParamSlider(
                    label = "Delay Echo Level",
                    valueText = "${(delayVal * 100).toInt()}%",
                    value = delayVal,
                    valueRange = 0.0f..1.0f,
                    activeColor = ElectricMagenta,
                    onValueChange = {
                        delayVal = it
                        viewModel.updateDelay(it)
                    }
                )

                // Overdrive Distortion Slider
                ParamSlider(
                    label = "Overdrive Distortion",
                    valueText = "${(distortionVal * 100).toInt()}%",
                    value = distortionVal,
                    valueRange = 0.0f..1.0f,
                    activeColor = Color(0xFFFF3355),
                    onValueChange = {
                        distortionVal = it
                        viewModel.updateDistortion(it)
                    }
                )
            }
        }

        // Interactive Piano Keyboard
        item {
            Column {
                Text(
                    text = "INTERACTIVE SYNTH PIANO (C3 - F4)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                InteractivePianoKeyboard(
                    onNoteOn = { note -> viewModel.triggerNoteOn(note) },
                    onNoteOff = { note -> viewModel.triggerNoteOff(note) }
                )
            }
        }

        // Interactive Drum Pads
        item {
            InteractiveDrumPads(
                onDrumTrigger = { pad -> viewModel.triggerDrumPad(pad) }
            )
        }
    }

    // Save Patch Modal Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Z-Patch Preset", color = NeonCyan) },
            text = {
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text("Preset Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            viewModel.saveCurrentPreset(presetNameInput)
                        }
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("SAVE", color = CyberDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CyberSurface
        )
    }
}

@Composable
fun ParamSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(text = valueText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = activeColor, fontFamily = FontFamily.Monospace)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = CyberBorder
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}
