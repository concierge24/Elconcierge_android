package com.trava.user.ui.home.complete_ride

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.PayMayaResponse
import com.trava.user.webservices.models.PaypalToken
import com.trava.user.webservices.models.ThawaniData
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InvoiceRequestPresenter : BasePresenterImpl<InvoiceRequestContrac.View>(), InvoiceRequestContrac.Presenter
{
    override fun tipAmountApi(map: HashMap<String, Any>)
    {
        getView()?.showLoader(true)
        RestClient.recreate().get().tipApi(map).enqueue(object : Callback<ApiResponseNew> {

            override fun onResponse(call: Call<ApiResponseNew>?, response: Response<ApiResponseNew>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.ApiSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseNew>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun applyPaymets(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().paymentApi(map["order_id"].toString(),map["amount"].toString(),map["nonce"].toString()).enqueue(object : Callback<ApiResponseNew> {

            override fun onResponse(call: Call<ApiResponseNew>?, response: Response<ApiResponseNew>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.PaymentApiSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseNew>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun applyPaymetsPayTab(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().paymentApiPayTab(map["order_id"].toString(),map["transaction_id"].toString()).enqueue(object : Callback<ApiResponseNew> {

            override fun onResponse(call: Call<ApiResponseNew>?, response: Response<ApiResponseNew>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.PaymentApiSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseNew>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun getPayPalToken(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().getPaypalToken(map).enqueue(object : Callback<PaypalToken> {

            override fun onResponse(call: Call<PaypalToken>?, response: Response<PaypalToken>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.PaypalTokenApiSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.message)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<PaypalToken>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }


    override fun saveDatatranseData(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().saveDatatranseData(map).enqueue(object : Callback<ApiResponseNew> {

            override fun onResponse(call: Call<ApiResponseNew>?, response: Response<ApiResponseNew>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.DatatranseDataSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseNew>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }


    override fun saveRazorPaymentData(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().saveRazorPaymentData(map).enqueue(object : Callback<ApiResponseNew> {

            override fun onResponse(call: Call<ApiResponseNew>?, response: Response<ApiResponseNew>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.RazorPaymentApiSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseNew>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun getPaymayaUrl(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().getPayMayaUrl(map).enqueue(object : Callback<PayMayaResponse> {
            override fun onResponse(call: Call<PayMayaResponse>?, response: Response<PayMayaResponse>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.PayMayaGeturlSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.message)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<PayMayaResponse>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun getThawaniUrl(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().getThiwaniUrl(map).enqueue(object : Callback<ApiResponse<ThawaniData>> {
            override fun onResponse(call: Call<ApiResponse<ThawaniData>>?, response: Response<ApiResponse<ThawaniData>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.PayThwaniGeturlSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ThawaniData>>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }
}