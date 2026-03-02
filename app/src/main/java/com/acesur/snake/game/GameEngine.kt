package com.acesur.snake.game

import kotlinx.coroutines.delay

class GameEngine(private val gameState: GameState) {
    
    // Load a level into the game state
    fun loadLevel(levelIndex: Int): Boolean {
        val level = LevelRepository.getLevel(levelIndex) ?: return false
        
        gameState.reset()
        gameState.currentLevel = levelIndex
        gameState.gridWidth = level.width
        gameState.gridHeight = level.height
        
        // Parse and set the grid
        gameState.grid = Level.parseGrid(level.grid)
        
        // Set worm starting position
        level.wormStart.forEach { pos ->
            gameState.wormSegments.add(WormSegment(pos))
        }
        
        // Verify spawn validity (check for walls or out of bounds)
        val isSpawnValid = gameState.wormSegments.all { segment ->
            gameState.isValidPosition(segment.position) && 
            gameState.grid[segment.position.y][segment.position.x] != CellType.WALL
        }
        
        if (!isSpawnValid) {
            // Level configuration error: worm inside wall
            return false
        }
        
        // Set apples
        level.apples.forEach { pos ->
            gameState.apples.add(pos)
        }
        
        // Set boxes
        level.boxes.forEach { pos ->
            gameState.boxes.add(pos)
        }
        
        // Set portal
        gameState.portal = level.portal
        
        checkWormStuck()
        
        return true
    }
    
    // Move the worm in a direction
    suspend fun moveWorm(direction: Direction): Boolean {
        if (gameState.isAnimating || gameState.isLevelComplete || gameState.isGameOver) {
            return false
        }
        
        val head = gameState.getWormHead() ?: return false
        val newHeadPos = head.move(direction)
        
        // Check if move is valid
        if (!gameState.isValidPosition(newHeadPos)) {
            return false
        }
        
        if (!gameState.isWalkable(newHeadPos)) {
            return false
        }
        
        // Check if hitting own body (treat whole body including tail as solid wall)
        if (gameState.wormSegments.any { it.position == newHeadPos }) {
            return false
        }
        
        // Check for box - try to push it
        if (gameState.hasBox(newHeadPos)) {
            val boxNewPos = newHeadPos.move(direction)
            
            // Check if box can be pushed
            if (!canPushBox(newHeadPos, direction)) {
                return false
            }
            
            // Push the box
            pushBox(newHeadPos, direction)
        }
        
        gameState.isAnimating = true
        gameState.totalMoves++
        
        // Check for apple at new position
        val ateApple = gameState.hasApple(newHeadPos)
        if (ateApple) {
            gameState.apples.removeAll { it == newHeadPos }
            gameState.applesEaten++
        }
        
        // Move worm - add new head
        gameState.wormSegments.add(0, WormSegment(newHeadPos))
        
        // Check if head moved onto TRAP
        if (gameState.grid[newHeadPos.y][newHeadPos.x] == CellType.TRAP && 
            !gameState.hasBox(newHeadPos)) {
            gameState.isGameOver = true
        }
        
        // Remove tail if no apple was eaten
        if (!ateApple) {
            gameState.wormSegments.removeLastOrNull()
        }
        
        // Check for immediate portal entry BEFORE gravity
        if (gameState.isPortal(newHeadPos) && gameState.apples.isEmpty()) {
            delay(300) // Brief delay for satisfaction
            gameState.isLevelComplete = true
            gameState.isAnimating = false
            return true
        }

        // Apply gravity after move
        applyBoxGravity()
        applyWormGravity()
        
        delay(100) // Animation delay
        gameState.isAnimating = false
        
        checkWormStuck()
        
        return true
    }
    
    // Check if a box can be pushed in a direction
    private fun canPushBox(boxPos: Position, direction: Direction): Boolean {
        val newPos = boxPos.move(direction)
        
        // Check bounds
        if (!gameState.isValidPosition(newPos)) return false
        
        // Check for walls
        if (!gameState.isWalkable(newPos)) return false
        
        // Check for other boxes
        if (gameState.hasBox(newPos)) return false
        
        // Check for worm body
        if (gameState.hasWorm(newPos)) return false
        
        // Check for portal (can't push box into portal)
        if (gameState.isPortal(newPos)) return false
        
        // Check for apple (can't push box into apple)
        if (gameState.hasApple(newPos)) return false
        
        return true
    }
    
    // Push a box in a direction
    private fun pushBox(boxPos: Position, direction: Direction) {
        val newPos = boxPos.move(direction)
        val boxIndex = gameState.boxes.indexOfFirst { it == boxPos }
        if (boxIndex != -1) {
            gameState.boxes[boxIndex] = newPos
        }
    }
    
    // Apply gravity to boxes (they fall down if not supported)
    private suspend fun applyBoxGravity() {
        var anyFell: Boolean
        do {
            anyFell = false
            
            // Sort boxes by Y position (bottom first) to handle stacking correctly
            val sortedBoxIndices = gameState.boxes.indices.sortedByDescending { 
                gameState.boxes[it].y 
            }
            
            for (index in sortedBoxIndices) {
                val box = gameState.boxes[index]
                val below = box.move(Direction.DOWN)
                
                if (gameState.isValidPosition(below) && 
                    gameState.grid[below.y][below.x] != CellType.WALL && 
                    !gameState.hasBox(below) && 
                    !gameState.hasWorm(below) &&
                    !gameState.isPortal(below) &&
                    !gameState.hasApple(below)) {
                    
                    gameState.boxes[index] = below
                    anyFell = true
                }
            }
            
            if (anyFell) {
                delay(50) // Brief delay for falling animation
            }
        } while (anyFell)
    }

    // Apply gravity to the worm (it falls if not supported)
    private suspend fun applyWormGravity() {
        var falling = true
        while (falling) {
            // Check if ANY segment is supported by a solid block (Wall, Box, or Apple) underneath
            val isSupported = gameState.wormSegments.any { segment ->
                val below = segment.position.move(Direction.DOWN)
                
                if (!gameState.isValidPosition(below)) {
                    false // Out of bounds is not support (abyss)
                } else {
                    val cellType = gameState.grid[below.y][below.x]
                    val hasBox = gameState.hasBox(below)
                    val hasApple = gameState.hasApple(below) 
                    // Support comes from Wall, Box, Apple, or Trap (solid floor)
                    cellType == CellType.WALL || cellType == CellType.TRAP || hasBox || hasApple
                }
            }

            if (!isSupported) {
                // Determine new positions
                val newSegments = gameState.wormSegments.map { 
                    WormSegment(it.position.move(Direction.DOWN)) 
                }
                
                // Check if fallen out of bounds (Game Over)
                val outOfBounds = newSegments.any { !gameState.isValidPosition(it.position) }
                
                if (outOfBounds) {
                    gameState.isGameOver = true
                    falling = false
                } else {
                    // Update worm position
                    gameState.wormSegments.clear()
                    newSegments.forEach { gameState.wormSegments.add(it) }
                    
                    // Check if fell onto TRAP (unless covered by box)
                    val fellOnTrap = gameState.wormSegments.any { segment ->
                        val pos = segment.position
                        gameState.grid[pos.y][pos.x] == CellType.TRAP && !gameState.hasBox(pos)
                    }
                     
                    if (fellOnTrap) {
                        gameState.isGameOver = true
                        falling = false
                    }
                    
                    delay(100) // Falling animation delay
                    
                    // Check if fell into portal? (Optional mechanic, for now assume standard win condition requires eating apples first)
                    // If we fall into portal with apples remaining -> effectively game over as we passed it?
                    // Or if we fall into portal with 0 apples -> Win?
                    // Let's keep it simple: falling logic just moves worm. Win check is elsewhere or here.
                    val head = gameState.getWormHead()
                    if (head != null && gameState.isPortal(head) && gameState.apples.isEmpty()) {
                        delay(200)
                        gameState.isLevelComplete = true
                        falling = false
                    }
                }
            } else {
                falling = false
            }
        }
    }
    
    // Restart current level
    fun restartLevel() {
        loadLevel(gameState.currentLevel)
    }
    
    // Go to next level
    fun nextLevel(): Boolean {
        val nextIndex = gameState.currentLevel + 1
        if (nextIndex < LevelRepository.getTotalLevels()) {
            return loadLevel(nextIndex)
        }
        return false
    }
    
    // Check if there are more levels
    fun hasNextLevel(): Boolean {
        return gameState.currentLevel + 1 < LevelRepository.getTotalLevels()
    }
    
    // Check if the worm is unable to move in any direction
    private fun checkWormStuck() {
        val head = gameState.getWormHead() ?: return
        
        var canMoveAnywhere = false
        for (dir in Direction.values()) {
            val nextPos = head.move(dir)
            
            // Check if position is within bounds
            if (!gameState.isValidPosition(nextPos)) continue
            
            // Check if position is walkable (not a wall)
            if (!gameState.isWalkable(nextPos)) continue
            
            // Check if position is occupied by body
            if (gameState.wormSegments.any { it.position == nextPos }) continue
            
            // Check if it's a box and if it can be pushed
            if (gameState.hasBox(nextPos)) {
                if (canPushBox(nextPos, dir)) {
                    canMoveAnywhere = true
                    break
                } else {
                    continue
                }
            }
            
            // If we reach here, the move is possible
            canMoveAnywhere = true
            break
        }
        
        gameState.isStuck = !canMoveAnywhere
    }
}
