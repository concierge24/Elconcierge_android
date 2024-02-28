package com.trava.user.webservices.models.travelPackages

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PackagePricingsItem(

	@field:SerializedName("category_brand_product_id")
	val categoryBrandProductId: Int? = null,

	@field:SerializedName("price_per_min")
	val pricePerMin: Double? = null,

	@field:SerializedName("package_pricing_id")
	val packagePricingId: Int? = null,

	@field:SerializedName("category_id")
	val categoryId: Int? = null,

	@field:SerializedName("category_brand_id")
	val categoryBrandId: Int? = null,

	@field:SerializedName("time_fixed_price")
	val timeFixedPrice: Double? = null,

	@field:SerializedName("distance_price_fixed")
	val distancePriceFixed: Double? = null,

	@field:SerializedName("price_per_km")
	var pricePerKm: Double? = null,

	@field:SerializedName("package_id")
	val packageId: Int? = null,

	@field:SerializedName("category_brand_product")
	val category_brand_product: CategoryBrandProduct? = null

) : Parcelable