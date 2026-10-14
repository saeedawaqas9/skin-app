package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.RoutineStep
import com.example.data.model.RoutineType
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreSkincareRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedUser_success() = runBlocking {
        val uid = signInTestUser("alice_beauty@example.com")
        val repository = FirestoreSkincareRepository(firestore)

        val prefs = UserPreferences(
            userId = uid,
            displayName = "Alice Beauty",
            email = "alice_beauty@example.com",
            skinGoals = listOf("Hydration", "Barrier Support"),
            isOnboardingDone = true
        )

        withTimeout(5000L) {
            repository.saveUserProfile(uid, prefs)
        }

        val fetched = withTimeout(5000L) {
            repository.getUserProfile(uid)
        }

        assertNotNull(fetched)
        assertEquals("Alice Beauty", fetched?.displayName)
        assertTrue(fetched?.isOnboardingDone == true)
    }

    @Test
    fun saveAndObserveRoutineSteps_authenticatedOwner_returnsSteps() = runBlocking {
        val uid = signInTestUser("bob_skincare@example.com")
        val repository = FirestoreSkincareRepository(firestore)

        val step = RoutineStep(
            id = "step_morning_cleanse",
            userId = uid,
            routineType = RoutineType.MORNING,
            stepNumber = 1,
            category = "Cleanser",
            customTitle = "Hydrating Foam Cleanser",
            purpose = "Gently cleanse skin",
            usageTip = "Massage onto damp skin"
        )

        withTimeout(5000L) {
            repository.saveRoutineStep(uid, step)
        }

        val steps = withTimeout(5000L) {
            repository.observeRoutineSteps(uid).first { it.isNotEmpty() }
        }

        assertEquals(1, steps.size)
        assertEquals("Hydrating Foam Cleanser", steps[0].customTitle)
    }
}
