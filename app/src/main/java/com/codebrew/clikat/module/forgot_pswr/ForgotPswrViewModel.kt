package com.codebrew.clikat.module.forgot_pswr

import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.modal.ExampleCommon
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ForgotPswrViewModel(
        dataManager: DataManager
) : BaseViewModel<ForgotNavigator>(dataManager) {

    fun forgotPass(email: String) {
        setIsLoading(true)

        dataManager.forgotPassword(email)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validateResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }
    }

    private fun validateResponse(it: ExampleCommon?) {

        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onForgotPswr(it.message)
            }
            NetworkConstants.AUTHFAILED -> navigator.onSessionExpire()
            else -> navigator.onErrorOccur(it?.message ?: "")
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
