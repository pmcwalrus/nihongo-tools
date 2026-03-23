package nihongo.tools.tools.marugoto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nihongo.tools.model.AppTool
import nihongo.tools.ui.ProgressSection
import nihongo.tools.ui.ScrollableContent
import nihongo.tools.ui.ToolScaffold
import nihongo.tools.util.FileDialogs
import java.io.File

class MarugotoAudioTool(
    private val service: MarugotoAudioService = MarugotoAudioService()
) : AppTool {
    override val id: String = "marugoto-audio"
    override val title: String = "Скачивание аудио из Marugoto"
    override val description: String = "Извлекает японское слово и MP3-ссылку из вставленного HTML-подобного текста и сохраняет файлы в выбранную папку."

    @Composable
    override fun Content(onBack: () -> Unit) {
        val scope = rememberCoroutineScope()
        var rawText by remember { mutableStateOf("") }
        var resultFolderName by remember { mutableStateOf("") }
        var progress by remember { mutableFloatStateOf(0f) }
        var status by remember { mutableStateOf("Вставьте фрагменты текста из Marugoto. Папка назначения будет запрошена при запуске.") }
        var resultSummary by remember { mutableStateOf<String?>(null) }
        var isRunning by remember { mutableStateOf(false) }

        ToolScaffold(
            title = title,
            description = description,
            onBack = onBack
        ) {
            ScrollableContent {
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    label = { Text("HTML-текст или фрагмент страницы") }
                )

                OutlinedTextField(
                    value = resultFolderName,
                    onValueChange = { resultFolderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Имя подпапки для MP3") },
                    supportingText = {
                        Text("Если оставить пустым, файлы сохранятся прямо в выбранную при запуске папку.")
                    }
                )

                Button(
                    onClick = {
                        if (rawText.isBlank()) {
                            status = "Нужно вставить текст."
                            return@Button
                        }

                        val baseOutput = FileDialogs.chooseDirectory("Выберите папку для аудиофайлов") ?: run {
                            status = "Скачивание отменено: папка не выбрана."
                            return@Button
                        }

                        val targetDirectory = resultFolderName.trim().takeIf { it.isNotEmpty() }?.let {
                            File(baseOutput, it)
                        } ?: baseOutput

                        isRunning = true
                        progress = 0f
                        resultSummary = null
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    val entries = service.parseEntries(rawText)
                                    if (entries.isEmpty()) {
                                        error("Не удалось найти подходящие блоки `_jpn` + `data-audio`.")
                                    }

                                    service.downloadEntries(
                                        entries = entries,
                                        outputDirectory = targetDirectory.apply { mkdirs() }
                                    ) { currentProgress, currentStatus ->
                                        progress = currentProgress
                                        status = currentStatus
                                    }
                                }
                            }.onSuccess { result ->
                                resultSummary =
                                    "Скачано файлов: ${result.savedFiles}\nПапка: ${result.outputDirectory.absolutePath}"
                            }.onFailure { error ->
                                status = error.message ?: "Ошибка при скачивании аудио."
                            }
                            isRunning = false
                        }
                    },
                    enabled = !isRunning
                ) {
                    Text(if (isRunning) "Скачивание..." else "Скачать MP3")
                }

                ProgressSection(progress = progress, status = status)

                if (resultSummary != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Text(
                            text = resultSummary!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
