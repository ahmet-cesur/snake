package com.acesur.snake.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acesur.snake.game.Level
import com.acesur.snake.game.LevelRepository
import com.acesur.snake.ui.theme.GameColors

@Composable
fun LevelManagerScreen(
    onEditLevel: (Level?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    // Force refresh check if list changed
    var refreshTrigger by remember { mutableStateOf(0) }
    val levels = remember(refreshTrigger) { LevelRepository.levels }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEditLevel(null) },
                containerColor = GameColors.WormPink,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Create New Level")
            }
        },
        containerColor = Color.White // Readable background as requested
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                }
                Text(
                    "Level Manager",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(levels.sortedBy { it.id }) { level ->
                    LevelManagerItem(
                        level = level,
                        onEdit = { onEditLevel(level) },
                        onDelete = {
                            if (level.id >= LevelRepository.CUSTOM_LEVEL_ID_START) {
                                LevelRepository.deleteLevel(level.id)
                                LevelRepository.saveAllLevels(context)
                                refreshTrigger++ // Refresh list
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelManagerItem(
    level: Level,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCustom = level.id >= LevelRepository.CUSTOM_LEVEL_ID_START
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCustom) Color(0xFFFFF3E0) else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Level Number in Center
            Text(
                text = "${level.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )

            // Edit Icon (Top Right)
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.Blue.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Delete Icon (Bottom Right)
            if (isCustom) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
