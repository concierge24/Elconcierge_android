package com.trava.user.ui.home.road_pickup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.trava.user.R
import com.trava.user.utils.AppUtils
import com.trava.user.utils.PermissionUtils
import com.trava.user.webservices.models.roadPickup.RoadPickupResponse
import com.trava.utilities.*
import kotlinx.android.synthetic.main.activity_road_pickup.*
import permissions.dispatcher.*
import java.util.*

@RuntimePermissions
class RoadPickupActivity : AppCompatActivity(), RoadPickupContract.View {

    private val presenter = RoadPickupPresenter()

    private var codeScanner: CodeScanner? = null

    private var dialogIndeterminate: DialogIndeterminate? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_road_pickup)
        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(this)
        getCameraWithPermissionCheck()
        setListener()
    }

    private fun setListener() {
        ivCross.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun intialize() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner?.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner?.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner?.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner?.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner?.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner?.isFlashEnabled = true // Whether to enable flash or not
        codeScanner?.startPreview()


        // Callbacks
        codeScanner?.decodeCallback = DecodeCallback {
            runOnUiThread {
                AppUtils.displayAlert(this,getString(R.string.codeScanned),true)
                hitScanQrCodeApi(it.text)
            }
        }
        codeScanner?.errorCallback = ErrorCallback {
            // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {

        }

    }

    private fun hitScanQrCodeApi(userId : String){
        if(CheckNetworkConnection.isOnline(this)){
            presenter.requestGetDataFromQRCodeApiCall(userId)
        }else{
            CheckNetworkConnection.showNetworkError(scanner_view)
        }
    }

    override fun onApiSuccess(responseData : RoadPickupResponse) {
        setResult(Activity.RESULT_OK, Intent().putExtra("data",responseData))
        finish()

    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)

    }

    override fun apiFailure() {
        rootView?.showSWWerror()

    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            rootView?.showSnack(error.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        if (codeScanner != null)
            codeScanner?.startPreview()
    }

    override fun onPause() {
        if (codeScanner != null)
            codeScanner?.releaseResources()
        super.onPause()
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun getCamera() {
        intialize()
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.permission_required_to_select_image, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(this,
                R.string.permission_required_to_select_image)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
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
