package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.PushPin
import com.example.data.repository.ThemeMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CommentEntity
import com.example.ui.NotesViewModel
import com.example.ui.components.CommentThreadItem
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DiamondGoalDialog
import com.example.ui.components.DiamondStoreDialog
import com.example.ui.components.DiamondTopBarBadge
import com.example.ui.components.EditCommentDialog
import com.example.ui.components.JsonImportExportDialog
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StreakBadge
import com.example.ui.components.StreakDialog
import com.example.ui.theme.PinGold
import com.example.ui.theme.PinGoldContainer
import com.example.ui.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier,
    isEmbeddedInSplitPane: Boolean = false
) {
    val note by viewModel.selectedNote.collectAsStateWithLifecycle()
    val commentsState by viewModel.commentsUiState.collectAsStateWithLifecycle()
    val replyingTo by viewModel.replyingTo.collectAsStateWithLifecycle()
    val editingNote by viewModel.editingNote.collectAsStateWithLifecycle()
    val rewardState by viewModel.rewardState.collectAsStateWithLifecycle()
    val rateLimitWarning by viewModel.rateLimitWarning.collectAsStateWithLifecycle()
    val autoBackupState by viewModel.autoBackupState.collectAsStateWithLifecycle()
    val streakState by viewModel.streakState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val activityStats by viewModel.activityStats.collectAsStateWithLifecycle()

    var commentInput by remember { mutableStateOf("") }
    var authorNameInput by remember { mutableStateOf("Bạn") }
    var showAuthorEditDialog by remember { mutableStateOf(false) }
    var showDiamondGoalDialog by remember { mutableStateOf(false) }
    var showDiamondStoreDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showStreakDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Deletion confirmation states
    var showDeleteNoteConfirm by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<CommentEntity?>(null) }
    var replyToDelete by remember { mutableStateOf<CommentEntity?>(null) }

    // Comment/Reply editing states
    var commentToEdit by remember { mutableStateOf<CommentEntity?>(null) }
    var replyToEdit by remember { mutableStateOf<CommentEntity?>(null) }

    // JSON export state
    var showExportJsonDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    if (note == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentNote = note!!

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentNote.title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.selectNote(null) },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = if (isEmbeddedInSplitPane) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEmbeddedInSplitPane) "Đóng ghi chú" else "Quay lại"
                        )
                    }
                },
                actions = {
                    DiamondTopBarBadge(
                        rewardState = rewardState,
                        onClick = { showDiamondStoreDialog = true }
                    )

                    StreakBadge(
                        streakState = streakState,
                        onClick = { showStreakDialog = true },
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("detail_btn_toggle_theme")
                    ) {
                        Icon(
                            imageVector = if (themeMode == ThemeMode.DARK) Icons.Default.WbSunny else Icons.Default.DarkMode,
                            contentDescription = if (themeMode == ThemeMode.DARK) "Chuyển sang Chế độ sáng" else "Chuyển sang Chế độ tối",
                            tint = if (themeMode == ThemeMode.DARK) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Pin Note Button
                    IconButton(
                        onClick = { viewModel.toggleNotePinned(currentNote) },
                        modifier = Modifier.testTag("detail_pin_note_button")
                    ) {
                        Icon(
                            imageVector = if (currentNote.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (currentNote.isPinned) "Bỏ ghim ghi chú" else "Ghim ghi chú",
                            tint = if (currentNote.isPinned) PinGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // More Menu
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("detail_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Tuỳ chọn"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa ghi chú") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                viewModel.startEditNote(currentNote)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cửa hàng gói nạp (Nâng cấp hạn mức)") },
                            leadingIcon = { Icon(Icons.Default.Diamond, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                menuExpanded = false
                                showDiamondStoreDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Đổi tên người gửi ($authorNameInput)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showAuthorEditDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Xuất ghi chú sang tệp JSON") },
                            leadingIcon = { Icon(Icons.Default.DataObject, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                menuExpanded = false
                                showExportJsonDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cài đặt & Tự động sao lưu") },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                menuExpanded = false
                                showSettingsDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Xoá ghi chú này", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                showDeleteNoteConfirm = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Comment & Reply input bar
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Replying to target notification banner
                    AnimatedVisibility(
                        visible = replyingTo != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        replyingTo?.let { target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Đang trả lời @${target.authorName}: \"${target.content.take(30)}...\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                IconButton(
                                    onClick = { viewModel.setReplyingTo(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Huỷ trả lời",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Rate limit & Diamond Reward mini banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "+1 💎 / bình luận & phản hồi",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (rewardState.commentsInCurrentMinute >= (rewardState.maxCommentsPerMinute - 4)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${rewardState.commentsInCurrentMinute}/${rewardState.maxCommentsPerMinute} cmt/phút",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (rewardState.commentsInCurrentMinute >= (rewardState.maxCommentsPerMinute - 4)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Trường "Tên của bạn"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = authorNameInput,
                            onValueChange = { authorNameInput = it },
                            label = { Text("Tên của bạn", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("Nhập tên hiển thị khi bình luận/phản hồi...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Tên của bạn",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (authorNameInput.isNotBlank()) {
                                    IconButton(
                                        onClick = { authorNameInput = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Xoá tên",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("author_name_input_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // Input bar row: Nội dung bình luận / phản hồi + Nút gửi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = {
                                Text(
                                    if (replyingTo != null) "Nhập phản hồi cho @${replyingTo?.authorName}..."
                                    else "Nhập bình luận của bạn..."
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input_field"),
                            maxLines = 4,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        )

                        // Send Button
                        IconButton(
                            onClick = {
                                if (commentInput.isNotBlank()) {
                                    val finalAuthorName = authorNameInput.trim().ifBlank { "Bạn" }
                                    viewModel.addCommentOrReply(
                                        content = commentInput,
                                        authorName = finalAuthorName
                                    )
                                    commentInput = ""
                                }
                            },
                            enabled = commentInput.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (commentInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .testTag("send_comment_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi",
                                tint = if (commentInput.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("comments_lazy_column"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Note Card Section (Tiêu đề ghi chú & Mô tả ghi chú & Ghim ghi chú)
            item(key = "note_card_header") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_note_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentNote.isPinned) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (currentNote.isPinned) 1.5.dp else 1.dp,
                        color = if (currentNote.isPinned) PinGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Title & Pinned Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GHI CHÚ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )

                            if (currentNote.isPinned) {
                                Surface(
                                    shape = CircleShape,
                                    color = PinGoldContainer,
                                    modifier = Modifier.testTag("note_pinned_indicator")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = null,
                                            tint = PinGold,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "GHI CHÚ ĐÃ GHIM",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PinGold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tiêu đề ghi chú
                        Text(
                            text = currentNote.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mô tả ghi chú
                        Text(
                            text = currentNote.description.ifBlank { "(Không có mô tả chi tiết)" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )

                        // Category & Tags Row
                        if (currentNote.category.isNotBlank() || currentNote.tagList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (currentNote.category.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.testTag("detail_category_badge")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = currentNote.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                currentNote.tagList.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.testTag("detail_tag_badge_$tag")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = "#$tag",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Timestamp & Edit Button
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "Thời gian ghi chú",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = DateUtils.formatDetailedElapsedTime(currentNote.updatedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                    fontSize = 11.5.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.startEditNote(currentNote) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("edit_note_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Chỉnh sửa ghi chú",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sửa ghi chú")
                                }
                            }
                        }
                    }
                }
            }

            // Comments Section Header
            item(key = "comments_section_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
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
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Bình luận & Phản hồi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${commentsState.totalCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Info indicator about 200 items/page
                        Text(
                            text = "200 mục/trang",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (commentsState.totalCount > 0) {
                        Text(
                            text = "Đang hiển thị ${commentsState.displayedCount} trong tổng số ${commentsState.totalCount} bình luận & phản hồi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // CRITICAL REQUIREMENT:
            // "Có thể trả lời 200 bình luận và phản hồi cho mỗi trang sẽ xuất hiện nút Tải bình luận trước."
            // When there are earlier comments exceeding current page of 200:
            if (commentsState.hasEarlierComments) {
                item(key = "load_earlier_comments_btn") {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { viewModel.loadEarlierComments() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("load_earlier_comments_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardDoubleArrowUp,
                                    contentDescription = "Tải bình luận trước",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tải bình luận trước (${commentsState.remainingEarlierCount} bình luận cũ hơn)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mỗi trang hiển thị 200 bình luận & phản hồi theo quy định",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Comments Empty State
            if (commentsState.commentThreads.isEmpty()) {
                item(key = "empty_comments") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Chưa có bình luận nào",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hãy là người đầu tiên để lại ý kiến hoặc phản hồi dưới ghi chú này!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Render threaded comments (pinned comments first, pinned replies first)
                items(
                    items = commentsState.commentThreads,
                    key = { "thread_${it.comment.id}" }
                ) { thread ->
                    CommentThreadItem(
                        thread = thread,
                        onReplyToComment = { target ->
                            viewModel.setReplyingTo(target)
                        },
                        onTogglePinComment = { target ->
                            viewModel.toggleCommentPinned(target)
                        },
                        onEditComment = { target ->
                            commentToEdit = target
                        },
                        onDeleteComment = { target ->
                            commentToDelete = target
                        },
                        onEditReply = { target ->
                            replyToEdit = target
                        },
                        onDeleteReply = { target ->
                            replyToDelete = target
                        }
                    )
                }
            }
        }
    }

    // Confirm Delete Note Dialog
    if (showDeleteNoteConfirm) {
        ConfirmDeleteDialog(
            title = "Xoá vĩnh viễn ghi chú?",
            message = "Bạn có chắc chắn muốn xoá ghi chú \"${currentNote.title}\" cùng toàn bộ các bình luận và phản hồi liên quan không? Hành động này không thể hoàn tác.",
            confirmButtonText = "Xoá ghi chú",
            onConfirm = {
                showDeleteNoteConfirm = false
                viewModel.deleteNote(currentNote.id)
            },
            onDismiss = { showDeleteNoteConfirm = false }
        )
    }

    // Confirm Delete Comment Dialog
    val currentCommentToDelete = commentToDelete
    if (currentCommentToDelete != null) {
        ConfirmDeleteDialog(
            title = "Xoá bình luận vĩnh viễn?",
            message = "Bạn có chắc chắn muốn xoá bình luận này của \"${currentCommentToDelete.authorName}\"? Tất cả các phản hồi liên quan trong luồng cũng sẽ bị xoá vĩnh viễn.",
            confirmButtonText = "Xoá bình luận",
            onConfirm = {
                viewModel.deleteComment(currentCommentToDelete.id)
                commentToDelete = null
            },
            onDismiss = { commentToDelete = null }
        )
    }

    // Confirm Delete Reply Dialog
    val currentReplyToDelete = replyToDelete
    if (currentReplyToDelete != null) {
        ConfirmDeleteDialog(
            title = "Xoá phản hồi vĩnh viễn?",
            message = "Bạn có chắc chắn muốn xoá phản hồi này của \"${currentReplyToDelete.authorName}\" không? Hành động này không thể hoàn tác.",
            confirmButtonText = "Xoá phản hồi",
            onConfirm = {
                viewModel.deleteComment(currentReplyToDelete.id)
                replyToDelete = null
            },
            onDismiss = { replyToDelete = null }
        )
    }

    // Edit Comment Dialog
    val currentCommentToEdit = commentToEdit
    if (currentCommentToEdit != null) {
        EditCommentDialog(
            comment = currentCommentToEdit,
            isReply = false,
            onDismiss = { commentToEdit = null },
            onSave = { newContent, newAuthor ->
                viewModel.updateComment(currentCommentToEdit.id, newContent, newAuthor)
                commentToEdit = null
            }
        )
    }

    // Edit Reply Dialog
    val currentReplyToEdit = replyToEdit
    if (currentReplyToEdit != null) {
        EditCommentDialog(
            comment = currentReplyToEdit,
            isReply = true,
            onDismiss = { replyToEdit = null },
            onSave = { newContent, newAuthor ->
                viewModel.updateComment(currentReplyToEdit.id, newContent, newAuthor)
                replyToEdit = null
            }
        )
    }

    // Edit Note Dialog
    if (editingNote != null) {
        NoteEditorDialog(
            initialNote = editingNote,
            onDismiss = { viewModel.dismissNoteDialog() },
            onSave = { title, description, isPinned, category, tags ->
                viewModel.saveNote(title, description, isPinned, category, tags)
            }
        )
    }

    // Change author name dialog
    if (showAuthorEditDialog) {
        var tempName by remember { mutableStateOf(authorNameInput) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAuthorEditDialog = false },
            title = { Text("Tên người bình luận") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Tên hiển thị") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (tempName.isNotBlank()) {
                        authorNameInput = tempName.trim()
                    }
                    showAuthorEditDialog = false
                }) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAuthorEditDialog = false }) {
                    Text("Huỷ")
                }
            }
        )
    }

    // Export single note to JSON dialog
    if (showExportJsonDialog) {
        JsonImportExportDialog(
            viewModel = viewModel,
            singleNoteToExport = currentNote,
            onDismiss = { showExportJsonDialog = false }
        )
    }

    // Diamond Rewards and Limits Summary Dialog
    if (showDiamondGoalDialog) {
        DiamondGoalDialog(
            rewardState = rewardState,
            onOpenStore = { showDiamondStoreDialog = true },
            onDismiss = { showDiamondGoalDialog = false }
        )
    }

    // Diamond Store (Gói Nạp & Nâng Cấp) Dialog
    if (showDiamondStoreDialog) {
        DiamondStoreDialog(
            rewardState = rewardState,
            onTopUp = { amount -> viewModel.topUpDiamonds(amount) },
            onActivatePackage = { tier -> viewModel.activateStorePackage(tier) },
            onTopUpAndActivate = { tier -> viewModel.topUpAndActivatePackage(tier) },
            onDismiss = { showDiamondStoreDialog = false }
        )
    }

    // Settings & Auto Daily Backup Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            themeMode = themeMode,
            onSelectThemeMode = { mode -> viewModel.setThemeMode(mode) },
            activityStats = activityStats,
            autoBackupState = autoBackupState,
            onToggleAutoBackup = { enabled -> viewModel.setAutoBackupEnabled(enabled) },
            onManualBackup = { callback -> viewModel.triggerManualBackup(callback) },
            getBackupFiles = { viewModel.getBackupFiles() },
            onDeleteBackupFile = { file -> viewModel.deleteBackupFile(file) },
            onRestoreBackupFile = { file, replaceExisting -> viewModel.restoreFromLocalBackup(file, replaceExisting) },
            onOpenJsonImportExport = { showExportJsonDialog = true },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Daily Streak Dialog
    if (showStreakDialog) {
        StreakDialog(
            streakState = streakState,
            onCreateNoteClicked = { viewModel.startCreateNote() },
            onDismiss = { showStreakDialog = false }
        )
    }

    // Rate Limit / Reward Warning Alert Dialog
    val warningMsg = rateLimitWarning
    if (warningMsg != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearRateLimitWarning() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Thông báo giới hạn",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = warningMsg,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearRateLimitWarning() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Đã hiểu")
                }
            }
        )
    }
}
