package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.ai.AssistantResponse
import com.example.data.ai.GeminiAssistant
import com.example.data.datasource.SkincareCatalog
import com.example.data.model.BudgetBundle
import com.example.data.model.Ingredient
import com.example.data.model.IngredientAnalysisResult
import com.example.data.model.JournalEntry
import com.example.data.model.Product
import com.example.data.model.RoutineStep
import com.example.data.model.RoutineType
import com.example.data.model.ShelfProduct
import com.example.data.model.UserPreferences
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirestoreSkincareRepository(
    private val db: FirebaseFirestore,
    private val aiAssistant: GeminiAssistant = GeminiAssistant()
) {
    // Secondary constructor requiring context to resolve R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // -------------------------------------------------------------
    // USER PROFILE & PREFERENCES
    // -------------------------------------------------------------
    suspend fun getUserProfile(userId: String): UserPreferences? {
        val path = "users/$userId"
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserPreferences::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    fun observeUserProfile(userId: String): Flow<UserPreferences?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserPreferences::class.java)
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, path)
                }
                emit(null)
            }
    }

    suspend fun saveUserProfile(userId: String, preferences: UserPreferences) {
        val path = "users/$userId"
        val docRef = db.collection("users").document(userId)
        try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update(
                    mapOf(
                        "displayName" to preferences.displayName,
                        "email" to preferences.email,
                        "skinGoals" to preferences.skinGoals,
                        "experienceLevel" to preferences.experienceLevel,
                        "sensitivities" to preferences.sensitivities,
                        "budgetTier" to preferences.budgetTier,
                        "currency" to preferences.currency,
                        "fragrancePreference" to preferences.fragrancePreference,
                        "routineComplexity" to preferences.routineComplexity,
                        "isOnboardingDone" to preferences.isOnboardingDone,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = hashMapOf<String, Any>(
                    "userId" to userId,
                    "displayName" to preferences.displayName,
                    "email" to preferences.email,
                    "skinGoals" to preferences.skinGoals,
                    "experienceLevel" to preferences.experienceLevel,
                    "sensitivities" to preferences.sensitivities,
                    "budgetTier" to preferences.budgetTier,
                    "currency" to preferences.currency,
                    "fragrancePreference" to preferences.fragrancePreference,
                    "routineComplexity" to preferences.routineComplexity,
                    "isOnboardingDone" to preferences.isOnboardingDone,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // ROUTINE STEPS
    // -------------------------------------------------------------
    fun observeRoutineSteps(userId: String): Flow<List<RoutineStep>> {
        val path = "users/$userId/routine_steps"
        return db.collection("users").document(userId)
            .collection("routine_steps")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(RoutineStep::class.java).sortedBy { it.stepNumber }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                emit(emptyList())
            }
    }

    suspend fun saveRoutineStep(userId: String, step: RoutineStep) {
        val stepId = if (step.id.isNotBlank()) step.id else "step_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val path = "users/$userId/routine_steps/$stepId"
        val docRef = db.collection("users").document(userId).collection("routine_steps").document(stepId)
        try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update(
                    mapOf(
                        "routineType" to step.routineType.name,
                        "stepNumber" to step.stepNumber,
                        "category" to step.category,
                        "customTitle" to (step.customTitle ?: step.category),
                        "purpose" to step.purpose,
                        "usageTip" to step.usageTip,
                        "isCompletedToday" to step.isCompletedToday,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = hashMapOf<String, Any>(
                    "userId" to userId,
                    "routineType" to step.routineType.name,
                    "stepNumber" to step.stepNumber,
                    "category" to step.category,
                    "customTitle" to (step.customTitle ?: step.category),
                    "purpose" to step.purpose,
                    "usageTip" to step.usageTip,
                    "isCompletedToday" to step.isCompletedToday,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun toggleStepCompleted(userId: String, stepId: String, currentCompleted: Boolean) {
        val path = "users/$userId/routine_steps/$stepId"
        try {
            db.collection("users").document(userId)
                .collection("routine_steps").document(stepId)
                .update(
                    mapOf(
                        "isCompletedToday" to !currentCompleted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun deleteRoutineStep(userId: String, stepId: String) {
        val path = "users/$userId/routine_steps/$stepId"
        try {
            db.collection("users").document(userId)
                .collection("routine_steps").document(stepId)
                .delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    suspend fun resetTodaySteps(userId: String) {
        val path = "users/$userId/routine_steps"
        try {
            val snapshot = db.collection("users").document(userId)
                .collection("routine_steps").get().await()
            for (doc in snapshot.documents) {
                doc.reference.update(
                    mapOf(
                        "isCompletedToday" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun seedDefaultRoutines(userId: String, skinGoals: List<String>) {
        val amSteps = listOf(
            RoutineStep(
                id = "am_cleanser",
                userId = userId,
                routineType = RoutineType.MORNING,
                stepNumber = 1,
                category = "Cleanser",
                customTitle = "Gentle Hydrating Cleanser",
                purpose = "Gently cleanse impurities without disrupting moisture barrier.",
                usageTip = "Massage onto damp skin for 45-60 seconds and rinse with lukewarm water."
            ),
            RoutineStep(
                id = "am_serum",
                userId = userId,
                routineType = RoutineType.MORNING,
                stepNumber = 2,
                category = "Serum",
                customTitle = if (skinGoals.any { it.contains("Brightening", ignoreCase = true) }) "Vitamin C Glow Serum" else "Hyaluronic Acid Hydrating Serum",
                purpose = "Target antioxidant defense and plump the outer skin layers.",
                usageTip = "Pat 3-4 drops gently over face and neck before moisturizer."
            ),
            RoutineStep(
                id = "am_moisturizer",
                userId = userId,
                routineType = RoutineType.MORNING,
                stepNumber = 3,
                category = "Moisturizer",
                customTitle = "Ceramide Barrier Cream",
                purpose = "Seal in hydration and fortify delicate barrier lipids.",
                usageTip = "Apply a nickel-sized amount smoothly across face and neck."
            ),
            RoutineStep(
                id = "am_sunscreen",
                userId = userId,
                routineType = RoutineType.MORNING,
                stepNumber = 4,
                category = "Sunscreen",
                customTitle = "Daily UV Fluid SPF 50+",
                purpose = "Essential shield against photoaging, hyperpigmentation, and collagen breakdown.",
                usageTip = "Apply two finger-lengths as the last morning step 15 min before sun exposure."
            )
        )

        val pmSteps = listOf(
            RoutineStep(
                id = "pm_cleanser",
                userId = userId,
                routineType = RoutineType.EVENING,
                stepNumber = 1,
                category = "Cleanser",
                customTitle = "Purifying Melt Cleanser",
                purpose = "Dissolve sunscreen, makeup, and daily environmental pollutants.",
                usageTip = "Apply to dry face first to break down oils, then emulsify with water."
            ),
            RoutineStep(
                id = "pm_serum",
                userId = userId,
                routineType = RoutineType.EVENING,
                stepNumber = 2,
                category = "Serum",
                customTitle = if (skinGoals.any { it.contains("Barrier", ignoreCase = true) }) "Barrier Repair Peptide Serum" else "Niacinamide Balance Serum",
                purpose = "Soothes overnight cell regeneration and balances texture.",
                usageTip = "Smooth 4 drops onto cleansed, slightly damp skin."
            ),
            RoutineStep(
                id = "pm_moisturizer",
                userId = userId,
                routineType = RoutineType.EVENING,
                stepNumber = 3,
                category = "Moisturizer",
                customTitle = "Overnight Recovery Cream",
                purpose = "Deeply replenish moisture lost throughout the day.",
                usageTip = "Press gently into skin and let absorb before sleep."
            )
        )

        for (step in (amSteps + pmSteps)) {
            saveRoutineStep(userId, step)
        }
    }

    // -------------------------------------------------------------
    // BEAUTY SHELF
    // -------------------------------------------------------------
    fun observeShelfProducts(userId: String): Flow<List<ShelfProduct>> {
        val path = "users/$userId/shelf_products"
        return db.collection("users").document(userId)
            .collection("shelf_products")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ShelfProduct::class.java)
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                emit(emptyList())
            }
    }

    suspend fun saveShelfProduct(userId: String, product: ShelfProduct) {
        val productId = if (product.id.isNotBlank()) product.id else "shelf_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val path = "users/$userId/shelf_products/$productId"
        val docRef = db.collection("users").document(userId).collection("shelf_products").document(productId)
        try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update(
                    mapOf(
                        "name" to product.name,
                        "brand" to product.brand,
                        "category" to product.category,
                        "notes" to product.notes,
                        "paoMonths" to product.paoMonths,
                        "routineAssignment" to product.routineAssignment,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = hashMapOf<String, Any>(
                    "userId" to userId,
                    "name" to product.name,
                    "brand" to product.brand,
                    "category" to product.category,
                    "notes" to product.notes,
                    "paoMonths" to product.paoMonths,
                    "routineAssignment" to product.routineAssignment,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun deleteShelfProduct(userId: String, productId: String) {
        val path = "users/$userId/shelf_products/$productId"
        try {
            db.collection("users").document(userId)
                .collection("shelf_products").document(productId)
                .delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // JOURNAL ENTRIES
    // -------------------------------------------------------------
    fun observeJournalEntries(userId: String): Flow<List<JournalEntry>> {
        val path = "users/$userId/journal_entries"
        return db.collection("users").document(userId)
            .collection("journal_entries")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(JournalEntry::class.java).sortedByDescending { it.dateString }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                emit(emptyList())
            }
    }

    suspend fun saveJournalEntry(userId: String, entry: JournalEntry) {
        val entryId = if (entry.id.isNotBlank()) entry.id else "journal_${entry.dateString}"
        val path = "users/$userId/journal_entries/$entryId"
        val docRef = db.collection("users").document(userId).collection("journal_entries").document(entryId)
        try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update(
                    mapOf(
                        "dateString" to entry.dateString,
                        "morningCompleted" to entry.morningCompleted,
                        "eveningCompleted" to entry.eveningCompleted,
                        "skinComfort" to entry.skinComfort,
                        "hydrationLevel" to entry.hydrationLevel,
                        "irritationLevel" to entry.irritationLevel,
                        "breakoutReported" to entry.breakoutReported,
                        "notes" to entry.notes,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = hashMapOf<String, Any>(
                    "userId" to userId,
                    "dateString" to entry.dateString,
                    "morningCompleted" to entry.morningCompleted,
                    "eveningCompleted" to entry.eveningCompleted,
                    "skinComfort" to entry.skinComfort,
                    "hydrationLevel" to entry.hydrationLevel,
                    "irritationLevel" to entry.irritationLevel,
                    "breakoutReported" to entry.breakoutReported,
                    "notes" to entry.notes,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun deleteJournalEntry(userId: String, entryId: String) {
        val path = "users/$userId/journal_entries/$entryId"
        try {
            db.collection("users").document(userId)
                .collection("journal_entries").document(entryId)
                .delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // CATALOG & AI COMPANION
    // -------------------------------------------------------------
    fun getAllCatalogProducts(): List<Product> = SkincareCatalog.products
    fun getAllIngredients(): List<Ingredient> = SkincareCatalog.ingredients

    fun filterProducts(
        query: String = "",
        category: String = "All",
        maxPriceUsd: Double = Double.MAX_VALUE,
        fragranceFreeOnly: Boolean = false
    ): List<Product> {
        val q = query.trim().lowercase()
        return SkincareCatalog.products.filter { prod ->
            val matchesCategory = (category == "All" || prod.category.equals(category, ignoreCase = true))
            val matchesPrice = prod.priceUsd <= maxPriceUsd
            val matchesFragrance = !fragranceFreeOnly || prod.isFragranceFree
            val matchesQuery = q.isEmpty() ||
                    prod.name.lowercase().contains(q) ||
                    prod.brand.lowercase().contains(q) ||
                    prod.category.lowercase().contains(q) ||
                    prod.keyActives.any { it.lowercase().contains(q) } ||
                    prod.suitability.lowercase().contains(q)

            matchesCategory && matchesPrice && matchesFragrance && matchesQuery
        }
    }


    fun decodeIngredientList(rawText: String): IngredientAnalysisResult {
        if (rawText.isBlank()) {
            return IngredientAnalysisResult(emptyList(), emptyList(), 0, "No ingredients entered.")
        }

        val tokens = rawText.split(",", ";", "\n").map { it.trim() }.filter { it.length > 2 }
        val foundIngredients = mutableListOf<Ingredient>()
        val sensitizers = mutableListOf<String>()

        val knownIrritants = listOf(
            "fragrance" to "Added Fragrance (Parfum) - potential sensitizer for reactive skin",
            "parfum" to "Parfum - synthetic or essential oil fragrance",
            "alcohol denat" to "Alcohol Denat - drying solvent; may compromise skin barrier if high on list",
            "essential oil" to "Essential Oils - contains volatile terpenes that may cause contact allergy",
            "lavender oil" to "Lavender Oil - phototoxic and sensitizing terpene",
            "eucalyptus" to "Eucalyptus Oil - potential skin irritant",
            "citrus" to "Citrus Extracts - can cause photosensitivity",
            "limonene" to "Limonene - fragrance allergen",
            "linalool" to "Linalool - fragrance allergen",
            "sodium lauryl sulfate" to "Sodium Lauryl Sulfate (SLS) - harsh anionic surfactant"
        )

        for (token in tokens) {
            val lowerToken = token.lowercase()
            for (known in SkincareCatalog.ingredients) {
                val base = known.name.lowercase().substringBefore("(").trim()
                val isMatch = lowerToken.contains(base) || base.contains(lowerToken) ||
                        (lowerToken.contains("ceramide") && base.contains("ceramide")) ||
                        (lowerToken.contains("hyaluron") && base.contains("hyaluronic")) ||
                        (lowerToken.contains("retin") && base.contains("retin")) ||
                        (lowerToken.contains("niacin") && base.contains("niacin")) ||
                        (lowerToken.contains("salicylic") && base.contains("salicylic")) ||
                        (lowerToken.contains("centella") && base.contains("centella"))
                if (isMatch && !foundIngredients.contains(known)) {
                    foundIngredients.add(known)
                }
            }

            for ((irritantKey, warning) in knownIrritants) {
                if (lowerToken.contains(irritantKey) && !sensitizers.contains(warning)) {
                    sensitizers.add(warning)
                }
            }
        }

        val note = if (sensitizers.isEmpty() && foundIngredients.isNotEmpty()) {
            "Formulation appears gentle with ${foundIngredients.size} beneficial active ingredients identified."
        } else if (sensitizers.isNotEmpty()) {
            "Identified ${foundIngredients.size} hero actives, with ${sensitizers.size} potential sensitizers/allergens detected."
        } else {
            "Parsed ${tokens.size} ingredients. Review individual component purposes below."
        }

        return IngredientAnalysisResult(
            matchedIngredients = foundIngredients,
            flaggedSensitizers = sensitizers,
            totalIdentifiedCount = foundIngredients.size,
            summaryNote = note
        )
    }


    fun calculateBudgetBundle(
        targetBudget: Double,
        currency: String,
        selectedCategories: List<String>
    ): BudgetBundle? {
        if (selectedCategories.isEmpty()) return null

        val availableProducts = SkincareCatalog.products
        val chosenProducts = mutableListOf<Product>()

        for (cat in selectedCategories) {
            val catProducts = availableProducts.filter { it.category.equals(cat, ignoreCase = true) }
                .sortedBy { it.getPrice(currency) }

            val match = catProducts.firstOrNull()
            if (match != null) {
                chosenProducts.add(match)
            }
        }

        val totalPrice = chosenProducts.sumOf { it.getPrice(currency) }
        val savings = targetBudget - totalPrice

        val alternatives = availableProducts
            .filter { prod -> selectedCategories.any { it.equals(prod.category, ignoreCase = true) } }
            .filter { !chosenProducts.contains(it) }
            .sortedBy { it.getPrice(currency) }
            .take(3)

        val reason = if (savings >= 0) {
            "This bundle provides a complete essential routine combining gentle barrier support, active treatment, and daily protection within your budget."
        } else {
            "The selected categories require a slightly higher budget for verified products. Consider simplifying steps or exploring affordable single-category alternatives."
        }

        return BudgetBundle(
            products = chosenProducts,
            totalPrice = totalPrice,
            budget = targetBudget,
            currency = currency,
            savings = if (savings > 0) savings else 0.0,
            reason = reason,
            alternativeProducts = alternatives
        )
    }

    suspend fun queryAiAssistant(
        query: String,
        preferences: UserPreferences,
        shelfList: List<ShelfProduct>,
        routineList: List<RoutineStep>
    ): AssistantResponse {
        val shelfSummary = if (shelfList.isEmpty()) "Empty shelf" else {
            shelfList.joinToString("; ") { "${it.name} (${it.brand}, ${it.category})" }
        }
        val routineSummary = if (routineList.isEmpty()) "Standard routine" else {
            routineList.joinToString("; ") { "${it.routineType}: Step ${it.stepNumber} - ${it.category}: ${it.customTitle ?: it.productId}" }
        }

        return aiAssistant.askAssistant(
            userQuery = query,
            userPreferences = preferences,
            shelfProductsSummary = shelfSummary,
            routineSummary = routineSummary
        )
    }
}
