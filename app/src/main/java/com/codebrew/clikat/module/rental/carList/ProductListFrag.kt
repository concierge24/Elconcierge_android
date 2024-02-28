package com.codebrew.clikat.module.rental.carList

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.DateTimeUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.RentalDataType
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.others.HomeRentalParam
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.ProductListFragmentBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.other.FilterInputNew
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.module.agent_listing.AgentListFragmentDirections
import com.codebrew.clikat.module.rental.boat_rental.ChooseSlotDirections
import com.codebrew.clikat.module.rental.carList.adapter.ProdListAdapter
import com.codebrew.clikat.module.rental.carList.adapter.ProdListener
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.product_list_fragment.*
import java.util.*
import javax.inject.Inject


class ProductListFrag : BaseFragment<ProductListFragmentBinding, ProductListViewModel>(), ProductListNavigator, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var dateTimeUtils: DateTimeUtils

    private lateinit var viewModel: ProductListViewModel
    private var mBinding: ProductListFragmentBinding? = null
    lateinit var mAdapter: ProdListAdapter
    lateinit var mHomeParam: HomeRentalParam


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.product_list_fragment
    }

    override fun getViewModel(): ProductListViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(ProductListViewModel::class.java)
        return viewModel
    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this
        rentalObserver()


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding

        viewDataBinding.color = Configurations.colors

        intializeLyt()

        if (viewModel.rentalDataLiveData.value?.isNullOrEmpty() == false) {
            updateRentalData(viewModel.rentalDataLiveData.value)
        } else {
            callApi()
        }

    }

    private fun callApi() {
        if (isNetworkConnected) {
            if (arguments == null) return

            if (arguments?.containsKey("intputData") == true) {
                mHomeParam = arguments?.getParcelable("intputData")
                        ?: HomeRentalParam()

                ed_adrs.text = if (mHomeParam.mRentalType == RentalDataType.Hourly) {
                    "${mHomeParam.source_port_id?.name} - ${mHomeParam.destination_port_id?.name}"
                } else {
                    mHomeParam.from_address
                }

                val catId = if (prefHelper.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().isNotEmpty()) {
                    prefHelper.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().toInt()
                } else {
                    0
                }

                val mFilterParam = FilterInputNew()
                mFilterParam.languageId = StaticFunction.getLanguage(activity).toString()
                mFilterParam.subCategoryId = emptyList()
                mFilterParam.supplier_ids = emptyList()
                mFilterParam.variant_ids = emptyList()
                mFilterParam.brand_ids = emptyList()
                mFilterParam.low_to_high = "0"
                mFilterParam.categoryId = catId
                mFilterParam.is_availability = "1"
                mFilterParam.max_price_range = "100000"
                mFilterParam.min_price_range = "0"
                //  mFilterParam.offset=1
                //mFilterParam.limit=10
                mFilterParam.is_discount = "0"
                mFilterParam.is_popularity = 1
                mFilterParam.product_name = ""
                mFilterParam.zone_offset = dateTimeUtils.getTimeOffset()
                mFilterParam.latitude = (mHomeParam.from_latitude ?: 0.0).toString()
                mFilterParam.longitude = (mHomeParam.from_longitude ?: 0.0).toString()
                mFilterParam.booking_to_date = mHomeParam.booking_to_date
                mFilterParam.booking_from_date = mHomeParam.booking_from_date
                mFilterParam.need_agent = mHomeParam.driveType ?: 0
                if (mHomeParam.mRentalType == RentalDataType.Hourly) {
                    if (mHomeParam.source_port_id != null) {
                        mFilterParam.source_port_id = mHomeParam.source_port_id?.id
                    }

                    if (mHomeParam.destination_port_id != null) {
                        mFilterParam.destination_port_id = mHomeParam.destination_port_id?.id
                    }

                    mFilterParam.is_boat = 1
                }

                viewModel.getRentalList(mFilterParam)
            }
        }
    }

    private fun intializeLyt() {


        refreshLayout.setOnRefreshListener(this)
        mAdapter = ProdListAdapter()

        mAdapter.settingCallback(ProdListener {

            if (mHomeParam.mRentalType==RentalDataType.Hourly)
            {
                val action = ProductListFragDirections.actionProductListFragToChooseSlot(mHomeParam,it)
                navController(this@ProductListFrag).navigate(action)
            }else
            {
                val bundle = bundleOf("prodData" to it,
                    "rentalparam" to mHomeParam)
                navController(this@ProductListFrag).navigate(R.id.action_productListFrag_to_carDetailFrag, bundle)
            }
        })

        rv_prod_list.adapter = mAdapter

        iv_back.setOnClickListener {
            navController(this@ProductListFrag).popBackStack()
        }

    }


    private fun rentalObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<MutableList<ProductDataBean>> { resource ->
            updateRentalData(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.rentalDataLiveData.observe(this, catObserver)
    }

    private fun updateRentalData(resource: MutableList<ProductDataBean>?) {

        val updatedList = if (mHomeParam.mRentalType == RentalDataType.Hourly) {
            val mFilterList = resource?.filter { it.is_boat == 1 }

            mFilterList
        } else {
            resource
        }

        if (updatedList?.isNotEmpty() == true) {
            mAdapter.submitItemList(updatedList)
        }
    }

    override fun onRefresh() {
        refreshLayout.isRefreshing = false
        callApi()
    }

}
