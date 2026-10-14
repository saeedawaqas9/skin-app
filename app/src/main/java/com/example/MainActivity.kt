package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.RoutineType
import com.example.ui.components.IngredientDetailDialog
import com.example.ui.components.ProductDetailDialog
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.BeautyShelfScreen
import com.example.ui.screens.BudgetFinderScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IngredientIntelligenceScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RoutineBuilderScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.screens.signOutUser
import com.example.ui.theme.SkinRoutineTheme
import com.example.ui.viewmodel.SkinRoutineViewModel
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

enum class MainDestination(val label: String, val icon: ImageVector) {
    HOME("Today", Icons.Default.Spa),
    ROUTINES("Routine", Icons.Default.FormatListNumbered),
    CATALOG("Catalog", Icons.Default.Search),
    ASSISTANT("AI Guide", Icons.Default.AutoAwesome),
    MORE("More", Icons.Default.MoreHoriz)
}

enum class SubDestination {
    NONE,
    INGREDIENTS,
    BUDGET,
    SHELF,
    JOURNAL,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check before setContent or ViewModels
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            SkinRoutineTheme {
                SkinRoutineApp()
            }
        }
    }
}

@Composable
fun SkinRoutineApp(
    viewModel: SkinRoutineViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Silent auto-sign-in on startup if not already signed in
    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                },
                onUnauthenticated = {
                    // Remain in unauthenticated state
                },
                scope = coroutineScope
            )
        }
    }

    // 1. Mandatory Auth Gate: If unauthenticated, show Google Sign-In
    if (currentUser == null) {
        SignInScreen(
            onAuthSuccess = { uid ->
                currentUser = Firebase.auth.currentUser
            }
        )
        return
    }

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val shelfProducts by viewModel.shelfProducts.collectAsStateWithLifecycle()
    val routineSteps by viewModel.routineSteps.collectAsStateWithLifecycle()
    val journalEntries by viewModel.journalEntries.collectAsStateWithLifecycle()
    val catalogProducts by viewModel.catalogProducts.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val selectedIngredient by viewModel.selectedIngredient.collectAsStateWithLifecycle()
    val budgetBundle by viewModel.budgetBundle.collectAsStateWithLifecycle()
    val ingredientAnalysis by viewModel.ingredientAnalysis.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(MainDestination.HOME) }
    var currentSubDestination by remember { mutableStateOf(SubDestination.NONE) }

    // Back handling
    BackHandler(enabled = currentSubDestination != SubDestination.NONE || currentDestination != MainDestination.HOME) {
        if (currentSubDestination != SubDestination.NONE) {
            currentSubDestination = SubDestination.NONE
        } else if (currentDestination != MainDestination.HOME) {
            currentDestination = MainDestination.HOME
        }
    }

    // 2. Onboarding Gate: If this Google user has not completed onboarding yet, onboard them
    if (!preferences.isOnboardingDone) {
        OnboardingScreen(
            currentPreferences = preferences,
            onComplete = { completedPrefs ->
                viewModel.finishOnboarding(completedPrefs)
            }
        )
        return
    }

    // 3. User signed in & profile exists -> Direct to dashboard
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                MainDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination && currentSubDestination == SubDestination.NONE
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentDestination = destination
                            currentSubDestination = SubDestination.NONE
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = Pair(currentDestination, currentSubDestination),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(innerPadding)
        ) { (dest, subDest) ->
            when {
                subDest == SubDestination.INGREDIENTS -> {
                    IngredientIntelligenceScreen(
                        ingredients = ingredients,
                        analysisResult = ingredientAnalysis,
                        onDecodeIngredients = { viewModel.decodeIngredients(it) },
                        onClearAnalysis = { viewModel.clearIngredientAnalysis() },
                        onSelectIngredient = { viewModel.selectIngredient(it) }
                    )
                }
                subDest == SubDestination.BUDGET -> {
                    BudgetFinderScreen(
                        currency = preferences.currency,
                        bundleResult = budgetBundle,
                        onCalculateBudget = { b, cats -> viewModel.calculateBudget(b, cats) },
                        onSelectProduct = { viewModel.selectProduct(it) },
                        onSaveToShelf = { viewModel.addProductToShelf(it) }
                    )
                }
                subDest == SubDestination.SHELF -> {
                    BeautyShelfScreen(
                        shelfProducts = shelfProducts,
                        onAddCustomProduct = { name, brand, cat, notes, pao, rout ->
                            viewModel.addCustomToShelf(name, brand, cat, notes, pao, rout)
                        },
                        onDeleteShelfProduct = { viewModel.deleteShelfProduct(it) }
                    )
                }
                subDest == SubDestination.JOURNAL -> {
                    JournalScreen(
                        journalEntries = journalEntries,
                        onAddEntry = { am, pm, comf, hyd, irr, note ->
                            viewModel.logJournalEntry(am, pm, comf, hyd, irr, note)
                        },
                        onDeleteEntry = { viewModel.deleteJournalEntry(it) }
                    )
                }
                subDest == SubDestination.SETTINGS -> {
                    SettingsScreen(
                        preferences = preferences,
                        onUpdatePreferences = { viewModel.updatePreferences(it) },
                        onRetakeConsultation = {
                            viewModel.updatePreferences(preferences.copy(isOnboardingDone = false))
                        },
                        onSignOut = {
                            signOutUser(
                                context = context,
                                credentialManager = credentialManager,
                                onComplete = {
                                    currentUser = null
                                },
                                scope = coroutineScope
                            )
                        }
                    )
                }
                dest == MainDestination.HOME -> {
                    val streakDays = (journalEntries.size + 1).coerceAtLeast(1)
                    HomeScreen(
                        preferences = preferences,
                        routineSteps = routineSteps,
                        recommendedProducts = catalogProducts,
                        streakDays = streakDays,
                        onToggleStep = { viewModel.toggleStepCompleted(it) },
                        onSelectProduct = { viewModel.selectProduct(it) },
                        onSaveToShelf = { viewModel.addProductToShelf(it) },
                        onNavigateToAi = { currentDestination = MainDestination.ASSISTANT },
                        onNavigateToIngredients = { currentSubDestination = SubDestination.INGREDIENTS },
                        onNavigateToBudget = { currentSubDestination = SubDestination.BUDGET },
                        onNavigateToShelf = { currentSubDestination = SubDestination.SHELF },
                        onNavigateToRoutines = { currentDestination = MainDestination.ROUTINES }
                    )
                }
                dest == MainDestination.ROUTINES -> {
                    RoutineBuilderScreen(
                        routineSteps = routineSteps,
                        catalogProducts = catalogProducts,
                        onAddStep = { type, cat, prod, title, purp, tip ->
                            viewModel.addRoutineStep(type, cat, prod, title, purp, tip)
                        },
                        onDeleteStep = { viewModel.deleteRoutineStep(it) },
                        onResetToday = { viewModel.resetDailyRoutine() }
                    )
                }
                dest == MainDestination.CATALOG -> {
                    CatalogScreen(
                        products = catalogProducts,
                        currency = preferences.currency,
                        onFilterChange = { q, c, f -> viewModel.filterCatalog(q, c, f) },
                        onSelectProduct = { viewModel.selectProduct(it) },
                        onSaveToShelf = { viewModel.addProductToShelf(it) }
                    )
                }
                dest == MainDestination.ASSISTANT -> {
                    AssistantScreen(
                        messages = chatMessages,
                        isLoading = isAiLoading,
                        currency = preferences.currency,
                        onSendMessage = { viewModel.sendAiMessage(it) },
                        onSelectProduct = { viewModel.selectProduct(it) },
                        onSaveToShelf = { viewModel.addProductToShelf(it) }
                    )
                }
                dest == MainDestination.MORE -> {
                    MoreHubScreen(
                        preferences = preferences,
                        onNavigateToIngredients = { currentSubDestination = SubDestination.INGREDIENTS },
                        onNavigateToBudget = { currentSubDestination = SubDestination.BUDGET },
                        onNavigateToShelf = { currentSubDestination = SubDestination.SHELF },
                        onNavigateToJournal = { currentSubDestination = SubDestination.JOURNAL },
                        onNavigateToSettings = { currentSubDestination = SubDestination.SETTINGS }
                    )
                }
            }
        }
    }

    // Product Detail Dialog
    selectedProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            currency = preferences.currency,
            onDismiss = { viewModel.selectProduct(null) },
            onAddToRoutine = { routineType ->
                viewModel.addRoutineStep(
                    type = routineType,
                    category = product.category,
                    product = product,
                    customTitle = product.name,
                    purpose = product.description,
                    usageTip = "Apply evenly as part of sequence."
                )
            },
            onSaveToShelf = {
                viewModel.addProductToShelf(product)
            }
        )
    }

    // Ingredient Detail Dialog
    selectedIngredient?.let { ing ->
        IngredientDetailDialog(
            ingredient = ing,
            onDismiss = { viewModel.selectIngredient(null) }
        )
    }
}
