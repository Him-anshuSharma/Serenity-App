package com.serenity


data class AiAnalysis(
    val summary: String? = "",
    val mood: Map<String, Int>? = emptyMap(),
    val insights: List<String>? = emptyList(),
    val activities: List<String>? = emptyList(),
    val people: List<AiPerson>? = emptyList(),
    val moments: List<AiMoment>? = emptyList(),
    val places: List<String>? = emptyList(),
    val emotions_psychological: List<String>? = emptyList(),
    val reasons_behind_emotions: Map<String, String>? = emptyMap(),
    val triggers: Map<String, String>? = emptyMap(),
    val psychological_interpretation: String? = ""
)

data class AiPerson(
    val name: String? = "",
    val relationship: String? = "neutral", // "positive", "negative", "neutral"
    val description: String? = ""
)

data class AiMoment(
    val context: String? = "",
    val emotion: String? = "neutral", // "positive", "negative", "neutral"
    val description: String? = ""
)