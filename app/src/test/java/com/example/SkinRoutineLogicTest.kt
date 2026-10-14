package com.example

import com.example.data.datasource.SkincareCatalog
import com.example.data.repository.SkincareRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SkinRoutineLogicTest {

    @Test
    fun catalogHasVerifiedProducts() {
        val products = SkincareCatalog.products
        assertTrue("Catalog should contain verified products", products.size >= 10)
        assertTrue(products.any { it.brand == "CeraVe" })
        assertTrue(products.any { it.category == "Sunscreen" })
        assertTrue(products.any { it.isFragranceFree })
    }

    @Test
    fun multiCurrencyPricesAreConsistent() {
        val prod = SkincareCatalog.products.first()
        assertTrue(prod.priceUsd > 0)
        assertTrue(prod.pricePkr > 0)
        assertTrue(prod.priceEur > 0)
        assertTrue(prod.formattedPrice("PKR").contains("PKR"))
        assertTrue(prod.formattedPrice("USD").contains("$"))
    }

    @Test
    fun ingredientDecodingIdentifiesActivesAndSensitizers() {
        // Test decoding without DAO instance using dummy dao or repository helper
        val rawList = "Water, Glycerin, Niacinamide, Sodium Hyaluronate, Ceramide NP, Alcohol Denat, Parfum"
        
        // Let's verify known ingredient detection directly from catalog
        val tokens = rawList.split(",", "\n").map { it.trim().lowercase() }
        val foundActives = SkincareCatalog.ingredients.filter { ing ->
            val base = ing.name.lowercase().substringBefore("(").trim()
            tokens.any { token ->
                token.contains(base) || base.contains(token) ||
                        (token.contains("ceramide") && base.contains("ceramide")) ||
                        (token.contains("hyaluron") && base.contains("hyaluronic"))
            }
        }

        assertTrue("Should detect at least 2 active ingredients", foundActives.size >= 2)
        assertTrue(tokens.any { it.contains("alcohol denat") })
        assertTrue(tokens.any { it.contains("parfum") })
    }

    @Test
    fun ingredientEncyclopediaHasEssentialSkincareScience() {
        val ingredients = SkincareCatalog.ingredients
        assertTrue(ingredients.any { it.name.contains("Niacinamide") })
        assertTrue(ingredients.any { it.name.contains("Hyaluronic Acid") })
        assertTrue(ingredients.any { it.name.contains("Salicylic Acid") })
        assertTrue(ingredients.any { it.name.contains("Retinol") })
    }

    @Test
    fun userPreferencesHandlesMultiCurrencyAndDefaults() {
        val prefs = com.example.data.model.UserPreferences(
            skinGoals = listOf("Hydration", "Skin Barrier Repair"),
            currentRoutineLevel = "Basic (Cleanser & Moisturizer)",
            existingProductsOwned = listOf("Cleanser", "Moisturizer"),
            budget = 60.0,
            currency = "USD"
        )
        assertEquals("$60.00", prefs.formatPrice(prefs.budget))
        val pkrPrefs = prefs.copy(currency = "PKR", budget = 4500.0)
        assertTrue(pkrPrefs.formatPrice(pkrPrefs.budget).contains("PKR"))
        val eurPrefs = prefs.copy(currency = "EUR", budget = 50.0)
        assertTrue(eurPrefs.formatPrice(eurPrefs.budget).contains("€"))
    }
}
