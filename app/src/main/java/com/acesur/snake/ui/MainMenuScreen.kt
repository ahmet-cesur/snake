package com.acesur.snake.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acesur.snake.ui.theme.GameColors

@Composable
fun MainMenuScreen(
    onPlayClicked: () -> Unit,
    onSelectLevelClicked: () -> Unit,
    onLevelEditorClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameColors.SkyTop, GameColors.SkyBottom)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Game Title
            Text(
                text = "🐛",
                fontSize = 72.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Apple Worm",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black, // Dark text
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Puzzle Game",
                fontSize = 18.sp,
                color = Color.Black.copy(alpha = 0.7f) // Dark text
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Play Button
            Button(
                onClick = onPlayClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GameColors.WormPinkDark
                )
            ) {
                Text(
                    text = "▶  PLAY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Keep button text white for contrast on filled button
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Level Selection Button
            OutlinedButton(
                onClick = onSelectLevelClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black // Dark content
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Black) // Dark border
                    )
                )
            ) {
                Text(
                    text = "📋  SELECT LEVEL",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Level Editor Button
            OutlinedButton(
                onClick = onLevelEditorClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black // Dark content
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Black) // Dark border
                    )
                )
            ) {
                Text(
                    text = "✏️  LEVEL EDITOR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Instructions
            Text(
                text = "Use buttons to move the worm\nEat apples to grow longer\nReach the portal to win!",
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.6f), // Dark text
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
