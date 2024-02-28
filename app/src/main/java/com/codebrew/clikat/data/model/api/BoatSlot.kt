package com.codebrew.clikat.data.model.api

data class BoatSlot(
    val data: MutableList<SlotData>,
    val message: String,
    val status: Int
)

data class SlotData(
    val date_id: Int,
    val end_time: String,
    val id: Int,
    val price: Float,
    var isSelected:Boolean=false,
    var format_start: String,
    var format_end: String,
    val product_id: Int,
    val start_time: String
)