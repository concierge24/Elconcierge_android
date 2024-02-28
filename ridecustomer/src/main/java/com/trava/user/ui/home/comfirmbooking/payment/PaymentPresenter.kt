package com.trava.user.ui.home.comfirmbooking.payment

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.DeleteCardResponseModel
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.CardModel
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentPresenter : BasePresenterImpl<PaymentContract.View>(), PaymentContract.Presenter
{
    override fun saveCard(map: HashMap<String, Any>)
    {
        getView()?.showLoader(true)
        RestClient.recreate().get().addCreditCard(map).enqueue(object : Callback<ApiResponse<CardModel>> {

            override fun onResponse(call: Call<ApiResponse<CardModel>>?, response: Response<ApiResponse<CardModel>>?) {
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

            override fun onFailure(call: Call<ApiResponse<CardModel>>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun getCards() {
        getView()?.showLoader(true)
        RestClient.recreate().get().getCreditCardList().enqueue(object : Callback<ApiResponse<ArrayList<CardModel>>> {

            override fun onResponse(call: Call<ApiResponse<ArrayList<CardModel>>>?, response: Response<ApiResponse<ArrayList<CardModel>>>?) {
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

            override fun onFailure(call: Call<ApiResponse<ArrayList<CardModel>>>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun deleteCards(map: HashMap<String, Any>)
    {
        getView()?.showLoader(true)
//        RestClient.recreate().get().deleteCreditCard(map).enqueue(object : Callback<ApiResponse<CardModel>> {
        RestClient.recreate().get().deleteCreditCard(map).enqueue(object : Callback<DeleteCardResponseModel> {

            override fun onResponse(call: Call<DeleteCardResponseModel>?, response: Response<DeleteCardResponseModel>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.result == 1) {
                        getView()?.onApiSuccess(true)
                    } else {
                        getView()?.apiFailure()
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }

                }


            override fun onFailure(call: Call<DeleteCardResponseModel>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }
}