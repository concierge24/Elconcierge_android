package com.trava.user.ui.menu.bookings

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.trava.user.R
import com.trava.user.utils.AppUtils
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.google.gson.Gson
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.utilities.Constants.SECRET_DB_KEY
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_booking.view.*
import java.util.*

class BookingAdapter(private val listBookings: ArrayList<Order?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM = 0

    private val LOADING = 1

    private var allItemsLoaded = false

    override fun getItemViewType(position: Int) =
            if (position >= listBookings.size) LOADING else ITEM


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false))
        } else {
            LoadViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loading_footer, parent, false))
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) listBookings.size else listBookings.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> (holder as ViewHolder).bind(listBookings[position])
        }
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView
            ?: View(itemView?.context)) {
        private var orderData: Order? = null

        init {
            itemView?.setOnClickListener {
                val intent = Intent(itemView.context, BookingDetailsActivity::class.java)
                it.context?.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("data", Gson().toJson(orderData)))
            }
        }

        fun bind(orderData: Order?) {
            this.orderData = orderData
            with(itemView) {
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transforms(CenterCrop(), RoundedCornersTransformation(8,
                        0, RoundedCornersTransformation.CornerType.TOP))
                var lat = orderData?.pickup_latitude ?: 0.0
                var lng = orderData?.pickup_longitude ?: 0.0
                /*if (orderData?.dropoff_latitude?.toInt() != 0 && orderData?.dropoff_latitude?.toInt() != 0) {
                    lat = orderData?.dropoff_latitude ?: 0.0
                    lng = orderData?.dropoff_longitude ?: 0.0
                }*/
                var drpLat = orderData?.dropoff_latitude ?: 0.0
                var drpLng  = orderData?.dropoff_longitude ?: 0.0

                var list: ArrayList<LatLng>? = null
                list = MapUtils.decodePoly(Uri.decode(orderData?.exact_path))
                if (orderData?.exact_path!="" && orderData?.exact_path!=null)
                {
                    Glide.with(this).applyDefaultRequestOptions(requestOptions).load(MapUtils.setStaticPolyLineOnMapPath(orderData?.exact_path ?: ""
                            ,ConfigPOJO.SECRET_API_KEY,lat, lng, list[list.size-1].latitude, list[list.size-1].longitude)).into(ivMap)
                }
                else
                {
                    Glide.with(this).applyDefaultRequestOptions(requestOptions).load(MapUtils.setStaticPolyLineOnMapPath(orderData?.exact_path ?: ""
                            ,ConfigPOJO.SECRET_API_KEY,lat, lng, drpLat, drpLng)).into(ivMap)
                }
//                Glide.with(context as Activity).applyDefaultRequestOptions(requestOptions).load(MapUtils.getStaticMapWithMarker(context, ConfigPOJO.SECRET_API_KEY, lat, lng)).into(ivMap)
                /*Glide.with(context).applyDefaultRequestOptions(requestOptions).load(MapUtils.getStaticMapWithPolyLinePath(orderData?.exact_path ?: ""
                        ,ConfigPOJO.SECRET_API_KEY,lat, lng, drpLat, drpLng)).into(ivMap)*/

                tvDateTime.text = DateUtils.getFormattedTimeForBooking(orderData?.order_timings
                        ?: "")
                val paymentTypeString = context?.getString(AppUtils.getPaymentStringId(orderData?.payment?.payment_type
                        ?: ""))
                if (orderData?.payment?.payment_type == PaymentType.E_TOKEN) {
                    tvPaymentTypeAmount.text = "$paymentTypeString · ${orderData.payment?.product_quantity}"
                } else {
                    var exactAmount=orderData?.payment?.final_charge?.toDouble()
                    if (SECRET_DB_KEY == "c18ae90d6f5d9cf1e350526f9e9e6e249f75fe21e5deec6ae0853e3fd5a59ee1") {
                        if (ConfigPOJO.OMAN_PHONECODE=="+968")
                        {
                            exactAmount = exactAmount?.times(ConfigPOJO.OMAN_CURRENCY)
                        }
                    }
                    tvPaymentTypeAmount.text = "$paymentTypeString · ${String.format(Locale.US, "%s %.2f", ConfigPOJO.currency, exactAmount?.toFloat())}"
                }

                tvPackgeStatus.visibility = if (orderData?.booking_type.equals("Package", true)) View.VISIBLE else View.GONE

                tvBookingId.text = "Id:${orderData?.order_token}"
                tvBookingStatus.setTextColor(Color.parseColor(ConfigPOJO.primary_color))
                tvBookingStatus.text = when (orderData?.order_status) {
                    OrderStatus.SERVICE_COMPLETE, OrderStatus.CUSTOMER_CONFIRM_ETOKEN -> {
                        if (orderData.booking_type == BOOKING_TYPE.CARPOOL)
                            context.getString(R.string.car_pool_completed)
                        else
                            context.getString(R.string.completed)
                    }
                    OrderStatus.SERVICE_HALFWAY_STOP -> {
                        context.getString(R.string.half_way)
                    }
                    OrderStatus.SCHEDULED, OrderStatus.DRIVER_PENDING, OrderStatus.DRIVER_APPROVED -> {
                        context.getString(R.string.Scheduled)
                    }
                    OrderStatus.CUSTOMER_CONFIRMATION_PENDING_ETOKEN, OrderStatus.ONGOING -> {
                        context.getString(R.string.ongoing)
                    }
                    OrderStatus.CONFIRMED -> {
                        context.getString(R.string.confirmed)
                    }
                    OrderStatus.UNASSIGNED -> {
                        "Unassigned"
                    }
                    OrderStatus.PENDING -> {
                        "Pending"
                    }
                    OrderStatus.REACHED -> {
                        context.getString(R.string.reached)
                    }
                    OrderStatus.C_TIMEOUT_ETOKEN -> {
                        context.getString(R.string.timeout)
                    }
                    else -> {
                        tvBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.pink_gradient_2))

                        context.getString(R.string.cancelled)
                    }
                }

                rr_layout.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            }
        }
    }

    inner class LoadViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView
            ?: View(itemView?.context))

    fun setAllItemsLoaded(isAllItemsLoaded: Boolean) {
        allItemsLoaded = isAllItemsLoaded
    }
}