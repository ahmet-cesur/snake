package com.acesur.snake.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acesur.snake.game.LevelRepository
import com.acesur.snake.ui.theme.GameColors

@Composable
fun LevelSelectionScreen(
    highestUnlockedLevel: Int,
    onLevelSelected: (Int) -> Unit,
    onBackToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameColors.SkyTop, GameColors.SkyBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToMenu) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Menu",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Select Level",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // Spacer to balance the back button
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Level grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(LevelRepository.levels.size) { index ->
                    val level = LevelRepository.levels[index]
                    val isUnlocked = level.id <= highestUnlockedLevel || level.id >= LevelRepository.CUSTOM_LEVEL_ID_START
                    
                    LevelButton(
                        levelNumber = level.id,
                        isUnlocked = isUnlocked,
                        isCustom = level.id >= LevelRepository.CUSTOM_LEVEL_ID_START,
                        onClick = { 
                            if (isUnlocked) {
                                onLevelSelected(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelButton(
    levelNumber: Int,
    isUnlocked: Boolean,
    isCustom: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isUnlocked) {
        if (isCustom) Color(0xFF34495E).copy(alpha = 0.9f) // Dark custom color
        else GameColors.WormPinkDark.copy(alpha = 0.8f)
    } else {
        Color.Gray.copy(alpha = 0.5f)
    }
    
    val borderColor = if (isUnlocked) {
        if (isCustom) Color(0xFFF1C40F) // Gold for custom
        else GameColors.WormPink
    } else {
        Color.DarkGray
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = isUnlocked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Custom Badge
        if (isCustom) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1C40F))
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text("C", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = if (isCustom) "★" else "$levelNumber",
                    fontSize = if (isCustom) 20.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "$levelNumber",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
