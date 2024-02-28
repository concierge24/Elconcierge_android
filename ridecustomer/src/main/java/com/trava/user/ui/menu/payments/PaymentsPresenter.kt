package com.trava.user.ui.menu.payments

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.paymentdetails.PaymentDetail
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentsPresenter : BasePresenterImpl<PaymentsContract.View>(), PaymentsContract.Presenter {

    override fun paymentDetail(categoryId: String?, categoryProductId: String?) {
        getView()?.showLoader(true)
        RestClient.get().paymentDetail(categoryId, categoryProductId).enqueue(object : Callback<ApiResponse<PaymentDetail>> {

            override fun onResponse(call: Call<ApiResponse<PaymentDetail>>?, response: Response<ApiResponse<PaymentDetail>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onPaymentDetailsApiSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<PaymentDetail>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}