package com.acesur.snake.game

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages level progress - tracks which levels are unlocked.
 * First 3 levels are unlocked by default.
 * Completing a level unlocks the next one.
 */
class LevelProgressManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "level_progress"
        private const val KEY_HIGHEST_UNLOCKED = "highest_unlocked_level"
        private const val KEY_LAST_PLAYED = "last_played_level"
        private const val KEY_BEST_SCORE_PREFIX = "best_score_level_"
        private const val DEFAULT_UNLOCKED_LEVELS = 3 // First 3 levels unlocked by default
    }
    
    /**
     * Get the personal best score (min moves) achieved for this level index.
     * Falls back to a default value if never played.
     */
    fun getBestScore(levelIndex: Int, fallback: Int = 50): Int {
        val key = KEY_BEST_SCORE_PREFIX + levelIndex
        return prefs.getInt(key, fallback)
    }

    /**
     * Update and save the personal best score for this level.
     */
    fun saveBestScore(levelIndex: Int, moves: Int) {
        val currentBest = getBestScore(levelIndex, 999)
        if (moves < currentBest) {
            forceSaveBestScore(levelIndex, moves)
        }
    }

    /**
     * Set the best score regardless of current value.
     */
    fun forceSaveBestScore(levelIndex: Int, moves: Int) {
        val key = KEY_BEST_SCORE_PREFIX + levelIndex
        prefs.edit().putInt(key, moves).apply()
    }

    /**
     * Get the last played level index (0-indexed).
     */
    fun getLastPlayedLevel(): Int {
        return prefs.getInt(KEY_LAST_PLAYED, 0)
    }
    
    /**
     * Save the last played level index.
     * @param levelIndex The level index (0-indexed)
     */
    fun saveLastPlayedLevel(levelIndex: Int) {
        prefs.edit().putInt(KEY_LAST_PLAYED, levelIndex).apply()
    }
    
    /**
     * Get the highest unlocked level number (1-indexed).
     */
    fun getHighestUnlockedLevel(): Int {
        return prefs.getInt(KEY_HIGHEST_UNLOCKED, DEFAULT_UNLOCKED_LEVELS)
    }
    
    /**
     * Check if a specific level is unlocked.
     * @param levelNumber The level number (1-indexed)
     */
    fun isLevelUnlocked(levelNumber: Int): Boolean {
        return levelNumber <= getHighestUnlockedLevel()
    }
    
    /**
     * Called when a level is completed. Unlocks the next level if needed.
     * @param completedLevel The level number that was completed (1-indexed)
     */
    fun onLevelCompleted(completedLevel: Int) {
        val currentHighest = getHighestUnlockedLevel()
        val nextLevel = completedLevel + 1
        
        if (nextLevel > currentHighest) {
            prefs.edit().putInt(KEY_HIGHEST_UNLOCKED, nextLevel).apply()
        }
    }
    
    /**
     * Resets the best score for a level to 50.
     * This is used when a level is modified to prevent unreachable records.
     */
    fun resetBestScore(levelIndex: Int) {
        val key = KEY_BEST_SCORE_PREFIX + levelIndex
        prefs.edit().putInt(key, 50).apply()
    }

    /**
     * Reset all progress (for testing/debug).
     */
    fun resetProgress() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Reset only scores.
     */
    fun clearAllBestScores() {
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith(KEY_BEST_SCORE_PREFIX) }.forEach {
            editor.remove(it)
        }
        editor.apply()
    }
}
