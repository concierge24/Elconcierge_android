package com.trava.user.ui.menu.contactUs

import com.trava.user.webservices.RestClient
import com.trava.driver.ui.home.contactUs.ContactUsContract
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactUsPresenter : BasePresenterImpl<ContactUsContract.View>(), ContactUsContract.Presenter {
    override fun sendMsg(msg: String) {
        getView()?.showLoader(true)
        RestClient.get().sendMessage(msg).
                enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>?, response: Response<Any>?) {
                        getView()?.showLoader(false)
                        if (response?.isSuccessful == true) {
                            getView()?.onApiSuccess()
                        }else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }

                    override fun onFailure(call: Call<Any>?, t: Throwable?) {
                        getView()?.showLoader(false)
                        getView()?.apiFailure()
                    }
                })
    }


}