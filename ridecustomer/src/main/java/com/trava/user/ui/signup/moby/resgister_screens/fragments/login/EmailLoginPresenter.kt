package com.trava.user.ui.signup.moby.resgister_screens.fragments.login

import com.trava.user.ui.signup.moby.landing.SocialLoginInteractor
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.getApiError
import com.trava.utilities.webservices.models.LoginModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmailLoginPresenter : BasePresenterImpl<EmailLoginContractor.View>(), EmailLoginContractor.Presenter  {
    override fun emailLogin(map: Map<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().emailLogin(map).enqueue(object : Callback<ApiResponse<LoginModel>> {

            override fun onResponse(call: Call<ApiResponse<LoginModel>>?, response: Response<ApiResponse<LoginModel>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true){
                    if (response.body()?.statusCode == SUCCESS_CODE) {
                        getView()?.onApiSuccess(response.body()?.result, map)
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