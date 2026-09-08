package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ThemeMode
import com.example.ui.NotesViewModel
import com.example.ui.screens.NoteDetailScreen
import com.example.ui.screens.NoteListScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NotesViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotesApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NotesApp(viewModel: NotesViewModel = viewModel()) {
    val selectedNoteId by viewModel.selectedNoteId.collectAsStateWithLifecycle()

    if (selectedNoteId != null) {
        BackHandler {
            viewModel.selectNote(null)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTabletSplitPane = maxWidth >= 800.dp

        if (isTabletSplitPane) {
            // Adaptive Tablet Master-Detail Layout (Side-by-side)
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(380.dp)
                        .fillMaxHeight()
                ) {
                    NoteListScreen(
                        viewModel = viewModel,
                        isEmbeddedInSplitPane = true
                    )
                }

                // Vertical Divider Line
                Surface(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                ) {}

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (selectedNoteId != null) {
                        NoteDetailScreen(
                            viewModel = viewModel,
                            isEmbeddedInSplitPane = true
                        )
                    } else {
                        TabletEmptyDetailPlaceholder(
                            onCreateNote = { viewModel.startCreateNote() }
                        )
                    }
                }
            }
        } else {
            // Phone Single-Pane Navigation Transition
            AnimatedContent(
                targetState = selectedNoteId != null,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { hasSelectedNote ->
                if (hasSelectedNote) {
                    NoteDetailScreen(
                        viewModel = viewModel,
                        isEmbeddedInSplitPane = false
                    )
                } else {
                    NoteListScreen(
                        viewModel = viewModel,
                        isEmbeddedInSplitPane = false
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletEmptyDetailPlaceholder(
    onCreateNote: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.width(360.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                text = "Chọn một ghi chú",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Hãy chọn một ghi chú từ danh sách bên trái để đọc nội dung và thảo luận bình luận, hoặc bấm nút bên dưới để tạo ghi chú mới.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCreateNote,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo ghi chú mới", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

