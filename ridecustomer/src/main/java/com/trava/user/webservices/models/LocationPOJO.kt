package com.trava.user.webservices.models

data class LocationPOJO(var name:String,var keys:String,var temp:Boolean)


data class FindLocationPOJO(var address:String,var vicinity:String,var lat:String,var lng:String)