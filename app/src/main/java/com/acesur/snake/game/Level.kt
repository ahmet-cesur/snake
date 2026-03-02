package com.acesur.snake.game
// Level data structure
data class Level(
    val id: Int,
    val width: Int,
    val height: Int,
    val grid: List<String>, // String representation of the grid
    val wormStart: List<Position>, // Initial worm segments (head first)
    val apples: List<Position>,
    val boxes: List<Position>,
    val portal: Position,
    val minMoves: Int = 10
) {
    companion object {
        // Parse grid from string representation
        // W = Wall, . = Empty, A = Apple, P = Portal, B = Box, H = Worm Head, T = Worm Tail
        fun parseGrid(gridStrings: List<String>): Array<Array<CellType>> {
            return gridStrings.map { row ->
                row.map { char ->
                    when (char) {
                        'W', '#' -> CellType.WALL
                        'A' -> CellType.EMPTY // Apples are tracked separately
                        'P' -> CellType.EMPTY // Portal is tracked separately
                        'B' -> CellType.EMPTY // Boxes are tracked separately
                        'X' -> CellType.TRAP
                        'H', 'T', '1', '2', '3' -> CellType.EMPTY // Worm positions
                        else -> CellType.EMPTY
                    }
                }.toTypedArray()
            }.toTypedArray()
        }
    }
}
// Level repository with 10 handcrafted levels
object LevelRepository {
    var levels: MutableList<Level> = mutableListOf(
        // Level 1: Intro
        Level(
            id = 1,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                ".......WWW....W.",
                "...WWWWW.WWWWWW.",
                "..WW............",
                "................"
            ),
            wormStart = listOf(Position(3, 6), Position(2, 6), Position(2, 7)),
            apples = listOf(Position(8, 3)),
            boxes = listOf(),
            portal = Position(14, 5),
            minMoves = 15
        ),
        // Level 2: The Step
        Level(
            id = 2,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "......WWW.......",
                "......W.W.......",
                "......W.W.......",
                "....WWW.WW..WWW.",
                "................",
                "................",
                "................",
                "................"
            ),
            wormStart = listOf(Position(5, 4), Position(4, 4), Position(3, 4)),
            apples = listOf(Position(3, 2)),
            boxes = listOf(),
            portal = Position(14, 2),
            minMoves = 21
        ),
        // Level 3: The Gap
        Level(
            id = 3,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                "................",
                "..WWWW....WWW...",
                "........WWW.....",
                "................",
                "................",
                "................",
                "................"
            ),
            wormStart = listOf(Position(5, 3), Position(4, 3), Position(3, 3)),
            apples = listOf(Position(7, 2)),
            boxes = listOf(),
            portal = Position(12, 3),
            minMoves = 9
        ),
        // Level 4: The Hook
        Level(
            id = 4,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                ".....WWW..WWW...",
                "....WW.WWWW.....",
                "....W..W........",
                ".......W........",
                "WWWWWW.W........",
                ".....WWW........",
                "................"
            ),
            wormStart = listOf(Position(2, 6), Position(1, 6), Position(0, 6)),
            apples = listOf(Position(5, 5)),
            boxes = listOf(),
            portal = Position(12, 5),
            minMoves = 27
        ),
        // Level 5: Hook - U turn
        Level(
            id = 5,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "..WW.WW..WW.....",
                "..W...W.........",
                "..W...W.........",
                "..WWW.W.........",
                "....WWW.........",
                "................",
                "................",
                "................"
            ),
            wormStart = listOf(Position(2, 1), Position(1, 1), Position(1, 2)),
            apples = listOf(Position(4, 4)),
            boxes = listOf(),
            portal = Position(10, 1),
            minMoves = 19
        ),
        // Level 6: The Pit - Dip and climb
        Level(
            id = 6,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                "................",
                ".WWW.WWW.W......",
                "...W.W...W......",
                "...W...W.W......",
                "...WWWWW.W......",
                ".......WWW......",
                "................"
            ),
            wormStart = listOf(Position(3, 3), Position(2, 3), Position(1, 3)),
            apples = listOf(Position(6, 6)),
            boxes = listOf(),
            portal = Position(12, 2),
            minMoves = 24
        ),
        // Level 7: Reach - Extend yourself
        Level(
            id = 7,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                "................",
                ".WWWW...........",
                ".W..W......W....",
                ".W.........W....",
                ".WWWW..WWW.W....",
                "................",
                "................"
            ),
            wormStart = listOf(Position(3, 3), Position(2, 3), Position(1, 3)),
            apples = listOf(Position(6, 6)),
            boxes = listOf(),
            portal = Position(11, 8),
            minMoves = 22
        ),
        // Level 8
        Level(
            id = 8,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "....W..WW.......",
                "....W...........",
                "....W...W.......",
                "................",
                ".WWWWW...WWWW...",
                "................",
                "................",
                "................"
            ),
            wormStart = listOf(Position(3, 5), Position(2, 5), Position(1, 5)),
            apples = listOf(Position(6, 4)),
            boxes = listOf(),
            portal = Position(12, 7),
            minMoves = 37
        ),
        // Level 9
        Level(
            id = 9,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                ".......WWWW.....",
                ".......W..W.....",
                "..........W.....",
                "......WW..W.....",
                ".......WWWW.....",
                "................",
                "................"
            ),
            wormStart = listOf(Position(7, 2), Position(8, 2), Position(9, 2)),
            apples = listOf(Position(8, 4)),
            boxes = listOf(),
            portal = Position(13, 4),
            minMoves = 25
        ),
        // Level 10
        Level(
            id = 10,
            width = 16,
            height = 10,
            grid = listOf(
                "........WWWWWW..",
                "........WWWWWW..",
                "........WW......",
                "........WW......",
                "WWWWWWW.WW..WWWW",
                "WWWWWW..W...WWWW",
                "....WW......WW..",
                "....WW..WWWWWW..",
                "....WWW.WWWWWW..",
                "................"
            ),
            wormStart = listOf(Position(5, 3), Position(4, 3), Position(3, 3)),
            apples = listOf(Position(7, 7)),
            boxes = listOf(),
            portal = Position(13, 3),
            minMoves = 31
        ),
        // Level 11
        Level(
            id = 11,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                "................",
                ".....W..........",
                "................",
                "................",
                "....WWWWWWW.....",
                "................",
                "................"
            ),
            wormStart = listOf(Position(6, 4), Position(6, 3), Position(5, 3)),
            apples = listOf(Position(5, 6)),
            boxes = listOf(Position(7, 6)),
            portal = Position(10, 2),
            minMoves = 15
        ),
        // Level 12
        Level(
            id = 12,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "......W.........",
                "......W.W.......",
                "................",
                "................",
                "WWW.WWW.WWW..WWW",
                "WWWWWWWWWWW..WWW",
                "............WW..",
                "................"
            ),
            wormStart = listOf(Position(4, 5), Position(3, 5), Position(2, 5)),
            apples = listOf(Position(7, 3)),
            boxes = listOf(Position(10, 5)),
            portal = Position(2, 1),
            minMoves = 24
        ),
        // Level 13
        Level(
            id = 13,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                ".......W........",
                ".....W.W........",
                ".......W........",
                "................",
                "...WWWWWWWW.....",
                "................",
                "................"
            ),
            wormStart = listOf(Position(5, 6), Position(4, 6), Position(3, 6)),
            apples = listOf(Position(8, 5)),
            boxes = listOf(Position(7, 2)),
            portal = Position(13, 4),
            minMoves = 28
        ),
        // Level 14
        Level(
            id = 14,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                ".....WW.W.WWWW..",
                ".....WW.W.WW....",
                "....WWW...WW....",
                "WWWWWWW...W.....",
                "......W.WWW.....",
                "......WWW.......",
                "................"
            ),
            wormStart = listOf(Position(3, 5), Position(2, 5), Position(1, 5)),
            apples = listOf(Position(8, 6)),
            boxes = listOf(Position(8, 5)),
            portal = Position(13, 4),
            minMoves = 27
        ),
        // Level 15
        Level(
            id = 15,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                ".....W..........",
                "................",
                "....W.W.........",
                "................",
                "....WWWWW.......",
                "................",
                "................",
                ".........WWWW..."
            ),
            wormStart = listOf(Position(4, 5), Position(3, 5), Position(3, 4)),
            apples = listOf(Position(5, 4)),
            boxes = listOf(Position(5, 3)),
            portal = Position(15, 6),
            minMoves = 38
        ),
        // Level 16
        Level(
            id = 16,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                "................",
                ".........W......",
                ".......WWW......",
                "................",
                "..WWWWWWWW.WWW..",
                ".........WWW....",
                "................"
            ),
            wormStart = listOf(Position(4, 6), Position(3, 6), Position(2, 6)),
            apples = listOf(Position(2, 3)),
            boxes = listOf(Position(6, 6), Position(7, 4)),
            portal = Position(13, 2),
            minMoves = 66
        ),
        // Level 17: Hoard - Blocks for descent
        Level(
            id = 17,
            width = 16,
            height = 10,
            grid = listOf(
                "....WWWWWWWW....",
                "...........W....",
                "...........W....",
                ".........WWW....",
                ".........W......",
                "....WWW..W......",
                "........WW......",
                "................",
                ".W....W.....WWW.",
                ".WWWWWWWW......."
            ),
            wormStart = listOf(Position(4, 3), Position(3, 3), Position(3, 4)),
            apples = listOf(Position(8, 3)),
            boxes = listOf(Position(9, 2), Position(4, 4)),
            portal = Position(15, 6),
            minMoves = 37
        ),
        // Level 18: Cramped - Stacking blocks
        Level(
            id = 18,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                "....WWWW.WWWW...",
                ".....WWW.WWW....",
                ".....W.....W....",
                ".....W.....W....",
                ".....WW..WWW....",
                "......WWWW......",
                "................"
            ),
            wormStart = listOf(Position(6, 2), Position(5, 2), Position(4, 2)),
            apples = listOf(Position(7, 6)),
            boxes = listOf(Position(7, 5), Position(9, 6)),
            portal = Position(12, 4),
            minMoves = 25
        ),
        // Level 19: Sparse - Limited ground
        Level(
            id = 19,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                "................",
                "................",
                "................",
                ".....W.....W....",
                ".....W..........",
                ".....W..........",
                "..WWWWW....WW...",
                "..WWWWW...WWW...",
                "..WWWWW...WWW..."
            ),
            wormStart = listOf(Position(5, 3), Position(4, 3), Position(4, 4)),
            apples = listOf(Position(8, 4)),
            boxes = listOf(Position(11, 5), Position(11, 6)),
            portal = Position(15, 4),
            minMoves = 39
        ),
        // Level 20: Divider - Complex block puzzle
        Level(
            id = 20,
            width = 16,
            height = 10,
            grid = listOf(
                "................",
                ".....W..........",
                ".....W..........",
                ".....W..........",
                ".....W..........",
                "...WWWWW....WWW.",
                "............W...",
                "............W...",
                ".W.WWWWWWW..W...",
                ".WWWWWWWWWWWW..."
            ),
            wormStart = listOf(Position(9, 7), Position(8, 7), Position(7, 7)),
            apples = listOf(Position(2, 6)),
            boxes = listOf(Position(3, 4), Position(7, 4)),
            portal = Position(14, 4),
            minMoves = 87
        )
    )
    init {
        // Populate to 100 levels if not already present
        val currentMax = if (levels.isEmpty()) 0 else levels.maxOf { it.id }
        if (currentMax < 100) {
            for (id in (currentMax + 1)..100) {
                levels.add(
                    Level(
                        id = id,
                        width = 16,
                        height = 10,
                        grid = listOf(
                            "................",
                            "................",
                            "................",
                            "................",
                            "................",
                            ".......P........",
                            ".......WW.......",
                            "WWWWWWWWWWWWWWWW",
                            "................",
                            "................"
                        ),
                        wormStart = listOf(Position(2, 6), Position(1, 6), Position(0, 6)),
                        apples = listOf(Position(13, 6)),
                        boxes = emptyList(),
                        portal = Position(7, 5),
                        minMoves = 10
                    )
                )
            }
        }
    }
    const val CUSTOM_LEVEL_ID_START = 101
    fun getLevel(index: Int): Level? {
        return levels.getOrNull(index)
    }
    fun getLevelById(id: Int): Level? {
        // Standardize all levels to 16x10 before returning
        return levels.find { it.id == id }?.let { standardizeLevel(it) }
    }
    fun getIndexById(id: Int): Int {
        return levels.indexOfFirst { it.id == id }
    }
    fun getTotalLevels(): Int = levels.size
    fun updateLevel(index: Int, level: Level) {
        if (index in levels.indices) {
            levels[index] = level
        }
    }
    fun addLevel(level: Level) {
        levels.add(level)
    }
    fun deleteLevel(id: Int) {
        levels.removeAll { it.id == id }
    }
    fun loadSavedLevels(context: android.content.Context) {
        val saved = LevelStorage.loadCustomLevels(context)
        if (saved.isNotEmpty()) {
             // Only load levels that are truly custom (ID >= 101)
             // This ensures hardcoded built-in levels (1-100) aren't overridden by old saved data
             saved.filter { it.id >= CUSTOM_LEVEL_ID_START }.forEach { custom ->
                 levels.removeAll { it.id == custom.id }
                 levels.add(custom)
             }
        }
        // Standardize all levels to 16x10
        levels = standardizeLevels(levels).toMutableList()
    }
    private fun standardizeLevel(level: Level): Level {
        if (level.width == 16 && level.height == 10) return level
        // Basic standardization logic: Take first 10 rows and 16 chars
        val newGrid = level.grid.take(10).map { row ->
            row.padEnd(16, '.').take(16)
        }.let { grid ->
            if (grid.size < 10) {
                grid + List(10 - grid.size) { ".".repeat(16) }
            } else grid
        }
        return level.copy(
            width = 16,
            height = 10,
            grid = newGrid,
            // Keep positions as they are, hope for the best
            wormStart = level.wormStart.filter { it.x < 16 && it.y < 10 },
            apples = level.apples.filter { it.x < 16 && it.y < 10 },
            boxes = level.boxes.filter { it.x < 16 && it.y < 10 },
            portal = if (level.portal.x < 16 && level.portal.y < 10) level.portal else Position(8, 5)
        )
    }
    private fun standardizeLevels(list: List<Level>): List<Level> {
        return list.map { standardizeLevel(it) }
    }
    fun saveAllLevels(context: android.content.Context) {
        LevelStorage.saveCustomLevels(context, levels)
    }
}
