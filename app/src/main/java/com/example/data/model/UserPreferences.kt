package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserPreferences(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val skinGoals: List<String> = listOf("Hydration", "Skin Barrier Repair"),
    val experienceLevel: String = "Beginner",
    val currentRoutineLevel: String = "Basic (Cleanser & Moisturizer)",
    val existingProductsOwned: List<String> = emptyList(),
    val sensitivities: List<String> = emptyList(),
    val fragrancePreference: String = "Fragrance-Free Preferred",
    val productTexturePreference: String = "Lightweight Gels & Fluids",
    val routineComplexity: String = "Balanced (4-5 steps)",
    val budgetTier: String = "Affordable",
    val budget: Double = 50.0,
    val currency: String = "USD",
    @get:PropertyName("isOnboardingDone") @set:PropertyName("isOnboardingDone")
    var isOnboardingDone: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun formatPrice(amount: Double): String {
        return when (currency) {
            "PKR" -> "PKR ${String.format("%,.0f", amount)}"
            "EUR" -> "€${String.format("%.2f", amount)}"
            "GBP" -> "£${String.format("%.2f", amount)}"
            else -> "$${String.format("%.2f", amount)}"
        }
    }
}
