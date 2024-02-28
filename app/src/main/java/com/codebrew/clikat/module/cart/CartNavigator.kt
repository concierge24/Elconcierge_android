package com.codebrew.clikat.module.cart

import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.model.api.*
import com.codebrew.clikat.data.model.api.distance_matrix.DistanceMatrix
import com.codebrew.clikat.data.model.api.paystack_url.PayStackCode
import com.codebrew.clikat.data.model.api.tap_payment.Transaction
import com.codebrew.clikat.modal.other.AddtoCartModel
import com.codebrew.clikat.modal.other.PromoCodeModel
import java.util.*

interface CartNavigator : BaseInterface {

    fun onUpdateCart(){}

    fun onRefreshCartError(){}

    fun onCartAdded(cartdata: AddtoCartModel.CartdataBean?) {}

    fun onOrderPlaced(data: ArrayList<Int>){}

    fun onValidatePromo(data: PromoCodeModel.DataBean){}

    fun onRefreshCart(mCartData: CartData?, loginFromCart: Boolean){}

    fun onCalculateDistance(value: DistanceMatrix?, supplierId: Int?){}

    fun onAddress(data: DataBean?){}

    fun onReferralAmt(value: Float?){}
    fun getSaddedPaymentSuccess(data: AddCardResponseData?){}
    fun getThawaniPaymentSuccess(data: String?){}
    fun getTelrPaymentSuccess(data: AddCardResponseData?){}
    fun getWindCavePaymentSuccess(data: AddCardResult?){}
    fun getMyFatoorahPaymentSuccess(data: AddCardResponseData?){}

    fun onTapPayment(transaction: Transaction){}
    fun onEvalonPayment(data: String){}

    fun getmPaisaPaymentSuccess(data: AddCardResponseData?){}
    fun getAamarPayPaymentSuccess(data: AddCardResponseData?){}

    fun getPayMayaUrl(data: AddCardResponseData?){}
    fun onBrainTreeTokenSuccess(clientToken:String?){}

    fun userBlocked(){}

    fun userUnBlock(){}
    fun onCancelSubscription(message: String) {}
    fun onGeofencePayment(data: GeofenceData?){}
    fun onBuySubscription(data: BuySubscription) {}
    fun onSubscripDetailList(data: MutableList<SubcripModel>) {}

    fun onTokenGenerate(token: String, message: String){}
    fun onProfileUpdate(){}

    fun onPayStackUrl(data: PayStackCode){}
}
