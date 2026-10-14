package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datasource.SkincareCatalog
import com.example.data.model.BudgetBundle
import com.example.data.model.ChatMessage
import com.example.data.model.Ingredient
import com.example.data.model.IngredientAnalysisResult
import com.example.data.model.JournalEntry
import com.example.data.model.Product
import com.example.data.model.RoutineStep
import com.example.data.model.RoutineType
import com.example.data.model.ShelfProduct
import com.example.data.model.UserPreferences
import com.example.data.repository.FirestoreSkincareRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SkinRoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirestoreSkincareRepository(application)
    private val auth: FirebaseAuth = Firebase.auth

    // Real-time auth state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    // Reactive Firestore flows bound to current authenticated user
    val preferences: StateFlow<UserPreferences> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.observeUserProfile(user.uid)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    ).let { baseFlow ->
        // Ensure non-null fallback
        MutableStateFlow(UserPreferences()).apply {
            viewModelScope.launch {
                baseFlow.collect { profile ->
                    value = profile ?: UserPreferences()
                }
            }
        }
    }

    val shelfProducts: StateFlow<List<ShelfProduct>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.observeShelfProducts(user.uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val routineSteps: StateFlow<List<RoutineStep>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.observeRoutineSteps(user.uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val journalEntries: StateFlow<List<JournalEntry>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.observeJournalEntries(user.uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _catalogProducts = MutableStateFlow<List<Product>>(repository.getAllCatalogProducts())
    val catalogProducts: StateFlow<List<Product>> = _catalogProducts.asStateFlow()

    private val _ingredients = MutableStateFlow<List<Ingredient>>(repository.getAllIngredients())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome_msg",
                text = "Welcome to SkinRoutine AI! I'm your beauty and skincare companion. I can analyze ingredients, compare catalog formulas, help design safe routines, and recommend products matching your budget.\n\nHow can I help your skin today?",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _selectedIngredient = MutableStateFlow<Ingredient?>(null)
    val selectedIngredient: StateFlow<Ingredient?> = _selectedIngredient.asStateFlow()

    private val _budgetBundle = MutableStateFlow<BudgetBundle?>(null)
    val budgetBundle: StateFlow<BudgetBundle?> = _budgetBundle.asStateFlow()

    private val _ingredientAnalysis = MutableStateFlow<IngredientAnalysisResult?>(null)
    val ingredientAnalysis: StateFlow<IngredientAnalysisResult?> = _ingredientAnalysis.asStateFlow()

    // -----------------------------------------------------------------
    // ACTIONS
    // -----------------------------------------------------------------
    fun finishOnboarding(updatedPreferences: UserPreferences) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val toSave = updatedPreferences.copy(
                userId = user.uid,
                displayName = user.displayName ?: "Beauty Enthusiast",
                email = user.email ?: "",
                isOnboardingDone = true
            )
            repository.saveUserProfile(user.uid, toSave)
            repository.seedDefaultRoutines(user.uid, toSave.skinGoals)
        }
    }

    fun updatePreferences(updatedPreferences: UserPreferences) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.saveUserProfile(user.uid, updatedPreferences.copy(userId = user.uid))
        }
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun selectIngredient(ingredient: Ingredient?) {
        _selectedIngredient.value = ingredient
    }

    fun filterCatalog(query: String = "", category: String = "All", fragranceFreeOnly: Boolean = false) {
        _catalogProducts.value = repository.filterProducts(
            query = query,
            category = category,
            fragranceFreeOnly = fragranceFreeOnly
        )
    }

    fun toggleStepCompleted(step: RoutineStep) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleStepCompleted(user.uid, step.id, step.isCompletedToday)
        }
    }

    fun resetDailyRoutine() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.resetTodaySteps(user.uid)
        }
    }

    fun addRoutineStep(
        type: RoutineType,
        category: String,
        product: Product?,
        customTitle: String?,
        purpose: String,
        usageTip: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val currentSteps = routineSteps.value.filter { it.routineType == type }
            val nextNumber = (currentSteps.maxOfOrNull { it.stepNumber } ?: 0) + 1
            repository.saveRoutineStep(
                userId = user.uid,
                step = RoutineStep(
                    id = "step_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                    userId = user.uid,
                    routineType = type,
                    stepNumber = nextNumber,
                    category = category,
                    productId = product?.id,
                    customTitle = customTitle ?: product?.name ?: "$category Step",
                    purpose = purpose.ifEmpty { product?.description ?: "Nourish and protect skin" },
                    usageTip = usageTip.ifEmpty { "Apply as part of your regular sequence." },
                    isCompletedToday = false
                )
            )
        }
    }

    fun deleteRoutineStep(stepId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteRoutineStep(user.uid, stepId)
        }
    }

    fun addProductToShelf(product: Product, routineTime: String = "Both") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val today = dateFormat.format(Date())
            repository.saveShelfProduct(
                userId = user.uid,
                product = ShelfProduct(
                    id = "shelf_${product.id}_${System.currentTimeMillis()}",
                    userId = user.uid,
                    productId = product.id,
                    name = product.name,
                    brand = product.brand,
                    category = product.category,
                    openedDate = today,
                    notes = "Added from catalog",
                    paoMonths = product.paoMonths,
                    routineAssignment = routineTime,
                    routineTime = routineTime
                )
            )
        }
    }

    fun addCustomToShelf(
        name: String,
        brand: String,
        category: String,
        notes: String,
        paoMonths: Int,
        routineTime: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val today = dateFormat.format(Date())
            repository.saveShelfProduct(
                userId = user.uid,
                product = ShelfProduct(
                    id = "custom_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                    userId = user.uid,
                    productId = null,
                    name = name,
                    brand = brand,
                    category = category,
                    openedDate = today,
                    notes = notes,
                    paoMonths = paoMonths,
                    routineAssignment = routineTime,
                    routineTime = routineTime
                )
            )
        }
    }

    fun deleteShelfProduct(id: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteShelfProduct(user.uid, id)
        }
    }

    fun logJournalEntry(
        morningDone: Boolean,
        eveningDone: Boolean,
        comfort: Int,
        hydration: Int,
        irritation: Int,
        notes: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            val todayStr = dateFormat.format(Date())
            repository.saveJournalEntry(
                userId = user.uid,
                entry = JournalEntry(
                    id = "journal_${todayStr.replace(" ", "_").replace(",", "")}",
                    userId = user.uid,
                    dateString = todayStr,
                    timestamp = System.currentTimeMillis(),
                    morningCompleted = morningDone,
                    eveningCompleted = eveningDone,
                    skinComfort = comfort,
                    comfortScore = comfort,
                    hydrationLevel = hydration,
                    hydrationScore = hydration,
                    irritationLevel = irritation,
                    irritationScore = irritation,
                    notes = notes
                )
            )
        }
    }

    fun deleteJournalEntry(id: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteJournalEntry(user.uid, id)
        }
    }

    fun decodeIngredients(text: String) {
        _ingredientAnalysis.value = repository.decodeIngredientList(text)
    }

    fun clearIngredientAnalysis() {
        _ingredientAnalysis.value = null
    }

    fun calculateBudget(targetBudget: Double, categories: List<String>) {
        val curr = preferences.value.currency
        _budgetBundle.value = repository.calculateBudgetBundle(targetBudget, curr, categories)
    }

    fun clearBudgetBundle() {
        _budgetBundle.value = null
    }

    fun sendAiMessage(promptText: String) {
        if (promptText.isBlank() || _isAiLoading.value) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = promptText.trim(),
            isUser = true
        )
        _chatMessages.value = _chatMessages.value + userMessage
        _isAiLoading.value = true

        viewModelScope.launch {
            val response = repository.queryAiAssistant(
                query = promptText,
                preferences = preferences.value,
                shelfList = shelfProducts.value,
                routineList = routineSteps.value
            )

            val aiMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = response.text,
                isUser = false,
                suggestedProducts = response.suggestedProducts
            )

            _chatMessages.value = _chatMessages.value + aiMessage
            _isAiLoading.value = false
        }
    }
}
