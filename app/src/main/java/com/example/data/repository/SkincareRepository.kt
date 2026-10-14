package com.example.data.repository

import com.example.data.ai.AssistantResponse
import com.example.data.ai.GeminiAssistant
import com.example.data.datasource.SkincareCatalog
import com.example.data.local.dao.SkinRoutineDao
import com.example.data.local.entity.JournalEntryEntity
import com.example.data.local.entity.PreferencesEntity
import com.example.data.local.entity.RoutineStepEntity
import com.example.data.local.entity.ShelfEntity
import com.example.data.model.BudgetBundle
import com.example.data.model.Ingredient
import com.example.data.model.IngredientAnalysisResult
import com.example.data.model.JournalEntry
import com.example.data.model.Product
import com.example.data.model.RoutineStep
import com.example.data.model.RoutineType
import com.example.data.model.ShelfProduct
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SkincareRepository(
    private val dao: SkinRoutineDao,
    private val aiAssistant: GeminiAssistant = GeminiAssistant()
) {

    // --- User Preferences ---
    val preferencesFlow: Flow<UserPreferences> = dao.getPreferences().map { entity ->
        if (entity == null) {
            UserPreferences()
        } else {
            UserPreferences(
                skinGoals = if (entity.skinGoals.isBlank()) emptyList() else entity.skinGoals.split(","),
                experienceLevel = entity.experienceLevel,
                currentRoutineLevel = entity.currentRoutineLevel.ifBlank { "Basic (Cleanser & Moisturizer)" },
                existingProductsOwned = if (entity.existingProductsOwned.isBlank()) emptyList() else entity.existingProductsOwned.split(","),
                sensitivities = if (entity.sensitivities.isBlank()) emptyList() else entity.sensitivities.split(","),
                fragrancePreference = entity.fragrancePreference,
                productTexturePreference = entity.productTexturePreference.ifBlank { "Lightweight Gels & Fluids" },
                routineComplexity = entity.routineComplexity,
                budget = entity.budget,
                currency = entity.currency,
                isOnboardingDone = entity.isOnboardingDone
            )
        }
    }

    suspend fun savePreferences(preferences: UserPreferences) {
        dao.insertPreferences(
            PreferencesEntity(
                id = 1,
                skinGoals = preferences.skinGoals.joinToString(","),
                experienceLevel = preferences.experienceLevel,
                currentRoutineLevel = preferences.currentRoutineLevel,
                existingProductsOwned = preferences.existingProductsOwned.joinToString(","),
                sensitivities = preferences.sensitivities.joinToString(","),
                fragrancePreference = preferences.fragrancePreference,
                productTexturePreference = preferences.productTexturePreference,
                routineComplexity = preferences.routineComplexity,
                budget = preferences.budget,
                currency = preferences.currency,
                isOnboardingDone = preferences.isOnboardingDone
            )
        )
    }

    // --- Beauty Shelf ---
    val shelfProductsFlow: Flow<List<ShelfProduct>> = dao.getAllShelfProducts().map { list ->
        list.map { entity ->
            ShelfProduct(
                id = entity.id,
                productId = entity.productId,
                name = entity.name,
                brand = entity.brand,
                category = entity.category,
                openedDate = entity.openedDate,
                notes = entity.notes,
                paoMonths = entity.paoMonths,
                routineTime = entity.routineTime
            )
        }
    }

    suspend fun addShelfProduct(product: ShelfProduct) {
        dao.insertShelfProduct(
            ShelfEntity(
                id = product.id.ifEmpty { UUID.randomUUID().toString() },
                productId = product.productId,
                name = product.name,
                brand = product.brand,
                category = product.category,
                openedDate = product.openedDate,
                notes = product.notes,
                paoMonths = product.paoMonths,
                routineTime = product.routineTime
            )
        )
    }

    suspend fun addCatalogProductToShelf(product: Product, routineTime: String = "Both") {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val today = dateFormat.format(Date())
        dao.insertShelfProduct(
            ShelfEntity(
                id = UUID.randomUUID().toString(),
                productId = product.id,
                name = product.name,
                brand = product.brand,
                category = product.category,
                openedDate = today,
                notes = "Saved from SkinRoutine catalog (${product.suitability})",
                paoMonths = product.paoMonths,
                routineTime = routineTime
            )
        )
    }

    suspend fun deleteShelfProduct(id: String) {
        dao.deleteShelfProduct(id)
    }

    // --- Routines (Morning & Evening) ---
    val routineStepsFlow: Flow<List<RoutineStep>> = dao.getAllRoutineSteps().map { list ->
        if (list.isEmpty()) {
            emptyList()
        } else {
            list.map { entity ->
                val type = try {
                    RoutineType.valueOf(entity.routineType)
                } catch (e: Exception) {
                    RoutineType.MORNING
                }
                RoutineStep(
                    id = entity.id,
                    routineType = type,
                    stepNumber = entity.stepNumber,
                    category = entity.category,
                    productId = entity.productId,
                    customTitle = entity.customTitle,
                    purpose = entity.purpose,
                    usageTip = entity.usageTip,
                    isCompletedToday = entity.isCompletedToday
                )
            }
        }
    }

    suspend fun initializeDefaultRoutinesIfNeeded(userPreferences: UserPreferences) {
        val existing = dao.getPreferencesDirect()
        // Check if steps already exist
        // If empty, create standard science-based routines
        val isMinimal = userPreferences.routineComplexity.contains("Minimal", ignoreCase = true)
        val isComprehensive = userPreferences.routineComplexity.contains("Comprehensive", ignoreCase = true)

        val morningSteps = if (isMinimal) {
            listOf(
                RoutineStepEntity(
                    id = "step_am_1",
                    routineType = "MORNING",
                    stepNumber = 1,
                    category = "Cleanser",
                    productId = "prod_cerave_cleanser",
                    customTitle = "Hydrating Facial Cleanser",
                    purpose = "Gentle barrier wash to remove overnight sebum",
                    usageTip = "Use lukewarm water. Massage for 30-45 seconds without aggressive scrubbing.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_am_2",
                    routineType = "MORNING",
                    stepNumber = 2,
                    category = "Moisturizer",
                    productId = "prod_neutrogena_hydroboost",
                    customTitle = "Hydro Boost Water Gel",
                    purpose = "Lightweight lipid seal against daytime environmental moisture loss",
                    usageTip = "Evenly distribute a nickel-sized amount over face and neck.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_am_3",
                    routineType = "MORNING",
                    stepNumber = 3,
                    category = "Sunscreen",
                    productId = "prod_boj_sunscreen",
                    customTitle = "Relief Sun : Rice + Probiotics SPF50+",
                    purpose = "Broad-spectrum UV protection to prevent premature aging and hyperpigmentation",
                    usageTip = "Use two finger-lengths for face and ears. Apply 15 minutes before sun exposure.",
                    isCompletedToday = false
                )
            )
        } else {
            listOf(
                RoutineStepEntity(
                    id = "step_am_1",
                    routineType = "MORNING",
                    stepNumber = 1,
                    category = "Cleanser",
                    productId = "prod_cerave_cleanser",
                    customTitle = "Hydrating Facial Cleanser",
                    purpose = "Gentle barrier wash to remove overnight sebum",
                    usageTip = "Use lukewarm water. Massage for 30-45 seconds without aggressive scrubbing.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_am_2",
                    routineType = "MORNING",
                    stepNumber = 2,
                    category = "Toner",
                    productId = "prod_klairs_unscented",
                    customTitle = "Supple Preparation Unscented Toner",
                    purpose = "Rehydrate stratum corneum and balance skin pH",
                    usageTip = "Pat gently into face and neck while skin is still damp.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_am_3",
                    routineType = "MORNING",
                    stepNumber = 3,
                    category = "Serum",
                    productId = "prod_ordinary_ha",
                    customTitle = "Hyaluronic Acid 2% + B5",
                    purpose = "Deep cellular hydration & barrier plumping",
                    usageTip = "Apply 2-3 drops. Follow immediately with moisturizer to lock in hydration.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_am_4",
                    routineType = "MORNING",
                    stepNumber = 4,
                    category = "Moisturizer",
                    productId = "prod_neutrogena_hydroboost",
                    customTitle = "Hydro Boost Water Gel",
                    purpose = "Lightweight lipid seal against daytime environmental moisture loss",
                    usageTip = "Evenly distribute a nickel-sized amount over face and neck.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_am_5",
                    routineType = "MORNING",
                    stepNumber = 5,
                    category = "Sunscreen",
                    productId = "prod_boj_sunscreen",
                    customTitle = "Relief Sun : Rice + Probiotics SPF50+",
                    purpose = "Broad-spectrum UV protection to prevent premature aging and hyperpigmentation",
                    usageTip = "Use two finger-lengths for face and ears. Apply 15 minutes before sun exposure.",
                    isCompletedToday = false
                )
            )
        }

        val eveningSteps = if (isMinimal) {
            listOf(
                RoutineStepEntity(
                    id = "step_pm_1",
                    routineType = "EVENING",
                    stepNumber = 1,
                    category = "Cleanser",
                    productId = "prod_bioderma_sensibio",
                    customTitle = "Sensibio H2O Micellar Water & Wash",
                    purpose = "Dissolve sunscreen, airborne particulate pollutants, and daily grime",
                    usageTip = "Saturate cotton pad, press gently for 5 seconds over eyes, then rinse or follow with gel cleanser.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_pm_2",
                    routineType = "EVENING",
                    stepNumber = 2,
                    category = "Moisturizer",
                    productId = "prod_cerave_pm",
                    customTitle = "PM Facial Moisturizing Lotion",
                    purpose = "Sustained ceramide and niacinamide delivery for nighttime barrier replenishment",
                    usageTip = "Apply generously. For extra dry nights, layer a barrier balm like Cicaplast.",
                    isCompletedToday = false
                )
            )
        } else {
            listOf(
                RoutineStepEntity(
                    id = "step_pm_1",
                    routineType = "EVENING",
                    stepNumber = 1,
                    category = "Cleanser",
                    productId = "prod_bioderma_sensibio",
                    customTitle = "Sensibio H2O Micellar Water & Wash",
                    purpose = "Dissolve sunscreen, airborne particulate pollutants, and daily grime",
                    usageTip = "Saturate cotton pad, press gently for 5 seconds over eyes, then rinse or follow with gel cleanser.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_pm_2",
                    routineType = "EVENING",
                    stepNumber = 2,
                    category = "Serum",
                    productId = "prod_cosrx_snail",
                    customTitle = "Advanced Snail 96 Mucin Essence",
                    purpose = "Nourish cellular repair and soothe daytime micro-inflammation",
                    usageTip = "Pump 1-2 times and pat into damp skin until fully absorbed.",
                    isCompletedToday = false
                ),
                RoutineStepEntity(
                    id = "step_pm_3",
                    routineType = "EVENING",
                    stepNumber = 3,
                    category = "Moisturizer",
                    productId = "prod_cerave_pm",
                    customTitle = "PM Facial Moisturizing Lotion",
                    purpose = "Sustained ceramide and niacinamide delivery for nighttime barrier replenishment",
                    usageTip = "Apply generously. For extra dry nights, layer a barrier balm like Cicaplast.",
                    isCompletedToday = false
                )
            )
        }

        dao.insertRoutineSteps(morningSteps + eveningSteps)

        // Pre-populate Beauty Shelf with user's selected existing owned products
        if (userPreferences.existingProductsOwned.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val today = dateFormat.format(Date())
            for (owned in userPreferences.existingProductsOwned) {
                val matched = SkincareCatalog.products.firstOrNull {
                    it.category.contains(owned, ignoreCase = true) || owned.contains(it.category, ignoreCase = true)
                }
                if (matched != null) {
                    dao.insertShelfProduct(
                        ShelfEntity(
                            id = UUID.randomUUID().toString(),
                            productId = matched.id,
                            name = matched.name,
                            brand = matched.brand,
                            category = matched.category,
                            openedDate = today,
                            notes = "Imported from your initial vanity inventory during consultation",
                            paoMonths = matched.paoMonths,
                            routineTime = "Both"
                        )
                    )
                }
            }
        }
    }

    suspend fun addRoutineStep(step: RoutineStep) {
        dao.insertRoutineStep(
            RoutineStepEntity(
                id = step.id.ifEmpty { UUID.randomUUID().toString() },
                routineType = step.routineType.name,
                stepNumber = step.stepNumber,
                category = step.category,
                productId = step.productId,
                customTitle = step.customTitle,
                purpose = step.purpose,
                usageTip = step.usageTip,
                isCompletedToday = step.isCompletedToday
            )
        )
    }

    suspend fun updateRoutineStep(step: RoutineStep) {
        dao.updateRoutineStep(
            RoutineStepEntity(
                id = step.id,
                routineType = step.routineType.name,
                stepNumber = step.stepNumber,
                category = step.category,
                productId = step.productId,
                customTitle = step.customTitle,
                purpose = step.purpose,
                usageTip = step.usageTip,
                isCompletedToday = step.isCompletedToday
            )
        )
    }

    suspend fun deleteRoutineStep(id: String) {
        dao.deleteRoutineStep(id)
    }

    suspend fun toggleStepCompleted(id: String, completed: Boolean) {
        dao.setStepCompleted(id, completed)
    }

    suspend fun resetAllStepsCompleted() {
        dao.resetAllStepsCompleted()
    }

    // --- Journal Entries ---
    val journalEntriesFlow: Flow<List<JournalEntry>> = dao.getAllJournalEntries().map { list ->
        list.map { entity ->
            JournalEntry(
                id = entity.id,
                dateString = entity.dateString,
                timestamp = entity.timestamp,
                morningCompleted = entity.morningCompleted,
                eveningCompleted = entity.eveningCompleted,
                comfortScore = entity.comfortScore,
                hydrationScore = entity.hydrationScore,
                irritationScore = entity.irritationScore,
                notes = entity.notes
            )
        }
    }

    suspend fun logJournalEntry(entry: JournalEntry) {
        dao.insertJournalEntry(
            JournalEntryEntity(
                id = entry.id.ifEmpty { UUID.randomUUID().toString() },
                dateString = entry.dateString,
                timestamp = entry.timestamp,
                morningCompleted = entry.morningCompleted,
                eveningCompleted = entry.eveningCompleted,
                comfortScore = entry.comfortScore,
                hydrationScore = entry.hydrationScore,
                irritationScore = entry.irritationScore,
                notes = entry.notes
            )
        )
    }

    suspend fun deleteJournalEntry(id: String) {
        dao.deleteJournalEntry(id)
    }

    // --- Catalog & Search ---
    fun getAllProducts(): List<Product> = SkincareCatalog.products

    fun getProductById(id: String): Product? = SkincareCatalog.products.find { it.id == id }

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

    // --- Ingredient Intelligence & Decoder ---
    fun getAllIngredients(): List<Ingredient> = SkincareCatalog.ingredients

    fun findIngredientByName(name: String): Ingredient? {
        val n = name.trim().lowercase()
        return SkincareCatalog.ingredients.find {
            it.name.lowercase().contains(n) || n.contains(it.name.lowercase())
        }
    }

    fun decodeIngredientList(rawText: String): IngredientAnalysisResult {
        if (rawText.isBlank()) {
            return IngredientAnalysisResult(emptyList(), emptyList(), 0, "No ingredients entered.")
        }

        // Split by comma or semicolon
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

        // For each category, pick the most cost-effective top-rated product
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

        // If total price exceeds budget, find if there are lower alternatives
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

    // --- AI Assistant Guidance ---
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
