package com.trava.user.ui.menu.noticeBoard

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.trava.user.R
import com.trava.user.webservices.models.noticeBoard.NotificationData
import com.trava.utilities.DateUtils
import kotlinx.android.synthetic.main.item_notifications.view.*

class  NotificationsAdapter(private val response: List<NotificationData>?) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_notifications,parent,
                    false))

    override fun getItemCount(): Int = response?.size?:0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(response?.get(position))

    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var notiData: NotificationData?=null
        init {
            itemView.setOnClickListener {
//                val phone = contactData?.phoneNumber.toString()
//                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
//                it.context?.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
        }
        fun bind(data: NotificationData?) = with(itemView) {
            notiData = data
            tvNotifications.text = data?.message
            tvTime.text = DateUtils.getFormattedTimeForBooking2(data?.createdAt?:"")
            //Glide.with(itemView.context).load(data?.image_url).into(itemView.ivContactImage)
        }
    }

}