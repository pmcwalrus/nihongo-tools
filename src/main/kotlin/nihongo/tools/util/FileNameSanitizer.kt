package nihongo.tools.util

object FileNameSanitizer {
    private val forbiddenChars = Regex("""[\\/:*?"<>|]""")

    fun sanitize(name: String, fallback: String): String {
        val cleaned = name.trim().replace(forbiddenChars, "_").trim('.').ifBlank { fallback }
        return cleaned.take(120)
    }
}

