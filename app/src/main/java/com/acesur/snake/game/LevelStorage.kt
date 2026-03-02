package com.acesur.snake.game

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object LevelStorage {
    private const val PREFS_NAME = "SnakePrefs"
    private const val KEY_CUSTOM_LEVELS = "custom_levels"

    fun saveCustomLevels(context: Context, levels: List<Level>) {
        // Save all levels to support editing built-ins and custom levels alike.
        val levelsToSave = levels
        
        val jsonArray = JSONArray()
        levelsToSave.forEach { level ->
            val json = JSONObject()
            json.put("id", level.id)
            json.put("width", level.width)
            json.put("height", level.height)
            
            // Grid
            val gridArray = JSONArray()
            level.grid.forEach { row -> gridArray.put(row) }
            json.put("grid", gridArray)
            
            // Worm Start
            val wormArray = JSONArray()
            level.wormStart.forEach { pos ->
                val posObj = JSONObject()
                posObj.put("x", pos.x)
                posObj.put("y", pos.y)
                wormArray.put(posObj)
            }
            json.put("wormStart", wormArray)
            
            // Apples
            val applesArray = JSONArray()
            level.apples.forEach { pos ->
                val posObj = JSONObject()
                posObj.put("x", pos.x)
                posObj.put("y", pos.y)
                applesArray.put(posObj)
            }
            json.put("apples", applesArray)
            
            // Boxes
            val boxesArray = JSONArray()
            level.boxes.forEach { pos ->
                val posObj = JSONObject()
                posObj.put("x", pos.x)
                posObj.put("y", pos.y)
                boxesArray.put(posObj)
            }
            json.put("boxes", boxesArray)
            
            // Portal
            val portalObj = JSONObject()
            portalObj.put("x", level.portal.x)
            portalObj.put("y", level.portal.y)
            json.put("portal", portalObj)
            
            json.put("minMoves", level.minMoves)
            
            jsonArray.put(json)
        }
        
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_LEVELS, jsonArray.toString()).apply()
    }

    fun loadCustomLevels(context: Context): List<Level> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_CUSTOM_LEVELS, null) ?: return emptyList()
        
        val levels = mutableListOf<Level>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getInt("id")
                val width = obj.getInt("width")
                val height = obj.getInt("height")
                
                val gridList = mutableListOf<String>()
                val gridArray = obj.getJSONArray("grid")
                for (j in 0 until gridArray.length()) {
                    gridList.add(gridArray.getString(j))
                }
                
                val wormStart = mutableListOf<Position>()
                val wormArray = obj.getJSONArray("wormStart")
                for (j in 0 until wormArray.length()) {
                    val p = wormArray.getJSONObject(j)
                    wormStart.add(Position(p.getInt("x"), p.getInt("y")))
                }
                
                val apples = mutableListOf<Position>()
                val applesArray = obj.getJSONArray("apples")
                for (j in 0 until applesArray.length()) {
                    val p = applesArray.getJSONObject(j)
                    apples.add(Position(p.getInt("x"), p.getInt("y")))
                }
                
                val boxes = mutableListOf<Position>()
                val boxesArray = obj.getJSONArray("boxes")
                for (j in 0 until boxesArray.length()) {
                    val p = boxesArray.getJSONObject(j)
                    boxes.add(Position(p.getInt("x"), p.getInt("y")))
                }
                
                val portalObj = obj.getJSONObject("portal")
                val portal = Position(portalObj.getInt("x"), portalObj.getInt("y"))
                
                val minMoves = obj.optInt("minMoves", 10)
                
                levels.add(Level(id, width, height, gridList, wormStart, apples, boxes, portal, minMoves))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return levels
    }
}
