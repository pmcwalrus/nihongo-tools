package nihongo.tools.tools.marugoto

import kotlin.test.Test
import kotlin.test.assertEquals

class MarugotoAudioServiceTest {
    private val service = MarugotoAudioService()

    @Test
    fun `parses marugoto audio entries`() {
        val input = """
            <div class="_jpn">
                あの
            </div>
            <span class="_btn-sound js-audioPlayer" data-audio="/a1/course/assets/sound/reference-word/A1W/A1W_0082.mp3"></span>
            <div class="_roman js-text-roman">ano</div>
        """.trimIndent()

        val entries = service.parseEntries(input)

        assertEquals(1, entries.size)
        assertEquals("あの", entries.first().label)
        assertEquals(
            "https://www.marugoto-online.jp/a1/course/assets/sound/reference-word/A1W/A1W_0082.mp3",
            entries.first().audioUrl
        )
    }
}
