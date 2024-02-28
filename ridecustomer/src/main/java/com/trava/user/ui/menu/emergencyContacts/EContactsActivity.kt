package com.trava.user.ui.menu.emergencyContacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.trava.user.R
import com.trava.user.utils.AppUtils
import com.trava.user.webservices.models.EContact
import com.trava.driver.ui.home.emergencyContacts.EContactPresenter
import com.trava.driver.ui.home.emergencyContacts.EContactsContract
import com.trava.user.databinding.ActivityContactsBinding
import com.trava.user.ui.menu.emergencyContacts.contacts.ContactsActivity
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.PermissionUtils
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.contacts.ContactModel
import com.trava.user.webservices.models.contacts.NewContactModel
import com.trava.user.webservices.models.eContacts.EContactsListing
import com.trava.utilities.*
import kotlinx.android.synthetic.main.activity_contacts.*
import org.json.JSONArray
import org.json.JSONObject
import permissions.dispatcher.*
import permissions.dispatcher.PermissionRequest
import java.util.*
import kotlin.collections.ArrayList

@RuntimePermissions
class EContactsActivity : AppCompatActivity(), EContactsContract.View,SosContactsAdapter.OnDeleteButton {

    private var eContactsListing = ArrayList<EContactsListing>()
    var binding : ActivityContactsBinding ?= null
    private val presenter = EContactPresenter()
    private var selectedContactsList = ArrayList<ContactModel>()

    private var newContactModel = ArrayList<NewContactModel>()
    private var nnewContactModel : NewContactModel ?= null
    private var dialogIndeterminate: DialogIndeterminate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_contacts)
        binding?.color = ConfigPOJO.Companion
        dialogIndeterminate = DialogIndeterminate(this)

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)

        presenter.attachView(this)
        ivBack.setOnClickListener {
            onBackPressed()
        }
        setAdapter()
        presenter.getContactsList()

        binding?.addContacts?.setOnClickListener {
            getUserContactsWithPermissionCheck()
        }
    }

    private fun setAdapter() {
        rvSosContactList.layoutManager = LinearLayoutManager(this)
        rvSosContactList.adapter = SosContactsAdapter(this,eContactsListing,this@EContactsActivity)
    }

    override fun onApiSuccess(response: List<EContact>?) {
        if (response?.isNotEmpty() == true) {
            flipperContacts.displayedChild = 1
        } else {
            flipperContacts.displayedChild = 2
        }

        presenter.getSosContactsList()
    }

    override fun onApiSuccess() {
       presenter.getSosContactsList()
    }

    override fun onSosApiSuccess(response: List<EContactsListing>?) {
        rvSosContactList.visibility = View.VISIBLE
        eContactsListing.clear()
        eContactsListing.addAll(response!!)
        if (eContactsListing.size>0)
        {
            tv_error.visibility=View.GONE
        }
        else
        {
            tv_error.visibility=View.VISIBLE
        }
        rvSosContactList.adapter?.notifyDataSetChanged()
    }

    override fun onDeleteSuccess() {
        presenter.getSosContactsList()
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
//        rvEContactList?.showSWWerror()
        rvSosContactList?.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        }else {
//            rvEContactList?.showSnack(error.toString())
            rvSosContactList?.showSnack(error.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun getUserContacts() {
        launchContactActivity()
    }


    private fun launchContactActivity() {
        startActivityForResult(Intent(this, ContactsActivity::class.java), Constants.REQUEST_CODE_PICK_CONTACT)
//        startActivityForResult(Intent(this,SosActivity :: class.java),SOS)
    }

    @OnShowRationale(Manifest.permission.READ_CONTACTS)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(add_iv.context, R.string.permission_required_to_select_image, request)
    }

    @OnNeverAskAgain(Manifest.permission.READ_CONTACTS)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(add_iv.context,
                R.string.permission_required_to_select_image)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        newContactModel.clear()
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_PICK_CONTACT && data != null) {
            selectedContactsList = data.getSerializableExtra("contacts") as ArrayList<ContactModel>
            var tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var countryCode = tm.getSimCountryIso()

            countryCode = ConfigPOJO.settingsResponse?.key_value?.iso_code

            for (item in selectedContactsList) {
                item.phoneNumber = AppUtils.getCountryCodeFromPhoneNumber(item.phoneNumber
                        ?: "", countryCode.toUpperCase()).replace("//s".toRegex(), "")
            }

            for (i in 0..selectedContactsList.size-1){
                if(!TextUtils.isEmpty(selectedContactsList.get(i).phoneCode)){
                    nnewContactModel = NewContactModel(selectedContactsList.get(i).phoneNumber.toString().trim(),selectedContactsList.get(i).phoneCode.toString(),selectedContactsList.get(i).name.toString().trim())
                }else{
                    var phoneNumb = selectedContactsList.get(i).phoneNumber.toString()
                    nnewContactModel = NewContactModel(phoneNumb.substring(phoneNumb.indexOf(" ")+1),countryCode.toUpperCase(),selectedContactsList.get(i).name.toString().trim())
                }
                newContactModel.add(nnewContactModel!!)
            }

            if(newContactModel.size>0){
                var arr = JSONArray()
                for (i in newContactModel.indices) {
                    var obj = JSONObject()
                    obj.put("phone_number", newContactModel[i].phone_number?.replace(" ","")?.trim().toString())
                    obj.put("phone_code", newContactModel[i].phone_code.toString())
                    obj.put("name",newContactModel[i].name.toString())
                    arr.put(obj)
                }
                Log.e("AsasasaS",arr.toString())
                presenter.saveContactList(arr)
            }
        }
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun OnContactDelete(pos: Int) {
       presenter.deleteSosContact(eContactsListing.get(pos).emergency_contact_id.toString())
    }
}