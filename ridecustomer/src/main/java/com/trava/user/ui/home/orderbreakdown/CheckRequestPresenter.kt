package com.trava.user.ui.home.orderbreakdown

import com.google.gson.JsonArray
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseReq
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.CheckListModelUpdate
import com.trava.user.webservices.models.order.CheckListModelArray
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckRequestPresenter : BasePresenterImpl<InvoiceRequestContract.View>(), InvoiceRequestContract.Presenter
{
    override fun deleteReqApi(ids: String) {
        getView()?.showLoader(true)
        RestClient.recreate().get().deleteCheckList(ids).enqueue(object : Callback<ApiResponseReq> {

            override fun onResponse(call: Call<ApiResponseReq>?, response: Response<ApiResponseReq>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.ApiSuccessDelete()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode,"error")
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseReq>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun updataReqApi(check_lists: JSONArray) {
        getView()?.showLoader(true)
        RestClient.recreate().get().updateCheckList(check_lists).enqueue(object : Callback<ApiResponseReq> {

            override fun onResponse(call: Call<ApiResponseReq>?, response: Response<ApiResponseReq>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.ApiSuccessUpdate()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, "error")
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseReq>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun onGoingOrderApi() {
        getView()?.showLoader(true)
        RestClient.get().onGoingOrder().enqueue(object : Callback<ApiResponse<List<Order>>> {

            override fun onResponse(call: Call<ApiResponse<List<Order>>>?, response: Response<ApiResponse<List<Order>>>?) {
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

            override fun onFailure(call: Call<ApiResponse<List<Order>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }
}