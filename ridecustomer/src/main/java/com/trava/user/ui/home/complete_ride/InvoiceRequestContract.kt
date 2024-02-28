package com.trava.user.ui.home.complete_ride

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.PayMayaResponse
import com.trava.user.webservices.models.PaypalToken
import com.trava.user.webservices.models.ThawaniData
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class InvoiceRequestContrac
{
    interface View: BaseView {
        fun ApiSuccess(order: ApiResponseNew?)
        fun PaymentApiSuccess(order: ApiResponseNew?)
        fun PaypalTokenApiSuccess(paypalToken: PaypalToken?)
        fun DatatranseDataSuccess(apiResponseNew: ApiResponseNew?)
        fun RazorPaymentApiSuccess(apiResponseNew: ApiResponseNew?)
        fun PayMayaGeturlSuccess(apiResponseNew: PayMayaResponse?)
        fun PayThwaniGeturlSuccess(apiResponseNew: ApiResponse<ThawaniData>?)
    }

    interface Presenter: BasePresenter<View> {
        fun tipAmountApi(map: HashMap<String, Any>)
        fun applyPaymets(map: HashMap<String, Any>)
        fun getPayPalToken(map: HashMap<String, Any>)
        fun saveDatatranseData(map: HashMap<String, Any>)
        fun saveRazorPaymentData(map: HashMap<String, Any>)
        fun applyPaymetsPayTab(map: HashMap<String, Any>)
        fun getPaymayaUrl(map:HashMap<String,Any>)
        fun getThawaniUrl(map:HashMap<String,Any>)
    }
}