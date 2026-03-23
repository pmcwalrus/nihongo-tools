package nihongo.tools.tools.kanji

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KanjiCounterServiceTest {
    private val service = KanjiCounterService()

    @Test
    fun `detects kanji by HAN script`() {
        assertTrue(service.isKanji('漢'))
        assertTrue(service.isKanji('日'))
        assertFalse(service.isKanji('あ'))
        assertFalse(service.isKanji('A'))
    }
}

