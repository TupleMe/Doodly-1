package tuple.me.lily.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by gokul on 06/04/2016.
 * This class helps to store and retrieve data form shared preferences.
 */
@SuppressWarnings({"UnusedDeclaration", "CommitPrefEdits"})
public class Prefs {

    private static Prefs prefs;
    private final SharedPreferences mPref;

    public Prefs(@NonNull Context context, @NonNull String name) {
        mPref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }


    public static synchronized void init(@NonNull Context context) {
        if (prefs == null) {
            prefs = new Prefs(context, context.getPackageName());
        }
    }

    public static synchronized void init(@NonNull Context context, @NonNull String name) {
        if (prefs == null) {
            prefs = new Prefs(context, name);
        }
    }

    public static synchronized Prefs getInstance() {
        if (prefs == null) {
            throw new IllegalStateException(Prefs.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return prefs;
    }

    public boolean set(@NonNull String key, boolean value) {
        return mPref.edit()
                .putBoolean(key, value)
                .commit();
    }

    public boolean set(@NonNull String key, int value) {
        return mPref.edit()
                .putInt(key, value)
                .commit();
    }

    public boolean set(@NonNull String key, long value) {
        return mPref.edit()
                .putLong(key, value)
                .commit();
    }

    public boolean set(@NonNull String key, float value) {
        return mPref.edit()
                .putFloat(key, value)
                .commit();
    }

    public boolean set(@NonNull String key, String value) {
        return mPref.edit()
                .putString(key, value)
                .commit();
    }

    public boolean set(@NonNull String key, Set<String> value) {
        return mPref.edit()
                .putStringSet(key, value)
                .commit();
    }

    public boolean setAndGet(@NonNull String key, boolean value) {
        mPref.edit()
                .putBoolean(key, value)
                .commit();
        return value;
    }

    public int setAndGet(@NonNull String key, int value) {
        mPref.edit()
                .putInt(key, value)
                .commit();
        return value;
    }

    public long setAndGet(@NonNull String key, long value) {
        mPref.edit()
                .putLong(key, value)
                .commit();
        return value;
    }

    public float setAndGet(@NonNull String key, float value) {
        mPref.edit()
                .putFloat(key, value)
                .commit();
        return value;
    }

    @Nullable
    public String setAndGet(@NonNull String key, @Nullable String value) {
        mPref.edit()
                .putString(key, value)
                .commit();
        return value;
    }


    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return mPref.getBoolean(key, defaultValue);
    }

    public int getInt(@NonNull String key, int defaultValue) {
        return mPref.getInt(key, defaultValue);
    }

    public long getLong(@NonNull String key, long defaultValue) {
        return mPref.getLong(key, defaultValue);
    }

    public float getFloat(@NonNull String key, float defaultValue) {
        return mPref.getFloat(key, defaultValue);
    }

    @Nullable
    public String getString(@NonNull String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    @Nullable
    public Set<String> getStringSet(@NonNull String key) {
        Set<String> set = new HashSet<>();
        return mPref.getStringSet(key, set);
    }

    @Nullable
    public SharedPreferences getPreferences() {
        return mPref;
    }

    public Map<String, ?> getMap() {
        return mPref.getAll();
    }

    public boolean remove(String key) {
        return mPref.edit()
                .remove(key)
                .commit();
    }

    public boolean clear() {
        return mPref.edit()
                .clear()
                .commit();
    }

    public boolean contains(String key) {
        return mPref.contains(key);
    }
}

