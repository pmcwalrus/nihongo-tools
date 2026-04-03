package nihongo.tools.tools

import nihongo.tools.model.AppTool
import nihongo.tools.tools.kanji.KanjiCounterTool
import nihongo.tools.tools.kanjimap.KanjiMapAudioTool
import nihongo.tools.tools.marugoto.MarugotoAudioTool

object ToolCatalog {
    val tools: List<AppTool> = listOf(
        KanjiCounterTool(),
        MarugotoAudioTool(),
        KanjiMapAudioTool()
    )
}
