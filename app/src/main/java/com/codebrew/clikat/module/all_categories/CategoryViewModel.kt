package com.codebrew.clikat.module.all_categories

import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.modal.other.CategoryListModel
import com.codebrew.clikat.modal.other.English
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class CategoryViewModel(dataManager: DataManager) : BaseViewModel<CategoryNavigator>(dataManager) {

    val categoryLiveData by lazy { SingleLiveEvent<MutableList<English>>() }


    fun getCategories(zoneFence: String?) {
        setIsLoading(true)

        val catId = if (dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().isNotEmpty()) {
            dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().toInt()
        } else {
            0
        }

        val param = dataManager.updateUserInf()
        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        if(catId!=0)
        {
            param["categoryId"]=catId.toString()
        }

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
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            categoryLiveData.value = it.data?.english ?: mutableListOf()
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
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
