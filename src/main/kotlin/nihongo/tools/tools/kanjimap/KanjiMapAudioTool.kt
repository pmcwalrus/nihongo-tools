package nihongo.tools.tools.kanjimap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nihongo.tools.model.AppTool
import nihongo.tools.ui.ProgressSection
import nihongo.tools.ui.RememberingDirectoryPickerRow
import nihongo.tools.ui.ScrollableContent
import nihongo.tools.ui.ToolScaffold
import java.io.File

class KanjiMapAudioTool(
    private val service: KanjiMapAudioService = KanjiMapAudioService()
) : AppTool {
    override val id: String = "kanji-map-audio-thief"
    override val title: String = "The Kanji Map Audio Thief"
    override val description: String = "Загружает список слов с аудио для выбранного кандзи со страницы The Kanji Map и позволяет скачать отмеченные MP3."

    @Composable
    override fun Content(onBack: () -> Unit) {
        val scope = rememberCoroutineScope()
        var outputDirectory by remember { mutableStateOf<File?>(null) }
        var kanji by remember { mutableStateOf("") }
        var progress by remember { mutableFloatStateOf(0f) }
        var status by remember { mutableStateOf("Выберите папку, введите кандзи и нажмите `Получить слова`.") }
        var isLoading by remember { mutableStateOf(false) }
        var isDownloading by remember { mutableStateOf(false) }
        val entries = remember { mutableStateListOf<KanjiMapAudioEntry>() }
        val selectedUrls = remember { mutableStateMapOf<String, Boolean>() }
        val itemStatuses = remember { mutableStateMapOf<String, String>() }

        val selectedEntries = entries.filter { selectedUrls[it.audioUrl] == true }

        ToolScaffold(
            title = title,
            description = description,
            onBack = onBack
        ) {
            ScrollableContent {
                RememberingDirectoryPickerRow(
                    label = "Папка для MP3",
                    directory = outputDirectory,
                    buttonText = "Выбрать папку",
                    dialogTitle = "Выберите папку для MP3",
                    onDirectoryPicked = { outputDirectory = it },
                    onClear = { outputDirectory = null }
                )

                OutlinedTextField(
                    value = kanji,
                    onValueChange = { kanji = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Кандзи") },
                    supportingText = { Text("Например: 食") }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (outputDirectory == null) {
                                status = "Сначала выберите папку для сохранения."
                                return@Button
                            }
                            if (kanji.trim().isEmpty()) {
                                status = "Введите кандзи."
                                return@Button
                            }

                            isLoading = true
                            progress = 0f
                            status = "Загружаю страницу The Kanji Map..."
                            entries.clear()
                            selectedUrls.clear()
                            itemStatuses.clear()

                            scope.launch {
                                runCatching {
                                    progress = 0.15f
                                    withContext(Dispatchers.IO) { service.fetchEntries(kanji) }
                                }.onSuccess { fetchedEntries ->
                                    entries.addAll(fetchedEntries)
                                    fetchedEntries.forEach { selectedUrls[it.audioUrl] = true }
                                    progress = 1f
                                    status = if (fetchedEntries.isEmpty()) {
                                        "На странице не найдено слов с доступным аудио."
                                    } else {
                                        "Найдено слов с аудио: ${fetchedEntries.size}."
                                    }
                                }.onFailure { error ->
                                    progress = 0f
                                    status = error.message ?: "Не удалось получить список слов."
                                }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading && !isDownloading
                    ) {
                        Text(if (isLoading) "Загрузка..." else "Получить слова")
                    }

                    Button(
                        onClick = {
                            entries.forEach { selectedUrls[it.audioUrl] = true }
                            status = "Выделены все слова."
                        },
                        enabled = entries.isNotEmpty() && !isLoading && !isDownloading
                    ) {
                        Text("Отметить все")
                    }

                    Button(
                        onClick = {
                            entries.forEach { selectedUrls[it.audioUrl] = false }
                            status = "Выделение снято."
                        },
                        enabled = entries.isNotEmpty() && !isLoading && !isDownloading
                    ) {
                        Text("Снять все")
                    }
                }

                Button(
                    onClick = {
                        val targetDirectory = outputDirectory
                        if (targetDirectory == null) {
                            status = "Сначала выберите папку для сохранения."
                            return@Button
                        }
                        if (selectedEntries.isEmpty()) {
                            status = "Отметьте хотя бы одно слово."
                            return@Button
                        }

                        isDownloading = true
                        progress = 0f
                        selectedEntries.forEach { itemStatuses[it.audioUrl] = "В очереди" }

                        scope.launch {
                            var savedCount = 0
                            selectedEntries.forEachIndexed { index, entry ->
                                itemStatuses[entry.audioUrl] = "Скачивание..."
                                status = "Скачивание ${index + 1} из ${selectedEntries.size}: ${entry.downloadName}"
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        service.downloadEntry(entry, targetDirectory)
                                    }
                                }.onSuccess { file ->
                                    savedCount += 1
                                    itemStatuses[entry.audioUrl] = "Сохранено: ${file.name}"
                                }.onFailure { error ->
                                    itemStatuses[entry.audioUrl] = error.message ?: "Ошибка скачивания"
                                }
                                progress = (index + 1).toFloat() / selectedEntries.size
                            }

                            status = "Скачано файлов: $savedCount из ${selectedEntries.size}. Папка: ${targetDirectory.absolutePath}"
                            isDownloading = false
                        }
                    },
                    enabled = selectedEntries.isNotEmpty() && !isLoading && !isDownloading
                ) {
                    Text(
                        if (isDownloading) "Скачивание..."
                        else "Скачать отмеченные (${selectedEntries.size})"
                    )
                }

                ProgressSection(progress = progress, status = status)

                if (entries.isNotEmpty()) {
                    Text(
                        text = "Слова с аудио",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    KanjiMapAudioTable(
                        entries = entries,
                        selectedUrls = selectedUrls,
                        itemStatuses = itemStatuses,
                        enabled = !isLoading && !isDownloading,
                        onToggle = { audioUrl, isChecked ->
                            selectedUrls[audioUrl] = isChecked
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun KanjiMapAudioTable(
    entries: List<KanjiMapAudioEntry>,
    selectedUrls: Map<String, Boolean>,
    itemStatuses: Map<String, String>,
    enabled: Boolean,
    onToggle: (String, Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp, max = 420.dp)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 14.dp)
        ) {
            item {
                KanjiMapAudioTableHeader()
            }
            items(entries, key = { it.audioUrl }) { entry ->
                KanjiMapAudioRow(
                    entry = entry,
                    checked = selectedUrls[entry.audioUrl] == true,
                    status = itemStatuses[entry.audioUrl].orEmpty(),
                    enabled = enabled,
                    onToggle = { onToggle(entry.audioUrl, it) }
                )
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(10.dp)
                .heightIn(min = 220.dp, max = 420.dp)
        )
    }
}

@Composable
private fun KanjiMapAudioTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
            Text("Выб.")
        }
        Text("Слово", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
        Text("Перевод", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold)
        Text("Файл", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Статус", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun KanjiMapAudioRow(
    entry: KanjiMapAudioEntry,
    checked: Boolean,
    status: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle(it) },
                enabled = enabled
            )
        }
        Text(entry.wordWithReading, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodyMedium)
        Text(
            entry.translation.ifBlank { "Перевод не указан" },
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text("${entry.downloadName}.mp3", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(status.ifBlank { "Не скачано" }, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
    }
}
