package chat.cabal.mobile.core

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LinkPreview(
    val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
)

@Suppress("unused")
class LinkPreviewManager {
    private val client = HttpClient(CIO) {
        followRedirects = true
    }

    @Suppress("unused")
    suspend fun getPreview(url: String): LinkPreview? = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.get(url)
            val body = response.bodyAsText()
            
            // Basic non-named group regex
            val titleRegex = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE)
            val title = titleRegex.find(body)?.groupValues?.get(1)
            
            val descRegex = Regex("<meta name=\"description\" content=\"(.*?)\"", RegexOption.IGNORE_CASE)
            val description = descRegex.find(body)?.groupValues?.get(1)
                ?: Regex("<meta property=\"og:description\" content=\"(.*?)\"", RegexOption.IGNORE_CASE)
                .find(body)?.groupValues?.get(1)

            val imgRegex = Regex("<meta property=\"og:image\" content=\"(.*?)\"", RegexOption.IGNORE_CASE)
            val imageUrl = imgRegex.find(body)?.groupValues?.get(1)

            LinkPreview(url, title, description, imageUrl)
        } catch (_: Exception) {
            null
        }
    }
}
