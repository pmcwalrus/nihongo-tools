package nihongo.tools

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import nihongo.tools.ui.NihongoToolsApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource("logo.png"),
        title = "Nihongo Tools",
        state = rememberWindowState(height = 720.dp)
    ) {
        NihongoToolsApp()
    }
}
