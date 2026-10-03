package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AX100ViewModel
import com.example.ui.components.HardwareTopHeader
import com.example.ui.screens.MidiMatrixScreen
import com.example.ui.screens.PresetsHistoryScreen
import com.example.ui.screens.SynthesizerScreen
import com.example.ui.screens.ZFileStudioScreen
import com.example.ui.theme.AX100Theme
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: AX100ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AX100Theme {
                AX100MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AX100MainApp(
    viewModel: AX100ViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    val midiEngine = viewModel.midiEngine
    val bpm by midiEngine.currentBpm.collectAsState()
    val isPlaying by midiEngine.isPlaying.collectAsState()
    val isRecording by midiEngine.isRecording.collectAsState()
    val isLooping by midiEngine.isLooping.collectAsState()

    val synthEngine = viewModel.synthEngine
    val isAudioActive by synthEngine.isAudioActive.collectAsState()

    val tabTitles = listOf("Z-FILE STUDIO", "SYNTHESIZER", "MIDI MATRIX", "PRESETS & MEMORY")
    val tabIcons = listOf(
        Icons.Default.AutoAwesome,
        Icons.Default.Piano,
        Icons.Default.Memory,
        Icons.Default.Save
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberDarkBg,
        topBar = {
            HardwareTopHeader(
                statusMessage = statusMsg,
                bpm = bpm,
                isPlaying = isPlaying,
                isRecording = isRecording,
                isLooping = isLooping,
                isAudioActive = isAudioActive,
                onBpmChange = { newBpm -> midiEngine.setBpm(newBpm) },
                onTogglePlay = { midiEngine.togglePlay() },
                onToggleRecord = { midiEngine.toggleRecord() },
                onToggleLoop = { midiEngine.toggleLoop() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, CyberBorder, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(index) },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = title,
                                tint = if (isSelected) NeonCyan else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = NeonCyan.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ZFileStudioScreen(viewModel = viewModel)
                1 -> SynthesizerScreen(viewModel = viewModel)
                2 -> MidiMatrixScreen(viewModel = viewModel)
                3 -> PresetsHistoryScreen(viewModel = viewModel)
            }
        }
    }
}
