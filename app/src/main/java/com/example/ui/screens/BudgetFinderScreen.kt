package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.BudgetBundle
import com.example.data.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.components.SafeNoticeBanner
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetFinderScreen(
    currency: String,
    bundleResult: BudgetBundle?,
    onCalculateBudget: (budget: Double, categories: List<String>) -> Unit,
    onSelectProduct: (Product) -> Unit,
    onSaveToShelf: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialBudgetStr = when (currency) {
        "PKR" -> "5000"
        "EUR" -> "40"
        "GBP" -> "35"
        else -> "45"
    }

    var budgetInput by remember { mutableStateOf(initialBudgetStr) }
    val availableCategories = listOf("Cleanser", "Toner", "Serum", "Moisturizer", "Sunscreen")
    val selectedCategories = remember {
        mutableStateListOf("Cleanser", "Moisturizer")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Title
            Text(
                text = "Budget-Based Finder",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Find high-performing verified formulas strictly within your target budget",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "1. Enter Target Budget ($currency)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Budget in $currency") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "2. Select Needed Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableCategories.forEach { cat ->
                            val isSelected = selectedCategories.contains(cat)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isSelected) {
                                            if (selectedCategories.size > 1) selectedCategories.remove(cat)
                                        } else {
                                            selectedCategories.add(cat)
                                        }
                                    }
                                    .testTag("budget_cat_$cat")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val budgetNum = budgetInput.toDoubleOrNull() ?: 50.0
                            onCalculateBudget(budgetNum, selectedCategories.toList())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("calculate_bundle_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculate Optimal Bundle", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Calculation Results
        if (bundleResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (bundleResult.savings >= 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Estimated Cost",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when (currency) {
                                        "PKR" -> "PKR ${String.format("%,.0f", bundleResult.totalPrice)}"
                                        "EUR" -> "€${String.format("%.2f", bundleResult.totalPrice)}"
                                        "GBP" -> "£${String.format("%.2f", bundleResult.totalPrice)}"
                                        else -> "$${String.format("%.2f", bundleResult.totalPrice)}"
                                    },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (bundleResult.savings >= 0) "Under Budget" else "Over Budget",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bundleResult.savings >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = bundleResult.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Selected Bundle Products (${bundleResult.products.size})",
                    subtitle = "Exact verified pricing calculated"
                )
            }

            items(bundleResult.products) { product ->
                ProductCard(
                    product = product,
                    currency = currency,
                    onClick = { onSelectProduct(product) },
                    onSaveToShelf = { onSaveToShelf(product) }
                )
            }

            if (bundleResult.alternativeProducts.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Affordable Alternatives",
                        subtitle = "Other suitable options in selected categories"
                    )
                }

                items(bundleResult.alternativeProducts) { alt ->
                    ProductCard(
                        product = alt,
                        currency = currency,
                        onClick = { onSelectProduct(alt) },
                        onSaveToShelf = { onSaveToShelf(alt) }
                    )
                }
            }
        }

        item {
            SafeNoticeBanner(
                message = "SkinRoutine AI never assumes discounts or fabricates fake prices. All product prices reflect current verified manufacturer listings."
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
