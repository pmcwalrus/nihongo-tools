package nihongo.tools.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nihongo.tools.util.FileDialogs
import nihongo.tools.util.RecentDirectoriesStore
import java.io.File

@Composable
fun RememberingDirectoryPickerRow(
    label: String,
    directory: File?,
    buttonText: String,
    dialogTitle: String,
    onDirectoryPicked: (File) -> Unit,
    onClear: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val initialDirectory = directory
                        ?: RecentDirectoriesStore.load().firstOrNull()
                        ?: File(System.getProperty("user.home"))
                    FileDialogs.chooseDirectory(dialogTitle, initialDirectory)?.let {
                        RecentDirectoriesStore.remember(it)
                        onDirectoryPicked(it)
                    }
                }
            ) {
                Text(buttonText)
            }
            if (onClear != null && directory != null) {
                Button(onClick = onClear) {
                    Text("Очистить")
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(directory?.absolutePath ?: "Не выбрано", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun FilePickerRow(
    label: String,
    file: File?,
    buttonText: String,
    onPick: () -> Unit,
    onClear: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPick) {
                Text(buttonText)
            }
            if (onClear != null && file != null) {
                Button(onClick = onClear) {
                    Text("Очистить")
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(file?.absolutePath ?: "Не выбрано", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ProgressSection(progress: Float, status: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Text(status, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ScrollableContent(content: @Composable ColumnScope.() -> Unit) {
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 14.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(10.dp)
        )
    }
}
