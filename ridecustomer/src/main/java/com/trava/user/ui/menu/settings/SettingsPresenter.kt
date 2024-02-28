package com.trava.user.ui.menu.settings

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsPresenter : BasePresenterImpl<SettingsContract.View>(), SettingsContract.Presenter {

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

}