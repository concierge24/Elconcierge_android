package com.trava.driver.ui.home.emergencyContacts

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseReq
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.EContact
import com.trava.user.webservices.models.eContacts.EContactsListing
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EContactPresenter: BasePresenterImpl<EContactsContract.View>(), EContactsContract.Presenter {

    override fun getContactsList() {
        getView()?.showLoader(true)
        RestClient.get().eContactsList().enqueue(object : Callback<ApiResponse<ArrayList<EContact>>> {
            override fun onResponse(call: Call<ApiResponse<ArrayList<EContact>>>?,
                                    response: Response<ApiResponse<ArrayList<EContact>>>?) {
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

            override fun onFailure(call: Call<ApiResponse<ArrayList<EContact>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

        })
    }

    override fun saveContactList(contactList: JSONArray) {
        getView()?.showLoader(true)
        RestClient.recreate().get().addEContatcs(contactList).enqueue(object : Callback<ApiResponseReq> {

            override fun onResponse(call: Call<ApiResponseReq>?, response: Response<ApiResponseReq>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onApiSuccess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, "error")
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseReq>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

    override fun deleteSosContact(id: String) {
        getView()?.showLoader(true)
        RestClient.recreate().get().removeEContact(id).enqueue(object : Callback<ApiResponseReq> {

            override fun onResponse(call: Call<ApiResponseReq>?, response: Response<ApiResponseReq>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onDeleteSuccess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, "error")
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseReq>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }


    override fun getSosContactsList() {
        getView()?.showLoader(true)
        RestClient.get().getEContactsList().enqueue(object : Callback<ApiResponse<ArrayList<EContactsListing>>> {
            override fun onResponse(call: Call<ApiResponse<ArrayList<EContactsListing>>>?,
                                    response: Response<ApiResponse<ArrayList<EContactsListing>>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onSosApiSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ArrayList<EContactsListing>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

        })
    }


}