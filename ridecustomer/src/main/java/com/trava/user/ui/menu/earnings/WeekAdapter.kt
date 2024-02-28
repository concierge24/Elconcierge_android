package com.trava.user.ui.menu.earnings

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.trava.user.R
import com.trava.user.webservices.models.earnings.StartEndDateModel
import kotlinx.android.synthetic.main.item_week.view.*

class WeekAdapter(private val response: List<String>?, var startEndDateList: ArrayList<StartEndDateModel>?,
                  val onclickWeek: OnclickWeek) :
        androidx.recyclerview.widget.RecyclerView.Adapter<WeekAdapter.ViewHolder>() {
    private var globalPos = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_week, parent,
                    false))

    override fun getItemCount(): Int = response?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(response?.get(position), position)
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        init {
            itemView?.setOnClickListener {
                if (adapterPosition != -1) {
                    globalPos = adapterPosition
                    notifyDataSetChanged()
                }
            }
        }

        fun bind(data: String?, position: Int) {
            itemView.tvDate.text = data

            if (startEndDateList?.get(position)?.isSelected ?: false) {
                itemView.viewVisible.visibility = View.GONE
                itemView.tvDate.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            } else {
                itemView.viewVisible.visibility = View.GONE
                itemView.tvDate.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
            }

            itemView.llWeek.setOnClickListener {
                onclickWeek.onClcikWeekListener(position = position)
                itemView.viewVisible.visibility = View.VISIBLE
                for (i in 0..((startEndDateList?.size ?: 0) - 1)) {
                    if (position == i)
                        startEndDateList?.get(position)?.isSelected = true
                    else
                        startEndDateList?.get(i)?.isSelected = false
                }
                notifyDataSetChanged()
            }
        }
    }

    interface OnclickWeek {
        fun onClcikWeekListener(position: Int)
    }
}