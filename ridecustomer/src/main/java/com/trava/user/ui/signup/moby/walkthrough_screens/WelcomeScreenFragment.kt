package com.trava.user.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.utilities.SharedPrefs
import kotlinx.android.synthetic.main.fragment_walkthrough.*

class WelcomeScreenFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(context).inflate(R.layout.fragment_welcome,container,false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val position = arguments?.getInt("pos")
        when(position){
            0 -> {

                if (ConfigPOJO.settingsResponse != null) {
//                    ivImage.setImageUrl(ConfigPOJO.settingsResponse ?.result?.walk_through?.get(0)?.image_url)
                    tvTitle.text = ConfigPOJO.settingsResponse?.walk_through?.get(0)?.value
                    tvSubTitle.text = ConfigPOJO.settingsResponse?.walk_through?.get(0)?.description

                    Glide.with(activity!!)
                            .load(ConfigPOJO.settingsResponse?.walk_through?.get(0)?.image_url)
                            .into(ivImage);
                }else {
                    ivImage.setImageResource(R.drawable.ic_illus_1)
                    tvSubTitle.text = getString(R.string.walk_desc1)
                    tvTitle.text = getString(R.string.book_schedule_taxi)
                }
            }

            1 -> {
                if (ConfigPOJO.settingsResponse  != null) {
//                    ivImage.setImageUrl(ConfigPOJO.settingsResponse?.walk_through?.get(1)?.image_url)
                    tvTitle.text = ConfigPOJO.settingsResponse?.walk_through?.get(1)?.value
                    tvSubTitle.text = ConfigPOJO.settingsResponse?.walk_through?.get(1)?.description
                    Glide.with(activity!!)
                            .load(ConfigPOJO.settingsResponse?.walk_through?.get(1)?.image_url)
                            .into(ivImage);
                }else {
                    ivImage.setImageResource(R.drawable.ic_illus_2)
                    tvSubTitle.text = getString(R.string.walk_desc1)
                    tvTitle.text = getString(R.string.book_schedule_taxi)
                }
            }

            2 -> {
                if (ConfigPOJO.settingsResponse  != null) {
//                    ivImage.setImageUrl(ConfigPOJO.settingsResponse?.walk_through?.get(2)?.image_url)
                    tvTitle.text = ConfigPOJO.settingsResponse?.walk_through?.get(2)?.value
                    tvSubTitle.text = ConfigPOJO.settingsResponse?.walk_through?.get(2)?.description
                    Glide.with(activity!!)
                            .load(ConfigPOJO.settingsResponse?.walk_through?.get(2)?.image_url)
                            .into(ivImage);
                }else {
                    ivImage.setImageResource(R.drawable.ic_illus_3)
                    tvSubTitle.text = getString(R.string.walk_desc1)
                    tvTitle.text = getString(R.string.book_schedule_taxi)
                }
            }
        }

    }


//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return LayoutInflater.from(context).inflate(R.layout.fragment_walkthrough,container,false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val position = arguments?.getInt("pos")
//        when(position){
//            0 -> {
//                ivImage.setImageResource(R.drawable.ic_illus_1)
//                tvTitle.text = getString(R.string.book_schedule_taxi)
//                tvSubTitle.text = getString(R.string.walk_desc1)
//            }
//
//            1 -> {
//                ivImage.setImageResource(R.drawable.ic_illus_2)
//                tvTitle.text = getString(R.string.book_schedule_taxi)
//                tvSubTitle.text = getString(R.string.walk_desc1)
//            }
//
//            2 -> {
//                ivImage.setImageResource(R.drawable.ic_illus_3)
//                tvTitle.text = getString(R.string.book_schedule_taxi)
//                tvSubTitle.text = getString(R.string.walk_desc1)
//            }
//        }
//
//    }
}