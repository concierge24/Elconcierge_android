package com.trava.user.ui.home.rating

import com.trava.user.R
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger

class RatingPresenter : BasePresenterImpl<RatingContract.View>(), RatingContract.Presenter {

    override fun checkValidations(rating: Int?, comments: String?) {
        if (rating == 0){
            getView()?.onValidationsResult(false, R.string.rating_validation_msg)
        } else{
            getView()?.onValidationsResult(true, R.string.validations_success)
        }
    }


    override fun rateOrder(rating: Int?, comments: String?, orderId: BigInteger?) {
        getView()?.showLoader(true)
        RestClient.get().rateService(rating, orderId, comments).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>?, response: Response<Any>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    getView()?.onApiSuccess()
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<Any>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })

    }

}