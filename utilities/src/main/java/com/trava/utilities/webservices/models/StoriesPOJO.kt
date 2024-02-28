package com.trava.utilities.webservices.models

data class StoriesPOJO(var id: Int,
                       var category_id: Int,
                       val video_url: String,
                       var image_url: String,
                       var thumbnail: String,
                       var duration: String)