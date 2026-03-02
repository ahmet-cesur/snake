package com.acesur.snake.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acesur.snake.game.CellType
import com.acesur.snake.game.Level
import com.acesur.snake.game.LevelRepository
import com.acesur.snake.game.Position
import com.acesur.snake.ui.theme.GameColors
import kotlin.math.min

enum class EditorTool(val icon: ImageVector, val label: String, val color: Color) {
    WALL(Icons.Default.Lock, "Wall", GameColors.WallGray),
    EMPTY(Icons.Default.Clear, "Eraser", Color.LightGray),
    TRAP(Icons.Default.Warning, "Trap", Color(0xFFE74C3C)),
    BOX(Icons.Default.ShoppingCart, "Box", GameColors.WoodMedium),
    APPLE(Icons.Default.Favorite, "Apple", GameColors.AppleRed), 
    PORTAL(Icons.Default.Refresh, "Portal", GameColors.PortalPurple),
    WORM_HEAD(Icons.Default.Person, "Start", GameColors.WormPink),
    WORM_BODY(Icons.Default.Menu, "Grow", GameColors.WormPinkLight)
}

@Composable
fun LevelEditorScreen(
    levelToEdit: Level? = null,
    onBack: () -> Unit,
    onLevelModified: (Int) -> Unit = {},
    onTestLevel: (Int) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Editor State
    var gridWidth by remember { mutableStateOf(levelToEdit?.width ?: 16) }
    var gridHeight by remember { mutableStateOf(levelToEdit?.height ?: 10) }
    var minMoves by remember { mutableIntStateOf(levelToEdit?.minMoves ?: 10) }
    
    // Grid data
    val grid = remember { mutableStateListOf<MutableList<CellType>>() }
    
    // Objects
    val apples = remember { mutableStateListOf<Position>() }
    val boxes = remember { mutableStateListOf<Position>() }
    var portal by remember { mutableStateOf<Position?>(levelToEdit?.portal) }
    val wormSegments = remember { mutableStateListOf<Position>() }
    
    var currentTool by remember { mutableStateOf(EditorTool.WALL) }
    var showSettings by remember { mutableStateOf(false) }

    // Initialize grid
    LaunchedEffect(levelToEdit) {
        if (grid.isEmpty()) {
            if (levelToEdit != null) {
                // Load existing level
                val parsedGrid = Level.parseGrid(levelToEdit.grid)
                parsedGrid.forEach { rowArr ->
                    val rowList = mutableStateListOf<CellType>()
                    rowList.addAll(rowArr)
                    grid.add(rowList)
                }
                apples.clear()
                apples.addAll(levelToEdit.apples)
                boxes.clear()
                boxes.addAll(levelToEdit.boxes)
                wormSegments.clear()
                wormSegments.addAll(levelToEdit.wormStart)
                portal = levelToEdit.portal
            } else {
                // Default new level
                repeat(gridHeight) {
                    val row = mutableStateListOf<CellType>()
                    repeat(gridWidth) { row.add(CellType.EMPTY) }
                    grid.add(row)
                }
                wormSegments.add(Position(1, 1))
            }
        }
    }

    // Handle grid resize
    fun resizeGrid(newW: Int, newH: Int) {
        val oldGrid = grid.toList()
        grid.clear()
        repeat(newH) { y ->
            val row = mutableStateListOf<CellType>()
            repeat(newW) { x ->
                if (y < oldGrid.size && x < oldGrid[y].size) {
                    row.add(oldGrid[y][x])
                } else {
                    row.add(CellType.EMPTY)
                }
            }
            grid.add(row)
        }
        gridWidth = newW
        gridHeight = newH
        
        // Remove objects out of bounds
        apples.removeAll { it.x >= newW || it.y >= newH }
        boxes.removeAll { it.x >= newW || it.y >= newH }
        wormSegments.removeAll { it.x >= newW || it.y >= newH }
        if (portal != null && (portal!!.x >= newW || portal!!.y >= newH)) portal = null
    }

    fun createLevelObject(): Level {
        val gridStrings = grid.map { row ->
            row.joinToString("") { type ->
                when (type) {
                    CellType.WALL -> "W"
                    CellType.TRAP -> "X"
                    else -> "."
                }
            }
        }
        
        // Determine ID: Use existing if editing, otherwise find next available custom ID
        val newId = levelToEdit?.id ?: run {
            val maxCustom = LevelRepository.levels.filter { it.id >= LevelRepository.CUSTOM_LEVEL_ID_START }.maxOfOrNull { it.id } 
            maxCustom?.plus(1) ?: LevelRepository.CUSTOM_LEVEL_ID_START
        }
        
        return Level(
            id = newId,
            width = gridWidth,
            height = gridHeight,
            grid = gridStrings,
            wormStart = wormSegments.toList(),
            apples = apples.toList(),
            boxes = boxes.toList(),
            portal = portal ?: Position(0, 0),
            minMoves = minMoves
        )
    }

    fun handleTap(x: Int, y: Int) {
        if (x !in 0 until gridWidth || y !in 0 until gridHeight) return
        val pos = Position(x, y)
        
        when (currentTool) {
            EditorTool.WALL -> {
                if (grid[y][x] == CellType.WALL) {
                    grid[y][x] = CellType.EMPTY
                } else {
                    grid[y][x] = CellType.WALL
                    apples.remove(pos)
                    boxes.remove(pos)
                    if (portal == pos) portal = null
                    wormSegments.remove(pos)
                }
            }
            EditorTool.TRAP -> {
                if (grid[y][x] == CellType.TRAP) {
                    grid[y][x] = CellType.EMPTY
                } else {
                    grid[y][x] = CellType.TRAP
                    apples.remove(pos)
                    boxes.remove(pos)
                    if (portal == pos) portal = null
                    wormSegments.remove(pos)
                }
            }
            EditorTool.EMPTY -> {
                grid[y][x] = CellType.EMPTY
                apples.remove(pos)
                boxes.remove(pos)
                if (portal == pos) portal = null
                wormSegments.remove(pos)
            }
            EditorTool.BOX -> {
                if (boxes.contains(pos)) {
                    boxes.remove(pos)
                } else {
                    grid[y][x] = CellType.EMPTY
                    boxes.add(pos)
                    apples.remove(pos)
                    if (portal == pos) portal = null
                    wormSegments.remove(pos)
                }
            }
            EditorTool.APPLE -> {
                if (apples.contains(pos)) {
                    apples.remove(pos)
                } else {
                    grid[y][x] = CellType.EMPTY
                    apples.add(pos)
                    boxes.remove(pos)
                    if (portal == pos) portal = null
                    wormSegments.remove(pos)
                }
            }
            EditorTool.PORTAL -> {
                if (portal == pos) {
                    portal = null
                } else {
                    grid[y][x] = CellType.EMPTY
                    portal = pos
                    apples.remove(pos)
                    boxes.remove(pos)
                    wormSegments.remove(pos)
                }
            }
            EditorTool.WORM_HEAD -> {
                if (wormSegments.firstOrNull() == pos) {
                    wormSegments.clear()
                } else {
                    wormSegments.clear()
                    wormSegments.add(pos)
                    grid[y][x] = CellType.EMPTY 
                    apples.remove(pos)
                    boxes.remove(pos)
                    if (portal == pos) portal = null
                }
            }
            EditorTool.WORM_BODY -> {
                if (wormSegments.contains(pos)) {
                    val index = wormSegments.indexOf(pos)
                    // If clicking the very last segment, just remove it
                    if (index == wormSegments.lastIndex) {
                        wormSegments.removeAt(index)
                    } else if (index > 0) { 
                         // If clicking middle segment, cut the worm
                         val count = wormSegments.size - index
                         repeat(count) { wormSegments.removeAt(wormSegments.lastIndex) }
                    }
                } else if (wormSegments.isNotEmpty()) {
                    val last = wormSegments.last()
                    val dx = kotlin.math.abs(pos.x - last.x)
                    val dy = kotlin.math.abs(pos.y - last.y)
                    if (dx + dy == 1) {
                         wormSegments.add(pos)
                         grid[y][x] = CellType.EMPTY
                         apples.remove(pos)
                         boxes.remove(pos)
                         if (portal == pos) portal = null
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A252F)) // Sleek dark Navy
    ) {
        // --- Top Action Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (levelToEdit != null) "Level ${levelToEdit.id}" else "New Level",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text("${gridWidth}x${gridHeight}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val level = createLevelObject()
                    val code = formatLevelToKotlin(level)
                    clipboardManager.setText(AnnotatedString(code))
                    Toast.makeText(context, "Kotlin Code Copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Menu, "Export Code", tint = Color.Cyan)
                }

                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, "Grid Settings", tint = Color.White)
                }
                
                Button(
                    onClick = {
                        val newLevel = createLevelObject()
                        
                        // Check if we are updating a custom level or creating new
                        val existingIndex = LevelRepository.getIndexById(newLevel.id)
                        if (existingIndex != -1) {
                            LevelRepository.updateLevel(existingIndex, newLevel)
                        } else {
                            LevelRepository.addLevel(newLevel)
                        }
                        
                        LevelRepository.saveAllLevels(context)
                        Toast.makeText(context, "Level Saved!", Toast.LENGTH_SHORT).show()
                        
                        // Find index to test
                        val testIndex = LevelRepository.getIndexById(newLevel.id)
                        
                        // Reset records as the level layout changed
                        onLevelModified(testIndex)
                        
                        onTestLevel(testIndex)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GameColors.ButtonPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("SAVE & TEST")
                }
            }
        }
        
        if (wormSegments.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE74C3C))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Warning: Worm Start Position is missing!", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- Main Editor Area (Grid) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            EditorGrid(
                width = gridWidth,
                height = gridHeight,
                grid = grid,
                apples = apples,
                boxes = boxes,
                portal = portal,
                worm = wormSegments,
                onTap = { x, y -> handleTap(x, y) }
            )
        }
        
        // --- Bottom Tool Palette ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(bottom = 24.dp, top = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PAINT TOOL", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Clear Obstacles
                    Text(
                        "CLEAR OBSTACLES", 
                        color = Color(0xFFF39C12), 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            grid.forEach { row -> row.indices.forEach { row[it] = CellType.EMPTY } }
                            apples.clear()
                            boxes.clear()
                        }
                    )
                    
                    // Quick Clear
                    Text(
                        "CLEAR ALL", 
                        color = Color(0xFFE74C3C), 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            grid.forEach { row -> row.indices.forEach { row[it] = CellType.EMPTY } }
                            apples.clear()
                            boxes.clear()
                            portal = null
                            wormSegments.clear()
                            wormSegments.add(Position(1,1))
                        }
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tools = EditorTool.values()
                val midPoint = (tools.size + 1) / 2
                
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tools.take(midPoint).forEach { tool ->
                        EditorToolItem(
                            tool = tool,
                            isSelected = currentTool == tool,
                            onClick = { currentTool = tool }
                        )
                    }
                }
                
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tools.drop(midPoint).forEach { tool ->
                        EditorToolItem(
                            tool = tool,
                            isSelected = currentTool == tool,
                            onClick = { currentTool = tool }
                        )
                    }
                }
            }
        }
    }
    
    // --- Grid Settings Dialog ---
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Level Settings") },
            text = {
                Column {
                    Text(
                        text = if (levelToEdit != null) "Editing Level ${levelToEdit.id}" else "Creating New Level",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text("Grid Width: $gridWidth")
                    Slider(
                        value = gridWidth.toFloat(),
                        onValueChange = { gridWidth = it.toInt() },
                        valueRange = 8f..24f,
                        steps = 16
                    )
                    
                    Text("Grid Height: $gridHeight")
                    Slider(
                        value = gridHeight.toFloat(),
                        onValueChange = { gridHeight = it.toInt() },
                        valueRange = 6f..16f,
                        steps = 10
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("Min Moves (3 Stars): $minMoves")
                    Slider(
                        value = minMoves.toFloat(),
                        onValueChange = { minMoves = it.toInt() },
                        valueRange = 5f..100f,
                        steps = 95
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    resizeGrid(gridWidth, gridHeight)
                    showSettings = false
                }) {
                    Text("Apply & Resize")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EditorToolItem(tool: EditorTool, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clickable(onClick = onClick)
    ) {
        val bg = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .border(2.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
             Icon(
                 imageVector = tool.icon,
                 contentDescription = tool.label,
                 tint = if (isSelected) Color.White else tool.color.copy(alpha = 0.8f),
                 modifier = Modifier.size(24.dp)
             )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = tool.label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun EditorGrid(
    width: Int,
    height: Int,
    grid: List<List<CellType>>,
    apples: List<Position>,
    boxes: List<Position>,
    portal: Position?,
    worm: List<Position>,
    onTap: (Int, Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        
        // Calculate cell size to best fit the box
        val maxCellW = canvasWidth / width
        val maxCellH = canvasHeight / height
        val cellSize = min(maxCellW, maxCellH)
        
        val gridPixelW = cellSize * width
        val gridPixelH = cellSize * height
        val left = (canvasWidth - gridPixelW) / 2
        val top = (canvasHeight - gridPixelH) / 2

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(width, height) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            val x = ((change.position.x - left) / cellSize).toInt()
                            val y = ((change.position.y - top) / cellSize).toInt()
                            onTap(x, y)
                        }
                    )
                }
                .pointerInput(width, height) {
                    detectTapGestures(
                        onTap = { offset ->
                            val x = ((offset.x - left) / cellSize).toInt()
                            val y = ((offset.y - top) / cellSize).toInt()
                            onTap(x, y)
                        }
                    )
                }
        ) {
            // Draw background (Sky-ish)
            drawRect(Color(0xFF2C3E50).copy(alpha = 0.3f), topLeft = Offset(left, top), size = Size(gridPixelW, gridPixelH))
            
            // Draw Grid lines
            for (i in 0..width) {
                drawLine(Color.White.copy(alpha = 0.1f), start = Offset(left + i*cellSize, top), end = Offset(left + i*cellSize, top + gridPixelH))
            }
            for (j in 0..height) {
                drawLine(Color.White.copy(alpha = 0.1f), start = Offset(left, top + j*cellSize), end = Offset(left + gridPixelW, top + j*cellSize))
            }
            
            // Draw Content
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val cX = left + x * cellSize
                    val cY = top + y * cellSize
                    
                    if (y < grid.size && x < grid[y].size) {
                        when (grid[y][x]) {
                            CellType.WALL -> {
                                drawRect(GameColors.WallGray, topLeft = Offset(cX+1, cY+1), size = Size(cellSize-2, cellSize-2))
                                drawRect(GameColors.WallOutline, topLeft = Offset(cX+1, cY+1), size = Size(cellSize-2, cellSize-2), style = Stroke(width = 2f))
                            }
                            CellType.TRAP -> {
                                drawRect(Color(0xFFE74C3C).copy(alpha = 0.4f), topLeft = Offset(cX, cY), size = Size(cellSize, cellSize))
                                drawRect(Color(0xFFE74C3C), topLeft = Offset(cX, cY), size = Size(cellSize, cellSize), style = Stroke(width = 2f))
                            }
                            else -> {}
                        }
                    }
                }
            }
            
            // Draw Boxes
            boxes.forEach { box ->
                val cX = left + box.x * cellSize
                val cY = top + box.y * cellSize
                drawRect(GameColors.WoodMedium, topLeft = Offset(cX + 4, cY + 4), size = Size(cellSize - 8, cellSize - 8))
                drawRect(GameColors.WoodDark, topLeft = Offset(cX + 4, cY + 4), size = Size(cellSize - 8, cellSize - 8), style = Stroke(width = 2f))
            }
            
            // Draw Apples
            apples.forEach { apple ->
                val cX = left + apple.x * cellSize + cellSize/2
                val cY = top + apple.y * cellSize + cellSize/2
                drawCircle(GameColors.AppleRed, radius = cellSize * 0.35f, center = Offset(cX, cY))
                drawCircle(Color.White.copy(alpha = 0.3f), radius = cellSize * 0.1f, center = Offset(cX - cellSize*0.1f, cY - cellSize*0.1f))
            }
            
            // Draw Portal
            portal?.let { p ->
                val cX = left + p.x * cellSize + cellSize/2
                val cY = top + p.y * cellSize + cellSize/2
                drawCircle(GameColors.PortalPurple, radius = cellSize * 0.45f, center = Offset(cX, cY))
                drawCircle(GameColors.PortalGlow, radius = cellSize * 0.25f, center = Offset(cX, cY))
            }
            
            // Draw Worm
            worm.forEachIndexed { index, pos ->
                val cX = left + pos.x * cellSize + cellSize/2
                val cY = top + pos.y * cellSize + cellSize/2
                val color = if (index == 0) GameColors.WormPink else GameColors.WormPinkLight
                drawCircle(color, radius = cellSize * 0.4f, center = Offset(cX, cY))
                drawCircle(Color.Black.copy(alpha = 0.2f), radius = cellSize * 0.4f, center = Offset(cX, cY), style = Stroke(width = 2f))
                
                if (index == 0) {
                     // Eyes
                     drawCircle(Color.White, radius = cellSize * 0.12f, center = Offset(cX - cellSize*0.12f, cY - cellSize*0.05f))
                     drawCircle(Color.White, radius = cellSize * 0.12f, center = Offset(cX + cellSize*0.12f, cY - cellSize*0.05f))
                     drawCircle(Color.Black, radius = cellSize * 0.05f, center = Offset(cX - cellSize*0.12f, cY - cellSize*0.02f))
                     drawCircle(Color.Black, radius = cellSize * 0.05f, center = Offset(cX + cellSize*0.12f, cY - cellSize*0.02f))
                }
            }
        }
    }
}

fun formatLevelToKotlin(level: Level): String {
    val gridStr = level.grid.joinToString(",\n        ") { "\"$it\"" }
    val wormStr = level.wormStart.joinToString(", ") { "Position(${it.x}, ${it.y})" }
    val appleStr = level.apples.joinToString(", ") { "Position(${it.x}, ${it.y})" }
    val boxStr = level.boxes.joinToString(", ") { "Position(${it.x}, ${it.y})" }
    val portalStr = "Position(${level.portal.x}, ${level.portal.y})"

    return """
        Level(
            id = ${level.id},
            width = ${level.width},
            height = ${level.height},
            grid = listOf(
                $gridStr
            ),
            wormStart = listOf($wormStr),
            apples = listOf($appleStr),
            boxes = listOf($boxStr),
            portal = $portalStr,
            minMoves = ${level.minMoves}
        )
    """.trimIndent()
}
