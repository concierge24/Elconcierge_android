package com.trava.user.ui.home.chatModule

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils.isToday
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.trava.user.ui.home.chatModule.chatMessage.ChatActivity
import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.user.R
import com.trava.utilities.*
import com.trava.utilities.Utils.convertDateTimeInMillis
import com.trava.utilities.Utils.existsInWeek
import com.trava.utilities.Utils.isYesterday
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.webservices.models.AppDetail
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import kotlinx.android.synthetic.main.item_chat_list.view.*
import java.util.*
import kotlin.collections.ArrayList


class ChatListAdapter(private var context: Context,
                      private val chatListing: ArrayList<ChatMessageListing>?,
                      private val chatsFragment: ChatUserListActivity) : androidx.recyclerview.widget.RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false))
    }

    override fun getItemCount(): Int {
        return chatListing?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatListing?.get(position))
    }

    inner class ViewHolder(itemView: View?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView!!) {

        init {
            itemView?.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(RECEIVER_ID, chatListing?.get(adapterPosition)?.oppositionId)
                intent.putExtra(USER_NAME, chatListing?.get(adapterPosition)?.name)
                intent.putExtra(PROFILE_PIC_URL, BASE_IMAGE_URL+chatListing?.get(adapterPosition)?.profile_pic)
                intent.putExtra(USER_DETAIL_ID,chatListing?.get(adapterPosition)?.user_detail_id.toString())
                intent.putExtra(CHAT_HISTORY,true)
                intent.putExtra(ORDER_ID,chatListing?.get(adapterPosition)?.order_id)
                chatsFragment.startActivity(intent)
                notifyDataSetChanged()
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(chat: ChatMessageListing?) = with(itemView) {

            var loc = LocaleManager.getLanguage(context)
            val locale = Locale(loc)

            val isMyMessage: Boolean = chat?.send_by == SharedPrefs.get().getObject(PROFILE, AppDetail::class.java).user_id.toString()
            tvName.text = chat?.name
             Glide.with(context).load(BASE_IMAGE_URL+chat?.profile_pic).apply(RequestOptions().circleCrop()).into(ivProfilePic)
            if (chat?.chat_type == ChatType.IMAGE) {
                if (isMyMessage) {
                    tvMessage.text = """${context.getString(R.string.you)} ${context.getString(R.string.sent_an_image)}"""
                } else {
                    tvMessage.text = """${chat.name} ${context.getString(R.string.sent_an_image)}"""
                }
            } else {
                tvMessage.text = chat?.text
            }
            val calendar = Calendar.getInstance()
            chat?.sent_at?.let { calendar.timeInMillis = convertDateTimeInMillis(it) }
            val dateString: String
            val time = getFormatFromDate(calendar.time, "h:mm a",locale)
            dateString = when {
                isToday(calendar.timeInMillis) -> String.format("%s", time)
                isYesterday(calendar) -> String.format("%s", context.getString(R.string.yesterday))
                existsInWeek(calendar) -> getFormatFromDate(calendar.time, "EEE",locale)!!
                else -> getFormatFromDate(calendar.time, "MMM dd",locale)!!
            }
            tvTime.text = dateString
            /* if (chat?.unDeliverCount ?: 0 > 0) {
                 tvUnreadCount.visibility = View.VISIBLE
                 if (chat?.unDeliverCount ?: 0 > 99)
                     tvUnreadCount.text = context.getString(R.string.nintynine_plus)
                 } else {
                     tvUnreadCount.text = chat?.unDeliverCount.toString()
                 }
             } else {
                 tvUnreadCount.visibility = View.GONE
             }*/
        }
    }
}