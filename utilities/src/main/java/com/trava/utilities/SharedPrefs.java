package com.trava.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedPrefs {

    public static final String TAG = "RoyoFoodCab";

    private static SharedPrefs singleton = null;

    private static SharedPreferences preferences;

    private static SharedPreferences.Editor editor;

    private List<SharedPrefsChangeListener> listeners = new ArrayList<>();

    private static Gson GSON = new Gson();
    Type typeOfObject = new TypeToken<Object>() {
    }.getType();

    @SuppressLint("CommitPrefEdits")
    private SharedPrefs(Context context) {
        preferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static SharedPrefs with(Context context) {
        if (singleton == null) {
            singleton = new Builder(context).build();
        }
        return singleton;
    }

    public static SharedPrefs get(){
        return singleton;
    }

    public void save(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public void save(String key, String value) {
        editor.putString(key, value).apply();
    }

    public void save(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public void save(String key, float value) {
        editor.putFloat(key, value).apply();
    }

    public void save(String key, long value) {
        editor.putLong(key, value).apply();
    }

    public void save(String key, Set<String> value) {
        editor.putStringSet(key, value).apply();
    }

    // to save object in prefrence
    public void save(String key, Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }

        if (key.equals("")) {
            throw new IllegalArgumentException("key is empty or null");
        }

        editor.putString(key, GSON.toJson(object)).apply();
    }

    // To get object from prefrences

    public <T> T getObject(String key, Class<T> a) {

        String gson = preferences.getString(key, null);
        if (gson == null) {
            return null;
        } else {
            try {
                return GSON.fromJson(gson, a);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object storaged with key "
                        + key + " is instanceof other class");
            }
        }
    }

    public <T> void saveList(String key, List<T> object) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }

        if (key.equals("")) {
            throw new IllegalArgumentException("key is empty or null");
        }
        editor.putString(key, GSON.toJson(object)).apply();
    }

    // To get object from prefrences
    public <T> List<T> getObjectList(String key, Class<T[]> a) {

        String gson = preferences.getString(key, null);
        if (gson == null) {
            return null;
        } else {
            try {
                T[] arr = new Gson().fromJson(gson, a);
                return Arrays.asList(arr);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object storaged with key "
                        + key + " is instanceof other class");
            }
        }
    }

    public boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public Set<String> getStringSet(String key, Set<String> defValue) {
        return preferences.getStringSet(key, defValue);
    }

    public Map<String, ?> getAll() {

        return preferences.getAll();
    }

    public void remove(String key) {
        editor.remove(key).apply();
    }


    public void removeAll() {
        editor.clear();
        editor.apply();
    }

    public void addSharedPreferenceChangeListener(SharedPrefsChangeListener listener) {
        if (listeners.isEmpty()) {
            preferences.registerOnSharedPreferenceChangeListener(sharedPrefsChangeLister);
        }
        listeners.add(listener);

    }

    public void removeSharedPreferenceChangeListener(SharedPrefsChangeListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            preferences.unregisterOnSharedPreferenceChangeListener(sharedPrefsChangeLister);
        }
    }

    public void removeAllSharedPrefsChangeListeners() {
        listeners.clear();
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPrefsChangeLister);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefsChangeLister = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            notifySharedPrefsChangeListeners(sharedPreferences, key);
        }
    };

    public interface SharedPrefsChangeListener {
        void onPrefsChanged(SharedPreferences sharedPreferences, String key);
    }

    private void notifySharedPrefsChangeListeners(SharedPreferences sharedPreferences, String key) {
        for (SharedPrefsChangeListener listener : listeners) {
            listener.onPrefsChanged(sharedPreferences, key);
        }
    }

    private static class Builder {

        private final Context context;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context.getApplicationContext();
        }

        /**
         * Method that creates an instance of com.codebrew.obsuser.utils.SharedPrefs
         *
         * @return an instance of com.codebrew.obsuser.utils.SharedPrefs
         */
        public SharedPrefs build() {
            return new SharedPrefs(context);
        }
    }
}