package com.trava.user.ui.menu.mygifts

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.gift.ResultItem
import com.trava.utilities.LocaleManager
import kotlinx.android.synthetic.main.mygift_adapter_item_layout.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MyGiftsAdapter(private val context: Context, private var giftListing: ArrayList<ResultItem>, private var type: String,private var onGiftClicked: OnGiftClicked) : RecyclerView.Adapter<MyGiftsAdapter.MyViewHolder>() {
    private val ITEM = 0
    private val LOADING = 1
    private var allItemsLoaded = false


    override fun getItemViewType(position: Int) =
            if (position >= giftListing.size) LOADING else ITEM

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): MyViewHolder {
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.mygift_adapter_item_layout, parent, false) as ConstraintLayout
        return MyViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return giftListing.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        when (giftListing[position].friend_order_status) {
            1 -> {
                holder.layout.btnAccept.visibility = View.GONE
                holder.layout.btnReject.visibility = View.GONE
                holder.layout.tvStatus.visibility = View.VISIBLE
                holder.layout.tvStatus.text = context.getString(R.string.accepted)
            }
            2 -> {
                holder.layout.btnAccept.visibility = View.GONE
                holder.layout.btnReject.visibility = View.GONE
                holder.layout.tvStatus.visibility = View.VISIBLE
                holder.layout.tvStatus.text = context.getString(R.string.rejected)
            }
            else -> {
                holder.layout.btnAccept.visibility = View.VISIBLE
                holder.layout.btnReject.visibility = View.VISIBLE
                holder.layout.tvStatus.visibility = View.GONE
                holder.layout.tvStatus.text = context.getString(R.string.pending)
            }
        }
        if(type == "SENT"){
            holder.layout.tvGiftText.text = "${context.resources.getString(R.string.you_sent_a_gift)} ${giftListing[position].friend_name} " +
                    "${context.getString(R.string.on_text)} ${giftListing[position].friend_phone_number}"
            holder.layout.btnAccept.visibility = View.GONE
            holder.layout.btnReject.visibility = View.GONE
        }else {
            holder.layout.tvGiftText.text = "${context.resources.getString(R.string.you_received_a_gift)} ${giftListing[position].Customer.name} " +
                    "${context.getString(R.string.on_text)} ${giftListing[position].friend_phone_number}"
            holder.layout.btnAccept.visibility = View.VISIBLE
            holder.layout.btnReject.visibility = View.VISIBLE
        }

        holder.layout.tvStatus.setTextColor(Color.parseColor(ConfigPOJO.settingsResponse?.key_value?.secondary_colour))
        holder.layout.btnAccept.setOnClickListener {
            onGiftClicked.onGiftSelected(position,"1")
        }

        holder.layout.btnReject.setOnClickListener {
            onGiftClicked.onGiftSelected(position,"2")
        }

        var loc = LocaleManager.getLanguage(context)
        val locale = Locale(loc)
        val format: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date: Date = format.parse(giftListing[position].order_timings)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", locale)
        holder.layout.tvDate.text = dateFormat.format(date)
    }

    inner class MyViewHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    fun setAllItemsLoaded(isAllItemsLoaded: Boolean) {
        allItemsLoaded = isAllItemsLoaded
    }
}

interface OnGiftClicked{
    fun onGiftSelected(position: Int,type:String)
}
