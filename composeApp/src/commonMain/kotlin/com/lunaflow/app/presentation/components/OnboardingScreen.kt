package com.lunaflow.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.minus
import kotlinx.datetime.DatePeriod

@Composable
fun OnboardingScreen(
    onComplete: (LocalDate, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var cycleLength by remember { mutableStateOf("28") }
    var periodLength by remember { mutableStateOf("5") }
    var daysSinceLastPeriod by remember { mutableStateOf("0") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to LunaFlow!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Let's personalize your experience.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = daysSinceLastPeriod,
            onValueChange = { daysSinceLastPeriod = it },
            label = { Text("Days since your last period started") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = cycleLength,
            onValueChange = { cycleLength = it },
            label = { Text("Average cycle length (days)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = periodLength,
            onValueChange = { periodLength = it },
            label = { Text("Average period length (days)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (isError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Please enter valid numbers.", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val cycleStr = cycleLength.toIntOrNull()
                val periodStr = periodLength.toIntOrNull()
                val daysStr = daysSinceLastPeriod.toIntOrNull()

                if (cycleStr != null && periodStr != null && daysStr != null) {
                    val lastPeriod = Clock.System.todayIn(TimeZone.currentSystemDefault())
                        .minus(kotlinx.datetime.DatePeriod(days = daysStr))
                    onComplete(lastPeriod, cycleStr, periodStr)
                } else {
                    isError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Complete Setup")
        }
    }
}
