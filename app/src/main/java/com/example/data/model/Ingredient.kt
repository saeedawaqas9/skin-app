package com.example.data.model

data class Ingredient(
    val id: String,
    val name: String,
    val category: String, // "Hydrator", "Antioxidant", "Exfoliant (AHA/BHA)", "Barrier Repair", "Soothing", "Retinoid"
    val plainSummary: String,
    val cosmeticPurpose: String,
    val recommendedFor: List<String>,
    val irritationRisk: String, // "Very Low", "Low", "Moderate (Intro Gradually)", "High if Overused"
    val pairsWellWith: List<String>,
    val avoidPairingWith: List<String>,
    val usageTip: String
)
