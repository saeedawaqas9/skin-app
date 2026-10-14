package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalEntry
import com.example.ui.components.LuxuryStatCard
import com.example.ui.components.SafeNoticeBanner

@Composable
fun JournalScreen(
    journalEntries: List<JournalEntry>,
    onAddEntry: (morning: Boolean, evening: Boolean, comfort: Int, hydration: Int, irritation: Int, notes: String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLogDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("log_journal_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Day")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = "Progress & Skin Journal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Track consistency, comfort observations, and barrier recovery",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LuxuryStatCard(
                    title = "Total Logged Days",
                    value = "${journalEntries.size}",
                    subtitle = "Days of conscious tracking",
                    modifier = Modifier.weight(1f)
                )
                val avgComfort = if (journalEntries.isEmpty()) 4.5 else journalEntries.map { it.comfortScore.toDouble() }.average()
                LuxuryStatCard(
                    title = "Average Comfort",
                    value = "${String.format("%.1f", avgComfort)} / 5",
                    subtitle = "Skin barrier satisfaction",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (journalEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Journal Entries Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the '+' button to log today's skin comfort and routine completion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(journalEntries) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("journal_entry_${entry.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.dateString,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    IconButton(
                                        onClick = { onDeleteEntry(entry.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete entry",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Routines Completed
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (entry.morningCompleted) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("AM Routine Done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (entry.eveningCompleted) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("PM Routine Done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Scores
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    ScorePill(title = "Comfort", score = entry.comfortScore, max = 5)
                                    ScorePill(title = "Hydration", score = entry.hydrationScore, max = 5)
                                    ScorePill(title = "Irritation", score = entry.irritationScore, max = 5, isNegative = true)
                                }

                                if (entry.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "\"${entry.notes}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SafeNoticeBanner(
                            message = "Skincare results manifest gradually over multiple cell turnover cycles (approx 28-40 days). Consistency with hydration and SPF is the strongest predictor of barrier resilience."
                        )
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
        }
    }

    if (showLogDialog) {
        LogEntryDialog(
            onDismiss = { showLogDialog = false },
            onConfirm = { am, pm, comf, hyd, irr, note ->
                onAddEntry(am, pm, comf, hyd, irr, note)
                showLogDialog = false
            }
        )
    }
}

@Composable
fun ScorePill(title: String, score: Int, max: Int, isNegative: Boolean = false) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "$score / $max",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isNegative && score > 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LogEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (am: Boolean, pm: Boolean, comfort: Int, hydration: Int, irritation: Int, notes: String) -> Unit
) {
    var morningDone by remember { mutableStateOf(true) }
    var eveningDone by remember { mutableStateOf(true) }
    var comfort by remember { mutableIntStateOf(4) }
    var hydration by remember { mutableIntStateOf(4) }
    var irritation by remember { mutableIntStateOf(1) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Log Today's Skin Observation", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(text = "Routine Completion:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (morningDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { morningDone = !morningDone }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Morning Done: ${if (morningDone) "Yes" else "No"}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (morningDone) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (eveningDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { eveningDone = !eveningDone }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Evening Done: ${if (eveningDone) "Yes" else "No"}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (eveningDone) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Text(text = "Skin Comfort Score: $comfort / 5", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = comfort.toFloat(),
                        onValueChange = { comfort = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                item {
                    Text(text = "Hydration Level: $hydration / 5", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = hydration.toFloat(),
                        onValueChange = { hydration = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                item {
                    Text(text = "Irritation / Redness: $irritation / 5 (1 = None, 5 = High)", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = irritation.toFloat(),
                        onValueChange = { irritation = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Personal Notes & Triggers") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Skin felt soothed after Cicaplast...") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(morningDone, eveningDone, comfort, hydration, irritation, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Observation", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
