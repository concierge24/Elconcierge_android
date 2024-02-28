package com.trava.user.ui.home.stories

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.trava.user.R
import com.trava.user.utils.AppUtils
import com.trava.user.webservices.models.stories.ResultItem
import com.trava.user.webservices.models.storybonus.StoryBonusPojo
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.location.LocationProvider
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_AD_URL
import kotlinx.android.synthetic.main.fragment_watch_stories.*
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WatchStories : AppCompatActivity(), MomentzCallback, StoriesContract.View {
    private var presenter = StoriesPresenter()
    private var dialog: DialogIndeterminate? = null
    lateinit var locationProvider: LocationProvider
    private var storiesList: ArrayList<ResultItem> = ArrayList<ResultItem>()
    private var completedStories: String = ""
    private var currentIndex: String = ""
    private var timerStory = Timer()
    private var momentzData: Momentz? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_watch_stories)
        presenter.attachView(this)
        dialog = DialogIndeterminate(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ivBack_snd.setOnClickListener {
            onBackPressed()
        }

        ivBack_exit.setOnClickListener {
            onBackPressed()
        }

        tvWebsiteUrl.setOnClickListener {
            presenter.watchStoryWeb(storiesList[currentIndex.toInt()].id.toString())
        }

        ivClose.setOnClickListener {
            showAndHideStoryData("hide")
        }

        cvbottomView.setOnClickListener {
            showAndHideStoryData("show")
        }
        manageBlinkEffect()
        locationProvider = LocationProvider.CurrentLocationBuilder(this).build()
        locationProvider.getLastKnownLocation(OnSuccessListener {
            try {
                var map = HashMap<String, Any>()
                map["timezone"] = TimeZone.getDefault().id
                map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
                map["latitude"] = it.latitude.toString() ?: "0.0"
                map["longitude"] = it.longitude.toString() ?: "0.0"
                map["limit"] = "50"
                map["skip"] = "0"
                presenter.requestStories(map)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun showAndHideStoryData(action: String) {
        var animation: Animation? = null
        clStoryData.bringToFront()
        if (action == "show") {
            intripWaiting = false
            startTimerThread()
            tvTitlee.text = storiesList[currentIndex.toInt()].name
            tvDescription.text = storiesList[currentIndex.toInt()].description
            tvWebsiteUrl.text = storiesList[currentIndex.toInt()].websiteUrl
            tvWebsiteUrl.paintFlags = tvWebsiteUrl.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            cvbottomView.visibility = View.GONE
            momentzData!!.callPause(true)
            animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
            clStoryData.startAnimation(animation)
            clStoryData.visibility = View.VISIBLE
        } else {
            intripWaiting = true
            startTimerThread()
            momentzData!!.callPause(false)
            clStoryData.visibility = View.GONE
            cvbottomView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("WrongConstant")
    private fun manageBlinkEffect() {
        val anim = AlphaAnimation(0.2f, 1.0f)
        anim.duration = 1500
        anim.startOffset = 20
        anim.repeatMode = Animation.RESTART
        anim.repeatCount = Animation.INFINITE
        iv_view.startAnimation(anim)
    }

    private fun runStory() {
        val internetLoadedImageView = ImageView(this)
        val internetLoadedVideo = VideoView(this)
        val listOfViews: ArrayList<MomentzView> = ArrayList<MomentzView>()
        for (i in 0 until storiesList.size) {
            if (storiesList[i].imageUrl?.trim() != "") {
                listOfViews.add(MomentzView(internetLoadedImageView, 10))
            } else {
                listOfViews.add(MomentzView(internetLoadedVideo, 60))
            }
        }
        startTimerThread()
        Momentz(this, listOfViews, container, this).start()
    }

    private fun playVideo(videoView: VideoView, index: Int, momentz: Momentz) {
        var str = ""
        momentzData = momentz
        str = BASE_AD_URL + "" + storiesList[index].videoUrl!!
        if (index > 0) {
            storiesList[(currentIndex.toInt() - 1)].tempConunt = counter
        }

        val uri = Uri.parse(str)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        //videoView.start()
        videoView.setOnPreparedListener {
            counter = 0
            videoView.start()
        }
        videoView.setOnInfoListener(object : MediaPlayer.OnInfoListener {
            override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    momentz.editDurationAndResume(index, (videoView.duration) / 1000)
                    return true
                }
                return false
            }
        })
    }

    override fun done() {
        intripWaiting = false
        storiesList[currentIndex.toInt()].tempConunt = counter
        onBackPressed()
    }

    override fun onSkip() {
        Log.e("AsasasasasA", "SkipCalled")
    }

    override fun onNextView() {
        Log.e("onNextView", "onNextView")
    }

    override fun onSwipeUp() {
        showAndHideStoryData("show")
    }

    override fun onSwipeDown() {
        showAndHideStoryData("hide")
    }

    override fun onPause() {
        super.onPause()
        timerStory.cancel()
        timerStory.purge()
        if (momentzData != null)
            momentzData!!.callPause(true)
    }

    override fun onNextCalled(view: View, momentz: Momentz, index: Int) {
        currentIndex = index.toString()
        momentzData = momentz
        if (view is VideoView) {
            momentz.pause(true)
            playVideo(view, index, momentz)
        } else if (view is ImageView) {
            momentz.pause(true)
            if (this != null) {
                try {

                    Picasso.get()
                            .load(BASE_AD_URL + "" + storiesList[index].imageUrl)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .into(view, object : Callback {
                                override fun onSuccess() {
                                    if (index > 0) {
                                        storiesList[(currentIndex.toInt() - 1)].tempConunt = counter
                                    }
                                    counter = 0
                                    momentz.resume()
                                }

                                override fun onError(e: Exception?) {
                                    momentz.resume()
                                    e?.printStackTrace()
                                }
                            })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    var counter = 0
    private var intripWaiting = true

    /*Thread to calculate per second*/
    fun startTimerThread() {
        Handler().postDelayed({
            if (intripWaiting) {
                counter += 1
                startTimerThread()
            }
        }, 1000)
    }

    override fun onApiSuccess(responseData: ArrayList<ResultItem>) {
        if (responseData.size > 0) {
            storiesList = responseData
            if (storiesList.size > 0) {
                cvbottomView.visibility = View.VISIBLE
            }
            val timer = object : CountDownTimer(1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    runStory()
                }
            }
            timer.start()
        }
    }

    override fun onBonusApiSuccess(responseData: StoryBonusPojo) {
        if (responseData.success == 1) {
            var bonus = 0
            for (i in responseData.result!!.indices) {
                bonus += responseData.result[i]?.credited ?: 0
            }
            showEarnedKmAlert(this, bonus)
        } else {
            showEarnedKmAlert(this, 0)
        }
    }

    override fun onStoryWebsiteApiSuccess(msg: String) {
        var url = storiesList[currentIndex.toInt()].websiteUrl
        if (!url!!.startsWith("https://") && !url.startsWith("http://")) {
            url = "http://$url"
        }

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    override fun showLoader(isLoading: Boolean) {
        dialog?.show(isLoading)
    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            rootView.showSnack(error.toString())
        }
    }

    private fun showEarnedKmAlert(context: Context, kmEarned: Int) {

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        var msg = if (kmEarned != 0) {
            "${getString(R.string.you_have_earned)} $kmEarned ${getString(R.string.km)}. ${getString(R.string.watch_more_for_ore_earnings)}"
        } else {
            "${getString(R.string.please_watch_more_complete_video_for_bonus)}"
        }

        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog?.dismiss()
        }
        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog?.dismiss()
            startActivity(Intent(this, StoriesEarned::class.java).putExtra("kmEarned", kmEarned))
            finish()
        }
        builder.show()
    }

    override fun onBackPressed() {

        val storiesIds = StringBuilder("")
        for (i in 0 until storiesList.size) {
            if (storiesList[i].imageUrl?.trim() != "") {
                if (storiesList[i].tempConunt!! >= 10) {
                    storiesIds.append(storiesList[i].id.toString() + "-10")
                    storiesIds.append(",")
                }
            } else {
                if (storiesList[i].tempConunt!! > 0) {
                    storiesIds.append(storiesList[i].id.toString() + "-" + storiesList[i].tempConunt)
                    storiesIds.append(",")
                }
            }
        }

        if (storiesIds.toString() != "") {
            val map = HashMap<String, Any>()
//            map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
            map["story_id"] = storiesIds.toString().substring(0, storiesIds.toString().length - 1)
            presenter?.saveStoryBonus(map)
        } else {
            showEarnedKmAlert(this, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (momentzData != null) {
            momentzData!!.stop()
        }
    }
}