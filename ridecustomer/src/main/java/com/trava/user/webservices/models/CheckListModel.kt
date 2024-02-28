package com.trava.user.webservices.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CheckListModel(var item_name : String
                        ,var price: String?):Parcelable


@Parcelize
data class CheckListModelUpdate(var check_list_id : String
                          ,var price: String?,var tax:String):Parcelable


@Parcelize
data class ProductListModel (var product_name:String, var receiver_name:String,var receiver_phone_number:String,var receiver_phone_code:String, var product_image:String): Parcelable


@Parcelize
data class ProductCheckListModel (var product_code:String,var phone_number:String):Parcelable