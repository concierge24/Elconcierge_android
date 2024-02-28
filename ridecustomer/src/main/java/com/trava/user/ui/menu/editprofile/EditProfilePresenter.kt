package com.trava.user.ui.menu.editprofile

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.getApiError
import com.trava.utilities.webservices.models.AppDetail
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditProfilePresenter : BasePresenterImpl<EditProfileContract.View>(), EditProfileContract.Presenter {

    override fun updateProfileApiCall(name: String?, email : String, phoneNumber : String, phonecode : String, profilePicFile: File?) {
        val map = HashMap<String, RequestBody>()
        map["name"] = RequestBody.create("text/plain".toMediaTypeOrNull(), name?:"")
        map["email"] = RequestBody.create("text/plain".toMediaTypeOrNull(), email?:"")
        map["phone_number"] = RequestBody.create("text/plain".toMediaTypeOrNull(),phoneNumber?:"")
        map["phone_code"] = RequestBody.create("text/plain".toMediaTypeOrNull(),phonecode?:"")
        if (profilePicFile != null && profilePicFile.exists()) {
            val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), profilePicFile)
            map["profile_pic\"; filename=\"profilePic.png\" "] = body
        }
        getView()?.showLoader(true)
        RestClient.recreate().get().updateProfile(map).enqueue(object : Callback<ApiResponse<AppDetail>> {
            override fun onResponse(call: Call<ApiResponse<AppDetail>>?, response: Response<ApiResponse<AppDetail>>?) {

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

            override fun onFailure(call: Call<ApiResponse<AppDetail>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}