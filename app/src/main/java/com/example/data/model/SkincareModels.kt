package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

enum class RoutineType {
    MORNING,
    EVENING
}

@IgnoreExtraProperties
data class RoutineStep(
    @DocumentId val id: String = "",
    val userId: String = "",
    val routineType: RoutineType = RoutineType.MORNING,
    val stepNumber: Int = 1,
    val category: String = "Cleanser",
    val productId: String? = null,
    val customTitle: String? = null,
    val purpose: String = "",
    val usageTip: String = "",
    @get:PropertyName("isCompletedToday") @set:PropertyName("isCompletedToday")
    var isCompletedToday: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ShelfProduct(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String? = null,
    val name: String = "",
    val brand: String = "",
    val category: String = "Serum",
    val openedDate: String = "",
    val notes: String = "",
    val paoMonths: Int = 12,
    val routineAssignment: String = "Both",
    val routineTime: String = "Both",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class JournalEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateString: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val morningCompleted: Boolean = false,
    val eveningCompleted: Boolean = false,
    val skinComfort: Int = 4,
    val comfortScore: Int = 4,
    val hydrationLevel: Int = 4,
    val hydrationScore: Int = 4,
    val irritationLevel: Int = 1,
    val irritationScore: Int = 1,
    val breakoutReported: Boolean = false,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedProducts: List<Product> = emptyList(),
    val isPending: Boolean = false
)

data class IngredientAnalysisResult(
    val matchedIngredients: List<Ingredient> = emptyList(),
    val flaggedSensitizers: List<String> = emptyList(),
    val totalIdentifiedCount: Int = 0,
    val summaryNote: String = ""
)

data class BudgetBundle(
    val products: List<Product> = emptyList(),
    val totalPrice: Double = 0.0,
    val budget: Double = 0.0,
    val currency: String = "USD",
    val savings: Double = 0.0,
    val reason: String = "",
    val alternativeProducts: List<Product> = emptyList()
)
