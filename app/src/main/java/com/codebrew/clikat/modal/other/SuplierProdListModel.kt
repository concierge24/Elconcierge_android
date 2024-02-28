package com.codebrew.clikat.modal.other

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class SuplierProdListModel(var status: Int = 0,
                                var message: Any? = null,
                                var data: DataBean? = null)

data class SupplierCategoryModel(var status: Int = 0,
                                 var message: String? = null,
                                 var data: ArrayList<ProductBean>? = null)

data class TableCapacityModel(var status: Int = 0,
                              var message: String? = null,
                              var data: ArrayList<String>? = null)
@Parcelize
data class DataBean(var supplier_detail: SupplierDetailBean? = null,
                    var product: List<ProductBean>? = null,
                    var count: Int? = null) : Parcelable

@Parcelize
data class SupplierDetailBean(var id: Int? = null,
                              var logo: String? = null,
                              var email: String? = null,
                              var trade_license_no: String? = null,
                              var total_reviews: Double? = null,
                              var rating: Float? = null,
                              var name: String? = null,
                              var ratingAndReviews:MutableList<RatingBean>?=null,
                              var user_request_flag: Int? = null,
                              var supplier_branch_id: Int? = null,
                              var timing: List<TimeDataBean>? = null,
                              var payment_method: Int? = null,
                              var phone: String? = null,
                              var description: String? = null,
                              var about: String? = null,
                              var terms_and_conditions: String? = null,
                              var address: String? = null,
                              var delivery_min_time: Int? = null,
                              var delivery_max_time: Int? = null,
                              var min_order: Int? = null,
                              var min_order_delivery: Int? = null,
                              var business_start_date: String? = null,
                              var total_order: Int? = null,
                              var delivery_prior_time: String? = null,
                              var urgent_delivery_time: String? = null,
                              var delivery_charges: Int? = null,
                              var onOffComm: Int? = null,
                              var Favourite: Int? = null,
                              var speciality: String? = null,
                              var nationality: String? = null,
                              var facebook_link: String? = null,
                              var linkedin_link: String? = null,
                              var brand: String? = null,
                              var branchCount: Int? = null,
                              var supplier_image: List<String>? = null,
                              var user_created_id: String? = null,
                              var message_id: String? = null,
                              var is_scheduled: Int? = null,
                              var is_dine_in: Int? = null,
                              var category_id: String? = null,
                              val category:ArrayList<Category>?=null,
                              var is_out_network:Int?=null,
                              var is_promo_codes:Int?=null,
                              var supplier_tags: ArrayList<SupplierTags>? = null,
                              var is_multi_branch: Int? = null,
                              var is_free_delivery:Int?=null) : Parcelable


@Parcelize
data class ProductBean(
        @SerializedName("sub_cat_name", alternate = ["name"])
        var sub_cat_name: String? = null,
        var detailed_sub_category: String? = null,
        var is_SubCat_visible: Boolean? = null,
        val detailed_category_name: List<DetailedCategoryName>? = null,
        var value: MutableList<ProductDataBean>? = null,
        var category_id: String? = null) : Parcelable

@Parcelize
data class TimeDataBean(var week_id: Int? = null,
                        var start_time: String? = null,
                        var end_time: String? = null,
                        var is_open: Int? = null) : Parcelable

@Parcelize
data class DetailedCategoryName(
        val detailed_sub_category_id: Int? = null,
        val name: String? = null
) : Parcelable

