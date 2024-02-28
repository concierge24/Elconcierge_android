package com.codebrew.clikat.module.subcategory.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.databinding.*
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.other.SubCategoryData
import com.codebrew.clikat.module.completed_order.adapter.OrderHistoryAdapter
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import com.makeramen.roundedimageview.RoundedImageView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_subcategory.view.*
import kotlinx.android.synthetic.main.item_subcategory_grid.view.*
import kotlinx.android.synthetic.main.item_subcategory_grid.view.tvCategoryName

/*
 * Created by Harman Sandhu .
 */
class SubCategorySelectAdapter(private val list: MutableList<SubCategoryData>?, private val viewType: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mCallback: SubCategoryCallback? = null
    fun settingCallback(mCallback: SubCategoryCallback?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_GRID -> {
                val binding: ItemSubcategoryGridBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                        R.layout.item_subcategory_grid, parent, false)
                binding.color = Configurations.colors
                binding.drawables = Configurations.drawables
                binding.strings = Configurations.strings
                GridViewHolder(binding.root)
            }
            else -> {
                val binding: ItemSubcategoryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                        R.layout.item_subcategory, parent, false)
                binding.color = Configurations.colors
                binding.drawables = Configurations.drawables
                binding.strings = Configurations.strings
                HorViewHolder(binding.root)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listItem = list?.get(position)

        if (holder is HorViewHolder) {
            holder.tvCategoryName.text = listItem?.name
            holder.imageView.loadImage(listItem?.image ?: "")

        } else {
            val horHolder = holder as GridViewHolder
            horHolder.tvCategoryName.text = listItem?.name
            horHolder.imageView.loadImage(listItem?.image ?: "")
        }
    }

    override fun getItemCount(): Int {
        return list?.size?:0
    }

    interface SubCategoryCallback {
        fun onSubCategoryDtail(dataBean: SubCategoryData?)
    }

    inner class HorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var tvCategoryName: TextView = itemView.tvCategory
        var imageView: CircleImageView = itemView.sdvProduct
        override fun onClick(v: View) {
            mCallback?.onSubCategoryDtail(list?.get(adapterPosition))
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var tvCategoryName: TextView = itemView.tvCategoryName
        var imageView: RoundedImageView = itemView.sdvProductImage
        override fun onClick(v: View) {
            mCallback?.onSubCategoryDtail(list?.get(adapterPosition))
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            BuildConfig.CLIENT_CODE === "dailyooz_0544" -> ITEM_HORIZONTAL
            else -> ITEM_GRID
        }
    }

    companion object {
        const val ITEM_GRID = 1
        const val ITEM_HORIZONTAL = 2
    }
}