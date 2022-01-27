package dev.chungjungsoo.guaranteewallet.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class LoginDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "LoginInfoDB"
        private const val DB_VERSION = 1
        private const val ID = "id"
        private const val TABLE_NAME = "logininfo"
        private const val LOGIN_JWT = "jwt"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME" +
            "($ID INTEGER PRIMARY KEY, $LOGIN_JWT TEXT)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }

    fun addToken(jwt: String) : Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        if (jwt == "") { return false }

        values.put(LOGIN_JWT, jwt)

        val success = db.insert(TABLE_NAME, null, values)
        db.close()
        return (Integer.parseInt("$success") != -1)
    }

    fun getToken() : String {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        val jwt : String
        var result = ""

        if (cursor == null) {
            Log.d("DATABASE", "Cursor is Null")
            db.close()
            return result
        }

        if (cursor.moveToFirst()) {
            val i = cursor.getColumnIndex(LOGIN_JWT)
            if (i != -1) {
                jwt = cursor.getString(i)
                result = jwt
            }
            else { result = "" }
        }
        else {
            Log.e("DATABASE", "Getting JWT has failed")
            result = ""
        }

        cursor.close()
        db.close()

        return result
    }

    fun removeToken() : Boolean {
        val db = this.writableDatabase
        val query = db.rawQuery("DELETE FROM $TABLE_NAME", null)
        val result = query.count

        query.close()
        db.close()
        return result == 0
    }

    fun numOfToken(): Int {
        val db = this.readableDatabase
        val query = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        val result = query.count

        query.close()
        return result
    }
}