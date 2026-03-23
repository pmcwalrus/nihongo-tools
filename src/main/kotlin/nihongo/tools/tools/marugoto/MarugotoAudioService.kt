package nihongo.tools.tools.marugoto

import nihongo.tools.util.FileNameSanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class MarugotoAudioEntry(
    val label: String,
    val audioUrl: String
)

data class MarugotoDownloadResult(
    val savedFiles: Int,
    val outputDirectory: File
)

class MarugotoAudioService(
    private val httpClient: HttpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
) {
    private val entryRegex = Regex(
        pattern = "(?s)<div class=\"_jpn\">\\s*(.*?)\\s*</div>.*?<span class=\"_btn-sound js-audioPlayer\"[^>]*data-audio=\"([^\"]+\\.mp3)\""
    )
    private val baseUri = URI("https://www.marugoto-online.jp/")

    fun parseEntries(rawText: String): List<MarugotoAudioEntry> {
        return entryRegex.findAll(rawText).mapNotNull { match ->
            val rawLabel = Jsoup.parse(match.groupValues[1]).text().trim()
            val audioPath = match.groupValues[2].trim()
            if (rawLabel.isBlank() || audioPath.isBlank()) {
                null
            } else {
                MarugotoAudioEntry(
                    label = rawLabel,
                    audioUrl = baseUri.resolve(audioPath.removePrefix("/")).toString()
                )
            }
        }.toList()
    }

    suspend fun downloadEntries(
        entries: List<MarugotoAudioEntry>,
        outputDirectory: File,
        progress: suspend (Float, String) -> Unit
    ): MarugotoDownloadResult {
        require(entries.isNotEmpty()) { "Не найдено ни одной аудиозаписи." }

        outputDirectory.mkdirs()
        entries.forEachIndexed { index, entry ->
            withContext(Dispatchers.Main) {
                progress(index.toFloat() / entries.size, "Скачивание ${index + 1} из ${entries.size}: ${entry.label}")
            }
            val request = HttpRequest.newBuilder(URI(entry.audioUrl)).GET().build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
            if (response.statusCode() !in 200..299) {
                error("Не удалось скачать ${entry.audioUrl}. HTTP ${response.statusCode()}")
            }

            val fileName = buildUniqueFileName(outputDirectory, entry.label)
            File(outputDirectory, fileName).writeBytes(response.body())
        }

        withContext(Dispatchers.Main) { progress(1f, "Готово") }
        return MarugotoDownloadResult(entries.size, outputDirectory)
    }

    private fun buildUniqueFileName(directory: File, rawLabel: String): String {
        val baseName = FileNameSanitizer.sanitize(rawLabel, "audio")
        var index = 1
        var candidate = "$baseName.mp3"
        while (File(directory, candidate).exists()) {
            index += 1
            candidate = "$baseName ($index).mp3"
        }
        return candidate
    }
}
