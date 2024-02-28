package com.buraq24.driver.ui.home.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trava.driver.webservices.models.WalletModel
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class WalletAdapter(private val listWalletHistory: ArrayList<WalletModel?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM = 0
    private val LOADING = 1
    private var allItemsLoaded = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_recharges, parent, false))
        } else {
            LoadViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loading_footer, parent, false))
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) listWalletHistory.size else listWalletHistory.size + 1

    override fun getItemViewType(position: Int) =
            if (position >= listWalletHistory.size) LOADING else ITEM

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> (holder as ViewHolder).bind(listWalletHistory.get(position))
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvRechargeAmt = itemView.findViewById<TextView>(R.id.tvAmtOne)
        var tvAmtToTakeRide = itemView.findViewById<TextView>(R.id.tvAmtTwo)
        var tvRechargeDate = itemView.findViewById<TextView>(R.id.tvRechargeDate)
        var tvDescription = itemView.findViewById<TextView>(R.id.tvDescription)
        init {
            itemView.setOnClickListener {

            }
        }

        fun bind(orderData: WalletModel?) {
            tvAmtToTakeRide.text = "${getFormattedPrice(orderData?.amount?.toDouble())} ${ConfigPOJO.currency}"
            tvRechargeAmt.text = "Total - "+"${getFormattedPrice(orderData?.updated_balance?.toDouble())} ${ConfigPOJO.currency}"
            tvRechargeDate.text = getFormattedTimeForBooking(orderData?.created_at ?: "")
            tvDescription.text = orderData?.comment ?: ""
        }

//        fun bind(orderData: WalletModel?) {
//            tvAmtToTakeRide.text = "${getFormattedPrice(orderData?.amount?.toDouble())} ${tvAmtToTakeRide.context.getString(R.string.currency)}"
//            tvRechargeAmt.text = "${getFormattedPrice(orderData?.updated_balance?.toDouble())} ${tvAmtToTakeRide.context.getString(R.string.currency)}"
//            tvRechargeDate.text = getFormattedTimeForBooking(orderData?.created_at ?: "")
//        }
    }

    inner class LoadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setAllItemsLoaded(isAllItemsLoaded: Boolean) {
        allItemsLoaded = isAllItemsLoaded
    }

    fun getFormattedTimeForBooking(dateCal: String): String {
        var cal = ""
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            val dateFormat2 = SimpleDateFormat("dd MMM, yyyy", Locale.US)
            dateFormat2.timeZone = TimeZone.getDefault()
            val date = dateFormat1.parse(dateCal)
            cal = dateFormat2.format(date)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }

    fun getFormattedPrice(value: Double?): String {
        value?.let {
            val nf = NumberFormat.getNumberInstance(Locale.ENGLISH)
            val formatter = nf as DecimalFormat
            formatter.applyPattern("#0.00")
            return formatter.format(value)
        }
        return value.toString()
    }
}