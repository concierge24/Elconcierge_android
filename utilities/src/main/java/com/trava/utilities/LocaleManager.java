package com.trava.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleManager {

    public static final String LANGUAGE_ENGLISH = "en";

    public static final String LANGUAGE_ARABIC = "ar";
    public static final String LANGUAGE_SPANISH = "es";
    public static final String LANGUAGE_URDU = "ur";

    public static final String LANGUAGE_HINDI = "hi";

    public static final String LANGUAGE_CHINESE = "zh";

    public static final String LANGUAGE_SINHALA = "si-rLK";

    public static final String LANGAGE_TAMIL = "ta-rLK";

     static final String LANGUAGE_KEY = "language_key";

    public static Context setLocale(Context c) {
        return updateResources(c, getLanguage(c));
    }

    public static Context setNewLocale(Context c, String language) {
        persistLanguage(c, language);
        return updateResources(c, language);
    }

    public static String getLanguage(Context c) {
        SharedPreferences prefs =c.getSharedPreferences(SharedPrefs.TAG,Context.MODE_PRIVATE);
        return prefs.getString(LANGUAGE_KEY, Locale.getDefault().getLanguage());
    }

    @SuppressLint("ApplySharedPref")
    private static void persistLanguage(Context c, String language) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        // use commit() instead of apply(), because sometimes we kill the application process immediately 
        // which will prevent apply() to finish 
        prefs.edit().putString(LANGUAGE_KEY, language).commit();
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= 21) {
            config.setLocale(locale);
            config.setLayoutDirection(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }

    public static Locale getLocale(Resources res) {
        Configuration config = res.getConfiguration();
        return Build.VERSION.SDK_INT >= 24 ? config.getLocales().get(0) : config.locale;
    }
} 