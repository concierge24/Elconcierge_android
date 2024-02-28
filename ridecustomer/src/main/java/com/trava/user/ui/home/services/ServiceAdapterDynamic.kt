package com.trava.user.ui.home.services

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.utilities.Constants.DELIVERY20
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.Utils
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_CATEGORY
import com.trava.utilities.webservices.models.Service
import kotlinx.android.synthetic.main.item_services_dynamic.view.*

class ServiceAdapterDynamic(private val context: Context, private val recyclerView: RecyclerView,
                            val mImages: ArrayList<Service>)
    : RecyclerView.Adapter<ServiceAdapterDynamic.ViewHolder>() {

    private val itemsCount = mImages.size

    override fun getItemCount(): Int {
        return itemsCount
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = position % itemsCount
        holder.onBind(pos, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_services_dynamic, parent, false))
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView ?: View(context)) {
        init {
            itemView?.layoutParams?.width = (Utils.getScreenWidth(context as Activity) / 5) + Utils.dpToPx(12).toInt()
            itemView?.setOnClickListener {
                recyclerView.smoothScrollToPosition(adapterPosition)
            }
        }

        fun onBind(position: Int, actualPosition: Int) {
            if (SECRET_DB_KEY!="a9f7a7a5157859a3b4b6c4172567c015")
            {
                itemView.tvSelectedServicee.visibility = View.VISIBLE
                itemView.tvSelectedServicee.setText(mImages[position].name)
            }
             Glide.with(itemView.imageView.context).load(BASE_IMAGE_CATEGORY+mImages[position].image).into(itemView.imageView)
        }

        fun setSelected(position: Int) {
            if (SECRET_DB_KEY!="a9f7a7a5157859a3b4b6c4172567c015")
            {
                itemView.tvSelectedServicee.visibility = View.VISIBLE
                itemView.tvSelectedServicee.setText(mImages[position].name)
            }

            Glide.with(itemView.imageView.context).load(BASE_IMAGE_CATEGORY+mImages[position].image).into(itemView.imageView)
        }

        fun setUnSelected(position: Int) {

            if (SECRET_DB_KEY!="a9f7a7a5157859a3b4b6c4172567c015")
            {
                itemView.tvSelectedServicee.visibility = View.VISIBLE
                itemView.tvSelectedServicee.setText(mImages[position].name)
            }

            Glide.with(itemView.imageView.context).load(BASE_IMAGE_CATEGORY+mImages[position].image).into(itemView.imageView)
        }
    }
}