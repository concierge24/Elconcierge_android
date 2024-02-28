package com.trava.user.ui.home.payment

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.trava.user.webservices.models.cards_model.AddCardResponse
import com.trava.user.webservices.models.contacts.RideShareResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SavingCardPresenter : BasePresenterImpl<SavingCardContract.View>(), SavingCardContract.Presenter {

    override fun requestUserCreditPoints() {
        getView()?.showLoader(false)
        RestClient.get().getUserCreditPoints().enqueue(object : Callback<ApiResponse<RideShareResponse>> {
            override fun onFailure(call: Call<ApiResponse<RideShareResponse>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

            override fun onResponse(call: Call<ApiResponse<RideShareResponse>>, response: Response<ApiResponse<RideShareResponse>>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onCreditPointsSucess(it) }
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

        })
    }

    override fun removeCardApi(userCardId : Int) {
        getView()?.showLoader(false)
        RestClient.get().removeCard(userCardId).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

            override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    response.body()?.result?.let { getView()?.onRemoveCardSuccess() }

                } else {
                    val errorModel = getApiError(response.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

        })
    }

    override fun addCardApi() {
        getView()?.showLoader(false)
        RestClient.get().addCard().enqueue(object : Callback<ApiResponse<AddCardResponse>> {
            override fun onFailure(call: Call<ApiResponse<AddCardResponse>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

            override fun onResponse(call: Call<ApiResponse<AddCardResponse>>, response: Response<ApiResponse<AddCardResponse>>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    response.body()?.result?.let { getView()?.onAddCardApiSuccess(it) }

                } else {
                    val errorModel = getApiError(response.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

        })
    }
}