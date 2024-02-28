package com.trava.user.ui.signup.entername

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

class EnterNamePresenter : BasePresenterImpl<EnterNameContract.View>(), EnterNameContract.Presenter {

    override fun addNameApiCall(firstName: String, lastName: String, gender: String, address: String,
                                email: String, referralCode: String, nationalID: String,
                                f_name: String, f_number: String, n_name: String, n_number: String,
                                pathFrontImagee: File, pathBackImagee: File, pathSchoolIdImage: File
                                , pathAddressProofImage: File, pathProfileImage: File)
    {
        getView()?.showLoader(true)
        val map = HashMap<String, RequestBody>()
        map["f_name"] = RequestBody.create("text/plain".toMediaTypeOrNull(), f_name ?: "")
        map["f_number"] = RequestBody.create("text/plain".toMediaTypeOrNull(), f_number ?: "")
        map["n_name"] = RequestBody.create("text/plain".toMediaTypeOrNull(), n_name ?: "")
        map["n_number"] = RequestBody.create("text/plain".toMediaTypeOrNull(), n_number ?: "")

        map["firstName"] = RequestBody.create("text/plain".toMediaTypeOrNull(), firstName ?: "")
        map["lastName"] = RequestBody.create("text/plain".toMediaTypeOrNull(), lastName ?: "")
        map["gender"] = RequestBody.create("text/plain".toMediaTypeOrNull(), gender ?: "")
        map["email"] = RequestBody.create("text/plain".toMediaTypeOrNull(), email ?: "")
        map["referral_code"] = RequestBody.create("text/plain".toMediaTypeOrNull(), referralCode ?: "")
        map["address"] = RequestBody.create("text/plain".toMediaTypeOrNull(), address ?: "")
        map["national_id"] = RequestBody.create("text/plain".toMediaTypeOrNull(), nationalID ?: "")

        if (pathFrontImagee != null && pathFrontImagee.exists()) {
            val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), pathFrontImagee)
            map["official_id_front_photo\"; filename=\"official_id_front_photo.png\""] = body
        }
        if (pathBackImagee != null && pathBackImagee.exists()) {
            val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), pathBackImagee)
            map["official_id_back_photo\"; filename=\"official_id_back_photo.png\""] = body
        }
        if (pathAddressProofImage != null && pathAddressProofImage.exists()) {
            val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), pathAddressProofImage)
            map["photo_proof_address\"; filename=\"photo_proof_address.png\""] = body
        }
        if (pathSchoolIdImage != null && pathSchoolIdImage.exists()) {
            val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), pathSchoolIdImage)
            map["identification_school_or_work\"; filename=\"identification_school_or_work.png\""] = body
        }
        if (pathProfileImage != null && pathProfileImage.exists()) {
            val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), pathProfileImage)
            map["profile_pic\"; filename=\"profile_pic.png\""] = body
        }

        RestClient.recreate().get().addName(map).enqueue(object : Callback<ApiResponse<AppDetail>> {
            override fun onResponse(call: Call<ApiResponse<AppDetail>>?, response: Response<ApiResponse<AppDetail>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == SUCCESS_CODE) {
                        getView()?.onApiSuccess(response.body()?.result)
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