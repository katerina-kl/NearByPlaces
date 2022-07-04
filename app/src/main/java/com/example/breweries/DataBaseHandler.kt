package com.example.breweries
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.breweries.data.BreweryObject

val DATABASENAME = "MY DATABASE"
val TABLENAME = "Breweries"
val COL_ID = "id"
val COL_NAME = "name"
val COL_BREWERY_TYPE = "brewery_type"
val COL_STREET = "street"
val COL_ADDRESS_2 = "address_2"
val COL_ADDRESS_3 = "address_3"
val COL_CITY = "city"
val COL_STATE = "state"
val COL_COUNTY_PROVINCE = "county_province"
val COL_POSTAL_CODE = "postal_code"
val COL_COUNTRY = "country"
val COL_LONGTITUDE = "longitude"
val COL_LATITUDE = "latitude"
val COL_PHONE = "phone"
val COL_WEBSITE_URL= "website_url"
val COL_UPDATED_AT = "updated_at"
val COL_CREATED_AT = "created_at"

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASENAME, null,
    1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE " + TABLENAME + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_NAME + " VARCHAR(256)," + COL_BREWERY_TYPE + " VARCHAR(256)," + COL_STREET + " VARCHAR(256),"  + COL_ADDRESS_2 + " VARCHAR(256),"+ COL_ADDRESS_3 + " VARCHAR(256),"  + COL_CITY + " VARCHAR(256),"  + COL_STATE + " VARCHAR(256),"  + COL_COUNTY_PROVINCE + " VARCHAR(256),"  + COL_POSTAL_CODE + " VARCHAR(256),"+ COL_COUNTRY + " VARCHAR(256),"  + COL_LONGTITUDE + " VARCHAR(256),"  + COL_LATITUDE + " VARCHAR(256),"  + COL_PHONE + " VARCHAR(256),"  + COL_WEBSITE_URL + " VARCHAR(256),"  + COL_UPDATED_AT + " VARCHAR(256),"+ COL_CREATED_AT + " VARCHAR(256)"+ ")"
        db?.execSQL(createTable)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //onCreate(db);
    }
    fun insertData(breweryObject: BreweryObject) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_NAME, breweryObject.name)
        contentValues.put(COL_BREWERY_TYPE, breweryObject.brewery_type)
        contentValues.put(COL_STREET, breweryObject.street)
        contentValues.put(COL_ADDRESS_2, breweryObject.address_2)
        contentValues.put(COL_ADDRESS_3, breweryObject.address_3)
        contentValues.put(COL_CITY, breweryObject.city)
        contentValues.put(COL_STATE, breweryObject.state)
        contentValues.put(COL_COUNTY_PROVINCE, breweryObject.county_province)
        contentValues.put(COL_POSTAL_CODE, breweryObject.postal_code)
        contentValues.put(COL_COUNTRY, breweryObject.country)
        contentValues.put(COL_LONGTITUDE, breweryObject.longitude)
        contentValues.put(COL_LATITUDE, breweryObject.latitude)
        contentValues.put(COL_PHONE, breweryObject.phone)
        contentValues.put(COL_WEBSITE_URL, breweryObject.website_url)
        contentValues.put(COL_UPDATED_AT, breweryObject.updated_at)
        contentValues.put(COL_CREATED_AT, breweryObject.created_at)

        val result = database.insert(TABLENAME, null, contentValues)
        if (result == (0).toLong()) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("Range")
    fun readData(): MutableList<BreweryObject> {
        val list: MutableList<BreweryObject> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $TABLENAME"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val breweryObject = BreweryObject(result.getString(result.getColumnIndex(COL_ID)),result.getString(result.getColumnIndex(COL_NAME)),result.getString(result.getColumnIndex(COL_BREWERY_TYPE))
                    ,result.getString(result.getColumnIndex(COL_STREET)),result.getString(result.getColumnIndex(COL_ADDRESS_2)),result.getString(result.getColumnIndex(COL_ADDRESS_3)),result.getString(result.getColumnIndex(COL_CITY))
                    ,result.getString(result.getColumnIndex(COL_STATE)),result.getString(result.getColumnIndex(COL_COUNTY_PROVINCE)),result.getString(result.getColumnIndex(COL_POSTAL_CODE))
                    ,result.getString(result.getColumnIndex(COL_COUNTRY)),result.getString(result.getColumnIndex(COL_LONGTITUDE)),result.getString(result.getColumnIndex(COL_LATITUDE))
                    ,result.getString(result.getColumnIndex(COL_PHONE)),result.getString(result.getColumnIndex(COL_WEBSITE_URL)),result.getString(result.getColumnIndex(COL_UPDATED_AT)),result.getString(result.getColumnIndex(COL_CREATED_AT))
                )


                list.add(breweryObject)
            }
            while (result.moveToNext())
        }
        return list
    }
}