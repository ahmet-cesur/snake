package com.acesur.snake

import com.acesur.snake.game.CellType
import com.acesur.snake.game.Level
import com.acesur.snake.game.LevelRepository
import com.acesur.snake.game.Position
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelTest {
    @Test
    fun testAllLevelsValidity() {
        val levels = LevelRepository.levels
        
        levels.forEach { level ->
            println("Testing Level ${level.id}: ${level.name}")
            
            // 1. Check Dimensions
            assertTrue("Width must be > 0", level.width > 0)
            assertTrue("Height must be > 0", level.height > 0)
            assertTrue("Grid height matches", level.grid.size == level.height)
            level.grid.forEach { row ->
                assertTrue("Grid width matches", row.length == level.width)
            }
            
            val gridObj = Level.parseGrid(level.grid)
            
            // 2. Check Worm Start
            assertFalse("Worm start cannot be empty", level.wormStart.isEmpty())
            level.wormStart.forEach { pos ->
                assertTrue("Worm segment $pos out of bounds", 
                    pos.x in 0 until level.width && pos.y in 0 until level.height)
                
                val cellType = gridObj[pos.y][pos.x]
                assertFalse("Worm segment at $pos inside WALL", cellType == CellType.WALL)
                // Note: TRAP is a valid start position (dead on start), but let's warn
                if (cellType == CellType.TRAP) {
                    println("WARNING: Level ${level.id} starts with worm on TRAP at $pos")
                }
            }
            
            // 3. Check Boxes
            level.boxes.forEach { pos ->
                assertTrue("Box $pos out of bounds", 
                    pos.x in 0 until level.width && pos.y in 0 until level.height)
                
                val cellType = gridObj[pos.y][pos.x]
                assertFalse("Box at $pos inside WALL", cellType == CellType.WALL)
                // Boxes can represent obstacles or pushable blocks. Usually start on EMPTY/TRAP.
            }
            
            // 4. Check Portal
            val p = level.portal
            assertTrue("Portal $p out of bounds", 
                p.x in 0 until level.width && p.y in 0 until level.height)
            val pCell = gridObj[p.y][p.x]
            assertFalse("Portal at $p inside WALL", pCell == CellType.WALL)
            
            // 5. Check Overlaps
            // Worm + Box
            level.wormStart.forEach { w ->
                level.boxes.forEach { b ->
                    assertFalse("Worm segment at $w overlaps Box at $b", w == b)
                }
            }
            
            // Box + Box (Duplicates)
            val distinctBoxes = level.boxes.distinct()
            assertTrue("Duplicate boxes found", distinctBoxes.size == level.boxes.size)
            
            // Apple + Box
            level.apples.forEach { a ->
                level.boxes.forEach { b ->
                   if (a == b) println("WARNING: Apple at $a inside Box at $b (Hidden apple?)")
                }
            }
            
            // Portal + Box
            level.boxes.forEach { b ->
                if (b == level.portal) println("WARNING: Box at $b on Portal")
            }
            
            println("Level ${level.id} OK")
        }
    }
}
