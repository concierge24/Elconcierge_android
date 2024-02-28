package com.codebrew.clikat.module.loyaltyPoints

import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.model.api.LoyaltyResponse
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject


class LoyaltyPointsViewModel(dataManager: DataManager) : BaseViewModel<LoyaltyPointsNavigator>(dataManager) {


    fun apiGetLoyaltyPoints() {
        setIsLoading(true)
        compositeDisposable.add(dataManager.getLoyaltyPointsData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponse(it) }, { this.handleError(it) })
        )
    }

    private fun validateResponse(it: LoyaltyResponse?) {
        setIsLoading(false)

        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.loyaltyPointsSuccess(it.data)

            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 ->
                    navigator.onErrorOccur(it1.toString()) }
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
