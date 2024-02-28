package com.trava.user.ui.home.services

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.utilities.Utils
import kotlinx.android.synthetic.main.item_home_services.view.*

class ServiceAdapter(private val context: Context, private val recyclerView: RecyclerView, val mImages: IntArray,var  mImagesSelected: IntArray) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {
    //private val mImages = intArrayOf(R.drawable.ic_gas_0, R.drawable.ic_water_0, R.drawable.ic_water_tank_0, R.drawable.ic_freight_0, R.drawable.ic_tow_truck_0,R.drawable.ic_car_0)
    //private val mImagesSelected = intArrayOf(R.drawable.ic_gas_1, R.drawable.ic_water_1, R.drawable.ic_water_tank_1, R.drawable.ic_freight_1, R.drawable.ic_tow_truck_1,R.drawable.ic_car_1)

    /*  private val mImages = intArrayOf(R.drawable.ic_tow_truck_0,R.drawable.ic_car_0)
      private val mImagesSelected = intArrayOf(R.drawable.ic_tow_truck_1,R.drawable.ic_car_1)*/

    private val itemsCount = mImages.size

    override fun getItemCount(): Int {
        return itemsCount
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = position % itemsCount
        holder.onBind(pos, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_home_services, parent, false))
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView?:View(context)) {
        init {
            itemView?.layoutParams?.width = (Utils.getScreenWidth(context as Activity) / 5) + Utils.dpToPx(12).toInt()
            itemView?.setOnClickListener {
                //                (context as HomeActivity).recyclerView.smoothScrollToPosition(adapterPosition-2)
//                layoutmanager.smoothScrollToPosition(context.recyclerView,RecyclerView.State(), adapterPosition-2)
                recyclerView.smoothScrollToPosition(adapterPosition)
//                layoutmanager.smoothScrollToPosition(adapterPosition-2)
//            }
            }
        }

        fun onBind(position: Int, actualPosition: Int) {
            itemView.imageView.setImageResource(mImages[position])
        }

        fun setSelected(position: Int) {
//            itemView.imageView.setImageResource(mImagesSelected[position % itemsCount])
            itemView.imageView.setImageResource(mImagesSelected[position])

        }

        fun setUnSelected(position: Int) {
//            itemView.imageView.setImageResource(mImages[position % itemsCount])
            itemView.imageView.setImageResource(mImages[position])

        }
    }

    var prevPosition = 0;
}