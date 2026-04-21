package com.lunaflow.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lunaflow.app.data.local.LunaDatabase
import com.lunaflow.app.data.local.getDatabaseBuilder
import com.lunaflow.app.data.local.getRoomDatabase
import com.lunaflow.app.data.remote.GeminiService
import com.lunaflow.app.data.repository.LunaRepositoryImpl
import com.lunaflow.app.domain.manager.CycleManager
import com.lunaflow.app.presentation.CycleIntent
import com.lunaflow.app.presentation.CycleViewModel
import com.lunaflow.app.presentation.components.ChatInterface
import com.lunaflow.app.presentation.components.EducationTipCard
import com.lunaflow.app.presentation.components.HeroDashboard
import com.lunaflow.app.presentation.components.SymptomLogger
import com.lunaflow.app.presentation.theme.LunaFlowTheme
import kotlinx.coroutines.rememberCoroutineScope

@Composable
fun App() {
    LunaFlowTheme {
        val scope = rememberCoroutineScope()
        
        // Dependency Injection (Simplified)
        val database = remember { getRoomDatabase(getDatabaseBuilder()) }
        val repository = remember { LunaRepositoryImpl(database.cycleDao()) }
        val cycleManager = remember { CycleManager() }
        val geminiService = remember { GeminiService(apiKey = "YOUR_GEMINI_API_KEY") }
        
        val viewModel = remember {
            CycleViewModel(
                repository = repository,
                cycleManager = cycleManager,
                geminiService = geminiService,
                scope = scope
            )
        }
        
        val uiState by viewModel.uiState.collectAsState()
        var selectedTab by remember { mutableStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Today") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Face, contentDescription = "Tutor") },
                        label = { Text("AI Tutor") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Log") },
                        label = { Text("Log") }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                when (selectedTab) {
                    0 -> {
                        uiState.status?.let { status ->
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(32.dp))
                                HeroDashboard(
                                    phase = status.currentPhase,
                                    daysRemaining = status.daysUntilNextPeriod
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                if (uiState.educationMode) {
                                    EducationTipCard()
                                }
                                
                                Button(
                                    onClick = { viewModel.onIntent(CycleIntent.GenerateWellnessPlan) },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Generate Weekly Wellness Plan")
                                }
                                
                                uiState.wellnessPlan?.let { plan ->
                                    Text(
                                        text = plan,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        ChatInterface(
                            messages = uiState.chatMessages,
                            onSendMessage = { viewModel.onIntent(CycleIntent.SendChatMessage(it)) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    2 -> {
                        SymptomLogger(
                            onSymptomSelected = { symptom ->
                                viewModel.onIntent(CycleIntent.LogSymptom(symptom.id))
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
