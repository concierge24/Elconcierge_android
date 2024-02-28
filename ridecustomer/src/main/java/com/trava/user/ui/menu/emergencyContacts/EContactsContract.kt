package com.trava.driver.ui.home.emergencyContacts

import com.trava.user.webservices.models.EContact
import com.trava.user.webservices.models.eContacts.EContactsListing
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import org.json.JSONArray
import org.json.JSONStringer

class EContactsContract{

    interface View: BaseView{
        fun onApiSuccess(response: List<EContact>?)
        fun onSosApiSuccess(response: List<EContactsListing>?)
        fun onApiSuccess()
        fun onDeleteSuccess()
    }

    interface Presenter: BasePresenter<View>{
        fun getContactsList()
        fun saveContactList(contactList: JSONArray)
        fun getSosContactsList()
        fun deleteSosContact(id : String)

    }

}