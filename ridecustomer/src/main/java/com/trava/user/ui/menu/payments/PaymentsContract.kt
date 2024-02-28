package com.trava.user.ui.menu.payments

import com.trava.user.webservices.models.paymentdetails.PaymentDetail
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class PaymentsContract{

    interface View: BaseView{
        fun onPaymentDetailsApiSuccess(response: PaymentDetail?)
    }

    interface Presenter: BasePresenter<View>{
        fun paymentDetail(categoryId: String?, categoryProductId: String?)
    }
}