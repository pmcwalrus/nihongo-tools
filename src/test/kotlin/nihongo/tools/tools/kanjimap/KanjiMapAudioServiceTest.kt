package nihongo.tools.tools.kanjimap

import kotlin.test.Test
import kotlin.test.assertEquals

class KanjiMapAudioServiceTest {
    private val service = KanjiMapAudioService()

    @Test
    fun `parses kanji map examples from page payload`() {
        val html = """
            <html>
            <body>
            <script>
                self.__next_f.push([1,"8:[\"$\",\"div\",null,{\"requestedId\":\"食\",\"kanjiInfo\":{\"id\":\"食\",\"kanjialiveData\":{\"examples\":[{\"japanese\":\"食堂（しょくどう）\",\"meaning\":{\"english\":\"dining hall\"},\"audio\":{\"mp3\":\"https://media.example.com/shokudo.mp3\"}},{\"japanese\":\"朝食（ちょうしょく）\",\"meaning\":{\"english\":\"breakfast\"},\"audio\":{\"mp3\":\"https://media.example.com/choshoku.mp3\"}}]}}}]"])
            </script>
            </body>
            </html>
        """.trimIndent()

        val entries = service.parseExamplesPage(html, "食")

        assertEquals(2, entries.size)
        assertEquals("食堂（しょくどう）", entries[0].wordWithReading)
        assertEquals("食堂", entries[0].downloadName)
        assertEquals("dining hall", entries[0].translation)
        assertEquals("https://media.example.com/shokudo.mp3", entries[0].audioUrl)
        assertEquals("朝食", entries[1].downloadName)
    }

    @Test
    fun `builds download name without reading in parentheses`() {
        assertEquals("食堂", service.buildDownloadName("食堂（しょくどう）"))
        assertEquals("休む", service.buildDownloadName("休む(やすむ)"))
        assertEquals("audio", service.buildDownloadName("（しょくどう）"))
    }
}
