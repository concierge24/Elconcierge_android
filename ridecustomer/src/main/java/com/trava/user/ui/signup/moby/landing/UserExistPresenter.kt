package com.trava.user.ui.signup.moby.landing

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.moby.response.UserExistResponseModel
import com.trava.user.webservices.moby.response.UserExistResult
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserExistPresenter : BasePresenterImpl<UserExistanceConract.View>(), UserExistanceConract.Presenter {

    override fun checkUserExist(social_key : String ,login_as : String) {
        getView()?.showLoader(true)
        RestClient.get().checkuserExistance(social_key,login_as).enqueue(object : Callback<ApiResponse<UserExistResult>> {

            override fun onResponse(call: Call<ApiResponse<UserExistResult>>?, response: Response<ApiResponse<UserExistResult>>?) {
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

            override fun onFailure(call: Call<ApiResponse<UserExistResult>>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })

    }
}

