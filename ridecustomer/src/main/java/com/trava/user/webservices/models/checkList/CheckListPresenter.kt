package com.trava.user.webservices.models.checkList

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.CheckListModel
import com.trava.user.webservices.models.CheckListResponseModel
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckListPresenter  : BasePresenterImpl<CheckListcontractor.View>(), CheckListcontractor.Presenter {
    override fun saveCheckList(checkListModel: ArrayList<CheckListModel>) {
        getView()?.showLoader(true)

        var checkListModel : RequestBody? = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), checkListModel.toString())


        RestClient.get().setCheckListApi(checkListModel!!).enqueue(object : Callback<ApiResponse<CheckListResponseModel>> {

            override fun onResponse(call: Call<ApiResponse<CheckListResponseModel>>?, response: Response<ApiResponse<CheckListResponseModel>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
//                        getView()?.onApiSuccess(response.body()?.result)
                    } else {
                        if(response.body()?.statusCode == 301){
//                            getView()?.onOutstandingCharges(response.body()?.result)
                        }else {
                            getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                        }
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<CheckListResponseModel>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}


