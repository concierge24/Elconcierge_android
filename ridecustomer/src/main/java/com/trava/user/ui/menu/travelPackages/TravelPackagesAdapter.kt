package com.trava.user.ui.menu.travelPackages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trava.user.R
import com.trava.user.webservices.models.travelPackages.PackagesItem
import kotlinx.android.synthetic.main.item_packages.view.*

class TravelPackagesAdapter(private val list : ArrayList<PackagesItem>, private val listener : PackageInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PackageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_packages,parent,false))

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is PackageViewHolder){
            holder.bind(list[position])
        }
    }


    inner class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val context = itemView.context
        private val pkgItem: PackagesItem? = null

        init {
            itemView.setOnClickListener {
                listener.getSelectedData(list[adapterPosition],adapterPosition)
            }
        }
        fun bind(data : PackagesItem) = with(itemView) {
            if(adapterPosition%2 == 0){
                Glide.with(itemView.context).load(R.drawable.green).into(ivImage)
            }else{
                Glide.with(itemView.context).load(R.drawable.red).into(ivImage)
            }
            tvPkgKm.text = String.format("%s","${"Package for"} ${data.packageData?.distanceKms}${"KM"}")
            tvPkgName.text = data.name
            tvPkgDesc.text = data.description
            when(data.packageData?.packageType){
                "2" -> {
                    /* Hour Packages */
                    if(data.packageData.validity?:0 > 1){
                        tvPkgValid.text = String.format("%s", "${context.getString(R.string.validFor)} ${data.packageData.validity} ${context.getString(R.string.hours)}")
                    }else {
                        tvPkgValid.text = String.format("%s", "${context.getString(R.string.validFor)} ${data.packageData.validity} ${context.getString(R.string.hour)}")
                    }

                }

                "1" -> {
                    /* Day Packages */
                    if(data.packageData.validity?:0 > 1){
                        tvPkgValid.text = String.format("%s", "${context.getString(R.string.validFor)} ${data.packageData.validity} ${context.getString(R.string.days)}")
                    }else {
                        tvPkgValid.text = String.format("%s", "${context.getString(R.string.validFor)} ${data.packageData.validity} ${context.getString(R.string.day)}")
                    }


                }

                "3" -> {
                    /* Month Packages */
                    if(data.packageData.validity?:0 > 1){
                        tvPkgValid.text = String.format("%s", "${context.getString(R.string.validFor)} ${data.packageData.validity} ${context.getString(R.string.months)}")

                    }else {
                        tvPkgValid.text = String.format("%s", "${context.getString(R.string.validFor)} ${data.packageData.validity} ${context.getString(R.string.month)}")
                    }


                }
            }
            val catList = data.packageData?.packagePricings
            rvCategories.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            rvCategories.adapter = catList?.let { TravelPkgCategoriesAdapter(it) }


        }
    }
}

interface PackageInterface {
    fun getSelectedData(data : PackagesItem,position: Int)
}