package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.backup.JsonBackupHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AutoBackupState(
    val isEnabled: Boolean = false,
    val lastBackupTimestamp: Long = 0L,
    val lastBackupFileName: String? = null,
    val totalBackupsCount: Int = 0,
    val isBackingUp: Boolean = false,
    val lastBackupError: String? = null
)

data class BackupFileInfo(
    val file: File,
    val fileName: String,
    val formattedSize: String,
    val formattedDate: String,
    val timestamp: Long
)

class AutoBackupManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _backupState = MutableStateFlow(loadStateFromPrefs())
    val backupState: StateFlow<AutoBackupState> = _backupState.asStateFlow()

    init {
        updateFilesCount()
    }

    private fun loadStateFromPrefs(): AutoBackupState {
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        val lastTimestamp = prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
        val lastFileName = prefs.getString(KEY_LAST_FILENAME, null)

        return AutoBackupState(
            isEnabled = enabled,
            lastBackupTimestamp = lastTimestamp,
            lastBackupFileName = lastFileName,
            totalBackupsCount = 0,
            isBackingUp = false,
            lastBackupError = null
        )
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _backupState.value = _backupState.value.copy(isEnabled = enabled)
    }

    fun getBackupDirectory(): File {
        val dir = File(context.filesDir, "backups")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getBackupFiles(): List<BackupFileInfo> {
        val dir = getBackupDirectory()
        val files = dir.listFiles { file -> file.isFile && file.name.endsWith(".json") } ?: emptyArray()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        return files.sortedByDescending { it.lastModified() }.map { file ->
            val sizeKb = file.length() / 1024.0
            val formattedSize = if (sizeKb < 1024) {
                String.format(Locale.getDefault(), "%.1f KB", sizeKb)
            } else {
                String.format(Locale.getDefault(), "%.2f MB", sizeKb / 1024.0)
            }

            BackupFileInfo(
                file = file,
                fileName = file.name,
                formattedSize = formattedSize,
                formattedDate = dateFormat.format(Date(file.lastModified())),
                timestamp = file.lastModified()
            )
        }
    }

    private fun updateFilesCount() {
        val count = getBackupDirectory().listFiles { file -> file.isFile && file.name.endsWith(".json") }?.size ?: 0
        _backupState.value = _backupState.value.copy(totalBackupsCount = count)
    }

    fun isBackupNeededToday(): Boolean {
        if (!_backupState.value.isEnabled) return false
        val lastTimestamp = _backupState.value.lastBackupTimestamp
        if (lastTimestamp == 0L) return true

        val lastCal = Calendar.getInstance().apply { timeInMillis = lastTimestamp }
        val nowCal = Calendar.getInstance()

        return (lastCal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR) ||
                lastCal.get(Calendar.DAY_OF_YEAR) != nowCal.get(Calendar.DAY_OF_YEAR))
    }

    suspend fun checkAndRunDailyBackup(repository: NoteRepository): Boolean = withContext(Dispatchers.IO) {
        if (!isBackupNeededToday()) return@withContext false

        val result = performBackup(repository, isAutomatic = true)
        return@withContext result.isSuccess
    }

    suspend fun performBackup(
        repository: NoteRepository,
        isAutomatic: Boolean = false
    ): Result<File> = withContext(Dispatchers.IO) {
        _backupState.value = _backupState.value.copy(isBackingUp = true, lastBackupError = null)

        try {
            val notes = repository.getAllNotesDirect()
            val comments = repository.getAllCommentsDirect()
            val jsonString = JsonBackupHelper.exportAllDataToJson(notes, comments)

            val dir = getBackupDirectory()
            val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val dateStr = timestampFormat.format(Date())
            val prefix = if (isAutomatic) "autobackup_daily" else "manual_backup"
            val fileName = "${prefix}_$dateStr.json"
            val backupFile = File(dir, fileName)

            backupFile.writeText(jsonString)

            // Cleanup old automatic backups if more than MAX_SAVED_BACKUPS (keep last 14)
            cleanOldBackups(dir, MAX_SAVED_BACKUPS)

            val now = System.currentTimeMillis()
            prefs.edit()
                .putLong(KEY_LAST_TIMESTAMP, now)
                .putString(KEY_LAST_FILENAME, fileName)
                .apply()

            val totalCount = dir.listFiles { f -> f.isFile && f.name.endsWith(".json") }?.size ?: 0

            _backupState.value = _backupState.value.copy(
                lastBackupTimestamp = now,
                lastBackupFileName = fileName,
                totalBackupsCount = totalCount,
                isBackingUp = false,
                lastBackupError = null
            )

            Result.success(backupFile)
        } catch (e: Exception) {
            _backupState.value = _backupState.value.copy(
                isBackingUp = false,
                lastBackupError = e.localizedMessage ?: "Lỗi khi tạo bản sao lưu"
            )
            Result.failure(e)
        }
    }

    suspend fun restoreFromBackupFile(
        file: File,
        repository: NoteRepository,
        replaceExisting: Boolean
    ): Result<ImportSummary> = withContext(Dispatchers.IO) {
        try {
            val jsonString = file.readText()
            val parseResult = JsonBackupHelper.parseJson(jsonString)
            if (parseResult.isFailure) {
                return@withContext Result.failure(
                    parseResult.exceptionOrNull() ?: Exception("Tệp sao lưu không hợp lệ")
                )
            }
            val backupData = parseResult.getOrThrow()
            val summary = repository.importBackupData(backupData, replaceExisting)
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteBackupFile(file: File): Boolean {
        val deleted = file.delete()
        updateFilesCount()
        return deleted
    }

    private fun cleanOldBackups(dir: File, keepCount: Int) {
        val autoBackups = dir.listFiles { file ->
            file.isFile && file.name.startsWith("autobackup_daily") && file.name.endsWith(".json")
        } ?: return

        if (autoBackups.size > keepCount) {
            val sorted = autoBackups.sortedBy { it.lastModified() }
            val toDeleteCount = autoBackups.size - keepCount
            for (i in 0 until toDeleteCount) {
                sorted[i].delete()
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "auto_backup_prefs"
        private const val KEY_ENABLED = "auto_backup_enabled"
        private const val KEY_LAST_TIMESTAMP = "auto_backup_last_timestamp"
        private const val KEY_LAST_FILENAME = "auto_backup_last_filename"
        private const val MAX_SAVED_BACKUPS = 14
    }
}
