package com.codebrew.clikat.utils.configurations

import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.modal.other.SettingModel.DataBean.SettingData
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.ClikatConstants

object Configurations {
    private var screenFlowBean: ScreenFlowBean = Prefs.getPrefs().getObject(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
    private var terminologyBean = Prefs.getPrefs().getObject(PrefenceConstants.APP_TERMINOLOGY, SettingModel.DataBean.Terminology::class.java)
    private val settingFlowBean = Prefs.getPrefs().getObject(DataNames.SETTING_DATA, SettingData::class.java)
    private val darkModeEnabled = Prefs.getPrefs().getBoolean(PrefenceConstants.DARK_MODE_ENABLE, false)
    private val languageId = Prefs.getPrefs().getString(DataNames.SELECTED_LANGUAGE, ClikatConstants.ENGLISH_FULL)

    @kotlin.jvm.JvmField
    var strings = TextConfig(screenFlowBean.app_type, terminologyBean, languageId)

    @kotlin.jvm.JvmField
    var colors = ColorConfig(settingFlowBean, darkModeEnabled)

    @kotlin.jvm.JvmField
    var drawables = DrawablesConfig()


}

