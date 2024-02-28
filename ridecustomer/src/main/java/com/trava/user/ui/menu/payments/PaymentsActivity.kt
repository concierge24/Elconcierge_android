package com.trava.user.ui.menu.payments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.trava.user.R
import com.trava.user.databinding.ActivityPaymentsBinding
import com.trava.user.ui.home.WebViewActivity
import com.trava.user.ui.home.payment.*
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.cards_model.AddCardResponse
import com.trava.user.webservices.models.cards_model.UserCardData
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.utilities.*
import com.trava.utilities.constants.*
import kotlinx.android.synthetic.main.activity_payments.*
import kotlinx.android.synthetic.main.activity_payments.rootView
import java.util.*
import kotlin.collections.ArrayList

class PaymentsActivity : AppCompatActivity(), View.OnClickListener, SavingCardContract.View, SavedcardInterface {

    private var presenter = SavingCardPresenter()
    private var addCardUrl = ""
    private var cardsList = ArrayList<UserCardData>()
    private var adapter: SavingCardsAdapter? = null
    private var deletecardDialog: AlertDialog? = null
    private var adapterPosition = 0
    var binding : ActivityPaymentsBinding ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_payments)
        binding!!.color = ConfigPOJO.Companion
        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
        presenter.attachView(this)
        intialize()
        setListener()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvBack -> {
                finish()
            }
        }
    }

    private fun intialize() {
        viewFlipperCards.displayedChild = 1
        adapter = SavingCardsAdapter(this)
        rvCards.adapter = adapter
        hitAddcardApi()
    }

    private fun hitAddcardApi() {
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.requestUserCreditPoints()
            presenter.addCardApi()
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    private fun setListener() {
        ivBack.setOnClickListener { super.onBackPressed() }
        tvAddCard.setOnClickListener {
            if (addCardUrl.isNotEmpty()) {
                startActivityForResult(Intent(this, WebViewActivity::class.java)
                        .putExtra(URL_TO_LOAD, addCardUrl)
                        .putExtra(TITLE, getString(R.string.add_debit_card)), Constants.ADD_CARD_REQUEST_CODE)
            }
        }
    }

    override fun onAddCardApiSuccess(data: AddCardResponse) {
        this.addCardUrl = data.url.replace(" ", "")
    }


    override fun getSavedCardData(actionType: String, adapterPosition: Int, cardData: UserCardData) {
        this.adapterPosition = adapterPosition
        when (actionType) {
            Constants.DELETE_CARD -> {
                if (cardsList.size > 1) {
                    if (deletecardDialog == null) {
                        deletecardDialog = AlertDialogUtil.getInstance().createOkCancelDialog(this, R.string.deleteCard,
                                R.string.deleteCardAlertMsg, R.string.ok, R.string.cancel, false,
                                object : AlertDialogUtil.OnOkCancelDialogListener {
                                    override fun onOkButtonClicked() {
                                        deletecardDialog?.dismiss()
                                        deletecardDialog = null
                                        hitRemoveCardApi(cardData.userCardId ?: 0)
                                    }

                                    override fun onCancelButtonClicked() {
                                        deletecardDialog?.dismiss()
                                        deletecardDialog = null
                                    }
                                })
                        deletecardDialog?.show()
                    }
                }else{
                    rootView.showSnack(getString(R.string.atleastOneCard))
                }
            }

            Constants.MAKE_PAYMENT -> {
//                listener?.makePaymentViaCard(cardData)
//                super.onBackPressed()
            }
        }

    }

    private fun hitRemoveCardApi(userCardId: Int) {
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.removeCardApi(userCardId)
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    override fun onRemoveCardSuccess() {
        cardsList.removeAt(adapterPosition)
        adapter?.refreshList(cardsList)
        if (cardsList.isEmpty()) {
            viewFlipperCards?.displayedChild = 2
        }
    }

    override fun showLoader(isLoading: Boolean) {
        if (isLoading) {
            viewFlipperCards.displayedChild = 0
        }
    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            rootView?.showSnack(error.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.ADD_CARD_REQUEST_CODE) {
            if (CheckNetworkConnection.isOnline(this)) {
                presenter.requestUserCreditPoints()
            } else {
                CheckNetworkConnection.showNetworkError(rootView)
            }
        }
    }

    override fun onCreditPointsSucess(response: RideShareResponse) {
        adapter?.refreshList(response.cards)
        viewFlipperCards.displayedChild = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}
