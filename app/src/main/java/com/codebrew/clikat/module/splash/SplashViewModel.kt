package com.codebrew.clikat.module.splash

import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.AppConstants.Companion.CURRENCY_SYMBOL
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.GetDbKeyModel
import com.codebrew.clikat.modal.DataCount
import com.codebrew.clikat.modal.PojoPendingOrders
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.retrofit.Config
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class SplashViewModel(dataManager: DataManager) : BaseViewModel<SplashNavigator>(dataManager) {


    val settingLiveData: MutableLiveData<SettingModel.DataBean> by lazy {
        MutableLiveData<SettingModel.DataBean>()
    }


    val orderCountLiveData: MutableLiveData<DataCount> by lazy {
        MutableLiveData<DataCount>()
    }

    val userCodeLiveData: MutableLiveData<GetDbKeyModel> by lazy {
        MutableLiveData<GetDbKeyModel>()
    }

    fun validateUserCode(code: String) {
        setIsLoading(true)

        compositeDisposable.add(dataManager.validateUserCode(code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.codeResponse(it) }, { this.handleError(it) })
        )
    }


    fun fetchSetting() {
        setIsLoading(true)

        compositeDisposable.add(dataManager.getSetting()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    this.validateResponse(it) }, {
                    this.handleError(it) })
        )
    }


    fun fetchOrderCount(accessToken: String) {
        setIsLoading(true)

        compositeDisposable.add(dataManager.getCount(accessToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.orderCountResponse(it) }, { this.handleError(it) })
        )
    }

    private fun orderCountResponse(it: PojoPendingOrders?) {
        setIsLoading(false)

        when (it?.status) {
            NetworkConstants.SUCCESS -> orderCountLiveData.setValue(it.data)
            NetworkConstants.AUTHFAILED -> navigator.onSessionExpire()
            else -> navigator.onErrorOccur(it?.message ?: "")
        }
    }


    private fun validateResponse(it: SettingModel?) {

        setIsLoading(false)

        when (it?.status) {
            NetworkConstants.SUCCESS -> settingLiveData.setValue(it.data)
            NetworkConstants.AUTHFAILED -> navigator.onSessionExpire()
            else -> navigator.onErrorOccur(it?.message ?: "")
        }
    }


    private fun codeResponse(it: GetDbKeyModel?) {

        setIsLoading(false)

        when (it?.statusCode) {
            NetworkConstants.SUCCESS -> {


                if (it.data.data.isNotEmpty()) {

                    dataManager.setkeyValue(DataNames.FEATURE_DATA, Gson().toJson(it.data.featureData
                            ?: ""))

                    Config.dbSecret = it.data.data[0].cbl_customer_domains[0].db_secret_key

                    dataManager.setkeyValue(PrefenceConstants.DB_SECRET, it.data.data[0].cbl_customer_domains[0].db_secret_key)
                    dataManager.setkeyValue(PrefenceConstants.WEBSITE_LINK, it.data.data[0].cbl_customer_domains[0].site_domain)

                    dataManager.setkeyValue(PrefenceConstants.AGENT_DB_SECRET, it.data.data[0].cbl_customer_domains[0].agent_db_secret_key)

                    dataManager.setkeyValue(PrefenceConstants.APP_UNIQUE_CODE, it.data.data[0].uniqueId)

                    dataManager.addGsonValue(PrefenceConstants.DB_INFORMATION, Gson().toJson(it.data.data[0].cbl_customer_domains[0]))

                    userCodeLiveData.value = it
                }

                if (!it.data.currency.isNullOrEmpty()) {
                    dataManager.addGsonValue(PrefenceConstants.CURRENCY_INF
                            ,
                            Gson().toJson(it.data.currency[0]))

                    CURRENCY_SYMBOL = it.data.currency[0].currency_symbol ?: ""
                }

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
