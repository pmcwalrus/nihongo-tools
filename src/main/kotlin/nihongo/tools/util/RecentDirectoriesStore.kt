package nihongo.tools.util

import java.io.File
import java.util.prefs.Preferences

object RecentDirectoriesStore {
    private const val Key = "recent_directories"
    private const val Separator = "\n"
    private const val Limit = 8
    private val preferences = Preferences.userRoot().node("nihongo-tools")

    fun load(): List<File> {
        return preferences.get(Key, "")
            .split(Separator)
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map(::File)
            .filter(File::exists)
            .filter(File::isDirectory)
            .distinctBy { it.absolutePath }
            .take(Limit)
            .toList()
    }

    fun remember(directory: File) {
        val normalized = directory.absoluteFile
        val updated = buildList {
            add(normalized)
            addAll(load().filterNot { it.absolutePath == normalized.absolutePath })
        }.take(Limit)

        preferences.put(
            Key,
            updated.joinToString(Separator) { it.absolutePath }
        )
    }
}
