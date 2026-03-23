package nihongo.tools.model

import androidx.compose.runtime.Composable

interface AppTool {
    val id: String
    val title: String
    val description: String

    @Composable
    fun Content(onBack: () -> Unit)
}

