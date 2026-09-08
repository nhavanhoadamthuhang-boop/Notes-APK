package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommentEntity
import com.example.ui.CommentWithReplies
import com.example.ui.theme.PinGold
import com.example.ui.theme.PinGoldContainer
import com.example.ui.util.DateUtils

@Composable
fun CommentThreadItem(
    thread: CommentWithReplies,
    onReplyToComment: (CommentEntity) -> Unit,
    onTogglePinComment: (CommentEntity) -> Unit,
    onEditComment: (CommentEntity) -> Unit,
    onDeleteComment: (CommentEntity) -> Unit,
    onEditReply: (CommentEntity) -> Unit,
    onDeleteReply: (CommentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("comment_thread_${thread.comment.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (thread.comment.isPinned) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (thread.comment.isPinned) 1.5.dp else 1.dp,
            color = if (thread.comment.isPinned) PinGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (thread.comment.isPinned) 1.5.dp else 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Root Comment
            SingleCommentContent(
                comment = thread.comment,
                isReply = false,
                onReply = { onReplyToComment(thread.comment) },
                onTogglePin = { onTogglePinComment(thread.comment) },
                onEdit = { onEditComment(thread.comment) },
                onDelete = { onDeleteComment(thread.comment) }
            )

            // Nested Replies if any
            if (thread.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Vertical thread indent guide line
                    Box(
                        modifier = Modifier
                            .padding(start = 14.dp, end = 12.dp)
                            .width(2.5.dp)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(1.dp)
                            )
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        thread.replies.forEach { reply ->
                            ReplyContent(
                                reply = reply,
                                onReply = { onReplyToComment(reply) },
                                onTogglePin = { onTogglePinComment(reply) },
                                onEdit = { onEditReply(reply) },
                                onDelete = { onDeleteReply(reply) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SingleCommentContent(
    comment: CommentEntity,
    isReply: Boolean,
    onReply: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Author Row + Pin Badge + Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Author Avatar
                AuthorAvatar(name = comment.authorName, isPinned = comment.isPinned)

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = comment.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (comment.isPinned) {
                            Surface(
                                shape = CircleShape,
                                color = PinGoldContainer,
                                modifier = Modifier.testTag("pinned_comment_badge_${comment.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PushPin,
                                        contentDescription = "Đã ghim",
                                        tint = PinGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "ĐÃ GHIM",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PinGold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = DateUtils.formatDetailedElapsedTime(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Actions: Pin, Edit, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pin / Unpin button
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("pin_comment_btn_${comment.id}")
                ) {
                    Icon(
                        imageVector = if (comment.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (comment.isPinned) "Bỏ ghim bình luận" else "Ghim bình luận",
                        tint = if (comment.isPinned) PinGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_comment_btn_${comment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa bình luận",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_comment_btn_${comment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xoá bình luận",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Reply action button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            TextButton(
                onClick = onReply,
                modifier = Modifier.testTag("reply_to_comment_btn_${comment.id}")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "Trả lời",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Trả lời",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ReplyContent(
    reply: CommentEntity,
    onReply: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (reply.isPinned) {
            PinGoldContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        border = BorderStroke(
            width = if (reply.isPinned) 1.2.dp else 0.8.dp,
            color = if (reply.isPinned) PinGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reply_item_${reply.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AuthorAvatar(name = reply.authorName, isPinned = reply.isPinned, size = 26)

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = reply.authorName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )

                            if (reply.replyToAuthor != null) {
                                Text(
                                    text = "→ @${reply.replyToAuthor}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (reply.isPinned) {
                                Surface(
                                    shape = CircleShape,
                                    color = PinGoldContainer,
                                    modifier = Modifier.testTag("pinned_reply_badge_${reply.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = "Phản hồi đã ghim",
                                            tint = PinGold,
                                            modifier = Modifier.size(9.dp)
                                        )
                                        Text(
                                            text = "GHIM",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PinGold
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = DateUtils.formatDetailedElapsedTime(reply.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }

                // Actions: Pin reply, Edit reply, Delete reply
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("pin_reply_btn_${reply.id}")
                    ) {
                        Icon(
                            imageVector = if (reply.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (reply.isPinned) "Bỏ ghim phản hồi" else "Ghim phản hồi",
                            tint = if (reply.isPinned) PinGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_reply_btn_${reply.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa phản hồi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_reply_btn_${reply.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Xoá phản hồi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Reply content
            Text(
                text = reply.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            // Reply to this reply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onReply,
                    modifier = Modifier.testTag("reply_to_reply_btn_${reply.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Trả lời",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Trả lời",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AuthorAvatar(name: String, isPinned: Boolean, size: Int = 34) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val avatarBg = if (isPinned) {
        PinGoldContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val avatarText = if (isPinned) {
        PinGold
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(avatarBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = avatarText,
            fontSize = (size * 0.42).sp
        )
    }
}
