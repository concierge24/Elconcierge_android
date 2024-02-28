package com.trava.user.ui.home

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.utilities.showSnack
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        tvRequestMessage.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        tvRequestCovidTest.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        tvRequestCovidVaccine.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        tvRequestMedicalService.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)


        setListeners()
    }

    private fun setListeners() {
        tvRequestMedicalService.setOnClickListener(this)
        tvRequestMessage.setOnClickListener(this)
        tvRequestCovidVaccine.setOnClickListener(this)
        tvRequestCovidTest.setOnClickListener(this)
        ivBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.tvRequestMessage->{
                finishAffinity()
                startActivity(Intent(this,HomeActivity::class.java))
            }
            R.id.tvRequestCovidTest->{
                tvRequestCovidTest.showSnack(getString(R.string.coming_soon))
            }
            R.id.tvRequestCovidVaccine->{
                tvRequestCovidTest.showSnack(getString(R.string.coming_soon))
            }
            R.id.tvRequestMedicalService->{
                tvRequestCovidTest.showSnack(getString(R.string.coming_soon))
            }
            R.id.ivBack->{
                finish()
            }
        }
    }
}