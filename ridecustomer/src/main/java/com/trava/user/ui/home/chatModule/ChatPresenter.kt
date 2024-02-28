package com.trava.user.ui.home.chatModule

import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatPresenter : BasePresenterImpl<ChatContract.View>(), ChatContract.Presenter {

    override fun getChatLogsApiCall(authorization: String, limit: Int, skip: Int) {
        RestClient.get().getChatLogs( limit, skip).enqueue(object : Callback<ApiResponse<ArrayList<ChatMessageListing>>> {
            override fun onFailure(call: Call<ApiResponse<ArrayList<ChatMessageListing>>>?, t: Throwable?) {
                getView()?.apiFailure()
            }

            override fun onResponse(call: Call<ApiResponse<ArrayList<ChatMessageListing>>>?, response: Response<ApiResponse<ArrayList<ChatMessageListing>>>?) {
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.chatLogsApiSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

        })
    }
}