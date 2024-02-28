package com.trava.user.ui.home.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.trava.driver.webservices.models.WalletBalModel
import com.trava.driver.webservices.models.WalletHisResponse
import com.trava.user.R
import com.trava.user.databinding.LayoutSendMoneyBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.SendMoneyResponseModel
import com.trava.user.webservices.models.ThawaniData
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.etPhone
import kotlinx.android.synthetic.main.fragment_login.iv_coutry
import kotlinx.android.synthetic.main.layout_send_money.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class TransferWalletMoneyActivity : AppCompatActivity(), WalletContract.View {
    var binding : LayoutSendMoneyBinding ?= null
    private val presenter = WalletPresenter()
    private var dialogIndeterminate: DialogIndeterminate? = null
    private var availBal=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.layout_send_money)
        binding?.color = ConfigPOJO.Companion
        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(this)

        binding?.tvSendMoney?.setBackgroundColor(Color.parseColor(ConfigPOJO.Btn_Colour));
        binding?.tvSendMoney?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour));

        availBal=intent.getDoubleExtra("balance",0.0)
        initViews()

        binding?.imageView7?.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            binding?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            binding?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }

        binding?.countryCodePicker?.setNumberAutoFormattingEnabled(false)
        binding?.countryCodePicker?.registerCarrierNumberEditText(etPhone) // registers the EditText with the country code picker
        iv_coutry.setOnClickListener {
            binding?.countryCodePicker?.show()
            binding?.countryCodePicker?.setNumberAutoFormattingEnabled(false)
            binding?.countryCodePicker?.registerCarrierNumberEditText(etPhone)
        }

        tvAmtWillBe.text = "${getFormattedPrice(0.00)} ${ConfigPOJO.currency}"
        etAddAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isEmpty()){
                    tvAmtWillBe.text = "${getFormattedPrice(0.00)} ${ConfigPOJO.currency}"
                    tvSendMoney.alpha = 0.6f
                }
                else{
                    if(tvAmtWillBe.text.length<15){
                        tvAmtWillBe.text = "${getFormattedPrice(p0.toString().toDouble())} ${ConfigPOJO.currency}"
                        tvSendMoney.alpha = 1.0f
                    }


                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        binding?.tvSendMoney?.setOnClickListener {
            if(binding?.tvSendMoney?.alpha == 1.0f){
                sendWalletMOney()
            }
        }
    }

    private fun sendWalletMOney() {
        if(dataVerified()){
            if (CheckNetworkConnection.isOnline(this)) {
                presenter.sendWalletMoney(binding?.etAddAmount?.text.toString().toInt()
                        ,binding?.countryCodePicker?.selectedCountryCodeWithPlus.toString(),binding?.etPhone?.text.toString())
            } else {
                Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dataVerified(): Boolean {
        return when {
            binding?.etPhone?.text.toString().trim().isEmpty() -> {
                tvProceed?.showSnack(getString(R.string.phone_validation_message))// Invalid phone number
                false
            }
            binding?.etPhone?.text.toString().trim().length<availBal -> {
                tvProceed?.showSnack(getString(R.string.enter_less_amount))// Invalid phone number
                false
            }
            else -> true
        }
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

    override fun onWalletHistorySuccess(listHistory: WalletHisResponse?) {
        TODO("Not yet implemented")
    }

    override fun onWalletAddedSuccess(response: Order?) {
        TODO("Not yet implemented")
    }

    override fun onWalletBalSuccess(response: WalletBalModel?) {
        TODO("Not yet implemented")
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        TODO("Not yet implemented")
    }

    override fun handleApiError(code: Int?, error: String?) {
        val returnIntent = Intent()
        returnIntent.putExtra("result", error)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onMoneySentSuccessFully(response: SendMoneyResponseModel?) {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish()
    }

    override fun RazorPaymentApiSuccess(apiResponseNew: ApiResponseNew?) {
        TODO("Not yet implemented")
    }

    override fun PayThwaniGeturlSuccess(apiResponseNew: ApiResponse<ThawaniData>?) {

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        dialogIndeterminate?.show(false)

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}