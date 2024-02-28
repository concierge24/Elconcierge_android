package com.trava.user.ui.menu.noticeBoard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.trava.user.R
import com.trava.user.databinding.ActivityNoticeBoardBinding
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.noticeBoard.NotificationData
import com.trava.utilities.*
import kotlinx.android.synthetic.main.activity_notice_board.*
import java.util.*
import kotlin.collections.ArrayList

class NoticeBoardActivity : AppCompatActivity(),NoticeBoardContract.View {

    private var notificationList = ArrayList<NotificationData>()
    private val presenter = NoticeBoardPresenter()
    var binding : ActivityNoticeBoardBinding ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this,R.layout.activity_notice_board)
        binding?.color = ConfigPOJO.Companion
        presenter.attachView(this)
        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)

        setAdapter()
        presenter.getNotifications()

        setClickLIstenrs()

    }

    private fun setClickLIstenrs() {
        binding?.ivBackSnd?.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setAdapter() {
        rvNotifications.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvNotifications.adapter = NotificationsAdapter(notificationList)
    }

    override fun onApiSuccess(response: List<NotificationData>?) {
        if(response?.isNotEmpty() == true){
            flipperContacts.visibility=View.VISIBLE
            llEMpty.visibility=View.GONE
            flipperContacts.displayedChild =1
            notificationList.addAll(response)
            rvNotifications.adapter?.notifyDataSetChanged()
        }
        else{
            flipperContacts.visibility=View.GONE
            llEMpty.visibility=View.VISIBLE
            flipperContacts.displayedChild = 2
        }

    }

    override fun showLoader(isLoading: Boolean) {
    }

    override fun apiFailure() {
        rvNotifications?.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
            return
        }
        rvNotifications?.showSnack(error.toString())
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
