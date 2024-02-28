package com.codebrew.clikat.data.model.api

data class ZoneResponse(
    val data: List<ZoneData>,
    val message: String,
    val status: Int
)

data class ZoneData(
    val coordinates: List<List<Coordinate>>,
    val id: Int,
    val is_live: Int,
    val is_under: Int,
    val name: String
)

data class Coordinate(
    val x: Double,
    val y: Double
)