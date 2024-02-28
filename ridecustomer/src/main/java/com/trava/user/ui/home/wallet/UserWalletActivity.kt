package com.trava.user.ui.home.wallet

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buraq24.driver.ui.home.wallet.WalletAdapter
import com.google.android.gms.maps.model.LatLng
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.trava.driver.webservices.models.WalletBalModel
import com.trava.driver.webservices.models.WalletHisResponse
import com.trava.driver.webservices.models.WalletModel
import com.trava.user.MainActivity
import com.trava.user.R
import com.trava.user.databinding.ActivityDriverWalletCorsaBinding
import com.trava.user.databinding.ActivityUserWalletBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.HomeContract
import com.trava.user.ui.home.HomePresenter
import com.trava.user.ui.home.WebViewActivity
import com.trava.user.ui.home.comfirmbooking.payment.SavedCards
import com.trava.user.ui.home.deliverystarts.paymentview.PaymentFragment
import com.trava.user.ui.signup.moby.landing.LandingScreen
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.GeoFenceData
import com.trava.user.webservices.models.SendMoneyResponseModel
import com.trava.user.webservices.models.ThawaniData
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.constants.ORDER
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.constants.TITLE
import com.trava.utilities.constants.URL_TO_LOAD
import com.trava.utilities.webservices.BaseRestClient
import com.trava.utilities.webservices.models.AppDetail
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import kotlinx.android.synthetic.main.activity_user_wallet.*
import kotlinx.android.synthetic.main.activity_wallet.rvRecentRecharges
import kotlinx.android.synthetic.main.activity_wallet.srlWallet
import kotlinx.android.synthetic.main.activity_wallet.toolbar
import kotlinx.android.synthetic.main.activity_wallet.tvAddMoney
import kotlinx.android.synthetic.main.activity_wallet.tvWalletAmt
import kotlinx.android.synthetic.main.activity_wallet.tvWalletDesc
import kotlinx.android.synthetic.main.activity_wallet.vfRechargeHistory
import kotlinx.android.synthetic.main.fragment_ride_complete.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class  UserWalletActivity : AppCompatActivity(), View.OnClickListener, WalletContract.View, SwipeRefreshLayout.OnRefreshListener
        , HomeContract.View, PaymentResultListener
/*PaymentResultListener*/ {
    override fun nearByResponse(jsonRootObject: JSONObject, sourceLatLng: LatLng?, destLatLng: LatLng?) {


    }

    private var skip = 0
    private var isLoading = false
    private var isLastPage = false
    private val listWalletHistory = ArrayList<WalletModel?>()
    private val presenter = WalletPresenter()
    private var dialogIndeterminate: DialogIndeterminate? = null
    private var adapter: WalletAdapter? = null
    var amontToAdded = ""
    var walletBalance = 0.0
    lateinit var profile: AppDetail
    var binding: ActivityUserWalletBinding? = null
    var bindingcorsa: ActivityDriverWalletCorsaBinding? = null
    private val logoutPresenter = HomePresenter()
    var rechargeAmount: Int? = null
    var isTransactioned: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA)
        {
            bindingcorsa = DataBindingUtil.setContentView(this, R.layout.activity_driver_wallet_corsa)
        }
        else
        {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_user_wallet)
        }
        binding?.color = ConfigPOJO.Companion
        bindingcorsa?.color = ConfigPOJO.Companion
        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)

        Checkout.preload(this)

        binding?.balanceLayout?.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color));
        binding?.tvSendMoney?.setBackgroundColor(Color.parseColor(ConfigPOJO.Btn_Colour));
        binding?.tvSendMoney?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour));
        binding?.tvAddMoney?.setBackgroundColor(Color.parseColor(ConfigPOJO.Btn_Colour));
        binding?.tvAddMoney?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour));

        bindingcorsa?.tvSendMoney?.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color));
        bindingcorsa?.tvSendMoney?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour));
        bindingcorsa?.tvAddMoney?.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color));
        bindingcorsa?.tvAddMoney?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour));

        if (ConfigPOJO?.gateway_unique_id == "stripe" || ConfigPOJO?.gateway_unique_id == "razorpay" || ConfigPOJO?.gateway_unique_id == "thawani") {
            tvAddMoney.visibility = View.VISIBLE
        }
        logoutPresenter.attachView(this)
        profile = SharedPrefs.with(this).getObject(PROFILE, AppDetail::class.java)
        initialize()
        getWalletBalance()
        initAdapter()
        vfRechargeHistory.displayedChild = 0
        resetPagination()
        if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA)
        {
            setData()
        }

       backbtn?.setOnClickListener {
            finish()
        }
    }

    private fun getWalletBalance() {
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.getWalletBalance(false)
        } else {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show()
        }
    }

    fun initialize() {
        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(this)
        toolbar.setOnClickListener(this)
        tvAddMoney.setOnClickListener(this)
        tvSendMoney.setOnClickListener(this)
        srlWallet.setOnRefreshListener(this)

        if (ConfigPOJO.gateway_unique_id.equals("stripe")) {
            binding?.tvAddMoney?.visibility = View.VISIBLE
        }
    }


    fun setData() {

        if(ConfigPOJO.minimum_wallet_balance.isNotEmpty()){
            tvWalletDesc.text = "${getString(R.string.wallet_text)} ${getFormattedPrice(ConfigPOJO.minimum_wallet_balance?.toDouble())} ${ConfigPOJO.currency} ${getString(R.string.wallet_text_2)}"

        }
        else{
            tvWalletDesc.text = "${getString(R.string.wallet_text)} ${"0"} ${ConfigPOJO.currency} ${getString(R.string.wallet_text_2)}"

        }
    }

    fun initAdapter() {
        adapter = WalletAdapter(listWalletHistory)
        rvRecentRecharges.layoutManager = LinearLayoutManager(rvRecentRecharges.context)
        rvRecentRecharges.adapter = adapter

        rvRecentRecharges.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = rvRecentRecharges.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= Constants.PAGE_LIMIT) {
                        walletHistoryApi()
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            toolbar.id -> onBackPressed()

            tvAddMoney.id -> showAddMoneyDialog()

            tvSendMoney.id -> startActivityForResult(Intent(this
                    , TransferWalletMoneyActivity::class.java).putExtra("balance",walletBalance), 777)

            srlWallet.id -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === 777) {
            if (resultCode === Activity.RESULT_CANCELED) {

                if (!CheckNetworkConnection.isOnline(this)) {
                    CheckNetworkConnection.showNetworkError(srlWallet)
                    return
                }
                getWalletBalance()
                resetPagination()
            } else if (resultCode == Activity.RESULT_OK) {
                binding?.root?.showSnack(data?.getStringExtra("result").toString())
            }
        }

        if (requestCode == 101) {
            if (data != null) {
                val cardId = data.getStringExtra("card_id").toString()
                rechargeWallet(rechargeAmount, cardId)
            }
        }
        if (requestCode == Constants.ADD_CARD_REQUEST_CODE) {
            if (data != null) {
                val cardId = data.getStringExtra("transactionId").toString()
                walletHistoryApi()
            }
        }
    }

    private fun rechargeWallet(amount: Int?, cardId: String) {
        if (!CheckNetworkConnection.isOnline(this)) {
            CheckNetworkConnection.showNetworkError(srlWallet)
            return
        } else {
            presenter.addMoneyToWallet(amount!!.toInt(), cardId, ConfigPOJO.gateway_unique_id.toString())
        }
    }


    fun showAddMoneyDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.layout_add_money)

        var window = dialog.getWindow()
        var wlp = window?.getAttributes()
        wlp?.gravity = Gravity.CENTER
        wlp?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window?.setAttributes(wlp)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val etAddAmount = dialog.findViewById<EditText>(R.id.etAddAmount)
        val ic_cross = dialog.findViewById<ImageView>(R.id.imageView7)
        val tvAmtWillBe = dialog.findViewById<TextView>(R.id.tvAmtWillBe)
        val tvContinueAddMoney = dialog.findViewById(R.id.tvContinueAddMoney) as TextView
        tvAmtWillBe.text = "${getFormattedPrice(0.00)} ${ConfigPOJO.currency}"

        tvContinueAddMoney.setBackgroundColor(Color.parseColor(ConfigPOJO.Btn_Colour))
        tvContinueAddMoney.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))

        etAddAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isEmpty())
                    tvAmtWillBe.text = "${getFormattedPrice(0.00)} ${ConfigPOJO.currency}"
                else
                    tvAmtWillBe.text = "${getFormattedPrice(p0.toString().toDouble())} ${ConfigPOJO.currency}"
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        tvContinueAddMoney.setOnClickListener {
            if (CheckNetworkConnection.isOnline(this)) {
                if (etAddAmount.text.toString().isNotEmpty() && etAddAmount.text.toString().toFloat() > 0.0) {
                    var enteredAmount = etAddAmount.text.toString().toFloat()
                    amontToAdded = etAddAmount.text.toString().trim()

                    if (ConfigPOJO.gateway_unique_id == "thawani"){

                        if (amontToAdded.toInt() < 100.0){
                            Toast.makeText(this, "Please enter minimum amount 100", Toast.LENGTH_SHORT).show()

                        }
                        else{
                            startThawaniPayment(amontToAdded)
                        }

                    }
                    else{
                        startPayment(enteredAmount)
                    }

                    dialog.dismiss()
                } else
                    Toast.makeText(this, getString(R.string.enter_valid_amount), Toast.LENGTH_SHORT).show()
            } else {
                CheckNetworkConnection.showNetworkError(srlWallet)
                dialog.dismiss()
            }
        }

        ic_cross.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onWalletHistorySuccess(listHistory: WalletHisResponse?) {
        walletBalance = listHistory?.amount?.toDouble() ?: 0.0

        if (listHistory?.result?.size == 0 && rvRecentRecharges.adapter?.itemCount == 0) {
            vfRechargeHistory.displayedChild = 2
            if (isTransactioned) {
                onRefresh()
                isTransactioned = false
            }
        } else if ((listHistory?.result?.size ?: 0) > 0) {
            vfRechargeHistory.displayedChild = 1
            // this.listWalletHistory.addAll(listHistory?.result ?: emptyList())
            rvRecentRecharges.adapter?.notifyDataSetChanged()
        }

        isLoading = false
        srlWallet.isRefreshing = false
        if (skip == 0 && listHistory?.result?.size == 0) {
            vfRechargeHistory.displayedChild = 2
        } else {
            if (skip == 0) {
                this.listWalletHistory.clear()
            }
            if ((listHistory?.result?.size ?: 0) < Constants.PAGE_LIMIT) {
                isLastPage = true
                adapter?.setAllItemsLoaded(true)
            }
            skip += Constants.PAGE_LIMIT
            vfRechargeHistory.displayedChild = 1
            this.listWalletHistory.addAll(listHistory?.result ?: emptyList())
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onWalletAddedSuccess(response: Order?) {
        if (response?.amount =="0" || response?.amount ==null)
        {
            tvSendMoney.visibility=View.GONE
        }
        tvWalletAmt.text = "${getFormattedPrice(response?.amount?.toDouble())} ${ConfigPOJO.currency}"
        walletBalance = response?.amount?.toDouble() ?: 0.0
        resetPagination()
    }

    override fun onWalletBalSuccess(response: WalletBalModel?) {
        if (response?.amount =="0" || response?.amount ==null)
        {
            tvSendMoney.visibility=View.GONE
        }
        tvWalletAmt.text = "${getFormattedPrice(response?.amount?.toDouble())} ${ConfigPOJO.currency}"
        walletBalance = response?.amount?.toDouble() ?: 0.0

    }

    override fun onMoneySentSuccessFully(response: SendMoneyResponseModel?) {
        TODO("Not yet implemented")
    }


    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
        if (isLoading)
            vfRechargeHistory.displayedChild = 0
    }

    override fun apiFailure() {
        vfRechargeHistory.displayedChild = 2
    }

    override fun handleApiError(code: Int?, error: String?) {
        vfRechargeHistory?.showSnack(error.toString())
        if (code == StatusCode.UNAUTHORIZED) {
            signOut(this)
        }
    }

    private fun signOut(userWalletActivity: UserWalletActivity) {
        if (CheckNetworkConnection.isOnline(this)) {
            logoutPresenter.logout()
        } else {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        logoutPresenter.detachView()
        dialogIndeterminate?.show(false)
    }

    override fun onRefresh() {
        getWalletBalance()
        resetPagination()
    }

    private fun resetPagination() {
        skip = 0
        isLastPage = false
        adapter?.setAllItemsLoaded(false)
        walletHistoryApi()
    }

    private fun walletHistoryApi() {
        if (!CheckNetworkConnection.isOnline(this)) {
            CheckNetworkConnection.showNetworkError(srlWallet)
            return
        }
        isLoading = true
        presenter.getWalletHistoryList(Constants.PAGE_LIMIT, skip)
    }

    fun startPayment(amount: Float?) {
        rechargeAmount = amount?.toInt()
        val proifle = SharedPrefs.with(this).getObject(PROFILE, AppDetail::class.java)
        if (ConfigPOJO.gateway_unique_id != "razorpay") {
            startActivityForResult(Intent(this, SavedCards::class.java)
                    .putExtra("amount", amount)
                    , 101)
        } else {
            val co = Checkout()
            co.setKeyID(ConfigPOJO.razorpay_private_key)
            try {
                val options = JSONObject()
                options.put("name", proifle.user?.name)
                options.put("description", "Demoing Charges")
                //You can omit the image option to fetch the image from dashboard
                options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
                if(ConfigPOJO.currency == "₹") {
                    options.put("currency", "INR")
                }else{
                    options.put("currency", ConfigPOJO.currency)
                }
                options.put("amount", "" + ((amount ?: 0.0F) * 100).toInt())
                val preFill = JSONObject()
                preFill.put("email", proifle.user?.email)
                preFill.put("contact", proifle.user?.phone_number)
                options.put("prefill", preFill)
                co.open(this, options)
            } catch (e: Exception) {
                Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        vfRechargeHistory?.showSnack(getString(R.string.payment_failed))
    }

    override fun onPaymentSuccess(p0: String?) {
        Log.e("asasasasas", p0.toString())
        var map = HashMap<String, Any>()
        map.put("amount", amontToAdded)
        map.put("payment_id", p0.toString())
        presenter?.saveRazorPaymentData(map)
    }

    fun getFormattedPrice(value: Double?): String {
        value?.let {
            val nf = NumberFormat.getNumberInstance(Locale.ENGLISH)
            val formatter = nf as DecimalFormat
            formatter.applyPattern("#0.00")
            return formatter.format(value)
        }
        return value.toString()
    }

    override fun onApiSuccess(login: LoginModel?) {

    }


    override fun logoutSuccess() {
        /* Logout successful. Clears*/
        SharedPrefs.with(this@UserWalletActivity).removeAllSharedPrefsChangeListeners()
        SharedPrefs.with(this@UserWalletActivity).removeAll()
        finishAffinity()
        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            startActivity(Intent(this@UserWalletActivity, LandingScreen::class.java))
        } else {
            startActivity(Intent(this@UserWalletActivity, MainActivity::class.java))
        }
        AppSocket.get().disconnect()
        AppSocket.get().off()
        AppSocket.get().socket=null
    }

    override fun onSupportListApiSuccess(response: List<Service>?) {
        TODO("Not yet implemented")
    }

    override fun polyLine(jsonRootObject: JSONObject, sourceLatLng: LatLng?, destLatLng: LatLng?) {
        TODO("Not yet implemented")
    }

    override fun driverDistanceTimeSuccess(jsonRootObject: JSONObject) {
        TODO("Not yet implemented")
    }

    override fun onHomeApiSuccess(response: ServiceDetails?) {
        TODO("Not yet implemented")
    }

    override fun onPolygonDataSuccess(response: ArrayList<GeoFenceData>?) {
        TODO("Not yet implemented")
    }

    override fun RazorPaymentApiSuccess(apiResponseNew: ApiResponseNew?) {
        if(apiResponseNew?.success ==1){
            getWalletBalance()
            resetPagination()
        }
    }

    override fun PayThwaniGeturlSuccess(apiResponseNew: ApiResponse<ThawaniData>?) {
        if (apiResponseNew?.statusCode==200) {

            startActivityForResult(Intent(this, WebViewActivity::class.java)
                .putExtra(URL_TO_LOAD, apiResponseNew.result?.payment_url)
                .putExtra(TITLE, getString(R.string.wallet)), Constants.ADD_CARD_REQUEST_CODE)
//
        } else {
            tvCompleteRide.showSnack(apiResponseNew?.msg ?: "")
        }
    }

    override fun DataThawaniSuccess(apiResponseNew: ApiResponseNew?) {

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onBackPressed() {
        if (intent.hasExtra("from")) {
            startActivity(Intent(this@UserWalletActivity,
                    HomeActivity::class.java)
                    .putExtra("from", "Notification")
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        } else {
            super.onBackPressed()
        }
    }
    private fun startThawaniPayment(amount:String) {
        val hashMap = HashMap<String, Any>()
        hashMap["amount"] = (amount)
        hashMap["email"] = profile?.user?.email?:""
        hashMap["name"] = profile?.user?.name?:""
        hashMap["success_url"] = BaseRestClient.BASE_URL +"/success"
        hashMap["cancel_url"] = BaseRestClient.BASE_URL +"/cancel"
        presenter.getThawaniUrl(hashMap)
    }
}
