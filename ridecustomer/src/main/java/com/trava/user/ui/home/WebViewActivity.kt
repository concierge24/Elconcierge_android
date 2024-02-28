package com.trava.user.ui.home

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import com.trava.user.R
import com.trava.utilities.constants.TITLE
import com.trava.utilities.constants.URL_TO_LOAD
import kotlinx.android.synthetic.main.activity_web_view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.trava.utilities.LocaleManager
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.appsettings.LanguageSets
import com.trava.utilities.Constants.GOMOVE_BASE
import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.LANGUAGE_CODE
import java.util.*


class WebViewActivity : AppCompatActivity() {

    private var LanguageSets = arrayListOf<LanguageSets>()
    var languageCode="en"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)


        val title = intent?.getStringExtra(TITLE)
        if (title?.isNotEmpty() == true) {
            tvTitle.text = title
        }

        LanguageSets.clear()
        LanguageSets.addAll(ConfigPOJO.settingsResponse?.languages!!)
        for (i in LanguageSets.indices)
        {
            if (LanguageSets[i].language_id.toString()== SharedPrefs.get().getString(LANGUAGE_CODE, ""))
            {
                languageCode=LanguageSets[i].language_code.toLowerCase()
            }
        }

        progressbar.max = 100

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.allowFileAccess = true
        webView?.settings?.allowContentAccess = true
        webView?.settings?.allowFileAccessFromFileURLs = true
        webView?.settings?.allowUniversalAccessFromFileURLs = true
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressbar.setProgress(progress, true)
                } else {
                    progressbar.progress = progress
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressbar.progress = 0
                progressbar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressbar.visibility = View.GONE

            }
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                return shouldOverrideUrlLoading(url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(webView: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return shouldOverrideUrlLoading(uri.toString())
            }

            private fun shouldOverrideUrlLoading(url: String): Boolean {
                Log.i("mmmmmmmmm", "shouldOverrideUrlLoading() URL : $url")

                if (ConfigPOJO.gateway_unique_id.toLowerCase() == "thawani") {
                    if (url?.contains("success") == true) {
                        val uri = Uri.parse(url)
                        var intent = Intent()
                        intent.putExtra("transactionId",uri.getQueryParameter("txn_id").toString())
                        setResult(Activity.RESULT_OK,intent)
                    } else {
                        Toast.makeText(this@WebViewActivity, "Payment Failed", Toast.LENGTH_SHORT).show()

                    }
                }
                // Here put your code
//                setResult(Activity.RESULT_OK)
//                finish()
                return true // Returning True means that application wants to leave the current WebView and handle the url itself, otherwise return false.
            }
        }
        if (ConfigPOJO.TEMPLATE_CODE == GOMOVE_BASE || ConfigPOJO.gateway_unique_id == "thawani") {
            webView.loadUrl(intent?.getStringExtra(URL_TO_LOAD)!!)
        } else {
            webView.loadUrl(intent?.getStringExtra(URL_TO_LOAD)+languageCode)
        }
        setListeners()
    }


    private fun setListeners() {
        ivBack.setOnClickListener { super.onBackPressed() }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

//    inner class MyWebViewClient : WebViewClient() {
//
//        override fun onPageFinished(view: WebView?, url: String?) {
//            super.onPageFinished(view, url)
//            progressbar.visibility = View.GONE
//        }
//
//        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//            super.onPageStarted(view, url, favicon)
//            progressbar.progress = 0
//            progressbar.visibility = View.VISIBLE
//        }
//
//        @Suppress("OverridingDeprecatedMember")
//        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//            view?.loadUrl(url)
//            return true
////            val uri = Uri.parse(url)
////            return handleUri(uri)
//        }
//
//        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//
//            val uri = request?.url
//            return handleUri(uri)
//        }
//
//        private fun handleUri(uri: Uri?): Boolean {
//            val host = uri?.host
//            // Based on some condition you need to determine if you are going to load the url
//            // in your web view itself or in a browser.
//            // You can use `host` or `scheme` or any part of the `uri` to decide.
//            return if (host == BASE_URL) {
//                // Returning false means that you are going to load this url in the webView itself
//                false
//            } else {
//                // Returning true means that you need to handle what to do with the url
//                // e.g. open web page in a Browser
//                val intent = Intent(Intent.ACTION_VIEW, uri)
//                startActivity(intent)
//                true
//            }
//        }
//
//    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}


