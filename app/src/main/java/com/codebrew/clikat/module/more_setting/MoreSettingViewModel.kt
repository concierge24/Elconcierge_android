package com.codebrew.clikat.module.more_setting

import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.modal.ExampleCommon
import com.trava.user.webservices.RestClient
import com.trava.utilities.getApiError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MoreSettingViewModel(dataManager: DataManager) : BaseViewModel<MoreSettingNavigator>(dataManager) {


    fun rideLogout()
    {
        setIsLoading(true)
        RestClient.get().logout().enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>?, response: Response<Any>?) {
                setIsLoading(false)
                if (response?.isSuccessful == true){
                    navigator.onLogoutSuccess()
                }else{
                    val errorModel = getApiError(response?.errorBody()?.string())
                    navigator.onErrorOccur(errorModel.msg?:"")
                }
            }

            override fun onFailure(call: Call<Any>?, t: Throwable?) {
                setIsLoading(false)
                navigator.onErrorOccur(t?.message?:"")
            }
        })
    }


    fun apiSos() {
        val address = dataManager.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java)
        setIsLoading(true)
        val mParam = HashMap<String?, String?>()
        mParam["user_id"] = dataManager.getKeyValue(PrefenceConstants.USER_ID, PrefenceConstants.TYPE_STRING).toString()
        mParam["device_type"] = "0" //for android 0
        mParam["latitude"] = address?.latitude ?: "0.0"
        mParam["longitude"] = address?.longitude ?: "0.0"
        mParam["address"] = if (!address?.address_line_1.isNullOrEmpty()) address?.address_line_1 else ""

        dataManager.apiSos(mParam)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validateResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun validateResponse(it: ExampleCommon?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onSosSuccess()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
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
