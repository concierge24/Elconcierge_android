package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class Addresses(

	@field:SerializedName("work")
	val work: RecentItem? = null,

	@field:SerializedName("recent")
	val recent: ArrayList<RecentItem> = ArrayList(),

	@field:SerializedName("home")
	val home: RecentItem? = null
)