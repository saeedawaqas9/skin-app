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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.Product
import com.example.data.model.RoutineStep
import com.example.data.model.RoutineType

@Composable
fun RoutineBuilderScreen(
    routineSteps: List<RoutineStep>,
    catalogProducts: List<Product>,
    onAddStep: (type: RoutineType, category: String, product: Product?, title: String?, purpose: String, tip: String) -> Unit,
    onDeleteStep: (String) -> Unit,
    onResetToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentType = if (selectedTab == 0) RoutineType.MORNING else RoutineType.EVENING
    val steps = routineSteps.filter { it.routineType == currentType }.sortedBy { it.stepNumber }

    var showAddDialog by remember { mutableStateOf(false) }

    // Conflict Checker
    val hasRetinol = steps.any { it.customTitle?.contains("Retinol", ignoreCase = true) == true || it.purpose.contains("Retinol", ignoreCase = true) }
    val hasBhaAha = steps.any { it.customTitle?.contains("BHA", ignoreCase = true) == true || it.customTitle?.contains("Salicylic", ignoreCase = true) == true || it.customTitle?.contains("Glycolic", ignoreCase = true) == true }
    val hasSunscreen = steps.any { it.category.equals("Sunscreen", ignoreCase = true) }

    val conflictWarning = when {
        hasRetinol && hasBhaAha && currentType == RoutineType.EVENING ->
            "Formulation Caution: Layering Retinoids and exfoliating acids (BHA/AHA) in the same night can disrupt your barrier. Alternate nights."
        currentType == RoutineType.MORNING && !hasSunscreen ->
            "Safety Recommendation: A morning routine is incomplete without broad-spectrum SPF to protect your skin barrier and active treatment progress."
        else -> null
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_routine_step_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Step")
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Routine Builder",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Personalized step order & scientific compatibility",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onResetToday,
                    modifier = Modifier.testTag("reset_today_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Daily Checkmarks",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs AM / PM
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Morning Routine (${routineSteps.count { it.routineType == RoutineType.MORNING }})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Evening Routine (${routineSteps.count { it.routineType == RoutineType.EVENING }})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Safety Warning if conflicts found
            if (conflictWarning != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Conflict",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = conflictWarning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Steps List
            if (steps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No steps in this routine yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the '+' button below to add your first skincare step.",
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
                    items(steps) { step ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("routine_step_${step.id}"),
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${step.stepNumber}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = step.category,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteStep(step.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Step",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = step.customTitle ?: "${step.category} Step",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (step.purpose.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Purpose: ${step.purpose}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (step.usageTip.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Application Tip: ${step.usageTip}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStepDialog(
            routineType = currentType,
            catalogProducts = catalogProducts,
            onDismiss = { showAddDialog = false },
            onConfirm = { cat, prod, title, purpose, tip ->
                onAddStep(currentType, cat, prod, title, purpose, tip)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddStepDialog(
    routineType: RoutineType,
    catalogProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (category: String, product: Product?, title: String, purpose: String, tip: String) -> Unit
) {
    val categories = listOf("Cleanser", "Toner", "Serum", "Moisturizer", "Sunscreen", "Treatment", "Eye Cream")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var customTitle by remember { mutableStateOf("") }
    var customPurpose by remember { mutableStateOf("") }
    var customTip by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    val categoryProducts = catalogProducts.filter { it.category.equals(selectedCategory, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Step to ${if (routineType == RoutineType.MORNING) "Morning" else "Evening"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(text = "Step Category:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(4).forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedCategory == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCategory = cat
                                        selectedProduct = null
                                    }
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (categoryProducts.isNotEmpty()) {
                    item {
                        Text(text = "Pick from verified catalog (or enter custom below):", style = MaterialTheme.typography.labelSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            categoryProducts.take(3).forEach { prod ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedProduct == prod) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedProduct = prod
                                            customTitle = prod.name
                                            customPurpose = prod.description
                                            customTip = "Apply evenly as part of sequence."
                                        }
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "${prod.brand} - ${prod.name}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selectedProduct == prod) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedProduct == prod) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = customPurpose,
                        onValueChange = { customPurpose = it },
                        label = { Text("Purpose (e.g., Hydrate, Soothe barrier)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = customTip,
                        onValueChange = { customTip = it },
                        label = { Text("Usage Tip (e.g., Apply to damp skin)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val title = customTitle.ifBlank { selectedProduct?.name ?: "$selectedCategory Step" }
                    val purpose = customPurpose.ifBlank { selectedProduct?.description ?: "Nourish and protect skin" }
                    val tip = customTip.ifBlank { "Apply as part of your regular sequence." }
                    onConfirm(selectedCategory, selectedProduct, title, purpose, tip)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Add Step", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
