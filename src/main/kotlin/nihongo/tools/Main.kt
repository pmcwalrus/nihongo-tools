package nihongo.tools

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import nihongo.tools.ui.NihongoToolsApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource("logo.png"),
        title = "Nihongo Tools"
    ) {
        NihongoToolsApp()
    }
}
