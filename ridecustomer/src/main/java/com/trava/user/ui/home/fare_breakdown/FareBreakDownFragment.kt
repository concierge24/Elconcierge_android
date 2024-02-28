package com.trava.user.ui.home.fare_breakdown

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.trava.user.R
import com.trava.user.databinding.FragmentFareBreakdownBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.Product
import com.trava.utilities.Constants.SECRET_DB_KEY
import kotlinx.android.synthetic.main.fragment_fare_breakdown.*
import java.util.*

class FareBreakDownFragment : Fragment() {

    private var serviceRequest: ServiceRequestModel? = null
    lateinit var fareBreakdownBinding: FragmentFareBreakdownBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fareBreakdownBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_fare_breakdown, container, false)
        fareBreakdownBinding.color = ConfigPOJO.Companion
        val view = fareBreakdownBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intialize()
        setListener()
    }

    private fun intialize() {
        serviceRequest = (activity as HomeActivity).serviceRequestModel
        if (serviceRequest?.pkgData != null) {
            textView30.visibility=View.INVISIBLE
            textView31.visibility=View.INVISIBLE
            tvPrice.visibility=View.INVISIBLE
            tvTax.visibility=View.INVISIBLE
        }
        if (arguments != null) {
            val product = arguments?.getParcelable("productItem")?:"" as Product
            if (SECRET_DB_KEY == "a072d31bb28d08fc2d8de7f21f1bd38e") {
                if (product.pool=="true")
                {
                    tvPrice.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.pool_alpha_price?.toDouble()
                            ?: 0.0)}")
                    tvTax.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.actual_value?.toDouble()
                            ?: 0.0)}")
                    tvTotal.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.pool_price_per_distance?.toDouble()
                            ?: 0.0)}")
                    tvGst.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.pool_price_per_hr?.toDouble()?:0.0)}")

                }
                else{
                    tvPrice.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.alpha_price?.toDouble()
                            ?: 0.0)}")
                    tvTax.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.actual_value?.toDouble()
                            ?: 0.0)}")
                    tvTotal.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.price_per_distance?.toDouble()
                            ?: 0.0)}")
                    tvGst.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.price_per_hr.toDouble())}")

                }

            }
            else
            {
                tvPrice.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.alpha_price?.toDouble()
                        ?: 0.0)}")
                tvTax.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.actual_value?.toDouble()
                        ?: 0.0)}")
                tvTotal.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.price_per_distance?.toDouble()
                        ?: 0.0)}")
                tvGst.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(product.price_per_hr.toDouble())}")

            }

            tvNext.text=product.description
        }
    }

    private fun getFormattedDecimal(num: Double): String? {
        return String.format(Locale.US, "%.2f", num)
    }

    private fun setListener() {
        ivCross.setOnClickListener { activity?.onBackPressed() }
    }
}