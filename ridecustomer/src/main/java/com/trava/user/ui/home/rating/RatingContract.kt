package com.trava.user.ui.home.rating

import androidx.annotation.StringRes
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import java.math.BigInteger

class RatingContract{

    interface View: BaseView{
        fun onApiSuccess()
        fun onValidationsResult(isSuccess: Boolean?, @StringRes message: Int?)
    }

    interface Presenter: BasePresenter<View>{
        fun rateOrder(rating: Int?,comments: String?, orderId: BigInteger?)
        fun checkValidations(rating: Int?,comments: String?)
    }
}