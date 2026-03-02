package com.acesur.snake.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Direction enum for worm movement
enum class Direction {
    UP, DOWN, LEFT, RIGHT;
    
    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
    
    fun toOffset(): Pair<Int, Int> = when (this) {
        UP -> Pair(0, -1)
        DOWN -> Pair(0, 1)
        LEFT -> Pair(-1, 0)
        RIGHT -> Pair(1, 0)
    }
}

// Cell types in the game grid
enum class CellType {
    EMPTY,
    WALL,
    APPLE,
    PORTAL,
    BOX,
    TRAP
}

// Position on the grid
data class Position(val x: Int, val y: Int) {
    fun move(direction: Direction): Position {
        val (dx, dy) = direction.toOffset()
        return Position(x + dx, y + dy)
    }
    
    fun move(dx: Int, dy: Int): Position = Position(x + dx, y + dy)
}

// Worm segment
data class WormSegment(val position: Position)

// Game state
class GameState {
    // Worm body segments (head is first)
    val wormSegments = mutableStateListOf<WormSegment>()
    
    // Current level
    var currentLevel by mutableIntStateOf(0)
    
    // Grid dimensions
    var gridWidth by mutableIntStateOf(10)
    var gridHeight by mutableIntStateOf(14)
    
    // Static grid (walls, empty spaces)
    var grid: Array<Array<CellType>> = arrayOf()
    
    // Dynamic objects
    val apples = mutableStateListOf<Position>()
    val boxes = mutableStateListOf<Position>()
    var portal: Position? by mutableStateOf(null)
    
    // Game status
    var isLevelComplete by mutableStateOf(false)
    var isGameOver by mutableStateOf(false)
    var isAnimating by mutableStateOf(false)
    var isStuck by mutableStateOf(false)
    
    // Score
    var applesEaten by mutableIntStateOf(0)
    var totalMoves by mutableIntStateOf(0)

    // Animation state
    var isBlinking by mutableStateOf(false)
    var lastBlinkTime = 0L
    
    // Check if a position is valid (within grid bounds)
    fun isValidPosition(pos: Position): Boolean {
        return pos.x in 0 until gridWidth && 
               pos.y in 0 until gridHeight &&
               pos.y < grid.size && 
               (grid.isNotEmpty() && pos.x < grid[0].size)
    }
    
    // Check if a position is walkable (not a wall)
    fun isWalkable(pos: Position): Boolean {
        if (!isValidPosition(pos)) return false
        return grid[pos.y][pos.x] != CellType.WALL
    }
    
    // Check if position has a box
    fun hasBox(pos: Position): Boolean = boxes.any { it == pos }
    
    // Check if position has an apple
    fun hasApple(pos: Position): Boolean = apples.any { it == pos }
    
    // Check if position is the portal
    fun isPortal(pos: Position): Boolean = portal == pos
    
    // Check if position is occupied by worm
    fun hasWorm(pos: Position): Boolean = wormSegments.any { it.position == pos }
    
    // Check if the worm head can enter the portal
    fun canEnterPortal(): Boolean {
        val head = wormSegments.firstOrNull()?.position ?: return false
        return isPortal(head) && apples.isEmpty()
    }
    
    // Get the worm head position
    fun getWormHead(): Position? = wormSegments.firstOrNull()?.position
    
    // Check if there's ground support (for gravity - not falling into void)
    fun hasGroundSupport(pos: Position): Boolean {
        val below = pos.move(Direction.DOWN)
        if (!isValidPosition(below)) return true // Bottom of grid is supported
        if (grid[below.y][below.x] == CellType.WALL) return true
        if (hasBox(below)) return true
        if (hasWorm(below)) return true
        return false
    }
    
    // Reset the game state for a new level
    fun reset() {
        wormSegments.clear()
        apples.clear()
        boxes.clear()
        portal = null
        isLevelComplete = false
        isGameOver = false
        isAnimating = false
        isStuck = false
        applesEaten = 0
        totalMoves = 0
    }
}
