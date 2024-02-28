package com.codebrew.clikat.module.searchProduct

import android.app.Activity
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.codebrew.clikat.BR
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.DialogsUtil
import com.codebrew.clikat.app_utils.ProdUtils
import com.codebrew.clikat.app_utils.extension.afterTextChanged
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.base.PagingResult
import com.codebrew.clikat.data.*
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.Currency
import com.codebrew.clikat.data.model.api.ProductData
import com.codebrew.clikat.data.model.others.FilterInputModel
import com.codebrew.clikat.databinding.FragmentCompareProdutcsBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.CartList
import com.codebrew.clikat.modal.database.SearchCatListModel
import com.codebrew.clikat.modal.database.SearchCategoryModel
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.modal.other.SettingModel.DataBean.BookingFlowBean
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.addon_quant.SavedAddon
import com.codebrew.clikat.module.filter.BottomSheetFragment
import com.codebrew.clikat.module.home_screen.listeners.OnSortByListenerClicked
import com.codebrew.clikat.module.login.LoginActivity
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.product.product_listing.adapter.ProductListingAdapter
import com.codebrew.clikat.module.product.product_listing.adapter.ProductListingAdapter.ProductCallback
import com.codebrew.clikat.module.product_addon.AddonFragment
import com.codebrew.clikat.module.product_detail.ProductDetails
import com.codebrew.clikat.module.searchProduct.adapter.ResturantListAdapter
import com.codebrew.clikat.module.searchProduct.adapter.ResturantListener
import com.codebrew.clikat.module.searchProduct.adapter.SuggestionListAdapter
import com.codebrew.clikat.module.searchProduct.adapter.SuggestionListAdapter.Searchcallback
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.StaticFunction.getCartQuantity
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.Utils
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.realm.OrderedRealmCollection
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_compare_produtcs.*
import kotlinx.android.synthetic.main.layout_bottom_cart.*
import kotlinx.android.synthetic.main.nothing_found.*
import kotlinx.android.synthetic.main.toolbar_app.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import java.lang.reflect.Type
import javax.inject.Inject

/*
 * Created by cbl80 on 8/7/16.
 */
class SearchFragment : BaseFragment<FragmentCompareProdutcsBinding, SearchViewModel>(),
    Searchcallback, ProductCallback, DialogListener, View.OnClickListener,
    SearchNavigator, AddonFragment.AddonCallback, AdapterView.OnItemSelectedListener, OnSortByListenerClicked {


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var prodUtils: ProdUtils


    private lateinit var mBinding: FragmentCompareProdutcsBinding


    private lateinit var viewModel: SearchViewModel
    private var selectedCurrency: Currency? = null

    private val listAllData = mutableListOf<ProductDataBean>()
    private var productAdapter: ProductListingAdapter? = null
    private var resturantListAdapter: ResturantListAdapter? = null
    private var suggestionListAdapter: SuggestionListAdapter? = null
    private var varientData: FilterVarientData? = null
    private var screenType = false
    private val selectedColor by lazy { Color.parseColor(Configurations.colors.tabSelected) }
    private val unselectedColor by lazy { Color.parseColor(Configurations.colors.tabUnSelected) }
    private var realm: Realm? = null
    private var bookingFlowBean: BookingFlowBean? = null
    private var parentPos: Int = 0
    private var screenFlowBean: ScreenFlowBean? = null
    private var clientInform: SettingModel.DataBean.SettingData? = null
    private var catId = 0
    private var subCat: List<Int>? = null
    private var mSearchType = ""

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    private val colorConfig by lazy { Configurations.colors }
    private var inputModel: FilterInputModel = FilterInputModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel.navigator = this

        productListObserver()

        supplierObserver()

        catId = if (dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().isNotEmpty()) {
            dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().toInt()
        } else {
            0
        }

        val subCatId = dataManager.getKeyValue(PrefenceConstants.SUB_CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString()

        subCat = if (subCatId.isNotEmpty()) {
            val listType: Type = object : TypeToken<List<Int?>?>() {}.type
            val jsonObj = JSONArray(subCatId)
            Gson().fromJson(jsonObj.toString(), listType) as List<Int>
        } else {
            listOf()
        }

        if (arguments != null && arguments?.containsKey("screenType") == true) {
            screenType = arguments?.getBoolean("screenType", false)!!
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding
        mBinding.color = colorConfig
        mBinding.drawables = Configurations.drawables
        mBinding.strings = textConfig

        EventBus.getDefault().register(this)

        screenFlowBean = dataManager.getGsonValue(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
        bookingFlowBean = dataManager.getGsonValue(DataNames.BOOKING_FLOW, BookingFlowBean::class.java)
        clientInform = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        selectedCurrency = dataManager.getGsonValue(PrefenceConstants.CURRENCY_INF, Currency::class.java)

        spinner_view.visibility = if (clientInform?.search_by == "2") {
            View.VISIBLE
        } else {
            View.GONE
        }
        searchView?.hint = textConfig?.what_are_u_looking_for

        if (clientInform?.show_ecom_v2_theme == "1") {
            toolbar.elevation = 0f
            search_lyt.cardElevation = 0f

            searchContainer?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.background_toolbar_bottom_radius)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                searchContainer.background.colorFilter = BlendModeColorFilter(Color.parseColor(colorConfig.toolbarColor), BlendMode.SRC_ATOP)
            }

            searchView.background = ContextCompat.getDrawable(requireActivity(), R.drawable.search_home_radius_background)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                searchView.background.colorFilter = BlendModeColorFilter(Color.parseColor(colorConfig.search_background), BlendMode.SRC_ATOP)
            }

            searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0)
            searchView?.setTextColor(Color.parseColor(colorConfig.search_textcolor))
            searchView?.setHintTextColor(Color.parseColor(colorConfig.search_textcolor))

            iv_search?.visibility = View.GONE
            dividerView?.visibility = View.GONE

            val params = searchView?.layoutParams as ConstraintLayout.LayoutParams
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            val margin = StaticFunction.pxFromDp(16, requireActivity())
            params.setMargins(margin, 0, margin / 2, margin)
            searchView?.layoutParams = params
        } else {
            searchView.background = ContextCompat.getDrawable(requireActivity(), R.drawable.rec_4_transparent)
            searchView?.setTextColor(ContextCompat.getColor(requireActivity(), android.R.color.darker_gray))
            searchView?.setHintTextColor(ContextCompat.getColor(requireActivity(), android.R.color.darker_gray))
        }

        setData()
        clickListner()

        searchView.afterTextChanged {
            if (it.trim().isNotEmpty()) {
                iv_search.isEnabled = true
            } else {
                varientData = null
                listAllData.clear()
                iv_search.isEnabled = false
                fetchpreviousResult()
            }
        }

        // noData.visibility=View.GONE

        searchView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Your piece of code on keyboard search click
                if (searchView.text.trim().isNotEmpty())
                    iv_search.callOnClick()
                true
            } else false
        }


        tb_title.text = getString(R.string.search, appUtils.loadAppConfig(0).strings?.product)
        tb_back.setOnClickListener {
            hideKeyboard()
            Navigation.findNavController(requireView()).popBackStack()
        }

        showBottomCart()

        onRecyclerViewScrolled()

        if (BuildConfig.CLIENT_CODE == "bookmytable_0882") {
            mSearchType = "resturant"
            tb_title.text = getString(R.string.search, appUtils.loadAppConfig(0).strings?.supplier)
        } else
            tb_title.text = getString(R.string.search, appUtils.loadAppConfig(0).strings?.product)

        if (arguments != null && arguments?.containsKey("searchText") == true) {
            searchView?.setText(arguments?.getString("searchText"))
            if (searchView?.text.toString().trim().isNotEmpty())
                iv_search?.callOnClick()
        }
    }

    private fun productListObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<PagingResult<ProductData>> { resource ->
            // Update the UI, in this case, a TextView.
            if (resource != null) {
                if (resource.isFirstPage) {
                    listAllData.clear()
                }
                handleProductList(resource.result, resource.isFirstPage)
            }
        }

        viewModel.productLiveData.observe(this, catObserver)

    }


    private fun supplierObserver() {
        // Create the observer which updates the UI.
        val supplierObserver = Observer<List<SupplierDataBean>> { resource ->
            resource.let {
                tv_filter?.visibility = if (clientInform?.hide_filter_on_search == "1") View.GONE else View.VISIBLE
                updateSupplierData(it)
            }
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.supplierLiveData.observe(this, supplierObserver)
    }

    private fun updateSupplierData(resource: List<SupplierDataBean>?) {
        mBinding.searchHistory = resource?.count() ?: 0 == 0

        checkAdapter(resturantListAdapter, false)

        resturantListAdapter?.notifyDataSetChanged()
        viewModel.setIsSearchHist(true)
        mBinding.executePendingBindings()
        AppConstants.SEARCH_OPTION = SearchType.TYPE_RESTU.type
        resturantListAdapter?.submitMessageList(resource)
    }


    private fun handleProductList(resource: ProductData?, isFirstPage: Boolean) {

        mBinding.searchHistory = resource?.product?.count() == 0

        AppConstants.SEARCH_OPTION = SearchType.TYPE_PROD.type
        viewModel.supplierLiveData.value = null
        hideKeyboard()

        checkAdapter(productAdapter, true)

        changeProductList(resource?.product)
        listAllData.addAll(resource?.product ?: emptyList())
        viewModel.setIsList(resource?.product?.count() ?: 0)
        viewModel.setIsSearchHist(false)
        iv_list_view.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
        iv_grid_view.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)
        tv_filter?.visibility = if (clientInform?.hide_filter_on_search == "1") View.GONE else View.VISIBLE
        productAdapter?.notifyDataSetChanged()
    }


    private fun clickListner() {
        iv_search.setOnClickListener(this)
        iv_grid_view.setOnClickListener(this)
        tv_filter.setOnClickListener(this)
        iv_list_view.setOnClickListener(this)

        if (clientInform?.show_ecom_v2_theme == "1") {
            iv_grid_view.visibility = View.INVISIBLE
            iv_list_view.visibility = View.INVISIBLE
            tv_view_product.visibility = View.INVISIBLE
        }
        if (clientInform?.is_skip_theme == "1") {
            tvTitleOoPs?.visibility = View.VISIBLE
            ivPlaceholder?.setImageResource(R.drawable.ic_graphic)
            tvText?.text = getString(R.string.nothing_found_try_again)
            tvText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
    }

    private fun fetchpreviousResult() {
        viewModel.setIsSearchHist(true)
        AppConstants.SEARCH_OPTION = SearchType.TYPE_PROD.type
        viewModel.setIsList(realm?.where(SearchCatListModel::class.java)?.findFirst()?.itemList?.size?.plus(1)
            ?: 1)
        tv_filter?.visibility = View.GONE
        if (suggestionListAdapter == null || recyclerview?.adapter !== suggestionListAdapter) {

            suggestionListAdapter = SuggestionListAdapter(realm?.where(SearchCatListModel::class.java)?.findFirst()?.itemList)
            suggestionListAdapter?.settingcallback(this)
            checkAdapter(suggestionListAdapter, false)
        } else suggestionListAdapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILTERDATA) {
            if (resultCode == Activity.RESULT_OK) {
                listAllData.clear()
                if (data!!.hasExtra("varientData")) {
                    varientData = data.getParcelableExtra("varientData")
                }
                if (data.hasExtra("result")) {
                    listAllData.addAll(data.getParcelableArrayListExtra("result") ?: arrayListOf())
                    changeProductList(listAllData)

                    viewModel.setIsSearchHist(false)
                    AppConstants.SEARCH_OPTION = SearchType.TYPE_PROD.type
                    viewModel.setIsList(listAllData.size)

                    if (listAllData.isNotEmpty()) {
                        checkAdapter(productAdapter, clientInform?.show_ecom_v2_theme != "1")
                    }
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {

                // viewModel.setIsSearchHist(false)
                viewModel.setIsList(listAllData.size)
            }
            productAdapter?.notifyDataSetChanged()
        } else if (requestCode == AppConstants.REQUEST_WISH_PROD && resultCode == Activity.RESULT_OK) {

            if (dataManager.getCurrentUserLoggedIn()) {
                viewModel.markFavProduct(listAllData.get(parentPos).product_id, if (listAllData.get(parentPos).is_favourite == 0) 1 else 0)
            }
        }else if(requestCode == AppConstants.REQUEST_WISH_PROD && resultCode == Activity.RESULT_CANCELED)
        {
            when (listAllData[parentPos].is_favourite) {
                1 -> {
                    listAllData[parentPos].is_favourite = 1
                }
                else -> {
                    listAllData[parentPos].is_favourite = 0
                }
            }
            productAdapter?.notifyItemChanged(parentPos)

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(filterVarientData: FilterResponseEvent) {
        varientData = filterVarientData.filterModel
        listAllData.clear()
        viewModel.setIsSearchHist(false)
        AppConstants.SEARCH_OPTION = SearchType.TYPE_PROD.type
        viewModel.setIsList(filterVarientData.productlist.size)
        if (filterVarientData.productlist.isNotEmpty()) {
            listAllData.addAll(filterVarientData.productlist)
            changeProductList(listAllData)

            if (listAllData.isNotEmpty()) {
                checkAdapter(productAdapter, true)
            }
        }
        productAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        if (productAdapter?.itemCount ?: 0 > 0) {
            mBinding.searchHistory = true
        }

        if (AppConstants.SEARCH_OPTION == SearchType.TYPE_PROD.type) {
            if (viewModel.productLiveData.value != null) {
                spinner_search.setSelection(1)
                viewModel.setIsSearchHist(false)
                AppConstants.SEARCH_OPTION = SearchType.TYPE_PROD.type
                checkAdapter(productAdapter, true)
            }

            changeProductList(listAllData)
            if (productAdapter != null) {
                productAdapter?.notifyDataSetChanged()
            }
        } else {
            if (viewModel.supplierLiveData.value != null) {
                updateSupplierData(viewModel.supplierLiveData.value)
            }
        }


    }

    private fun setData() {
        val screenFlowBean = Prefs.with(activity).getObject(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
        /* tv_filter.visibility = if (screenFlowBean.app_type == AppDataType.Food.type) View.INVISIBLE else View.VISIBLE*/
        iv_list_view.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
        iv_grid_view.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)
        // Create the Realm instance
        realm = Realm.getDefaultInstance()
        productAdapter = ProductListingAdapter(activity
            ?: requireContext(), listAllData, appUtils, clientInform?.show_ecom_v2_theme, selectedCurrency)

        resturantListAdapter = ResturantListAdapter(ResturantListener {

            val bundle = bundleOf("supplierId" to it.id,
                "branchId" to it.supplier_branch_id,
                "deliveryType" to Utils.getDeliveryType(it.self_pickup ?: 0),
                "categoryId" to it.category_id,
                "title" to it.name)

            if (clientInform?.show_supplier_detail != null && clientInform?.show_supplier_detail == "1") {

                navController(this@SearchFragment).navigate(R.id.action_supplierDetail, bundle)
            } else if (clientInform?.app_selected_template != null && clientInform?.app_selected_template == "1") {
                navController(this@SearchFragment)
                    .navigate(R.id.action_searchFragment_to_restaurantDetailNewFrag, bundle)
            } else {
                navController(this@SearchFragment)
                    .navigate(R.id.action_searchFragment_to_restaurantDetailFrag, bundle)
            }
        }, appUtils)

        mBinding.searchHistory = (viewModel.productCount.get() == 0 && viewModel.supplierCount.get() == 0)

        if (suggestionListAdapter == null) {
            fetchpreviousResult()
        } else {
            viewModel.setIsSearchHist(false)
            // AppConstants.SEARCH_OPTION = SearchType.TYPE_PROD.type
            recyclerview.layoutManager = setLayoutManager(clientInform?.show_ecom_v2_theme != "1")
            checkAdapter(productAdapter, true)
        }
        productAdapter?.settingCallback(this)


        val mList = arrayListOf<String>()

        //  0 product 1 supplier 2 both
        clientInform?.search_by.let {

            when (it?.toInt()) {
                SearchDataType.Product.searchType -> {
                    mList.add(appUtils.loadAppConfig(0).strings?.product ?: "")
                }
                SearchDataType.Supplier.searchType -> {
                    mList.add(appUtils.loadAppConfig(0).strings?.supplier ?: "")
                }
                else -> {
                    mList.add(appUtils.loadAppConfig(0).strings?.product ?: "")
                    mList.add(appUtils.loadAppConfig(0).strings?.supplier ?: "")
                }
            }
        }


        ArrayAdapter(
            activity ?: requireContext(), android.R.layout.simple_spinner_item,
            mList
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner_search.adapter = adapter
        }

        spinner_search.onItemSelectedListener = this

    }

    override fun onDestroyView() {
        super.onDestroyView()
        realm?.close()

        //  varientData = FilterVarientData()
        EventBus.getDefault().unregister(this)
    }

    override fun getSearch(keyword: String) {
        searchView?.setText(keyword)
        if (mSearchType == "resturant") {
            if (!isNetworkConnected) return
            viewModel.getSupplierList(searchView.text.toString().trim(), categoryId = catId.toString())
        } else {
            checkAdapter(productAdapter, true)
            setFilterData(keyword)
        }
    }

    override fun clearHistory() {
        realm?.executeTransactionAsync { realm: Realm ->
            // realm.deleteAll();
            val data: OrderedRealmCollection<SearchCategoryModel>? = realm.where(SearchCatListModel::class.java).findFirst()!!.itemList
            // Otherwise it has been deleted already.
            data?.deleteAllFromRealm()
        }
        fetchpreviousResult()
    }


    private fun setLayoutManager(type: Boolean): LayoutManager {
        return if (type) GridLayoutManager(activity, 2) else LinearLayoutManager(activity)
    }

    private fun setFilterData(keyword: String) {
        var low_to_high = 1
        var popularity = 0
        if (varientData != null) {
            when (varientData!!.sortBy) {
                "Price: Low to High" -> low_to_high = 1
                "Price: High to Low" -> low_to_high = 0
                "Popularity" -> popularity = 1
            }
        }

        if (varientData != null) {
            inputModel.subCategoryId?.addAll(varientData!!.subCatId)
            inputModel.max_price_range = varientData!!.maxPrice.toString()
            inputModel.min_price_range = varientData!!.minPrice.toString()
            inputModel.variant_ids?.addAll(varientData!!.varientID)
            // inputModel.setSupplier_ids("");
            inputModel.brand_ids?.addAll(varientData!!.brandId)
            inputModel.is_availability = (if (varientData!!.isAvailability) "1" else "0")
            inputModel.is_discount = (if (varientData!!.isDiscount) "1" else "0")
        } else {
            inputModel = FilterInputModel()
            inputModel.is_availability = 0.toString()
            inputModel.is_discount = 0.toString()
            inputModel.max_price_range = 100000.toString()
            inputModel.min_price_range = 0.toString()
        }

        inputModel.subCategoryId?.addAll(subCat ?: listOf())

        inputModel.languageId = getLanguage(activity).toString()
        val mLocUser = dataManager.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java)

        if (mLocUser != null) {
            inputModel.latitude = mLocUser.latitude ?: ""
            inputModel.longitude = mLocUser.longitude ?: ""
        }
        /*if (catId != 0) {
            inputModel.categoryId = catId
        }*/

        inputModel.low_to_high = low_to_high.toString()
        inputModel.is_popularity = popularity
        if (!keyword.isEmpty()) {
            inputModel.product_name = keyword
        }

        inputModel.need_agent = if (screenFlowBean?.app_type == AppDataType.HomeServ.type) {
            1
        } else {
            0
        }

        callApi(inputModel, true)
    }


    override fun addToCart(position: Int, agentType: Boolean) {

        parentPos = position
        /*check service existance*/


        // if vendor status ==0 single vendor && vendor status==1 multiple vendor
        if (bookingFlowBean?.vendor_status == 0 && appUtils.checkVendorStatus(listAllData[position].supplier_id, vendorBranchId = listAllData[position].supplier_branch_id, branchFlow = clientInform?.branch_flow)) {

            mDialogsUtil.openAlertDialog(activity
                ?: requireContext(), getString(R.string.clearCart, appUtils.loadAppConfig(0).strings?.supplier
                ?: "", appUtils.loadAppConfig(0).strings?.proceed ?: ""), "Yes", "No", this)
        } else {
            if (listAllData[position].adds_on?.isNotEmpty() == true) {
                val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

                if (appUtils.checkProdExistance(listAllData[position].product_id)) {
                    val savedProduct = cartList?.cartInfos?.filter {
                        it.supplierId == listAllData[position].supplier_id && it.productId == listAllData[position].product_id
                    } ?: emptyList()

                    SavedAddon.newInstance(listAllData[position], listAllData[position].self_pickup
                        ?: -1, savedProduct, this).show(childFragmentManager, "savedAddon")
                } else {
                    AddonFragment.newInstance(listAllData[position], listAllData[position].self_pickup
                        ?: -1, this).show(childFragmentManager, "addOn")
                }

            } else {
                if (appUtils.checkBookingFlow(requireContext(), listAllData[position].product_id
                        ?: 0, this)) {
                    addCart(position)
                }
            }
        }
    }


    private fun addCart(position: Int) {

        val mProduct = listAllData[position]

        if (mProduct.is_question == 1 && prodUtils.addItemToCart(mProduct) != null) {
            //  mProduct.prod_quantity==0
            val bundle = bundleOf("productBean" to mProduct, "is_Category" to false,
                "categoryId" to mProduct.detailed_sub_category_id, "subCategoryId" to mProduct.category_id)
            navController(this@SearchFragment).navigate(R.id.action_searchFragment_to_questionFrag, bundle)
        } else {
            mProduct.type = screenFlowBean?.app_type
            mProduct.apply { prodUtils.addItemToCart(mProduct) }
            productAdapter?.notifyItemChanged(position)
            productAdapter?.notifyDataSetChanged()

            showBottomCart()
        }
    }


    override fun removeToCart(position: Int) {

        val mProduct = listAllData[position]

        if (mProduct.prod_quantity ?: 0f < 0) return

        parentPos = position

        if (mProduct.adds_on?.isNotEmpty() == true) {
            val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

            val savedProduct = cartList?.cartInfos?.filter {
                it.supplierId == mProduct.supplier_id && it.productId == mProduct.product_id
            } ?: emptyList()

            SavedAddon.newInstance(mProduct, mProduct.self_pickup
                ?: -1, savedProduct, this).show(childFragmentManager, "savedAddon")

        } else {

            listAllData[position] = mProduct.apply { prodUtils.removeItemToCart(mProduct) }
            productAdapter?.notifyItemChanged(position)
        }

        showBottomCart()
    }

    override fun productDetail(bean: ProductDataBean?) {

        if (clientInform?.product_detail != null && clientInform?.product_detail == "0") return

        val bundle = Bundle()
        bundle.putInt("productId", bean?.product_id ?: 0)
        bundle.putString("title", bean?.name)
        bundle.putInt("categoryId", bean?.category_id ?: 0)
        bundle.putInt("deliveryType", bean?.self_pickup ?: 0)

        val screenFlowBean = Prefs.with(activity).getObject(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)

        if (screenFlowBean?.app_type == AppDataType.Food.type) {

            bundle.putInt("supplierId", bean?.supplier_id ?: 0)
            bundle.putInt("branchId", bean?.supplier_branch_id ?: 0)
            bundle.putString("deliveryType", Utils.getDeliveryType(bean?.self_pickup ?: 0))

            if (clientInform?.show_supplier_detail != null && clientInform?.show_supplier_detail == "1") {

                navController(this@SearchFragment).navigate(R.id.action_supplierDetail, bundle)
            } else if (clientInform?.app_selected_template != null && clientInform?.app_selected_template == "1") {
                navController(this@SearchFragment)
                    .navigate(R.id.action_searchFragment_to_restaurantDetailNewFrag, bundle)
            } else {
                navController(this@SearchFragment)
                    .navigate(R.id.action_searchFragment_to_restaurantDetailFrag, bundle)
            }
        } else {
            if (clientInform?.show_ecom_v2_theme == "1") {
                navController(this@SearchFragment)
                    .navigate(R.id.action_searchFragment_to_productDetailsV2, ProductDetails.newInstance(bean, 0, false))
            } else {
                navController(this@SearchFragment)
                    .navigate(R.id.action_searchFragment_to_productDetails, ProductDetails.newInstance(bean, 0, false))
            }
        }

    }

    private fun changeProductList(product: List<ProductDataBean>?) {

        product?.mapIndexed { index, productDataBean ->
            productDataBean.prod_quantity = getCartQuantity(activity, productDataBean.product_id)

            //for fixed price

            productDataBean.let {
                prodUtils.changeProductList(false, productDataBean, clientInform)
            }
        }
    }

    override fun publishResult(count: Int) {

        viewModel.setIsList(count)
    }

    override fun addtoWishList(adapterPosition: Int, status: Int?, productId: Int?) {

        parentPos = adapterPosition

        if (dataManager.getCurrentUserLoggedIn()) {
            if (isNetworkConnected) {

                viewModel.markFavProduct(listAllData[parentPos].product_id, if (listAllData[parentPos].is_favourite == 0) 1 else 0)
            }
        } else {
            startActivityForResult(Intent(activity, LoginActivity::class.java), AppConstants.REQUEST_WISH_PROD)
        }
    }

    override fun bookNow(adapterPosition: Int, bean: ProductDataBean) {
        //do nothing
    }

    override fun onSucessListner() {

        appUtils.clearCart()

        listAllData.map {
            it.prod_quantity = 0f
        }
        productAdapter?.notifyDataSetChanged()

        showBottomCart()
    }

    override fun onErrorListener() {}

    companion object {
        private const val FILTERDATA = 788
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.iv_search -> {
                if (searchView.text.toString().isEmpty()) return

                hideKeyboard()
                getSearch(searchView.text.toString().trim())
                realm?.executeTransaction { realm: Realm? -> SearchCategoryModel.create(realm, searchView?.text.toString().trim()) }

            }

            R.id.iv_grid_view -> {
                viewModel.setIsSearchHist(false)
                AppConstants.SEARCH_OPTION = SearchType.TYPE_PROD.type
                iv_list_view.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
                iv_grid_view.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)
                checkAdapter(productAdapter, viewType = true, isAdapterRefresh = true)
            }
            R.id.iv_list_view -> {
                viewModel.setIsSearchHist(false)
                AppConstants.SEARCH_OPTION = SearchType.TYPE_PROD.type
                iv_list_view.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)
                iv_grid_view.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
                checkAdapter(productAdapter, viewType = false, isAdapterRefresh = true)
            }
            R.id.tv_filter -> {
                ///  productAdapter.settingLayout(false);
                //  recyclerview.setAdapter(productAdapter);
                if (mSearchType == "resturant") {
                    if (searchView.text.toString().isEmpty()) return

                    hideKeyboard()
                    openPrepTimeDialog()
                } else {
                    val bottomSheetFragment = BottomSheetFragment()
                    val bundle = Bundle()
                    if (varientData != null) {
                        bundle.putParcelable("varientData", varientData)
                    }
                    bundle.putString("product_name", searchView!!.text.toString())
                    bottomSheetFragment.arguments = bundle
                    bottomSheetFragment.show(childFragmentManager, "bottom_sheet")
                }
            }
        }
    }

    private fun openPrepTimeDialog() {
        appUtils.showPrepTimeDialog(requireContext(), this)
    }

    override fun onPrepTimeSelected(filters: FiltersSupplierList) {
        if (isNetworkConnected)
            viewModel.getSupplierList(searchView.text.toString().trim(),
                filters, categoryId = catId.toString())
    }


    private fun showBottomCart() {

        val appCartModel = appUtils.getCartData(clientInform, selectedCurrency)

        bottom_cart.visibility = View.VISIBLE

        if (appCartModel.cartAvail) {

            tv_total_product.text = getString(R.string.total_item_tag, Utils.getDecimalPointValue(clientInform, appCartModel.totalCount))
            tv_total_price.text = getString(R.string.total).plus(" ").plus(AppConstants.CURRENCY_SYMBOL).plus(" ").plus(appCartModel.totalPrice)


            if (appCartModel.supplierName.isNotEmpty() && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                tv_supplier_name.visibility = View.VISIBLE
                tv_supplier_name.text = getString(R.string.supplier_tag, appCartModel.supplierName)
            } else {
                tv_supplier_name.visibility = View.GONE
            }

            bottom_cart.setOnClickListener {
                if (clientInform?.show_ecom_v2_theme == "1") {
                    navController(this@SearchFragment).navigate(R.id.action_searchFragment_to_cartV2)
                } else {
                    navController(this@SearchFragment).navigate(R.id.action_searchFragment_to_cart)
                }
            }
        } else {
            bottom_cart.visibility = View.GONE
        }

    }


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_compare_produtcs
    }

    override fun getViewModel(): SearchViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
        return viewModel
    }

    override fun onFavStatus() {

        listAllData.map {

            if (it.product_id == listAllData[parentPos].product_id) {
                it.is_favourite = if (it.is_favourite == 0) 1 else 0
            }
        }

        productAdapter?.notifyItemChanged(parentPos)
    }

    override fun onErrorOccur(message: String) {
        mBinding.root.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onAddonAdded(productModel: ProductDataBean) {
        if (productModel.adds_on?.isNullOrEmpty() == false && appUtils.checkProdExistance(productModel.product_id)) {
            val cartList: CartList? = appUtils.getCartList()
            productModel.prod_quantity = (cartList?.cartInfos?.filter { productModel.product_id == it.productId }?.sumByDouble { it.quantity.toDouble() }
                ?: 0.0).toFloat()
        }

        listAllData[parentPos] = productModel

        productAdapter?.notifyItemChanged(parentPos)
        productAdapter?.notifyDataSetChanged()

        showBottomCart()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if (position == 1) {
            recyclerview.layoutManager = setLayoutManager(false)
            listAllData.clear()
            mSearchType = "resturant"
            viewModel.setIsSearchHist(true)
        } else {
            mSearchType = "product"
            checkAdapter(productAdapter, false)

            if (viewModel.supplierLiveData.value?.count() == 0 && viewModel.productLiveData.value?.result?.product?.count() == 0 || (viewModel.supplierLiveData.value == null && viewModel.productLiveData.value == null) && searchView?.text.toString().isEmpty()) {
                fetchpreviousResult()
            }

        }
    }

    private fun checkAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?, viewType: Boolean, isAdapterRefresh: Boolean = false) {
        if (recyclerview.adapter !== adapter || isAdapterRefresh) {
            recyclerview.layoutManager = setLayoutManager(viewType)
            productAdapter?.settingLayout(viewType)
            recyclerview.adapter = adapter
        }
    }

    private fun callApi(inputModel: FilterInputModel, isFirstPage: Boolean) {
        if (isNetworkConnected) {
            viewModel.getProductList(inputModel, isFirstPage)
        }
    }

    private fun onRecyclerViewScrolled() {
        recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val isPagingActive = viewModel.validForPaging()

                if (recyclerview.adapter is SuggestionListAdapter || recyclerview.adapter is ResturantListAdapter || varientData != null) return

                if (recyclerView.layoutManager is LinearLayoutManager && !recyclerView.canScrollVertically(1) && isPagingActive) {
                    callApi(inputModel, false)
                } else if (recyclerView.layoutManager is GridLayoutManager) {

                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.findLastCompletelyVisibleItemPosition() + 1

                    if (visibleItemCount == layoutManager.itemCount && isPagingActive)
                        callApi(inputModel, false)
                }
            }
        })
    }

    override fun onItemSelected(type: Int) {

    }


}