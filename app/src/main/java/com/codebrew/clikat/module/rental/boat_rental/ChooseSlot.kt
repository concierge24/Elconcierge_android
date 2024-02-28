package com.codebrew.clikat.module.rental.boat_rental

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.DateTimeUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.RentalDataType
import com.codebrew.clikat.data.model.api.SlotData
import com.codebrew.clikat.data.model.others.HomeRentalParam
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentChooseSlotBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.CartInfoServer
import com.codebrew.clikat.modal.other.AddtoCartModel
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.module.rental.HomeRentalNavigator
import com.codebrew.clikat.module.rental.HomeRentalViewModel
import com.codebrew.clikat.module.rental.boat_rental.adapter.SlotListener
import com.codebrew.clikat.module.rental.boat_rental.adapter.SlotsAdapter
import com.codebrew.clikat.module.rental.carDetail.CarDetailNavigator
import com.codebrew.clikat.module.rental.carDetail.CarDetailViewModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_choose_slot.*
import kotlinx.android.synthetic.main.item_choose_supplier_lyt.*
import javax.inject.Inject


class ChooseSlot : BaseFragment<FragmentChooseSlotBinding, CarDetailViewModel>(),
    CarDetailNavigator, SwipeRefreshLayout.OnRefreshListener {


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var dateTimeUtils: DateTimeUtils

    @Inject
    lateinit var prefHelper: PreferenceHelper

    private lateinit var viewModel: CarDetailViewModel
    private lateinit var mHomeParam: HomeRentalParam
    private lateinit var productdata: ProductDataBean
    private lateinit var mSlotData: SlotData
    private var mBinding: FragmentChooseSlotBinding? = null

    val argument: ChooseSlotArgs by navArgs()

    private var adapter: SlotsAdapter? = null

    @Inject
    lateinit var appUtils: AppUtils

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this
        productSlotObserver()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding
        viewDataBinding.color = Configurations.colors
        viewDataBinding.strings = textConfig

        settingToolbar()

        settingRecylerView()
        settingProductData()

        callApi()

        refreshLayout.setOnRefreshListener(this)

        btn_choose_slot.setOnClickListener {
            if (::mSlotData.isInitialized) {
                if (prefHelper.getCurrentUserLoggedIn()) {
                    addIntoCart()
                } else {
                    appUtils.checkLoginFlow(
                        activity ?: requireActivity(),
                        DataNames.REQUEST_CART_LOGIN
                    )
                }
            } else {
                mBinding?.root?.onSnackbar(getString(R.string.please_select_slot))
            }

        }
    }

    private fun callApi() {
        if (isNetworkConnected) {
            val hashMap = hashMapOf(
                "productId" to argument.prodData?.product_id.toString(),
                "date" to argument.rentalparam?.booking_from_date.toString(),
                "offset" to dateTimeUtils.getTimeOffset()
            )

            viewModel.getProductSlots(hashMap)
        }
    }

    private fun settingProductData() {
        with(argument.prodData)
        {
            mBinding?.model = this
        }

        mHomeParam = argument.rentalparam ?: HomeRentalParam()
        productdata = argument.prodData ?: ProductDataBean()
        tv_supplier_detail.text = getString(R.string.supplier_detail, textConfig?.supplier)
        tv_slot_detail.text = getString(R.string.slot_detail)
    }

    private fun settingRecylerView() {
        adapter = SlotsAdapter()
        adapter?.settingCallback(SlotListener { slot ->

            if (viewModel.prodSlotLiveData.value != null && viewModel.prodSlotLiveData.value?.isNotEmpty() == true) {

                viewModel.prodSlotLiveData.value?.map {
                    if (it.id == slot.id) {
                        it.isSelected = !it.isSelected

                        mSlotData = it
                    } else {
                        it.isSelected = false
                    }
                }

                adapter?.submitItemList(viewModel.prodSlotLiveData.value)
            }

        })

        rv_timeslot.adapter = adapter
    }

    private fun settingToolbar() {
        toolbar?.setTitle(R.string.choose_prod_slot)
        toolbar.setNavigationOnClickListener {
            navController(this@ChooseSlot).popBackStack()
        }
    }

    private fun productSlotObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<MutableList<SlotData>> { resource ->

            resource?.map {
                it.isSelected = false
                it.format_end =
                    dateTimeUtils.convertDateOneToAnother(it.end_time, "HH:mm:ss", "hh:mm aaa")
                        ?: ""
                it.format_start =
                    dateTimeUtils.convertDateOneToAnother(it.start_time, "HH:mm:ss", "hh:mm aaa")
                        ?: ""
            }

            if (refreshLayout.isRefreshing) {
                refreshLayout.isRefreshing = false
            }

            adapter?.submitItemList(resource)
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.prodSlotLiveData.observe(this, catObserver)
    }


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_choose_slot
    }

    override fun getViewModel(): CarDetailViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(CarDetailViewModel::class.java)
        return viewModel
    }

    override fun addCart(cartdata: AddtoCartModel.CartdataBean) {
        if (isNetworkConnected) {
            if (!::mHomeParam.isInitialized) return

            mHomeParam.mTotalRentalDuration = 1

            val mSubTotalAmt = with(mSlotData) {
                price.times(mHomeParam.mTotalRentalDuration ?: 0)
            }

            val mTotalTax = mSubTotalAmt.times(
                productdata.handling_admin?.div(100)
                    ?: 0f
            )

            mHomeParam.totalAmt = mSubTotalAmt.plus(mTotalTax).toString()
            mHomeParam.netAmount = mSubTotalAmt.plus(mTotalTax).toDouble()
            mHomeParam.handling_admin = mTotalTax

            mHomeParam.cartId = cartdata.cartId ?: ""
            viewModel.updateCartInfo(mHomeParam)
        }
    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    private fun addIntoCart() {
        if (productdata.id ?: 0 < 0 && productdata.price?.toInt() ?: 0 > 0) return


        val productList = mutableListOf<CartInfoServer>()
        val productdata = productdata
        val cartInfoServer = CartInfoServer()

        cartInfoServer.quantity = 1f
        cartInfoServer.pricetype = productdata.price_type ?: 0
        cartInfoServer.productId = productdata.product_id.toString()
        cartInfoServer.category_id = productdata.category_id ?: 0
        cartInfoServer.handlingAdmin = productdata.handling_admin ?: 0.0f
        cartInfoServer.supplier_branch_id = productdata.supplier_branch_id ?: 0
        cartInfoServer.handlingSupplier = productdata.handling_supplier ?: 0.0f
        cartInfoServer.supplier_id = productdata.supplier_id ?: 0
        cartInfoServer.agent_type = productdata.is_agent ?: 0
        cartInfoServer.agent_list = productdata.agent_list ?: 0
        cartInfoServer.deliveryType = DataNames.DELIVERY_TYPE_STANDARD
        cartInfoServer.name = productdata.name ?: ""
        cartInfoServer.deliveryCharges = 0.0f
        cartInfoServer.price = productdata.price?.toFloatOrNull() ?: 0f



        productList.add(cartInfoServer)

        if (isNetworkConnected)
            viewModel.addCart(
                productdata.supplier_branch_id
                    ?: 0, productList
            )
    }

    override fun updateCart() {
        if (!::mHomeParam.isInitialized) return

        mHomeParam.booking_from_date=  "${mHomeParam.booking_from_date} ${mSlotData.start_time}"


        mHomeParam.price = mSlotData.price.toString()
        mHomeParam.handling_admin = productdata.handling_admin

        mHomeParam.product_from_time =mHomeParam.booking_from_date
        mHomeParam.product_to_time ="${mHomeParam.booking_from_date} ${mSlotData.end_time}"
        mHomeParam.product_slot_price=mSlotData.price
        mHomeParam.product_slot_id =mSlotData.id

        val bundle = bundleOf("rentalParam" to mHomeParam)
        navController(this@ChooseSlot)
            .navigate(R.id.action_carDetailFrag_to_rentalCheckoutFrag, bundle)
    }

    override fun onRefresh() {
        callApi()
    }


}