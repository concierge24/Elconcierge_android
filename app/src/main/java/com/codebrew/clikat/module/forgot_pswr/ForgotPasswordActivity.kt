package com.codebrew.clikat.module.forgot_pswr

import android.os.Bundle
import android.widget.EditText
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.R
import com.codebrew.clikat.BR
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.databinding.ForgotPasswordFragmentBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Order
import kotlinx.android.synthetic.main.forgot_password_fragment.*
import javax.inject.Inject

class ForgotPasswordActivity : BaseActivity<ForgotPasswordFragmentBinding, ForgotPswrViewModel>(), ForgotNavigator, Validator.ValidationListener {

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @NotEmpty(message = "Please enter Email")
    @Email
    @Order(1)
    private lateinit var emailEditText: EditText

    @Inject
    lateinit var appUtils: AppUtils

    val colorConfig by lazy { Configurations.colors }


    // Validation
    private val validator = Validator(this)

    private lateinit var mForgotViewModel: ForgotPswrViewModel
    private var mActivityForgotBinding: ForgotPasswordFragmentBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityForgotBinding = viewDataBinding
        mForgotViewModel.navigator = this

        mActivityForgotBinding?.colors=colorConfig

        emailEditText = findViewById(R.id.edit_Email)

        // Validator
        validator.validationMode = Validator.Mode.IMMEDIATE
        // Listeners
        validator.setValidationListener(this)

        btn_login.setOnClickListener {
            validator.validate()
        }

        iv_back.setOnClickListener {
            finish()
        }

    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.forgot_password_fragment
    }

    override fun getViewModel(): ForgotPswrViewModel {
        mForgotViewModel = ViewModelProviders.of(this, factory).get(ForgotPswrViewModel::class.java)
        return mForgotViewModel
    }

    override fun onForgotPswr(message: String) {
        if(mActivityForgotBinding!=null)
        {
            StaticFunction.sweetDialogueSuccess(this@ForgotPasswordActivity, resources.getString(R.string.success), message,
                    false, 33, null, null)
        }
    }

    override fun onErrorOccur(message: String) {
        if(mActivityForgotBinding!=null)
        {
            mActivityForgotBinding?.root?.onSnackbar(message)
        }
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        val error = errors?.get(0)
        val message = error?.getCollatedErrorMessage(this)
        val editText = error?.view as EditText
        editText.error = message
        editText.requestFocus()
    }

    override fun onValidationSucceeded() {
        hideKeyboard()
        mForgotViewModel.forgotPass(emailEditText.text.toString())
    }


}
