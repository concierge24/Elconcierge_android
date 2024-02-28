package com.trava.user.ui.signup.login

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.SendOtp
import com.trava.user.webservices.models.appsettings.LanguageSets
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPresenter: BasePresenterImpl<LoginContract.View>(), LoginContract.Presenter {

    override fun sendOtpApiCall(map: Map<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().sendOtp(map).enqueue(object : Callback<ApiResponse<SendOtp>>{

            override fun onResponse(call: Call<ApiResponse<SendOtp>>?, response: Response<ApiResponse<SendOtp>>?) {
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

            override fun onFailure(call: Call<ApiResponse<SendOtp>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun updateSettingsApiCall(languageId: String?, notificationFlag: String?) {
        getView()?.showLoader(true)
        RestClient.get().updateSettings(languageId, notificationFlag).enqueue(object : Callback<ApiResponse<Any>> {

            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onSettingsApiSuccess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun getLanguages() {
        getView()?.showLoader(true)
        RestClient.get().getLanguages().enqueue(object : Callback<ApiResponse<ArrayList<LanguageSets>>> {

            override fun onResponse(call: Call<ApiResponse<ArrayList<LanguageSets>>>?, response: Response<ApiResponse<ArrayList<LanguageSets>>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onSettingsApiSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ArrayList<LanguageSets>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}