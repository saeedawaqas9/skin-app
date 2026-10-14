package com.example.data.model

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: String, // "Cleanser", "Toner", "Serum", "Moisturizer", "Sunscreen", "Treatment"
    val priceUsd: Double,
    val pricePkr: Double,
    val priceEur: Double,
    val priceGbp: Double,
    val rating: Float,
    val description: String,
    val suitability: String, // e.g. "Normal to Dry, Sensitive Skin"
    val keyActives: List<String>,
    val ingredientsList: String,
    val warnings: String = "",
    val isFragranceFree: Boolean = true,
    val isHypoallergenic: Boolean = true,
    val crueltyFree: Boolean = true,
    val paoMonths: Int = 12,
    val accentColorHex: Long = 0xFFF5EFEB
) {
    fun getPrice(currency: String): Double {
        return when (currency) {
            "PKR" -> pricePkr
            "EUR" -> priceEur
            "GBP" -> priceGbp
            else -> priceUsd
        }
    }

    fun formattedPrice(currency: String): String {
        val amount = getPrice(currency)
        return when (currency) {
            "PKR" -> "PKR ${String.format("%,.0f", amount)}"
            "EUR" -> "€${String.format("%.2f", amount)}"
            "GBP" -> "£${String.format("%.2f", amount)}"
            else -> "$${String.format("%.2f", amount)}"
        }
    }
}
