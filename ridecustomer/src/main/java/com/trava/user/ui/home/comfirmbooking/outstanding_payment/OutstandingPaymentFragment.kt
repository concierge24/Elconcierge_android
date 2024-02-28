package com.trava.user.ui.home.comfirmbooking.outstanding_payment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.trava.user.R
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.cards_model.UserCardData
import com.trava.user.webservices.models.order.OutstandingChargesModel
import com.trava.utilities.*
import kotlinx.android.synthetic.main.fragment_outstanding_fee.*
import kotlinx.android.synthetic.main.fragment_outstanding_fee.rootView
import kotlinx.android.synthetic.main.fragment_outstanding_fee.tvCreditCard

class OutstandingPaymentFragment : Fragment(),OutstandingPaymentContract.View {

    private var listener : OutstandingPaymentInterface? = null
    private var outStdPayData : OutstandingChargesModel? = null
    private var cardsList = java.util.ArrayList<UserCardData>()
    private var progressDialog : DialogIndeterminate? = null


    private var presenter = OutstandingPaymentPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_outstanding_fee,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        intialize()
        setListener()
    }

    private fun intialize() {
        progressDialog = DialogIndeterminate(context)
        outStdPayData = arguments?.getParcelable("data")
        cardsList = arguments?.getParcelableArrayList(Constants.CARDS_DATA)?:ArrayList()
        tvOutstanding.text = String.format("%s","${getString(R.string.pay)} ${ConfigPOJO.currency} ${""} ${outStdPayData?.cancellation_charges} ${getString(R.string.outPreviousTrip)}")
//        tvDate.text = getDateFromMilliseconds()

    }

    fun setInterfaceListener(listener : OutstandingPaymentInterface){
        this.listener = listener
    }

    private fun setListener() {
        ivCross.setOnClickListener {
            activity?.onBackPressed()
        }

        tvPayCash.setText("Pay through "+outStdPayData?.paymentType?.toLowerCase()+" to next driver")

        tvPayCash.setOnClickListener {
            listener?.getOutstandingPayment(outStdPayData?.paymentType?:""
                    ,outStdPayData?.cancellation_charges?:0.0
                    ,outStdPayData?.payment_id?:0)
            activity?.onBackPressed()


        }

        tvCreditCard.visibility = View.INVISIBLE
    }

    private fun hitOutStandingPaymentviaCard(userCardId : Int,amount : Double){
        if(CheckNetworkConnection.isOnline(context)){
            presenter.outStandingPaymentByCard(userCardId,amount)
        }else{
            CheckNetworkConnection.showNetworkError(rootView)
        }

    }

    override fun outStandingPaymentSuccess() {
        listener?.getOutstandingPayment(PaymentType.CARD,outStdPayData?.cancellation_charges?:0.0,outStdPayData?.payment_id?:0)
        activity?.onBackPressed()
    }

    override fun showLoader(isLoading: Boolean) {
        progressDialog?.show(isLoading)
    }

    override fun apiFailure() {
        rootView?.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(activity as Activity)
        } else {
            rootView.showSnack(error.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()

    }
}

interface OutstandingPaymentInterface {
    fun getOutstandingPayment(paymentType : String,cancellationCharges : Double, payment_id : Int)
}