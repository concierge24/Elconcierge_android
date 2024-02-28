package com.trava.user.ui.menu.travelPackages

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.trava.user.webservices.models.travelPackages.PackagesItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TravelPackagesPresenter : BasePresenterImpl<TravellingPackagesContract.View>(), TravellingPackagesContract.Presenter {

    override fun requestGetPackagesApiCall() {
        getView()?.showLoader(true)
        RestClient.get().getPackageListing().enqueue(object : Callback<ApiResponse<List<PackagesItem>>> {

            override fun onResponse(call: Call<ApiResponse<List<PackagesItem>>>?, response: Response<ApiResponse<List<PackagesItem>>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onApiSuccess(it) }
                    } else {
                        val errorBody = response.errorBody()?.string()
//                        val errorResponse: ApiResponse<Order> = Gson().fromJson(errorBody, object : TypeToken<ApiResponse<Order>>() {}.type)
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)

                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    val errorModel = getApiError(errorBody)
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)

                }
            }

            override fun onFailure(call: Call<ApiResponse<List<PackagesItem>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}