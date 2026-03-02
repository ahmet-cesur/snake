package com.acesur.snake.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.acesur.snake.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acesur.snake.game.Direction
import com.acesur.snake.game.GameEngine
import com.acesur.snake.game.GameState
import com.acesur.snake.game.LevelRepository
import com.acesur.snake.ui.theme.GameColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@Composable
fun GameScreen(
    startingLevel: Int = 0,
    getBestScore: (Int) -> Int = { 10 },
    onLevelStarted: (Int) -> Unit = {},
    onLevelComplete: (Int, Int) -> Unit = { _, _ -> },
    onBackToMenu: () -> Unit = {}
) {
    val gameState = remember { GameState() }
    val gameEngine = remember { GameEngine(gameState) }
    val gameRenderer = remember { GameRenderer() }
    val coroutineScope = rememberCoroutineScope()
    
    // Animation for portal and apples
    val infiniteTransition = rememberInfiniteTransition(label = "gameAnim")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portalAnim"
    )
    
    // Swipe detection
    var dragStart by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var dragEnd by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    
    // Load starting level
    LaunchedEffect(startingLevel) {
        gameEngine.loadLevel(startingLevel)
        onLevelStarted(startingLevel)
    }

    // Eye blinking logic
    LaunchedEffect(Unit) {
        while (true) {
            // Wait for a random interval between blinks (e.g., 2 to 5 seconds)
            val nextBlinkDelay = (2000..5000).random().toLong()
            kotlinx.coroutines.delay(nextBlinkDelay)
            
            // Start blink
            gameState.isBlinking = true
            
            // Blink duration (e.g., 150ms)
            kotlinx.coroutines.delay(150)
            
            // End blink
            gameState.isBlinking = false
        }
    }
    
    // Track level completion
    LaunchedEffect(gameState.isLevelComplete) {
        if (gameState.isLevelComplete) {
            onLevelComplete(gameState.currentLevel, gameState.totalMoves)
        }
    }
    
    // Calculate grid dimensions
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameColors.SkyTop, GameColors.SkyBottom)
                )
            )
    ) {
        // Background decorations
        CloudBackground()
        MountainBackground()
        GrassBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with level info
            TopBar(
                level = gameState.currentLevel + 1,
                totalLevels = LevelRepository.getTotalLevels(),
                applesLeft = gameState.apples.size,
                moves = gameState.totalMoves,
                bestMoves = getBestScore(gameState.currentLevel),
                isStuck = gameState.isStuck,
                onRestartClick = {
                    gameEngine.restartLevel()
                },
                onMenuClick = onBackToMenu
            )
            
            // Game canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    // Calculate cell size to fit the grid
                    val cellWidth = canvasWidth / gameState.gridWidth
                    val cellHeight = canvasHeight / gameState.gridHeight
                    val cellSize = min(cellWidth, cellHeight)
                    
                    // Center the grid
                    val gridWidth = cellSize * gameState.gridWidth
                    val gridHeight = cellSize * gameState.gridHeight
                    val offsetX = (canvasWidth - gridWidth) / 2
                    val offsetY = (canvasHeight - gridHeight) / 2
                    
                    with(gameRenderer) {
                        drawGame(gameState, cellSize, offsetX, offsetY, animationProgress)
                    }
                }
            }
            
            // On-screen controls
            OnScreenControls(
                onMove = { direction ->
                    coroutineScope.launch {
                        gameEngine.moveWorm(direction)
                    }
                }
            )
        }
        
        // Level complete overlay
        if (gameState.isLevelComplete) {
            LevelCompleteOverlay(
                level = gameState.currentLevel + 1,
                moves = gameState.totalMoves,
                minMoves = getBestScore(gameState.currentLevel),
                hasNextLevel = gameEngine.hasNextLevel(),
                onNextLevel = {
                    gameEngine.nextLevel()
                    onLevelStarted(gameState.currentLevel)
                },
                onRestart = {
                    gameEngine.restartLevel()
                },
                onBackToMenu = onBackToMenu
            )
        }
        
        // Game Over overlay
        if (gameState.isGameOver) {
            GameOverOverlay(
                onRestart = {
                    gameEngine.restartLevel()
                }
            )
        }
    }
}

@Composable
fun OnScreenControls(onMove: (Direction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // UP Row
        ControlButton(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            rotation = -90f,
            onClick = { onMove(Direction.UP) }
        )
        
        // Mid Row: LEFT - [Spacer] - RIGHT
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                rotation = 180f,
                onClick = { onMove(Direction.LEFT) }
            )
            
            Spacer(modifier = Modifier.width(77.dp))
            
            ControlButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                rotation = 0f,
                onClick = { onMove(Direction.RIGHT) }
            )
        }
        
        // DOWN Row
        ControlButton(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            rotation = 90f,
            onClick = { onMove(Direction.DOWN) }
        )
    }
}

@Composable
fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    rotation: Float,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(77.dp)
            .padding(4.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ButtonPrimary.copy(alpha = 0.5f),
            contentColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp) 
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(38.dp)
                .rotate(rotation)
        )
    }
}

@Composable
fun GameOverOverlay(onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.AppleRed
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = GameColors.ButtonSecondary)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Try Again")
                }
            }
        }
    }
}

// Helper Modifier for rotation
fun Modifier.rotate(degrees: Float) = this.then(
    Modifier.graphicsLayer(rotationZ = degrees)
)

@Composable
fun StatBox(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val boxModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    } else {
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun TopBar(
    level: Int,
    totalLevels: Int,
    applesLeft: Int,
    moves: Int,
    bestMoves: Int,
    isStuck: Boolean,
    onRestartClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left: Home
        MinimalIcon(Icons.Default.Home, onMenuClick, transparent = true)
        
        VerticalDivider()

        // Integrated Stats & Text in 1 Row
        Text(
            text = "$level/$totalLevels",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        
        VerticalDivider()
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🍎", fontSize = 14.sp)
            Text(
                text = "$applesLeft",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
        
        VerticalDivider()

        Text(
            text = "👣 $moves",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        VerticalDivider()

        Text(
            text = "Best: $bestMoves",
            fontSize = 10.sp,
            color = GameColors.WormPink,
            fontWeight = FontWeight.ExtraBold
        )

        VerticalDivider()
        
        // Right: Restart
        MinimalIcon(
            icon = Icons.Default.Refresh, 
            onClick = onRestartClick, 
            transparent = true,
            isStuck = isStuck
        )
    }
}

@Composable
fun MinimalIcon(
    icon: ImageVector, 
    onClick: () -> Unit, 
    transparent: Boolean = false,
    isStuck: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeOffset"
    )
    
    val stuckScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stuckScale"
    )

    val modifier = Modifier
        .size(36.dp)
        .graphicsLayer {
            if (isStuck) {
                translationX = shakeOffset * 3.dp.toPx()
                scaleX = stuckScale
                scaleY = stuckScale
            }
        }
        .clip(CircleShape)
        .background(if (transparent) Color.Transparent else Color.Black.copy(alpha = 0.3f))
        .clickable(onClick = onClick)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, 
            null, 
            tint = if (isStuck) GameColors.WormPink else Color.White, 
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(16.dp)
            .background(Color.White.copy(alpha = 0.2f))
    )
}

data class CloudState(val x: Float, val y: Float, val scale: Float, val alpha: Float, val duration: Int)

val CLOUD_STATES = listOf(
    CloudState(0.1f, 0.12f, 1.2f, 0.6f, 35000), // big fast
    CloudState(0.5f, 0.18f, 0.8f, 0.4f, 50000), // small slow
    CloudState(0.8f, 0.08f, 1.0f, 0.5f, 40000), // mid mid
    CloudState(0.3f, 0.22f, 0.7f, 0.35f, 60000), // small slow
    CloudState(0.7f, 0.30f, 1.1f, 0.55f, 38000), // big fast
    CloudState(0.9f, 0.15f, 0.9f, 0.45f, 45000) // mid mid
)

@Composable
fun CloudBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val p0 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(CLOUD_STATES[0].duration, easing = LinearEasing)))
    val p1 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(CLOUD_STATES[1].duration, easing = LinearEasing)))
    val p2 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(CLOUD_STATES[2].duration, easing = LinearEasing)))
    val p3 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(CLOUD_STATES[3].duration, easing = LinearEasing)))
    val p4 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(CLOUD_STATES[4].duration, easing = LinearEasing)))
    val p5 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(CLOUD_STATES[5].duration, easing = LinearEasing)))
    
    val progresses = listOf(p0, p1, p2, p3, p4, p5)

    Canvas(modifier = Modifier.fillMaxSize()) {
        CLOUD_STATES.forEachIndexed { index, cloud -> 
            val cloudMaxWidthApproximation = (30f * cloud.scale * 4f) / size.width
            val margin = cloudMaxWidthApproximation + 0.1f 
            val range = 1.0f + 2 * margin
            
            var animatedX = (cloud.x + progresses[index] * range) % range
            animatedX -= margin
            
            drawCloud(
                x = size.width * animatedX,
                y = size.height * cloud.y,
                scale = cloud.scale,
                alpha = cloud.alpha
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(
    x: Float, 
    y: Float, 
    scale: Float,
    alpha: Float
) {
    val baseRadius = 30f * scale
    val white = Color.White.copy(alpha = alpha)
    
    // Simple fluffy cloud made of 5 circles
    drawCircle(white, baseRadius * 1.5f, Offset(x, y))
    drawCircle(white, baseRadius * 1.2f, Offset(x - baseRadius * 1.2f, y + baseRadius * 0.3f))
    drawCircle(white, baseRadius * 1.2f, Offset(x + baseRadius * 1.2f, y + baseRadius * 0.3f))
    drawCircle(white, baseRadius * 1.0f, Offset(x - baseRadius * 2.0f, y + baseRadius * 0.6f))
    drawCircle(white, baseRadius * 1.0f, Offset(x + baseRadius * 2.0f, y + baseRadius * 0.6f))
}

@Composable
fun MountainBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val mountains = listOf(
            Triple(0.2f, 0.90f, 1.0f),
            Triple(0.5f, 0.88f, 1.4f),
            Triple(0.8f, 0.92f, 1.1f)
        )
        
        mountains.forEach { (xPct, yPct, scale) ->
            drawMountain(
                x = size.width * xPct,
                y = size.height * yPct,
                radius = size.width * 0.45f * scale
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMountain(
    x: Float, 
    y: Float, 
    radius: Float
) {
    val mountainColor = Color(0xFFBDC3C7).copy(alpha = 0.5f) // Light Gray
    val snowColor = Color.White.copy(alpha = 0.7f)
    
    // Mountain base
    drawCircle(
        color = mountainColor,
        radius = radius,
        center = Offset(x, y)
    )
    
    // Snowy caps
    val peakY = y - radius * 0.85f
    drawCircle(snowColor, radius * 0.25f, Offset(x, peakY))
    drawCircle(snowColor, radius * 0.2f, Offset(x - radius * 0.12f, peakY + radius * 0.05f))
    drawCircle(snowColor, radius * 0.2f, Offset(x + radius * 0.12f, peakY + radius * 0.05f))
}

@Composable
fun GrassBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val grassBlobs = listOf(
            Offset(0.0f, 1.0f),
            Offset(0.2f, 0.98f),
            Offset(0.4f, 1.02f),
            Offset(0.6f, 0.97f),
            Offset(0.8f, 1.01f),
            Offset(1.0f, 0.99f),
            // Second layer
            Offset(0.1f, 0.95f),
            Offset(0.35f, 0.94f),
            Offset(0.55f, 0.96f),
            Offset(0.75f, 0.93f),
            Offset(0.95f, 0.96f)
        )
        
        grassBlobs.forEachIndexed { index, pos ->
            val isSecondLayer = index >= 6
            val color = if (isSecondLayer) GameColors.GrassGreen else GameColors.GrassDark
            val radius = if (isSecondLayer) size.width * 0.25f else size.width * 0.3f
            
            drawCircle(
                color = color.copy(alpha = 0.8f),
                radius = radius,
                center = Offset(size.width * pos.x, size.height * pos.y)
            )
        }
    }
}

@Composable
fun SwipeInstructions() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoItem("👆", "Swipe")
            InfoItem("🍎", "Eat")
            InfoItem("🌀", "Goal")
        }
    }
}

@Composable
private fun InfoItem(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun LevelCompleteOverlay(
    level: Int,
    moves: Int,
    minMoves: Int,
    hasNextLevel: Boolean,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.OverlayBackground),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebration emoji
                Text(
                    text = "🎉",
                    fontSize = 64.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Level Complete!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Level $level",
                    fontSize = 18.sp,
                    color = GameColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stars based on moves
                Row {
                    val stars = when {
                        moves <= minMoves -> 3
                        moves <= minMoves + 7 -> 2
                        moves <= minMoves + 15 -> 1
                        else -> 1 // Always give at least 1 star for completion? 
                                  // User request says "1 star for variable + 15 moves".
                                  // I'll stick to 1 min for now as per current behavior.
                    }
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = if (index < stars) GameColors.GoldStar else GameColors.StarEmpty
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val isNewRecord = moves < minMoves
                
                Text(
                    text = if (isNewRecord) "New record! $moves moves!" else "Moves: $moves",
                    fontSize = 18.sp,
                    fontWeight = if (isNewRecord) FontWeight.Bold else FontWeight.Normal,
                    color = if (isNewRecord) GameColors.WormPink else GameColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                if (isNewRecord && minMoves != 50) {
                    Text(
                        text = "(Previously: $minMoves)",
                        fontSize = 12.sp,
                        color = GameColors.TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Restart button
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GameColors.ButtonSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                    
                    // Next level button
                    if (hasNextLevel) {
                        Button(
                            onClick = onNextLevel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GameColors.ButtonPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // Game complete - back to menu
                        Button(
                            onClick = onBackToMenu,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GameColors.GoldStar
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🏆 All Complete! Menu")
                        }
                    }
                }
            }
        }
    }
}
