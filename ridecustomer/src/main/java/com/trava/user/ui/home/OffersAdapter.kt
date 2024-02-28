package com.trava.user.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.utils.AppUtils
import com.trava.user.webservices.models.categories.BannerData
import com.trava.utilities.setImageUrl
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import kotlinx.android.synthetic.main.item_offers.view.*

class OffersAdapter : RecyclerView.Adapter<OffersAdapter.ViewHolder>() {
    private var listener : OfferInterface? = null
    private var list = ArrayList<BannerData>()

    fun refreshList(list : ArrayList<BannerData>,listener : OfferInterface){
        this.list = list
        this.listener = listener
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_offers, parent, false))

    override fun getItemCount(): Int = list.size?:0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.ivImage?.setImageUrl(BASE_IMAGE_URL+list[position].image)

//
//        if(position == response?.size?.minus(1)){
//           holder.itemView.bottomView.hide()
//        }else{
//            holder.itemView.bottomView.show()
//        }

//        holder.bind(response?.get(position))
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView?:View(itemView?.context)) {

           init {
            itemView?.setOnClickListener {
                listener?.onOfferItemclick(list[adapterPosition])

//                if(adapterPosition == 0) {
//                    listener.onOfferItemclick(response?.get(adapterPosition))
//                }else{
//                    Toast.makeText(itemView.context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
//
//                }
            }
        }

//        fun bind(data: String?) {
//            itemView.tvSupportServiceName.text = data
////            Glide.with(itemView.context).load(BASE_IMAGE_URL+data?.image).into(itemView.ivSupportServiceImage)
//        }
    }
}

interface OfferInterface{
    fun onOfferItemclick(data : BannerData)
}