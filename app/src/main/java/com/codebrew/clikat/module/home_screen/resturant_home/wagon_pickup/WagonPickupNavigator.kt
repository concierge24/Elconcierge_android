package com.codebrew.clikat.module.home_screen.resturant_home.wagon_pickup

import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.model.api.ZoneData
import com.codebrew.clikat.data.model.api.supplier_detail.DataSupplierDetail
import com.codebrew.clikat.modal.other.SupplierInArabicBean

interface WagonPickupNavigator : BaseInterface {

    fun onFavStatus()
    fun unFavSupplierResponse(data: SupplierInArabicBean?)
    fun favSupplierResponse(supplierId: SupplierInArabicBean?)
    fun supplierDetailSuccess(data: DataSupplierDetail)
    fun onZoneResponse(data : List<ZoneData>)
    fun onTableSuccessfullyBooked()
}
