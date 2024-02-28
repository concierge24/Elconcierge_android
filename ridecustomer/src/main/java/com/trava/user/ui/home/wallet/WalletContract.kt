package com.trava.user.ui.home.wallet

import com.trava.driver.webservices.models.WalletBalModel
import com.trava.driver.webservices.models.WalletHisResponse
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.SendMoneyResponseModel
import com.trava.user.webservices.models.ThawaniData
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView


class WalletContract {
    interface View: BaseView {
        fun onWalletHistorySuccess(listHistory: WalletHisResponse?)
        fun onWalletAddedSuccess(response: Order?)
        fun onWalletBalSuccess(response: WalletBalModel?)
        fun onMoneySentSuccessFully(response : SendMoneyResponseModel?)
        fun RazorPaymentApiSuccess(apiResponseNew: ApiResponseNew?)
        fun PayThwaniGeturlSuccess(apiResponseNew: ApiResponse<ThawaniData>?)

    }

    interface Presenter: BasePresenter<View> {
        fun getWalletHistoryList(take: Int, skip: Int)
        fun addMoneyToWallet(amount : Int , cardId : String , gatewayUniqueId : String)
        fun getWalletBalance(isLoader: Boolean)
        fun sendWalletMoney(amount : Int , phoneCode : String , phoneNumber : String)
        fun saveRazorPaymentData(map: HashMap<String, Any>)
        fun getThawaniUrl(map:HashMap<String,Any>)
    }
}