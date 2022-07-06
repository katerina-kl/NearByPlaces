package com.example.breweries

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_ADDRESS_2
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_ADDRESS_3
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_BREWERY_TYPE
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_CITY
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_COUNTRY
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_COUNTY_PROVINCE
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_CREATED_AT
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_ID
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_LATITUDE
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_LONGTITUDE
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_NAME
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_PHONE
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_POSTAL_CODE
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_STATE
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_STREET
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_UPDATED_AT
import com.example.breweries.DBContract.BreweryEntry.Companion.COL_WEBSITE_URL
import com.example.breweries.DBContract.BreweryEntry.Companion.TABLE_NAME
import com.example.breweries.data.BreweryObject

class BreweryDBHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        DBContract.BreweryEntry.DATABASE_NAME,
        null,
        DBContract.BreweryEntry.DATABASE_VERSION
    ) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertBrewery(breweryObject: BreweryObject): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(COL_NAME, breweryObject.name)
        values.put(COL_BREWERY_TYPE, breweryObject.brewery_type)
        values.put(COL_STREET, breweryObject.street)
        values.put(COL_ADDRESS_2, breweryObject.address_2)
        values.put(COL_ADDRESS_3, breweryObject.address_3)
        values.put(COL_CITY, breweryObject.city)
        values.put(COL_STATE, breweryObject.state)
        values.put(COL_COUNTY_PROVINCE, breweryObject.county_province)
        values.put(COL_POSTAL_CODE, breweryObject.postal_code)
        values.put(COL_COUNTRY, breweryObject.country)
        values.put(COL_LONGTITUDE, breweryObject.longitude)
        values.put(COL_LATITUDE, breweryObject.latitude)
        values.put(COL_PHONE, breweryObject.phone)
        values.put(COL_WEBSITE_URL, breweryObject.website_url)
        values.put(COL_UPDATED_AT, breweryObject.updated_at)
        values.put(COL_CREATED_AT, breweryObject.created_at)

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.replace(TABLE_NAME, null, values)

        return true
    }

    @SuppressLint("Range")
    fun readBreweriesByCity(cityName: String): ArrayList<BreweryObject> {
        val breweries = ArrayList<BreweryObject>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("Select * from breweries where city like '$cityName%'", null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        if (cursor!!.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val breweryObject = BreweryObject(
                    cursor.getString(cursor.getColumnIndex(COL_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_NAME)),
                    cursor.getString(cursor.getColumnIndex(COL_BREWERY_TYPE)),
                    cursor.getString(cursor.getColumnIndex(COL_STREET)),
                    cursor.getString(cursor.getColumnIndex(COL_ADDRESS_2)),
                    cursor.getString(cursor.getColumnIndex(COL_ADDRESS_3)),
                    cursor.getString(cursor.getColumnIndex(COL_CITY)),
                    cursor.getString(cursor.getColumnIndex(COL_STATE)),
                    cursor.getString(cursor.getColumnIndex(COL_COUNTY_PROVINCE)),
                    cursor.getString(cursor.getColumnIndex(COL_POSTAL_CODE)),
                    cursor.getString(cursor.getColumnIndex(COL_COUNTRY)),
                    cursor.getString(cursor.getColumnIndex(COL_LONGTITUDE)),
                    cursor.getString(cursor.getColumnIndex(COL_LATITUDE)),
                    cursor.getString(cursor.getColumnIndex(COL_PHONE)),
                    cursor.getString(cursor.getColumnIndex(COL_WEBSITE_URL)),
                    cursor.getString(cursor.getColumnIndex(COL_UPDATED_AT)),
                    cursor.getString(cursor.getColumnIndex(COL_CREATED_AT))
                )

                breweries.add(breweryObject)
                cursor.moveToNext()
            }
        }
        return breweries
    }

    @SuppressLint("Range")
    fun readAllBreweries(): ArrayList<BreweryObject> {
        val breweries = ArrayList<BreweryObject>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME, null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        if (cursor!!.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val breweryObject = BreweryObject(
                    cursor.getString(cursor.getColumnIndex(COL_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_NAME)),
                    cursor.getString(cursor.getColumnIndex(COL_BREWERY_TYPE)),
                    cursor.getString(cursor.getColumnIndex(COL_STREET)),
                    cursor.getString(cursor.getColumnIndex(COL_ADDRESS_2)),
                    cursor.getString(cursor.getColumnIndex(COL_ADDRESS_3)),
                    cursor.getString(cursor.getColumnIndex(COL_CITY)),
                    cursor.getString(cursor.getColumnIndex(COL_STATE)),
                    cursor.getString(cursor.getColumnIndex(COL_COUNTY_PROVINCE)),
                    cursor.getString(cursor.getColumnIndex(COL_POSTAL_CODE)),
                    cursor.getString(cursor.getColumnIndex(COL_COUNTRY)),
                    cursor.getString(cursor.getColumnIndex(COL_LONGTITUDE)),
                    cursor.getString(cursor.getColumnIndex(COL_LATITUDE)),
                    cursor.getString(cursor.getColumnIndex(COL_PHONE)),
                    cursor.getString(cursor.getColumnIndex(COL_WEBSITE_URL)),
                    cursor.getString(cursor.getColumnIndex(COL_UPDATED_AT)),
                    cursor.getString(cursor.getColumnIndex(COL_CREATED_AT))
                )

                breweries.add(breweryObject)
                cursor.moveToNext()
            }
        }
        return breweries
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    companion object {
        // If you change the database schema, you must increment the database version.

        private val SQL_CREATE_ENTRIES =
            "CREATE TABLE $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COL_NAME VARCHAR(256),$COL_BREWERY_TYPE VARCHAR(256),$COL_STREET VARCHAR(256),$COL_ADDRESS_2 VARCHAR(256),$COL_ADDRESS_3 VARCHAR(256),$COL_CITY VARCHAR(256),$COL_STATE VARCHAR(256),$COL_COUNTY_PROVINCE VARCHAR(256),$COL_POSTAL_CODE VARCHAR(256),$COL_COUNTRY VARCHAR(256),$COL_LONGTITUDE VARCHAR(256),$COL_LATITUDE VARCHAR(256),$COL_PHONE VARCHAR(256),$COL_WEBSITE_URL VARCHAR(256),$COL_UPDATED_AT VARCHAR(256),$COL_CREATED_AT VARCHAR(256))"


        private val SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME
    }

}