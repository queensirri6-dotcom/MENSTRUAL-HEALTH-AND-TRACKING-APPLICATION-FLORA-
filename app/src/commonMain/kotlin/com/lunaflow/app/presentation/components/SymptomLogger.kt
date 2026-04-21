package com.lunaflow.app.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class Symptom(val id: String, val label: String, val icon: ImageVector)

@Composable
fun SymptomLogger(
    onSymptomSelected: (Symptom) -> Unit,
    modifier: Modifier = Modifier
) {
    val symptoms = listOf(
        Symptom("cramps", "Cramps", Icons.Default.Warning),
        Symptom("headache", "Headache", Icons.Default.Info),
        Symptom("bloating", "Bloating", Icons.Default.AccountBox),
        Symptom("moody", "Moody", Icons.Default.Face),
        Symptom("tired", "Tired", Icons.Default.Settings),
        Symptom("happy", "Happy", Icons.Default.Star)
    )

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(250.dp)
        ) {
            items(symptoms) { symptom ->
                SymptomItem(symptom = symptom, onClick = { onSymptomSelected(symptom) })
            }
        }
    }
}

@Composable
fun SymptomItem(symptom: Symptom, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = symptom.icon,
            contentDescription = symptom.label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = symptom.label, style = MaterialTheme.typography.bodyMedium)
    }
}
