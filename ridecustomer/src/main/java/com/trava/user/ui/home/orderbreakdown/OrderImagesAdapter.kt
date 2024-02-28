package com.trava.driver.ui.home.orderDetails

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.trava.user.R
import com.trava.user.webservices.models.order.OrderImageUrls
import kotlinx.android.synthetic.main.item_order_details_image.view.*

class OrderImagesAdapter(private val l1 : ArrayList<OrderImageUrls>) : RecyclerView.Adapter<OrderImagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v1 = View.inflate(parent.context, R.layout.item_order_details_image, null)
        return ViewHolder(v1)
    }

    override fun getItemCount(): Int {
        return l1.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(l1[position])
    }


    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        fun bind(data: OrderImageUrls) = with(itemView) {
            Glide.with(ivOrderImage.context).setDefaultRequestOptions(RequestOptions()
                  .placeholder(R.drawable.loading_image))
                    .load(data.image_url)
                    .into(ivOrderImage)
        }
    }

}