package com.trava.user.webservices.models.appsettings

import com.trava.utilities.webservices.models.Service

data class SettingItems(
        val key_value: Key_value,
        val walk_through: ArrayList<Walk_through>,
        val registration_forum: Registration_forum,
        var services: List<Service>?,
        var languages: List<LanguageSets>?,
        var level_values: List<Levels>?,
        var dynamicbar: DynamicBar,
        var user_forum: List<UserForum>?,
        val supports: List<SupportsItem>?)

data class Levels(var level_id: Int, var level_value: String)

data class LanguageSets(var language_id:Int,var language_name:String,val language_code:String)
