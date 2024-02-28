package com.trava.user.ui.menu.earnings

import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.earnings.AdsEarningResponse
import com.trava.user.webservices.models.earnings.ResultItem
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EarningPresenter : BasePresenterImpl<EarningContract.View>(), EarningContract.Presenter {
    override fun requestEarnings(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.get().requestEarnings(map).enqueue(object : Callback<AdsEarningResponse> {

            override fun onResponse(call: Call<AdsEarningResponse>?, response: Response<AdsEarningResponse>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.let { getView()?.onEarningAiSuccess(it) }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    val errorModel = getApiError(errorBody)
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<AdsEarningResponse>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }
}