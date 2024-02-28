package com.trava.user.utils

import com.trava.user.webservices.models.applabels.AppForunSetting
import com.trava.user.webservices.models.appsettings.DynamicBar
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.user.webservices.models.homeapi.TerminologyData

class ConfigPOJO() {
    companion object {
        var TEMPLATE_CODE = 111
        var primary_color = "#000000"
        var transparent = "#00000000"
        var black_color = "#000000"
        var gray_color = "#a9a9a9"
        var white_color = "#ffffff"
        var secondary_color = "#9ccb53"
        var defaultCoutryCode = "+1"
        var ISO_CODE = "US"
        var headerColor = "#ffffff"
        var header_txt_colour = "#ffffff"
        var Btn_Text_Colour = "#ffffff"
        var currency = "NGN"
        var Btn_Colour = "#ffffff"

        //var SECRET_API_KEY ="AIzaSyAGCCNk1wfi0ZH3R_JzYgGCX5CgRbYNnWY" //biplife
        //essential
        //var SECRET_API_KEY ="AIzaSyBZ4f5pkv9sOeg-pqIRptImIE9rFfeUrzs"
        //delibear
        //   var SECRET_API_KEY ="AIzaSyDmh2miaoR6ZIO8UhrriKYtSd-i5fPJLMY"
        // var SECRET_API_KEY ="AIzaSyDmh2miaoR6ZIO8UhrriKYtSd-i5fPJLMY"
        //movify
        //var SECRET_API_KEY = "AIzaSyBVN0NqrbMR4iJ8wIkrTo9yrZ0MP2F_igw"

        var SECRET_API_KEY ="AIzaSyDvlkMN-vHxyJH7eOaT4pDGFFnChno1kyM" //wassel
        // var SECRET_API_KEY ="AIzaSyCH2i_159alRNWwSt4x8Njdxt_sUNkwFOc"
        //   var SECRET_API_KEY ="AIzaSyAePLpGrTOkT1BacgEo6kw-J63lxebZM2Y"//bagndelivery

        // var SECRET_API_KEY ="AIzaSyCS3jH5k8pc9zendUy8RkspVwwvnSEHEWk"//duka
        //var SECRET_API_KEY ="AIzaSyDDQTIAxyBhCHWl3NCmI3nV_hnVR4cda_w"//clickapp
       // var SECRET_API_KEY ="AIzaSyAGCCNk1wfi0ZH3R_JzYgGCX5CgRbYNnWY"//marketplace
        //var SECRET_API_KEY ="AIzaSyBDG7HQl27Vr_sDpa7i60lrW3UjkjQT3ic"//transporter

        //var SECRET_API_KEY = "AIzaSyCn_33y8TFxb6ODMj_4WtVqlY-pk_QkhFA"//jamesessential

        //  var SECRET_API_KEY ="AIzaSyDXRh9tewQv-qyTgge-V31xqgX5Ee3Zeek"//affar
        // var SECRET_API_KEY = "AIzaSyAeSpI2mWs9Xe_uNM76030ybjeljV29WRE"  //mozzeats
         //var SECRET_API_KEY = "AIzaSyBHtQ2XgcOaFu-TtDJufebqCwFHCQicPsM"  //road

        //    var SECRET_API_KEY = "AIzaSyCbOJ6SZ_0mHB1NJf37-67fxqKGTvkrzgc"//thena

        //    var SECRET_API_KEY = "AIzaSyAeSpI2mWs9Xe_uNM76030ybjeljV29WRE"  //mozzeats
        //var SECRET_API_KEY = "AIzaSyCeSxMxVEqAzEQhwPQOJ4AweYEGzMMm3Kc"  //wagon

        var OMAN_CURRENCY = 0.0
        var OMAN_PHONECODE = ""
        var settingsResponse: SettingItems? = null
        var TerminologyData: TerminologyData? = null
        var gateway_unique_id = ""
        var STRIPE_SECRET_KEY = ""
        var STRIPE_PUBLIC_KEY = ""
        var THAWANI_API_KEY = ""
        var THAWANI_PUBLIC_KEY = ""
        var razorpay_private_key = ""
        var razorpay_secret_key = ""
        var conekta_api_key = ""
        var conekta_api_version = ""
        var conekta_locale = "en"
        var conekta_public_key = ""
        var surCharge = ""
        var is_countrycheck = ""
        var surChargePercentage = ""
        var minimum_wallet_balance = "0"
        var dynamicBar: DynamicBar? = null
        var placeApiKey = ""
        var support_number = ""
        var support_email = ""
        var is_merchant = ""
        var is_omco = ""
        var is_facebookLogin = ""
        var is_asap = ""
        var is_water_platform = ""
        var search_count = "0"
        var distance_search_increment = "0"
        var distance_search_start = "0"
        var is_cash_on_Delivery = ""
        var multiple_request = "1"
        var is_childrenTraveling = ""
        var is_darkMap = ""
        var is_gift = ""
        var is_cash_payment_enabled = ""
        var is_card_payment_enabled = ""
        var is_google_login = ""
        var is_gender_selection_enabled = ""
        var admin_base_url = ""
        var play_video_after_splash_images = ""
        var is_play_video_after_splash = ""
        var play_video_url = ""
        var is_hood_app=""
        var appLablesList: ArrayList<AppForunSetting> = ArrayList()
    }
}