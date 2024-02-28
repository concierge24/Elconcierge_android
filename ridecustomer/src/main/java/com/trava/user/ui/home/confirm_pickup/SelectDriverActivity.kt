package com.trava.user.ui.home.confirm_pickup

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.trava.user.R
import com.trava.user.databinding.ActivityBookingsBinding
import com.trava.user.databinding.ActivitySelectDriverBinding
import com.trava.user.ui.home.comfirmbooking.DriverAdapter
import com.trava.user.ui.home.comfirmbooking.SavedDriverInterface
import com.trava.user.ui.home.comfirmbooking.payment.SavedcardInterface
import com.trava.user.ui.menu.bookings.BookingAdapter
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.utilities.Constants
import kotlinx.android.synthetic.main.activity_select_driver.*

class SelectDriverActivity : AppCompatActivity() , SavedDriverInterface {

    var binding : ActivitySelectDriverBinding?= null
    private var adapter: DriverAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_select_driver)
        binding!!.color = ConfigPOJO.Companion


        ivBack.setOnClickListener { finish() }

        var driver=intent.getParcelableArrayListExtra<HomeDriver>("drivers")

        binding?.rvBookings?.layoutManager = LinearLayoutManager(binding?.rvBookings?.context)
        adapter = DriverAdapter(this)
        binding?.rvBookings?.adapter = adapter
        adapter?.refreshList(driver!!)
    }

    override fun getSavedCardData(actionType: String, adapterPosition: Int, cardData: HomeDriver) {
        val intent = Intent()
        intent.putExtra("driver_id", cardData?.user_type_id)
        intent.putExtra("user_detail_id", cardData?.user_detail_id.toString())
        setResult(101, intent)
        finish()
    }
}