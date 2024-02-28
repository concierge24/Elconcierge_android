package com.trava.user.ui.signup.moby.resgister_screens.fragments.institution


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.trava.user.R
import com.trava.user.databinding.FragmentStep2InstitutionSignupBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.orderdetails.heavyloads.onRequestPermissionsResult
import com.trava.user.ui.signup.verifytop.OtpFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.ImageUtils
import com.trava.user.utils.PermissionUtils
import com.trava.user.webservices.moby.response.PvtCooperativeRegResponseModel
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.constants.SERVICES
import kotlinx.android.synthetic.main.fragment_enter_order_details2.*
import kotlinx.android.synthetic.main.fragment_enter_order_details2.etPhone
import kotlinx.android.synthetic.main.fragment_signup.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import permissions.dispatcher.*
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
@RuntimePermissions
class Step2_InstitutionSignup : Fragment(),NewAddImageAdapter.AddImageCallback,PvyCooperativeFirmContarctor.View {

    var binding : FragmentStep2InstitutionSignupBinding?= null
    private val imageList = ArrayList<String>()
    private var dialogIndeterminate: DialogIndeterminate? = null
    var presenter = PvtCooperativeFirmPresenter()
    var isoCode : String ?= null
    var cooperativeID : String ?= null

    private var adapter: NewAddImageAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_step2__institution_signup, container, false)
        binding?.color = ConfigPOJO.Companion


        return  binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(activity)

        binding?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)?:0)?.toLowerCase())
        binding?.countryCodePicker?.setNumberAutoFormattingEnabled(false)
        binding?.countryCodePicker?.registerCarrierNumberEditText(etPhone) // registers the EditText with the country code picker

        cooperativeID =  arguments?.getString("inst_id")
        binding?.instName?.setText(arguments?.getString("title").toString())

        rvCompanies.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        adapter = NewAddImageAdapter(imageList)
        adapter?.settingCallback(this@Step2_InstitutionSignup)
        rvCompanies.adapter = adapter

        binding?.tvProceedFab?.background?.mutate()?.setTint(Color.parseColor(ConfigPOJO.primary_color))


        binding?.ivBack?.setOnClickListener {
            activity?.onBackPressed()
        }

        binding?.tvProceedFab?.setOnClickListener {
            registerPvtCooperative()
        }
    }

    private fun registerPvtCooperative() {
        if(isEmailValidationOk()){
            if(ConfigPOJO.settingsResponse?.key_value?.iso_code.isNullOrEmpty()){
                isoCode = "US"
            }else{
                isoCode = ConfigPOJO.settingsResponse?.key_value?.iso_code.toString()
            }

            var file : File ?= null
            if (CheckNetworkConnection.isOnline(activity)) {
                if(imageList.size>0){
                    imageList.forEach {
                        file = File(it)
                    }

                }
                presenter.pvyCooperariveRegister(cooperativeID,binding?.idEdt?.text.toString(),binding?.emailEdt?.text.toString()
                                               ,binding?.countryCodePicker?.selectedCountryCodeWithPlus.toString()
                                                ,isoCode.toString(),binding?.etPhone?.text.toString(),binding?.passEdt?.text.toString()
                                                , TimeZone.getDefault().id,"0.0","0.0",file)
            } else {
                CheckNetworkConnection.showNetworkError(binding?.root!!)
            }

        }
//        (cooperation_id: String?, identification_number: String?,
//        name: String?, email: String, phoneNumber: String,
//        phone_code: String, iso: String, phone_number: String,
//        password: String, timezone: String, latitude: String,
//        longitude: String, document: File?)


    }

    /*Checks for the validations*/
    private fun isEmailValidationOk(): Boolean {
        return when {
            binding?.idEdt?.text.toString().trim().isEmpty() -> {
                binding?.root?.showSnack(getString(R.string.cannot_be_empty))// Invalid phone number
                false
            }
            !(isValidEmail(binding?.emailEdt?.text.toString())) -> {
                binding?.root?.showSnack(R.string.email_valid_validation_message)
                return false
            }
            (binding?.passEdt?.text.toString().length<6 || binding?.passEdt?.text.toString().isNullOrEmpty()) -> {
                binding?.root?.showSnack(getString(R.string.pass_not_empty))
                return false
            }
            binding?.etPhone?.text.toString().trim().isEmpty() -> {
                binding?.root?.showSnack(getString(R.string.phone_validation_message))// Invalid phone number
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


    private fun addImage(imagePath: String?) {
        imageList.add(imagePath.toString())
        adapter?.notifyDataSetChanged()
    }


    override fun addImageHeader() {
        getStorageWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        if (imageList.size < 1) {
            ImageUtils.displayImagePicker(this)
        } else {
            binding?.root?.showSnack(getString(R.string.max_images_validation_msg))
        }
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showLocationRationale(request: PermissionRequest) {
        activity?.let { PermissionUtils.showRationalDialog(it, R.string.permission_required_to_select_image, request) }
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        activity?.let {
            PermissionUtils.showAppSettingsDialog(it,
                    R.string.permission_required_to_select_image)
        }
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
                    val file = ImageUtils.getFile(activity)
                    imagePath = file.absolutePath
                    addImage(imagePath)
                }

                ImageUtils.REQ_CODE_GALLERY_PICTURE -> {
                    data?.let {
                        val file = ImageUtils.getImagePathFromGallery(activity, data.data!!)
//                        ivProfilePic.setRoundProfilePic(file)
                        imagePath = file.absolutePath
                        addImage(imagePath)
                    }
                }
            }
        }
    }

    override fun onApiSuccess(response: PvtCooperativeRegResponseModel?) {

        if(!TextUtils.isEmpty(response?.getAccessToken())){

            SharedPrefs.with(activity).save(ACCESS_TOKEN_KEY, response?.getAccessToken())
            val fragment = OtpFragment()
            val bundle = Bundle()
            bundle.putString("phone_number", binding?.countryCodePicker?.selectedCountryCodeWithPlus.toString() + "-" + binding?.etPhone?.text.toString().replace(Regex("[^0-9]"), ""))
            bundle.putString("otp", response?.getOtp().toString())
            bundle.putString("access_token", response?.getAccessToken())
            fragment.arguments = bundle
            fragmentManager?.let { it1 ->
                Utils.replaceFragment(it1, fragment, R.id.container, OtpFragment::class.java.simpleName)
            }

        }else{
            binding?.root?.showSnack(getString(R.string.sww_error))
        }

    }


    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        binding?.root?.showSnack(getString(R.string.sww_error))
    }

    override fun handleApiError(code: Int?, error: String?) {
        binding?.root?.showSnack(error.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }


}
