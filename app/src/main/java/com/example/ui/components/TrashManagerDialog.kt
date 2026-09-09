package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.CommentEntity
import com.example.data.local.NoteEntity
import com.example.data.repository.DiamondRewardState
import com.example.data.repository.StorePackageTier
import com.example.ui.util.DateUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val DiamondGold = Color(0xFFFFB300)

enum class TrashTab(val title: String) {
    NOTES("Thùng rác Ghi chú"),
    COMMENTS("Thùng rác Bình luận & Phản hồi")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrashManagerDialog(
    trashNotes: List<NoteEntity>,
    trashComments: List<CommentEntity>,
    allNotes: List<NoteEntity>,
    rewardState: DiamondRewardState,
    onRestoreNote: (Long) -> Unit,
    onPermanentlyDeleteNote: (Long) -> Unit,
    onRestoreAllNotes: () -> Unit,
    onEmptyTrashNotes: () -> Unit,
    onRestoreComment: (Long) -> Unit,
    onPermanentlyDeleteComment: (Long) -> Unit,
    onRestoreAllComments: () -> Unit,
    onEmptyTrashComments: () -> Unit,
    onEmptyAllTrash: () -> Unit,
    onOpenStore: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCommentFilter by remember { mutableStateOf<String>("ALL") } // ALL, ROOT, REPLY

    var itemToDeletePermanently by remember { mutableStateOf<Pair<String, Long>?>(null) } // Pair(Type: "NOTE" or "COMMENT", Id)
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }

    val retentionDays = rewardState.trashRetentionDays
    val notesMap = remember(allNotes, trashNotes) {
        (allNotes + trashNotes).associateBy { it.id }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("dialog_trash_manager"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Thùng Rác & Đã Xoá Gần Đây",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lưu trữ ${retentionDays} ngày trước khi xoá vĩnh viễn",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_trash_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                HorizontalDivider()

                // Retention Tier Banner
                TrashRetentionBanner(
                    rewardState = rewardState,
                    onOpenStore = {
                        onDismiss()
                        onOpenStore()
                    }
                )

                // Tabs: Notes vs Comments
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Ghi chú (${trashNotes.size})",
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Bình luận & Phản hồi (${trashComments.size})",
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (selectedTabIndex == 0) {
                        // Trash Notes Tab
                        TrashNotesList(
                            trashNotes = trashNotes,
                            retentionDays = retentionDays,
                            onRestoreNote = onRestoreNote,
                            onPermanentlyDeleteNote = { noteId ->
                                itemToDeletePermanently = Pair("NOTE", noteId)
                            },
                            onRestoreAllNotes = onRestoreAllNotes,
                            onEmptyTrashNotes = {
                                showEmptyTrashConfirm = true
                            }
                        )
                    } else {
                        // Trash Comments & Replies Tab
                        TrashCommentsList(
                            trashComments = trashComments,
                            notesMap = notesMap,
                            retentionDays = retentionDays,
                            selectedFilter = selectedCommentFilter,
                            onFilterChange = { selectedCommentFilter = it },
                            onRestoreComment = onRestoreComment,
                            onPermanentlyDeleteComment = { commentId ->
                                itemToDeletePermanently = Pair("COMMENT", commentId)
                            },
                            onRestoreAllComments = onRestoreAllComments,
                            onEmptyTrashComments = {
                                showEmptyTrashConfirm = true
                            }
                        )
                    }
                }

                HorizontalDivider()

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (trashNotes.isNotEmpty() || trashComments.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showEmptyTrashConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dọn sạch toàn bộ", fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Đóng")
                    }
                }
            }
        }
    }

    // Confirmation for Permanent Deletion of single item
    itemToDeletePermanently?.let { (type, id) ->
        val itemTitle = if (type == "NOTE") "ghi chú này" else "bình luận / phản hồi này"
        ConfirmDeleteDialog(
            title = "Xoá vĩnh viễn $itemTitle?",
            message = "Hành động này không thể hoàn tác. Dữ liệu sẽ bị xóa hoàn toàn khỏi thiết bị của bạn ngay lập tức.",
            confirmButtonText = "Xoá vĩnh viễn",
            onConfirm = {
                if (type == "NOTE") {
                    onPermanentlyDeleteNote(id)
                } else {
                    onPermanentlyDeleteComment(id)
                }
                itemToDeletePermanently = null
            },
            onDismiss = { itemToDeletePermanently = null }
        )
    }

    // Confirmation for Emptying Trash
    if (showEmptyTrashConfirm) {
        val targetName = if (selectedTabIndex == 0) "tất cả ghi chú trong thùng rác" else "tất cả bình luận trong thùng rác"
        ConfirmDeleteDialog(
            title = "Dọn sạch thùng rác?",
            message = "Bạn có chắc chắn muốn xóa vĩnh viễn $targetName không? Hành động này không thể hoàn tác.",
            confirmButtonText = "Dọn sạch ngay",
            onConfirm = {
                if (selectedTabIndex == 0) {
                    onEmptyTrashNotes()
                } else {
                    onEmptyTrashComments()
                }
                showEmptyTrashConfirm = false
            },
            onDismiss = { showEmptyTrashConfirm = false }
        )
    }
}

@Composable
private fun TrashRetentionBanner(
    rewardState: DiamondRewardState,
    onOpenStore: () -> Unit
) {
    val tier = rewardState.currentTier
    val retentionDays = tier.trashRetentionDays

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = when (tier) {
            StorePackageTier.PACKAGE_2 -> Color(0xFF673AB7).copy(alpha = 0.12f)
            StorePackageTier.PACKAGE_1 -> Color(0xFFFFB300).copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        },
        border = BorderStroke(
            1.dp,
            when (tier) {
                StorePackageTier.PACKAGE_2 -> Color(0xFF673AB7).copy(alpha = 0.4f)
                StorePackageTier.PACKAGE_1 -> Color(0xFFFFB300).copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoDelete,
                    contentDescription = null,
                    tint = when (tier) {
                        StorePackageTier.PACKAGE_2 -> Color(0xFF673AB7)
                        StorePackageTier.PACKAGE_1 -> DiamondGold
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Thời gian lưu trữ thùng rác: $retentionDays ngày",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Đang áp dụng: ${tier.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (tier != StorePackageTier.PACKAGE_2) {
                Button(
                    onClick = onOpenStore,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (tier) {
                            StorePackageTier.PACKAGE_1 -> Color(0xFF673AB7)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (tier == StorePackageTier.DEFAULT) "Gói 1 (120 ngày) / Gói 2 (180 ngày)" else "Gói 2 (180 ngày)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrashNotesList(
    trashNotes: List<NoteEntity>,
    retentionDays: Int,
    onRestoreNote: (Long) -> Unit,
    onPermanentlyDeleteNote: (Long) -> Unit,
    onRestoreAllNotes: () -> Unit,
    onEmptyTrashNotes: () -> Unit
) {
    if (trashNotes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "Thùng rác ghi chú trống",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Các ghi chú đã xóa gần đây trong vòng $retentionDays ngày sẽ xuất hiện tại đây trước khi bị xóa vĩnh viễn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Batch Action Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Có ${trashNotes.size} ghi chú trong thùng rác",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onRestoreAllNotes,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Khôi phục tất cả", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = onEmptyTrashNotes,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Xoá tất cả", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(trashNotes, key = { it.id }) { note ->
                TrashNoteCard(
                    note = note,
                    retentionDays = retentionDays,
                    onRestore = { onRestoreNote(note.id) },
                    onDeletePermanently = { onPermanentlyDeleteNote(note.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrashNoteCard(
    note: NoteEntity,
    retentionDays: Int,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val deletedTime = note.deletedAt ?: note.updatedAt
    val remainingDays = calculateRemainingDays(deletedTime, retentionDays)
    val deletedDateStr = DateUtils.formatRelativeTime(deletedTime)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title & Countdown Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.title.ifBlank { "Ghi chú không tiêu đề" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                RemainingDaysBadge(remainingDays = remainingDays)
            }

            // Excerpt
            if (note.description.isNotBlank()) {
                Text(
                    text = note.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Category & Tags
            if (note.category.isNotBlank() || note.tagList.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (note.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Outlined.Folder, contentDescription = null, modifier = Modifier.size(12.dp))
                                Text(text = note.category, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    note.tagList.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Outlined.Label, contentDescription = null, modifier = Modifier.size(10.dp))
                                Text(text = "#$tag", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Footer: Deletion Date & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Đã xoá: $deletedDateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRestore,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Khôi phục", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDeletePermanently,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xoá vĩnh viễn", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashCommentsList(
    trashComments: List<CommentEntity>,
    notesMap: Map<Long, NoteEntity>,
    retentionDays: Int,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onRestoreComment: (Long) -> Unit,
    onPermanentlyDeleteComment: (Long) -> Unit,
    onRestoreAllComments: () -> Unit,
    onEmptyTrashComments: () -> Unit
) {
    val filteredComments = remember(trashComments, selectedFilter) {
        when (selectedFilter) {
            "ROOT" -> trashComments.filter { it.parentId == null }
            "REPLY" -> trashComments.filter { it.parentId != null }
            else -> trashComments
        }
    }

    if (trashComments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "Thùng rác bình luận trống",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Các bình luận và phản hồi đã xóa gần đây trong vòng $retentionDays ngày sẽ xuất hiện tại đây trước khi bị xóa vĩnh viễn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { onFilterChange("ALL") },
                            label = { Text("Tất cả (${trashComments.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "ROOT",
                            onClick = { onFilterChange("ROOT") },
                            label = { Text("Bình luận (${trashComments.count { it.parentId == null }})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "REPLY",
                            onClick = { onFilterChange("REPLY") },
                            label = { Text("Phản hồi (${trashComments.count { it.parentId != null }})", fontSize = 11.sp) }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = onRestoreAllComments,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Khôi phục hết", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onEmptyTrashComments,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Dọn sạch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(filteredComments, key = { it.id }) { comment ->
                val parentNote = notesMap[comment.noteId]
                TrashCommentCard(
                    comment = comment,
                    parentNoteTitle = parentNote?.title ?: "Ghi chú #${comment.noteId}",
                    retentionDays = retentionDays,
                    onRestore = { onRestoreComment(comment.id) },
                    onDeletePermanently = { onPermanentlyDeleteComment(comment.id) }
                )
            }
        }
    }
}

@Composable
private fun TrashCommentCard(
    comment: CommentEntity,
    parentNoteTitle: String,
    retentionDays: Int,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val isReply = comment.parentId != null
    val deletedTime = comment.deletedAt ?: comment.createdAt
    val remainingDays = calculateRemainingDays(deletedTime, retentionDays)
    val deletedDateStr = DateUtils.formatRelativeTime(deletedTime)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Author, Type badge & Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isReply) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (isReply) {
                                Icon(Icons.Outlined.Reply, contentDescription = null, modifier = Modifier.size(11.dp))
                                Text(
                                    text = if (comment.replyToAuthor != null) "Phản hồi @${comment.replyToAuthor}" else "Phản hồi",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Bình luận gốc",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                RemainingDaysBadge(remainingDays = remainingDays)
            }

            // Note context reference
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Thuộc: $parentNoteTitle",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Footer: Date and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Đã xoá: $deletedDateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRestore,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Khôi phục", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDeletePermanently,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xoá vĩnh viễn", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RemainingDaysBadge(remainingDays: Int) {
    val isUrgent = remainingDays <= 7
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        border = BorderStroke(
            1.dp,
            if (isUrgent) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HourglassBottom,
                contentDescription = null,
                tint = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "Còn $remainingDays ngày",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun calculateRemainingDays(deletedTime: Long, totalRetentionDays: Int): Int {
    val now = System.currentTimeMillis()
    val elapsedMs = max(0L, now - deletedTime)
    val elapsedDays = (elapsedMs / (24L * 60L * 60L * 1000L)).toInt()
    return max(0, totalRetentionDays - elapsedDays)
}
