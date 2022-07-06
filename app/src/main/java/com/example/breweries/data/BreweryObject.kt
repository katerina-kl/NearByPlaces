package com.example.breweries.data

import java.io.Serializable

class BreweryObject :Serializable {
        
       var id =""
       var name=""
       var brewery_type=""
       var street=""
       var address_2=""
       var address_3=""
       var city=""
       var state=""
       var county_province=""
       var postal_code=""
       var country=""
       var longitude=""
       var latitude=""
       var phone=""
       var website_url=""
       var updated_at=""
       var created_at =""

        constructor(id: String, name: String, brewery_type: String , street: String , address_2: String , address_3: String , city: String
                    , state: String , county_province: String , postal_code: String , country: String , longitude: String , latitude: String
                    , phone: String , website_url: String  , updated_at: String , created_at: String){
                this.id = id
                this.name = name
                this.brewery_type = brewery_type
                this.street = street
                this.address_2 = address_2
                this.address_3 = address_3
                this.city = city
                this.state = state
                this.county_province = county_province
                this.postal_code = postal_code
                this.country = country
                this.longitude = longitude
                this.latitude = latitude
                this.phone = phone
                this.website_url = website_url
                this.updated_at = updated_at
                this.created_at = created_at

        }

        constructor()
}
