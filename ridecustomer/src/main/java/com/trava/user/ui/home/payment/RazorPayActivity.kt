package com.trava.user.ui.home.payment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import kotlinx.android.synthetic.main.activity_razor_pay.*
import org.json.JSONObject


class RazorPayActivity : AppCompatActivity() ,PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_razor_pay)

        etAddAmount.setText("100 " + ConfigPOJO.currency)

        tvSendMoney.setOnClickListener {
                startPayment()
        }

    }

    fun startPayment() {
        val activity: Activity = this
        val co = Checkout()
        try {
            val options = JSONObject()
            options.put("name", "CodeBrew Innovations")
            options.put("description", "Payment")
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://rzp-mobile.s3.amazonaws.com/images/rzp.png")
            options.put("currency", ConfigPOJO.currency)
            val payment: String = "100"
            // amount is in paise so please multiple it by 100
            //Payment failed Invalid amount (should be passed in integer paise. Minimum value is 100 paise, i.e. ₹ 1)
            var total = payment.toDouble()
            total = total * 100
            options.put("amount", total)
            val preFill = JSONObject()
            preFill.put("email", "navdeep@gmail.com")
            preFill.put("contact", "9144040888")
            options.put("prefill", preFill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentError(s: Int, i: String?) {
        Log.e("STATUS", "error code " + java.lang.String.valueOf(i) + " -- Payment failed " + s.toString())
        try {
            Toast.makeText(this, "Payment error please try again", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            Log.e("OnPaymentError", "Exception in onPaymentError", e)
        }    }

    override fun onPaymentSuccess(s: String?) {
// payment successfull pay_DGU19rDsInjcF2
        Log.e("STATUS", " payment successfull "+ s.toString());
        Toast.makeText(this, "Payment successfully done! " +s, Toast.LENGTH_SHORT).show();
    }
}