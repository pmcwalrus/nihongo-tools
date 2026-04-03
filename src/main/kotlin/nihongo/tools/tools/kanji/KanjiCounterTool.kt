package nihongo.tools.tools.kanji

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nihongo.tools.model.AppTool
import nihongo.tools.ui.FilePickerRow
import nihongo.tools.ui.NeonButton
import nihongo.tools.ui.ProgressSection
import nihongo.tools.ui.RememberingDirectoryPickerRow
import nihongo.tools.ui.ScrollableContent
import nihongo.tools.ui.StatusCard
import nihongo.tools.ui.ToolScaffold
import nihongo.tools.ui.WebpunkTextFieldFrame
import nihongo.tools.util.FileDialogs
import java.io.File

class KanjiCounterTool(
    private val service: KanjiCounterService = KanjiCounterService()
) : AppTool {
    override val id: String = "kanji-counter"
    override val title: String = "Счетчик кандзи в файле"
    override val description: String = "Подсчитывает все кандзи в документе, поддерживает файл-исключение и сохраняет результат в CSV."

    @Composable
    override fun Content(onBack: () -> Unit) {
        val scope = rememberCoroutineScope()
        var sourceFile by remember { mutableStateOf<File?>(null) }
        var exclusionFile by remember { mutableStateOf<File?>(null) }
        var outputDirectory by remember { mutableStateOf<File?>(null) }
        var resultFolderName by remember { mutableStateOf("") }
        var progress by remember { mutableFloatStateOf(0f) }
        var status by remember { mutableStateOf("Выберите файл и папку для сохранения CSV.") }
        var resultSummary by remember { mutableStateOf<String?>(null) }
        var isRunning by remember { mutableStateOf(false) }

        ToolScaffold(
            title = title,
            description = description,
            onBack = onBack
        ) {
            ScrollableContent {
                FilePickerRow(
                    label = "Исходный файл",
                    file = sourceFile,
                    buttonText = "Выбрать файл",
                    onPick = { sourceFile = FileDialogs.chooseFile("Выберите документ для подсчета кандзи") }
                )

                FilePickerRow(
                    label = "Файл с исключениями",
                    file = exclusionFile,
                    buttonText = "Выбрать файл исключений",
                    onPick = { exclusionFile = FileDialogs.chooseFile("Выберите файл с исключениями") },
                    onClear = { exclusionFile = null }
                )

                RememberingDirectoryPickerRow(
                    label = "Папка для CSV",
                    directory = outputDirectory,
                    buttonText = "Выбрать папку",
                    dialogTitle = "Выберите папку для сохранения CSV",
                    onDirectoryPicked = { outputDirectory = it },
                    onClear = { outputDirectory = null }
                )

                WebpunkTextFieldFrame {
                    Text(
                        "OUTPUT PATCH",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
                    )
                    OutlinedTextField(
                        value = resultFolderName,
                        onValueChange = { resultFolderName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Имя подпапки для результата") },
                        supportingText = {
                            Text("Если оставить пустым, CSV сохранится прямо в выбранную при запуске папку.")
                        }
                    )
                }

                NeonButton(
                    text = if (isRunning) "Обработка..." else "Запустить подсчет",
                    enabled = !isRunning,
                    onClick = {
                        val currentSource = sourceFile
                        if (currentSource == null) {
                            status = "Нужно выбрать исходный файл."
                            return@NeonButton
                        }

                        val baseOutput = outputDirectory ?: run {
                            status = "Нужно выбрать папку для сохранения CSV."
                            return@NeonButton
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
                                    targetDirectory.mkdirs()
                                    service.countKanji(
                                        sourceFile = currentSource,
                                        exclusionFile = exclusionFile,
                                        outputDirectory = targetDirectory
                                    ) { currentProgress, currentStatus ->
                                        progress = currentProgress
                                        status = currentStatus
                                    }
                                }
                            }.onSuccess { result ->
                                resultSummary =
                                    "Сохранено: ${result.outputFile.absolutePath}\nУникальных кандзи: ${result.uniqueKanji}\nВсего вхождений: ${result.totalKanji}"
                            }.onFailure { error ->
                                status = error.message ?: "Произошла ошибка во время подсчета."
                            }
                            isRunning = false
                        }
                    }
                )

                ProgressSection(progress = progress, status = status)

                if (resultSummary != null) {
                    StatusCard(resultSummary!!)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
