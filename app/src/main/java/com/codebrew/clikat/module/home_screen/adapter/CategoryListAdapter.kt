package com.codebrew.clikat.module.home_screen.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.databinding.*
import com.codebrew.clikat.modal.other.English
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.home_screen.adapter.CategoryListAdapter.CategoryViewHolder
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.card.MaterialCardView
import com.makeramen.roundedimageview.RoundedImageView

class CategoryListAdapter internal constructor(private val beanList: List<English>,
                                               private val screenType: Int,
                                               private val clientInform: SettingModel.DataBean.SettingData?) : RecyclerView.Adapter<CategoryViewHolder>() {
    private var mCallback: CategoryDetail? = null

    private val colorConfig by lazy { Configurations.colors }

    private var mContext: Context? = null
    private var isSkipLarge: Boolean? = false

    fun settingCallback(mCallback: CategoryDetail?) {
        this.mCallback = mCallback
    }

    fun isSkipViewAllUi(isSkipLarge: Boolean) {
        this.isSkipLarge = isSkipLarge
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {

        mContext = parent.context

        return when (viewType) {
            MULTIPLE_CATEGORY -> {
                val binding: ItemCategoryVerticalV2Binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_category_vertical_v2, parent, false)
                binding.color = colorConfig
                CategoryViewHolder(binding.root, viewType)
            }
            CUSTOM_CATEGORY -> {
                val binding: ItemCategoryCustomBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_category_custom, parent, false)
                binding.color = colorConfig
                CategoryViewHolder(binding.root, viewType)
            }
            ROUND_CATEGORY -> {
                val binding: ItemRoundCategoryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_round_category, parent, false)
                binding.color = Configurations.colors
                CategoryViewHolder(binding.root, viewType)
            }
            DELVIN_APP_CATEGORY -> {
                val binding: ItemCategoryDelvinBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                        R.layout.item_category_delvin, parent, false)
                binding.color = colorConfig
                CategoryViewHolder(binding.root, viewType)
            }

            CLIKAT_CATEGORY -> {
                val binding: ItemClikatCategoryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_clikat_category, parent, false)
                binding.color = colorConfig
                CategoryViewHolder(binding.root, viewType)
            }
            TYPE_SKIP_CATEGORY -> {
                val binding: ViewDataBinding = if (isSkipLarge == false)
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_skip_category, parent, false)
                else
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_category_large, parent, false)
                when (binding) {
                    is ItemSkipCategoryBinding -> {
                        binding.color = colorConfig
                    }
                    is ItemCategoryLargeBinding -> {
                        binding.color = colorConfig
                    }
                }
                CategoryViewHolder(binding.root, viewType)
            }
            NEW_CATEGORY -> {
                val binding: ItemNewCategoryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_new_category, parent, false)
                binding.color = Configurations.colors
                CategoryViewHolder(binding.root, viewType)
            }

            SCRUBBLE_APP_CATEGORY -> {
                val binding: ItemCategoryScrubbleBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_category_scrubble, parent, false)
                binding.color = Configurations.colors
                CategoryViewHolder(binding.root, viewType)
            }
            else -> {
                val binding: ItemCategoryServiceBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_category_service, parent, false)
                binding.color = Configurations.colors
                CategoryViewHolder(binding.root, viewType)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: CategoryViewHolder, i: Int) {
        viewHolder.onBind()
    }

    fun getCategoriesList(): List<English> {
        return beanList
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            BuildConfig.CLIENT_CODE == "thepeoplestore_0386" -> {
                DELVIN_APP_CATEGORY
            }
            BuildConfig.CLIENT_CODE == "scrubble_0566" || clientInform?.is_wagon_app=="1" && screenType==AppDataType.HomeServ.type-> {
                SCRUBBLE_APP_CATEGORY
            }
            clientInform?.is_skip_theme == "1" -> {
                TYPE_SKIP_CATEGORY
            }
            clientInform?.selected_template == "3" || clientInform?.is_wagon_app=="1" -> {
                NEW_CATEGORY
            }

            (screenType > AppDataType.Custom.type && clientInform?.is_health_theme == "1") -> {
                CUSTOM_CATEGORY
            }

            screenType > AppDataType.Custom.type && clientInform?.clikat_theme == "1" -> {
                CLIKAT_CATEGORY
            }
            screenType == AppDataType.Ecom.type && clientInform?.is_lubanah_theme == "1" -> {
                ROUND_CATEGORY
            }
            else -> {
                SERVICE_CATEGORY
            }
        }
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    interface CategoryDetail {
        fun onCategoryDetail(bean: English?)
    }

    inner class CategoryViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var ivCategory: RoundedImageView = itemView.findViewById(R.id.iv_userImage)
        var tvCategory: TextView = itemView.findViewById(R.id.category_text)
        val categoryCenterText: TextView? =
                if (clientInform?.show_ecom_v2_theme == "1") {
                    itemView.findViewById(R.id.category_center_text)
                } else null
        val clMain: MaterialCardView? = if (clientInform?.is_skip_theme == "1") itemView.findViewById(R.id.cvMain) else null

        //var tvDesc: TextView
        var viewType: Int = viewType

        @RequiresApi(Build.VERSION_CODES.M)
        fun onBind() {
            //tvDesc.visibility = if (screenType == AppDataType.Beauty.type) View.VISIBLE else View.GONE
            tvCategory.text = beanList[adapterPosition].name
            categoryCenterText?.text = beanList[adapterPosition].name
            //  tvDesc.text = beanList[adapterPosition].description
            itemView.isEnabled = true
            itemView.setOnClickListener { v: View? ->
                // holder.itemView.setEnabled(false);
                mCallback?.onCategoryDetail(beanList[adapterPosition])
            }

            if (clientInform?.show_ecom_v2_theme == "1" && adapterPosition == itemCount - 1) {
                categoryCenterText?.visibility = View.VISIBLE
                ivCategory.background = null
                tvCategory.visibility = View.INVISIBLE
            } else {
                if (clientInform?.show_tags_for_suppliers == "1")
                    loadImage(beanList[adapterPosition].tag_image, ivCategory, true)
                else
                    loadImage(if (screenType == AppDataType.HomeServ.type || screenType == AppDataType.Beauty.type) beanList[adapterPosition].icon else beanList[adapterPosition].icon, ivCategory, true)
            }


        }
    }

    companion object {
        private const val MULTIPLE_CATEGORY = 1
        private const val TWO_CATEGORY = 2
        private const val SERVICE_CATEGORY = 3
        private const val CUSTOM_CATEGORY = 4
        private const val CLIKAT_CATEGORY = 5
        private const val DELVIN_APP_CATEGORY = 6
        private const val NEW_CATEGORY = 7
        private const val ROUND_CATEGORY = 8
        private const val SCRUBBLE_APP_CATEGORY = 9
        private const val TYPE_SKIP_CATEGORY = 10
    }

}