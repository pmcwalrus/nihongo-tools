package nihongo.tools.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

object FileDialogs {
    fun chooseFile(title: String): File? {
        return runCatching {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            JFileChooser().apply {
                dialogTitle = title
                fileSelectionMode = JFileChooser.FILES_ONLY
            }.let { chooser ->
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
            }
        }.getOrElse {
            val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
            dialog.isVisible = true
            dialog.files.firstOrNull()
        }
    }

    fun chooseDirectory(title: String, initialDirectory: File? = null): File? {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        return JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false
            approveButtonText = "Выбрать"
            approveButtonToolTipText = "Выбрать текущую папку"
            initialDirectory?.takeIf { it.exists() && it.isDirectory }?.let {
                currentDirectory = it
                selectedFile = it
            }
        }.let { chooser ->
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
        }
    }
}
