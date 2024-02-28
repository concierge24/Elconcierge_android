package com.trava.user.webservices.models.earnings

import java.io.Serializable

data class StartEndDateModel (
        var startDate : String ? = null,
        var endDate : String ? = null,
        var isSelected : Boolean ? = false
) : Serializable