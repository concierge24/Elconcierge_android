package com.trava.user.ui.home.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.trava.user.R
import com.trava.user.databinding.FragmentCardsBinding
import com.trava.user.ui.home.WebViewActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.cards_model.AddCardResponse
import com.trava.user.webservices.models.cards_model.UserCardData
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.utilities.*
import com.trava.utilities.constants.TITLE
import com.trava.utilities.constants.URL_TO_LOAD
import kotlinx.android.synthetic.main.fragment_cards.*
import kotlinx.android.synthetic.main.fragment_cards.rootView

class SavingCardsFragment : Fragment(),SavingCardContract.View,SavedcardInterface {

    private var presenter = SavingCardPresenter()
    private var addCardUrl = ""
    private var cardsList = ArrayList<UserCardData>()
    private var adapter : SavingCardsAdapter? = null
    private var listener : CardPaymentInterface? = null
    private var deletecardDialog : AlertDialog? = null
    private var adapterPosition = 0
    lateinit var cardsBinding: FragmentCardsBinding
    fun setCardPaymentListener(listener : CardPaymentInterface){
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        cardsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cards, container, false)
        cardsBinding.color = ConfigPOJO.Companion
        val view=cardsBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        intialize()
        setListener()
    }

    private fun intialize() {
        if(arguments!=null){
           cardsList = arguments?.getParcelableArrayList(Constants.CARDS_DATA)?:ArrayList()
            adapter = SavingCardsAdapter(this)
            rvCards.adapter = adapter
            adapter?.refreshList(cardsList)
            viewFlipperCards.displayedChild = 1
            if(cardsList.isEmpty()){
                viewFlipperCards?.displayedChild = 2
            }
        }
        hitAddcardApi()
    }

    private fun hitAddcardApi(){
        if(CheckNetworkConnection.isOnline(context)){
            presenter.addCardApi()
        }else{
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    private fun setListener() {
        ivBack.setOnClickListener { activity?.onBackPressed() }
        tvAddCard.setOnClickListener {
            if(addCardUrl.isNotEmpty()) {
                startActivityForResult(Intent(context, WebViewActivity::class.java)
                        .putExtra(URL_TO_LOAD,addCardUrl)
                        .putExtra(TITLE,getString(R.string.add_debit_card)), Constants.ADD_CARD_REQUEST_CODE)
            }
        }
    }

    override fun onAddCardApiSuccess(data : AddCardResponse) {
        this.addCardUrl = data.url.replace(" ","")
    }



    override fun getSavedCardData(actionType: String, adapterPosition: Int, cardData: UserCardData) {
        this.adapterPosition = adapterPosition
        when(actionType){
            Constants.DELETE_CARD -> {
                if(deletecardDialog == null) {
                    deletecardDialog = AlertDialogUtil.getInstance().createOkCancelDialog(context, R.string.deleteCard,
                            R.string.deleteCardAlertMsg, R.string.ok, R.string.cancel, false,
                            object : AlertDialogUtil.OnOkCancelDialogListener {
                                override fun onOkButtonClicked() {
                                    deletecardDialog?.dismiss()
                                    hitRemoveCardApi(cardData.userCardId?:0)
                                }

                                override fun onCancelButtonClicked() {
                                    deletecardDialog?.dismiss()
                                }

                            })
                    deletecardDialog?.show()
                }
            }

            Constants.MAKE_PAYMENT -> {
                listener?.makePaymentViaCard(cardData)
                activity?.onBackPressed()
            }
        }

    }

    private fun hitRemoveCardApi(userCardId : Int){
        if(CheckNetworkConnection.isOnline(context)){
            presenter.removeCardApi(userCardId)
        }else{
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    override fun onRemoveCardSuccess() {
        cardsList.removeAt(adapterPosition)
        adapter?.refreshList(cardsList)
        if(cardsList.isEmpty()){
            viewFlipperCards?.displayedChild = 2
        }
    }

    override fun showLoader(isLoading: Boolean) {
        if(isLoading){
            viewFlipperCards.displayedChild = 0
        }
    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(context as Activity)
        } else {
            rootView?.showSnack(error.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.ADD_CARD_REQUEST_CODE){
            if(CheckNetworkConnection.isOnline(context)){
                presenter.requestUserCreditPoints()
            }else{
                CheckNetworkConnection.showNetworkError(rootView)
            }
        }
    }

    override fun onCreditPointsSucess(response: RideShareResponse) {
        adapter?.refreshList(response.cards)
        viewFlipperCards.displayedChild = 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }
}

interface CardPaymentInterface{
    fun makePaymentViaCard(data : UserCardData)
}