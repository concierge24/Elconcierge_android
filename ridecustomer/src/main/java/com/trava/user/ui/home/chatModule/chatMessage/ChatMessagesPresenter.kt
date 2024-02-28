package com.trava.user.ui.home.chatModule.chatMessage

import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatMessagesPresenter : BasePresenterImpl<ChatMessageContract.View>(), ChatMessageContract.Presenter {

    override fun getChatMessagesApiCall(orderId:String,messageId: String, receiverId: String, limit: Int, skip: Int, messageOrder: String) {
        getView()?.showLoader(true)
       RestClient.get().getChatMessages( orderId,messageId, receiverId, limit, skip, messageOrder)
        .enqueue(object : Callback<ApiResponse<ArrayList<ChatMessageListing>>> {
            override fun onFailure(call: Call<ApiResponse<ArrayList<ChatMessageListing>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

            override fun onResponse(call: Call<ApiResponse<ArrayList<ChatMessageListing>>>?, response: Response<ApiResponse<ArrayList<ChatMessageListing>>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.chatMessagesApiSuccess(response.body()?.result, messageOrder)
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