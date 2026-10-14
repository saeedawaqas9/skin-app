package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserPreferences
import com.example.ui.components.SafeNoticeBanner

enum class OnboardingStep(val title: String, val subtitle: String) {
    WELCOME("Welcome", "Introduction & Philosophy"),
    GOALS("Skin Goals", "Identify your target outcomes"),
    ROUTINE_INVENTORY("Current Routine", "Your habits & existing products"),
    SENSITIVITIES("Formulation Preferences", "Fragrance, texture & irritants"),
    COMPLEXITY_BUDGET("Complexity & Budget", "Time commitment & currency"),
    REVIEW("Routine Blueprint", "Review your personalized profile")
}

@Composable
fun OnboardingScreen(
    currentPreferences: UserPreferences,
    onComplete: (UserPreferences) -> Unit,
    modifier: Modifier = Modifier
) {
    // State-driven tracking for all user selections
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }

    val selectedGoals = remember {
        mutableStateListOf<String>().apply {
            addAll(currentPreferences.skinGoals.ifEmpty { listOf("Hydration", "Skin Barrier Repair") })
        }
    }
    var experienceLevel by remember { mutableStateOf(currentPreferences.experienceLevel) }
    var currentRoutineHabit by remember {
        mutableStateOf(currentPreferences.currentRoutineLevel.ifBlank { "Basic (Cleanser & Moisturizer)" })
    }
    val existingProductsOwned = remember {
        mutableStateListOf<String>().apply {
            addAll(currentPreferences.existingProductsOwned.ifEmpty { listOf("Cleanser", "Moisturizer") })
        }
    }
    val selectedSensitivities = remember {
        mutableStateListOf<String>().apply {
            addAll(currentPreferences.sensitivities)
        }
    }
    var fragrancePreference by remember { mutableStateOf(currentPreferences.fragrancePreference) }
    var productTexturePreference by remember {
        mutableStateOf(currentPreferences.productTexturePreference.ifBlank { "Lightweight Gels & Fluids" })
    }
    var routineComplexity by remember { mutableStateOf(currentPreferences.routineComplexity) }
    var selectedCurrency by remember { mutableStateOf(currentPreferences.currency) }
    var budgetValue by remember { mutableDoubleStateOf(currentPreferences.budget) }

    val stepIndex = currentStep.ordinal
    val totalSteps = OnboardingStep.entries.size - 1 // Exclude welcome from calculation
    val progress = if (stepIndex == 0) 0.05f else (stepIndex.toFloat() / totalSteps)

    fun buildUserPreferences(): UserPreferences {
        return UserPreferences(
            skinGoals = selectedGoals.toList(),
            experienceLevel = experienceLevel,
            currentRoutineLevel = currentRoutineHabit,
            existingProductsOwned = existingProductsOwned.toList(),
            sensitivities = selectedSensitivities.toList(),
            fragrancePreference = fragrancePreference,
            productTexturePreference = productTexturePreference,
            routineComplexity = routineComplexity,
            budget = budgetValue,
            currency = selectedCurrency,
            isOnboardingDone = true
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Navigation & Progress Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep != OnboardingStep.WELCOME) {
                IconButton(
                    onClick = {
                        val prevIndex = (currentStep.ordinal - 1).coerceAtLeast(0)
                        currentStep = OnboardingStep.entries[prevIndex]
                    },
                    modifier = Modifier.testTag("prev_step_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            if (currentStep != OnboardingStep.WELCOME) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Step $stepIndex of $totalSteps",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Text(
                    text = "Skin Consultation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            TextButton(
                onClick = { onComplete(buildUserPreferences()) },
                modifier = Modifier.testTag("skip_onboarding_button")
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Animated Screen Content
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut()
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> {
                    WelcomeStepView(
                        onStart = { currentStep = OnboardingStep.GOALS }
                    )
                }
                OnboardingStep.GOALS -> {
                    GoalsStepView(
                        selectedGoals = selectedGoals,
                        onToggleGoal = { goal ->
                            if (selectedGoals.contains(goal)) {
                                if (selectedGoals.size > 1) selectedGoals.remove(goal)
                            } else {
                                selectedGoals.add(goal)
                            }
                        },
                        onNext = { currentStep = OnboardingStep.ROUTINE_INVENTORY }
                    )
                }
                OnboardingStep.ROUTINE_INVENTORY -> {
                    RoutineInventoryStepView(
                        currentHabit = currentRoutineHabit,
                        onHabitSelected = { currentRoutineHabit = it },
                        experienceLevel = experienceLevel,
                        onExperienceSelected = { experienceLevel = it },
                        ownedProducts = existingProductsOwned,
                        onToggleProduct = { prod ->
                            if (existingProductsOwned.contains(prod)) {
                                existingProductsOwned.remove(prod)
                            } else {
                                existingProductsOwned.add(prod)
                            }
                        },
                        onNext = { currentStep = OnboardingStep.SENSITIVITIES }
                    )
                }
                OnboardingStep.SENSITIVITIES -> {
                    SensitivitiesStepView(
                        fragrancePreference = fragrancePreference,
                        onFragranceSelected = { fragrancePreference = it },
                        texturePreference = productTexturePreference,
                        onTextureSelected = { productTexturePreference = it },
                        sensitivities = selectedSensitivities,
                        onToggleSensitivity = { sens ->
                            if (selectedSensitivities.contains(sens)) {
                                selectedSensitivities.remove(sens)
                            } else {
                                selectedSensitivities.add(sens)
                            }
                        },
                        onNext = { currentStep = OnboardingStep.COMPLEXITY_BUDGET }
                    )
                }
                OnboardingStep.COMPLEXITY_BUDGET -> {
                    ComplexityBudgetStepView(
                        complexity = routineComplexity,
                        onComplexitySelected = { routineComplexity = it },
                        currency = selectedCurrency,
                        onCurrencySelected = { curr ->
                            selectedCurrency = curr
                            budgetValue = when (curr) {
                                "PKR" -> 4000.0
                                "EUR" -> 45.0
                                "GBP" -> 40.0
                                else -> 50.0
                            }
                        },
                        budget = budgetValue,
                        onBudgetChanged = { budgetValue = it },
                        onNext = { currentStep = OnboardingStep.REVIEW }
                    )
                }
                OnboardingStep.REVIEW -> {
                    ReviewStepView(
                        preferences = buildUserPreferences(),
                        onConfirm = { onComplete(buildUserPreferences()) },
                        onEditSection = { targetStep -> currentStep = targetStep }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 0: WELCOME & PHILOSOPHY
// -------------------------------------------------------------
@Composable
fun WelcomeStepView(
    onStart: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = "SkinRoutine AI",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SkinRoutine AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Your Skin. Your Routine. Your Confidence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Why Personalization Matters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "There is no single 'miracle product'. Effective skincare comes from understanding formulation chemistry, respecting your unique moisture barrier, and applying compatible steps consistently.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    PillBullet(text = "Evidence-based cosmetic active recommendations")
                    PillBullet(text = "Exclusion filters for fragrance & sensitizers")
                    PillBullet(text = "Budget-conscious bundles in your preferred currency")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SafeNoticeBanner(
                message = "Important Medical Disclaimer: SkinRoutine AI provides cosmetic formulation guidance. It does not medically diagnose skin conditions or prescribe treatments. Consult a dermatologist for persistent skin disease."
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_consultation_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Begin Personalized Consultation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 1: SKINCARE GOALS
// -------------------------------------------------------------
@Composable
fun GoalsStepView(
    selectedGoals: List<String>,
    onToggleGoal: (String) -> Unit,
    onNext: () -> Unit
) {
    data class GoalItem(val title: String, val subtitle: String, val icon: ImageVector)

    val goals = listOf(
        GoalItem("Hydration & Deep Moisture", "Quench dull tightness, replenish plumping humectants", Icons.Default.WaterDrop),
        GoalItem("Skin Barrier Repair", "Soothe over-exfoliated stinging, replenish ceramides & lipids", Icons.Default.Shield),
        GoalItem("Acne & Blemish Clarifying", "Unclog pores, target blackheads & balance sebum", Icons.Default.Sanitizer),
        GoalItem("Brightening & Dark Spots", "Fade post-acne marks, hyperpigmentation & even tone", Icons.Default.AutoAwesome),
        GoalItem("Calming & Redness Relief", "Quench flushed, reactive, rosacea-prone skin with cica", Icons.Default.Spa),
        GoalItem("Healthy Aging & Firmness", "Support cell renewal, collagen resilience & elasticity", Icons.Default.Schedule),
        GoalItem("Pore Refinement & Oil Balance", "Minimize pore visibility & mattify excess daytime shine", Icons.Default.Favorite)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "What are your primary skincare priorities?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Select all that apply. We'll balance active treatments so your skin barrier stays calm.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(goals.size) { index ->
            val item = goals[index]
            val isSelected = selectedGoals.contains(item.title)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleGoal(item.title) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("goal_${item.title.take(10)}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_step_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Continue (${selectedGoals.size} Selected)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 2: ROUTINE HABIT & INVENTORY
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutineInventoryStepView(
    currentHabit: String,
    onHabitSelected: (String) -> Unit,
    experienceLevel: String,
    onExperienceSelected: (String) -> Unit,
    ownedProducts: List<String>,
    onToggleProduct: (String) -> Unit,
    onNext: () -> Unit
) {
    val habits = listOf(
        "Starting fresh (No routine currently)",
        "Basic (Cleanser & Moisturizer)",
        "Inconsistent (Skips days or unsure of order)",
        "Advanced (Daily multi-step routine)"
    )

    val productCategories = listOf(
        "Cleanser",
        "Toner / Essence",
        "Vitamin C Serum",
        "Hyaluronic Acid Serum",
        "Niacinamide Serum",
        "Exfoliant (AHA/BHA)",
        "Retinol / Retinoid",
        "Moisturizer",
        "Sunscreen (SPF 30+)"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "What does your routine look like today?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "We will seamlessly incorporate products you already love and identify any missing essential steps.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Routine Consistency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    habits.forEach { habit ->
                        val isSelected = currentHabit == habit
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onHabitSelected(habit) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .testTag("habit_$habit")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = habit,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Products You Already Own",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any you own. We'll automatically add them to your Beauty Shelf vanity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        productCategories.forEach { cat ->
                            val isSelected = ownedProducts.contains(cat)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onToggleProduct(cat) }
                                    .testTag("owned_$cat")
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_step_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Continue to Formulation Preferences",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 3: SENSITIVITIES & FRAGRANCE PREFERENCES
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SensitivitiesStepView(
    fragrancePreference: String,
    onFragranceSelected: (String) -> Unit,
    texturePreference: String,
    onTextureSelected: (String) -> Unit,
    sensitivities: List<String>,
    onToggleSensitivity: (String) -> Unit,
    onNext: () -> Unit
) {
    val fragranceOptions = listOf(
        "Fragrance-Free Preferred" to "Strictly zero parfum or masking fragrances (ideal for sensitive skin)",
        "Subtle / Natural" to "Gentle, non-lingering natural scents are acceptable",
        "Any" to "Light scented formulations are completely fine"
    )

    val textures = listOf(
        "Lightweight Gels & Fluids",
        "Rich Nourishing Creams & Balms",
        "Balanced Lotions"
    )

    val potentialSensitizers = listOf(
        "Fragrance / Parfum",
        "Essential Oils",
        "Alcohol Denat (Drying Alcohol)",
        "High Niacinamide (>5%)",
        "Strong Retinoids",
        "Direct Acids (AHA/BHA)",
        "Sulfates (SLS)"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Formulation & Sensitivity Rules",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "We treat known sensitizers cautiously and enforce strict exclusion filters.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Fragrance Choice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fragrance Preference",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    fragranceOptions.forEach { (option, desc) ->
                        val isSelected = fragrancePreference == option
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onFragranceSelected(option) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .testTag("fragrance_$option")
                        ) {
                            Column {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Known Sensitivities
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ingredients to Exclude",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Any selected ingredients will be flagged with warning alerts and filtered out.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        potentialSensitizers.forEach { sens ->
                            val isSelected = sensitivities.contains(sens)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onToggleSensitivity(sens) }
                                    .testTag("sens_$sens")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = sens,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Texture Preference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferred Formula Texture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        textures.forEach { tex ->
                            val isSelected = texturePreference == tex
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onTextureSelected(tex) }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = tex,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SafeNoticeBanner(
                message = "Note: A high-sounding chemical name (like 'Tocopherol' or 'Sodium Hyaluronate') does not mean an ingredient is harsh. Many are gentle vitamins and skin-identical lipids."
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_step_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Continue to Complexity & Budget",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 4: ROUTINE COMPLEXITY & BUDGET
// -------------------------------------------------------------
@Composable
fun ComplexityBudgetStepView(
    complexity: String,
    onComplexitySelected: (String) -> Unit,
    currency: String,
    onCurrencySelected: (String) -> Unit,
    budget: Double,
    onBudgetChanged: (Double) -> Unit,
    onNext: () -> Unit
) {
    val complexityCards = listOf(
        Triple("Minimal (2-3 steps)", "Cleanser, Moisturizer, SPF", "Takes ~3 mins/day • Best for beginners & barrier recovery"),
        Triple("Balanced (4-5 steps)", "Cleanser, Toner, Targeted Active, Moisturizer, SPF", "Takes ~5 mins/day • Optimal balance of protection & targeted care"),
        Triple("Comprehensive (6+ steps)", "Double Cleanse, Essence, Dual Actives, Eye Care, Barrier Cream, SPF", "Takes ~8-10 mins/day • For dedicated skincare enthusiasts")
    )

    val currencies = listOf("USD", "PKR", "EUR", "GBP")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "How much time and budget do you want to invest?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Consistency always outperforms complexity. Pick a routine you can easily sustain.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Complexity selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferred Routine Structure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    complexityCards.forEach { (title, steps, desc) ->
                        val isSelected = complexity == title
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onComplexitySelected(title) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .testTag("comp_$title")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(
                                    text = "Steps: $steps",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Currency & Budget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferred Currency & Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { curr ->
                            val isSelected = currency == curr
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onCurrencySelected(curr) }
                                    .padding(vertical = 10.dp)
                                    .testTag("currency_$curr")
                            ) {
                                Text(
                                    text = curr,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val formatted = when (currency) {
                        "PKR" -> "PKR ${String.format("%,.0f", budget)}"
                        "EUR" -> "€${String.format("%.0f", budget)}"
                        "GBP" -> "£${String.format("%.0f", budget)}"
                        else -> "$${String.format("%.0f", budget)}"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Budget Target",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatted,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = budget.toFloat(),
                        onValueChange = { onBudgetChanged(it.toDouble()) },
                        valueRange = when (currency) {
                            "PKR" -> 1500f..25000f
                            else -> 15f..200f
                        },
                        steps = 20,
                        modifier = Modifier.testTag("budget_slider")
                    )

                    Text(
                        text = "Allows ~${
                            when (currency) {
                                "PKR" -> "PKR ${(budget / 3).toInt()}"
                                "EUR" -> "€${(budget / 3).toInt()}"
                                "GBP" -> "£${(budget / 3).toInt()}"
                                else -> "$${(budget / 3).toInt()}"
                            }
                        } per verified product in your primary routine.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_step_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Review Routine Blueprint",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 5: REVIEW BLUEPRINT & SAVE
// -------------------------------------------------------------
@Composable
fun ReviewStepView(
    preferences: UserPreferences,
    onConfirm: () -> Unit,
    onEditSection: (OnboardingStep) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Personalized Routine Blueprint",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Review your consultation answers before saving your profile to the database.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    ReviewRow(
                        title = "Target Goals",
                        content = preferences.skinGoals.joinToString(", "),
                        onEdit = { onEditSection(OnboardingStep.GOALS) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ReviewRow(
                        title = "Current Habit & Vanity",
                        content = "${preferences.currentRoutineLevel} • Owns: ${if (preferences.existingProductsOwned.isEmpty()) "None yet" else preferences.existingProductsOwned.joinToString(", ")}",
                        onEdit = { onEditSection(OnboardingStep.ROUTINE_INVENTORY) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ReviewRow(
                        title = "Formulation Filters",
                        content = "Fragrance: ${preferences.fragrancePreference} • Exclusions: ${if (preferences.sensitivities.isEmpty()) "None" else preferences.sensitivities.joinToString(", ")}",
                        onEdit = { onEditSection(OnboardingStep.SENSITIVITIES) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ReviewRow(
                        title = "Complexity & Budget",
                        content = "${preferences.routineComplexity} • Budget: ${preferences.formatPrice(preferences.budget)}",
                        onEdit = { onEditSection(OnboardingStep.COMPLEXITY_BUDGET) }
                    )
                }
            }
        }

        item {
            SafeNoticeBanner(
                message = "All preferences are saved to your local database. You can adjust your goals, currency, or sensitivities anytime in Settings."
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("complete_onboarding_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate My Personalized Routine",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ReviewRow(
    title: String,
    content: String,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Edit",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onEdit() }
                .padding(4.dp)
        )
    }
}

@Composable
fun PillBullet(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
