package com.trava.user.ui.home.deliverystarts.contacts

import com.trava.user.webservices.models.contacts.ContactModel


/**
 * Created by ubuntu-android on 3/9/18.
 */
interface ContactInterface {
    fun getSelectedContactData(contactData: ContactModel)
}