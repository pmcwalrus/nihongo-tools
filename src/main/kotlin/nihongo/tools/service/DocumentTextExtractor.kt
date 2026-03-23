package nihongo.tools.service

import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import java.io.File

class DocumentTextExtractor {
    fun extractText(file: File): String {
        file.inputStream().buffered().use { input ->
            val parser = AutoDetectParser()
            val handler = BodyContentHandler(-1)
            parser.parse(input, handler, Metadata(), ParseContext())
            return handler.toString()
        }
    }
}

