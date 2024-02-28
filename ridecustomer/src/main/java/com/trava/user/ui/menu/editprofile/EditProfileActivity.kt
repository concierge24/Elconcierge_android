package com.trava.user.ui.menu.editprofile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.trava.user.R
import com.trava.user.databinding.ActivityEditProfileBinding
import com.trava.user.utils.*
import com.trava.user.utils.PermissionUtils
import com.trava.utilities.*
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.webservices.models.AppDetail
import com.trava.utilities.webservices.models.User
import kotlinx.android.synthetic.main.activity_edit_profile.*
import permissions.dispatcher.*
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

@RuntimePermissions
class EditProfileActivity : AppCompatActivity(), EditProfileContract.View {

    private val presenter = EditProfilePresenter()
    private var dialogIndeterminate: DialogIndeterminate? = null
    var binding: ActivityEditProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        binding?.color = ConfigPOJO.Companion

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
        tvSave.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)

        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(this)

        etName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.gray_color, GradientDrawable.RECTANGLE)
        etEmail.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.gray_color, GradientDrawable.RECTANGLE)
        etPhone.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.gray_color, GradientDrawable.RECTANGLE)
        llPhoneCode.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.gray_color, GradientDrawable.RECTANGLE)
        divider1.visibility = View.GONE
        divider2.visibility = View.GONE
        divider3.visibility = View.GONE


        setData()
        setListeners()
    }

    private fun setListeners() {
        tvBack.setOnClickListener { onBackPressed() }
        ivProfilePic.setOnClickListener { getStorageWithPermissionCheck() }
        tvSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phoneNumber = etPhone.text.toString().trim()
            if (name.isEmpty()) {
                rootView.showSnack(R.string.name_empty_validation_message)
            } else if (phoneNumber.isEmpty()) {
                rootView.showSnack(R.string.phone_empty_validation_message)
            } else if (ValidationUtils.isPhoneNumberAllZeros(phoneNumber)) {
                rootView.showSnack(R.string.phone_valid_validation_message)
            } else if (!isValidEmail(email)) {
                rootView.showSnack(R.string.email_valid_validation_message)
            } else {
                editProfileApiCall(name, email, phoneNumber, countryCodePicker?.selectedCountryCodeWithPlus ?: "")
            }
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

    private fun setData() {
        val userData = SharedPrefs.with(this).getObject(PROFILE, AppDetail::class.java)
        etName.setText(userData.user?.name)
        if (userData?.profile_pic_url?.isEmpty() == false) {
            ivProfilePic.setRoundProfileUrl(userData.profile_pic_url)
            ivEdit.hide()
        }

        etEmail.setText(userData?.user?.email ?: "")
        etPhone.setText(userData?.user?.phone_number.toString())
        var gson = Gson()
        var data = gson.toJson(User::class.java)
        if (!userData?.user?.phone_code.isNullOrEmpty()) {
            countryCodePicker?.setCountryForPhoneCode(userData?.user?.phone_code?.toInt() ?: 91)
            countryCodePicker?.showFlag(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun editProfileApiCall(name: String, email: String, phoneNumber: String, phoneCode: String) {
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.updateProfileApiCall(name, email, phoneNumber, phoneCode, File(imagePath))
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    override fun onApiSuccess(response: AppDetail?) {
        Toast.makeText(this, getString(R.string.profile_updated_successfully), Toast.LENGTH_LONG).show()
        SharedPrefs.with(this).save(PROFILE, response)
        finish()
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            rootView.showSnack(error ?: "")
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    fun getStorage() {
        ImageUtils.displayImagePicker(this)
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(ivProfilePic.context, R.string.permission_required_to_select_image, request)
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(ivProfilePic.context,
                R.string.permission_required_to_select_image)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private var imagePath: String? = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ImageUtils.REQ_CODE_CAMERA_PICTURE -> {
                    val file = ImageUtils.getFile(this)
                    ivProfilePic.setRoundProfilePic(file)
                    imagePath = file.absolutePath
                }

                ImageUtils.REQ_CODE_GALLERY_PICTURE -> {
                    data?.let {
                        val file = ImageUtils.getImagePathFromGallery(this, data.data!!)
                        ivProfilePic.setRoundProfilePic(file)
                        imagePath = file.absolutePath
                    }
                }
            }
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
