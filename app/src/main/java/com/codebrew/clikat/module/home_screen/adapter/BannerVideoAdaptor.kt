package com.codebrew.clikat.module.home_screen.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.VideoView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.codebrew.clikat.R
import com.codebrew.clikat.databinding.ItemBannerVideoBinding

class BannerVideoAdaptor(private val bannerVideo: List<String>,
                         private val viewPager: ViewPager2) : RecyclerView.Adapter<BannerVideoAdaptor.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemBannerVideoBinding>(LayoutInflater.from(parent.context),
                R.layout.item_banner_video, parent, false)
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return bannerVideo.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.videoView?.setVideoPath(bannerVideo[position])

        holder.videoView?.setOnPreparedListener { mp ->
            mp.start()

            mp.isLooping = true
            val videoRatio: Float = (mp.videoWidth / mp.videoHeight).toFloat()
            val screenRatio = holder.videoView.width / holder.videoView.height.toFloat()
            val scale = videoRatio / screenRatio
            if (scale >= 1f) {
                holder.videoView.scaleX = scale
            } else {
                holder.videoView.scaleY = 1f / scale
            }
        }


        val onInfoToPlayStateListener = MediaPlayer.OnInfoListener { mp, what, extra ->
            when (what) {

                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    setProgressBarVisibility(holder.progressBar,View.GONE)
                }

                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    setProgressBarVisibility(holder.progressBar,View.VISIBLE)
                }

                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    setProgressBarVisibility(holder.progressBar,View.GONE)
                }

                else -> {
                    setProgressBarVisibility(holder.progressBar,View.GONE)
                }
            }

        }

        holder.videoView?.setOnInfoListener(onInfoToPlayStateListener)

    }

   private fun setProgressBarVisibility(progressBar: ProgressBar?, status: Int): Boolean {
        progressBar?.visibility = status
        return true
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val videoView: VideoView? = itemView.findViewById(R.id.videoView)
        val progressBar: ProgressBar? = itemView.findViewById(R.id.progressBar)

    }
}