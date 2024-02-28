package com.trava.user.ui.signup.verifytop

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.SendOtp
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.getApiError
import com.trava.utilities.webservices.models.LoginModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyOtpPresenter : BasePresenterImpl<VerifyOtpContract.View>(), VerifyOtpContract.Presenter {

    override fun verifyOtp(map: Map<String, String>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().verifyOtp(map).enqueue(object : Callback<ApiResponse<LoginModel>> {

            override fun onResponse(call: Call<ApiResponse<LoginModel>>?, response: Response<ApiResponse<LoginModel>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onApiSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginModel>>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun sendOtpApiCall(map: Map<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().sendOtp(map).enqueue(object : Callback<ApiResponse<SendOtp>> {

            override fun onResponse(call: Call<ApiResponse<SendOtp>>?, response: Response<ApiResponse<SendOtp>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == SUCCESS_CODE) {
                        getView()?.resendOtpSuccss(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<SendOtp>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}