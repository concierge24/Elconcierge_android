package com.trava.user.ui.home.deliverystarts.contacts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import com.trava.user.R
import com.trava.user.utils.MyAnimationUtils
import com.trava.user.webservices.models.contacts.ContactModel
import com.trava.utilities.LocaleManager
import com.trava.utilities.show
import kotlinx.android.synthetic.main.activity_contacts2.*
import java.util.*
import kotlin.collections.ArrayList

class ContactsActivity : AppCompatActivity(),ContactInterface {

    private var adapter: ContactListAdapter? = null
    private var contactList = ArrayList<ContactModel>()
    private var notExistPhoneList = ArrayList<ContactModel>()
    private var selectedContactCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts2)
        intialize()
        setListeners()
    }

    private fun intialize() {
        fetchingContactsList()
        searchListener()
    }

    private fun fetchingContactsList(){
        val phones = contentResolver?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones?.moveToNext()!!) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//            val contactId = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            contactList.add(ContactModel(phoneNumber, name,null,false))
        }
        phones.close()
        adapter = ContactListAdapter(this)
        rvContact.adapter = adapter
        val sortedAppsList = contactList.sortedBy { it.name?.toUpperCase() }
        val hashSet = LinkedHashSet<ContactModel>()
        hashSet.addAll(sortedAppsList.reversed())
        contactList.clear()
        contactList.addAll(hashSet)
        adapter?.refreshList(contactList)
    }

    private fun searchListener() {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter?.filter(newText ?: "")
                return false
            }
        })
    }

    private fun setListeners() {
        ivBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED,intent )
            finish()
        }

        tvDone.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("contacts",notExistPhoneList))
            finish()

        }
    }

    override fun getSelectedContactData(contactData: ContactModel) {
        if(flSelectedContact.childCount == 0) {
            MyAnimationUtils.animateShowHideView(animContainer, flSelectedContact, true)
        }
        for (i in contactList.indices) {
            if (contactList[i].phoneNumber == contactData.phoneNumber) {
                contactList[i].isSelected = true
                if (!notExistPhoneList.contains(contactData)) {
                    notExistPhoneList.add(contactData)
                    selectedContactCount++
                    tvDone.text = String.format("%s", "${getString(R.string.done)}${"("}$selectedContactCount${")"}")
                } else {
                    contactList[i].isSelected = false
                    notExistPhoneList.remove(contactData)
                    selectedContactCount--
                    tvDone.text = String.format("%s", "${getString(R.string.done)}${"("}$selectedContactCount${")"}")
                }
                setSelectedContacts()
                break
            }
        }
    }

    /**ADD Contacts TO FLOW LAYOUT*/
    private fun setSelectedContacts() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        flSelectedContact.removeAllViews()
        for (item in contactList) {
            if (item.isSelected == true) {
                val view = inflater.inflate(R.layout.item_selected_contact_view, null)
                val tvName = view.findViewById<TextView>(R.id.tvName)
                val ivImage = view.findViewById<ImageView>(R.id.ivImage)
                val clContact = view.findViewById<RelativeLayout>(R.id.rlMain)
                view.tag = item.uniqueId
//                view.id = item.id?.toInt() ?: 1
//                tvName.id = item.id?.toInt() ?: 1
                tvName.text = item.name?.split(" ")?.get(0)
                ivImage.setImageResource(R.drawable.ic_placeholder)
                flSelectedContact.addView(view)
                clContact.setOnClickListener {
                    contactList[contactList.indexOf(item)].isSelected = false
                    notExistPhoneList.remove(item)
                    if (selectedContactCount != 0){
                        selectedContactCount--
                        tvDone.text = String.format("%s","${getString(R.string.done)}${"("}$selectedContactCount${")"}")
                    }
                    setSelectedContacts()
                }
            }
        }

        if (flSelectedContact.childCount > 0) {
            val view = View.inflate(this, R.layout.item_selected_contact_view, null)
            val clContact = view.findViewById<RelativeLayout>(R.id.rlMain)
            clContact.visibility = View.INVISIBLE
            flSelectedContact.addView(view)
        }

        if(selectedContactCount == 0){
            MyAnimationUtils.animateShowHideView(animContainer,flSelectedContact,false)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}
