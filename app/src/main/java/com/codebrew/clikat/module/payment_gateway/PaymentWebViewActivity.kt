package com.codebrew.clikat.module.payment_gateway

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.WebViewBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.module.webview.WebViewModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.nothing_found.*
import kotlinx.android.synthetic.main.web_view.*
import javax.inject.Inject


/*
 * Created by cbl80 on 5/7/16.
 */
class PaymentWebViewActivity : BaseActivity<WebViewBinding, WebViewModel>(), BaseInterface {

    private var mViewModel: WebViewModel? = null

    @Inject
    lateinit var factory: ViewModelProviderFactory


    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtils: AppUtils

    private var paymentData: AddCardResponseData? = null

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        viewDataBinding?.color = Configurations.colors
        viewDataBinding?.drawables = Configurations.drawables
        viewDataBinding?.strings = Configurations.strings


        initialise()
        setlanguage()
        settoolbar()



        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.builtInZoomControls = true
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        val paymentUrl = if (intent?.hasExtra("payment_gateway") == true) {
            when (intent?.getStringExtra("payment_gateway")) {
                getString(R.string.tap), getString(R.string.elavon), getString(R.string.hyper_pay), getString(R.string.sadad), getString(R.string.trans_bank), getString(R.string.paystack), getString(R.string.thawani) -> {
                    intent?.getStringExtra("payment_url") ?: ""
                }
                getString(R.string.mpaisa) -> paymentData?.finalUrl ?: ""
                getString(R.string.aamar_pay) -> paymentData?.cards?.firstOrNull()?.url ?: ""

                textConfig?.my_fatoorah -> paymentData?.PaymentURL ?: ""

                getString(R.string.teller) -> paymentData?.order?.url ?: ""
                getString(R.string.windcave) -> paymentData?.url ?: ""
                getString(R.string.pay_maya) -> paymentData?.redirectUrl ?: ""
                else -> paymentData?.payment_url ?: ""
            }
        } else
            paymentData?.payment_url ?: ""

        if (paymentUrl.isNotEmpty()) {
            webView.visibility = View.VISIBLE
            noData.visibility = View.GONE
            webView.loadUrl(paymentUrl)
        } else {
            noData.visibility = View.VISIBLE
            webView.visibility = View.GONE
            tvText.text = getString(R.string.payment_failed)
        }



        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url ?: "")
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                mViewModel?.setIsLoading(true)
                //Timber.e("url..", url)
                checkForFinalUrl(url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                mViewModel?.setIsLoading(false)
                checkForFinalUrl(url)
            }
        }
    }

    //https://billing.royoapps.com/payment-success?trxref=imf5w79c6n&reference=imf5w79c6n

    //{"status":200,"message":"Success","data":{"status":true,"message":"Authorization URL created",
    // "data":{"authorization_url":"https://checkout.paystack.com/ltevk3k4lag0m8x","access_code":"ltevk3k4lag0m8x","reference":"imf5w79c6n"}}}

    private fun checkForFinalUrl(url: String) {
        val paymentGateway = if (intent?.hasExtra("payment_gateway") == true)
            intent?.getStringExtra("payment_gateway")
        else ""

        if (paymentGateway == getString(R.string.mpaisa)) {
            if (url.contains("rCode") && Uri.parse(url).getQueryParameter("rCode") == "111") {
                setResult(Activity.RESULT_CANCELED, Intent().putExtra("showError", true))
                finish()
            } else if (!url.contains("mpaisa"))
                checkUrl(url, paymentGateway)
        } else if (paymentGateway == getString(R.string.paystack) && url.contains("payment-success")) {
            val uri = Uri.parse(url)

            val intentNew = Intent()
            intentNew.putExtra("paymentId", uri.getQueryParameter("reference"))
            setResult(Activity.RESULT_OK, intentNew)
            finish()
        } else {
            checkUrl(url, paymentGateway)
        }

    }

    private fun checkUrl(url: String, paymentGateway: String?) {
        if (url.contains("success")) {
            if (paymentGateway == getString(R.string.sadad) && !url.contains("tran_id")) {
                return
            } else {
                Handler().postDelayed({
                    val intentNew = Intent()
                    //https://sandbox-gateway.payments.tap.company/success?tap_id=chg_TSCw3Z11220200057b8MB1206496

                    val uri = Uri.parse(url)

                    when (intent?.getStringExtra("payment_gateway") ?: "") {
                        textConfig?.my_fatoorah -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("paymentId"))
                        }
                        getString(R.string.elavon) -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("ssl_txn_id"))
                        }
                        getString(R.string.tap) -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("tap_id"))
                        }
                        getString(R.string.mpaisa) -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("rID"))
                        }
                        getString(R.string.aamar_pay), getString(R.string.teller), getString(R.string.sadad) -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("tran_id"))
                        }
                        getString(R.string.trans_bank) -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("buyOrder"))
                        }
                        getString(R.string.thawani) -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("txn_id"))
                        }
                        getString(R.string.hyper_pay) -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("id"))
                        }
                        getString(R.string.pay_maya) -> {
                            intentNew.putExtra("paymentId", uri.getQueryParameter("ref_id"))
                        }
                        getString(R.string.windcave) -> intentNew.putExtra("paymentId", uri.getQueryParameter("result"))
                    }
                    setResult(Activity.RESULT_OK, intentNew)
                    finish()
                }, 1000)
            }
        } else if (url.contains("error")) {
            Handler().postDelayed({
                setResult(Activity.RESULT_CANCELED, Intent().putExtra("showError", true))
                finish()
            }, 1000)
        }
    }

    private fun initialise() {
        tvTitle.text = getString(R.string.payment)
        intent?.let {
            paymentData = it.getParcelableExtra("paymentData")
        }
    }

    private fun settoolbar() {
        setSupportActionBar(toolbar)
        val icon = resources.getDrawable(R.drawable.ic_back)
        icon.setColorFilter(Color.parseColor(Configurations.colors.toolbarText), PorterDuff.Mode.SRC_IN)
        supportActionBar?.setHomeAsUpIndicator(icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun destroyWebView() {
        // Make sure you remove the WebView from its parent view before doing anything.
        // mWebContainer.removeAllViews()
        webView.clearHistory()
        // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
        // Probably not a great idea to pass true if you have other WebViews still alive.
        webView.clearCache(true)
        // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
        webView.loadUrl("about:blank")
        webView.onPause()
        webView.removeAllViews()
        //  webView.destroyDrawingCache()
        // NOTE: This pauses JavaScript execution for ALL WebViews,
        // do not use if you have other WebViews still alive.
        // If you create another WebView after calling this,
        // make sure to call mWebView.resumeTimers().
        webView.pauseTimers()
        // NOTE: This can occasionally cause a segfault below API 17 (4.2)
        webView.destroy()
        // Null out the reference so that you don't end up re-using it.
        // mWebView = null
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()

        destroyWebView()
    }

    private fun setlanguage() {
        val selectedLang = prefHelper.getKeyValue(DataNames.SELECTED_LANGUAGE, PrefenceConstants.TYPE_STRING).toString()
        if (selectedLang == "arabic" || selectedLang == "ar") {
            GeneralFunctions.force_layout_to_RTL(this)
        } else {
            GeneralFunctions.force_layout_to_LTR(this)
        }
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.web_view
    }

    override fun getViewModel(): WebViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(WebViewModel::class.java)
        return mViewModel as WebViewModel
    }

    override fun onErrorOccur(message: String) {
        viewDataBinding.root.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }
}