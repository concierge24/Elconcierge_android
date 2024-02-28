package com.trava.user.ui.home.chatModule.chatMessage

import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.user.R
import com.trava.utilities.ChatType
import com.trava.utilities.SharedPrefs
import com.trava.utilities.Utils.existsInWeek
import com.trava.utilities.Utils.isYesterday
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.getFormatFromDate
import com.trava.utilities.webservices.models.AppDetail
import com.bumptech.glide.Glide
import com.trava.user.utils.ConfigPOJO
import com.trava.utilities.LocaleManager
import kotlinx.android.synthetic.main.fragment_delivery_starts.view.tvTime
import kotlinx.android.synthetic.main.item_chat_media.view.*
import kotlinx.android.synthetic.main.item_chat_media_others.view.*
import kotlinx.android.synthetic.main.item_chat_others.view.*
import kotlinx.android.synthetic.main.item_direct_chat.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val context: Context?, private val chatListing: ArrayList<ChatMessageListing>) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val ITEM_TEXT = 1

    private val ITEM_TEXT_OTHERS = 2

    private val ITEM_MEDIA = 3

    private val ITEM_MEDIA_OTHERS = 4

    val CHANGE_SENT_STATUS = "change_sent_status"

    override fun getItemViewType(position: Int): Int {
        return if (chatListing[position].send_by == SharedPrefs.get().getObject(PROFILE, AppDetail::class.java).user_id.toString()) {
            if (chatListing[position].chat_type?: ChatType.TEXT == ChatType.TEXT) {
                ITEM_TEXT
            } else {
                ITEM_MEDIA
            }
        } else {
            if (chatListing[position].chat_type?:ChatType.TEXT == ChatType.TEXT) {
                ITEM_TEXT_OTHERS
            } else {
                ITEM_MEDIA_OTHERS
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TEXT -> TextViewHolder(LayoutInflater.from(context).inflate(R.layout.item_direct_chat, parent, false))
            ITEM_TEXT_OTHERS -> TextOthersViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_others, parent, false))
            ITEM_MEDIA -> MediaViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_media, parent, false))
            ITEM_MEDIA_OTHERS -> MediaOthersViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_media_others, parent, false))
            else -> TextViewHolder(LayoutInflater.from(context).inflate(R.layout.item_direct_chat, parent, false))

        }
    }

    override fun getItemCount(): Int {
        return chatListing.size
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val showDateHeader: Boolean
        if (position == 0) {
            showDateHeader = true
        } else {
            val cal1 = Calendar.getInstance()
            cal1.timeInMillis = convertDateTimeInMillis(chatListing[position - 1].sent_at)
            val cal2 = Calendar.getInstance()
            cal2.timeInMillis = convertDateTimeInMillis(chatListing[position - 1].sent_at)
            showDateHeader = !(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
        }
        var payload = ""
        if (payloads.isNotEmpty()) {
            payload = payloads[0] as String
        }
        when (holder) {
            is TextViewHolder -> holder.bind(chatListing[position], payload, showDateHeader)
            is TextOthersViewHolder -> holder.bind(chatListing[position], showDateHeader)
            is MediaViewHolder -> holder.bind(chatListing[position], payload, showDateHeader)
            is MediaOthersViewHolder -> holder.bind(chatListing[position], showDateHeader)
        }
    }


    inner class TextViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val clickListener = View.OnClickListener {
            if (chatListing[adapterPosition].isFailed == true) {
                chatListing[adapterPosition].isFailed = false
                (context as ChatActivity).sendMessage(chatListing[adapterPosition])
                notifyItemChanged(adapterPosition, CHANGE_SENT_STATUS)
            }
        }

        init {
            itemView.tvTime.setOnClickListener(clickListener)
            itemView.ivTick.setOnClickListener(clickListener)
        }


        fun bind(chat: ChatMessageListing, payload: String, showDateHeader: Boolean) {
            if (payload.isEmpty()) {
                itemView.tvMessage.text = chat.text
                itemView.cvTextt.setCardBackgroundColor(Color.parseColor(ConfigPOJO.primary_color))

                if (showDateHeader) {
                    itemView.tvDateHeader.text = chat.sent_at?.let { getDateHeader(convertDateTimeInMillis(it)) }
                    itemView.tvDateHeader.visibility = View.VISIBLE
                } else {
                    itemView.tvDateHeader.visibility = View.GONE
                }
                setSentStatus(chat)
            } else {
                setSentStatus(chat)
            }
        }

        private fun setSentStatus(chat: ChatMessageListing) {
            when {
                chat.isFailed == true -> {
                    itemView.ivTick.setImageResource(R.drawable.ic_error_outline)
                    itemView.tvTime.text = context?.getString(R.string.retry)
                    context?.let { ContextCompat.getColor(it, R.color.pink) }?.let { itemView.tvTime.setTextColor(it) }
                }
                chat.isSent == true -> {
                    itemView.ivTick.setImageResource(R.drawable.ic_sent)
                    itemView.tvTime.text = chat.sent_at?.let { DateUtils.formatDateTime(context, convertDateTimeInMillis(it), DateUtils.FORMAT_SHOW_TIME) }
                    context?.let { ContextCompat.getColor(it, R.color.black) }?.let { itemView.tvTime.setTextColor(it) }
                }
                else -> {
                    itemView.ivTick.setImageResource(R.drawable.ic_sent)
                    itemView.tvTime.text = chat.sent_at?.let { DateUtils.formatDateTime(context, convertDateTimeInMillis(it), DateUtils.FORMAT_SHOW_TIME) }
                    context?.let { ContextCompat.getColor(it, R.color.black) }?.let { itemView.tvTime.setTextColor(it) }
                }
            }
        }
    }


    inner class TextOthersViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(chat: ChatMessageListing, showDateHeader: Boolean) {
            itemView.tvTimeOthers.text = chat.sent_at?.let { DateUtils.formatDateTime(context, convertDateTimeInMillis(it), DateUtils.FORMAT_SHOW_TIME) }
            itemView.tvMessageOthers.text = chat.text
            if (showDateHeader) {
                itemView.tvDateHeaderOthers.text = chat.sent_at?.let { getDateHeader(convertDateTimeInMillis(it)) }
                itemView.tvDateHeaderOthers.visibility = View.VISIBLE
            } else {
                itemView.tvDateHeaderOthers.visibility = View.GONE
            }
        }
    }

    inner class MediaViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(chat: ChatMessageListing, payload: String, showDateHeader: Boolean) {
            if (payload.isEmpty()) {
                itemView.tvTimeMedia.text = chat.sent_at?.let { DateUtils.formatDateTime(context, convertDateTimeInMillis(it), DateUtils.FORMAT_SHOW_TIME) }
                if (showDateHeader) {
                    itemView.tvDateHeaderMedia.text = chat.sent_at?.let { getDateHeader(convertDateTimeInMillis(it)) }
                    itemView.tvDateHeaderMedia.visibility = View.VISIBLE
                } else {
                    itemView.tvDateHeaderMedia.visibility = View.GONE
                }
                context?.let { Glide.with(it).load(chat.original_image).into(itemView.imageView) }
                setSentStatus(chat)
            } else {
                if (payload == CHANGE_SENT_STATUS) {
                    setSentStatus(chat)
                }
            }
        }
        private fun setSentStatus(chat: ChatMessageListing) {
            when {
                chat.isFailed == true -> {
                    itemView.ivTickMedia.setImageResource(R.drawable.ic_error_outline)
                    itemView.tvTimeMedia.text = context?.getString(R.string.retry)
                    context?.let { ContextCompat.getColor(it, R.color.pink) }?.let { itemView.tvTime?.setTextColor(it) }
                }
                chat.isSent == true -> {
                    itemView.ivTickMedia.setImageResource(R.drawable.ic_sent)
                    itemView.tvTimeMedia.text = chat.sent_at?.let { DateUtils.formatDateTime(context, convertDateTimeInMillis(it), DateUtils.FORMAT_SHOW_TIME) }
                    context?.let { ContextCompat.getColor(it, R.color.black) }?.let { itemView.tvTimeMedia?.setTextColor(it) }
                }
                else -> {
                    itemView.ivTickMedia.setImageResource(R.drawable.ic_sent)
                    itemView.tvTimeMedia.text = chat.sent_at?.let { DateUtils.formatDateTime(context, convertDateTimeInMillis(it), DateUtils.FORMAT_SHOW_TIME) }
                    context?.let { ContextCompat.getColor(it, R.color.black) }?.let { itemView.tvTimeMedia?.setTextColor(it) }
                }
            }
        }

    }

    inner class MediaOthersViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(chat: ChatMessageListing, showDateHeader: Boolean) {
            context?.let { Glide.with(it).load(chat.original_image) }
            itemView.tvTimeMediaOther.text = chat.sent_at?.let { DateUtils.formatDateTime(context, convertDateTimeInMillis(it), DateUtils.FORMAT_SHOW_TIME) }
            if (showDateHeader) {
                itemView.tvDateHeaderMediaOther.text = chat.sent_at?.let { getDateHeader(convertDateTimeInMillis(it)) }
                itemView.tvDateHeaderMediaOther.visibility = View.VISIBLE
            } else {
                itemView.tvDateHeaderMediaOther.visibility = View.GONE
            }
            context?.let { Glide.with(it).load(chat.original_image).into(itemView.imageViewOther) }
        }
    }

    private fun getDateHeader(millis: Long): String? {

        var loc = LocaleManager.getLanguage(context)
        val locale = Locale(loc)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        val dateString: String?
        dateString = when {
            DateUtils.isToday(calendar.timeInMillis) -> context?.getString(R.string.today)
            isYesterday(calendar) -> String.format("%s", context?.getString(R.string.yesterday))
            existsInWeek(calendar) -> getFormatFromDate(calendar.time, "EEEE",locale)
            else -> getFormatFromDate(calendar.time, "MMM dd",locale)
        }
        return dateString
    }


    fun convertDateTimeInMillis(givenDateString: String): Long {
        var timeInMilliseconds: Long = 0
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            var mDate = sdf.parse(givenDateString)
            timeInMilliseconds = mDate.getTime()
            System.out.println("Date in milli :: " + timeInMilliseconds)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timeInMilliseconds
    }

}