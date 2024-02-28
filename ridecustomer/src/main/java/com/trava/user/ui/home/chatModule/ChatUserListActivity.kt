package com.trava.user.ui.home.chatModule

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.user.R
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chats.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val RQ_CODE_CHAT = 100

class ChatUserListActivity : AppCompatActivity(), ChatContract.View {

    private val presenter = ChatPresenter()

    private val chatListing: ArrayList<ChatMessageListing>? = ArrayList()

    private lateinit var adapter: ChatListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chats)
        presenter.attachView(this)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = ChatListAdapter(recyclerView.context, chatListing, this)
        recyclerView.adapter = adapter
        progressbar.visibility = View.GONE
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorGreen)
        chatLogsApiCall()
        setListeners()
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager
                .CONNECTIVITY_ACTION))

        tvBack.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onResume() {
        super.onResume()
        AppSocket.get().addOnMessageReceiver(messageReceiver)

    }

    override fun onPause() {
        super.onPause()
        AppSocket.get().removeOnMessageReceiver(messageReceiver)
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        unregisterReceiver(broadcastReceiver)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_CODE_CHAT && resultCode == Activity.RESULT_OK && data != null) {
            val lastMsgData = Gson().fromJson(data.getStringExtra(LAST_MESSAGE), ChatMessageListing::class.java)
            val userId = data.getStringExtra(USER_ID)
            refreshChatLogs(lastMsgData, userId)
        }
    }

    private fun setListeners() {
        swipeRefresh.setOnRefreshListener {
            chatLogsApiCall()
        }
    }

    private fun chatLogsApiCall() {
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.getChatLogsApiCall(SharedPrefs.get().getString(ACCESS_TOKEN_KEY,""), Constants.PAGE_LIMIT, 0)
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
            progressbar.visibility = View.GONE
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                if (CheckNetworkConnection.isOnline(context as Activity)) {
                    chatLogsApiCall()
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            }
        }
    }

    private val messageReceiver = AppSocket.OnMessageReceiver { message ->
        refreshChatLogs(message, message.send_by, 1)
        Log.e("message_received", message.text?:"")
    }

    private fun refreshChatLogs(message: ChatMessageListing, userId: String?, unDeliveredCount: Int = 0) {
        val index = chatListing?.indexOf(chatListing.find {
            it.send_by == userId
        })
        if (index == null || index == -1) {
            chatLogsApiCall()
        } else {
            chatListing?.get(index)?.message_id = message.message_id
            chatListing?.get(index)?.text = message.text
            chatListing?.get(index)?.original_image = message.original_image
            chatListing?.get(index)?.sent_at = message.sent_at
            chatListing?.sortByDescending {
                convertDateTimeInMillis(it.sent_at)
            }
            adapter.notifyDataSetChanged()
        }
    }

    fun convertDateTimeInMillis(givenDateString: String): Long {
        var timeInMilliseconds: Long = 0
        var sdf = SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.US);
        try {
            var mDate = sdf.parse(givenDateString)
            timeInMilliseconds = mDate.getTime()
            System.out.println("Date in milli :: " + timeInMilliseconds)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timeInMilliseconds
    }

    override fun chatLogsApiSuccess(chatList: ArrayList<ChatMessageListing>?) {
        swipeRefresh.isRefreshing = false
        chatListing?.clear()
        chatList?.let { chatListing?.addAll(it) }
        progressbar.visibility = View.GONE
        if (chatList?.size == 0) {
            tvEmptyMessage.visibility = View.VISIBLE
        } else {
            tvEmptyMessage.visibility = View.GONE
        }
        adapter.notifyDataSetChanged()
    }

    override fun showLoader(isLoading: Boolean) {

    }

    override fun apiFailure() {
        rootView.showSWWerror()
        swipeRefresh.isRefreshing = false
    }

    override fun handleApiError(code: Int?, errorBody: String?) {
        errorBody?.let { rootView.showSnack(it) }
        swipeRefresh.isRefreshing = false
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}