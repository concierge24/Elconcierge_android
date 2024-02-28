package com.codebrew.clikat.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build.VERSION_CODES
import android.os.LocaleList
import androidx.annotation.RequiresApi
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.Utils.isAtLeastVersion
import com.trava.utilities.LocaleManager
import com.trava.utilities.SharedPrefs
import com.trava.utilities.Utils
import com.trava.utilities.constants.LANGUAGE_CHANGED
import com.trava.utilities.constants.LANGUAGE_CODE
import com.trava.utilities.constants.PREFS_LANGUAGE_ID
import java.util.*

class LocaleManager(private val context: Context?) {

    private var prefs: SharedPreferences ?=null

    fun setLocale(c: Context): Context {
        return updateResources(c, getLanguage())
    }

    fun setNewLocale(c: Context, language: String): Context {
        AppConstants.LANG_CODE=language
        persistLanguage(language)
        return updateResources(c, language)
    }

    fun  getLanguage(): String {
        return prefs?.getString(DataNames.SELECTED_LANGUAGE, LANGUAGE_ENGLISH)?:""
    }


    @SuppressLint("ApplySharedPref")
    private fun persistLanguage(language: String) {

        SharedPrefs.get().save(PREFS_LANGUAGE_ID, if (language != "en") 2 else 1)
        SharedPrefs.get().save(LANGUAGE_CHANGED, language != "en")
        SharedPrefs.get().save(LANGUAGE_CODE, Utils.getLanguageId(language).toString())

        // use commit() instead of apply(), because sometimes we kill the application process
        // immediately that prevents apply() from finishing
        prefs?.edit()?.putString(DataNames.SELECTED_LANGUAGE, language)?.commit()
        prefs?.edit()?.putString(DataNames.RIDE_LANGUAGE_KEY, language)?.commit()
    }

    private fun updateResources(context: Context, language: String?): Context {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        if (isAtLeastVersion(VERSION_CODES.N)) {
            setLocaleForApi24(config, locale)
            context = context.createConfigurationContext(config)
        } else if (isAtLeastVersion(VERSION_CODES.JELLY_BEAN_MR1)) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }

    @RequiresApi(api = VERSION_CODES.N)
    private fun setLocaleForApi24(config: Configuration, target: Locale) {
        val set: MutableSet<Locale> = LinkedHashSet()
        // bring the target locale to the front of the list
        set.add(target)
        val all = LocaleList.getDefault()
        for (i in 0 until all.size()) {
            // append other locales supported by the user
            set.add(all[i])
        }
        val locales = set.toTypedArray()
        config.setLocales(LocaleList(*locales))
    }

    companion object {
        const val LANGUAGE_ENGLISH = "english"

        fun getLocale(res: Resources): Locale {
            val config = res.configuration
            return if (isAtLeastVersion(VERSION_CODES.N)) config.locales[0] else config.locale
        }
    }

    init {
        prefs = context?.getSharedPreferences(PrefenceConstants.SharedPrefenceName, Context.MODE_PRIVATE)
    }

}