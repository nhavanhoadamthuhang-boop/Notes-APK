package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.BackupData
import com.example.data.backup.JsonBackupHelper
import com.example.data.local.NoteEntity
import com.example.data.repository.ImportSummary
import com.example.ui.NotesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JsonImportExportDialog(
    viewModel: NotesViewModel,
    singleNoteToExport: NoteEntity? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(if (singleNoteToExport != null) 0 else 0) }
    var isProcessing by remember { mutableStateOf(false) }
    val autoBackupState by viewModel.autoBackupState.collectAsStateWithLifecycle()

    // Export State
    var exportedJsonText by remember { mutableStateOf("") }
    var exportStats by remember { mutableStateOf<Pair<Int, Int>?>(null) } // notes, comments
    var showJsonPreview by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Import State
    var importedRawText by remember { mutableStateOf("") }
    var parsedBackupData by remember { mutableStateOf<BackupData?>(null) }
    var parseErrorMessage by remember { mutableStateOf<String?>(null) }
    var replaceExistingData by remember { mutableStateOf(false) }
    var importSuccessSummary by remember { mutableStateOf<ImportSummary?>(null) }

    // Default File Name for Export
    val timestampStr = remember {
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
    val defaultFileName = remember(singleNoteToExport) {
        if (singleNoteToExport != null) {
            val sanitizedTitle = singleNoteToExport.title
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                .take(20)
            "GhiChu_${sanitizedTitle}_$timestampStr.json"
        } else {
            "GhiChu_Backup_$timestampStr.json"
        }
    }

    // Prepare export JSON on open or when singleNoteToExport changes
    LaunchedEffect(singleNoteToExport) {
        isProcessing = true
        withContext(Dispatchers.IO) {
            val json = if (singleNoteToExport != null) {
                viewModel.exportSingleNoteJson(singleNoteToExport.id) ?: ""
            } else {
                viewModel.exportAllNotesJson()
            }
            val parseResult = JsonBackupHelper.parseJson(json)
            val stats = if (parseResult.isSuccess) {
                val data = parseResult.getOrThrow()
                Pair(data.notes.size, data.totalCommentsCount)
            } else {
                Pair(0, 0)
            }
            withContext(Dispatchers.Main) {
                exportedJsonText = json
                exportStats = stats
                isProcessing = false
            }
        }
    }

    // SA/Document Create Launcher for Saving JSON
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && exportedJsonText.isNotBlank()) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(exportedJsonText.toByteArray(Charsets.UTF_8))
                    }
                    withContext(Dispatchers.Main) {
                        exportSuccessMessage = "Đã lưu tệp JSON thành công vào thiết bị!"
                        Toast.makeText(context, "Đã lưu tệp JSON thành công!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lỗi khi lưu tệp: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // File Picker Launcher for Importing JSON
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    } ?: ""
                    withContext(Dispatchers.Main) {
                        importedRawText = content
                        val result = JsonBackupHelper.parseJson(content)
                        if (result.isSuccess) {
                            parsedBackupData = result.getOrThrow()
                            parseErrorMessage = null
                        } else {
                            parsedBackupData = null
                            parseErrorMessage = result.exceptionOrNull()?.message ?: "Tệp JSON không hợp lệ"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        parsedBackupData = null
                        parseErrorMessage = "Không thể đọc tệp: ${e.message}"
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .testTag("json_import_export_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DataObject,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (singleNoteToExport != null) "Xuất ghi chú sang JSON" else "Nhập / Xuất tệp JSON",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Tab Selection (Export vs Import)
                if (singleNoteToExport == null) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .padding(bottom = 16.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Xuất tệp (.json)", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Nhập tệp (.json)", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Đang xử lý dữ liệu JSON...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else if (selectedTab == 0) {
                    // ==========================================
                    // EXPORT TAB
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Export Info Card
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (singleNoteToExport != null)
                                            "Xuất ghi chú \"${singleNoteToExport.title}\""
                                        else "Sao lưu toàn bộ ghi chú & bình luận",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val noteCount = exportStats?.first ?: 0
                                val commentCount = exportStats?.second ?: 0
                                Text(
                                    text = "• Số lượng ghi chú: $noteCount\n• Số lượng bình luận & phản hồi: $commentCount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tệp tin định dạng JSON chuẩn chứa đầy đủ thông tin tiêu đề, nội dung, thời gian, người gửi và các luồng phản hồi phân cấp.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (exportSuccessMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = exportSuccessMessage!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        // Primary Action: Save to File
                        Button(
                            onClick = {
                                saveFileLauncher.launch(defaultFileName)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_json_file"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lưu tệp .json vào máy", fontWeight = FontWeight.Bold)
                        }

                        // Secondary Actions: Share & Copy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    try {
                                        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                                        val cacheFile = File(exportDir, defaultFileName)
                                        cacheFile.writeText(exportedJsonText)

                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            cacheFile
                                        )

                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }

                                        val shareIntent = Intent.createChooser(sendIntent, "Chia sẻ tệp JSON ghi chú").apply {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(shareIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Không thể chia sẻ tệp: ${e.localizedMessage ?: "Lỗi hệ thống"}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chia sẻ")
                            }

                            FilledTonalButton(
                                onClick = {
                                    try {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Ghi chú JSON", exportedJsonText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Đã sao chép văn bản JSON vào khay nhớ tạm!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Dung lượng quá lớn để sao chép vào khay nhớ tạm, vui lòng dùng tính năng Lưu tệp hoặc Chia sẻ!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sao chép")
                            }
                        }

                        // JSON Preview Toggle
                        OutlinedButton(
                            onClick = { showJsonPreview = !showJsonPreview },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (showJsonPreview) "Ẩn nội dung JSON" else "Xem trước mã JSON")
                        }

                        AnimatedVisibility(visible = showJsonPreview) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = exportedJsonText.take(1500) + if (exportedJsonText.length > 1500) "\n\n... (đã rút gọn xem trước)" else "",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Auto Daily Backup Settings Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_json_auto_backup_quick"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (autoBackupState.isEnabled)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = null,
                                            tint = if (autoBackupState.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Tự động sao lưu hàng ngày",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (autoBackupState.isEnabled) "Đang bật sao lưu định kỳ trên máy" else "Đang tắt tự động sao lưu",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (autoBackupState.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = autoBackupState.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            viewModel.setAutoBackupEnabled(isChecked)
                                            Toast.makeText(
                                                context,
                                                if (isChecked) "Đã bật tự động sao lưu JSON hàng ngày" else "Đã tắt tự động sao lưu",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.testTag("switch_json_auto_backup")
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // IMPORT TAB
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // File picker button
                        Button(
                            onClick = {
                                openFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_select_json_file"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chọn tệp .json từ thiết bị", fontWeight = FontWeight.Bold)
                        }

                        // Or Paste text directly
                        OutlinedTextField(
                            value = importedRawText,
                            onValueChange = { newText ->
                                importedRawText = newText
                                if (newText.isNotBlank()) {
                                    val result = JsonBackupHelper.parseJson(newText)
                                    if (result.isSuccess) {
                                        parsedBackupData = result.getOrThrow()
                                        parseErrorMessage = null
                                    } else {
                                        parsedBackupData = null
                                        parseErrorMessage = result.exceptionOrNull()?.message ?: "JSON không hợp lệ"
                                    }
                                } else {
                                    parsedBackupData = null
                                    parseErrorMessage = null
                                }
                            },
                            label = { Text("Hoặc dán nội dung JSON vào đây") },
                            placeholder = { Text("{\n  \"notes\": [...]\n}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 150.dp)
                                .testTag("import_json_textfield"),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Validation Result Badge
                        if (parsedBackupData != null) {
                            val data = parsedBackupData!!
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Tệp JSON hợp lệ!",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Phát hiện ${data.notes.size} ghi chú và ${data.totalCommentsCount} bình luận/phản hồi sẵn sàng nhập.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else if (parseErrorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = parseErrorMessage!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Import Mode Selection
                        if (parsedBackupData != null) {
                            Text(
                                text = "Chế độ nhập:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    // Option 1: Merge / Append
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { replaceExistingData = false }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = !replaceExistingData,
                                            onClick = { replaceExistingData = false }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Thêm vào danh sách hiện có", fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "Giữ nguyên các ghi chú hiện tại, thêm các ghi chú mới từ tệp JSON.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Option 2: Replace all
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { replaceExistingData = true }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = replaceExistingData,
                                            onClick = { replaceExistingData = true }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Thay thế toàn bộ (Ghi đè)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                                            Text(
                                                "Xoá tất cả ghi chú hiện có và nạp dữ liệu hoàn toàn từ tệp JSON.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Confirm Import Button
                            Button(
                                onClick = {
                                    val rawJson = importedRawText
                                    if (rawJson.isNotBlank()) {
                                        isProcessing = true
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val result = viewModel.importJsonData(rawJson, replaceExistingData)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                if (result.isSuccess) {
                                                    val summary = result.getOrThrow()
                                                    importSuccessSummary = summary
                                                    Toast.makeText(
                                                        context,
                                                        "Đã nhập thành công ${summary.notesImported} ghi chú, ${summary.commentsImported} bình luận và ${summary.repliesImported} phản hồi!",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } else {
                                                    parseErrorMessage = "Lỗi khi nhập dữ liệu: ${result.exceptionOrNull()?.message}"
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_confirm_import"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (replaceExistingData) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (replaceExistingData) "Xác nhận ghi đè & Nhập" else "Bắt đầu nhập dữ liệu",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Success Summary Card
                        if (importSuccessSummary != null) {
                            val summary = importSuccessSummary!!
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text(
                                            text = "Nhập dữ liệu thành công!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "• Đã nạp: ${summary.notesImported} ghi chú\n• Đã nạp: ${summary.commentsImported} bình luận gốc\n• Đã nạp: ${summary.repliesImported} phản hồi lồng nhau",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_close_button")
            ) {
                Text("Xong")
            }
        }
    )
}
