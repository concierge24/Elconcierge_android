package com.trava.user.ui.home.dropofflocation

import com.trava.user.webservices.models.add_address_home_work.HomeWorkResponse
import com.trava.user.webservices.models.locationaddress.GetAddressesPojo
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.roadPickup.RoadPickupResponse
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import java.math.BigInteger

class AddStopsContract{

    interface View: BaseView{
        fun onApiSuccess(order : Order)
        fun onApiHomeWorkSuccess(response: HomeWorkResponse)
        fun onApiEditHomeWorkSuccess(response: HomeWorkResponse)
        fun onApiLocationsSuccess(response: GetAddressesPojo)
    }

    interface Presenter: BasePresenter<View>{
        fun requestAddStopsDuringRide(map : HashMap<String,String>)
        fun requestHomeWorkAddress(map : HashMap<String,String>)
        fun requestEditHomeWorkAddress(map : HashMap<String,String>)
        fun requestLocationsData(map : HashMap<String,String>)
    }
}