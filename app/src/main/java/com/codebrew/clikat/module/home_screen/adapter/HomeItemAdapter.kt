package com.codebrew.clikat.module.home_screen.adapter

import android.content.Context
import android.graphics.*
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import androidx.viewpager2.widget.ViewPager2
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.DateTimeUtils
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.app_utils.extension.pushDownClickListener
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.NearBySupplierType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.Currency
import com.codebrew.clikat.databinding.*
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.module.completed_order.adapter.OrderHistoryAdapter
import com.codebrew.clikat.module.custom_home.ClikatHomeFragment
import com.codebrew.clikat.module.custom_home.CustomHomeFrag
import com.codebrew.clikat.module.home_screen.HomeFragment
import com.codebrew.clikat.module.home_screen.resturant_home.dineIn.DineInResturantFrag
import com.codebrew.clikat.module.home_screen.resturant_home.pickup.PickupResturantFrag
import com.codebrew.clikat.module.home_screen.resturant_home.wagon_pickup.WagonPickUp
import com.codebrew.clikat.module.restaurant_detail.adapter.ProdListAdapter
import com.codebrew.clikat.module.wishlist_prod.adapter.WishlistAdapter
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.ZoomInTransformer
import com.codebrew.clikat.utils.configurations.Configurations
import com.codebrew.clikat.utils.customviews.ClikatTextView
import com.makeramen.roundedimageview.RoundedImageView
import com.trava.user.webservices.models.Home
import kotlinx.android.synthetic.main.item_special_offer_view.view.*
import me.relex.circleindicator.CircleIndicator3


class HomeItemAdapter(
    private val mSupplierList: MutableList<SupplierDataBean>?, private var deliveryType: Int,
    private val appUtils: AppUtils, private val clientInform: SettingModel.DataBean.SettingData?,
    var userAdrs: AddressBean?, val screenFlowBean: SettingModel.DataBean.ScreenFlowBean?,
    var currency: Currency?
) : RecyclerView.Adapter<ViewHolder>(), Filterable {


    private var fragment: Fragment? = null
    var mFilterList: MutableList<SupplierDataBean>? = mutableListOf()

    //  private val viewPool = RecycledViewPool()

    private var mCallback: SupplierListCallback? = null
    private var mContext: Context? = null
    private var itemModel: HomeItemModel? = null
    private var dynamicName: String? = null
    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }
    private val colorConfig by lazy { Configurations.colors }

    private val selectedColor by lazy { Color.parseColor(colorConfig.primaryColor) }
    private val unselectedColor by lazy { Color.parseColor(colorConfig.textBody) }

    init {
        mFilterList = mSupplierList
    }

    fun changeCurrency(selectedCurrency: Currency?) {
        currency = selectedCurrency
    }

    fun setFragCallback(fragment: Fragment) {
        this.fragment = fragment
    }

    fun setDeliveryType(type: Int) {
        deliveryType = type
    }

    fun settingCallback(mCallback: SupplierListCallback?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        return when (viewType) {
            TYPE_SUPPLER -> {
                val binding: ViewDataBinding = if (clientInform?.full_view_supplier_theme == "1")
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_supplier_layout_new,
                        parent,
                        false
                    )
                else
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_home_supplier,
                        parent,
                        false
                    )
                SuplierViewHolder(binding)
            }
            TYPE_SEARCH -> {
                val binding: ItemSearchViewBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_search_view,
                    parent,
                    false
                )
                binding.color = colorConfig
                binding.strings = textConfig
                SearchViewHolder(binding.root)
            }
            TYPE_RECOMEND_SUPLR, TYPE_RECENT_ORDERS, TYPE_BEST_SELLERS_SUPLR, TYPE_HORIZONTAL_SUPPLIERS, TYPE_FASTEST_DELIVERY, TYPE_CLIKAT_THEME_SUPPLIERS, TYPE_HIGHEST_RATING_SUPPLIERS,
            TYPE_NEW_RESTUARENT_SUPPLIERS, TYPE_CATEGORY_WISE_SUPPLIERS, TYPE_WISHLIST -> {
                val binding: ItemSupplierViewBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_supplier_view,
                    parent,
                    false
                )
                binding.color = Configurations.colors
                binding.strings = appUtils.loadAppConfig(0).strings
                RecomendViewHolder(binding.root)
            }

            TYPE_NEAR_BY_SUPPLIERS_MAP -> {
                val binding: ItemMapBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_map,
                    parent,
                    false
                )
                binding.color = colorConfig
                binding.strings = textConfig
                NearYouMapViewHolder(binding.root)
            }
            TYPE_SPCL_PRDT, TYPE_RECOMENDED_FOOD -> {
                val binding: ItemSpecialOfferViewBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_special_offer_view,
                    parent,
                    false
                )
                binding.color = colorConfig
                SpclViewHolder(binding)
            }

            TYPE_FILTER_TAG -> {
                val binding: ItemFilterTagBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_filter_tag,
                    parent,
                    false
                )
                binding.color = colorConfig
                FilterTagHolder(binding.root)
            }
            TYPE_APP_CATEGORY -> {
                val binding: ItemAppCategoryBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_app_category,
                    parent,
                    false
                )
                binding.color = colorConfig
                ItemAppHolder(binding.root)
            }
            TYPE_SINGLE_PROD -> {
                val binding: ItemTimeslotViewBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_timeslot_view,
                    parent,
                    false
                )
                binding.color = colorConfig
                ItemRestProdHolder(binding.root)
            }

            TYPE_BANNER_VIDEOS -> {
                val binding: ItemBannerVideoListBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_banner_video_list,
                    parent,
                    false
                )
                binding.color = colorConfig
                BannerVideoListViewHolder(binding.root)
            }
            else -> {
                val binding: ItemHomeListBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_home_list,
                    parent,
                    false
                )
                binding.color = colorConfig
                HomeListHolder(binding.root, viewType)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemModel = mFilterList?.get(position)?.itemModel
        dynamicName = mFilterList?.get(position)?.dynamicSectionName

        if (holder is RecomendViewHolder) {
            holder.groupGridList.visibility = View.GONE
            when {
                itemModel?.sponserList != null && itemModel?.sponserList?.isNotEmpty() == true -> {
                    holder.itemView.visibility = View.VISIBLE
                    holder.groupGridList.visibility = View.GONE
                    if (clientInform?.dynamic_home_screen_sections == "1" && !dynamicName.isNullOrEmpty())
                        holder.tvTitle.text = dynamicName
                    else
                        holder.tvTitle.text = when {
                            getItemViewType(position) == TYPE_BEST_SELLERS_SUPLR -> {
                                mContext?.getString(
                                    R.string.restaurant_best_sellers,
                                    textConfig?.suppliers
                                )
                            }
                            getItemViewType(position) == TYPE_HORIZONTAL_SUPPLIERS -> {
                                mContext?.getString(
                                    R.string.restaurant_trending_sellers,
                                    textConfig?.suppliers
                                )
                            }
                            getItemViewType(position) == TYPE_FASTEST_DELIVERY -> {
                                mContext?.getString(
                                    R.string.restaurant_fastest_delivery,
                                    textConfig?.suppliers
                                )
                            }
                            getItemViewType(position) == TYPE_CLIKAT_THEME_SUPPLIERS -> {
                                mContext?.getString(
                                    R.string.near_you_suppliers,
                                    textConfig?.suppliers
                                )
                            }
                            getItemViewType(position) == TYPE_WISHLIST -> {
                                mContext?.getString(R.string.your_favourite)
                            }

                            itemModel?.screenType ?: 0 > AppDataType.Custom.type -> {
                                val spannableString = SpannableString(
                                    mContext?.getString(
                                        R.string.recommed_supplier,
                                        textConfig?.suppliers
                                    )
                                )
                                spannableString.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    0,
                                    11,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                spannableString
                            }
                            else -> {
                                if (AppConstants.APP_SUB_TYPE == AppDataType.Food.type) {
                                    textConfig?.recommended_supplier ?: ""
                                } else {
                                    if (itemModel?.screenType == 8) {
                                        mContext?.getString(R.string.all_items)
                                    } else {
                                        mContext?.getString(
                                            R.string.recommed_supplier,
                                            textConfig?.suppliers
                                        )
                                    }
                                }
                            }
                        }

                    val listAdapter = SponsorListAdapter(
                        itemModel?.sponserList
                            ?: listOf(), itemModel?.screenType
                            ?: 0, clientInform, position, appUtils
                    )

                    holder.rvRecomndSupplier.apply {
                        layoutManager = if (itemModel?.screenType == AppDataType.HomeServ.type) {
                            lytManager("vertical")
                        } else {
                            lytManager("horizontal")
                        }

                        adapter = listAdapter
                        //  setRecycledViewPool(viewPool)
                    }

                    if (getItemViewType(position) == TYPE_HORIZONTAL_SUPPLIERS || getItemViewType(
                            position
                        ) == TYPE_FASTEST_DELIVERY ||
                        (getItemViewType(position) == TYPE_RECOMEND_SUPLR && clientInform?.table_book_mac_theme == "1") ||
                        (getItemViewType(position) == TYPE_RECOMEND_SUPLR && screenFlowBean?.app_type == AppDataType.Food.type && clientInform?.is_wagon_app == "1")
                    ) {
                        listAdapter.setIsSupplierDetail(true)
                    } else
                        listAdapter.setIsSupplierDetail(false)



                    if ((clientInform?.app_selected_theme == "3" || clientInform?.is_skip_theme == "1" || clientInform?.app_selected_theme == "1") && clientInform.hide_view_all != "1") {

                        if (clientInform.app_selected_theme == "3" || clientInform.app_selected_theme == "1")
                            holder.ivFwd.visibility = View.VISIBLE
                        else holder.tvViewAll.visibility = View.VISIBLE

                        holder.clMain.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground))
                    } else {
                        holder.ivFwd.visibility = View.GONE
                        holder.clMain.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground))
                    }

                    if (fragment != null)
                        when (fragment) {
                            is HomeFragment, is WagonPickUp -> {

                                if (fragment is HomeFragment) {
                                    listAdapter.settingCallback(fragment as HomeFragment)
                                } else if (fragment is WagonPickUp) {
                                    listAdapter.settingCallback(fragment as WagonPickUp)
                                }

                                holder.ivFwd.setOnClickListener {
                                    mCallback?.supplierViewMoreCliked(
                                        mFilterList?.get(position),
                                        getItemViewType(position),
                                        title = holder.tvTitle.text.toString()
                                    )
                                }
                                holder.tvViewAll.setOnClickListener {
                                    holder.ivFwd.callOnClick()
                                }
                            }
                            is CustomHomeFrag -> listAdapter.settingCallback(fragment as CustomHomeFrag)
                            is ClikatHomeFragment -> listAdapter.settingCallback(fragment as ClikatHomeFragment)
                        }


                }
                !itemModel?.recentOrdersList.isNullOrEmpty() -> {
                    holder.itemView.visibility = View.VISIBLE
                    holder.groupGridList.visibility = View.GONE
                    holder.tvTitle.text =
                        if (clientInform?.dynamic_home_screen_sections == "1") dynamicName
                        else
                            mContext?.getString(R.string.recent_orders)

                    val listAdapter = OrderHistoryAdapter(
                        holder.itemView.context, itemModel?.recentOrdersList
                            ?: mutableListOf(),
                        appUtils, clientInform, screenFlowBean, currency
                    )
                    listAdapter.setViewTypeUi(true)
                    holder.rvRecomndSupplier.apply {
                        layoutManager = lytManager("horizontal")
                        adapter = listAdapter
                    }

                    if (fragment != null)
                        when (fragment) {
                            is WagonPickUp -> {
                                listAdapter.settingCallback(fragment as WagonPickUp)
                            }
                            is HomeFragment -> {
                                listAdapter.settingCallback(fragment as HomeFragment)
                            }
                        }

                }
                !itemModel?.suppliersList.isNullOrEmpty() -> {
                    holder.itemView.visibility = View.VISIBLE
                    holder.groupGridList.visibility = View.VISIBLE
                    val listAdapter = NearBySuppliersAdapter(
                        itemModel?.suppliersList
                            ?: listOf(),
                        itemModel?.screenType
                            ?: 0,
                        clientInform,
                        position,
                        tableBooked = appUtils.getCurrentTableData() != null,
                        appUtils = appUtils,
                        deliveryType = deliveryType
                    )
                    holder.rvRecomndSupplier.apply {
                        layoutManager = if (clientInform?.dynamic_home_screen_sections == "1")
                            lytManager("vertical")
                        else lytManager("horizontal")
                        adapter = listAdapter
                    }

                    if (clientInform?.dynamic_home_screen_sections == "1") {
                        holder.tvTitle.text = dynamicName
                        listAdapter.setSectionType(itemModel?.nearBySupplierView ?: 1, true)
                    } else {
                        listAdapter.setSectionType(0, false)
                        holder.tvTitle.text = mContext?.getString(
                            R.string.near_you_suppliers,
                            appUtils.loadAppConfig(0).strings?.suppliers
                        )
                    }

                    if (itemModel?.nearBySupplierView == NearBySupplierType.GRID.type) {
                        holder.ivList.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
                        holder.ivGrid.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)
                    } else {
                        holder.ivList.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)
                        holder.ivGrid.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
                    }


                    if (fragment != null)
                        when (fragment) {
                            is HomeFragment, is WagonPickUp -> {
                                holder.ivList.setOnClickListener {
                                    mCallback?.onListViewChanges(position, false)
                                }
                                holder.ivGrid.setOnClickListener {
                                    mCallback?.onListViewChanges(position, true)
                                }
                                if (fragment is HomeFragment) {
                                    listAdapter.settingCallback(fragment as HomeFragment)
                                } else if (fragment is WagonPickUp) {
                                    listAdapter.settingCallback(fragment as WagonPickUp)
                                }

                            }
                        }
                }
                !itemModel?.highestRatingSuppliersList.isNullOrEmpty() -> {

                    holder.itemView.visibility = View.VISIBLE
                    if (clientInform?.dynamic_home_screen_sections == "1")
                        holder.tvTitle.text = dynamicName
                    else
                        holder.tvTitle.text = mContext?.getString(
                            R.string.highest_rating_suppliers,
                            appUtils.loadAppConfig(0).strings?.suppliers
                        )


                    val listAdapter = NearBySuppliersAdapter(
                        itemModel?.highestRatingSuppliersList
                            ?: listOf(),
                        itemModel?.screenType
                            ?: 0,
                        clientInform,
                        position,
                        tableBooked = appUtils.getCurrentTableData() != null,
                        appUtils = appUtils,
                        deliveryType = deliveryType
                    )
                    holder.rvRecomndSupplier.apply {
                        layoutManager = lytManager("horizontal")
                        adapter = listAdapter
                    }
                    if (fragment != null)
                        when (fragment) {
                            is HomeFragment -> {
                                listAdapter.settingCallback(fragment as HomeFragment)
                            }
                            is WagonPickUp -> {
                                listAdapter.settingCallback(fragment as WagonPickUp)
                            }
                        }
                }
                !itemModel?.newSuppliersList.isNullOrEmpty() -> {
                    holder.itemView.visibility = View.VISIBLE

                    if (clientInform?.dynamic_home_screen_sections == "1")
                        holder.tvTitle.text = dynamicName
                    else
                        holder.tvTitle.text = mContext?.getString(
                            R.string.new_suppliers,
                            appUtils.loadAppConfig(0).strings?.suppliers
                        )


                    val listAdapter = NearBySuppliersAdapter(
                        itemModel?.newSuppliersList
                            ?: listOf(),
                        itemModel?.screenType
                            ?: 0,
                        clientInform,
                        position,
                        tableBooked = appUtils.getCurrentTableData() != null,
                        appUtils = appUtils,
                        deliveryType = deliveryType
                    )
                    holder.rvRecomndSupplier.apply {
                        layoutManager = lytManager("horizontal")
                        adapter = listAdapter
                    }
                    if (fragment != null)
                        when (fragment) {
                            is HomeFragment -> {
                                listAdapter.settingCallback(fragment as HomeFragment)
                            }
                            is WagonPickUp -> {
                                listAdapter.settingCallback(fragment as WagonPickUp)
                            }
                        }
                }
                !itemModel?.wishListProd.isNullOrEmpty() -> {
                    holder.itemView.visibility = View.VISIBLE
                    holder.tvTitle.text = mContext?.getString(R.string.your_favourite)
                    val listAdapter = WishlistAdapter(
                        appUtils.getBookingFlow(),
                        screenFlowBean,
                        clientInform,
                        currency,
                        true
                    )
                    listAdapter.updateList(itemModel?.wishListProd)

                    holder.rvRecomndSupplier.apply {
                        layoutManager = lytManager("grid")
                        adapter = listAdapter
                    }
                    if (fragment != null)
                        when (fragment) {
                            is HomeFragment -> {
                                listAdapter.settingCallback(fragment as HomeFragment)
                            }
                            is WagonPickUp -> {
                                listAdapter.settingCallback(fragment as WagonPickUp)
                            }
                        }
                }
                !itemModel?.categoryWiseSuppliers.isNullOrEmpty() -> {
                    holder.itemView.visibility = View.VISIBLE
                    holder.tvTitle.visibility = View.GONE

//                    if (clientInform?.dynamic_home_screen_sections == "1")
//                        holder.tvTitle.text = dynamicName
//                    else
//                        holder.tvTitle.text = mContext?.getString(R.string.category_wise_suppliers, appUtils.loadAppConfig(0).strings?.suppliers)


                    val listAdapter = CategoryWiseSuppliersAdapter(
                        itemModel?.categoryWiseSuppliers
                            ?: arrayListOf(),
                        itemModel?.screenType
                            ?: 0,
                        clientInform,
                        position,
                        tableBooked = appUtils.getCurrentTableData() != null,
                        deliveryType
                    )

                    holder.rvRecomndSupplier.apply {
                        layoutManager = lytManager("vertical")
                        adapter = listAdapter
                    }
                    if (fragment != null)
                        when (fragment) {
                            is HomeFragment -> {
                                listAdapter.settingCallback(fragment as HomeFragment)
                            }
                            is WagonPickUp -> {
                                listAdapter.settingCallback(fragment as WagonPickUp)
                            }
                        }
                }
                else -> {
                    holder.itemView.visibility = View.GONE
                }
            }
        } else if (holder is HomeListHolder) {
            holder.onBind(dynamicName)
        } else if (holder is FilterTagHolder) {
            holder.onBind(itemModel)
        } else if (holder is ItemAppHolder) {
            holder.onBind(itemModel?.screenType)
        } else if (holder is SpclViewHolder) {
            holder.onBind(itemModel)
        } else if (holder is SearchViewHolder) {
            holder.onBind(itemModel)
        } else if (holder is ItemRestProdHolder) {
            if (itemModel?.vendorProdList != null) {
                holder.onBind(itemModel?.vendorProdList)
            }
        } else if (holder is NearYouMapViewHolder) {
            holder.onBind(itemModel)

            holder.onClickListener()
        } else if (holder is SuplierViewHolder) {
            holder.onBinData(mFilterList?.get(holder.adapterPosition))
            holder.onClickListener()
        } else if (holder is BannerVideoListViewHolder) {
            holder.onBind(itemModel?.bannerVideos ?: arrayListOf())
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (mFilterList?.get(position)?.viewType ?: TYPE_SUPPLER) {
            SPL_PROD_TYPE -> TYPE_SPCL_PRDT
            RECOMEND_TYPE -> TYPE_RECOMEND_SUPLR
            BEST_SELLERS -> TYPE_BEST_SELLERS_SUPLR
            BANNER_TYPE -> TYPE_BANNER
            CATEGORY_TYPE -> TYPE_CATEGORY
            APP_CAT_TYPE -> TYPE_APP_CATEGORY
            BRAND_TYPE -> TYPE_BRANDS
            SEARCH_TYPE -> TYPE_SEARCH
            SOCIAL_TYPE -> TYPE_SOCIAL
            FILTER_TYPE -> TYPE_FILTER_TAG
            SINGLE_PROD_TYPE -> TYPE_SINGLE_PROD
            POPULAR_TYPE -> TYPE_SPCL_PRDT
            TAGS_SUPPLIER_TYPE -> TYPE_TAGS
            HORIZONTAL_SUPPLIERS -> TYPE_HORIZONTAL_SUPPLIERS
            CLIKAT_THEME_SUPPLIERS -> TYPE_CLIKAT_THEME_SUPPLIERS
            HIGEST_RATING_SUPPLIERS -> TYPE_HIGHEST_RATING_SUPPLIERS
            NEW_RESTUARENT_SUPPLIERS -> TYPE_NEW_RESTUARENT_SUPPLIERS
            CATEGORY_WISE_SUPPLIERS -> TYPE_CATEGORY_WISE_SUPPLIERS
            NEAR_BY_SPPLIERS -> TYPE_NEAR_BY_SUPPLIERS_MAP
            FASTEST_DELIVERY_SUPPLIERS -> TYPE_FASTEST_DELIVERY
            RECOMMENDED_FOOD -> TYPE_RECOMENDED_FOOD
            RECENT_ORDERS -> TYPE_RECENT_ORDERS
            BANNER_VIDEOS -> TYPE_BANNER_VIDEOS
            WISHLIST_TYPE -> TYPE_WISHLIST
            else -> TYPE_SUPPLER
        }
    }

    override fun getItemCount(): Int {
        return mFilterList?.size ?: 0
    }

    internal inner class SuplierViewHolder(private var binding: ViewDataBinding) :
        ViewHolder(binding.root) {
        var sdvImage: RoundedImageView
        var tvName: ClikatTextView
        var tvSupplierInf: TextView
        var tvSupplierloc: ClikatTextView
        var tvRating: TextView
        var tvTrackOption: TextView
        var ivStatus: TextView
        fun onBinData(dataBean: SupplierDataBean?) {
            dataBean?.isOpen = appUtils.checkResturntTiming(dataBean?.timing)
            when (binding) {
                is ItemSupplierLayoutNewBinding -> {
                    (binding as ItemSupplierLayoutNewBinding).color = colorConfig
                    (binding as ItemSupplierLayoutNewBinding).strings = textConfig
                    (binding as ItemSupplierLayoutNewBinding).supplierData = dataBean
                    (binding as ItemSupplierLayoutNewBinding).settingData = clientInform
                    (binding as ItemSupplierLayoutNewBinding).isHungerApp =
                        clientInform?.is_hunger_app == "1"
                    (binding as ItemSupplierLayoutNewBinding).ratingBar.rating =
                        dataBean?.rating?.toFloat()
                            ?: 0f
                }

                is ItemHomeSupplierBinding -> {
                    (binding as ItemHomeSupplierBinding).color = colorConfig
                    (binding as ItemHomeSupplierBinding).strings = textConfig
                    (binding as ItemHomeSupplierBinding).supplierData = dataBean
                    (binding as ItemHomeSupplierBinding).settingData = clientInform
                    (binding as ItemHomeSupplierBinding).isHungerApp =
                        clientInform?.is_hunger_app == "1"
                    (binding as ItemHomeSupplierBinding).groupTags.visibility =
                        if (!dataBean?.supplier_tags.isNullOrEmpty() && clientInform?.show_tags_for_suppliers == "1")
                            View.VISIBLE else View.GONE
                    val tags = if (!dataBean?.supplier_tags.isNullOrEmpty()) {
                        dataBean?.supplier_tags?.joinToString(",")
                    } else ""
                    (binding as ItemHomeSupplierBinding).tvTags.text = tags


                    if (clientInform?.app_selected_theme == "3" && clientInform.is_table_booking == "1" && deliveryType == FoodAppType.DineIn.foodType) {
                        if (dataBean?.is_dine_in == 1)
                            (binding as ItemHomeSupplierBinding).tvBookNow.visibility = View.VISIBLE
                        else
                            (binding as ItemHomeSupplierBinding).tvBookNow.visibility = View.GONE

                        if (appUtils.getCurrentTableData() != null)
                            (binding as ItemHomeSupplierBinding).tvBookNow.visibility = View.GONE

                        (binding as ItemHomeSupplierBinding).tvBookNow.setOnClickListener {
                            mCallback?.onBookNow(dataBean ?: SupplierDataBean())
                        }
                    } else (binding as ItemHomeSupplierBinding).tvBookNow.visibility = View.GONE
                }
            }

            if (BuildConfig.CLIENT_CODE == "muraa_0322") {
                tvTrackOption.visibility = View.INVISIBLE
                ivStatus.visibility = View.INVISIBLE
                tvSupplierInf.visibility = View.INVISIBLE
            }
            mContext?.let {
                sdvImage.loadImage(dataBean?.logo ?: "")
            }
            //tvRating.setVisibility(dataBean.getRating() > 0 ? View.VISIBLE : View.GONE);
            // 0 for delivery 1 for pickup
            //  if (dataBean?.rating ?: 0.0 > 0) tvRating.text = dataBean?.rating.toString() else tvRating.text = "NEW"
            tvSupplierInf.text = if (clientInform?.full_view_supplier_theme == "1") {
                mContext?.getString(
                    R.string.min_max_time_,
                    dataBean?.delivery_min_time,
                    dataBean?.delivery_max_time
                )
            } else
                mContext?.getString(
                    R.string.min_max_time,
                    dataBean?.delivery_min_time,
                    dataBean?.delivery_max_time
                )

            tvSupplierInf.visibility =
                if (dataBean?.delivery_max_time != null && StaticFunction.checkVisibility(
                        clientInform?.show_supplier_delivery_timing,
                        clientInform?.show_supplier_info_settings
                    ) && deliveryType != FoodAppType.Pickup.foodType
                )
                    View.VISIBLE else View.INVISIBLE

        }

        fun onClickListener() {
            itemView.pushDownClickListener {
                mCallback?.onSupplierDetail(mFilterList?.get(adapterPosition))
            }
        }

        init {
            val view = binding.root

            ivStatus = view.findViewById(R.id.ivStatus)
            sdvImage = view.findViewById(R.id.sdvImage)
            tvName = view.findViewById(R.id.tvName)
            tvSupplierInf = view.findViewById(R.id.tv_supplier_inf)
            tvSupplierloc = view.findViewById(R.id.tvSupplierloc)
            tvRating = view.findViewById(R.id.tv_rating)
            tvTrackOption = view.findViewById(R.id.tv_live_track)
        }
    }


    internal inner class RecomendViewHolder(itemView: View) : ViewHolder(itemView) {
        var rvRecomndSupplier: RecyclerView = itemView.findViewById(R.id.rv_recomd_supplier)
        var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        var clMain: ConstraintLayout = itemView.findViewById(R.id.clMain)
        var ivFwd: ImageView = itemView.findViewById(R.id.ivFwd)
        val tvViewAll: TextView = itemView.findViewById(R.id.tvViewAll)
        val ivGrid: ImageView = itemView.findViewById(R.id.ivGrid)
        val ivList: ImageView = itemView.findViewById(R.id.ivList)
        val groupGridList: Group = itemView.findViewById(R.id.groupGridList)
    }

    inner class NearYouMapViewHolder(itemView: View) : ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        var tvViewAll: TextView = itemView.findViewById(R.id.tvViewAll)
        val rvMap: RoundedImageView = itemView.findViewById(R.id.rvMap)

        fun onBind(model: HomeItemModel?) {

            var markersString: String? = ""
            model?.suppliersList?.forEach {
                markersString += """&markers=color:red%7C${(it.latitude ?: 0.0)} , ${(it.longitude ?: 0.0)}"""

            }

            val map = if (!clientInform?.google_map_key.isNullOrEmpty())
                "https://maps.googleapis.com/maps/api/staticmap?center=" + userAdrs?.latitude + "," + userAdrs?.longitude + "&zoom=11&size=800x800&maptype=roadmap" + markersString + "&key=" + clientInform?.google_map_key
            else ""
            rvMap.loadImage(map)

            tvTitle.text = mContext?.getString(R.string.near_you)
            tvViewAll.visibility = View.VISIBLE
        }

        fun onClickListener() {
            tvViewAll.setOnClickListener {
                mCallback?.viewAllNearBy()
            }
        }

    }

    internal inner class SearchViewHolder(itemView: View) : ViewHolder(itemView) {
        var ivSearch: EditText = itemView.findViewById(R.id.ed_search)
        var ivFilter: ImageView = itemView.findViewById(R.id.iv_filter)
        fun onBind(model: HomeItemModel?) {
            ivFilter.setImageResource(if (model!!.isSingleVendor == VendorAppType.Single.appType) R.drawable.ic_search else R.drawable.ic_filter)
            ivSearch.isEnabled = model.isSingleVendor != VendorAppType.Single.appType
            ivSearch.hint =
                if (model.isSingleVendor == VendorAppType.Single.appType) mContext?.getString(R.string.search_for_resturant) else mContext!!.getString(
                    R.string.search_hint
                )
            ivFilter.setOnClickListener { v: View? ->
                if (model.isSingleVendor == VendorAppType.Single.appType && ivSearch.text.toString()
                        .isNotEmpty()
                ) {
                    mCallback!!.onSearchItem(ivSearch.text.toString())
                } else {
                    mCallback!!.onFilterScreen()
                }
            }

            if (clientInform?.show_ecom_v2_theme == "1") {
                ivSearch.isFocusable = false
                itemView.background = ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.background_toolbar_bottom_radius
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    itemView.background.colorFilter = BlendModeColorFilter(
                        Color.parseColor(colorConfig.toolbarColor),
                        BlendMode.SRC_ATOP
                    )
                }
                ivSearch.setOnClickListener {
                    mCallback?.onSearchClickedForV2Theme()
                }

                ivSearch.background = ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.search_home_radius_background
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    ivSearch.background.colorFilter = BlendModeColorFilter(
                        Color.parseColor(colorConfig.search_background),
                        BlendMode.SRC_ATOP
                    )
                }
                ivSearch.setTextColor(Color.parseColor(colorConfig.search_textcolor))
                ivSearch.setHintTextColor(Color.parseColor(colorConfig.search_textcolor))
            } else {
                ivSearch.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.rec_4_transparent)
            }
        }

    }

    internal inner class FilterTagHolder(itemView: View) : ViewHolder(itemView) {
        var tvResturantCount: TextView = itemView.findViewById(R.id.tv_resturant_count)
        var tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        var tvDesc: TextView = itemView.findViewById(R.id.tv_car_desc)
        var gpCategory: Group = itemView.findViewById(R.id.gp_category)
        var gpSupplier: Group = itemView.findViewById(R.id.gp_supplier)
        var tvSortBy: TextView = itemView.findViewById(R.id.tvSortBy)
        var ivFilter: ImageView = itemView.findViewById(R.id.ivFilter)
        var tvViewAll: TextView = itemView.findViewById(R.id.tv_view_all)
        fun onBind(model: HomeItemModel?) {
            tvDesc.text = mContext?.getString(
                R.string.we_hand_picked_some_great_services_for_you,
                textConfig?.suppliers
            )
            if (BuildConfig.CLIENT_CODE == "skipp_0631") {
                tvResturantCount.text = mContext?.getString(R.string.all_restaurants)
            } else if (BuildConfig.CLIENT_CODE == "ghaytah_0021") {
                tvResturantCount.text = mContext?.getString(R.string.all_items)
            } else {
                tvResturantCount.text = mContext?.getString(
                    R.string.resturant_count_tag, model?.supplierCount,
                    if (model?.supplierCount == 1) textConfig?.supplier else textConfig?.suppliers
                )
            }

            gpCategory.visibility = View.VISIBLE
            gpSupplier.visibility = View.GONE

            if (model?.screenType == AppDataType.Ecom.type) {
                tvCategory.text = mContext?.getString(R.string.special_offer)
                tvDesc.visibility = View.GONE
            } else if (model?.screenType != AppDataType.HomeServ.type && BuildConfig.CLIENT_CODE != "dailyooz_0544") {
                gpCategory.visibility = View.GONE
                gpSupplier.visibility = View.VISIBLE
            }

            if (BuildConfig.CLIENT_CODE == "yummy_0122") {
                tvDesc.visibility = View.GONE
            }

            tvSortBy.text = mContext?.getString(R.string.sort_by_popup, SORT_POPUP)
            tvSortBy.visibility =
                if (clientInform?.is_restaurant_sort == "1") View.VISIBLE else View.GONE
            ivFilter.visibility =
                if (clientInform?.show_filter_on_home == "1") View.VISIBLE else View.GONE
            tvViewAll.visibility =
                if (clientInform?.is_wagon_app == "1") View.VISIBLE else View.GONE
        }

        init {
            ivFilter.setOnClickListener {
                mCallback?.onFilterClicked(ivFilter)
            }
            tvSortBy.setOnClickListener {
                mCallback?.onSortByClicked(tvSortBy)
            }

            tvViewAll.setOnClickListener {
                mCallback?.onViewAllSupplier()
            }
        }
    }

    internal inner class ItemAppHolder(itemView: View) : ViewHolder(itemView) {
        private var tvTitle_1: TextView = itemView.findViewById(R.id.tv_catgry_1)
        private var tvTitle_2: TextView = itemView.findViewById(R.id.tv_catgry_2)
        private var tvTitle_3: TextView = itemView.findViewById(R.id.tv_catgry_3)
        private var catImages: IntArray? = null
        private var catNames: Array<String?>? = null
        fun onBind(screenType: Int?) {
            when (screenType) {
                0 -> {
                    catImages = intArrayOf(
                        R.drawable.ic_ecom_discount,
                        R.drawable.ic_ecom_brand,
                        R.drawable.ic_ecom_recomd
                    )
                    catNames = arrayOf(
                        mContext?.getString(R.string.discount_product),
                        mContext?.getString(R.string.popular_brand),
                        mContext?.getString(
                            R.string.royo_recommend,
                            mContext?.getString(R.string.app_name)
                        )
                    )
                }
                5 -> {
                    catImages = intArrayOf(
                        R.drawable.ic_cnstrction_discount,
                        R.drawable.ic_cnstrction_popular,
                        R.drawable.ic_cnstrction_recomed
                    )
                    catNames = arrayOf(
                        mContext?.getString(R.string.discount_product),
                        mContext?.getString(R.string.popular_brand),
                        mContext?.getString(
                            R.string.royo_recommend,
                            mContext?.getString(R.string.app_name)
                        )
                    )
                }
                else -> {
                    catImages = intArrayOf(
                        R.drawable.ic_discount,
                        R.drawable.ic_nearby,
                        R.drawable.ic_recomended
                    )
                    catNames = arrayOf(
                        mContext?.getString(R.string.discount_product),
                        mContext?.getString(R.string.popular_brand),
                        mContext?.getString(
                            R.string.royo_recommend,
                            mContext?.getString(R.string.app_name)
                        )
                    )
                }
            }
            categryImageText(tvTitle_1, catImages?.get(0) ?: 0, catNames?.get(0) ?: "")
            categryImageText(tvTitle_2, catImages?.get(1) ?: 0, catNames?.get(1) ?: "")
            categryImageText(tvTitle_3, catImages?.get(2) ?: 0, catNames?.get(2) ?: "")
            tvTitle_1.setOnClickListener { v: View? -> mCallback?.onHomeCategory(0) }
            tvTitle_2.setOnClickListener { v: View? -> mCallback?.onHomeCategory(1) }
            tvTitle_3.setOnClickListener { v: View? -> mCallback?.onHomeCategory(2) }
        }

        private fun categryImageText(textView: TextView, catImage: Int, catName: String) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, catImage, 0, 0)
            textView.text = catName
        }

    }

    internal inner class HomeListHolder(itemView: View, type: Int) : ViewHolder(itemView) {
        private var rvHomeList: RecyclerView = itemView.findViewById(R.id.rv_home_list)
        private var rvCustomCategory: RecyclerView = itemView.findViewById(R.id.rv_custom_category)
        private var rvBannerList: RecyclerView = itemView.findViewById(R.id.rv_banner_list)
        var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        var tvSubHead: TextView = itemView.findViewById(R.id.tv_title_subhead)
        private var tvCustomTag: TextView = itemView.findViewById(R.id.tv_custom_tag)
        private var tvViewAll: TextView = itemView.findViewById(R.id.tvViewAll)
        private var ItemCnstraint: ConstraintLayout = itemView.findViewById(R.id.item_cnstraint)
        var type: Int = type

        fun onBind(dynamicName: String?) {
            itemModel = mFilterList!![adapterPosition].itemModel
            rvHomeList.visibility = View.GONE
            rvBannerList.visibility = View.VISIBLE
            tvCustomTag.visibility = View.GONE
            rvCustomCategory.visibility = View.GONE
            tvTitle.visibility = View.GONE
            tvViewAll.visibility = View.GONE
            if (type == TYPE_BRANDS) {
                if (itemModel?.brandsList?.isNotEmpty() == true) {

                    rvHomeList.visibility = View.VISIBLE
                    val listAdapter = BrandsListAdapter(itemModel?.brandsList!!)

                    rvHomeList.apply {
                        layoutManager = lytManager("horizontal")
                        adapter = listAdapter
                        // setRecycledViewPool(viewPool)
                    }

                    if (fragment != null && fragment is HomeFragment) {
                        listAdapter.settingCallback(fragment as HomeFragment)
                    } else if (fragment != null && fragment is WagonPickUp) {
                        listAdapter.settingCallback(fragment as WagonPickUp)
                    }

                    if (itemModel!!.screenType == AppDataType.Ecom.type) {
                        tvTitle.text = mContext?.getString(R.string.popular_brand)
                        tvTitle.visibility = View.VISIBLE
                        ItemCnstraint.setBackgroundColor(Color.parseColor(colorConfig.homelistBackground))
                    } else {
                        ItemCnstraint.setBackgroundColor(Color.parseColor(colorConfig.appBackground))
                    }

                    if (clientInform?.show_ecom_v2_theme == "1") {
                        tvTitle.visibility = View.VISIBLE
                    }
                }
            } else if (type == TYPE_CATEGORY || type == TYPE_TAGS) {
                if (itemModel?.categoryList != null && itemModel?.categoryList?.isNotEmpty() == true) {

                    val listAdapter = CategoryListAdapter(
                        itemModel?.categoryList
                            ?: emptyList(), itemModel!!.screenType, clientInform
                    )
                    if (itemModel?.screenType ?: 0 > AppDataType.Custom.type || BuildConfig.CLIENT_CODE == "scrubble_0566" || (clientInform?.is_wagon_app == "1" && itemModel?.screenType == AppDataType.HomeServ.type)) {
                        rvCustomCategory.layoutManager = GridLayoutManager(mContext, 2)
                        rvCustomCategory.visibility = View.VISIBLE
                        rvCustomCategory.adapter = listAdapter
                    } else {
                        if (clientInform?.app_selected_theme == "3" || clientInform?.is_skip_theme == "1") {
                            rvHomeList.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground))
                            ItemCnstraint.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground))
                        } else {
                            ItemCnstraint.setBackgroundColor(Color.parseColor(Configurations.colors.homelistBackground))
                            rvHomeList.setBackgroundColor(Color.parseColor(Configurations.colors.homelistBackground))
                        }

                        rvHomeList.visibility = View.VISIBLE
                        rvHomeList.layoutManager = lytManager("horizontal")
                        rvHomeList.adapter = listAdapter

                    }
                    tvViewAll.visibility =
                        if (clientInform?.is_skip_theme == "1" || (clientInform?.is_wagon_app == "1" && screenFlowBean?.app_type == AppDataType.Ecom.type)) View.VISIBLE else View.GONE

                    if (fragment != null) {
                        when (fragment) {
                            is CustomHomeFrag -> listAdapter.settingCallback(fragment as CustomHomeFrag)
                            is ClikatHomeFragment -> listAdapter.settingCallback(fragment as ClikatHomeFragment)
                            is PickupResturantFrag -> listAdapter.settingCallback(fragment as PickupResturantFrag)
                            is DineInResturantFrag -> listAdapter.settingCallback(fragment as DineInResturantFrag)
                            is HomeFragment, is WagonPickUp -> {

                                if (fragment is HomeFragment) {
                                    listAdapter.settingCallback(fragment as HomeFragment)
                                } else if (fragment is WagonPickUp) {
                                    listAdapter.settingCallback(fragment as WagonPickUp)
                                }

                                tvViewAll.setOnClickListener {
                                    mCallback?.onViewAllCategories(listAdapter.getCategoriesList())
                                }
                            }
                        }
                    }

                    if (clientInform?.is_skip_theme == "1" || clientInform?.dynamic_home_screen_sections == "1") {
                        tvTitle.visibility = View.VISIBLE
                        tvTitle.text =
                            if (clientInform.dynamic_home_screen_sections == "1" && !dynamicName.isNullOrEmpty()) dynamicName else
                                mContext?.getString(R.string.categories)
                        tvSubHead.visibility = if (clientInform.is_wagon_app == "1") {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                        tvSubHead.text = mContext?.getString(
                            R.string.some_of_our_best_food_categories_for_you,
                            textConfig?.product
                        )
                    } else if (clientInform?.show_ecom_v2_theme == "1") {
                        tvTitle.visibility = View.INVISIBLE
                    }


                } else {
                    rvHomeList.visibility = View.GONE
                }
            } else if (type == TYPE_BANNER) {
                if (itemModel!!.bannerList!!.isNotEmpty()) {
                    val snapHelper = PagerSnapHelper()
                    rvBannerList.layoutManager = lytManager("horizontal")
                    rvHomeList.onFlingListener = null
                    snapHelper.attachToRecyclerView(rvHomeList)
                    rvBannerList.visibility = View.VISIBLE
                    /*    if (itemModel!!.screenType == AppDataType.HomeServ.type) {
                            rvBannerList.background = ContextCompat.getDrawable(mContext!!, R.drawable.shape_home_header)
                        } else {
                            rvBannerList.background = ContextCompat.getDrawable(mContext!!, R.drawable.shape_white)
                        }*/
                    val listAdapter = BannerListAdapter(
                        itemModel?.bannerList
                            ?: listOf(), itemModel?.bannerWidth ?: 0, itemModel?.isSingleVendor
                            ?: 0, clientInform, itemModel?.screenType
                    )
                    rvBannerList.adapter = listAdapter

                    // rvBannerList.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                    if (fragment != null) {
                        if (fragment is HomeFragment) {
                            listAdapter.settingCallback(fragment as HomeFragment)
                        } else if (fragment is WagonPickUp) {
                            listAdapter.settingCallback(fragment as WagonPickUp)
                        } else if (fragment is ClikatHomeFragment)
                            listAdapter.settingCallback(fragment as ClikatHomeFragment)

                        mCallback?.onPagerScroll(listAdapter, rvBannerList)
                    }

                    if (clientInform?.app_selected_theme == "3" || clientInform?.is_skip_theme == "1")
                        rvBannerList.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground))
                    else
                        rvBannerList.setBackgroundColor(Color.parseColor(Configurations.colors.homelistBackground))
                }
            }

        }

    }

    internal inner class SpclViewHolder(private val specialBinding: ItemSpecialOfferViewBinding) :
        ViewHolder(specialBinding.root) {
        private var rvSplOffer: RecyclerView
        private var gpViewMore: Group
        private var tvCategory: TextView
        private var tvCarDesc: TextView
        private var tvViewmore: TextView
        private var ivFwd: ImageView
        fun onBind(itemModel: HomeItemModel?) {

            if (clientInform?.dynamic_home_screen_sections == "1")
                tvCategory.text = dynamicName
            else {
                tvCategory.text = when {
                    itemModel?.screenType ?: 0 > AppDataType.Custom.type -> {
                        val spannableString = SpannableString(mContext?.getString(R.string.top_deals, itemModel?.mSpecialOfferName))
                        spannableString.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            13,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannableString
                    }
                    itemModel?.specialOffers?.isNotEmpty() == true -> {
                        textConfig?.special_offers
                    }
                    itemModel?.recentProdList?.isNotEmpty() == true -> {
                        "Recently Viewed " + textConfig?.products
                    }
                    itemModel?.recentViewHistory?.isNotEmpty() == true -> {
                        mContext?.getString(R.string.recomended)
                    }
                    else -> {
                        if (clientInform?.app_selected_theme == "3")
                            mContext?.getString(R.string.top_Selling_dishes)
                        else { 
                            if (BuildConfig.CLIENT_CODE == "ghaytah_0021") {
                                if (itemModel?.screenType == 8) {
                                    mContext?.getString(R.string.popular_tag, textConfig?.products)
                                } else {
                                    mContext?.getString(R.string.popular_tag_new,mContext?.getString(R.string.products_text))
                                }
                            } else {
                                mContext?.getString(R.string.popular_tag, textConfig?.products)
                            }
                        }
                    }
                }
            }

            if (clientInform?.show_ecom_v2_theme == "1") {
                tvViewmore.text = ""
                tvViewmore.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_arrow_next,
                    0,
                    0,
                    0
                )
            }


            val supplierlist: MutableList<ProductDataBean>? = mutableListOf()

            supplierlist?.clear()

            supplierlist?.addAll(
                when {
                    itemModel?.specialOffers?.isNotEmpty() == true -> {
                        itemModel.specialOffers ?: mutableListOf()
                    }
                    itemModel?.recentProdList?.isNotEmpty() == true -> {
                        itemModel.recentProdList ?: mutableListOf()
                    }
                    itemModel?.recentViewHistory?.isNotEmpty() == true -> {
                        itemModel.recentViewHistory ?: mutableListOf()
                    }
                    else -> {
                        itemModel?.popularProdList ?: mutableListOf()
                    }
                }
            )

            gpViewMore.visibility =
                if (itemModel?.screenType == AppDataType.Ecom.type) View.GONE else View.VISIBLE

            if (!supplierlist.isNullOrEmpty()) {
                // tvTitle.visibility=View.VISIBLE
                specialBinding.root.visibility = View.VISIBLE

                val listAdapter = SpecialListAdapter(
                    supplierlist, itemModel?.screenType ?: 0,
                    itemModel?.isSingleVendor
                        ?: 0, if (itemModel?.popularProdList?.count() ?: 0 > 0 &&
                        clientInform?.laundary_service_flow == "1"
                    ) 0 else 1, clientInform, currency = currency, appUtils = appUtils
                )

                rvSplOffer.apply {
                    layoutManager = when {
                        clientInform?.show_ecom_v2_theme == "1" -> lytManager("grid")
                        itemModel?.mSpecialType == 0 -> lytManager("linear")
                        else -> lytManager("horizontal")
                    }
                    adapter = listAdapter
                    //  setRecycledViewPool(viewPool)
                }

                if (itemModel?.recentViewHistory?.isNotEmpty() == true)
                    listAdapter.isRecommended(true)

                if (itemModel?.specialOffers?.isNotEmpty() == true && itemModel.mSpecialType ?: 0 > 0 && clientInform?.app_selected_theme != "3" && clientInform?.app_selected_theme != "1") {
                    gpViewMore.visibility = View.VISIBLE
                } else {
                    gpViewMore.visibility = View.GONE
                }
                ivFwd.visibility =
                    if ((clientInform?.app_selected_theme == "3" || clientInform?.app_selected_theme == "1") && clientInform.hide_view_all != "1") View.VISIBLE else View.GONE

                when (itemModel?.screenType) {
                    AppDataType.HomeServ.type -> {
                        // specialBinding.root.setBackgroundResource(R.drawable.shape_home_header)
                    }
                    AppDataType.Ecom.type -> {
                        specialBinding.root.setBackgroundColor(Color.parseColor(colorConfig.appBackground))
                    }
                    else -> {
                        if (clientInform?.app_selected_theme == "3" || clientInform?.app_selected_theme == "1") {
                            specialBinding.root.setBackgroundColor(Color.parseColor(colorConfig.appBackground))
                        } else
                            specialBinding.root.setBackgroundColor(Color.parseColor(colorConfig.homelistBackground))
                    }
                }



                if (fragment == null) return
                when (fragment) {
                    is HomeFragment, is WagonPickUp -> {
                        if (fragment is HomeFragment) {
                            listAdapter.settingCllback(fragment as HomeFragment)
                        } else if (fragment is WagonPickUp) {
                            listAdapter.settingCllback(fragment as WagonPickUp)
                        }

                        tvViewmore.setOnClickListener { v: View? ->

                            if (itemModel?.specialOffers?.isNotEmpty() == true && clientInform?.enable_supplier_in_special_offer == "1") {
                                mCallback?.supplierViewMoreCliked(
                                    mFilterList?.get(position),
                                    30,
                                    title = tvCategory.text.toString()
                                )
                            } else {
                                mCallback?.onViewMore(
                                    tvCategory.text.toString(),
                                    (itemView.rv_spl_offer_supplier.adapter as SpecialListAdapter).getSpecialList()
                                )
                            }
                        }
                        ivFwd.setOnClickListener {
                            tvViewmore.callOnClick()
                        }
                    }
                    is CustomHomeFrag -> listAdapter.settingCllback(fragment as CustomHomeFrag)
                    is ClikatHomeFragment -> listAdapter.settingCllback(fragment as ClikatHomeFragment)
                }
                mCallback?.onSpclView(listAdapter)
            } else {
                specialBinding.root.visibility = View.GONE
            }
        }

        init {
            val view = specialBinding.root
            rvSplOffer = view.findViewById(R.id.rv_spl_offer_supplier)
            gpViewMore = view.findViewById(R.id.gp_viewmore)
            tvCategory = view.findViewById(R.id.tv_category)
            tvCarDesc = view.findViewById(R.id.tv_car_desc)
            tvViewmore = view.findViewById(R.id.tv_viewmore)
            ivFwd = view.findViewById(R.id.ivFwd)
            // this.itemView = itemView
        }
    }

    interface SupplierListCallback {
        fun onSupplierDetail(supplierBean: SupplierDataBean?)
        fun viewAllNearBy()
        fun onSpclView(specialListAdapter: SpecialListAdapter?)
        fun onFilterScreen()
        fun onSearchItem(text: String?)
        fun onHomeCategory(position: Int)
        fun onSortByClicked(tvSortBy: TextView)
        fun onViewMore(title: String?, specialList: List<ProductDataBean?>)
        fun onViewAllCategories(list: List<English>)
        fun onPagerScroll(listAdapter: BannerListAdapter, rvBannerList: RecyclerView)
        fun onSearchClickedForV2Theme()
        fun supplierViewMoreCliked(data: SupplierDataBean?, listType: Int, title: String)
        fun onBookNow(supplierData: SupplierDataBean)
        fun onListViewChanges(adapterPosition: Int, isGrid: Boolean)
        fun onFilterClicked(ivFilter: ImageView)
        fun onViewAllSupplier()
    }

    private fun lytManager(type: String): LayoutManager {
        return when (type) {
            "horizontal" -> {
                LinearLayoutManager(mContext, HORIZONTAL, false)
            }
            "grid" -> {
                GridLayoutManager(mContext, 2)
            }
            else -> LinearLayoutManager(mContext, VERTICAL, false)
        }
    }

    internal inner class ItemRestProdHolder(root: View?) : ViewHolder(root!!) {
        var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        var rvProdList: RecyclerView = itemView.findViewById(R.id.rv_timeperiod_slot)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tv_sub_title)
        fun onBind(itemModel: ProductBean?) {
            tvTitle.text = itemModel?.sub_cat_name

            if (itemModel?.detailed_sub_category != null && clientInform?.zipTheme == null) {
                tvSubTitle.visibility = View.VISIBLE
                tvSubTitle.text = itemModel.detailed_sub_category
            } else {
                tvSubTitle.visibility = View.GONE
            }

            rvProdList.layoutManager = LinearLayoutManager(mContext, VERTICAL, false)
            val adapter = ProdListAdapter(clientInform, currency)
            adapter.updateRestTime(true)
            adapter.updateParentPos(adapterPosition)
            rvProdList.adapter = adapter
            adapter.addItmSubmitList(itemModel?.value ?: mutableListOf())

            if (fragment is WagonPickUp) {
                adapter.settingCallback(fragment as WagonPickUp, appUtils)
            } else if (fragment is HomeFragment) {
                adapter.settingCallback(fragment as HomeFragment, appUtils)
            }
        }

    }

    internal inner class BannerVideoListViewHolder(root: View?) : ViewHolder(root!!) {
        var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        var vpVideos: ViewPager2 = itemView.findViewById(R.id.vpVideo)
        var indicator: CircleIndicator3 = itemView.findViewById(R.id.indicator)
        fun onBind(videoList: List<String>) {
            tvTitle.text = mContext?.getString(R.string.video_banner_title)
            val adapter = BannerVideoAdaptor(videoList, viewPager = vpVideos)
            vpVideos.adapter = adapter
            vpVideos.setPageTransformer(ZoomInTransformer())
            indicator.setViewPager(vpVideos)
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                mFilterList = if (charString.isEmpty()) {
                    mSupplierList?.map { it.copy() }?.toMutableList()
                } else {

                    val beanList: MutableList<ProductDataBean>? = mutableListOf()
                    val mSortedList: MutableList<SupplierDataBean>? = mutableListOf()

                    mSupplierList?.map { it.copy() }?.toMutableList()
                        ?.forEachIndexed { index, supplierDataBean ->
                            beanList?.clear()
                            if (supplierDataBean.viewType == SINGLE_PROD_TYPE && supplierDataBean.itemModel?.vendorProdList?.value?.isNotEmpty() == true) {
                                supplierDataBean.itemModel?.vendorProdList?.value?.forEachIndexed { index1, productDataBean ->
                                    if (productDataBean.name?.toLowerCase(DateTimeUtils.timeLocale)
                                            ?.contains(charSequence) == true
                                    ) {
                                        beanList?.add(productDataBean.copy())
                                    }

                                    if (beanList?.isNotEmpty() == true) {
                                        supplierDataBean.itemModel?.vendorProdList?.value =
                                            beanList.toMutableList()
                                        mSortedList?.add(supplierDataBean.copy())
                                    }
                                }
                            } else {
                                mSortedList?.add(supplierDataBean.copy())
                            }
                        }

                    mSortedList?.toMutableList()
                }
                val filterResults = FilterResults()
                filterResults.values = mFilterList?.toMutableList()
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                mFilterList = filterResults.values as MutableList<SupplierDataBean>?
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        private const val TYPE_SUPPLER = 0
        private const val TYPE_RECOMEND_SUPLR = 1
        private const val TYPE_SPCL_PRDT = 2
        private const val TYPE_BRANDS = 3
        private const val TYPE_CATEGORY = 4
        private const val TYPE_BANNER = 5
        private const val TYPE_APP_CATEGORY = 6
        private const val TYPE_SOCIAL = 6
        private const val TYPE_FILTER_TAG = 7
        private const val TYPE_SEARCH = 8
        private const val TYPE_SINGLE_PROD = 9
        private const val TYPE_POPULAR_LIST = 10
        const val TYPE_BEST_SELLERS_SUPLR = 11
        private const val TYPE_TAGS = 12
        const val TYPE_HORIZONTAL_SUPPLIERS = 13
        private const val TYPE_SUPPLIERS = 14
        private const val TYPE_CLIKAT_THEME_SUPPLIERS = 15
        private const val TYPE_NEAR_BY_SUPPLIERS_MAP = 16
        const val TYPE_FASTEST_DELIVERY = 17
        private const val TYPE_RECOMENDED_FOOD = 18
        private const val TYPE_HIGHEST_RATING_SUPPLIERS = 19
        private const val TYPE_NEW_RESTUARENT_SUPPLIERS = 20
        private const val TYPE_CATEGORY_WISE_SUPPLIERS = 21
        private const val TYPE_RECENT_ORDERS = 22
        private const val TYPE_BANNER_VIDEOS = 23
        private const val TYPE_WISHLIST = 24

        var BANNER_TYPE = "BannerList"
        var CATEGORY_TYPE = "CategoryList"
        var RECOMEND_TYPE = "RecomendSupplier"
        var BEST_SELLERS = "BestSellers"
        var SPL_PROD_TYPE = "SplProduct"
        var SEARCH_TYPE = "SearchProd"
        var APP_CAT_TYPE = "AppCategory"
        var SOCIAL_TYPE = "Social"
        var BRAND_TYPE = "Brands"
        var RECENT_TYPE = "RecentView"
        var FILTER_TYPE = "FilterTag"
        var TAGS_SUPPLIER_TYPE = "Tags"
        var SUPL_TYPE = "SupplierList"
        var SINGLE_PROD_TYPE = "RestProductList"
        var POPULAR_TYPE = "PopularList"
        var HORIZONTAL_SUPPLIERS = "HorizontalSuppliers"
        var FASTEST_DELIVERY_SUPPLIERS = "Fastest_delivery"
        var SORT_POPUP = ""
        var RECOMMENDED_FOOD = "RecomendedFood"
        val CLIKAT_THEME_SUPPLIERS = "clikat_suppliers"
        val HIGEST_RATING_SUPPLIERS = "higest_rating_suppliers"
        val NEW_RESTUARENT_SUPPLIERS = "new_suppliers"
        val CATEGORY_WISE_SUPPLIERS = "category_wise_suppliers"
        val NEAR_BY_SPPLIERS = "NearBySuppliers"
        val RECENT_ORDERS = "RecentOrders"
        val BANNER_VIDEOS = "BannerVideos"
        val WISHLIST_TYPE = "WishList"
    }

}