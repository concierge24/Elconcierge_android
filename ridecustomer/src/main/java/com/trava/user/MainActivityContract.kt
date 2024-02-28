package com.trava.user

import com.trava.user.webservices.models.applabels.Data
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import org.json.JSONObject

class MainActivityContract
{
    interface View: BaseView{
        fun onApiSuccess(response: SettingItems?)
        fun onApiSuccess(price: JSONObject)
        fun onAppLablesSuccess(appLables: Data?)
    }

    interface Presenter: BasePresenter<View>
    {
        fun appSetting()
        fun getCurrencyPrice()
        fun getAppLables()
    }
}