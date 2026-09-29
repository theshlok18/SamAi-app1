package dev.shlok.samai.profile

object DeveloperProfile {
    const val NAME = "Shlok"
    const val ROLE = "Data Science Student"
    const val DESCRIPTION = "AI Enthusiast"
    const val INSPIRATION = "Inspired by Tony Stark's JARVIS concept"
    const val GITHUB = "theshlok18"
    const val INSTAGRAM = "iishlok23"

    fun getDescription(): String = """
        I was developed by $NAME, a $ROLE and $DESCRIPTION.
        SAM AI was $INSPIRATION.
        GitHub: $GITHUB
        Instagram: $INSTAGRAM
    """.trimIndent()

    fun isDevQuery(query: String): Boolean {
        val keywords = listOf(
            "who developed", "who created", "who made", "who built",
            "your developer", "your creator", "your maker",
            "who are you made by", "who is behind"
        )
        val lower = query.lowercase()
        return keywords.any { lower.contains(it) }
    }
}
