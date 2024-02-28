package com.trava.user.ui.menu.noticeBoard


import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.noticeBoard.NotificationData
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeBoardPresenter: BasePresenterImpl<NoticeBoardContract.View>(), NoticeBoardContract.Presenter {
    override fun getNotifications() {
        getView()?.showLoader(true)
        RestClient.get().getNotificationListing().enqueue(object : Callback<ApiResponse<ArrayList<NotificationData>>> {
            override fun onResponse(call: Call<ApiResponse<ArrayList<NotificationData>>>?,
                                    response: Response<ApiResponse<ArrayList<NotificationData>>>?) {
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

            override fun onFailure(call: Call<ApiResponse<ArrayList<NotificationData>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

        })
    }

}