package com.lunaflow.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Symptom(val id: String, val label: String, val icon: ImageVector, val emoji: String)

@Composable
fun SymptomLogger(
    onSymptomSelected: (Symptom, Int) -> Unit, // symptom + severity
    loggedSymptoms: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    // S1 — Expanded symptom list (20 symptoms)
    val symptoms = listOf(
        Symptom("cramps",         "Cramps",         Icons.Default.Warning,     "😣"),
        Symptom("headache",       "Headache",        Icons.Default.Info,        "🤕"),
        Symptom("bloating",       "Bloating",        Icons.Default.AccountBox,  "🫢"),
        Symptom("moody",          "Moody",           Icons.Default.Face,        "😤"),
        Symptom("tired",          "Tired",           Icons.Default.Settings,    "😴"),
        Symptom("happy",          "Happy",           Icons.Default.Star,        "😊"),
        Symptom("nausea",         "Nausea",          Icons.Default.Warning,     "🤢"),
        Symptom("back_pain",      "Back Pain",       Icons.Default.Info,        "🔙"),
        Symptom("acne",           "Acne",            Icons.Default.AccountBox,  "😬"),
        Symptom("anxiety",        "Anxiety",         Icons.Default.Face,        "😰"),
        Symptom("spotting",       "Spotting",        Icons.Default.Warning,     "🩸"),
        Symptom("discharge",      "Discharge",       Icons.Default.Info,        "💧"),
        Symptom("high_energy",    "High Energy",     Icons.Default.Star,        "⚡"),
        Symptom("tender_breasts", "Tender Breasts",  Icons.Default.AccountBox,  "💞"),
        Symptom("constipation",   "Constipation",    Icons.Default.Warning,     "😖"),
        Symptom("diarrhea",       "Diarrhea",        Icons.Default.Warning,     "🚽"),
        Symptom("brain_fog",      "Brain Fog",       Icons.Default.Info,        "😶‍🌫️"),
        Symptom("cravings",       "Cravings",        Icons.Default.Star,        "🍫"),
        Symptom("hot_flashes",    "Hot Flashes",     Icons.Default.Face,        "🥵"),
        Symptom("insomnia",       "Insomnia",        Icons.Default.Settings,    "🌙")
    )

    var pendingSeveritySymptom by remember { mutableStateOf<Symptom?>(null) }
    var selectedSeverity by remember { mutableStateOf(3) }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "🩺 How are you feeling?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (loggedSymptoms.isNotEmpty()) {
            Text(
                "Logged today: ${loggedSymptoms.size} symptoms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(340.dp)
        ) {
            items(symptoms) { symptom ->
                SymptomItem(
                    symptom = symptom,
                    isLogged = symptom.id in loggedSymptoms,
                    onClick = {
                        pendingSeveritySymptom = symptom
                        selectedSeverity = 3
                    }
                )
            }
        }

        // S2 — Severity selector bottom sheet style
        AnimatedVisibility(visible = pendingSeveritySymptom != null) {
            pendingSeveritySymptom?.let { symptom ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "${symptom.emoji} Rate ${symptom.label} severity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Mild", "Low", "Medium", "High", "Severe").forEachIndexed { i, label ->
                                val sev = i + 1
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selectedSeverity == sev)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedSeverity = sev }
                                        .padding(8.dp)
                                ) {
                                    Text("$sev", fontWeight = FontWeight.Bold, color =
                                    if (selectedSeverity == sev) Color.White
                                    else MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                                    Text(label, fontSize = 8.sp,
                                        color = if (selectedSeverity == sev) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { pendingSeveritySymptom = null },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                            Button(
                                onClick = {
                                    onSymptomSelected(symptom, selectedSeverity)
                                    pendingSeveritySymptom = null
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Log It ✓") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SymptomItem(symptom: Symptom, isLogged: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isLogged) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (isLogged) 1.5.dp else 0.dp,
                color = if (isLogged) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(symptom.emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = symptom.label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = if (isLogged) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isLogged) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
        if (isLogged) Text("✓", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
    }
}
