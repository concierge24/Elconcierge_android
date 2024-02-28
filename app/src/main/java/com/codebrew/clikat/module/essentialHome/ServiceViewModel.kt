package com.codebrew.clikat.module.essentialHome

import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.ClikatConstants
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.applabels.AppLabels
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.utilities.Utils
import com.trava.utilities.getApiError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ServiceViewModel(dataManager: DataManager) : BaseViewModel<BaseInterface>(dataManager) {

    val homeDataLiveData by lazy { SingleLiveEvent<Data>() }

    val rideSettingLiveData by lazy { SingleLiveEvent<SettingItems>() }

    val appLabelLiveData by lazy { SingleLiveEvent<com.trava.user.webservices.models.applabels.Data>() }

    fun getRideSetting() {

        setIsLoading(true)
        RestClient.recreate().get().appSettingApi().enqueue(object : Callback<ApiResponse<SettingItems>> {

            override fun onResponse(call: Call<ApiResponse<SettingItems>>?, response: Response<ApiResponse<SettingItems>>?) {
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        rideSettingLiveData.value = response.body()?.result
                    } else {
                        navigator.onErrorOccur(response.body()?.msg ?: "")
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<SettingItems>>?, t: Throwable?) {
                navigator?.onErrorOccur("Something went wrong" ?: "")
            }
        })
    }

    fun getAppLables() {
        setIsLoading(true)
        RestClient.recreate().get().getAppLables().enqueue(object : Callback<AppLabels<com.trava.user.webservices.models.applabels.Data>> {

            override fun onResponse(call: Call<AppLabels<com.trava.user.webservices.models.applabels.Data>>?, response: Response<AppLabels<com.trava.user.webservices.models.applabels.Data>>?) {
                setIsLoading(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        appLabelLiveData.value = response.body()?.data
                    } else {
                        navigator.onErrorOccur(response.body()?.msg ?: "")
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    navigator.onErrorOccur(errorModel.msg ?: "")
                }
            }

            override fun onFailure(call: Call<AppLabels<com.trava.user.webservices.models.applabels.Data>>?, t: Throwable?) {
                navigator.onErrorOccur(t?.message ?: "")
                setIsLoading(false)
            }
        })
    }


    fun getCategories(zoneFence: String?) {
        setIsLoading(true)

        val param = dataManager.updateUserInf()
        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()


        val api = if (zoneFence == "1")
            dataManager.getAllCategoryNewV1(param)
        else
            dataManager.getAllCategoryNew(param)

        compositeDisposable.add(api
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponse(it) }, { this.handleError(it) })
        )
    }


    private fun validateResponse(it: CategoryListModel?) {
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                homeDataLiveData.value = it.data
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }


    private fun handleError(e: Throwable) {
        setIsLoading(false)
        handleErrorMsg(e).let {
            if (it == NetworkConstants.AUTH_MSG) {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            } else {
                navigator.onErrorOccur(it)
            }
        }
    }
}



