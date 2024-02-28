package com.trava.user.ui.menu.travelPackages

import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class TravellingPackagesContract{

    interface View: BaseView{
        fun onApiSuccess(list : List<PackagesItem>)
    }

    interface Presenter: BasePresenter<View>{
        fun requestGetPackagesApiCall()
    }
}