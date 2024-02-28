package com.trava.user.ui.home.rating


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.services.ServicesFragment
import com.trava.user.ui.home.vehicles.SelectVehicleTypeFragment
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.constants.ORDER
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import kotlinx.android.synthetic.main.fragment_rating.*


class RatingFragment : Fragment(), RatingContract.View {

    private var prevSelectedRating: TextView? = null

    private var rating = 0

    private var presenter = RatingPresenter()
    private var onRateDone: OnRateDone? = null
    private var order: Order? = null

    private var dialogIndeterminate: DialogIndeterminate? = null

    private val ratingUnselectedDrawables = listOf(R.drawable.ic_angry_smile_gray, R.drawable.ic_2off, R.drawable.ic_3off, R.drawable.ic_4off, R.drawable.ic_5off)

    private val ratingselectedDrawables = listOf(R.drawable.ic_angry_smile, R.drawable.ic_2, R.drawable.ic_3, R.drawable.ic_4, R.drawable.ic_5)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(activity)

        if (SECRET_DB_KEY == "a072d31bb28d08fc2d8de7f21f1bd38e"|| arguments?.containsKey("detail") == true) {
            tvSkipNow.visibility = View.GONE
        }

        tvSubmit.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        tvSkipNow.setTextColor(Color.parseColor(ConfigPOJO.primary_color))
        setData()
        setListeners()
    }

    private fun setData() {
        order = Gson().fromJson(arguments?.getString(ORDER), Order::class.java)
        ivDriverImage.setRoundProfileUrl(BASE_IMAGE_URL + order?.driver?.profile_pic)
        tvDriverName.text = order?.driver?.name
    }

    private fun setListeners() {
        tvRate1.setOnClickListener(ratingsClickListener)
        tvRate2.setOnClickListener(ratingsClickListener)
        tvRate3.setOnClickListener(ratingsClickListener)
        tvRate4.setOnClickListener(ratingsClickListener)
        tvRate5.setOnClickListener(ratingsClickListener)
        tvSubmit.setOnClickListener { checkValidations() }
        tvSkipNow.setOnClickListener {
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            context?.sendBroadcast(Intent("update"))
            val fragment = ServicesFragment()
            val fragment1 = SelectVehicleTypeFragment()

            val bundle = Bundle()
            bundle.putString("via", "skipNow")
            fragment.arguments = bundle
            fragment1.arguments = bundle
            (activity as? HomeActivity)?.serviceRequestModel = ServiceRequestModel()
            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                activity!!.recreate()
//                (activity as? HomeActivity)?.selectVehicleTypeFragmentManager = fragment1
//                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment1)?.addToBackStack("backstack")?.commit()

            } else {
                (activity as? HomeActivity)?.servicesFragment = fragment
                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()

            }
        }
    }

    private val ratingsClickListener = View.OnClickListener {
        prevSelectedRating?.setCompoundDrawablesWithIntrinsicBounds(0, ratingUnselectedDrawables[prevSelectedRating?.tag.toString().toInt() - 1], 0, 0)
        rating = it.tag.toString().toInt()
        (it as TextView).setCompoundDrawablesWithIntrinsicBounds(0, ratingselectedDrawables[rating - 1], 0, 0)
        prevSelectedRating = it
        tvRate1.isSelected = rating == 1
        tvRate2.isSelected = rating == 2
        tvRate3.isSelected = rating == 3
        tvRate4.isSelected = rating == 4
        tvRate5.isSelected = rating == 5
    }

    private fun checkValidations() {
        presenter.checkValidations(rating, tvAddComment.text.toString().trim())
    }

    override fun onValidationsResult(isSuccess: Boolean?, message: Int?) {
        if (isSuccess == true) {
            rateApiCall()
        } else {
            rootView.showSnack(message ?: 0)
        }
    }

    private fun rateApiCall() {
        if (CheckNetworkConnection.isOnline(activity)) {
            presenter.rateOrder(rating, tvAddComment.text.toString().trim(), order?.order_id)
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }


    override fun onApiSuccess() {
        if (order?.payment?.payment_type == PaymentType.E_TOKEN) {
            activity?.finish()
        } else {
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                val fragment1 = SelectVehicleTypeFragment()
                (activity as? HomeActivity)?.serviceRequestModel = ServiceRequestModel()
                (activity as? HomeActivity)?.selectVehicleTypeFragmentManager = fragment1
//                fragmentManager?.beginTransaction()?.add(R.id.container, fragment1)?.addToBackStack("backstack")?.commit()
                activity!!.recreate();
            } else {
                if (arguments?.containsKey("detail") == true) {
                    onRateDone?.onRated()
                    activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
                }
                else
                {
                    val fragment = ServicesFragment()
                    (activity as? HomeActivity)?.serviceRequestModel = ServiceRequestModel()
                    (activity as? HomeActivity)?.servicesFragment = fragment
                    fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                }
            }
        }
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        rootView?.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(activity)
        } else {
            rootView?.showSnack(error ?: "")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onRateDone = context as? OnRateDone
    }

    interface OnRateDone {
        fun onRated()
    }

}
