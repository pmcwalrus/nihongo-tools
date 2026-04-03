package nihongo.tools.tools.kanjimap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nihongo.tools.util.FileNameSanitizer
import org.jsoup.Jsoup
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.text.StringBuilder

data class KanjiMapAudioEntry(
    val wordWithReading: String,
    val downloadName: String,
    val translation: String,
    val audioUrl: String
)

class KanjiMapAudioService(
    private val httpClient: HttpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
) {
    suspend fun fetchEntries(kanji: String): List<KanjiMapAudioEntry> {
        val normalizedKanji = kanji.trim()
        require(normalizedKanji.isNotEmpty()) { "Введите иероглиф." }

        val pageUrl = "https://thekanjimap.com/${URLEncoder.encode(normalizedKanji, Charsets.UTF_8)}"
        val request = HttpRequest.newBuilder(URI(pageUrl))
            .header("User-Agent", "Mozilla/5.0 NihongoTools")
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            error("Не удалось открыть страницу The Kanji Map. HTTP ${response.statusCode()}")
        }

        val html = response.body()
        val examplesJson = extractExamplesJson(html, normalizedKanji)
            ?: error("Не удалось найти список слов с аудио на странице $pageUrl")

        return parseExamples(examplesJson)
            .filter { it.audioUrl.isNotBlank() }
            .distinctBy { it.wordWithReading to it.audioUrl }
    }

    suspend fun downloadEntry(
        entry: KanjiMapAudioEntry,
        outputDirectory: File
    ): File {
        outputDirectory.mkdirs()
        val request = HttpRequest.newBuilder(URI(entry.audioUrl))
            .header("User-Agent", "Mozilla/5.0 NihongoTools")
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
        if (response.statusCode() !in 200..299) {
            error("Не удалось скачать ${entry.audioUrl}. HTTP ${response.statusCode()}")
        }

        val file = File(outputDirectory, buildUniqueFileName(outputDirectory, entry.downloadName))
        withContext(Dispatchers.IO) {
            file.writeBytes(response.body())
        }
        return file
    }

    internal fun parseExamplesPage(html: String, kanji: String): List<KanjiMapAudioEntry> {
        val examplesJson = extractExamplesJson(html, kanji.trim()) ?: return emptyList()
        return parseExamples(examplesJson)
    }

    private fun extractExamplesJson(html: String, kanji: String): String? {
        val normalizedHtml = normalizePayloadText(html)
        val requestedIdMarker = "\"requestedId\":\"${escapeJson(kanji)}\""
        val requestedIndex = normalizedHtml.indexOf(requestedIdMarker)
        if (requestedIndex == -1) {
            return null
        }

        val kanjiInfoIndex = normalizedHtml.indexOf("\"kanjiInfo\":", requestedIndex)
        if (kanjiInfoIndex == -1) {
            return null
        }

        val kanjiInfoObject = extractBalancedBlock(
            text = normalizedHtml,
            startIndex = normalizedHtml.indexOf('{', kanjiInfoIndex)
        ) ?: return null

        val examplesKeyIndex = kanjiInfoObject.indexOf("\"examples\":")
        if (examplesKeyIndex == -1) {
            return null
        }

        return extractBalancedBlock(
            text = kanjiInfoObject,
            startIndex = kanjiInfoObject.indexOf('[', examplesKeyIndex)
        )
    }

    private fun parseExamples(examplesJson: String): List<KanjiMapAudioEntry> {
        return splitTopLevelObjects(examplesJson).mapNotNull { exampleJson ->
            val japanese = extractJsonString(exampleJson, "japanese")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val translation = extractJsonString(exampleJson, "english").orEmpty()
            val mp3 = extractJsonString(exampleJson, "mp3")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

            KanjiMapAudioEntry(
                wordWithReading = japanese,
                downloadName = buildDownloadName(japanese),
                translation = Jsoup.parse(translation).text().trim(),
                audioUrl = mp3
            )
        }
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

    internal fun buildDownloadName(wordWithReading: String): String {
        val withoutReading = wordWithReading
            .replace(Regex("""\s*[（(][^）)]*[）)]\s*$"""), "")
            .trim()
        return FileNameSanitizer.sanitize(withoutReading, "audio")
    }

    private fun extractJsonString(json: String, fieldName: String): String? {
        val key = "\"$fieldName\":\""
        val start = json.indexOf(key)
        if (start == -1) {
            return null
        }

        val valueStart = start + key.length
        val valueEnd = findStringEnd(json, valueStart)
        return decodeJsonString(json.substring(valueStart, valueEnd))
    }

    private fun findStringEnd(text: String, startIndex: Int): Int {
        var index = startIndex
        var escaped = false
        while (index < text.length) {
            val char = text[index]
            if (escaped) {
                escaped = false
            } else if (char == '\\') {
                escaped = true
            } else if (char == '"') {
                return index
            }
            index += 1
        }
        error("Не удалось завершить JSON-строку.")
    }

    private fun splitTopLevelObjects(arrayJson: String): List<String> {
        val result = mutableListOf<String>()
        var index = 0
        while (index < arrayJson.length) {
            if (arrayJson[index] == '{') {
                val block = extractBalancedBlock(arrayJson, index) ?: break
                result += block
                index += block.length
            } else {
                index += 1
            }
        }
        return result
    }

    private fun extractBalancedBlock(text: String, startIndex: Int): String? {
        if (startIndex !in text.indices) {
            return null
        }

        val opening = text[startIndex]
        val closing = when (opening) {
            '{' -> '}'
            '[' -> ']'
            else -> return null
        }

        var depth = 0
        var index = startIndex
        var inString = false
        var escaped = false

        while (index < text.length) {
            val char = text[index]
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == '"') {
                    inString = false
                }
            } else {
                when (char) {
                    '"' -> inString = true
                    opening -> depth += 1
                    closing -> {
                        depth -= 1
                        if (depth == 0) {
                            return text.substring(startIndex, index + 1)
                        }
                    }
                }
            }
            index += 1
        }

        return null
    }

    private fun decodeJsonString(value: String): String {
        val builder = StringBuilder(value.length)
        var index = 0
        while (index < value.length) {
            val char = value[index]
            if (char != '\\') {
                builder.append(char)
                index += 1
                continue
            }

            val next = value.getOrNull(index + 1) ?: break
            when (next) {
                '"', '\\', '/' -> builder.append(next)
                'b' -> builder.append('\b')
                'f' -> builder.append('\u000C')
                'n' -> builder.append('\n')
                'r' -> builder.append('\r')
                't' -> builder.append('\t')
                'u' -> {
                    val hex = value.substring(index + 2, index + 6)
                    builder.append(hex.toInt(16).toChar())
                    index += 4
                }
                else -> builder.append(next)
            }
            index += 2
        }
        return builder.toString()
    }

    private fun normalizePayloadText(html: String): String {
        return html.replace("\\\"", "\"")
    }

    private fun escapeJson(value: String): String {
        val builder = StringBuilder()
        value.forEach { char ->
            when (char) {
                '\\' -> builder.append("\\\\")
                '"' -> builder.append("\\\"")
                '\b' -> builder.append("\\b")
                '\u000C' -> builder.append("\\f")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> builder.append(char)
            }
        }
        return builder.toString()
    }
}
