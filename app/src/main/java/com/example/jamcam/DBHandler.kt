package com.example.jamcam

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHandler(
    context: Context,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "jamcamDB.db"

        val TABLE_MATCHES = "matches"
        val COLUMN_ID = "id"
        val COLUMN_DESCRIPTION = "description"
        val COLUMN_PLACE = "place"
        val COLUMN_DATE = "date"

        val TABLE_EVENTS = "events"
        val COLUMN_ID_AUTO = "_id"
        val COLUMN_MATCHID = "matchid"
        val COLUMN_EVENTTYPE = "eventtype"
        val COLUMN_PLAYER = "player"
        val COLUMN_VIDEO = "video"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_MATCHES_TABLE = ("CREATE TABLE " +
                TABLE_MATCHES + "(" + COLUMN_ID_AUTO + " INTEGER PRIMARY KEY, "
                + COLUMN_DESCRIPTION + " TEXT, " + COLUMN_PLACE + " TEXT, " + COLUMN_DATE +
                " TEXT" + ")")
        db.execSQL(CREATE_MATCHES_TABLE)

        val CREATE_EVENTS_TABLE = ("CREATE TABLE " +
                TABLE_EVENTS + "(" + COLUMN_ID_AUTO + " INTEGER PRIMARY KEY, "
                + COLUMN_MATCHID + " INTEGER, " + COLUMN_EVENTTYPE + " TEXT, " + COLUMN_PLAYER +
                " TEXT, " + COLUMN_VIDEO + " TEXT" + ")")
        db.execSQL(CREATE_EVENTS_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MATCHES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        onCreate(db)
    }

//    fun getNewMatchId(): Int {
//        val query = "SELECT * FROM $TABLE_MATCHES"
//        val db = this.writableDatabase
//        val cursor = db.rawQuery(query, null)
//        cursor.moveToFirst()
//        var id = 1
//        while (!cursor.isAfterLast) {
//            id = cursor.getInt(0)
//        }
//        cursor.close()
//        db.close()
//        println(id)
//        return id + 1
//    }

    fun addMatch(match: Match): Long {
        val values = ContentValues()
//        values.put(COLUMN_ID, match.id)
        values.put(COLUMN_DESCRIPTION, match.description)
        values.put(COLUMN_PLACE, match.place)
        values.put(COLUMN_DATE, match.date)

        val db = this.writableDatabase
        val result = db.insert(TABLE_MATCHES, null, values)
        db.close()

        println(result)
        return result
    }


    fun addEvent(event: Event) {
        val values = ContentValues()
        values.put(COLUMN_MATCHID, event.matchId)
        values.put(COLUMN_EVENTTYPE, event.eventType)
        values.put(COLUMN_PLAYER, event.player)
        values.put(COLUMN_VIDEO, event.video)

        val db = this.writableDatabase
        val result = db.insert(TABLE_EVENTS, null, values)
        db.close()
    }

//    fun findRecipe(id: Int): Recipe? {
//        val query = "SELECT * FROM $TABLE_RECIPES WHERE $COLUMN_ID LIKE \"$id\""
//        val db = this.writableDatabase
//        val cursor = db.rawQuery(query, null)
//        var recipe: Recipe? = null
//        if (cursor.moveToFirst()) {
//            val id = cursor.getInt(0)
//            val name = cursor.getString(1)
//            val ingredients = cursor.getString(2)
//            val actions = cursor.getString(3)
//            val minutes = cursor.getInt(4)
//            val imageId = cursor.getInt(5)
//            val type = cursor.getString(6)
//            recipe = Recipe(id, name, ingredients, actions, minutes, imageId, type)
//            cursor.close()
//        }
//        db.close()
//        return recipe
//    }
//
//    fun getAllRecipes():MutableList<Recipe> {
//        val query = "SELECT * FROM $TABLE_RECIPES"
//        val db = this.writableDatabase
//        val cursor = db.rawQuery(query, null)
//        val list: MutableList<Recipe> = mutableListOf()
//        var recipe: Recipe? = null
//        cursor.moveToFirst()
//        while (!cursor.isAfterLast) {
//            val id = cursor.getInt(0)
//            val name = cursor.getString(1)
//            val ingredients = cursor.getString(2)
//            val actions = cursor.getString(3)
//            val minutes = cursor.getInt(4)
//            val imageId = cursor.getInt(5)
//            val type = cursor.getString(6)
//            recipe = Recipe(id, name, ingredients, actions, minutes, imageId, type)
//            list.add(recipe)
//            cursor.moveToNext()
//        }
//        cursor.close()
//        db.close()
//        return list
//    }
}
