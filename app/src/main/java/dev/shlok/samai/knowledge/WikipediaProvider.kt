package dev.shlok.samai.knowledge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WikipediaProvider @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun search(query: String): String? = withContext(Dispatchers.IO) {
        try {
            // Extract meaningful search term
            val searchTerm = extractSearchTerm(query)
            val encodedQuery = java.net.URLEncoder.encode(searchTerm, "UTF-8")
            val url = "https://en.wikipedia.org/api/rest_v1/page/summary/$encodedQuery"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                // Try search API fallback
                return@withContext searchFallback(searchTerm)
            }

            val body = response.body?.string() ?: return@withContext null
            val parsed = gson.fromJson(body, WikiSummaryResponse::class.java)
            parsed.extract?.takeIf { it.isNotBlank() }?.let { text ->
                val truncated = if (text.length > 800) text.take(800) + "..." else text
                "📚 $truncated\n\n[Source: Wikipedia - ${parsed.title}]"
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun searchFallback(query: String): String? = withContext(Dispatchers.IO) {
        try {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encoded&format=json&srlimit=1"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val parsed = gson.fromJson(body, WikiSearchResponse::class.java)
            val title = parsed.query?.search?.firstOrNull()?.title ?: return@withContext null
            search(title)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractSearchTerm(query: String): String {
        val patterns = listOf(
            Regex("(?:tell me about|explain|what is|who is|search for|wikipedia for)\\s+(.+)", RegexOption.IGNORE_CASE),
            Regex("(?:what are)\\s+(.+)", RegexOption.IGNORE_CASE)
        )
        for (p in patterns) {
            val match = p.find(query) ?: continue
            return match.groupValues[1].trim()
        }
        return query.trim()
    }

    data class WikiSummaryResponse(val title: String?, val extract: String?)
    data class WikiSearchResponse(val query: WikiQuery?)
    data class WikiQuery(val search: List<WikiSearchResult>?)
    data class WikiSearchResult(val title: String?)
}

interface WebSearchProvider {
    suspend fun search(query: String): String?
}
