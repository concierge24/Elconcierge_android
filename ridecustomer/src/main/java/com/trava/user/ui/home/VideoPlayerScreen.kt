package com.trava.user.ui.home

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.trava.user.R
import com.trava.user.ui.signup.SignupActivity
import com.trava.user.ui.signup.moby.landing.LandingScreen
import com.trava.user.utils.ConfigPOJO
import com.trava.user.walkthrough.WalkthroughActivity
import com.trava.utilities.Constants
import com.trava.utilities.DialogIndeterminate
import com.trava.utilities.LocaleManager
import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.webservices.models.AppDetail
import kotlinx.android.synthetic.main.activity_video_player_screen.*
import java.util.*


class VideoPlayerScreen : AppCompatActivity() , MediaPlayer.OnCompletionListener {
    var dialogIndeterminate: DialogIndeterminate? = null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player_screen)

        dialogIndeterminate = DialogIndeterminate(this)

        initializePlayer()
        setListener()
    }

    private fun setListener() {
        ivBack.setOnClickListener {
            finish()
        }

        tvSkipNow.setOnClickListener {
            openDesiredScreen()
        }
    }

    private fun initializePlayer() {
//        val mediaController = MediaController(this)
//        mediaController.setAnchorView(videoView)
        val uri: Uri = Uri.parse(ConfigPOJO.play_video_after_splash_images)
//        videoView.setMediaController(mediaController)
        dialogIndeterminate!!.show(true)

        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.setOnCompletionListener(this)
        videoView.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.start()
                dialogIndeterminate!!.show(false)
                mp.setOnVideoSizeChangedListener(object : MediaPlayer.OnVideoSizeChangedListener{
                    override fun onVideoSizeChanged(mp: MediaPlayer, arg1: Int, arg2: Int) {
                        mp.start()
                    }
                })
            }
        })
        videoView.start()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        openDesiredScreen()
    }


    private fun openDesiredScreen(){
        val proifle = SharedPrefs.with(this).getObject(PROFILE, AppDetail::class.java)
        if (proifle != null) {
            startActivity(Intent(this@VideoPlayerScreen, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
        }else {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
                startActivity(Intent(this@VideoPlayerScreen,
                        SignupActivity::class.java))
            } else if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
                startActivity(Intent(this@VideoPlayerScreen,
                        LandingScreen::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            } else {
                if (ConfigPOJO.is_facebookLogin == "true") {
                    startActivity(Intent(this@VideoPlayerScreen,
                            LandingScreen::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                } else {
                    startActivity(Intent(this@VideoPlayerScreen,
                            WalkthroughActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
            }
        }
        finishAffinity()
    }

}