package com.trava.user.ui.home.orderbreakdown

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.trava.user.R
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import kotlinx.android.synthetic.main.extra_charges_layout.view.*

class ChargesListAdapter(var mContext: Context?, var listRequests: ArrayList<String>) : RecyclerView.Adapter<MyHolderExtra>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolderExtra {
        return MyHolderExtra(LayoutInflater.from(mContext).inflate(R.layout.extra_charges_layout, parent, false))
    }

    override fun onBindViewHolder(holder: MyHolderExtra, position: Int) {
        holder.ivImage?.let {
            Glide.with(holder.ivImage.context).setDefaultRequestOptions(RequestOptions()
                    .placeholder(R.drawable.loading_image)).load(BASE_IMAGE_URL + listRequests.get(position))
                    .into(it)
        }
    }

    override fun getItemCount(): Int {
        return listRequests.size
    }
}

open class MyHolderExtra(view: View) : RecyclerView.ViewHolder(view) {
    var ivImage = view.ivImage
}