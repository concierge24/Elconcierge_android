package com.trava.user.ui.home.comfirmbooking.payment

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import com.trava.user.R
import com.trava.user.ui.home.payment.RazorPayActivity
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.CardModel
import com.trava.utilities.CheckNetworkConnection
import com.trava.utilities.LocaleManager
import com.trava.utilities.showSnack
import io.conekta.conektasdk.Conekta
import io.conekta.conektasdk.Token.CreateToken
import kotlinx.android.synthetic.main.activity_add_card.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AddNewCard : AppCompatActivity(), CardInterface, PaymentContract.View {
    override fun onApiSuccess(response: ArrayList<CardModel>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onApiSuccess(isDeleted: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var listOfPattern = ArrayList<String>()
    private var keyDel = 0
    private var isDefault = false
    private var isDefaultStatus = ""
    private var cardBrand = ""
    private val presenter = PaymentPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)
        presenter.attachView(this)
        val statusColor= Color.parseColor(ConfigPOJO.primary_color)
        StaticFunction.setStatusBarColor(this, statusColor)
        ivBack.setOnClickListener {
            onBackPressed()
        }
        tvSaveCard.background=StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        if(ConfigPOJO.gateway_unique_id.equals("conekta")|| ConfigPOJO.gateway_unique_id.equals("peach")){
            etLastName.visibility = View.GONE
        }

        cardExpiresFormatting()
        setCardRegxList()
        etCardNo.addTextChangedListener(FourDigitCardFormatWatcher(etCardNo, listOfPattern, this))
        tvSetAsDefault.setOnClickListener {
            if (!isDefault) {
                isDefault = true
                isDefaultStatus = "1"
                tvSetAsDefault.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_1, 0, 0, 0)
            } else {
                isDefault = false
                isDefaultStatus = "0"
                tvSetAsDefault.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_0, 0, 0, 0)
            }
        }

        tvSaveCard.setOnClickListener {
            when{
               ConfigPOJO.gateway_unique_id.equals("stripe") -> {
                   if (ConfigPOJO.settingsResponse?.key_value?.stripe_public_key!=null||ConfigPOJO.settingsResponse?.key_value?.stripe_secret_key!=null)
                   {
                       saveStripebtnClick()
                   }
                   else{
                       rootView.showSnack("Stripe details not added properly")
                   }
               }
                ConfigPOJO.gateway_unique_id.equals("epayco") -> {
                    saveEPayCobtnClick()
                }
                ConfigPOJO.gateway_unique_id.equals("peach") -> {
                    savePeachbtnClick()
                }
                ConfigPOJO.gateway_unique_id.equals("conekta") -> {
                    saveConektaBtnClick()
                }
                ConfigPOJO.gateway_unique_id.equals("razorpay")->{
//                    startActivity(Intent(this,RazorPayActivity::class.java))
                    saveRazorPayCard()
                }
            }
        }
    }

    private fun saveConektaBtnClick() {
        Conekta.setPublicKey(ConfigPOJO.conekta_api_key);
        Conekta.collectDevice(this); //Collect device*/

        val cardNo = etCardNo.text.toString()
        val cardExpires = etExpires.text.toString().trim()
        val cvv = etCvv.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()

        if (cardExpires.length < 4) {
            rootView.showSnack("Invalid expiry date")
            return
        }
        p_bar.visibility = View.VISIBLE
        val cardExpiresYear = cardExpires.substring(2,cardExpires.toString().length).toInt()
        val cardExpMonth = cardExpires.substring(0, 2).toInt()

        val yr= "20$cardExpiresYear"
        val card = io.conekta.conektasdk.Card(firstName.toString(),
                cardNo.toString(),
                cvv.toString(),
                cardExpMonth.toString(),
                yr)


        val token = io.conekta.conektasdk.Token(this)



        token.onCreateTokenListener(CreateToken { data ->
            try {
                Log.d("The token::::", data.getString("id"))

                val map = HashMap<String, Any>()
                map["token_id"] = data.getString("id")
                map["gateway_unique_id"] = ConfigPOJO.gateway_unique_id.toString()
                showLoader(true)
                presenter.saveCard(map = map)
            } catch (error: java.lang.Exception) {
                Log.d("Error", error.toString())
            }
        })
        token.create(card) //Create token
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun saveStripebtnClick() {

        val cardNo = etCardNo.text.toString()
        val cardExpires =etExpires.text.toString().trim()
        val cvv = etCvv.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()

        if (CheckNetworkConnection.isOnline(this)) {
            if (validateDataStripe(cardNo, cardExpires, cvv, firstName, lastName)) {
                if (cardExpires.length < 4) {
                    rootView.showSnack("Invalid expiry date")
                    return
                }
                p_bar.visibility = View.VISIBLE

                val cardExpiresYear = cardExpires.substring(2,cardExpires.toString().length).toInt()

                val stripe = Stripe(this, ConfigPOJO.STRIPE_PUBLIC_KEY)

                val yr= "20$cardExpiresYear"
                val card = Card.create(cardNo,
                        cardExpires.substring(0, 2).toInt(),
                        yr.toInt(),
                        cvv)

                var vCard = Card.Builder(card.number, card.expMonth, card.expYear, card.cvc)
                        .name(firstName)
                        .addressLine1(lastName)
                        .build()

                stripe.createCardToken(vCard,  UUID.randomUUID().toString(),callback = object : ApiResultCallback<Token> {
                    override fun onSuccess(result: Token) {
                        Log.d("The token::::", result.id)

                        val map = HashMap<String, Any>()
                        map["token_id"] = result.id
                        map["gateway_unique_id"] = ConfigPOJO.gateway_unique_id.toString()
                        presenter.saveCard(map = map)
                    }

                    override fun onError(e: Exception) {
                        p_bar.visibility=View.GONE
                        rootView.showSnack(e.message?:"")
                        Log.e("StripeExample", "Error while creating card token", e)
                    }
                })
            }
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    private fun saveEPayCobtnClick() {
        val cardNo = etCardNo.text.toString()
        val cardExpires =etExpires.text.toString().trim()
        val cvv = etCvv.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val LastName = etLastName.text.toString().trim()

        if (cardExpires.length < 4) {
            rootView.showSnack("Invalid expiry date")
            return
        }

        if (CheckNetworkConnection.isOnline(this)) {
            if (validateData(cardNo, cardExpires, cvv, firstName, LastName)) {
                p_bar.visibility = View.VISIBLE

                val map = HashMap<String, Any>()
                map["card_number"] = cardNo
                map["card_holder_name"] = firstName
                map["email"] = LastName
                map["exp_month"] = cardExpires.substring(0, 2)
                map["exp_year"] = "20"+cardExpires.substring(2, cardExpires.length)
                map["cvc"] = cvv
                map["gateway_unique_id"] = ConfigPOJO.gateway_unique_id.toString()
                presenter.saveCard(map = map)
            }
        } else {
            p_bar.visibility=View.GONE
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    private fun savePeachbtnClick() {
        val cardNo = etCardNo.text.toString()
        val cardExpires =etExpires.text.toString().trim()
        val cvv = etCvv.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val LastName = etLastName.text.toString().trim()

        if (cardExpires.length < 4) {
            rootView.showSnack("Invalid expiry date")
            return
        }

        if (CheckNetworkConnection.isOnline(this)) {
            if (validateData(cardNo, cardExpires, cvv, firstName, LastName)) {
                p_bar.visibility = View.VISIBLE

                val map = HashMap<String, Any>()
                map["card_number"] = cardNo
                map["card_brand"] = cardBrand
                map["card_holder_name"] = firstName
//                map["email"] = LastName
                map["exp_month"] = cardExpires.substring(0, 2)
                map["exp_year"] = "20"+cardExpires.substring(2, cardExpires.length)
                map["cvc"] = cvv
                map["gateway_unique_id"] = ConfigPOJO.gateway_unique_id.toString()
                presenter.saveCard(map = map)
            }
        } else {
            p_bar.visibility=View.GONE
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }


    private fun saveRazorPayCard() {
        val cardNo = etCardNo.text.toString()
        val cardExpires =etExpires.text.toString().trim()
        val cvv = etCvv.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val LastName = etLastName.text.toString().trim()

        if (cardExpires.length < 4) {
            rootView.showSnack("Invalid expiry date")
            return
        }

        if (CheckNetworkConnection.isOnline(this)) {
            if (validateData(cardNo, cardExpires, cvv, firstName, LastName)) {
                p_bar.visibility = View.VISIBLE

                val map = HashMap<String, Any>()
                map["card_number"] = cardNo
                map["card_brand"] = cardBrand
                map["card_holder_name"] = firstName
//                map["email"] = LastName
                map["exp_month"] = cardExpires.substring(0, 2)
                map["exp_year"] = "20"+cardExpires.substring(2, cardExpires.length)
                map["cvc"] = cvv
                map["gateway_unique_id"] = ConfigPOJO.gateway_unique_id.toString()
                presenter.saveCard(map = map)
            }
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    override fun getSelectedCardData(position: Int) {
        when (position) {
            0 -> {
                cardBrand="VISA"
                ivCard.setImageResource(R.drawable.ic_visa_new)
            }
            1 -> {
                cardBrand="MASTER CARD"
                ivCard.setImageResource(R.drawable.ic_mastercard)
            }
            2 -> {
                cardBrand="AMEX"
                ivCard.setImageResource(R.drawable.ic_amex)
            }
            3 -> {
                cardBrand="DINERS"
                ivCard.setImageResource(R.drawable.ic_diners)
            }
            4 -> {
                cardBrand="DISCOVER"
                ivCard.setImageResource(R.drawable.ic_discover)
            }
            5 -> {
                cardBrand="JCB"
                ivCard.setImageResource(R.drawable.ic_jcb)
            }
            6 -> {
                cardBrand="UNION PAY"
                ivCard.setImageResource(R.drawable.ic_up)
            }
            7 -> {
                cardBrand="ELO"
                ivCard.setImageResource(R.drawable.ic_elo)
            }
            8 -> {
                cardBrand="HIPER"
                ivCard.setImageResource(R.drawable.ic_hiper)
            }
            10 -> {
                ivCard.setImageDrawable(null)
            }
        }
    }

    private fun cardExpiresFormatting() {
        etExpires.addTextChangedListener(ExpiryDateTextWatcher())
    }

    private fun validateData(cardNumber: String, cardExpires: String, cvv: String, firstName: String, lastName: String): Boolean {
        return when {
            cardNumber.isEmpty() -> {
                rootView.showSnack(getString(R.string.error_enter_cardNumber))
                false
            }

            ValidationUtils.isPhoneNumberFormatAllZeros(cardNumber.replace(" ", "")) -> {
                rootView.showSnack(getString(R.string.error_invalid_card))
                false
            }

            cardNumber.length < 16 -> {
                rootView.showSnack(getString(R.string.error_invalid_card))
                false
            }

            cardExpires.isEmpty() -> {
                rootView.showSnack(getString(R.string.error_empty_cardExpires))
                false
            }
            cvv.isEmpty() -> {
                rootView.showSnack(getString(R.string.error_empty_cvv))
                false
            }
            cvv.length < 3 -> {
                rootView.showSnack(getString(R.string.error_valid_cvv))
                false
            }

            firstName.isEmpty() -> {
                rootView.showSnack(getString(R.string.error_empty_firstName))
                false
            }

            !ValidationUtils.checkName(firstName) -> {
                rootView.showSnack(getString(R.string.specialCharactersnot_first))
                false
            }

            /*lastName.isEmpty() -> {
                rootView.showSnack(getString(R.string.email_empty_validation_message))
                false
            }

            lastName.isNotEmpty() -> {
                if (!isValidEmail(lastName)) {
                    rootView.showSnack(getString(R.string.email_valid_validation_message))
                    false
                } else {
                    true
                }
            }*/
            else -> true
        }
    }

    private fun validateDataStripe(cardNumber: String, cardExpires: String, cvv: String, firstName: String, lastName: String): Boolean {
        return when {
            cardNumber.isEmpty() -> {
                rootView.showSnack(getString(R.string.error_enter_cardNumber))
                false
            }

            ValidationUtils.isPhoneNumberFormatAllZeros(cardNumber.replace(" ", "")) -> {
                rootView.showSnack(getString(R.string.error_invalid_card))
                false
            }

            cardNumber.length < 16 -> {
                rootView.showSnack(getString(R.string.error_invalid_card))
                false
            }

            cardExpires.isEmpty() -> {
                rootView.showSnack(getString(R.string.error_empty_cardExpires))
                false
            }
            cvv.isEmpty() -> {
                rootView.showSnack(getString(R.string.error_empty_cvv))
                false
            }
            cvv.length < 3 -> {
                rootView.showSnack(getString(R.string.error_valid_cvv))
                false
            }

            firstName.isEmpty() -> {
                rootView.showSnack(getString(R.string.error_empty_firstName))
                false
            }

            !ValidationUtils.checkName(firstName) -> {
                rootView.showSnack(getString(R.string.specialCharactersnot_first))
                false
            }

            lastName.isEmpty() -> {
                rootView.showSnack(getString(R.string.address_empty_validation_message))
                false
            }

            else -> true
        }
    }


    fun isValidEmail(target: CharSequence): Boolean {
        val check: Boolean
        val p: Pattern
        val m: Matcher

        val EMAIL_STRING = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

        p = Pattern.compile(EMAIL_STRING)

        m = p.matcher(target)
        check = m.matches()

        if (!check) {
            return false
        }
        return check
    }

    private fun setCardRegxList() {
        listOfPattern = ArrayList<String>()
        val ptVisa = "^4[0-9]{3,}$"
        listOfPattern.add(ptVisa)
        val ptMasterCard = "^5[1-5][0-9]{3,}$"
        listOfPattern.add(ptMasterCard)
        val ptAmeExp = "^3[47][0-9]{5,}$"
        listOfPattern.add(ptAmeExp)
        val ptDinClb = "^3(?:0[0-5]|[68][0-9])[0-9]{4,}$"
        listOfPattern.add(ptDinClb)
        val ptDiscover = "^6(?:011|5[0-9]{2})[0-9]{3,}$"
        listOfPattern.add(ptDiscover)
        val ptJcb = "^(?:2131|1800|35[0-9]{3})[0-9]{3,}$"
        listOfPattern.add(ptJcb)
        val ptUnionPay = "^(62|88)[0-9]{5,}$"
        listOfPattern.add(ptUnionPay)
        val ptElo = "^((((636368)|(438935)|(504175)|(451416)|(636297))[0-9]{0,10})|((5067)|(4576)|(4011))[0-9]{0,12})$"
        listOfPattern.add(ptElo)
        val ptHiperCard = "^(606282|3841)[0-9]{5,}$"
        listOfPattern.add(ptHiperCard)
    }

    override fun onApiSuccess(response: CardModel?) {
        p_bar.visibility = View.GONE
        Toast.makeText(this, "Card added successfully", Toast.LENGTH_SHORT).show()

        var intent=Intent()
        setResult(101,intent)
        finish()
    }

    override fun showLoader(isLoading: Boolean) {
        Log.e("isLoading", "" + isLoading)
    }

    override fun apiFailure() {
        p_bar.visibility = View.GONE
        etCardNo.showSnack(getString(R.string.sww_error))
    }

    override fun handleApiError(code: Int?, error: String?) {
        p_bar.visibility = View.GONE
        etCardNo.showSnack(error.toString())
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}