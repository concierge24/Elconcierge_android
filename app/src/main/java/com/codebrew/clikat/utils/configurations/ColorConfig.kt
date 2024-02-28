package com.codebrew.clikat.utils.configurations

import android.graphics.Color
import com.codebrew.clikat.modal.other.SettingModel.DataBean.SettingData
import java.util.regex.Pattern

class ColorConfig(settingFlow: SettingData?, darkMode: Boolean) {

    private val pattern: Pattern = Pattern.compile(HEX_PATTERN)


    @kotlin.jvm.JvmField
    var offwhite: String? = "#80FFFFFF"

    @kotlin.jvm.JvmField
    var primaryColor: String? = "#ACB1AD"

    @kotlin.jvm.JvmField
    var appBackground = "#ffffff"

    @kotlin.jvm.JvmField
    var listBackground = "#FFFFFF"

    @kotlin.jvm.JvmField
    var textAppTitle: String? = "#000000"

    @kotlin.jvm.JvmField
    var textHead = "#000000"

    @kotlin.jvm.JvmField
    var textSubhead = "#909090"

    @kotlin.jvm.JvmField
    var textBody = "#909090"

    @kotlin.jvm.JvmField
    var radioSelected = "#58dc00"

    @kotlin.jvm.JvmField
    var yellowcolor = "#FFC947"

    @kotlin.jvm.JvmField
    var supplier_bg = "#f9f9f9"

    @kotlin.jvm.JvmField
    var app_light_bg = "#f9f9f9"

    @kotlin.jvm.JvmField
    var toolbarColor: String? = "#FFFFFF"

    @kotlin.jvm.JvmField
    var toolbarText: String? = "#FFFFFF"

    @kotlin.jvm.JvmField
    var rating_color = "#ACB1AD"

    @kotlin.jvm.JvmField
    var primaryColor_trans = "#80ACB1AD"

    @kotlin.jvm.JvmField
    var category_color_trans = "#4D000000"

    @kotlin.jvm.JvmField
    var fb_button = "#FF435898"

    @kotlin.jvm.JvmField
    var google_button = "#4386F4"

    @kotlin.jvm.JvmField
    var open_now = "#5AA82B"

    @kotlin.jvm.JvmField
    var rejectColor = Color.RED

    @kotlin.jvm.JvmField
    var listItemBackground: String? = null

    @kotlin.jvm.JvmField
    var divider = "#DDDDDD"

    @kotlin.jvm.JvmField
    var tabSelected = "#ACB1AD"

    @kotlin.jvm.JvmField
    var tabUnSelected = "#909090"

    @kotlin.jvm.JvmField
    var homelistBackground = "#F5F7FB"

    @kotlin.jvm.JvmField
    var navSubTextColor = "#ffffff"

    @kotlin.jvm.JvmField
    var textListHead = "#000000"

    @kotlin.jvm.JvmField
    var textListSubhead = "#909090"

    @kotlin.jvm.JvmField
    var textListBody = "#909090"

    @kotlin.jvm.JvmField
    var search_background = "#F4F4F4"

    @kotlin.jvm.JvmField
    var search_textcolor = "#80000000"

    @kotlin.jvm.JvmField
    var food_rate_color = "#ED8A19"

    @kotlin.jvm.JvmField
    var toolbarTabIndicatorColor: String? = "#FFFFFF"

    private fun validate(hexColorCode: String?): Boolean {
        return if (hexColorCode.isNullOrEmpty()) {
            false
        } else {
            val matcher = pattern.matcher(hexColorCode)
            matcher.matches()
        }
    }

    companion object {
        private const val HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
    }

    init {
        primaryColor = if (validate(settingFlow?.theme_color)) settingFlow?.theme_color else "#ACB1AD"
        textAppTitle = if (validate(settingFlow?.element_color)) settingFlow?.element_color else "#515151"
        toolbarColor = if (validate(settingFlow?.header_color)) settingFlow?.header_color else "#212B36"
        toolbarText = if (validate(settingFlow?.header_text_color)) settingFlow?.header_text_color else "#000000"

        if (settingFlow?.zipTheme == "1") {
            supplier_bg = StringBuilder(primaryColor ?: "").insert(1, "33").toString()
        }
        app_light_bg = StringBuilder(primaryColor ?: "").insert(1, "33").toString()
        primaryColor_trans = StringBuilder(primaryColor ?: "").insert(1, "80").toString()
        navSubTextColor = StringBuilder(primaryColor ?: "").insert(1, "A6").toString()


        if (darkMode) {
            appBackground = "#000000"
            listBackground = "#000000"
            textAppTitle = "#FFFFFF"
            textSubhead = "#fff5f5f5"
            textBody = "#909090"
            supplier_bg = "#000000"
            app_light_bg = "#FFFFFF"
            toolbarColor = "#000000"
            toolbarText = "#000000"
            divider = "#FFFFFF"
            homelistBackground = "#000000"
            search_background = "#99000000"
        }else
        {
            textSubhead=StringBuilder(textAppTitle ?: "").insert(1, "B3").toString()
            textBody=StringBuilder(textAppTitle ?: "").insert(1, "CC").toString()
        }

        textHead = textAppTitle ?: "#000000"
        tabSelected = primaryColor?:"#ACB1AD"
        navSubTextColor = appBackground?:"#ffffff"
        textListHead = textHead?:"#000000"
        textListSubhead = textSubhead
        textListBody = textBody
        listItemBackground=appBackground
    }
}