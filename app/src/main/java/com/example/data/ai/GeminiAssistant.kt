package com.example.data.ai

import com.example.BuildConfig
import com.example.data.datasource.SkincareCatalog
import com.example.data.model.Product
import com.example.data.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun askAssistant(
        userQuery: String,
        userPreferences: UserPreferences,
        shelfProductsSummary: String,
        routineSummary: String
    ): AssistantResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val isKeyValid = apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY")

        if (!isKeyValid) {
            // Generate verified smart fallback response from internal skincare intelligence
            return@withContext generateLocalResponse(userQuery, userPreferences)
        }

        try {
            val systemPrompt = """
                You are SkinRoutine AI, an elite beauty-tech companion, cosmetic scientist, and skincare routine strategist.
                Brand Personality: Premium, clean, modern, trustworthy, calm, science-informed, easy for beginners.
                
                USER PROFILE CONTEXT:
                - Skin Goals: ${userPreferences.skinGoals.joinToString(", ")}
                - Experience: ${userPreferences.experienceLevel}
                - Stated Sensitivities: ${if (userPreferences.sensitivities.isEmpty()) "None declared" else userPreferences.sensitivities.joinToString(", ")}
                - Fragrance Preference: ${userPreferences.fragrancePreference}
                - Routine Complexity: ${userPreferences.routineComplexity}
                - Currency & Budget: ${userPreferences.currency} ${userPreferences.budget}
                - User's Beauty Shelf: $shelfProductsSummary
                - Current Daily Routine: $routineSummary
                
                RULES & SAFETY (MANDATORY):
                1. Provide cosmetic, formulation, and routine information. NEVER medically diagnose skin conditions or prescribe drug treatments.
                2. If the user presents with persistent or severe symptoms (e.g. cystic painful breakouts, dermatitis flares, bleeding, suspicious moles), advise consulting a board-certified dermatologist.
                3. Explain active ingredients in understandable, empowering language (e.g., explain why BHA penetrates pores or why Niacinamide strengthens ceramides).
                4. Cross-check for ingredient clashes: warn against layering strong Retinoids with AHAs/BHAs on the same evening.
                5. Highlight sun protection: always emphasize broad-spectrum SPF when actives or daytime routines are discussed.
                6. Do NOT fabricate fake clinical studies or make magical cure promises. Be honest about product timelines (typically 4-12 weeks for noticeable results).
                7. Keep formatting elegant with short, readable paragraphs and clean bullet points.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userQuery)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (!text.isNullOrBlank()) {
                    val suggestedProducts = findRelevantProducts(userQuery)
                    return@withContext AssistantResponse(
                        text = text.trim(),
                        suggestedProducts = suggestedProducts,
                        isFromLiveAi = true
                    )
                }
            }
            // If request had error or empty candidate, fallback gracefully
            generateLocalResponse(userQuery, userPreferences)
        } catch (e: Exception) {
            generateLocalResponse(userQuery, userPreferences)
        }
    }

    private fun generateLocalResponse(query: String, prefs: UserPreferences): AssistantResponse {
        val lower = query.lowercase()
        val suggested = findRelevantProducts(query)

        val text = when {
            lower.contains("vitamin c") && lower.contains("niacinamide") -> {
                """
                **Can you combine Vitamin C with Niacinamide?**
                
                Yes, in modern cosmetic formulations!
                
                • **The Myth:** Decades ago, non-stabilized ascorbic acid and niacinamide in high heat were thought to neutralize each other and cause flushing.
                • **Modern Science:** High-quality modern skincare products use stabilized forms or buffered pH levels, allowing both to work harmoniously together.
                • **SkinRoutine Tip:** If you have reactive skin, an easy gold-standard method is applying **Vitamin C in the morning** (to protect against UV free radicals) and **Niacinamide in the evening** (for barrier repair and sebum control).
                """.trimIndent()
            }
            lower.contains("retinol") || lower.contains("retinoid") -> {
                """
                **Retinoid Guidance & Safe Introduction**
                
                Retinoids (Vitamin A) are the dermatological gold-standard for cell renewal and collagen stimulation.
                
                • **How to Start:** Begin with low concentration 2 nights a week (Monday & Thursday). Gradually increase frequency over 4-6 weeks as skin acclimates.
                • **The Sandwich Method:** To minimize flaking, apply a light moisturizer first, followed by a pea-sized amount of Retinol, then a richer barrier cream (like Ceramides or Cicaplast).
                • **What to Avoid:** Do not combine with AHA/BHA exfoliating acids or strong physical scrubs in the same evening.
                • **Daytime Rule:** Retinoids increase photosensitivity; a broad-spectrum SPF 30+ is mandatory every morning.
                """.trimIndent()
            }
            lower.contains("bha") || lower.contains("salicylic") || lower.contains("blackhead") || lower.contains("pore") -> {
                """
                **Understanding Salicylic Acid (BHA) for Pores**
                
                Salicylic Acid is unique because it is oil-soluble:
                
                • It penetrates beneath surface sebum inside pore linings to dissolve accumulated dead cells and keratin plugs.
                • **Recommended Frequency:** 2 to 3 times per week at night on clean, dry skin. Overusing BHA can strip natural lipids and cause rebound oiliness.
                • Follow with hydrating ingredients such as Hyaluronic Acid or Centella Asiatica to keep the barrier calm.
                """.trimIndent()
            }
            lower.contains("dry") || lower.contains("barrier") || lower.contains("dehydrated") -> {
                """
                **Restoring a Compromised Skin Barrier**
                
                When skin stings, flakes, or feels tight, the moisture barrier needs immediate replenishment:
                
                • **Strip Back:** Temporarily pause all exfoliating acids (AHA/BHA), retinoids, and pure Vitamin C for 10-14 days.
                • **Gentle Cleansing:** Use a non-foaming lotion cleanser without sulfates.
                • **Hero Ingredients:** Look for **Ceramides (1, 3, 6-II)**, **Panthenol (5%)**, and **Centella Asiatica**.
                • Apply a restorative soothing balm like *La Roche-Posay Cicaplast Baume B5+* over slightly damp skin.
                """.trimIndent()
            }
            lower.contains("routine") || lower.contains("order") -> {
                """
                **The Ideal Skincare Application Order**
                
                Apply from **thinnest consistency to thickest**:
                
                **Morning (Protection):**
                1. Gentle Cleanser (or lukewarm water rinse)
                2. Hydrating Toner / Essence
                3. Antioxidant Serum (e.g., Vitamin C or Niacinamide)
                4. Lightweight Moisturizer
                5. Broad-Spectrum Sunscreen (SPF 30-50+)
                
                **Evening (Recovery):**
                1. Cleanser (Double cleanse if wearing makeup/SPF)
                2. Hydrating Toner
                3. Target Treatment (BHA or Retinol — alternate nights)
                4. Barrier Moisturizer / Night Cream
                """.trimIndent()
            }
            else -> {
                """
                **Skincare Intelligence Insight**
                
                Here is guidance tailored to your goals (${prefs.skinGoals.joinToString(", ")}):
                
                • **Consistency over Complexity:** A simple 3-4 step routine used daily produces far better, longer-lasting results than an overly complex regimen with conflicting actives.
                • **Barrier First:** Healthy skin starts with a resilient moisture barrier. Ensure your routine includes humectants (Hyaluronic Acid/Glycerin) and barrier lipids (Ceramides).
                • **Sensitivities:** ${if (prefs.sensitivities.isEmpty()) "Keep formulas fragrance-free where possible." else "Always scan ingredient lists to avoid ${prefs.sensitivities.joinToString(", ")}."}
                
                *Note: This advice is cosmetic and formulation guidance. If experiencing persistent skin conditions or allergies, please consult a qualified dermatologist.*
                """.trimIndent()
            }
        }

        return AssistantResponse(
            text = text,
            suggestedProducts = suggested,
            isFromLiveAi = false
        )
    }

    private fun findRelevantProducts(query: String): List<Product> {
        val lower = query.lowercase()
        return SkincareCatalog.products.filter { prod ->
            lower.contains(prod.name.lowercase()) ||
            lower.contains(prod.brand.lowercase()) ||
            lower.contains(prod.category.lowercase()) ||
            prod.keyActives.any { active -> lower.contains(active.lowercase()) }
        }.take(3)
    }
}

data class AssistantResponse(
    val text: String,
    val suggestedProducts: List<Product> = emptyList(),
    val isFromLiveAi: Boolean = false
)
