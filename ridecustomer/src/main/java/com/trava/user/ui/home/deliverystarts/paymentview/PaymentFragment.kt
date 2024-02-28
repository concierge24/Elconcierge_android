package com.trava.user.ui.home.deliverystarts.paymentview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.razorpay.PaymentResultListener
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.complete_ride.CompleteRideFragment
import com.trava.user.ui.home.rating.RatingFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.Constants
import com.trava.utilities.constants.ORDER
import kotlinx.android.synthetic.main.activity_web_view.*

class PaymentFragment : Fragment() {
    private var order: Order? = null
    private lateinit var progressbar: ProgressBar
    private var mListener: ThawaniPaymentInterface? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.payment_web_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressbar = view.findViewById(R.id.progressbar)
        setListeners()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setListeners() {
        order = Gson().fromJson(arguments?.getString("order"), Order::class.java)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressbar.setProgress(progress, true)
                } else {
                    progressbar.progress = progress
                }
            }
        }
        if (ConfigPOJO.gateway_unique_id.toLowerCase() == "wipay") {
            webView.webViewClient = MyWebViewClientt()
        } else {
            webView.webViewClient = MyWebViewClient()
        }
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        if (ConfigPOJO.gateway_unique_id.toLowerCase() == "qpaypro") {
            var url = ConfigPOJO?.settingsResponse?.key_value?.admin_base_url + "/qpaypro/payment?amount=" + order?.payment?.final_charge + "&order_id=" + order?.order_id
            webView.loadUrl(url)
        } else if (ConfigPOJO.gateway_unique_id.toLowerCase() == "paymaya" || ConfigPOJO.gateway_unique_id.toLowerCase() == "thawani") {
            var url = requireArguments().getString("payment_url") ?: ""
            webView.loadUrl(url)
        } else {
            if (arguments?.containsKey("payment_method") != false) {
                webView.loadData(arguments?.getString("payment_method") ?: "", "text/html", "UTF-8")
            } else if (arguments?.containsKey("payment_body") != false) {
                if (ConfigPOJO.gateway_unique_id.toLowerCase() == "wipay") {
                    webView.loadData(arguments?.getString("payment_body")
                            ?: "", "text/html", "UTF-8")
                } else {
                    webView.loadUrl(arguments?.getString("payment_body") ?: "")
                }
            } else {
                if (ConfigPOJO.gateway_unique_id.toLowerCase() == "wipay") {
                    webView.loadData(order?.payment?.payment_body ?: "", "text/html", "UTF-8")
                } else {
                    webView.loadUrl(order?.payment?.payment_body ?: "")
                }
            }
        }
    }

    inner class MyWebViewClientt : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressbar.visibility = View.GONE
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressbar.progress = 0
            progressbar.visibility = View.VISIBLE
        }

        @Suppress("OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val uri = Uri.parse(url)
            return handleUri(uri)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

            val uri = request?.url
            return handleUri(uri)
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            val message: String
            message = when (error.getPrimaryError()) {
                SslError.SSL_DATE_INVALID -> "SSL_DATE_INVALID"
                SslError.SSL_EXPIRED -> "SSL_EXPIRED"
                SslError.SSL_IDMISMATCH -> "SSL_IDMISMATCH"
                SslError.SSL_INVALID -> "SSL_INVALID"
                SslError.SSL_NOTYETVALID -> "SSL_NOTYETVALID"
                SslError.SSL_UNTRUSTED -> "SSL_UNTRUSTED"
                else -> "CERTIFICATE INVALID"
            }

            val builder = AlertDialog.Builder(activity)
            builder.setMessage(message)
            builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog?.dismiss()
                handler.proceed()
            }
            builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog?.dismiss()
                handler.cancel()
            }
            builder.show()
        }


        private fun handleUri(uri: Uri?): Boolean {
            Log.e("asasasasasAS", uri.toString())
            return if (uri.toString().contains(Constants.SOCKET_URL)) {
                showRating()
                false
            } else {
                webView.loadUrl(uri.toString())
                true
            }
        }
    }


    private fun openRating() {
        fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment = RatingFragment()
        val bundle = Bundle()
        bundle.putString(ORDER, Gson().toJson(order))
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
    }

    private fun openRatingFragment() {
        fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment = CompleteRideFragment()
        val bundle = Bundle()
        bundle.putString("order", Gson().toJson(order))
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressbar.visibility = View.GONE
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressbar.progress = 0
            progressbar.visibility = View.VISIBLE
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            view?.loadUrl(url ?: "")
            if (ConfigPOJO.gateway_unique_id.toLowerCase() == "qpaypro") {
                if (url?.contains("success") == true) {
                    showRating()
                } else if (url?.contains("error") == true) {
                    Toast.makeText(activity, "Payment Failed", Toast.LENGTH_SHORT).show()
                    activity?.finish()
                    activity?.startActivity(Intent(activity, HomeActivity::class.java))
                }
            } else if (ConfigPOJO.gateway_unique_id.toLowerCase() == "thawani") {
                if (url?.contains("success") == true) {
                    val uri = Uri.parse(url)
                    mListener?.grtPayment(uri.getQueryParameter("txn_id").toString(),Gson().toJson(order))
                    fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                } else {
                    Toast.makeText(activity, "Payment Failed", Toast.LENGTH_SHORT).show()
                    activity?.finish()
                    activity?.startActivity(Intent(activity, HomeActivity::class.java))
                }
            } else {
                if (url?.contains(Constants.SOCKET_URL) == true) {
                    openRating()
                }
            }
            return true
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            val message: String
            message = when (error.getPrimaryError()) {
                SslError.SSL_DATE_INVALID -> "SSL_DATE_INVALID"
                SslError.SSL_EXPIRED -> "SSL_EXPIRED"
                SslError.SSL_IDMISMATCH -> "SSL_IDMISMATCH"
                SslError.SSL_INVALID -> "SSL_INVALID"
                SslError.SSL_NOTYETVALID -> "SSL_NOTYETVALID"
                SslError.SSL_UNTRUSTED -> "SSL_UNTRUSTED"
                else -> "CERTIFICATE INVALID"
            }

            val builder = AlertDialog.Builder(activity)
            builder.setMessage(message)
            builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog?.dismiss()
                handler.proceed()
            }
            builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog?.dismiss()
                handler.cancel()
            }
            builder.show()
        }

    }

    private fun showRating() {
        openRatingFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ThawaniPaymentInterface)
            mListener = context
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
}

interface ThawaniPaymentInterface {
    fun grtPayment(transId: String, orderData: String)
}





