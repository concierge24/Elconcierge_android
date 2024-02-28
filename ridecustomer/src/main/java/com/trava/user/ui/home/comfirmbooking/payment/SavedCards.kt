package com.trava.user.ui.home.comfirmbooking.payment

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.CardModel
import com.trava.utilities.CheckNetworkConnection
import com.trava.utilities.LocaleManager
import com.trava.utilities.showSnack
import kotlinx.android.synthetic.main.activity_saved_cards.*
import kotlinx.android.synthetic.main.activity_saved_cards.ivBack
import kotlinx.android.synthetic.main.activity_saved_cards.rootView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.error

class SavedCards : AppCompatActivity(), PaymentContract.View, SavedcardInterface {

    private val presenter = PaymentPresenter()
    private var adapterPosition = 0
    private lateinit var adapter: SavingCardsAdapter
    private val listBookings = ArrayList<CardModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_cards)
        presenter.attachView(this)
        ivBack.setOnClickListener {
            onBackPressed()
        }
        val statusColor= Color.parseColor(ConfigPOJO.primary_color)
        StaticFunction.setStatusBarColor(this, statusColor)
        tvAddNewCard.background= StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        tvAddNewCard.setOnClickListener {
            startActivityForResult(Intent(this, AddNewCard::class.java),100)
        }

        setAdapter()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==100 && resultCode==101)
        {
            error.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun onResume() {
        super.onResume()
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.getCards()
        }else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    private fun setAdapter() {
        rv_list.layoutManager = LinearLayoutManager(this)
        adapter = SavingCardsAdapter(this)
        rv_list.adapter = adapter
    }

    override fun onApiSuccess(response: ArrayList<CardModel>?) {
        if (response?.size!! > 0) {
            listBookings.clear()
            listBookings.addAll(response)
            adapter.refreshList(listBookings)
            error.visibility = View.GONE
        } else {
            error.visibility = View.VISIBLE
        }
    }

    override fun onApiSuccess(isDeleted: Boolean) {

            listBookings.removeAt(adapterPosition)
            adapter.notifyDataSetChanged()

            if (listBookings.size == 0) {
                error.visibility = View.VISIBLE
            } else {
                error.visibility = View.GONE
            }

    }

    override fun onApiSuccess(response: CardModel?)
    {
        Log.e("response", "" + response?.deleted)
        if (response?.deleted?:true)
        {
            listBookings.removeAt(adapterPosition)
            adapter.notifyDataSetChanged()

            if (listBookings.size == 0) {
                error.visibility = View.VISIBLE
            } else {
                error.visibility = View.GONE
            }

        }
    }

    override fun showLoader(isLoading: Boolean) {
        Log.e("isLoading", "" + isLoading)
    }

    override fun apiFailure() {
        rv_list.showSnack(getString(R.string.sww_error))
    }

    override fun handleApiError(code: Int?, error: String?) {
        rv_list.showSnack(error.toString())
    }

    override fun getSavedCardData(actionType: String, adapterPosition: Int, cardData: CardModel) {
        this.adapterPosition = adapterPosition
        if (CheckNetworkConnection.isOnline(this)) {
            if (actionType == "selectForPayment") {

                val intent=Intent()
                intent.putExtra("card_id",listBookings[adapterPosition].user_card_id)
                intent.putExtra("customer_id",listBookings[adapterPosition].customer_token)
                intent.putExtra("last_four",listBookings[adapterPosition].last4)
                setResult(101,intent)
                finish()

            } else {
                showAlertDialog(getString(R.string.alert_delete_card), cardData)
            }
        } else {
            CheckNetworkConnection.showNetworkError(rv_list)
        }
    }

    private fun showAlertDialog(msg: String, cardData: CardModel) {

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog?.dismiss()
            if (CheckNetworkConnection.isOnline(this)) {
                deleteCardAPI(cardData)
                dialog?.dismiss()
            } else {
                Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(R.string.no) { dialog, _ -> dialog?.dismiss() }
        builder.show()
    }

    private fun deleteCardAPI(cardData: CardModel) {
        val map = HashMap<String, Any>()
        map["user_card_id"] = cardData.user_card_id?:""
        map["gateway_unique_id"] = ConfigPOJO.gateway_unique_id.toString()
        presenter.deleteCards(map)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}