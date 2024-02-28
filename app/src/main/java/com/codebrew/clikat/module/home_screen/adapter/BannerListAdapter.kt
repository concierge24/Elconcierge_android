package com.codebrew.clikat.module.home_screen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.codebrew.clikat.R
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.databinding.ItemFullBannerBinding
import com.codebrew.clikat.databinding.ItemFullEssentialBinding
import com.codebrew.clikat.databinding.ItemHalfBannerBinding
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.TopBanner
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.item_half_banner.view.*


class BannerListAdapter(private val bannerList: List<TopBanner>, private val mBannerWidth: Int, private val mSingleVendor: Int,
                        private val clientInform: SettingModel.DataBean.SettingData?, private val screenType: Int?) : RecyclerView.Adapter<BannerListAdapter.ViewHolder>() {
    private var callback: BannerCallback? = null

    fun settingCallback(mCallabck: BannerCallback?) {
        callback = mCallabck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == ITEM_BANNER_FULL) {
            val binding: ItemFullBannerBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_full_banner, parent, false)
            binding.color = Configurations.colors
            ViewHolder(binding.root)
        } else if (viewType == TYPE_BANNER_ESSENTIAL) {
            val binding: ItemFullEssentialBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_full_essential, parent, false)
            binding.color = Configurations.colors
            ViewHolder(binding.root)
        } else {
            val binding: ItemHalfBannerBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_half_banner, parent, false)
            binding.color = Configurations.colors
            ViewHolder(binding.root)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*   if (bannerList.get(position).getName() != null) {
            holder.bannerName.setText(bannerList.get(position).getName());
        }*/

        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.ic_banner_placeholder)
                .error(R.drawable.ic_banner_placeholder)

        if (bannerList[position].staticImage == true) {
            callback?.onPagerScroll( bannerList)

            Glide.with(holder.bannerImage.context).load(bannerList[position].bannerImage).apply(requestOptions).into(holder.bannerImage)
        } else if (bannerList[position].phone_image != null) {
            if (bannerList[position].isEnabled) holder.onBind()

            if (bannerList[position].isWebsite == true) {
                loadImage(bannerList[position].website_image, holder.bannerImage, true)
            } else {
                loadImage(bannerList[position].phone_image, holder.bannerImage, true)
            }

        } else {
            if (bannerList[0].pickupImage?.isNotEmpty() == true) {
                Glide.with(holder.bannerImage.context).load(bannerList[position].pickupImage).apply(requestOptions).into(holder.bannerImage)
            } else {
                Glide.with(holder.bannerImage.context).load(bannerList[position].bannerImage).apply(requestOptions).into(holder.bannerImage)

            }
        }

        if (mBannerWidth == TYPE_BANNER_HALF && clientInform?.is_wagon_app == "1") {
            val set = ConstraintSet()
            set.clone(holder.itemView.itemLayout)

            when (screenType) {
                AppDataType.Food.type -> {
                    set.setDimensionRatio(holder.bannerImage.id, "4:3")
                }
                else -> {
                    set.setDimensionRatio(holder.bannerImage.id, "4:4")
                }
            }
            set.applyTo(holder.itemView.itemLayout)
        }
    }

    override fun getItemCount(): Int {
        return bannerList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mBannerWidth == 0) ITEM_BANNER_FULL
        else if(mBannerWidth == 2) TYPE_BANNER_ESSENTIAL
        else TYPE_BANNER_HALF
    }

    interface BannerCallback {
        fun onBannerDetail(bannerBean: TopBanner?)
        fun onPagerScroll(bannerList: List<TopBanner>){}
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bannerImage: RoundedImageView
        var bannerName: TextView
        fun onBind() {
            if (mSingleVendor == VendorAppType.Multiple.appType) {
                itemView.setOnClickListener { v: View? -> callback?.onBannerDetail(bannerList[adapterPosition]) }
            }
        }
        init {
            bannerImage = itemView.findViewById(R.id.iv_banner)
            bannerName = itemView.findViewById(R.id.tv_bannerName)
        }
    }

    companion object {
        const val ITEM_BANNER_FULL = 0
        const val TYPE_BANNER_HALF = 1
        const val TYPE_BANNER_ESSENTIAL = 2
    }

//    companion object {
//        const val ITEM_BANNER_FULL = 0
//        const val TYPE_BANNER_HALF = 1
//        const val TYPE_BANNER_ESSENTIAL = 2
//    }


}