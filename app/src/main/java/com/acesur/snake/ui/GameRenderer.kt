package com.acesur.snake.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import com.acesur.snake.game.CellType
import com.acesur.snake.game.GameState
import com.acesur.snake.game.Position
import com.acesur.snake.ui.theme.GameColors
import kotlin.math.cos
import kotlin.math.sin

class GameRenderer {
    
    fun DrawScope.drawGame(gameState: GameState, cellSize: Float, offsetX: Float, offsetY: Float, animationProgress: Float) {
        // Draw grid elements
        drawGrid(gameState, cellSize, offsetX, offsetY)
        
        // Draw portal (animated)
        gameState.portal?.let { portal ->
            drawPortal(portal, cellSize, offsetX, offsetY, animationProgress, gameState.apples.isEmpty())
        }
        
        // Draw boxes
        gameState.boxes.forEach { box ->
            drawWoodenBox(box, cellSize, offsetX, offsetY)
        }
        
        // Draw apples
        gameState.apples.forEach { apple ->
            drawApple(apple, cellSize, offsetX, offsetY, animationProgress)
        }
        
        // Draw worm (drawn BEFORE portal for entering effect)
        drawWorm(gameState, cellSize, offsetX, offsetY)

        // Draw portal (drawn AFTER worm to create 3D entering effect)
        gameState.portal?.let { portal ->
            drawPortal(portal, cellSize, offsetX, offsetY, animationProgress, gameState.apples.isEmpty())
        }
    }
    
    private fun DrawScope.drawBackground(gameState: GameState, cellSize: Float, offsetX: Float, offsetY: Float) {
        // Sky gradient
        val skyBrush = Brush.verticalGradient(
            colors = listOf(GameColors.SkyTop, GameColors.SkyBottom),
            startY = 0f,
            endY = size.height
        )
        drawRect(brush = skyBrush)
    }
    
    private fun DrawScope.drawGrid(gameState: GameState, cellSize: Float, offsetX: Float, offsetY: Float) {
        for (y in 0 until gameState.gridHeight) {
            for (x in 0 until gameState.gridWidth) {
                val cellX = offsetX + x * cellSize
                val cellY = offsetY + y * cellSize
                
                // Safety check to avoid crash if grid is not fully initialized
                if (y < gameState.grid.size && x < gameState.grid[y].size) {
                    when (gameState.grid[y][x]) {
                        CellType.WALL -> drawWall(cellX, cellY, cellSize, x, y, gameState)
                        CellType.TRAP -> drawTrap(cellX, cellY, cellSize)
                        CellType.EMPTY -> drawEmptyCell(cellX, cellY, cellSize)
                        else -> {}
                    }
                }
            }
        }
    }
    
    private fun DrawScope.drawTrap(x: Float, y: Float, cellSize: Float) {
        // Dark metallic base
        drawRect(
            color = Color(0xFF2C3E50), // Dark steel blue
            topLeft = Offset(x, y),
            size = Size(cellSize, cellSize)
        )
        
        // Hazard stripes (diagonal yellow/black) - Simplified to just border
        drawRect(
            color = Color(0xFFE74C3C), // Red hazard border
            topLeft = Offset(x, y),
            size = Size(cellSize, cellSize),
            style = Stroke(width = cellSize * 0.1f)
        )
        
        // Spikes
        val spikeCount = 3
        val spikeWidth = cellSize / spikeCount
        val spikeHeight = cellSize * 0.4f
        val basePath = Path()
        
        for (i in 0 until spikeCount) {
            val spikeX = x + i * spikeWidth
            val spikeY = y + cellSize - cellSize * 0.1f // Start from bottom (+margin)
            
            basePath.moveTo(spikeX, spikeY)
            basePath.lineTo(spikeX + spikeWidth / 2, spikeY - spikeHeight) // Peak
            basePath.lineTo(spikeX + spikeWidth, spikeY) // Base right
            basePath.close()
        }
        
        drawPath(
            path = basePath,
            color = Color(0xFF95A5A6), // Metal grey spikes
            style = Fill
        )
        
        drawPath(
            path = basePath,
            color = Color(0xFF34495E), // Spike outline
            style = Stroke(width = cellSize * 0.02f)
        )
    }
    
    private fun DrawScope.drawEmptyCell(x: Float, y: Float, cellSize: Float) {
        // Empty cell is transparent for floating island effect
        // No drawing here
    }
    
    private fun DrawScope.drawWall(x: Float, y: Float, cellSize: Float, gridX: Int, gridY: Int, gameState: GameState) {
        val padding = cellSize * 0.02f
        
        // Main stone block
        val stoneBrush = Brush.verticalGradient(
            colors = listOf(GameColors.WallLight, GameColors.WallGray, GameColors.WallDark),
            startY = y,
            endY = y + cellSize
        )
        
        drawRoundRect(
            brush = stoneBrush,
            topLeft = Offset(x + padding, y + padding),
            size = Size(cellSize - padding * 2, cellSize - padding * 2),
            cornerRadius = CornerRadius(cellSize * 0.1f)
        )
        
        // Stone outline
        drawRoundRect(
            color = GameColors.WallOutline,
            topLeft = Offset(x + padding, y + padding),
            size = Size(cellSize - padding * 2, cellSize - padding * 2),
            cornerRadius = CornerRadius(cellSize * 0.1f),
            style = Stroke(width = cellSize * 0.05f)
        )
        
        // Inner details (cracks/texture)
        val detailColor = GameColors.WallDark.copy(alpha = 0.4f)
        drawLine(
            color = detailColor,
            start = Offset(x + cellSize * 0.3f, y + cellSize * 0.3f),
            end = Offset(x + cellSize * 0.5f, y + cellSize * 0.5f),
            strokeWidth = 2f
        )
        drawLine(
            color = detailColor,
            start = Offset(x + cellSize * 0.6f, y + cellSize * 0.7f),
            end = Offset(x + cellSize * 0.8f, y + cellSize * 0.6f),
            strokeWidth = 2f
        )
    }
    
    private fun DrawScope.drawWoodenBox(pos: Position, cellSize: Float, offsetX: Float, offsetY: Float) {
        val x = offsetX + pos.x * cellSize
        val y = offsetY + pos.y * cellSize
        val padding = cellSize * 0.05f
        val boxSize = cellSize - padding * 2
        
        // Box shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.3f),
            topLeft = Offset(x + padding + 3f, y + padding + 3f),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(cellSize * 0.1f)
        )
        
        // Main wood body
        val woodBrush = Brush.verticalGradient(
            colors = listOf(GameColors.WoodLight, GameColors.WoodMedium, GameColors.WoodDark),
            startY = y,
            endY = y + cellSize
        )
        
        drawRoundRect(
            brush = woodBrush,
            topLeft = Offset(x + padding, y + padding),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(cellSize * 0.1f)
        )
        
        // Wood grain lines
        val grainColor = GameColors.WoodGrain.copy(alpha = 0.5f)
        for (i in 1..3) {
            val lineY = y + padding + (boxSize / 4) * i
            drawLine(
                color = grainColor,
                start = Offset(x + padding + cellSize * 0.1f, lineY),
                end = Offset(x + padding + boxSize - cellSize * 0.1f, lineY),
                strokeWidth = 2f
            )
        }
        
        // Cross planks
        val plankWidth = cellSize * 0.08f
        drawRect(
            color = GameColors.WoodDark,
            topLeft = Offset(x + cellSize * 0.45f, y + padding),
            size = Size(plankWidth, boxSize)
        )
        drawRect(
            color = GameColors.WoodDark,
            topLeft = Offset(x + padding, y + cellSize * 0.45f),
            size = Size(boxSize, plankWidth)
        )
        
        // Outline
        drawRoundRect(
            color = GameColors.WoodDark,
            topLeft = Offset(x + padding, y + padding),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(cellSize * 0.1f),
            style = Stroke(width = cellSize * 0.04f)
        )
    }
    
    private fun DrawScope.drawApple(pos: Position, cellSize: Float, offsetX: Float, offsetY: Float, animProgress: Float) {
        val x = offsetX + pos.x * cellSize + cellSize / 2
        val y = offsetY + pos.y * cellSize + cellSize / 2
        val radius = cellSize * 0.35f
        
        // Subtle bounce animation
        val bounce = sin(animProgress * 4) * cellSize * 0.03f
        val animY = y + bounce
        
        // Apple shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.2f),
            topLeft = Offset(x - radius * 0.8f, y + radius * 0.6f),
            size = Size(radius * 1.6f, radius * 0.4f)
        )
        
        // Apple body
        val appleBrush = Brush.radialGradient(
            colors = listOf(GameColors.AppleRedLight, GameColors.AppleRed, GameColors.AppleRedDark),
            center = Offset(x - radius * 0.3f, animY - radius * 0.3f),
            radius = radius * 1.5f
        )
        
        drawCircle(
            brush = appleBrush,
            radius = radius,
            center = Offset(x, animY)
        )
        
        // Apple shine
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = radius * 0.25f,
            center = Offset(x - radius * 0.3f, animY - radius * 0.3f)
        )
        
        // Stem
        drawLine(
            color = GameColors.AppleStem,
            start = Offset(x, animY - radius),
            end = Offset(x + cellSize * 0.05f, animY - radius - cellSize * 0.12f),
            strokeWidth = cellSize * 0.06f
        )
        
        // Leaf
        val leafPath = Path().apply {
            moveTo(x + cellSize * 0.05f, animY - radius - cellSize * 0.08f)
            quadraticTo(
                x + cellSize * 0.2f, animY - radius - cellSize * 0.2f,
                x + cellSize * 0.15f, animY - radius - cellSize * 0.05f
            )
        }
        drawPath(
            path = leafPath,
            color = GameColors.AppleLeaf,
            style = Fill
        )
    }
    
    private fun DrawScope.drawPortal(pos: Position, cellSize: Float, offsetX: Float, offsetY: Float, animProgress: Float, isActive: Boolean) {
        val x = offsetX + pos.x * cellSize + cellSize / 2
        val y = offsetY + pos.y * cellSize + cellSize / 2
        val baseRadius = cellSize * 0.4f
        
        // Pulsing animation
        val pulse = if (isActive) sin(animProgress * 3) * 0.1f + 1f else 0.8f
        val radius = baseRadius * pulse
        
        // Outer glow
        val glowRadius = radius * 1.5f
        val glowBrush = Brush.radialGradient(
            colors = listOf(
                if (isActive) GameColors.PortalGlow.copy(alpha = 0.6f) else GameColors.PortalPurple.copy(alpha = 0.5f),
                Color.Transparent
            ),
            center = Offset(x, y),
            radius = glowRadius
        )
        drawCircle(
            brush = glowBrush,
            radius = glowRadius,
            center = Offset(x, y)
        )
        
        // Portal rings
        val ringCount = 3
        for (i in ringCount downTo 1) {
            val ringRadius = radius * (i.toFloat() / ringCount)
            val rotation = animProgress * (if (i % 2 == 0) 1f else -1f) * 2f
            val ringColor = when (i) {
                3 -> GameColors.PortalPurple
                2 -> GameColors.PortalPink
                else -> GameColors.PortalBlue
            }
            
            drawCircle(
                color = ringColor.copy(alpha = if (isActive) 0.8f else 0.6f),
                radius = ringRadius,
                center = Offset(x, y),
                style = Stroke(width = cellSize * 0.06f)
            )
        }
        
        // Center vortex
        val centerBrush = Brush.radialGradient(
            colors = listOf(GameColors.PortalCenter, GameColors.PortalPurple),
            center = Offset(x, y),
            radius = radius * 0.7f
        )
        drawCircle(
            brush = centerBrush,
            radius = radius * 0.6f,
            center = Offset(x, y)
        )
        
        // Spiral effect
        if (isActive) {
            for (i in 0 until 8) {
                val angle = (animProgress * 2 + i * 0.785f) // 45 degrees apart
                val spiralX = x + cos(angle) * radius * 0.4f
                val spiralY = y + sin(angle) * radius * 0.4f
                drawCircle(
                    color = GameColors.PortalGlow.copy(alpha = 0.7f),
                    radius = cellSize * 0.04f,
                    center = Offset(spiralX.toFloat(), spiralY.toFloat())
                )
            }
        }
    }
    
    private fun DrawScope.drawWorm(gameState: GameState, cellSize: Float, offsetX: Float, offsetY: Float) {
        val segments = gameState.wormSegments
        if (segments.isEmpty()) return
        
        // Draw all segments (body first, then head to layer correctly)
        // We need to know connections for each segment to draw correct shapes
        
        // First pass: Draw connections/fills between segments to hide gaps
        for (i in segments.indices) {
            if (i < segments.size - 1) {
                val current = segments[i]
                val next = segments[i + 1]
                drawSegmentConnection(current.position, next.position, cellSize, offsetX, offsetY)
            }
        }
        
        // Second pass: Draw segment bodies (color fill)
        for (i in segments.indices.reversed()) {
            val segment = segments[i]
            val isHead = i == 0
            val isTail = i == segments.lastIndex
            
            val x = offsetX + segment.position.x * cellSize + cellSize / 2
            val y = offsetY + segment.position.y * cellSize + cellSize / 2
            
            // Determine connections for this segment
            // Connected to previous (towards head)
            val prevPos = if (i > 0) segments[i - 1].position else null
            // Connected to next (towards tail)
            val nextPos = if (i < segments.lastIndex) segments[i + 1].position else null
            
            drawWormSegmentBody(x, y, cellSize, isHead, isTail, 
                segment.position, prevPos, nextPos, 
                if (isHead) getWormDirection(segments) else com.acesur.snake.game.Direction.RIGHT)
        }
        
        // Third pass: Draw outlines
        // We draw outlines for each segment but skip the side that connects to another segment
        for (i in segments.indices.reversed()) {
            val segment = segments[i]
            val isHead = i == 0
            val isTail = i == segments.lastIndex
            
            val x = offsetX + segment.position.x * cellSize + cellSize / 2
            val y = offsetY + segment.position.y * cellSize + cellSize / 2
            
            // Determine connections
            val prevPos = if (i > 0) segments[i - 1].position else null
            val nextPos = if (i < segments.lastIndex) segments[i + 1].position else null
            
            drawWormSegmentOutline(x, y, cellSize, segment.position, prevPos, nextPos)
            
            // Draw face details if head (after outline)
            if (isHead) {
                drawWormFace(x, y, cellSize, getWormDirection(segments), gameState)
            }
        }
    }
    
    // Draw a bridging rectangle between two adjacent positions to fill the gap
    private fun DrawScope.drawSegmentConnection(pos1: Position, pos2: Position, cellSize: Float, offsetX: Float, offsetY: Float) {
        // Calculate center of both cells
        val x1 = offsetX + pos1.x * cellSize + cellSize / 2
        val y1 = offsetY + pos1.y * cellSize + cellSize / 2
        val x2 = offsetX + pos2.x * cellSize + cellSize / 2
        val y2 = offsetY + pos2.y * cellSize + cellSize / 2
        
        // Determine direction and size
        // We want a rectangle that connects the visual bodies
        // The body size is 0.95 * cellSize
        val size = cellSize * 0.95f
        val halfSize = size / 2
        
        // We draw a rectangle that bridges the two centers
        // For horizontal connection
        if (pos1.y == pos2.y) {
            val left = minOf(x1, x2)
            val right = maxOf(x1, x2)
            val top = y1 - halfSize
            
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(GameColors.WormPinkLight, GameColors.WormPink),
                    startY = top,
                    endY = top + size
                ),
                topLeft = Offset(left, top),
                size = Size(right - left, size)
            )
        } 
        // For vertical connection
        else if (pos1.x == pos2.x) {
            val top = minOf(y1, y2)
            val bottom = maxOf(y1, y2)
            val left = x1 - halfSize
            
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(GameColors.WormPinkLight, GameColors.WormPink),
                    startX = left,
                    endX = left + size
                ),
                topLeft = Offset(left, top),
                size = Size(size, bottom - top)
            )
        }
    }
    
    private fun DrawScope.drawWormSegmentBody(
        x: Float, y: Float, cellSize: Float, 
        isHead: Boolean, isTail: Boolean,
        currentPos: Position, prevPos: Position?, nextPos: Position?,
        direction: com.acesur.snake.game.Direction
    ) {
        val size = cellSize * 0.95f
        val halfSize = size / 2
        val cellHalf = cellSize / 2
        // Match the corner radius of the outline (0.45f) for more rounded corners
        val cornerRadius = CornerRadius(cellSize * 0.45f)
        val topLeft = Offset(x - halfSize, y - halfSize)
        
        val l = x - halfSize
        val t = y - halfSize
        
        // Determine which corners should be rounded
        val connUp = (prevPos?.y ?: -1) < currentPos.y || (nextPos?.y ?: -1) < currentPos.y
        val connDown = (prevPos?.y ?: -1) > currentPos.y || (nextPos?.y ?: -1) > currentPos.y
        val connLeft = (prevPos?.x ?: -1) < currentPos.x || (nextPos?.x ?: -1) < currentPos.x
        val connRight = (prevPos?.x ?: -1) > currentPos.x || (nextPos?.x ?: -1) > currentPos.x
        
        // Main body shape
        val bodyBrush = Brush.radialGradient(
            colors = listOf(GameColors.WormPinkLight, GameColors.WormPink, GameColors.WormPinkDark),
            center = Offset(x - halfSize * 0.5f, y - halfSize * 0.5f),
            radius = size * 0.8f
        )
        
        drawRoundRect(
            brush = bodyBrush,
            topLeft = topLeft,
            size = Size(size, size),
            cornerRadius = cornerRadius
        )
        
        // Fill in corners to make connections look seamless (square off the connected sides)
        val rectSize = cornerRadius.x
        
        if (connUp) {
            drawRect(brush = bodyBrush, topLeft = topLeft, size = Size(size, rectSize))
        }
        if (connDown) {
            drawRect(brush = bodyBrush, topLeft = Offset(topLeft.x, topLeft.y + size - rectSize), size = Size(size, rectSize))
        }
        if (connLeft) {
            drawRect(brush = bodyBrush, topLeft = topLeft, size = Size(rectSize, size))
        }
        if (connRight) {
            drawRect(brush = bodyBrush, topLeft = Offset(topLeft.x + size - rectSize, topLeft.y), size = Size(rectSize, size))
        }
        
        // Fill Inner Corner Fillets
        // Top-Left Inner Corner
        if (connUp && connLeft) {
            val path = Path().apply {
                moveTo(x - cellHalf, t)
                quadraticTo(x - cellHalf, y - cellHalf, l, y - cellHalf)
                lineTo(l, t)
                close()
            }
            drawPath(path = path, brush = bodyBrush)
        }
        
        // Top-Right Inner Corner
        if (connUp && connRight) {
            val r = x + halfSize
            val path = Path().apply {
                moveTo(r, y - cellHalf)
                quadraticTo(x + cellHalf, y - cellHalf, x + cellHalf, t)
                lineTo(r, t)
                close()
            }
            drawPath(path = path, brush = bodyBrush)
        }
        
        // Bottom-Right Inner Corner
        if (connDown && connRight) {
            val r = x + halfSize
            val b = y + halfSize
            val path = Path().apply {
                moveTo(x + cellHalf, b)
                quadraticTo(x + cellHalf, y + cellHalf, r, y + cellHalf)
                lineTo(r, b)
                close()
            }
            drawPath(path = path, brush = bodyBrush)
        }
        
        // Bottom-Left Inner Corner
        if (connDown && connLeft) {
            val b = y + halfSize
            val path = Path().apply {
                moveTo(l, y + cellHalf)
                quadraticTo(x - cellHalf, y + cellHalf, x - cellHalf, b)
                lineTo(l, b)
                close()
            }
            drawPath(path = path, brush = bodyBrush)
        }
    }
    
    private fun DrawScope.drawWormSegmentOutline(
        x: Float, y: Float, cellSize: Float,
        currentPos: Position, prevPos: Position?, nextPos: Position?
    ) {
        val size = cellSize * 0.95f
        val halfSize = size / 2
        val cellHalf = cellSize / 2
        // 45% corner radius for more rounded, pill-like segments
        val cornerRad = cellSize * 0.45f 
        val strokeWidth = cellSize * 0.03f
        
        val l = x - halfSize
        val r = x + halfSize
        val t = y - halfSize
        val b = y + halfSize
        
        // Connections
        val connUp = (prevPos?.y ?: -1) < currentPos.y || (nextPos?.y ?: -1) < currentPos.y
        val connDown = (prevPos?.y ?: -1) > currentPos.y || (nextPos?.y ?: -1) > currentPos.y
        val connLeft = (prevPos?.x ?: -1) < currentPos.x || (nextPos?.x ?: -1) < currentPos.x
        val connRight = (prevPos?.x ?: -1) > currentPos.x || (nextPos?.x ?: -1) > currentPos.x
        
        val path = Path()
        
        // --- Top Sequence ---
        if (connUp) {
            // Open Top: Handle Inner Corners overlapping Top Edge
            if (connLeft) { 
                // Inner Top-Left 
                path.moveTo(x - cellHalf, t)
                path.quadraticTo(x - cellHalf, y - cellHalf, l, y - cellHalf)
            }
            if (connRight) { 
                // Inner Top-Right
                path.moveTo(r, y - cellHalf)
                path.quadraticTo(x + cellHalf, y - cellHalf, x + cellHalf, t)
            }
        } else {
            // Closed Top: Draw Top Edge
            // Start
            if (connLeft) path.moveTo(x - cellHalf, t)
            else {
                path.moveTo(l, t + cornerRad)
                path.quadraticTo(l, t, l + cornerRad, t)
            }
            
            // End
            if (connRight) path.lineTo(x + cellHalf, t)
            else {
                path.lineTo(r - cornerRad, t)
                path.quadraticTo(r, t, r, t + cornerRad)
            }
        }
        
        // --- Right Sequence ---
        if (connRight) {
            // Open Right: Handle Inner Corners overlapping Right Edge
            if (connDown) {
                // Inner Bottom-Right
                path.moveTo(x + cellHalf, b)
                path.quadraticTo(x + cellHalf, y + cellHalf, r, y + cellHalf)
            }
            // Inner Top-Right handled by Top Sequence
        } else {
            // Closed Right: Draw Right Edge
            // Start
            if (connUp) path.moveTo(r, y - cellHalf)
            
            // End
            if (connDown) path.lineTo(r, y + cellHalf)
            else {
                path.lineTo(r, b - cornerRad)
                path.quadraticTo(r, b, r - cornerRad, b)
            }
        }
        
        // --- Bottom Sequence ---
        if (connDown) {
            // Open Bottom: Handle Inner Corners overlapping Bottom Edge
            if (connLeft) {
                // Inner Bottom-Left
                path.moveTo(l, y + cellHalf)
                path.quadraticTo(x - cellHalf, y + cellHalf, x - cellHalf, b)
            }
            // Inner Bottom-Right handled by Right Sequence
        } else {
            // Closed Bottom: Draw Bottom Edge
            // Start (from Right)
            if (connRight) path.moveTo(x + cellHalf, b)
            
            // End (to Left)
            if (connLeft) path.lineTo(x - cellHalf, b)
            else {
                path.lineTo(l + cornerRad, b)
                path.quadraticTo(l, b, l, b - cornerRad)
            }
        }
        
        // --- Left Sequence ---
        if (connLeft) {
            // Open Left. Inner corners handled by Top/Bottom sequences.
        } else {
            // Closed Left: Draw Left Edge
            // Start (from Bottom)
            if (connDown) path.moveTo(l, y + cellHalf)
            
            // End (to Top)
            if (connUp) path.lineTo(l, y - cellHalf)
            else {
                path.lineTo(l, t + cornerRad)
                // Loop closed by Top Sequence Start
            }
        }
        
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }

    private fun getWormDirection(segments: List<com.acesur.snake.game.WormSegment>): com.acesur.snake.game.Direction {
        if (segments.size < 2) return com.acesur.snake.game.Direction.RIGHT
        
        val head = segments[0].position
        val neck = segments[1].position
        
        return when {
            head.x > neck.x -> com.acesur.snake.game.Direction.RIGHT
            head.x < neck.x -> com.acesur.snake.game.Direction.LEFT
            head.y > neck.y -> com.acesur.snake.game.Direction.DOWN
            else -> com.acesur.snake.game.Direction.UP
        }
    }
    
    private fun DrawScope.drawWormFace(x: Float, y: Float, cellSize: Float, direction: com.acesur.snake.game.Direction, gameState: GameState) {
        // Large droopy eyes (Apple Worm style - big white with half-closed look)
        val eyeOffsetX = when (direction) {
            com.acesur.snake.game.Direction.LEFT -> -cellSize * 0.08f
            com.acesur.snake.game.Direction.RIGHT -> cellSize * 0.08f
            else -> 0f
        }
        val eyeOffsetY = when (direction) {
            com.acesur.snake.game.Direction.UP -> -cellSize * 0.08f
            com.acesur.snake.game.Direction.DOWN -> cellSize * 0.08f
            else -> -cellSize * 0.03f
        }
        
        val eyeSpacing = cellSize * 0.14f
        val eyeRadiusX = cellSize * 0.12f
        val eyeRadiusY = cellSize * 0.15f // Taller oval eyes
        
        if (gameState.isBlinking) {
            // Closed eyes (curved lines)
            val blinkPathLeft = Path().apply {
                moveTo(x - eyeSpacing - eyeRadiusX + eyeOffsetX, y + eyeOffsetY)
                quadraticTo(
                    x - eyeSpacing + eyeOffsetX, y + eyeOffsetY + cellSize * 0.05f,
                    x - eyeSpacing + eyeRadiusX + eyeOffsetX, y + eyeOffsetY
                )
            }
            drawPath(
                path = blinkPathLeft,
                color = Color.Black.copy(alpha = 0.7f),
                style = Stroke(width = cellSize * 0.04f, cap = StrokeCap.Round)
            )
            
            val blinkPathRight = Path().apply {
                moveTo(x + eyeSpacing - eyeRadiusX + eyeOffsetX, y + eyeOffsetY)
                quadraticTo(
                    x + eyeSpacing + eyeOffsetX, y + eyeOffsetY + cellSize * 0.05f,
                    x + eyeSpacing + eyeRadiusX + eyeOffsetX, y + eyeOffsetY
                )
            }
            drawPath(
                path = blinkPathRight,
                color = Color.Black.copy(alpha = 0.7f),
                style = Stroke(width = cellSize * 0.04f, cap = StrokeCap.Round)
            )
        } else {
            // Open eyes
            
            // Left eye white (large oval)
            drawOval(
                color = GameColors.WormEyeWhite,
                topLeft = Offset(x - eyeSpacing - eyeRadiusX + eyeOffsetX, y + eyeOffsetY - eyeRadiusY),
                size = Size(eyeRadiusX * 2f, eyeRadiusY * 2f)
            )
            // Left eye outline
            drawOval(
                color = GameColors.WormFace.copy(alpha = 0.4f),
                topLeft = Offset(x - eyeSpacing - eyeRadiusX + eyeOffsetX, y + eyeOffsetY - eyeRadiusY),
                size = Size(eyeRadiusX * 2f, eyeRadiusY * 2f),
                style = Stroke(width = cellSize * 0.015f)
            )
            // Left pupil (black dot, positioned lower for sleepy look)
            drawCircle(
                color = Color.Black,
                radius = eyeRadiusX * 0.45f,
                center = Offset(x - eyeSpacing + eyeOffsetX + eyeOffsetX * 0.3f, y + eyeOffsetY + cellSize * 0.03f)
            )
            // Left eye shine
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = cellSize * 0.025f,
                center = Offset(x - eyeSpacing + eyeOffsetX - cellSize * 0.02f, y + eyeOffsetY - cellSize * 0.02f)
            )
            
            // Right eye white (large oval)
            drawOval(
                color = GameColors.WormEyeWhite,
                topLeft = Offset(x + eyeSpacing - eyeRadiusX + eyeOffsetX, y + eyeOffsetY - eyeRadiusY),
                size = Size(eyeRadiusX * 2f, eyeRadiusY * 2f)
            )
            // Right eye outline
            drawOval(
                color = GameColors.WormFace.copy(alpha = 0.4f),
                topLeft = Offset(x + eyeSpacing - eyeRadiusX + eyeOffsetX, y + eyeOffsetY - eyeRadiusY),
                size = Size(eyeRadiusX * 2f, eyeRadiusY * 2f),
                style = Stroke(width = cellSize * 0.015f)
            )
            // Right pupil
            drawCircle(
                color = Color.Black,
                radius = eyeRadiusX * 0.45f,
                center = Offset(x + eyeSpacing + eyeOffsetX + eyeOffsetX * 0.3f, y + eyeOffsetY + cellSize * 0.03f)
            )
            // Right eye shine
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = cellSize * 0.025f,
                center = Offset(x + eyeSpacing + eyeOffsetX - cellSize * 0.02f, y + eyeOffsetY - cellSize * 0.02f)
            )
            
            // Droopy eyelids (for sleepy Apple Worm look)
            val eyelidPath = Path().apply {
                // Left eyelid
                moveTo(x - eyeSpacing - eyeRadiusX * 1.1f + eyeOffsetX, y + eyeOffsetY - eyeRadiusY * 0.5f)
                quadraticTo(
                    x - eyeSpacing + eyeOffsetX, y + eyeOffsetY - eyeRadiusY * 1.2f,
                    x - eyeSpacing + eyeRadiusX * 1.1f + eyeOffsetX, y + eyeOffsetY - eyeRadiusY * 0.5f
                )
            }
            drawPath(
                path = eyelidPath,
                color = GameColors.WormPink,
                style = Stroke(width = cellSize * 0.06f, cap = StrokeCap.Round)
            )
            
            val eyelidPathRight = Path().apply {
                // Right eyelid
                moveTo(x + eyeSpacing - eyeRadiusX * 1.1f + eyeOffsetX, y + eyeOffsetY - eyeRadiusY * 0.5f)
                quadraticTo(
                    x + eyeSpacing + eyeOffsetX, y + eyeOffsetY - eyeRadiusY * 1.2f,
                    x + eyeSpacing + eyeRadiusX * 1.1f + eyeOffsetX, y + eyeOffsetY - eyeRadiusY * 0.5f
                )
            }
            drawPath(
                path = eyelidPathRight,
                color = GameColors.WormPink,
                style = Stroke(width = cellSize * 0.06f, cap = StrokeCap.Round)
            )
        }
        
        // Cheeks (pink blush)
        drawCircle(
            color = GameColors.WormCheek.copy(alpha = 0.4f),
            radius = cellSize * 0.07f,
            center = Offset(x - cellSize * 0.25f, y + cellSize * 0.12f)
        )
        drawCircle(
            color = GameColors.WormCheek.copy(alpha = 0.4f),
            radius = cellSize * 0.07f,
            center = Offset(x + cellSize * 0.25f, y + cellSize * 0.12f)
        )
        
        // Thick lips/mouth (Apple Worm signature look - prominent pink lips)
        val mouthY = y + cellSize * 0.22f
        
        // Upper lip - thick and prominent
        val upperLipPath = Path().apply {
            moveTo(x - cellSize * 0.14f, mouthY)
            quadraticTo(x, mouthY - cellSize * 0.05f, x + cellSize * 0.14f, mouthY)
        }
        drawPath(
            path = upperLipPath,
            color = GameColors.WormLips,
            style = Stroke(width = cellSize * 0.07f, cap = StrokeCap.Round)
        )
        
        // Lower lip - full and plump
        val lowerLipPath = Path().apply {
            moveTo(x - cellSize * 0.12f, mouthY + cellSize * 0.02f)
            quadraticTo(x, mouthY + cellSize * 0.12f, x + cellSize * 0.12f, mouthY + cellSize * 0.02f)
        }
        drawPath(
            path = lowerLipPath,
            color = GameColors.WormLips,
            style = Stroke(width = cellSize * 0.06f, cap = StrokeCap.Round)
        )
    }
}
