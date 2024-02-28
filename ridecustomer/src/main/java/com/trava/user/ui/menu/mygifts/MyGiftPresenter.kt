package com.trava.user.ui.menu.mygifts

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.gift.ResultItem
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyGiftPresenter : BasePresenterImpl<MyGiftContract.View>(), MyGiftContract.Presenter {
    override fun getReceivedGiftList(take: Int, skip: Int) {
        getView()?.showLoader(true)
        RestClient.get().getReceivedGiftList(skip = skip, take = take)
                .enqueue(object : Callback<ApiResponse<ArrayList<ResultItem?>>> {
                    override fun onResponse(call: Call<ApiResponse<ArrayList<ResultItem?>>>?, response: Response<ApiResponse<ArrayList<ResultItem?>>>?) {
                        getView()?.showLoader(false)
                        if (response?.isSuccessful == true) {
                            getView()?.onReceivedGiftSuccess(response.body()?.result ?: ArrayList())
                        } else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ArrayList<ResultItem?>>>?, t: Throwable?) {
                        getView()?.showLoader(false)
                        getView()?.apiFailure()
                    }
                })
    }

    override fun getSentGiftList(take: Int, skip: Int) {
        getView()?.showLoader(true)
        RestClient.get().getSentGiftList(skip = skip, take = take)
                .enqueue(object : Callback<ApiResponse<ArrayList<ResultItem?>>> {

                    override fun onResponse(call: Call<ApiResponse<ArrayList<ResultItem?>>>?, response: Response<ApiResponse<ArrayList<ResultItem?>>>?) {
                        getView()?.showLoader(false)
                        if (response?.isSuccessful == true) {
                            getView()?.onSentGiftSuccess(response.body()?.result ?: ArrayList())
                        } else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ArrayList<ResultItem?>>>?, t: Throwable?) {
                        getView()?.showLoader(false)
                        getView()?.apiFailure()
                    }
                })
    }

    override fun acceptRejectGiftSuccess(order_id: String, action: String) {
        getView()?.showLoader(true)
        RestClient.get().acceptRejectGift(order_id = order_id, action = action)
                .enqueue(object : Callback<ApiResponse<Any>> {

                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        getView()?.showLoader(false)
                        if (response?.isSuccessful == true) {
                            getView()?.acceptRejectGiftSuccess(response.body()!!)
                        } else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable?) {
                        getView()?.showLoader(false)
                        getView()?.apiFailure()
                    }
                })
    }
}