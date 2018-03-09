package it.cnr.iit.sensapp;

import android.content.Context;

public class PreferencesController {

    private static final String PREFS = "contextLabeler";
    public static final String PREF_SETUP_COMPLETE = "setupComplete";

    public static final String PREF_FB = "fb";
    public static final String PREF_TW = "tw";

    public static boolean isSetupComplete(Context context){
        return context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getBoolean(
                PREF_SETUP_COMPLETE, false);
    }

    public static void storeSetupComplete(Context context){
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(
                PREF_SETUP_COMPLETE, true).apply();
    }

    public static void storeSocialLogin(Context context, boolean fb, long tw){
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(
                PREF_FB, fb).apply();
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(
                PREF_TW, tw).apply();
    }

    public static boolean isFbLogged(Context context){
        return context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getBoolean(
                PREF_FB, false);
    }

    public static long isTwitterLogged(Context context){
        return context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getLong(
                PREF_TW, -1);
    }
}
