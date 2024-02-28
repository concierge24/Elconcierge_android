package com.trava.utilities

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.util.*


object MapUtils {

    fun getStaticMapWithMarker(context: Context,key:String, latitude: Double, longitude: Double): String
    {
        return  "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=16&scale=1&size=600x300&maptype=roadmap&key=$key&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0x3c3a46%7Clabel:B%7C$latitude,$longitude"
    }

    fun getStaticMapWithPolyLine(context: Context,key:String,pickUpLat: Double,pickUpLng: Double,dropLat: Double,dropLng: Double) : String{
        return "http://maps.googleapis.com/maps/api/staticmap?center=$pickUpLat,$pickUpLng&size=600x300&markers=size:mid%7Ccolor:0xFF8A2B%7Clabel:S%7C$pickUpLat,$pickUpLng&markers=size:mid%7Ccolor:0x3c3a46%7Clabel:D%7C$dropLat,$dropLng&key=$key&path=color:0x1E2E4D|weight:2|$pickUpLat,$pickUpLng|$dropLat,$dropLng"
    }

    fun getStaticMapWithPolyLinePath(path: String, key: String, pickUpLat: Double, pickUpLng: Double, dropLat: Double, dropLng: Double): String {
        return "http://maps.googleapis.com/maps/api/staticmap?center=$pickUpLat,$pickUpLng&size=600x300&markers=size:mid%7Ccolor:0xFF8A2B%7Clabel:S%7C$pickUpLat,$pickUpLng&markers=size:mid%7Ccolor:0x3c3a46%7Clabel:D%7C$dropLat,$dropLng&key=$key&path=color:0x1E2E4D|weight:2|enc%3A$path"
    }

    fun setStaticPolyLineOnMapPath(path: String,key:String,pickUpLat: Double,pickUpLng: Double,dropLat: Double,dropLng: Double) : String
    {
        return "https://maps.googleapis.com/maps/api/staticmap?center=$pickUpLat,$pickUpLng&size=600x300&markers=size:mid%7Ccolor:0x26B100%7C$pickUpLat,$pickUpLng&markers=size:mid%7Ccolor:0xD82A2A%7C$dropLat,$dropLng&key=$key&path=color:0x1E2E4D|weight:2|enc%3A$path"
    }

    /* decode the encoded path we got from the direction api to draw polyline*/
    fun decodePoly(encoded: String): ArrayList<LatLng> {
        val poly = java.util.ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }


    /* fun getStaticMapWithMarker(context: Context, latitude: Double, longitude: Double): String {
         //val key = context.getString(R.string.google_maps_key)
         val key = "AIzaSyDNjbIaiPB41uhvhD0mb9Xdi2tA7n0AFlo"
         val languageCode = Locale.getDefault().language
 //        return "https://maps.googleapis.com/maps/api/staticmap?zoom=16&size=600x250&scale=1&markers=icon:https://www.iconsdb.com/icons/preview/violet/map-marker-2-xxl.png|$latitude,$longitude&key=$key&language=$languageCode"
         return  "https://maps.googleapis.com/maps/api/staticmap?key=$key&zoom=12&format=png&maptype=roadmap&style=element:geometry%7Ccolor:0xf5f5f5&style=element:labels.icon%7Cvisibility:off&style=element:labels.text.fill%7Ccolor:0x616161&style=element:labels.text.stroke%7Ccolor:0xf5f5f5&style=feature:administrative.land_parcel%7Celement:labels.text.fill%7Ccolor:0xbdbdbd&style=feature:poi%7Celement:geometry%7Ccolor:0xeeeeee&style=feature:poi%7Celement:labels.text.fill%7Ccolor:0x757575&style=feature:poi.park%7Celement:geometry%7Ccolor:0xe5e5e5&style=feature:poi.park%7Celement:labels.text.fill%7Ccolor:0x9e9e9e&style=feature:road%7Celement:geometry%7Ccolor:0xffffff&style=feature:road.arterial%7Celement:labels.text.fill%7Ccolor:0x757575&style=feature:road.highway%7Celement:geometry%7Ccolor:0xdadada&style=feature:road.highway%7Celement:labels.text.fill%7Ccolor:0x616161&style=feature:road.local%7Celement:labels.text.fill%7Ccolor:0x9e9e9e&style=feature:transit.line%7Celement:geometry%7Ccolor:0xe5e5e5&style=feature:transit.station%7Celement:geometry%7Ccolor:0xeeeeee&style=feature:water%7Celement:geometry%7Ccolor:0xc9c9c9&style=feature:water%7Celement:labels.text.fill%7Ccolor:0x9e9e9e&size=600x250&scale=1&markers=icon:http://45.232.252.55:9007/images/marker.png|$latitude,$longitude"
     }

     fun getStaticMapWithPolyLine(context: Context,pickUpLat: Double,pickUpLng: Double,dropLat: Double,dropLng: Double) : String{
         val key = "AIzaSyDNjbIaiPB41uhvhD0mb9Xdi2tA7n0AFlo"
         val languageCode = Locale.getDefault().language
         return "http://maps.googleapis.com/maps/api/staticmap?size=600x250&markers=icon:http://45.232.252.55:9007/images/pickupNew.png|$pickUpLat,$pickUpLng&markers=icon:http://45.232.252.55:9007/images/drop.png|$dropLat,$dropLng&path=$pickUpLat,$pickUpLng|$dropLat,$dropLng&key=$key&zoom=12&language=$languageCode&sensor=false&format=png&maptype=roadmap&style=element:geometry%7Ccolor:0xf5f5f5&style=element:labels.icon%7Cvisibility:off&style=element:labels.text.fill%7Ccolor:0x616161&style=element:labels.text.stroke%7Ccolor:0xf5f5f5&style=feature:administrative.land_parcel%7Celement:labels.text.fill%7Ccolor:0xbdbdbd&style=feature:poi%7Celement:geometry%7Ccolor:0xeeeeee&style=feature:poi%7Celement:labels.text.fill%7Ccolor:0x757575&style=feature:poi.park%7Celement:geometry%7Ccolor:0xe5e5e5&style=feature:poi.park%7Celement:labels.text.fill%7Ccolor:0x9e9e9e&style=feature:road%7Celement:geometry%7Ccolor:0xffffff&style=feature:road.arterial%7Celement:labels.text.fill%7Ccolor:0x757575&style=feature:road.highway%7Celement:geometry%7Ccolor:0xdadada&style=feature:road.highway%7Celement:labels.text.fill%7Ccolor:0x616161&style=feature:road.local%7Celement:labels.text.fill%7Ccolor:0x9e9e9e&style=feature:transit.line%7Celement:geometry%7Ccolor:0xe5e5e5&style=feature:transit.station%7Celement:geometry%7Ccolor:0xeeeeee&style=feature:water%7Celement:geometry%7Ccolor:0xc9c9c9&style=feature:water%7Celement:labels.text.fill%7Ccolor:0x9e9e9e"
     }*/

    fun setStaticPolyLineOnMap(pickUpLat:Double,pickUpLng:Double,dropLat:Double,dropLng:Double):String{
        val key = "AIzaSyDNjbIaiPB41uhvhD0mb9Xdi2tA7n0AFlo"
        val languageCode = Locale.getDefault().language
        return "http://maps.googleapis.com/maps/api/staticmap?size=600x250&markers=icon:http://45.232.252.55:9007/images/pickupNew.png|$pickUpLat,$pickUpLng&markers=icon:http://45.232.252.55:9007/images/drop.png|$dropLat,$dropLng&path=$pickUpLat,$pickUpLng|$dropLat,$dropLng&key=$key&language=$languageCode&sensor=false"
    }


    fun bearingBetweenLocations(sourceLatLng: LatLng, destinationLatLng: LatLng): Float {
        val source = Location("source")
        source.latitude = sourceLatLng.latitude
        source.longitude = sourceLatLng.longitude

        val destination = Location("destination")
        destination.latitude = destinationLatLng.latitude
        destination.longitude = destinationLatLng.longitude
        return source.bearingTo(destination)
    }

    fun calculateBearing(sourceLatLng: LatLng?, destinationLatLng: LatLng?): Float {

        val PI = 3.14159
        val lat1 = sourceLatLng?.latitude ?: 0.0 * PI / 180
        val long1 = sourceLatLng?.longitude ?: 0.0 * PI / 180
        val lat2 = destinationLatLng?.latitude ?: 0.0 * PI / 180
        val long2 = destinationLatLng?.longitude ?: 0.0 * PI / 180

        val dLon = long2 - long1

        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon))

        var brng = Math.atan2(y, x)

        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360

        return brng.toFloat()
    }

    fun getBearing(start: LatLng, end: LatLng): Float? {
        val absoluteLatitude = Math.abs(start.latitude - end.latitude)
        val absoluteLongitude = Math.abs(start.longitude - end.longitude)

        if (start.latitude < end.latitude && start.longitude < end.longitude)
            return Math.toDegrees(Math.atan(absoluteLongitude / absoluteLatitude)).toFloat()
        else if (start.latitude >= end.latitude && start.longitude < end.longitude)
            return (90 - Math.toDegrees(Math.atan(absoluteLongitude / absoluteLatitude)) + 90).toFloat()
        else if (start.latitude >= end.latitude && start.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(absoluteLongitude / absoluteLatitude)) + 180).toFloat()
        else if (start.latitude < end.latitude && start.longitude >= end.longitude)
            return (90 - Math.toDegrees(Math.atan(absoluteLongitude / absoluteLatitude)) + 270).toFloat()
        return -1f
    }

    fun interpolateLatLng(fraction: Float, start: LatLng, end: LatLng): LatLng {
        val updatedLatitude = (end.latitude - start.latitude) * fraction + start.latitude
        var deltaLongitude = end.longitude - start.longitude

        // Take the shortest path across the 180th meridian.
        if (Math.abs(deltaLongitude) > 180) {
            deltaLongitude -= Math.signum(deltaLongitude) * 360
        }

        val updatedLongitude = deltaLongitude * fraction + start.longitude
        return LatLng(updatedLatitude, updatedLongitude)
    }

    fun getDistanceBetweenTwoPoints(latLng1: LatLng, latLng2: LatLng): Float {
        val distance = FloatArray(2)
        Location.distanceBetween(latLng1.latitude, latLng1.longitude,
                latLng2.latitude, latLng2.longitude, distance)
        Log.e("Order Distance",""+"Distance Calculated : ${distance[0]}")

        return distance[0]
    }

    fun getDistanceBetweenTwoPointsCancel(latLng1: LatLng, latLng2: LatLng): Double {
        val distance = FloatArray(2)

        Location.distanceBetween(latLng1.latitude, latLng1.longitude,
                latLng2.latitude, latLng2.longitude, distance)
        //Timber.d("Distance Calculated : ${distance[0]}")

        return distance[0].toDouble().div(1000)
    }

    fun getDistanceTimeBetweenTwoPoints(latLng1: LatLng, latLng2: LatLng): Long {
        val distance = FloatArray(2)
        Location.distanceBetween(latLng1.latitude, latLng1.longitude,
                latLng2.latitude, latLng2.longitude, distance)
        Log.e("Order Distance",""+"Distance Calculated : ${distance[0]}")
        //For example speed is 60 meters per minute.
        val speed = 60
        val time = distance[0].div(speed)
        return time.toLong()
    }

    fun distance(lat1:Double, lon1:Double, lat2:Double, lon2:Double):Double {
        val theta = lon1 - lon2
        var dist = ((Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))) + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta))))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60.0 * 1.1515
        return (dist)
    }
    private fun deg2rad(deg:Double):Double {
        return (deg * Math.PI / 180.0)
    }
    private fun rad2deg(rad:Double):Double {
        return (rad * 180.0 / Math.PI)
    }
}