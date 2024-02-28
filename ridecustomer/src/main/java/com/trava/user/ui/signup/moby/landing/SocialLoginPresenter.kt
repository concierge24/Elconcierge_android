package com.trava.user.ui.signup.moby.landing

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

class SocialLoginPresenter : BasePresenterImpl<SocialLoginInteractor.View>(), SocialLoginInteractor.Presenter  {

    override fun socialLogin(map: Map<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().socialLogin(map).enqueue(object : Callback<ApiResponse<LoginModel>> {

            override fun onResponse(call: Call<ApiResponse<LoginModel>>?, response: Response<ApiResponse<LoginModel>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true){
                    if (response.body()?.statusCode == SUCCESS_CODE) {
                        getView()?.onSocialLoginApiSuccess(response.body()?.result, map)
                    }else{
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                }else{
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginModel>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }
}