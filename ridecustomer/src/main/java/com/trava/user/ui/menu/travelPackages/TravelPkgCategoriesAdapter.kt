package com.trava.user.ui.menu.travelPackages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trava.user.R
import com.trava.user.webservices.models.travelPackages.PackagePricingsItem
import kotlinx.android.synthetic.main.item_pkg_categories.view.*

class TravelPkgCategoriesAdapter(private val list: ArrayList<PackagePricingsItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PackageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pkg_categories, parent, false))

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PackageViewHolder) {
            holder.bind(list[position])
        }
    }


    inner class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: PackagePricingsItem) = with(itemView) {
            if (data.category_brand_product?.image != null) {
                Glide.with(ivCatImage).load(data.category_brand_product.image).into(ivCatImage)
            }
        }
    }
}