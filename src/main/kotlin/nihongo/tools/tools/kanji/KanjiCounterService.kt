package nihongo.tools.tools.kanji

import nihongo.tools.service.DocumentTextExtractor
import nihongo.tools.util.CsvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class KanjiCountResult(
    val outputFile: File,
    val totalKanji: Int,
    val uniqueKanji: Int
)

class KanjiCounterService(
    private val extractor: DocumentTextExtractor = DocumentTextExtractor()
) {
    suspend fun countKanji(
        sourceFile: File,
        exclusionFile: File?,
        outputDirectory: File,
        progress: suspend (Float, String) -> Unit
    ): KanjiCountResult {
        withContext(Dispatchers.Main) { progress(0.05f, "Чтение исходного файла") }
        val sourceText = extractor.extractText(sourceFile)

        withContext(Dispatchers.Main) { progress(0.25f, "Чтение файла исключений") }
        val excludedKanji = exclusionFile?.let { file ->
            extractor.extractText(file).asSequence().filter(::isKanji).toSet()
        }.orEmpty()

        withContext(Dispatchers.Main) { progress(0.55f, "Подсчет кандзи") }
        val counts = linkedMapOf<Char, Int>()
        sourceText.forEach { char ->
            if (isKanji(char) && char !in excludedKanji) {
                counts[char] = (counts[char] ?: 0) + 1
            }
        }

        withContext(Dispatchers.Main) { progress(0.8f, "Подготовка CSV") }
        val sorted = counts.entries.sortedByDescending { it.value }
        val outputFile = File(outputDirectory, "${sourceFile.nameWithoutExtension}_kanji_counts.csv")
        CsvWriter.writeRows(
            file = outputFile,
            header = listOf("kanji", "count"),
            rows = sorted.map { listOf(it.key.toString(), it.value.toString()) }
        )

        withContext(Dispatchers.Main) { progress(1f, "Готово") }
        return KanjiCountResult(
            outputFile = outputFile,
            totalKanji = sorted.sumOf { it.value },
            uniqueKanji = sorted.size
        )
    }

    fun isKanji(char: Char): Boolean = Character.UnicodeScript.of(char.code) == Character.UnicodeScript.HAN
}
