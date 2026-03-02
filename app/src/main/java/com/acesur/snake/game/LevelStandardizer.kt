package com.acesur.snake.game

/**
 * Standardizes levels to a 16x10 grid size.
 */
fun standardizeLevels(levels: List<Level>): List<Level> {
    val TARGET_WIDTH = 16
    val TARGET_HEIGHT = 10
    
    return levels.map { level ->
        if (level.width == TARGET_WIDTH && level.height == TARGET_HEIGHT) {
            level
        } else {
            // Create a new grid with target dimensions
            val newGrid = MutableList(TARGET_HEIGHT) { ".".repeat(TARGET_WIDTH) }
            
            // Calculate offsets to center the old grid or just place it at top-left
            // Placing at top-left is safer for coordinate-based objects (worm, apples, etc.)
            
            for (y in 0 until minOf(level.grid.size, TARGET_HEIGHT)) {
                val oldRow = level.grid[y]
                val rowToCopy = if (oldRow.length > TARGET_WIDTH) {
                    oldRow.substring(0, TARGET_WIDTH)
                } else {
                    oldRow.padEnd(TARGET_WIDTH, '.')
                }
                newGrid[y] = rowToCopy
            }
            
            // Filter out objects that are now out of bounds
            val newWormStart = level.wormStart.filter { it.x < TARGET_WIDTH && it.y < TARGET_HEIGHT }
            val newApples = level.apples.filter { it.x < TARGET_WIDTH && it.y < TARGET_HEIGHT }
            val newBoxes = level.boxes.filter { it.x < TARGET_WIDTH && it.y < TARGET_HEIGHT }
            val newPortal = if (level.portal.x < TARGET_WIDTH && level.portal.y < TARGET_HEIGHT) {
                level.portal
            } else {
                Position(0, 0) // Should not happen with 16x10 usually
            }
            
            level.copy(
                width = TARGET_WIDTH,
                height = TARGET_HEIGHT,
                grid = newGrid,
                wormStart = if (newWormStart.isNotEmpty()) newWormStart else level.wormStart,
                apples = newApples,
                boxes = newBoxes,
                portal = newPortal
            )
        }
    }
}
