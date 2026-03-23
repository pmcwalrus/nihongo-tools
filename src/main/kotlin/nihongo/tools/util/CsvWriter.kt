package nihongo.tools.util

import java.io.File

object CsvWriter {
    fun writeRows(file: File, header: List<String>, rows: List<List<String>>) {
        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.appendLine(header.joinToString(",") { escape(it) })
            rows.forEach { row ->
                writer.appendLine(row.joinToString(",") { escape(it) })
            }
        }
    }

    private fun escape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}

