package com.acesur.snake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.acesur.snake.game.LevelProgressManager
import com.acesur.snake.game.LevelRepository
import com.acesur.snake.ui.GameScreen
import com.acesur.snake.ui.LevelSelectionScreen
import com.acesur.snake.ui.MainMenuScreen
import com.acesur.snake.ui.LevelEditorScreen
import com.acesur.snake.ui.LevelManagerScreen
import com.acesur.snake.game.Level
import com.acesur.snake.ui.theme.SnakeTheme

enum class Screen {
    MAIN_MENU,
    LEVEL_SELECTION,
    GAME,
    LEVEL_EDITOR,
    LEVEL_EDITOR_EDIT
}

class MainActivity : ComponentActivity() {
    
    private lateinit var levelProgressManager: LevelProgressManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        levelProgressManager = LevelProgressManager(this)
        LevelRepository.loadSavedLevels(applicationContext)
        
        // One-time reset of scores to match current Level.kt definitions 
        // We use a preference flag to ensure this only runs once after this update
        val prefs = getSharedPreferences("app_migration", MODE_PRIVATE)
        if (!prefs.getBoolean("scores_reset_v1", false)) {
            LevelRepository.levels.forEachIndexed { index, level ->
                levelProgressManager.forceSaveBestScore(index, level.minMoves)
            }
            prefs.edit().putBoolean("scores_reset_v1", true).apply()
        }
        
        enableEdgeToEdge()
        setContent {
            SnakeTheme {
                var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }
                var selectedLevel by remember { mutableIntStateOf(levelProgressManager.getLastPlayedLevel()) }
                var editingLevel by remember { mutableStateOf<Level?>(null) }
                var highestUnlockedLevel by remember {  
                    mutableIntStateOf(levelProgressManager.getHighestUnlockedLevel()) 
                }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        Screen.MAIN_MENU -> {
                            MainMenuScreen(
                                onPlayClicked = {
                                    // selectedLevel is already initialized to last played
                                    currentScreen = Screen.GAME
                                },
                                onSelectLevelClicked = {
                                    currentScreen = Screen.LEVEL_SELECTION
                                },
                                onLevelEditorClicked = {
                                    currentScreen = Screen.LEVEL_EDITOR
                                }
                            )
                        }
                        
                        Screen.LEVEL_SELECTION -> {
                            LevelSelectionScreen(
                                highestUnlockedLevel = highestUnlockedLevel,
                                onLevelSelected = { level ->
                                    selectedLevel = level
                                    levelProgressManager.saveLastPlayedLevel(level)
                                    currentScreen = Screen.GAME
                                },
                                onBackToMenu = {
                                    currentScreen = Screen.MAIN_MENU
                                }
                            )
                        }
                        
                        Screen.GAME -> {
                            GameScreen(
                                startingLevel = selectedLevel,
                                getBestScore = { index -> 
                                    val level = LevelRepository.getLevel(index)
                                    levelProgressManager.getBestScore(index, level?.minMoves ?: 50) 
                                },
                                onLevelStarted = { level ->
                                    levelProgressManager.saveLastPlayedLevel(level)
                                },
                                onLevelComplete = { completedLevel, moves ->
                                    // Update best score for stars
                                    levelProgressManager.saveBestScore(completedLevel, moves)
                                    // Unlock the next level
                                    levelProgressManager.onLevelCompleted(completedLevel + 1)
                                    highestUnlockedLevel = levelProgressManager.getHighestUnlockedLevel()
                                },
                                onBackToMenu = {
                                    currentScreen = Screen.MAIN_MENU
                                }
                            )
                        }

                        
                        Screen.LEVEL_EDITOR -> {
                            LevelManagerScreen(
                                onEditLevel = { level ->
                                    editingLevel = level
                                    currentScreen = Screen.LEVEL_EDITOR_EDIT
                                },
                                onBack = {
                                    currentScreen = Screen.MAIN_MENU
                                }
                            )
                        }
                        
                        Screen.LEVEL_EDITOR_EDIT -> {
                            LevelEditorScreen(
                                levelToEdit = editingLevel,
                                onBack = {
                                    currentScreen = Screen.LEVEL_EDITOR
                                },
                                onLevelModified = { index ->
                                    levelProgressManager.resetBestScore(index)
                                },
                                onTestLevel = { levelIndex ->
                                    selectedLevel = levelIndex
                                    currentScreen = Screen.GAME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}