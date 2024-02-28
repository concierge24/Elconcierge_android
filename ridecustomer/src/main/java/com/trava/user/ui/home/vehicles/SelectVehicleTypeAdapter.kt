package com.trava.user.ui.home.vehicles

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.Category
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.user.webservices.models.homeapi.Product
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.Constants
import com.trava.utilities.Constants.CORSA
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.hide
import com.trava.utilities.invisible
import com.trava.utilities.show
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_CATEGORY
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import kotlinx.android.synthetic.main.item_vehicle_type.view.*
import org.json.JSONArray
import org.json.JSONObject


class SelectVehicleTypeAdapter(val categoriesList: ArrayList<Category>?, private var prevSelectedPosition: Int, private var isMover: Boolean
                               , val onInfoCallBack: OnInfocallBack, val onCategorySelected: OnCategorySelected) : RecyclerView.Adapter<SelectVehicleTypeAdapter.ViewHolder>() {


    open interface OnInfocallBack {
        //        fun onInfoClick(pos: Int)
        fun onInfoClick(title: String)
    }

    open interface OnCategorySelected {
        fun onCategorySelected(position: Int)
    }

    companion object {
        var onInfoCallBack: OnInfocallBack? = null
    }

    private var serviceReqModel: ServiceRequestModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        serviceReqModel = (parent.context as HomeActivity).serviceRequestModel
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle_type, parent, false))
    }

    override fun getItemCount(): Int {
        return categoriesList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(categoriesList?.get(position))
    }


    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView
            ?: View(itemView?.context)) {
        init {
            categoriesList?.get(prevSelectedPosition)?.isSelected = true
            itemView?.setOnClickListener {
                categoriesList?.get(prevSelectedPosition)?.isSelected = false
                categoriesList?.get(adapterPosition)?.isSelected = true
                prevSelectedPosition = adapterPosition
//                    itemSelectedListener?.onItemSelected(adapterPosition)
                onCategorySelected.onCategorySelected(position)
                notifyDataSetChanged()
            }
        }

        fun onBind(category: Category?) = with(itemView) {
            tvVehicleName.text = category?.name
            if (ConfigPOJO.TEMPLATE_CODE == CORSA) {
                Glide.with(ivVehicleImage.context).load(BASE_IMAGE_CATEGORY + category?.image).into(ivVehicleImage)
            } else {
                Glide.with(ivVehicleImage.context).load(BASE_IMAGE_URL + category?.image).into(ivVehicleImage)
            }

            if (isMover) {
                info_iv.visibility = VISIBLE
            }

            info_iv.setOnClickListener {
                onInfoCallBack.onInfoClick(category?.name.toString())
            }

            if (category?.isSelected == true) {
                tvVehicleName.setTextColor(Color.parseColor(ConfigPOJO.secondary_color))
                viewSelector.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.black_color, GradientDrawable.OVAL)
            } else {
                viewSelector.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.white_color, GradientDrawable.OVAL)
                tvVehicleName.setTextColor(Color.parseColor(ConfigPOJO.black_color))
            }

            if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                tvEstimatePrice.hide()
            } else {
                if (category?.driverList?.isNotEmpty() == true) {
                    if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                        tvTime.visibility = View.INVISIBLE
                        if (category?.products?.isNotEmpty() == true) {
                            val totalPrice = getFinalCharge(category.products[0], itemView?.context, category)
                            tvEstimatePrice.text = String.format("%s", "${tvEstimatePrice.context.getString(R.string.est)} ${ConfigPOJO.currency} ${AppUtils.getFormattedDecimal(totalPrice)}")
                        }
                    } else {
                        if (category.products?.isNotEmpty() == true) {
                            var totalPrice=0.0
                            if ((context as HomeActivity).serviceRequestModel.pkgData != null) {
                                totalPrice = getFinalChargePackage((context as HomeActivity).serviceRequestModel.pkgData, itemView?.context)
                            }
                            else
                            {
                                totalPrice = getFinalCharge(category.products[0], itemView?.context, category)
                            }


                            tvEstimatePrice.text = String.format("%s", "${tvEstimatePrice.context.getString(R.string.est)} ${ConfigPOJO.currency} ${AppUtils.getFormattedDecimal(totalPrice)}")
                            tvEstimatePricenew.text = String.format("%s", "${tvEstimatePrice.context.getString(R.string.est)} ${ConfigPOJO.currency} ${AppUtils.getFormattedDecimal(totalPrice)}")

                            var distance = driverDistance(category.driverList[0].latitude?.toFloat()
                                    ?: 0f, category.driverList[0].longitude?.toFloat() ?: 0f,
                                    (context as HomeActivity).serviceRequestModel?.dropoff_latitude?.toFloat()
                                            ?: 0f, (context as HomeActivity).serviceRequestModel?.dropoff_longitude?.toFloat()
                                    ?: 0f)
                            var timeInMin = ((distance / 60f))
                            var timeInSec = timeInMin * 60

                            tvTime.text = convertIntoHrs(timeInSec)

                            Log.e("asasassASA", distance.toString() + "SDsdsdsd" + "nnn" + category.estimatedTime + "------" + timeInMin)
                        }
                    }
                } else {
                    if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                        tvTime.visibility = View.INVISIBLE
                        if (category?.products?.isNotEmpty() == true) {
                            val totalPrice = getFinalCharge(category.products[0], itemView?.context, category)
                            tvEstimatePrice.text = String.format("%s", "${tvEstimatePrice.context.getString(R.string.est)} ${ConfigPOJO.currency} ${AppUtils.getFormattedDecimal(totalPrice)}")
                        }
                    } else {
                        if (SECRET_DB_KEY == "acf1dc2839389f07dbd1e2731874626e" ||
                                SECRET_DB_KEY == "a8fbdf721d90f8213181b1b7611a8ba0") {
                            tvEstimatePrice.text = ""
                            tvEstimatePricenew.text = ""
                            tvTime.text = ""
                        }
                        else
                        {
                            tvEstimatePrice.text = context.resources.getString(R.string.no_driver)
                            tvEstimatePricenew.text = context.resources.getString(R.string.no_driver)
                            tvTime.text = context.resources.getString(R.string.no_driver)
                        }
                    }
                }
            }

            if (SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7" ||
                    SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5") {
                tvTime.visibility = View.VISIBLE
                tvEstimatePricenew.visibility = View.VISIBLE
                tvEstimatePrice.visibility = View.INVISIBLE
            } else {
                tvEstimatePrice.invisible()
                tvTime.visibility = View.GONE
            }

            if (ConfigPOJO.is_asap == "true" || Constants.SECRET_DB_KEY == "6ae915e1698d88c4e7dda839852fa5fd") {
                tvEstimatePrice.show()
                tvTime.hide()
                tvEstimatePricenew.hide()
            }

            if(ConfigPOJO.is_merchant == "true"){
                tvEstimatePrice.invisible()
                tvTime.hide()
            }

            for (item in category?.products!!) {
                item.buraq_percentage = category.buraq_percentage ?: 0.0f
            }
        }
    }

    fun getFinalChargePackage(product: PackagesItem?, context: Context): Double {
        var distance_r = 0.0
        var time_r = 0.0

        distance_r = if ((context as HomeActivity).serviceRequestModel.order_distance?.toDouble() ?: 0.0 >
                product?.packageData?.distanceKms ?: 0.0) {
            val distance = (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()
                    ?.minus(product?.packageData?.distanceKms ?: 0.0)
            product?.packageData?.packagePricings?.get(0)?.distancePriceFixed
                    ?.plus(distance?.times(product.packageData.packagePricings.get(0).pricePerKm
                            ?: 0.0)?:0.0)?:0.0
        } else {
            product?.packageData?.packagePricings?.get(0)?.distancePriceFixed ?: 0.0
        }

        time_r = if (product?.packageData?.packageType == "1") {
            if ((context as HomeActivity).serviceRequestModel.eta?.toDouble() ?: 0.0 > 60.0) {
                val timePrice = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.minus(60.0)
                product?.packageData?.packagePricings?.get(0)?.timeFixedPrice
                        ?.plus(timePrice?.times(product.packageData.packagePricings.get(0).pricePerMin
                                ?: 0.0)?:0.0)?:0.0
            }
            else
            {
                product.packageData.packagePricings.get(0).timeFixedPrice ?: 0.0
            }
        } else {
            product?.packageData?.packagePricings?.get(0)?.timeFixedPrice ?: 0.0
        }

        val totalPrice = distance_r.plus(time_r)
        return totalPrice
    }

    fun getFinalCharge(product: Product, context: Context, category: Category?): Double {
        var distance_r = 0.0
        var time_r = 0.0
        var buraqfare_r = 0.0
        var actualAmount = 0.0
        var scheduleCharge = 0.0
        var final_amount = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.is_distance == "true")//distance required
        {
            distance_r = (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()?.times(product.price_per_distance
                    ?: 0.0f) ?: 0.0
        }
        if (ConfigPOJO.settingsResponse?.key_value?.is_time == "true")//Time required
        {
            time_r = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.times(product.price_per_hr
                    ?: 0.0f) ?: 0.0
        }

        val totalPrice = distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f)
        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
        {
            buraqfare_r = totalPrice.times(category?.buraq_percentage ?: 0F).div(100)
        }

        actualAmount = totalPrice.plus(buraqfare_r)
        var sur = product.surchargeValue.toDouble()

        if (product.actual_value!! > actualAmount.toFloat()){
            actualAmount =  product.actual_value!!.toDouble()
            if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
            {
                buraqfare_r = actualAmount.times(category?.buraq_percentage ?: 0F).div(100)
            }

           actualAmount =  actualAmount.plus(buraqfare_r)
        }

        final_amount = actualAmount.plus(sur)
        if (ConfigPOJO.settingsResponse?.key_value?.schedule_charge == "true") {
            scheduleCharge = if (product.schedule_charge_type == "value") {
                product.schedule_charge.toDouble()
            } else {
                final_amount.times(product.schedule_charge).div(100)
            }
        }
        return (final_amount.plus(scheduleCharge))
    }

    fun getSelectedPosition(): Int {
        return prevSelectedPosition
    }

    fun getSelectedCategoryBrandId(): Int {
        return categoriesList?.get(prevSelectedPosition)?.category_brand_id ?: 0
    }

    fun getSelectedCategoryHelperPercentageId(): Float {
        return categoriesList?.get(prevSelectedPosition)?.helper_percentage ?: 0F
    }

    fun getSelectedCategoryBrandName(): String {
        return categoriesList?.get(prevSelectedPosition)?.name ?: ""
    }

    fun getSelectedCategoryBrandImage(): String {
        return categoriesList?.get(prevSelectedPosition)?.image_url ?: ""
    }

    fun getSelectedCategoryProducts(): List<Product>? {
        return categoriesList?.get(prevSelectedPosition)?.products
    }

    fun getSelectedCategoryDrivers(): List<HomeDriver>? {
        return categoriesList?.get(prevSelectedPosition)?.driverList
    }

    fun driverDistance(lat_a: Float, lng_a: Float, lat_b: Float, lng_b: Float): Float {
        val earthRadius = 3958.75
        val latDiff = Math.toRadians(lat_b - lat_a.toDouble())
        val lngDiff = Math.toRadians(lng_b - lng_a.toDouble())
        val a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a.toDouble())) * Math.cos(Math.toRadians(lat_b.toDouble())) *
                Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = earthRadius * c
        val meterConversion = 1609
        return (distance * meterConversion.toFloat()).toFloat()
    }

    private fun convertIntoHrs(seconds: Float): String {
        val p1: Int = seconds.toInt() % 60
        var p2: Int = seconds.toInt() / 60
        val p3 = p2 % 60

        p2 = p2 / 60
        var timeData = ""
        if (p2 < 1) {
            timeData = p3.toString() + ":" + p1
        } else {
            timeData = p2.toString() + ":" + p3 + ":" + p1
        }
        return timeData
    }
}