package com.trava.user.ui.signup.entername

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.trava.user.R
import com.trava.user.databinding.FragmentEnterNameBinding
import com.trava.user.databinding.FragmentEnterNameSummerBinding
import com.trava.user.databinding.FragmentEnterNameVinitBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.MainMenuActivity
import com.trava.user.ui.home.VideoPlayerScreen
import com.trava.user.ui.home.comfirmbooking.payment.AddNewCard
import com.trava.utilities.*
import com.trava.utilities.webservices.models.AppDetail
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.ImageUtils
import com.trava.user.utils.PermissionUtils
import com.trava.user.utils.StaticFunction
import com.trava.utilities.Utils.getEditTextFilter
import com.trava.utilities.constants.*
import kotlinx.android.synthetic.main.fragment_enter_name.const_user
import kotlinx.android.synthetic.main.fragment_enter_name.etAddress
import kotlinx.android.synthetic.main.fragment_enter_name.etEinNumber
import kotlinx.android.synthetic.main.fragment_enter_name.etEmail
import kotlinx.android.synthetic.main.fragment_enter_name.etFamilyName
import kotlinx.android.synthetic.main.fragment_enter_name.etFamilyNumber
import kotlinx.android.synthetic.main.fragment_enter_name.etFirstName
import kotlinx.android.synthetic.main.fragment_enter_name.etFullName
import kotlinx.android.synthetic.main.fragment_enter_name.etLastName
import kotlinx.android.synthetic.main.fragment_enter_name.etNationalIdNo
import kotlinx.android.synthetic.main.fragment_enter_name.etNeighbourName
import kotlinx.android.synthetic.main.fragment_enter_name.etNeighbourNumber
import kotlinx.android.synthetic.main.fragment_enter_name.etPhoneNumber
import kotlinx.android.synthetic.main.fragment_enter_name.etRefrral
import kotlinx.android.synthetic.main.fragment_enter_name.frame_address_proof
import kotlinx.android.synthetic.main.fragment_enter_name.frame_id_back
import kotlinx.android.synthetic.main.fragment_enter_name.frame_id_front
import kotlinx.android.synthetic.main.fragment_enter_name.frame_school_id
import kotlinx.android.synthetic.main.fragment_enter_name.imgAddressProof
import kotlinx.android.synthetic.main.fragment_enter_name.imgBackClose
import kotlinx.android.synthetic.main.fragment_enter_name.imgCloseAddressProof
import kotlinx.android.synthetic.main.fragment_enter_name.imgCloseBuilding_id
import kotlinx.android.synthetic.main.fragment_enter_name.imgCloseBuilding_id1
import kotlinx.android.synthetic.main.fragment_enter_name.imgCloseSchool_id
import kotlinx.android.synthetic.main.fragment_enter_name.imgFrontClose
import kotlinx.android.synthetic.main.fragment_enter_name.imgIdBack
import kotlinx.android.synthetic.main.fragment_enter_name.imgIdFront
import kotlinx.android.synthetic.main.fragment_enter_name.imgUser
import kotlinx.android.synthetic.main.fragment_enter_name.img_building_id
import kotlinx.android.synthetic.main.fragment_enter_name.img_building_id1
import kotlinx.android.synthetic.main.fragment_enter_name.img_school_id
import kotlinx.android.synthetic.main.fragment_enter_name.ivBack
import kotlinx.android.synthetic.main.fragment_enter_name.iv_four
import kotlinx.android.synthetic.main.fragment_enter_name.iv_one
import kotlinx.android.synthetic.main.fragment_enter_name.iv_three
import kotlinx.android.synthetic.main.fragment_enter_name.iv_two
import kotlinx.android.synthetic.main.fragment_enter_name.ll_family
import kotlinx.android.synthetic.main.fragment_enter_name.ll_neibhour
import kotlinx.android.synthetic.main.fragment_enter_name.rbGroup
import kotlinx.android.synthetic.main.fragment_enter_name.rbOthers
import kotlinx.android.synthetic.main.fragment_enter_name.rootView
import kotlinx.android.synthetic.main.fragment_enter_name.tvSubmit
import kotlinx.android.synthetic.main.fragment_enter_name.tv_address
import kotlinx.android.synthetic.main.fragment_enter_name.tv_address_proof
import kotlinx.android.synthetic.main.fragment_enter_name.tv_ein
import kotlinx.android.synthetic.main.fragment_enter_name.tv_email
import kotlinx.android.synthetic.main.fragment_enter_name.tv_fName
import kotlinx.android.synthetic.main.fragment_enter_name.tv_id_back
import kotlinx.android.synthetic.main.fragment_enter_name.tv_id_front
import kotlinx.android.synthetic.main.fragment_enter_name.tv_lName
import kotlinx.android.synthetic.main.fragment_enter_name.tv_nationalId
import kotlinx.android.synthetic.main.fragment_enter_name.tv_option
import kotlinx.android.synthetic.main.fragment_enter_name.tv_phone
import kotlinx.android.synthetic.main.fragment_enter_name.tv_refcode
import kotlinx.android.synthetic.main.fragment_enter_name.tv_school_work_id
import permissions.dispatcher.*
import java.io.File

@RuntimePermissions
class EnterNameFragment : Fragment(), EnterNameContract.View, View.OnClickListener {

    private val presenter = EnterNamePresenter()
    lateinit var enterNameBinding: FragmentEnterNameBinding
    lateinit var enterNameBindingVinit: FragmentEnterNameVinitBinding
    lateinit var enterNameSummerBinding: FragmentEnterNameSummerBinding
    private lateinit var dialogIndeterminate: DialogIndeterminate
    private var firstName_check: Boolean = true
    private var lastName_check: Boolean = true
    private var isProfilePic: Boolean = true
    private var isFrontPic: Boolean = true
    private var isBackPic: Boolean = true
    private var isSchoolIdPic: Boolean = true
    private var isAddressProofPic: Boolean = true
    private var pathProfileImage = ""
    private var pathBackImagee = ""
    private var pathFrontImagee = ""
    private var pathSchoolIdImage = ""
    private var pathAddressProofImage = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        enterNameBindingVinit = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_name_vinit, container, false)
        enterNameBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_name, container, false)
        enterNameSummerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_name_summer, container, false)
        enterNameBindingVinit.color = ConfigPOJO.Companion
        enterNameBinding.color = ConfigPOJO.Companion
        enterNameSummerBinding.color = ConfigPOJO.Companion
        val view = if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) enterNameSummerBinding.root else if (Constants.SECRET_DB_KEY == "e03beef2da96672a58fddb43e7468881" || Constants.SECRET_DB_KEY == "0035690c91fbcffda0bb1df570e8cb98") enterNameBindingVinit.root else enterNameBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(activity)

        if (Constants.SECRET_DB_KEY == "97b883a388f65760a09e6571e3d16825") {
            rbOthers.visibility = View.GONE
        }

        if (Constants.SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7" ||
                Constants.SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5" ||
                ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
            rbOthers.text = "Other"
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
            rbOthers.text = "Prefer not to say"
        }

        setViewVisibility()
        setClickListeners()

        etFirstName.filters = arrayOf<InputFilter>(getEditTextFilter())
        etLastName.filters = arrayOf<InputFilter>(getEditTextFilter())
        img_school_id.background = StaticFunction.changeBorderDashColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        imgAddressProof.background = StaticFunction.changeBorderDashColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        imgIdFront.background = StaticFunction.changeBorderDashColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        imgIdBack.background = StaticFunction.changeBorderDashColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        img_building_id.background = StaticFunction.changeBorderDashColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        img_building_id1.background = StaticFunction.changeBorderDashColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)

        if (ConfigPOJO.TEMPLATE_CODE != Constants.SUMMER_APP_BASE) {
            etFullName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etFirstName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etLastName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etEmail.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etAddress.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etRefrral.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etNationalIdNo.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etFamilyName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etFamilyNumber.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etNeighbourName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etNeighbourNumber.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etPhoneNumber.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            etEinNumber.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        }

        if (Constants.SECRET_DB_KEY == "0035690c91fbcffda0bb1df570e8cb98" || Constants.SECRET_DB_KEY == "e03beef2da96672a58fddb43e7468881")//vinit
        {
            enterNameBindingVinit.etAddressLine.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            enterNameBindingVinit.etCity.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            enterNameBindingVinit.etState.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        }

        setListeners()

        if (ConfigPOJO.is_water_platform == "true") {
            rbGroup.orientation = LinearLayout.HORIZONTAL
            rbOthers.visibility = View.GONE
        }

        if (LOGIN_TYPE == "Email") {
            etEmail.setText(EMAIL_ADDRESS)
        }

        if (ConfigPOJO.settingsResponse?.key_value?.is_enable_business_user ?: "" == "true" && SharedPrefs.get().getString("user_type", "customer") == "business") {
            tv_fName.text = resources.getString(R.string.business_name)
            tv_lName.text = resources.getString(R.string.contact_person)
        }
    }

    private fun setClickListeners() {
        imgFrontClose.setOnClickListener(this)
        imgIdFront.setOnClickListener(this)
        imgBackClose.setOnClickListener(this)
        imgIdBack.setOnClickListener(this)
        img_school_id.setOnClickListener(this)
        imgCloseSchool_id.setOnClickListener(this)
        img_building_id1.setOnClickListener(this)
        img_building_id.setOnClickListener(this)
        imgCloseBuilding_id1.setOnClickListener(this)
        imgCloseBuilding_id.setOnClickListener(this)
        imgAddressProof.setOnClickListener(this)
        imgCloseAddressProof.setOnClickListener(this)
        imgUser.setOnClickListener(this)
    }

    private fun setViewVisibility() {
        var listcheck = ConfigPOJO.settingsResponse?.user_forum
        for (i in listcheck?.indices!!) {
            if (listcheck[i].key_name == "firstName") {
                if (listcheck[i].required == "0") {
                    firstName_check = false
                    tv_fName.visibility = View.GONE
                    etFirstName.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "lastName") {
                if (listcheck[i].required == "0") {
                    lastName_check = false
                    tv_lName.visibility = View.GONE
                    etLastName.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "phone_number") {
                if (listcheck[i].required == "0") {
                    tv_phone.visibility = View.GONE
                    etPhoneNumber.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "ein") {
                if (listcheck[i].required == "0") {
                    tv_ein.visibility = View.GONE
                    etEinNumber.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "profile_pic") {
                if (listcheck[i].required == "0") {
                    const_user.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "gender") {
                if (listcheck[i].required == "0") {
                    tv_option.visibility = View.GONE
                    rbGroup.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "address") {
                if (listcheck[i].required == "0") {
                    tv_address.visibility = View.GONE
                    etAddress.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "referral_code") {
                if (listcheck[i].required == "0") {
                    tv_refcode.visibility = View.GONE
                    etRefrral.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "email") {
                if (listcheck[i].required == "0") {
                    tv_email.visibility = View.GONE
                    etEmail.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "national_id") {
                tv_nationalId.text = listcheck[i].terminology
                if (listcheck[i].required == "0") {
                    tv_nationalId.visibility = View.GONE
                    etNationalIdNo.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "official_id_front_photo") {
                if (listcheck[i].required == "0") {
                    tv_id_front.visibility = View.GONE
                    frame_id_front.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "official_id_back_photo") {
                if (listcheck[i].required == "0") {
                    tv_id_back.visibility = View.GONE
                    frame_id_back.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "photo_proof_address") {
                if (listcheck[i].required == "0") {
                    tv_address_proof.visibility = View.GONE
                    frame_address_proof.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "identification_school_or_work") {
                if (listcheck[i].required == "0") {
                    img_school_id.visibility = View.GONE
                    tv_school_work_id.visibility = View.GONE
                    frame_school_id.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "name_phnNo_family_member") {
                if (listcheck[i].required == "0") {
                    ll_family.visibility = View.GONE
                }
            }
            if (listcheck[i].key_name == "name_phnNo_family_neighbour") {
                if (listcheck[i].required == "0") {
                    ll_neibhour.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    /*---Edit Text Validation---*/
    fun invalidString(editText: EditText, errorMsg: String) {
        editText.error = errorMsg
        editText.requestFocus()
    }

    private fun setListeners() {
        tvSubmit.setOnClickListener {
            val selectedId = rbGroup.checkedRadioButtonId
            val radioButton = view?.findViewById<RadioButton>(selectedId)
            val selectedGender = radioButton?.text.toString()

            val firstName = etFirstName.text.toString().trim()
            val etEmailtext = etEmail.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val address = etAddress.text.toString()
            val refCode = etRefrral.text.toString()
            if (firstName_check) {
                if (firstName.isEmpty()) {
                    invalidString(etFirstName, getString(R.string.name_empty_validation_message))
                    return@setOnClickListener
                }
            }
            if (lastName_check) {
                if (lastName.isEmpty()) {
                    invalidString(etLastName, getString(R.string.fullname_empty_validation_message))
                    return@setOnClickListener
                }
            }

            var homeAddress = ""
            if (Constants.SECRET_DB_KEY == "e03beef2da96672a58fddb43e7468881" || Constants.SECRET_DB_KEY == "0035690c91fbcffda0bb1df570e8cb98") {
                if (enterNameBindingVinit.etAddress.text.toString().trim().isEmpty()) {
                    invalidString(enterNameBindingVinit.etAddress, "Please enter address line 1")
                    return@setOnClickListener
                }
                if (enterNameBindingVinit.etAddressLine.text.toString().trim().isEmpty()) {
                    invalidString(enterNameBindingVinit.etAddressLine, "Please enter address line 2")
                    return@setOnClickListener
                }
                if (enterNameBindingVinit.etCity.text.toString().trim().isEmpty()) {
                    invalidString(enterNameBindingVinit.etCity, "Please enter city")
                    return@setOnClickListener
                }
                if (enterNameBindingVinit.etState.text.toString().trim().isEmpty()) {
                    invalidString(enterNameBindingVinit.etState, "Please enter state")
                    return@setOnClickListener
                }
                homeAddress = address + "," + enterNameBindingVinit.etAddressLine.text.toString().trim() + "," + enterNameBindingVinit.etCity.text.toString().trim() + "," + enterNameBindingVinit.etState.text.toString().trim()
            } else {
                homeAddress = address
            }

            if (etEmailtext.isEmpty()) {
                invalidString(etEmail, getString(R.string.email_empty_validation_message))
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString().trim()).matches()) {
                invalidString(etEmail, getString(R.string.email_valid_validation_message))
                return@setOnClickListener
            } else {
                if (CheckNetworkConnection.isOnline(activity)) {
                    presenter.addNameApiCall(firstName, lastName,
                            if (selectedGender == "No Preference" || selectedGender == "Other" || selectedGender == "Prefer not to say") "Other" else selectedGender,
                            homeAddress, etEmailtext, refCode,
                            etNationalIdNo.text.toString(),
                            etFamilyName.text.toString(),
                            etFamilyNumber.text.toString(),
                            etNeighbourName.text.toString(),
                            etNeighbourNumber.text.toString(),
                            File(pathFrontImagee),
                            File(pathBackImagee),
                            File(pathSchoolIdImage),
                            File(pathAddressProofImage),
                            File(pathProfileImage))
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            }
        }
        ivBack.setOnClickListener { fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE) }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    fun getStorage() {
        ImageUtils.displayImagePicker(this)
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(imgIdFront.context, R.string.permission_required_to_select_image, request)
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(imgIdFront.context, R.string.permission_required_to_select_image)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    /* Api success for add name api*/
    override fun onApiSuccess(response: AppDetail?) {
        /* Sign up successful. Redirects user to home screen*/
        ACCESS_TOKEN = response?.access_token ?: ""
        SharedPrefs.with(activity).save(ACCESS_TOKEN_KEY, response?.access_token ?: "")
        Log.d("TOKEN>>", response?.access_token ?: "")
        SharedPrefs.with(activity).save(PROFILE, response)

        if (Constants.SECRET_DB_KEY == "8969983867fe9a873b1b39d290ffa25c") {
            requireActivity().finishAffinity()
            startActivity(Intent(activity, MainMenuActivity::class.java))
        } else {

            if (ConfigPOJO.settingsResponse?.key_value?.is_add_card_after_signup == "true") {
                startActivityForResult(Intent(requireActivity(), AddNewCard::class.java), 100)
            } else {
                if(ConfigPOJO.settingsResponse?.key_value?.is_play_video_after_splash == "true"){
                    requireActivity().finishAffinity()
                    startActivity(Intent(requireActivity(), VideoPlayerScreen::class.java))
                }
                else
                {
                    requireActivity().finishAffinity()
                    startActivity(Intent(requireActivity(), HomeActivity::class.java))
                }
            }
        }
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate.show(isLoading)
    }

    override fun apiFailure() {
        tvSubmit.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        tvSubmit.showSnack(error.toString())
    }

    override fun onClick(v: View?) {
        when (v) {
            imgIdFront -> {
                isFrontPic = true
                isProfilePic = false
                isBackPic = false
                isSchoolIdPic = false
                isAddressProofPic = false
                getStorageWithPermissionCheck()
            }
            imgUser -> {
                isProfilePic = true
                isFrontPic = false
                isBackPic = false
                isSchoolIdPic = false
                isAddressProofPic = false
                getStorageWithPermissionCheck()
            }

            imgBackClose -> {
                imgIdBack.setImageDrawable(null)
                imgBackClose.visibility = View.GONE
                iv_four.visibility = View.VISIBLE
                pathBackImagee = ""
            }
            imgIdBack -> {
                isFrontPic = false
                isBackPic = true
                isProfilePic = false
                isSchoolIdPic = false
                isAddressProofPic = false
                getStorageWithPermissionCheck()
            }

            img_school_id -> {
                isProfilePic = false
                isFrontPic = false
                isBackPic = false
                isSchoolIdPic = true
                isAddressProofPic = false
                getStorageWithPermissionCheck()

            }

            imgCloseSchool_id -> {
                img_school_id.setImageDrawable(null)
                imgCloseSchool_id.visibility = View.GONE
                iv_one.visibility = View.VISIBLE
                pathSchoolIdImage = ""
            }

            imgAddressProof -> {
                isProfilePic = false
                isFrontPic = false
                isBackPic = false
                isSchoolIdPic = false
                isAddressProofPic = true
                getStorageWithPermissionCheck()

            }

            imgCloseAddressProof -> {
                imgAddressProof.setImageDrawable(null)
                imgCloseAddressProof.visibility = View.GONE
                iv_two.visibility = View.VISIBLE
                pathAddressProofImage = ""
            }

            imgFrontClose -> {
                imgIdFront.setImageDrawable(null)
                imgFrontClose.visibility = View.GONE
                iv_three.visibility = View.VISIBLE
                pathFrontImagee = ""
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ImageUtils.REQ_CODE_CAMERA_PICTURE -> {
                    val file = ImageUtils.getFile(activity)
                    when {
                        isProfilePic -> {
                            Glide.with(imgUser.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgUser)
                            pathProfileImage = file.absolutePath
                        }
                        isFrontPic -> {
                            imgFrontClose.visibility = View.VISIBLE
                            iv_three.visibility = View.GONE
                            Glide.with(imgIdFront.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgIdFront)
                            pathFrontImagee = file.absolutePath

                        }
                        isBackPic -> {
                            imgBackClose.visibility = View.VISIBLE
                            iv_four.visibility = View.GONE
                            Glide.with(imgIdBack.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgIdBack)
                            pathBackImagee = file.absolutePath
                        }
                        isSchoolIdPic -> {
                            imgCloseSchool_id.visibility = View.VISIBLE
                            iv_one.visibility = View.GONE
                            Glide.with(img_school_id.context).setDefaultRequestOptions(RequestOptions()).load(file).into(img_school_id)
                            pathSchoolIdImage = file.absolutePath
                        }
                        isAddressProofPic -> {
                            imgCloseAddressProof.visibility = View.VISIBLE
                            iv_two.visibility = View.GONE
                            Glide.with(imgAddressProof.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgAddressProof)
                            pathAddressProofImage = file.absolutePath
                        }
                        else -> {
                            imgFrontClose.visibility = View.VISIBLE
                            iv_three.visibility = View.GONE
                            Glide.with(imgIdFront.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgIdFront)
                            pathFrontImagee = file.absolutePath
                        }
                    }
                }
                ImageUtils.REQ_CODE_GALLERY_PICTURE -> {
                    data?.let {
                        val file = ImageUtils.getImagePathFromGallery(activity, data.data!!)
                        when {
                            isProfilePic -> {
                                Glide.with(imgUser.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgUser)
                                pathProfileImage = file.absolutePath
                            }
                            isFrontPic -> {
                                imgFrontClose.visibility = View.VISIBLE
                                iv_three.visibility = View.GONE
                                Glide.with(imgIdFront.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgIdFront)
                                pathFrontImagee = file.absolutePath

                            }
                            isBackPic -> {
                                imgBackClose.visibility = View.VISIBLE
                                iv_four.visibility = View.GONE
                                Glide.with(imgIdBack.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgIdBack)
                                pathBackImagee = file.absolutePath
                            }
                            isSchoolIdPic -> {
                                imgCloseSchool_id.visibility = View.VISIBLE
                                iv_one.visibility = View.GONE
                                Glide.with(img_school_id.context).setDefaultRequestOptions(RequestOptions()).load(file).into(img_school_id)
                                pathSchoolIdImage = file.absolutePath
                            }
                            isAddressProofPic -> {
                                imgCloseAddressProof.visibility = View.VISIBLE
                                iv_two.visibility = View.GONE
                                Glide.with(imgAddressProof.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgAddressProof)
                                pathAddressProofImage = file.absolutePath
                            }
                            else -> {
                                imgFrontClose.visibility = View.VISIBLE
                                iv_three.visibility = View.GONE
                                Glide.with(imgIdFront.context).setDefaultRequestOptions(RequestOptions()).load(file).into(imgIdFront)
                                pathFrontImagee = file.absolutePath
                            }
                        }
                    }
                }
            }
        }
        if (requestCode == 100 && resultCode == 101) {
            requireActivity().finishAffinity()
            startActivity(Intent(requireActivity(), HomeActivity::class.java))
        }
    }
}