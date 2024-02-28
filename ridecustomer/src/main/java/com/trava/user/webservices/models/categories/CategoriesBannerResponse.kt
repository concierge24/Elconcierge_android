package com.trava.user.webservices.models.categories

import com.trava.utilities.webservices.models.Service

data class CategoriesBannerResponse(
        var services: ArrayList<Service>,
        var banners : ArrayList<BannerData>
)