package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class PreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val skinGoals: String, // Comma-separated
    val experienceLevel: String,
    val currentRoutineLevel: String = "",
    val existingProductsOwned: String = "", // Comma-separated
    val sensitivities: String, // Comma-separated
    val fragrancePreference: String,
    val productTexturePreference: String = "",
    val routineComplexity: String,
    val budget: Double,
    val currency: String,
    val isOnboardingDone: Boolean
)

@Entity(tableName = "shelf_products")
data class ShelfEntity(
    @PrimaryKey val id: String,
    val productId: String?,
    val name: String,
    val brand: String,
    val category: String,
    val openedDate: String,
    val notes: String,
    val paoMonths: Int,
    val routineTime: String
)

@Entity(tableName = "routine_steps")
data class RoutineStepEntity(
    @PrimaryKey val id: String,
    val routineType: String, // "MORNING" or "EVENING"
    val stepNumber: Int,
    val category: String,
    val productId: String?,
    val customTitle: String?,
    val purpose: String,
    val usageTip: String,
    val isCompletedToday: Boolean
)

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val dateString: String,
    val timestamp: Long,
    val morningCompleted: Boolean,
    val eveningCompleted: Boolean,
    val comfortScore: Int,
    val hydrationScore: Int,
    val irritationScore: Int,
    val notes: String
)
