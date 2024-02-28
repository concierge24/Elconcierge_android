package com.trava.user.ui.signup.moby.resgister_screens.fragments.institution

import com.trava.user.ui.signup.moby.resgister_screens.fragments.login.EmailLoginContractor
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.moby.response.InstituteListing
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.getApiError
import com.trava.utilities.webservices.models.LoginModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InstituteListingPresenter : BasePresenterImpl<InstituteListingContractor.View>(), InstituteListingContractor.Presenter {
    override fun getInstituteListing(items: String, cooperation_type: String) {
        getView()?.showLoader(true)
        RestClient.recreate().get().getCooperationListing(items, cooperation_type).enqueue(object : Callback<ApiResponse<ArrayList<InstituteListing>>> {

            override fun onResponse(call: Call<ApiResponse<ArrayList<InstituteListing>>>?, response: Response<ApiResponse<ArrayList<InstituteListing>>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onApiSuccess(response.body()?.result ?: ArrayList())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ArrayList<InstituteListing>>>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }
}