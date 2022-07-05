package com.example.breweries

import android.provider.BaseColumns

object DBContract {

    class BreweryEntry : BaseColumns {
        companion object {
            const val DATABASE_NAME = "breweries.db"
            const val DATABASE_VERSION = 1
            const val TABLE_NAME = "breweries"
            const val COL_ID = "id"
            const val COL_NAME = "name"
            const val COL_BREWERY_TYPE = "brewery_type"
            const val COL_STREET = "street"
            const val COL_ADDRESS_2 = "address_2"
            const val COL_ADDRESS_3 = "address_3"
            const val COL_CITY = "city"
            const val COL_STATE = "state"
            const val COL_COUNTY_PROVINCE = "county_province"
            const val COL_POSTAL_CODE = "postal_code"
            const val COL_COUNTRY = "country"
            const val COL_LONGTITUDE = "longitude"
            const val COL_LATITUDE = "latitude"
            const val COL_PHONE = "phone"
            const val COL_WEBSITE_URL= "website_url"
            const val COL_UPDATED_AT = "updated_at"
            const val COL_CREATED_AT = "created_at"
        }
    }
}