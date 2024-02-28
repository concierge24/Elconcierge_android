package com.trava.user.ui.home.chatModule.chatMessage

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.user.R
import com.trava.user.ui.home.chatModule.WrapContentLinearLayoutManager
import com.trava.user.utils.ImageUtils
import com.trava.user.utils.PermissionUtils
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.ORDER
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.webservices.models.AppDetail
import com.google.gson.Gson
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_CHAT_URL
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_chat.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.RuntimePermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RuntimePermissions
class ChatActivity : AppCompatActivity(), ChatMessageContract.View, AppSocket.OnMessageReceiver, AppSocket.ConnectionListener {
    private val chatMessages = ArrayList<ChatMessageListing>()
    private val TAG: String = "ChatActivity"
    private val presenter = ChatMessagesPresenter()
    private lateinit var adapter: ChatAdapter
    private lateinit var userId: String
    private  var otherUserId: String?=""
    private  var otherUserName: String?=""
    private  var otherUserPicture: String?=""
    private lateinit var phoneNumber: String
    private lateinit var driverUserDetailId: String
    private var dialogIndeterminate: DialogIndeterminate? = null
    var token: String = ""
    private var callingApi = false
    private var socketReconnecting = false
    var order: Order? = null
    var orderID:String=""
    private lateinit var layoutManager: WrapContentLinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        presenter.attachView(this)
        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)

        AppSocket.get().init()
        dialogIndeterminate = DialogIndeterminate(this)
        layoutManager = WrapContentLinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvMessages.layoutManager = layoutManager
        adapter = ChatAdapter(this, chatMessages)
        rvMessages.adapter = adapter
        userId = SharedPrefs.get().getObject(PROFILE, AppDetail::class.java).user_id.toString()
        order = SharedPrefs.get().getObject(ORDER, Order::class.java)
        token = SharedPrefs.get().getObject(PROFILE, AppDetail::class.java).access_token.toString()

        Log.e(TAG, "" + order.toString())
        Log.e(TAG, "order " + order?.driver?.user_id)


        if (intent.getStringExtra(RECEIVER_ID).isNullOrEmpty()) {
            otherUserId = order?.driver?.user_id.toString()
            otherUserName = order?.driver?.name.toString()
            otherUserPicture = order?.driver?.profile_pic.toString()
            driverUserDetailId = order?.driver_user_detail_id.toString() ?: ""
            orderID=order?.order_id.toString()
        } else {
            otherUserId = intent.getStringExtra(RECEIVER_ID) ?: ""
            otherUserName = intent.getStringExtra(USER_NAME) ?: ""
            otherUserPicture = intent.getStringExtra(PROFILE_PIC_URL) ?: ""
            driverUserDetailId = intent.getStringExtra(USER_DETAIL_ID) ?: ""
            orderID=intent.getStringExtra(ORDER_ID)?:""
        }

        visibilty()

        if (CheckNetworkConnection.isOnline(this)) {
            getChatApiCall()
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
        setListeners()
        AppSocket.get().connect()
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

    }

    private fun visibilty() {
         if (intent.getBooleanExtra(CHAT_HISTORY,false)){
             ivSend.visibility=View.GONE
             ivAdd.visibility=View.GONE
             cvBottom.visibility=View.GONE
         }else{
             ivSend.visibility=View.VISIBLE
             //ivAdd.visibility=View.VISIBLE
             cvBottom.visibility=View.VISIBLE
         }
    }

    override fun onBackPressed() {
        if (chatMessages.isNotEmpty()) {
            var x = chatMessages.size - 1
            while (x > 0) {
                if ((chatMessages[x].send_by == userId && chatMessages[x].isSent == true) || chatMessages[x].send_by == otherUserId) {
                    val intent = Intent()
                    intent.putExtra(LAST_MESSAGE, Gson().toJson(chatMessages[x]))
                    intent.putExtra(USER_ID, otherUserId)
                    setResult(Activity.RESULT_OK, intent)
                    break
                }
                x--
            }
        }
        super.onBackPressed()
    }

    private fun setListeners() {
        ivDown.setOnClickListener { rvMessages.scrollToPosition(chatMessages.size - 1) }
        AppSocket.get().addConnectionListener(this)
        AppSocket.get().addOnMessageReceiver(this)
        tvBack.setOnClickListener { onBackPressed() }
        ivSend.setOnClickListener {
            val msg = etMessage.text.toString().trim()
            etMessage.setText("")
            if (!msg.trim().isEmpty())
            {
                val message = ChatMessageListing("", getCurrentDateTime(), otherUserId, userId, msg, "", getCurrentDateTime(), ChatType.TEXT, false, "", otherUserName, otherUserPicture, otherUserId, user_detail_id = driverUserDetailId,order_id = orderID)
                message.let { chatMessages.add(it) }
                adapter.notifyItemInserted(chatMessages.size - 1)
                rvMessages.scrollToPosition(chatMessages.size - 1)
                sendMessage(message)
            }
            else{
                Toast.makeText(this,"Please Enter message",Toast.LENGTH_SHORT).show()
            }
        }
        etMessage.addTextChangedListener(textChangeListener)
        ivAdd.setOnClickListener { getStorageWithPermissionCheck() }
        rvMessages.addOnScrollListener(onScrollListener)
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        ImageUtils.displayImagePicker(this)
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showLocationRationale(request: permissions.dispatcher.PermissionRequest) {
        PermissionUtils.showRationalDialog(ivAdd.context, R.string.permission_required_to_select_image, request)
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(ivAdd.context,
                R.string.permission_required_to_select_image)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private var imagePath: String? = ""
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ImageUtils.REQ_CODE_CAMERA_PICTURE -> {
                    val file = ImageUtils.getFile(this)
                    imagePath = file.absolutePath
                    passImageURI(File(imagePath), imagePath)
                }

                ImageUtils.REQ_CODE_GALLERY_PICTURE -> {
                    data?.let {
                        val file = ImageUtils.getImagePathFromGallery(this, data.data!!)
                        imagePath = file.absolutePath
                        passImageURI(File(imagePath), imagePath)

                    }
                }
            }
        }
    }

    private var onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (chatMessages.isNotEmpty() && layoutManager.findFirstVisibleItemPosition() == 0 && !callingApi) {
                getChatApiCall(chatMessages.first().c_id ?: "")
            }
            if ((recyclerView?.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
                            .findLastCompletelyVisibleItemPosition() > chatMessages.size - 4) {
                ivDown.visibility = View.GONE
            } else {
                ivDown.visibility = View.VISIBLE
            }
        }
    }

    private val textChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.trim()?.isEmpty() == true) {
                ivSend.setImageResource(R.drawable.ic_menu_send)
                ivSend.isEnabled = false
            } else {
                ivSend.setImageResource(R.drawable.ic_menu_send)
                ivSend.isEnabled = true
            }
        }

    }

    private fun getChatApiCall(messageId: String = "", skip: Int = 0, messageOrder: String = MessageOrder.BEFORE) {
        callingApi = true
        Log.e(TAG, "token" + token)
        presenter.getChatMessagesApiCall(orderID,messageId, otherUserId ?: "", Constants.PAGE_LIMIT, skip, messageOrder)
    }

    fun sendMessage(message: ChatMessageListing?) {
        if (AppSocket.get().isConnected) {

            AppSocket.get().sendMessage(message) {
                val index = chatMessages.indexOf(chatMessages.find {
                    it.message_id == message?.message_id
                })
                chatMessages[index].isSent = true
                adapter.notifyItemChanged(index, adapter.CHANGE_SENT_STATUS)
                Log.e("+++++++++++", message.toString())
                rvMessages.scrollToPosition(chatMessages.size - 1)
            }
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
            setFailedMessages()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        AppSocket.get().removeOnMessageReceiver(this)
        unregisterReceiver(broadcastReceiver)
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun chatMessagesApiSuccess(chatList: ArrayList<ChatMessageListing>?, messageOrder: String) {

        Thread(Runnable {
            callingApi = false
            if (messageOrder == MessageOrder.BEFORE) {
                if (chatMessages.isEmpty()) {
                    chatList?.let { chatMessages.addAll(it) }
                    runOnUiThread { adapter.notifyDataSetChanged() }
                } /*else {
                    chatList?.let { chatMessages.addAll(0, it) }
                    val tempChat = ArrayList<ChatMessageListing>()
                    tempChat.addAll(chatMessages.distinctBy { it.message_id })
                    tempChat.sortBy { convertDateTimeInMillis(it.sent_at) }
                    chatMessages.clear()
                    chatMessages.addAll(tempChat)
                    runOnUiThread {
                        chatList?.size?.let { adapter.notifyItemRangeInserted(0, it) }
                        adapter.notifyItemChanged(chatList?.size ?: 0)
                    }
                }*/
            } else {
                chatList?.let { chatMessages.addAll(it) }
                val tempChat = ArrayList<ChatMessageListing>()
                tempChat.addAll(chatMessages.distinctBy { it.message_id })
                tempChat.sortBy { convertDateTimeInMillis(it.sent_at) }
                runOnUiThread {
                    chatMessages.clear()
                    chatMessages.addAll(tempChat)
                    adapter.notifyDataSetChanged()
                    if (messageOrder == MessageOrder.AFTER) {
                        scrollToBottom()
                    }
                }
            }
        }).start()
    }

    override fun apiFailure() {
        callingApi = false
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, errorBody: String?) {
        callingApi = false
        errorBody?.let { rootView.showSnack(it) }
    }


    override fun onConnectionStatusChanged(status: String?) {
        when (status) {
            Socket.EVENT_CONNECT -> {
                if (socketReconnecting) {
                    socketReconnecting = false
                    var x = chatMessages.size - 1
                    while (x > 0) {
                        if (chatMessages[x].isSent == true) {
                            getChatApiCall(messageId = chatMessages[x].c_id.toString(), messageOrder = MessageOrder.AFTER)
                            break
                        }
                        x--
                    }
                }
            }
            Socket.EVENT_DISCONNECT -> {
                socketReconnecting = true
                setFailedMessages()
            }
            Socket.EVENT_ERROR -> {

            }
            else -> {

            }
        }
    }

    private fun setFailedMessages() {
        for (i in 0 until chatMessages.size) {
            if (chatMessages[i].isSent == false) {
                chatMessages[i].isFailed = true
                adapter.notifyItemChanged(i, adapter.CHANGE_SENT_STATUS)
            }
        }
    }

    override fun onMessageReceive(message: ChatMessageListing?) {
//        ivDown.setBackgroundResource(R.drawable.shape_scroll_bottom_chat_blue)
        /*val index = chatMessages.indexOfFirst { message?.message_id == it.message_id }
        if (index == -1) {

        }*/
        message?.let { chatMessages.add(it) }
        var msg = chatMessages[chatMessages.size - 1]
        msg.message_id = chatMessages[0].message_id
        chatMessages.set(chatMessages.size - 1, msg)
        adapter.notifyItemChanged(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        if ((rvMessages.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
                        .findLastCompletelyVisibleItemPosition() > chatMessages.size - 4) {
            rvMessages.scrollToPosition(chatMessages.size - 1)
        }
    }

    fun passImageURI(file: File?, photoURI: String?) {
        getUploadImageApiCall(SharedPrefs.get().getString(ACCESS_TOKEN_KEY, ""), file)
    }


    fun getUploadImageApiCall(authorization: String, file: File?) {
        val map = HashMap<String, RequestBody>()
//        map["access_token"] = RequestBody.create(MediaType.parse("text/plain"), authorization)
        if (file != null && file?.exists()) {
            val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
            map["file_upload\"; filename=\"picture.png\" "] = body
        }
        showLoader(true)
        RestClient.get().uploadImageChat(map)
                .enqueue(object : Callback<ApiResponse<ChatMessageListing>> {
                    override fun onFailure(call: Call<ApiResponse<ChatMessageListing>>?, t: Throwable?) {
                        showLoader(false)
                        apiFailure()
                    }

                    override fun onResponse(call: Call<ApiResponse<ChatMessageListing>>?, response: Response<ApiResponse<ChatMessageListing>>?) {
                        showLoader(false)
                        if (response?.isSuccessful == true) {
                            if (response.body()?.statusCode == 200) {
                                var originalImage = BASE_CHAT_URL + response.body()?.result?.original_image
                                var thumbnail = BASE_CHAT_URL + response.body()?.result?.thumbnail_image
                                val message = ChatMessageListing("", getCurrentDateTime(), otherUserId, userId, "", thumbnail, getCurrentDateTime(), ChatType.IMAGE, false, originalImage, otherUserName, otherUserPicture, otherUserId, user_detail_id = driverUserDetailId,order_id = orderID)
                                sendMessage(message)
                                chatMessages.add(message)
                                adapter.notifyItemInserted(chatMessages.size - 1)
                                rvMessages.scrollToPosition(chatMessages.size - 1)
                            } else {
                                handleApiError(response.body()?.statusCode, response.body()?.msg)
                            }
                        } else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }
                })
    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                if (CheckNetworkConnection.isOnline(this@ChatActivity)) {

                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            }
        }
    }

    fun convertDateTimeInMillis(givenDateString: String): Long {
        var timeInMilliseconds: Long = 0
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            var mDate = sdf.parse(givenDateString)
            timeInMilliseconds = mDate.getTime()
            System.out.println("Date in milli :: " + timeInMilliseconds)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timeInMilliseconds
    }

    fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }

    override fun onResume() {
        super.onResume()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}
