package com.trava.user.ui.home.comfirmbooking.outstanding_payment

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.trava.user.webservices.models.cards_model.AddCardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OutstandingPaymentPresenter : BasePresenterImpl<OutstandingPaymentContract.View>(), OutstandingPaymentContract.Presenter {

    override fun outStandingPaymentByCard(userCardId: Int, amount: Double) {
        getView()?.showLoader(true)
        RestClient.get().payPendingAmountWithCard(userCardId,amount).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

            override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    response.body()?.msg.let { getView()?.outStandingPaymentSuccess() }
                } else {
                    val errorModel = getApiError(response.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

        })
    }
}