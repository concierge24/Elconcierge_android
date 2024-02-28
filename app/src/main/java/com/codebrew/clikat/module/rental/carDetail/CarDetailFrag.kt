package com.codebrew.clikat.module.rental.carDetail

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.adapters.PagerImageAdapter
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.RentalDataType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.model.others.HomeRentalParam
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentCarDetailBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.CartInfoServer
import com.codebrew.clikat.modal.other.AddtoCartModel
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.module.login.LoginActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.content_car_detail.*
import kotlinx.android.synthetic.main.fragment_car_detail.*
import javax.inject.Inject


class CarDetailFrag : BaseFragment<FragmentCarDetailBinding, CarDetailViewModel>(), CarDetailNavigator {


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtil: AppUtils


    private lateinit var viewModel: CarDetailViewModel
    private var mBinding: FragmentCarDetailBinding? = null


    var favStatus: String = ""

    lateinit var mHomeParam: HomeRentalParam

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_car_detail
    }

    override fun getViewModel(): CarDetailViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(CarDetailViewModel::class.java)
        return viewModel
    }

    override fun updateCart() {

        if (!::mHomeParam.isInitialized) return


        mHomeParam.price = viewDataBinding.productdata?.price
        mHomeParam.handling_admin = viewDataBinding.productdata?.handling_admin

        val bundle = bundleOf("rentalParam" to mHomeParam)
        navController(this@CarDetailFrag)
                .navigate(R.id.action_carDetailFrag_to_rentalCheckoutFrag, bundle)
    }

    override fun addCart(cartdata: AddtoCartModel.CartdataBean) {
        if (isNetworkConnected) {
            if (!::mHomeParam.isInitialized) return

            mHomeParam.mTotalRentalDuration = viewDataBinding.productdata?.totalRentalDuration

            val mSubTotalAmt = with(viewDataBinding?.productdata ?: ProductDataBean()) {
                price?.toFloatOrNull()?.times(mHomeParam.mTotalRentalDuration ?: 0)
            }

            val mTotalTax = mSubTotalAmt?.times(viewDataBinding.productdata?.handling_admin?.div(100)
                    ?: 0f)

            mHomeParam.totalAmt = mSubTotalAmt?.plus(mTotalTax ?: 0f).toString()
            mHomeParam.netAmount = mSubTotalAmt?.plus(mTotalTax ?: 0f)?.toDouble()
            mHomeParam.handling_admin = mTotalTax

            mHomeParam.cartId = cartdata.cartId ?: ""
            viewModel.updateCartInfo(mHomeParam)
        }
    }

    override fun onFavStatus() {

        if (favStatus == "0") {
            tb_favourite.setImageResource(R.drawable.ic_unfavorite)
            mBinding?.root?.onSnackbar(getString(R.string.successUnFavourite))
        } else {
            tb_favourite.setImageResource(R.drawable.ic_favourite)
            mBinding?.root?.onSnackbar(getString(R.string.successFavourite))
        }
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
        productDetailObserver()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding
        viewDataBinding.color = Configurations.colors

        loadProductDetail(arguments)

        btn_continue.setOnClickListener {

            if (prefHelper.getCurrentUserLoggedIn()) {
                addIntoCart()
            } else {
                appUtil.checkLoginFlow(activity ?: requireActivity(), DataNames.REQUEST_CART_LOGIN)
            }
        }

        tb_back.setOnClickListener {
            if (mHomeParam.mRentalType == RentalDataType.Hourly) {
                navController(this@CarDetailFrag)
                        .navigate(R.id.action_carDetailFrag_to_boatRental)
            } else {
                navController(this@CarDetailFrag)
                        .navigate(R.id.action_carDetailFrag_to_homeRental)
            }
        }


        tb_favourite.setOnClickListener {
            if (prefHelper.getCurrentUserLoggedIn()) {
                addFavUnFavProd()
            } else {
                activity?.launchActivity<LoginActivity>(AppConstants.REQUEST_PRODUCT_FAV)
            }
        }

        this.view?.setOnKeyListener(object : DialogInterface.OnKeyListener, View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                return keyCode != KeyEvent.KEYCODE_BACK
            }

            override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                return keyCode != KeyEvent.KEYCODE_BACK
            }
        })
    }

    private fun addFavUnFavProd() {

        if (viewDataBinding.productdata?.id ?: 0 < 0 && viewDataBinding.productdata?.price?.toInt() ?: 0 > 0) return

        if (isNetworkConnected) {
            favStatus = if (checkFavouriteImage(tb_favourite)) {
                "0"
            } else {
                "1"
            }
            viewModel.markFavProduct(viewDataBinding.productdata?.id.toString(), favStatus)
        }
    }

    private fun addIntoCart() {
        if (viewDataBinding.productdata?.id ?: 0 < 0 && viewDataBinding.productdata?.price?.toInt() ?: 0 > 0) return


        val productList = mutableListOf<CartInfoServer>()
        val productdata = viewDataBinding.productdata
        val cartInfoServer = CartInfoServer()

        cartInfoServer.quantity = if(mHomeParam.mRentalType==RentalDataType.Weekly) 1f else productdata?.totalRentalDuration?.toFloat()
        cartInfoServer.pricetype = productdata?.price_type ?: 0
        cartInfoServer.productId = productdata?.id.toString()
        cartInfoServer.category_id = productdata?.category_id ?: 0
        cartInfoServer.handlingAdmin = productdata?.handling_admin ?: 0.0f
        cartInfoServer.supplier_branch_id = productdata?.supplier_branch_id ?: 0
        cartInfoServer.handlingSupplier = productdata?.handling_supplier ?: 0.0f
        cartInfoServer.supplier_id = productdata?.supplier_id ?: 0
        cartInfoServer.agent_type = productdata?.is_agent ?: 0
        cartInfoServer.agent_list = productdata?.agent_list ?: 0
        cartInfoServer.deliveryType = DataNames.DELIVERY_TYPE_STANDARD
        cartInfoServer.name = productdata?.name ?: ""
        cartInfoServer.deliveryCharges = 0.0f
        cartInfoServer.price = productdata?.price?.toFloatOrNull() ?: 0f



        productList.add(cartInfoServer)

        if (isNetworkConnected)
            viewModel.addCart(viewDataBinding.productdata?.supplier_branch_id
                    ?: 0, productList)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DataNames.REQUEST_CART_LOGIN && resultCode == Activity.RESULT_OK) {
            val pojoLoginData = StaticFunction.isLoginProperly(activity)
            if (pojoLoginData.data != null) {

                addIntoCart()
            }

        } else if (requestCode == AppConstants.REQUEST_PRODUCT_FAV && resultCode == Activity.RESULT_OK) {
            val pojoLoginData = StaticFunction.isLoginProperly(activity)
            if (pojoLoginData.data != null) {
                addFavUnFavProd()
            }
        }
    }


    private fun loadProductDetail(arguments: Bundle?) {

        if (arguments == null) return

        mHomeParam = arguments.getParcelable("rentalparam") ?: HomeRentalParam()
        val mListProd = arguments.getParcelable("prodData") ?: ProductDataBean()

        viewDataBinding.productdata = mListProd

        if (isNetworkConnected) {
            viewModel.getProductDetail(mListProd.product_id.toString(), mListProd.supplier_branch_id.toString())

        }

    }

    private fun intializePagerAdapter(imagePath: List<String>?) {

        viewPager.adapter = PagerImageAdapter(activity, imagePath, viewPager)
        cpiIndicator.setViewPager(viewPager)
        cpiIndicator.setSnap(false)
        cpiIndicator.setFillColor(ContextCompat.getColor(activity
                ?: requireContext(), R.color.white))
        cpiIndicator.setUnFillColor(ContextCompat.getColor(activity
                ?: requireContext(), R.color.black))
    }


    private fun productDetailObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<ProductDataBean> { resource ->

            updateProductData(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.productDetailLiveData.observe(this, catObserver)
    }

    private fun updateProductData(resource: ProductDataBean?) {

        val mTotalMinutes = appUtil.getTotalMinutes(mHomeParam.booking_from_date, mHomeParam.booking_to_date,
                mHomeParam.mRentalType ?: RentalDataType.Hourly)


        resource?.totalRentalDuration = mTotalMinutes

        if (mHomeParam.mRentalType == RentalDataType.Hourly) {

            if (resource?.hourly_price?.count() ?: 0 > 1) {
                (mTotalMinutes >= resource?.hourly_price?.firstOrNull()?.min_hour ?: 0 && mTotalMinutes <= resource?.hourly_price?.firstOrNull()?.max_hour ?: 0)
                resource?.price = (resource?.hourly_price?.firstOrNull()?.price_per_hour
                        ?: 0.0f).toString()
            }
        } else {
            if (mHomeParam.mRentalType == RentalDataType.Weekly) {
                resource?.price = resource?.hourly_price?.filter { it.min_hour != 1 && it.price_per_hour?:0f>0  }?.lastOrNull()?.price_per_hour.toString()
            }

            resource?.hourly_price?.forEach {
                if (mHomeParam.mRentalType == RentalDataType.Weekly && it.min_hour != 1 && it.max_hour != 30 && mTotalMinutes in (it.min_hour
                                ?: 0)..(it.max_hour ?: 0) && it.price_per_hour?:0f>0 ) {
                    resource.price = (it.price_per_hour ?: 0.0f).toString()
                } else if (mHomeParam.mRentalType == RentalDataType.Monthly && it.min_hour == 1 && it.max_hour == 30) {
                    //for monthly  1-30
                    resource.price = (it.price_per_hour ?: 0.0f).toString()
                }
            }
        }

        if (resource?.hourly_price?.isEmpty() == true) {
            resource.price = resource.display_price
        }

        resource?.netPrice = (resource?.price?.toFloatOrNull()?.times(resource.totalRentalDuration
                ?: 0))

        viewDataBinding.productdata = resource


        if (resource?.is_favourite == 1) {
            tb_favourite.setImageResource(R.drawable.ic_favourite)
        } else {
            tb_favourite.setImageResource(R.drawable.ic_unfavorite)
        }

        intializePagerAdapter(resource?.image_path as MutableList<String>?)
        webview_prodDesc.loadData(resource?.product_desc?:"", "text/html; charset=UTF-8", null)


        tv_prod_price.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, resource?.price.toString())

    }

    private fun checkFavouriteImage(tbFavourite: ImageView): Boolean {
        return CommonUtils.areDrawablesIdentical(tbFavourite.drawable, ContextCompat.getDrawable(activity
                ?: requireContext(), R.drawable.ic_unfavorite)!!)

    }

}
