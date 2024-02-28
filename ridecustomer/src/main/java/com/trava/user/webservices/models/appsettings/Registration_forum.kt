package com.trava.user.webservices.models.appsettings

data class Registration_forum(
        val gender: Int,
        val app_template: Int,
        val reg_template: String,
        val form_details: ArrayList<FormDetails>


)

data class FormDetails(var id: Int, var key_name: String, var value: String, var description: String, var terminology: String,
                       var required: String, var optional: String)