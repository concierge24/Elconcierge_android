package com.trava.user.ui.home.submodels

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trava.user.R
import com.trava.user.databinding.ItemVehicleSubModelBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.homeapi.Product
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.COUPEN_TYPE
import com.trava.utilities.Constants
import kotlinx.android.synthetic.main.item_vehicle_sub_model.view.*

class SelectProductsAdapter constructor(private var products: ArrayList<Product?>?,
                                        private var prevSelectedPosition: Int,
                                        private var listener: InvoiceInterface) : RecyclerView.Adapter<SelectProductsAdapter.ViewHolder>() {

    private var totalAmount = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var otpBinding: ItemVehicleSubModelBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_vehicle_sub_model, parent, false)
        otpBinding.color = ConfigPOJO.Companion
        return ViewHolder(otpBinding.root)
    }

    override fun getItemCount(): Int {
        return products?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(products?.get(position))
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView
            ?: View(itemView?.context)) {

        var count = 1

        init {
            products?.get(prevSelectedPosition)?.isSelected = true
            itemView?.setOnClickListener {
                products?.get(prevSelectedPosition)?.isSelected = false
                products?.get(adapterPosition)?.isSelected = true
                prevSelectedPosition = adapterPosition
                notifyDataSetChanged()
            }



            itemView?.iv_info?.setOnClickListener {
                listener.OnInfoClick(products?.get(adapterPosition))
            }

            itemView?.iv_left?.setOnClickListener {
                if (count == 1) {
                    itemView.tvSeatingcounts.text = "1"
                } else {
                    count -= 1
                    itemView.tvSeatingcounts.text = "" + count
                }
                var final_char = getFinalCharge(products?.get(adapterPosition)!!, itemView?.context).times(count)
                itemView.tv_total_pool_amount.text = "$" + AppUtils.getFormattedDecimal(final_char)
                itemView.tv_total_passenger.text = "for $count passengers"
                products?.get(adapterPosition)?.selected_seating_capacity = count
                products?.get(adapterPosition)?.selected_seating_capacity = count
            }

            itemView?.iv_right?.setOnClickListener {
                if (count < products?.get(adapterPosition)?.seating_capacity ?: 0) {
                    count += 1
                    itemView.tvSeatingcounts.text = "" + count

                    var final_char = getFinalCharge(products?.get(adapterPosition)!!, itemView?.context).times(count)
                    itemView.tv_total_pool_amount.text = "$" + AppUtils.getFormattedDecimal(final_char)
                    itemView.tv_total_passenger.text = "for $count passengers"
                    products?.get(adapterPosition)?.selected_seating_capacity = count
                }
            }
        }

        fun onBind(product: Product?) = with(itemView) {
            tvVehicleName.text = product?.name

            Glide.with(ivVehicleImage.context).load(product?.image_url).into(ivVehicleImage)
            if ((context as HomeActivity).serviceRequestModel.bookingFlow == "1") {
                ll_seating_cap.visibility = View.GONE

                if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                    tvVehicleDescription.visibility = View.VISIBLE
                    tvVehicleDescription.text = product?.description
                }
            }
            tvCurrency.text = ConfigPOJO.currency
            tvSeatingCapcity.text = product?.seating_capacity.toString()
            if ((context as HomeActivity).serviceRequestModel.pkgData != null) {
                val totalPrice = AppUtils.getFormattedDecimal(getFinalChargePackage((context as HomeActivity).serviceRequestModel.pkgData, context))
                tvPriceRange.text = totalPrice
                if (product?.isSelected == true) {
                    mainLayout.background = StaticFunction.changeBorderTextColorService(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
                } else {
                    mainLayout.background = null
                }
            } else {
                if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE)//view show according to template selection
                {
                    tvPriceRange.text = AppUtils.getFormattedDecimal(getFinalCharge(product!!, context))

                    val amount = getFinalCharge(product!!, context).times(10).div(100)
                    val start_price = getFinalCharge(product, context).minus(amount)
                    val end_price = getFinalCharge(product, context).plus(amount)
                    tvEstPrice.text = context.resources.getString(R.string.est) + " : " + ConfigPOJO.currency + "" + String.format("%.2f %s %.2f", start_price, "-", end_price)
                } else {
                    val amount = getFinalCharge(product!!, context).times(10).div(100)
                    val start_price = getFinalCharge(product, context).minus(amount)
                    val end_price = getFinalCharge(product, context).plus(amount)
                    tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)
                    tvEstPrice.text = context.resources.getString(R.string.est) + " : " + ConfigPOJO.currency + "" + String.format("%.2f %s %.2f", start_price, "-", end_price)
                }

                tv_total_pool_amount.text = "$" + AppUtils.getFormattedDecimal(getFinalCharge(product!!, context))
                tv_total_passenger.text = "for " + product.selected_seating_capacity + " passengers"

                if (product?.isSelected == true) {
                    mainLayout.background = StaticFunction.changeBorderTextColorService(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
                } else {
                    rl_pool.visibility = View.GONE
                    mainLayout.background = null
                }
            }

            iv_info.setColorFilter(Color.parseColor(ConfigPOJO.secondary_color), PorterDuff.Mode.SRC_IN)
            iv_seating_image.setColorFilter(Color.parseColor(ConfigPOJO.secondary_color), PorterDuff.Mode.SRC_IN)
            if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE)//view show according to template selection
            {
                iv_info.visibility = View.VISIBLE
                iv_seating_image.visibility = View.GONE
                iv_seating.text = context.getString(R.string.seatCapacity)
            } else {
                iv_info.visibility = View.GONE
                iv_seating_image.visibility = View.GONE
                tvSeatingCapcity.visibility = View.GONE
                iv_seating.text = ""
            }

            if(resources.getString(R.string.app_name).toLowerCase() =="wasila"){
                ll_seating_cap.visibility  = View.GONE
            }

            if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                iv_info.visibility = View.VISIBLE
                llPrice.visibility = View.INVISIBLE
            }

            if (ConfigPOJO.is_asap == "true") {
                llEstimation.visibility = View.VISIBLE
                llPrice.visibility = View.VISIBLE
                if (product?.driversList == null) {
                    tvEstPrice.text = context.resources.getString(R.string.no_driver)
                    llPrice.visibility = View.INVISIBLE
                } else {
                    if (product?.driversList?.size ?: 0 > 0) {

                    } else {
                        tvEstPrice.text = context.resources.getString(R.string.no_driver)
                        llPrice.visibility = View.INVISIBLE
                    }
                }
            }


            if(ConfigPOJO.is_merchant =="true"){
                tvEstPrice.text = ""
            }

            if(Constants.SECRET_DB_KEY =="a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60" || Constants.SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5"){
                ll_seating_cap.visibility = View.GONE
            }
        }
    }

    fun getSelectedBrandProductId(): Int {
        return products?.get(prevSelectedPosition)?.category_brand_product_id ?: 0
    }

    fun getSelectedBrandProductLoadPrice(): Int {
        return products?.get(prevSelectedPosition)?.load_unload_charges ?: 0
    }

    fun getSelectedBrandProductName(): String {
        return products?.get(prevSelectedPosition)?.name ?: ""
    }

    fun getSelectedProduct(): Product? {
        return products?.get(prevSelectedPosition)
    }

    fun getFinalCharge(product: Product, context: Context): Double {
        var distance_r = 0.0
        var time_r = 0.0
        var buraqfare_r = 0.0
        var actualAmount = 0.0
        var scheduleCharge = 0.0
        var final_amount = 0.0
        var totalPrice=0.0
        var exactAmount=0.0
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

        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            var airport_charge = 0.0
            if ((context as HomeActivity).serviceRequestModel?.airportChargesPickup != "") {
                airport_charge = (context as HomeActivity).serviceRequestModel?.airportChargesPickup?.toDouble() ?: 0.0
            }
            if ((context as HomeActivity).serviceRequestModel?.airportChargesDrop != "") {
                airport_charge += (context as HomeActivity).serviceRequestModel?.airportChargesDrop?.toDouble() ?: 0.0
            }

            var scheduleCharge = 0.0
            if (ConfigPOJO.settingsResponse?.key_value?.schedule_charge == "true") {
                scheduleCharge =product.schedule_charge.toDouble()
            }
            totalPrice = distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f).plus(airport_charge).plus(scheduleCharge)

            var afterPromoAmount = 0.0
            if ((context as HomeActivity).serviceRequestModel?.promoData!=null)
            {
                when ((context as HomeActivity).serviceRequestModel?.promoData?.couponType) {
                    COUPEN_TYPE.VALUE -> {
                        afterPromoAmount = totalPrice?.minus((context as HomeActivity).serviceRequestModel?.promoData?.amountValue?.toDouble() ?: 0.0) ?: 0.0
                    }

                    COUPEN_TYPE.PERCENTAGE -> {
                        var disvalue=totalPrice?.times((context as HomeActivity).serviceRequestModel?.promoData?.amountValue?.toDouble()?: 0.0)?.div(100) ?: 0.0

                        if (disvalue<(context as HomeActivity).serviceRequestModel?.promoData?.maximum_discount?.toDouble()?: 0.0)
                        {
                            afterPromoAmount=totalPrice?.minus(disvalue)
                        }
                        else
                        {
                            afterPromoAmount=totalPrice?.minus((context as HomeActivity).serviceRequestModel?.promoData?.maximum_discount?.toDouble()?: 0.0)
                        }
                    }
                }
            }

            if (afterPromoAmount!=0.0)
            {
                totalPrice=afterPromoAmount
            }
            buraqfare_r = totalPrice.times(product.buraq_percentage).div(100)
            actualAmount = totalPrice.plus(buraqfare_r)
            exactAmount=actualAmount
        }
        else
        {
            totalPrice = distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f)
            if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
            {
                buraqfare_r = totalPrice.times(product.buraq_percentage).div(100)
                actualAmount = totalPrice.plus(buraqfare_r)
                if (product.actual_value!! > actualAmount.toFloat()){
                    actualAmount =  product.actual_value!!.toDouble()
                    if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                    {
                        buraqfare_r = actualAmount.times(product?.buraq_percentage ?: 0F).div(100)
                    }

                    actualAmount =  actualAmount.plus(buraqfare_r)
                }
                var sur = product.surchargeValue.toDouble()

                final_amount = actualAmount.plus(sur)
                if (ConfigPOJO.settingsResponse?.key_value?.schedule_charge == "true") {
                    scheduleCharge = if (product.schedule_charge_type == "value") {
                        product.schedule_charge.toDouble()
                    } else {
                        final_amount.times(product.schedule_charge).div(100)
                    }
                }
                exactAmount = final_amount.plus(scheduleCharge)
                if (context.getString(R.string.app_name) == "Wasila") {
                    if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                        exactAmount = exactAmount.times(ConfigPOJO.OMAN_CURRENCY)
                    }
                }
            }
        }
        (context as HomeActivity).serviceRequestModel.final_charge=exactAmount
        return (exactAmount)
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
}

interface InvoiceInterface {
    fun OnInfoClick(data: Product?)
}