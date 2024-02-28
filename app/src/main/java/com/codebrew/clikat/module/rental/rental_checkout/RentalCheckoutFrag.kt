package com.codebrew.clikat.module.rental.rental_checkout

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.*
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.data.AppDataManager
import com.codebrew.clikat.data.PaymentType
import com.codebrew.clikat.data.RentalDataType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.model.api.AddCardResult
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.GeofenceData
import com.codebrew.clikat.data.model.api.tap_payment.Transaction
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.data.model.others.HomeRentalParam
import com.codebrew.clikat.data.model.others.SaveCardInputModel
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentRentalCheckoutBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.other.PlaceOrderInput
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.cart.*
import com.codebrew.clikat.module.order_detail.OrderDetailActivity
import com.codebrew.clikat.module.payment_gateway.PaymentListActivity
import com.codebrew.clikat.module.payment_gateway.PaymentWebViewActivity
import com.codebrew.clikat.module.payment_gateway.dialog_card.CardDialogFrag
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.DialogIntrface
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_rental_checkout.*
import kotlinx.android.synthetic.main.layout_instructions.*
import java.lang.reflect.Type
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val ARG_PARAM1 = "rentalParam"

class RentalCheckoutFrag : BottomSheetDialogFragment(), CartNavigator, DialogIntrface, CardDialogFrag.onPaymentListener {
    private var homeRentalModel: HomeRentalParam? = null


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils


    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var dataManager: AppDataManager

    @Inject
    lateinit var paymentUtil: PaymentUtil

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }
    private var orderId = arrayListOf<Int>()
    private var mViewModel: CartViewModel? = null
    private var mBinding: FragmentRentalCheckoutBinding? = null

    lateinit var mSelectedPayment: CustomPayModel

    private var mTotalAmt: Float = 0f
    private var mTotalTax: Float = 0f
    private var mSubTotal: Float = 0f
    private var phoneNumber: String? = null
    private var adrsData: AddressBean? = null
    private var mumyBeneDialog: Dialog? = null
    private var settingData: SettingModel.DataBean.SettingData? = null
    private lateinit var placeOrderInput: PlaceOrderInput

    var baseActivity: BaseActivity<*, *>? = null
        private set

    val isNetworkConnected: Boolean
        get() = baseActivity != null && baseActivity?.isNetworkConnected == true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*, *>) {
            this.baseActivity = context
            context.onFragmentAttached()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel?.navigator = this

        arguments?.let {
            homeRentalModel = it.getParcelable(ARG_PARAM1)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_rental_checkout, container, false)
        AndroidSupportInjection.inject(this)
        mViewModel = ViewModelProviders.of(this, factory).get(CartViewModel::class.java)
        mBinding?.viewModel = mViewModel

        mViewModel?.navigator = this

        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding?.color = Configurations.colors

        adrsData = dataManager.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java)
        settingData = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        mumyBeneDialog = mDialogsUtil.openDialogMumybene(activity ?: requireContext()) {
            mViewModel?.cancelGenerateOrder()
        }

        tv_pickup_adrs.text = if (homeRentalModel?.mRentalType == RentalDataType.Hourly) {
            "${homeRentalModel?.source_port_id?.name},${homeRentalModel?.destination_port_id?.address}"
        } else {
            homeRentalModel?.from_address
        }

        tv_pickup_date_time.text = appUtils.convertDateOneToAnother(homeRentalModel?.booking_from_date
                ?: "", "yyyy-MM-dd HH:mm:ss", "EEEE dd, yyyy - hh:mm aa")
        tv_total.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, homeRentalModel?.totalAmt)

        total_duration.text = when (homeRentalModel?.mRentalType) {
            RentalDataType.Hourly -> {
                getString(R.string.total_duration, getString(R.string.hours_tag))
            }
            RentalDataType.Daily, RentalDataType.Weekly -> {
                getString(R.string.total_duration, getString(R.string.days_tag))
            }
            else -> {
                getString(R.string.total_duration, getString(R.string.months_tag))
            }
        }
        tv_duration.text = homeRentalModel?.mTotalRentalDuration.toString()

        mSubTotal = homeRentalModel?.price?.toFloatOrNull()?.times(homeRentalModel?.mTotalRentalDuration
                ?: 0) ?: 0f
        mTotalTax = mSubTotal.times(homeRentalModel?.handling_admin?.div(100) ?: 0f)

        mTotalAmt = mSubTotal.plus(mTotalTax ?: 0f)

        tv_tax.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTotalTax.toString())
        tv_subtotal.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, (mSubTotal).toString())
        tv_total.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTotalAmt.toString())


        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateTimeUtils.timeLocale)

        placeOrderInput = PlaceOrderInput()
        placeOrderInput.accessToken = prefHelper.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        placeOrderInput.offset = SimpleDateFormat("ZZZZZ", DateTimeUtils.timeLocale).format(System.currentTimeMillis())
        placeOrderInput.cartId = Prefs.with(activity).getString(DataNames.CART_ID, "0")
        placeOrderInput.paymentType = DataNames.PAYMENT_CASH
        placeOrderInput.languageId = dataManager.getLangCode().toInt()

        val calendar = Calendar.getInstance()
        placeOrderInput.booking_date_time = dateFormat.format(calendar.time)

        placeOrderInput.duration = when (homeRentalModel?.mRentalType) {
            RentalDataType.Hourly -> {
                homeRentalModel?.mTotalRentalDuration?.times(60) ?: 0
            }
            RentalDataType.Weekly,RentalDataType.Daily -> {
                homeRentalModel?.mTotalRentalDuration?:0
            }
            else -> {
                30
            }
        }
        placeOrderInput.from_address = homeRentalModel?.from_address
        placeOrderInput.to_address = homeRentalModel?.to_address
        placeOrderInput.booking_from_date = homeRentalModel?.booking_from_date
        placeOrderInput.drop_off_date = homeRentalModel?.booking_to_date
        placeOrderInput.from_latitude = homeRentalModel?.from_latitude
        placeOrderInput.to_latitude = homeRentalModel?.to_latitude
        placeOrderInput.from_longitude = homeRentalModel?.from_longitude
        placeOrderInput.to_longitude = homeRentalModel?.to_longitude


        if (homeRentalModel?.mRentalType == RentalDataType.Hourly) {
            placeOrderInput.source_port_id = homeRentalModel?.source_port_id?.id ?: 0
            placeOrderInput.destination_port_id = homeRentalModel?.destination_port_id?.id ?: 0

            placeOrderInput.product_from_time = homeRentalModel?.product_from_time?: ""
            placeOrderInput.product_to_time = homeRentalModel?.product_to_time?:""
            placeOrderInput.product_slot_price = homeRentalModel?.product_slot_price?:0f
            placeOrderInput.product_slot_id = homeRentalModel?.product_slot_id?:0
        }

        btn_place_order.setOnClickListener {
            if (!::mSelectedPayment.isInitialized) {
                AppToasty.error(requireContext(),getString(R.string.choose_payment))
            }else if(mSelectedPayment.payment_token=="cod")
            {
                if (isNetworkConnected) {
                    offlinePayment()
                }
            }else {
                payment_option_lyt?.performClick()
            }
        }

        payment_option_lyt.setOnClickListener {
            if (isNetworkConnected) {
                mViewModel?.getPaymentGeofence()
            }
        }
    }

    override fun onOrderPlaced(data: ArrayList<Int>) {
        orderId = data
        StaticFunction.sweetDialogueSuccess11(activity, getString(R.string.success),
                getString(R.string.succesfully_order,
                        textConfig?.order), false, 1001, this, true)

    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        baseActivity?.openActivityOnTokenExpire()
    }

    override fun onSuccessListener() {
        NavHostFragment.findNavController(this).navigate(R.id.action_rentalCheckoutFrag_to_mainFragment)
        activity?.startActivity(Intent(activity, OrderDetailActivity::class.java)
                .putExtra(DataNames.REORDER_BUTTON, false).putIntegerArrayListExtra("orderId", orderId))
    }

    override fun onErrorListener() {

    }

    override fun onGeofencePayment(data: GeofenceData?) {
        dataManager.getKeyValue(DataNames.FEATURE_DATA, PrefenceConstants.TYPE_STRING).toString().let {
            val listType: Type = object : TypeToken<ArrayList<SettingModel.DataBean.FeatureData?>?>() {}.type
            val featureList: ArrayList<SettingModel.DataBean.FeatureData> = Gson().fromJson(it, listType)

            val intent = Intent(activity, PaymentListActivity::class.java)
            intent.putParcelableArrayListExtra("feature_data", featureList)
            intent.putExtra("mSelectPayment", data?.gateways as ArrayList<String>?)
            intent.putExtra("enablePayment", false)
            intent.putExtra("mTotalAmt", mTotalAmt)

            startActivityForResult(intent, AppConstants.REQUEST_PAYMENT_OPTION)
        }
    }

    override fun getSaddedPaymentSuccess(data: AddCardResponseData?) {
        mSelectedPayment.keyId = data?.transaction_reference
        val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).putExtra("paymentData", data)
        startActivityForResult(intent, SADDED_PAYMENT_REQUEST)
    }

    override fun getWindCavePaymentSuccess(data: AddCardResult?) {
        val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).putExtra("paymentData", data?.Request)
                .putExtra("payment_gateway", getString(R.string.windcave))
        startActivityForResult(intent, SADDED_PAYMENT_REQUEST)
    }

    override fun getMyFatoorahPaymentSuccess(data: AddCardResponseData?) {
        val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).putExtra("paymentData", data)
                .putExtra("payment_gateway", textConfig?.my_fatoorah)
        startActivityForResult(intent, MY_FATOORAH_PAYMENT_REQUEST)
    }

    override fun onTapPayment(transaction: Transaction) {
        activity?.launchActivity<PaymentWebViewActivity>(TAP_PAYMENT_REQUEST) {
            putExtra("payment_url", transaction.url)
            putExtra("payment_gateway", getString(R.string.tap))
        }
    }

    override fun getPayMayaUrl(data: AddCardResponseData?) {
        activity?.launchActivity<PaymentWebViewActivity>(PAY_MAYA_REQUEST) {
            putExtra("paymentData", data)
            putExtra("payment_gateway", getString(R.string.pay_maya))
        }
    }

    override fun getTelrPaymentSuccess(data: AddCardResponseData?) {
        mSelectedPayment.keyId = data?.order?.ref
        val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).putExtra("paymentData", data)
                .putExtra("payment_gateway", getString(R.string.teller))
        startActivityForResult(intent, TELR_PAYMENT_REQUEST)
    }


    override fun onEvalonPayment(data: String) {
        val paymentUrl = if (BuildConfig.DEBUG) {
            NetworkConstants.EVALON_TEST_LINK
        } else {
            NetworkConstants.EVALON_PROD_LINK
        }
        activity?.launchActivity<PaymentWebViewActivity>(EVALON_PAYMENT_REQUEST)
        {
            putExtra("payment_url", "${paymentUrl}${URLEncoder.encode(data, "utf-8")}")
            putExtra("payment_gateway", getString(R.string.tap))
        }
    }

    override fun getmPaisaPaymentSuccess(data: AddCardResponseData?) {
        mSelectedPayment.keyId = data?.rID
        val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).putExtra("paymentData", data)
                .putExtra("payment_gateway", getString(R.string.mpaisa))
        startActivityForResult(intent, SADDED_PAYMENT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQUEST_PAYMENT_OPTION && resultCode == Activity.RESULT_OK) {
            mSelectedPayment = data?.getParcelableExtra("payItem") ?: CustomPayModel()

            tv_pay_option.text = mSelectedPayment.payName

            if (mSelectedPayment.payName == getString(R.string.mumybene)) {
                mDialogsUtil.showMumyBenePhone(activity ?: requireContext(), phoneNumber
                        ?: "", payName = mSelectedPayment.mumybenePay ?: "") {
                    phoneNumber = it
                    mumyBeneDialog?.show()
                    onlinePayment(mSelectedPayment)
                }
            } else if (mSelectedPayment.payName == getString(R.string.pipol_pay)) {
                onlinePayment(mSelectedPayment)
            } else if (mSelectedPayment.addCard == true) {
                onlinePayment(mSelectedPayment)
            } else if (requestCode == MY_FATOORAH_PAYMENT_REQUEST || requestCode == TAP_PAYMENT_REQUEST || requestCode == EVALON_PAYMENT_REQUEST ||
                    requestCode == AAMAR_PAY_PAYMENT_REQUEST || requestCode == TELR_PAYMENT_REQUEST || requestCode == HYPERPAY_PAYMENT_REQUEST) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        AppToasty.success(requireContext(), getString(R.string.payment_done_successful))
                        if (data != null && data.hasExtra("paymentId"))
                            mSelectedPayment.keyId = data.getStringExtra("paymentId")

                        onlinePayment(mSelectedPayment)

                        // use the result to update your UI and send the payment method nonce to your server
                    }
                    else -> {
                        val message = if (data?.hasExtra("showError") == true)
                            getString(R.string.payment_failed)
                        else
                            getString(R.string.payment_unsuccessful)
                        AppToasty.error(requireContext(), message)
                    }
                }
            } else if(mSelectedPayment.payment_token!="cod") {
                paymentUtil.checkPaymentMethod(this@RentalCheckoutFrag, mViewModel, mSelectedPayment, mTotalAmt.toDouble(),
                        adrsData ?: AddressBean())
            }

        }

        paymentUtil.activityResult(requestCode, resultCode, data).let {
            if (it != null) {
                mSelectedPayment.keyId = it
                onlinePayment(mSelectedPayment)
            }
        }
    }

    override fun getThawaniPaymentSuccess(data: String?) {
        val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).putExtra("payment_url", data)
                .putExtra("payment_gateway", getString(R.string.thawani))
        startActivityForResult(intent, THAWANI_PAYMENT_REQUEST)
    }

    override fun paymentToken(token: String, paymentMethod: String, savedCard: SaveCardInputModel?) {
        token.let {
            if (it != null) {
                mSelectedPayment.keyId = it
                mSelectedPayment.payment_token = paymentMethod
                onlinePayment(mSelectedPayment, savedCard)
            }
        }
    }

    private fun onlinePayment(selectedPayment: CustomPayModel?, mSavedCard: SaveCardInputModel? = null) {
        mViewModel?.rentalGenerateOrder(placeOrderInput,
                if (mSelectedPayment.payment_token == "cod") DataNames.PAYMENT_CASH else {
                    DataNames.PAYMENT_CARD
                },
                payToken = mSelectedPayment.keyId ?: "",
                uniqueId = mSelectedPayment.payment_token ?: "",
                mSelectedPayment = selectedPayment,
                mobileNo = phoneNumber,
                savedCard = mSavedCard)

    }

    private fun offlinePayment() {
        mViewModel?.rentalGenerateOrder(placeOrderInput,
                if (mSelectedPayment.payment_token == "cod") DataNames.PAYMENT_CASH else {
                    DataNames.PAYMENT_CARD
                })

    }

}
