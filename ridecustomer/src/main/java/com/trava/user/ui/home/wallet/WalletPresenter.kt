package com.trava.user.ui.home.wallet


import com.trava.driver.webservices.models.WalletBalModel
import com.trava.driver.webservices.models.WalletHisResponse
import com.trava.user.ui.home.wallet.WalletContract
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.SendMoneyResponseModel
import com.trava.user.webservices.models.ThawaniData
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WalletPresenter : BasePresenterImpl<WalletContract.View>(), WalletContract.Presenter {

    override fun getWalletHistoryList(take: Int, skip: Int) {
        getView()?.showLoader(true)
        RestClient.get().getWalletHistory(skip = skip, take = take)
                .enqueue(object : Callback<WalletHisResponse> {

                    override fun onResponse(call: Call<WalletHisResponse>?, response: Response<WalletHisResponse>?) {
                        getView()?.showLoader(false)
                        if (response?.isSuccessful == true) {
                            getView()?.onWalletHistorySuccess(response.body())

                        } else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }

                    override fun onFailure(call: Call<WalletHisResponse>?, t: Throwable?) {
                        getView()?.showLoader(false)
                        getView()?.apiFailure()
                    }
                })
    }

    override fun addMoneyToWallet( amount : Int , cardId : String , gatewayUniqueId : String ) {
        getView()?.showLoader(true)
        RestClient.get().addToWallet(amount,cardId,gatewayUniqueId)
                .enqueue(object : Callback<ApiResponse<Order?>> {
                    override fun onResponse(call: Call<ApiResponse<Order?>>?, response: Response<ApiResponse<Order?>>?) {
                        getView()?.showLoader(false)
                        if (response?.isSuccessful == true) {
                            getView()?.onWalletAddedSuccess(response.body()?.result)

                        } else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Order?>>?, t: Throwable?) {
                        getView()?.showLoader(false)
                        getView()?.apiFailure()
                    }
                })
    }

    override fun getWalletBalance(isLoader: Boolean) {
        if (isLoader)
            getView()?.showLoader(true)
        RestClient.get().getWalletBalance()
                .enqueue(object : Callback<ApiResponse<WalletBalModel?>> {
                    override fun onResponse(call: Call<ApiResponse<WalletBalModel?>>?, response: Response<ApiResponse<WalletBalModel?>>?) {
                        getView()?.showLoader(false)
                        if (response?.isSuccessful == true) {
                            getView()?.onWalletBalSuccess(response.body()?.result)
                        } else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<WalletBalModel?>>?, t: Throwable?) {
                        getView()?.showLoader(false)
                        getView()?.apiFailure()
                    }
                })
    }

    override fun sendWalletMoney(amount: Int, phoneCode: String, phoneNumber: String) {
        getView()?.showLoader(true)
        RestClient.get().walletTransfer(amount,phoneCode,phoneNumber).enqueue(object : Callback<ApiResponse<SendMoneyResponseModel>> {
            override fun onResponse(call: Call<ApiResponse<SendMoneyResponseModel>>?, response: Response<ApiResponse<SendMoneyResponseModel>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == SUCCESS_CODE) {
                        getView()?.onMoneySentSuccessFully(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<SendMoneyResponseModel>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
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