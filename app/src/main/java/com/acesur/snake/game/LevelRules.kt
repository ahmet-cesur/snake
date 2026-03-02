package com.acesur.snake.game

/**
 * LevelRules.kt
 * 
 * Defines the core rules and constraints for Level Generation and Gameplay.
 * These rules ensure consistency across all levels and valid gameplay mechanics.
 */
object LevelRules {

    // =========================================================================
    // Rule 1: Worm Configuration
    // =========================================================================
    
    /**
     * The worm must always start with this many segments.
     * User Constraint: "start with 3 boxes" (interpreted as 3 worm segments/blocks).
     */
    const val STARTING_WORM_LENGTH = 3

    // =========================================================================
    // Rule 2: Movement Constraints
    // =========================================================================
    
    /**
     * Rule: Worm cannot return backwards.
     * The head cannot move directly into the position of the second segment (the neck).
     * This prevents 180-degree instant turns which are physically impossible for the worm.
     * 
     * Logic:
     * IF current_direction is UP, cannot move DOWN.
     * IF current_direction is LEFT, cannot move RIGHT.
     * (And vice versa).
     */
    fun canMove(currentHead: Position, secondSegment: Position?, target: Position): Boolean {
        // If there is no second segment (length 1), it can move anywhere (except walls)
        if (secondSegment == null) return true
        
        // The target position cannot be the same as the second segment
        return target != secondSegment
    }

    // =========================================================================
    // Rule 3: Level Elements
    // =========================================================================
    
    /**
     * Rule: Levels must be solvable.
     * - Must have at least one Portal.
     * - Must have enough Apples/Boxes to traverse gaps if gravity is involved.
     */
    const val MIN_PORTALS = 1
}
