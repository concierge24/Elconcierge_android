package com.trava.utilities.webservices.models

data class LoginModel(
        var AppDetail: AppDetail?,
        var services: List<Service>?,
        var supports: List<Support>?
)