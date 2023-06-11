package com.example.jamcam

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


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


    fun addMatch(match: Match): Long {
        val values = ContentValues()
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

    fun findEventId(matchId: Int, eventType: String, player: String, video: String): Int {
        val query =
            "SELECT * FROM $TABLE_EVENTS WHERE $COLUMN_MATCHID LIKE \"$matchId\" AND $COLUMN_EVENTTYPE LIKE \"$eventType\" AND $COLUMN_PLAYER LIKE \"$player\" AND $COLUMN_VIDEO LIKE \"$video\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0)
            cursor.close()
        }
        db.close()
        return id
    }

    fun getMatch(matchId: Int): Match {
        val query =
            "SELECT _id, description, place, date FROM matches where _id = $matchId"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var match = Match(
            "",
            "",
            ""
        )
        if (cursor.moveToFirst()) {
            match = Match(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3)
            )
        }

        cursor.close()
        db.close()
        return match

    }

    fun getAllMatches(): MutableList<Match> {
        val query = "SELECT * FROM $TABLE_MATCHES"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val list: MutableList<Match> = mutableListOf()
        var match: Match? = null
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val description = cursor.getString(1)
            val place = cursor.getString(2)
            val date = cursor.getString(3)
            match = Match(description, place, date)
            list.add(match)
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return list
    }

    fun getEvent(eventId: Int): Event {
        val query =
            "SELECT _id, matchid, eventtype, player, video FROM events where _id = $eventId"

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var event = Event(
            -1,
            "",
            "",
            ""
        )
        if (cursor.moveToFirst()) {
            event = Event(
                cursor.getInt(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4)
            )
        }

        cursor.close()
        db.close()
        return event
    }

    fun getEvent(videoName: String): Event {
        val query =
            "SELECT _id, matchid, eventtype, player, video FROM events where video LIKE \"$videoName\""

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var event = Event(
            -1,
            "",
            "",
            ""
        )
        if (cursor.moveToFirst()) {
            event = Event(
                cursor.getInt(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4)
            )
        }

        cursor.close()
        db.close()
        return event
    }

    fun getEvents(video: String): MutableList<Event> {
        val query =
            "SELECT _id, matchid, eventtype, player, video FROM events where video != \"$video\" ORDER BY _id DESC"

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val list: MutableList<Event> = mutableListOf()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(
                Event(
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                )
            )
            cursor.moveToNext()
        }

        cursor.close()
        db.close()
        return list
    }

    fun resetEventVideo(eventVideo: String) {
        val query =
            "UPDATE events SET video = 'no_video' WHERE video LIKE \"$eventVideo\""
        val db = this.writableDatabase
        try {
            db.execSQL(query)
        } catch (e: SQLException) {
            // Handle any potential SQL exception
            Log.e("Database", "Error executing query: $query")
        } finally {
            db.close()
        }
    }

    fun deleteEvent(id: Int) {
        val db = this.writableDatabase
        val whereClause = "$COLUMN_ID_AUTO=?"
        val whereArgs = arrayOf(id.toString())
        db.delete(TABLE_EVENTS, whereClause, whereArgs)
        db.close()
    }

    fun getAllEvents(): MutableList<Event> {
        val query = "SELECT * FROM $TABLE_EVENTS"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val list: MutableList<Event> = mutableListOf()
        var event: Event? = null
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val matchId = cursor.getInt(1)
            val eventType = cursor.getString(2)
            val player = cursor.getString(3)
            val video = cursor.getString(4)
            event = Event(matchId, eventType, player, video)
            list.add(event)
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return list
    }
}
