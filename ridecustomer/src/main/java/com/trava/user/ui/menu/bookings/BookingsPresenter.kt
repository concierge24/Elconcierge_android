package com.trava.user.ui.menu.bookings

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingsPresenter: BasePresenterImpl<BookingContract.View>(), BookingContract.Presenter {

    override fun getOrderDetails(orderId: Long) {
        getView()?.showLoader(true)
        RestClient.get().getOrderDetails(orderId).enqueue(object : Callback<ApiResponse<Order>> {

            override fun onResponse(call: Call<ApiResponse<Order>>?, response: Response<ApiResponse<Order>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onOrderDetailsSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Order>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun getHistoryList(take: Int, skip: Int, type: Int,startDate : String , endDate : String,category_id : String) {
        getView()?.showLoader(true)
        RestClient.get().getBookingsHistory(skip = skip,take = take,type = type,startdate = startDate,emdDAte = endDate ,category_id=category_id)
                .enqueue(object : Callback<ApiResponse<ArrayList<Order?>>> {

            override fun onResponse(call: Call<ApiResponse<ArrayList<Order?>>>?, response: Response<ApiResponse<ArrayList<Order?>>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true){
                    getView()?.onApiSuccess(response.body()?.result?:ArrayList())
                    getView()?.onFilterSuccess()

                }else{
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ArrayList<Order?>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun requestCancelApiCall(map: HashMap<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().cancelService(map).enqueue(object : Callback<ApiResponse<Any>> {

            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onCancelApiSuccess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }
}