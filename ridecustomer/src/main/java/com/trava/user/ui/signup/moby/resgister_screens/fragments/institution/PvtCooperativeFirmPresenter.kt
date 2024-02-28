package com.trava.user.ui.signup.moby.resgister_screens.fragments.institution

import com.trava.user.ui.signup.moby.resgister_screens.fragments.login.EmailLoginContractor
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.moby.response.PvtCooperativeRegResponseModel
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.getApiError
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class PvtCooperativeFirmPresenter : BasePresenterImpl<PvyCooperativeFirmContarctor.View>(),
                                                        PvyCooperativeFirmContarctor.Presenter {

    override fun pvyCooperariveRegister(cooperation_id: String?, identification_number: String?,
                                        email: String, phone_code: String, iso: String,
                                        phone_number: String, password: String, timezone: String,
                                        latitude: String, longitude: String, document: File?) {
        val map = HashMap<String, RequestBody>()
        map["cooperation_id"] = RequestBody.create("text/plain".toMediaTypeOrNull(), cooperation_id?:"")
        map["identification_number"] = RequestBody.create("text/plain".toMediaTypeOrNull(), identification_number?:"")
        map["email"] = RequestBody.create("text/plain".toMediaTypeOrNull(), email?:"")
        map["phone_code"] = RequestBody.create("text/plain".toMediaTypeOrNull(),phone_code?:"")
        map["iso"] = RequestBody.create("text/plain".toMediaTypeOrNull(),iso?:"")
        map["phone_number"] = RequestBody.create("text/plain".toMediaTypeOrNull(),phone_number?:"")
        map["password"] = RequestBody.create("text/plain".toMediaTypeOrNull(),password?:"")
        map["timezone"] = RequestBody.create("text/plain".toMediaTypeOrNull(),timezone?:"")
        map["latitude"] = RequestBody.create("text/plain".toMediaTypeOrNull(),latitude?:"")
        map["longitude"] = RequestBody.create("text/plain".toMediaTypeOrNull(),longitude?:"")
        map["device_type"] = RequestBody.create("text/plain".toMediaTypeOrNull(),"Android")
        map["user_type_id"] = RequestBody.create("text/plain".toMediaTypeOrNull(),"1")
        if (document != null && document.exists()) {
            val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), document)
            map["document"] = body
        }
        getView()?.showLoader(true)
        RestClient.recreate().get().pvtCooperativeRegfirm(map).enqueue(object : Callback<ApiResponse<PvtCooperativeRegResponseModel>> {
            override fun onResponse(call: Call<ApiResponse<PvtCooperativeRegResponseModel>>?, response: Response<ApiResponse<PvtCooperativeRegResponseModel>>?) {

                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == SUCCESS_CODE) {
                        getView()?.onApiSuccess(response?.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<PvtCooperativeRegResponseModel>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })

//        RestClient.get().pvtCooperativeRegfirm(map).enqueue(object : Callback<ApiResponse<PvtCooperativeRegResponseModel>> {
//
//            override fun onResponse(call: Call<ApiResponse<PvtCooperativeRegResponseModel>>?, response: Response<ApiResponse<PvtCooperativeRegResponseModel>>?) {
//                getView()?.showLoader(false)
//                if (response?.isSuccessful == true){
//                    if (response.body()?.statusCode == SUCCESS_CODE) {
//                        getView()?.onApiSuccess(response.body()?.result, map)
//                    }else{
//                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
//                    }
//                }else{
//                    val errorModel = getApiError(response?.errorBody()?.string())
//                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse<PvtCooperativeRegResponseModel>>?, t: Throwable?) {
//                getView()?.showLoader(false)
//                getView()?.apiFailure()
//            }
//        })
    }


}