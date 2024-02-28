package com.trava.user.ui.home.promocodes

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.promocodes.CouponsItem
import com.trava.user.webservices.models.promocodes.PromoCodeResponse
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.trava.user.webservices.models.travelPackages.PackagesItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PromoCodesPresenter : BasePresenterImpl<PromoCodeContract.View>(), PromoCodeContract.Presenter {
    override fun checkPromoCodeApiCall(code: String) {
        getView()?.showLoader(true)
        RestClient.get().checkCoupenCode(code).enqueue(object : Callback<ApiResponse<CouponsItem>> {

            override fun onResponse(call: Call<ApiResponse<CouponsItem>>?, response: Response<ApiResponse<CouponsItem>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onApiCheckPromoSuccess(it) }
                    } else {
                        val errorBody = response.errorBody()?.string()
//                        val errorResponse: ApiResponse<Order> = Gson().fromJson(errorBody, object : TypeToken<ApiResponse<Order>>() {}.type)
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    val errorModel = getApiError(errorBody)
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)

                }
            }

            override fun onFailure(call: Call<ApiResponse<CouponsItem>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })

    }

    override fun requestPromoCodesapiCall() {
        getView()?.showLoader(true)
        RestClient.get().getPromoCodesList().enqueue(object : Callback<ApiResponse<PromoCodeResponse>> {

            override fun onResponse(call: Call<ApiResponse<PromoCodeResponse>>?, response: Response<ApiResponse<PromoCodeResponse>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onApiSuccess(it) }
                    } else {
                        val errorBody = response.errorBody()?.string()
//                        val errorResponse: ApiResponse<Order> = Gson().fromJson(errorBody, object : TypeToken<ApiResponse<Order>>() {}.type)
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)

                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    val errorModel = getApiError(errorBody)
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)

                }
            }

            override fun onFailure(call: Call<ApiResponse<PromoCodeResponse>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}