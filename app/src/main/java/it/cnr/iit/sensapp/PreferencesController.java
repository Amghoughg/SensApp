package it.cnr.iit.sensapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.mcampana.instadroid.model.User;

public class PreferencesController {

    private static final String PREFS = "contextLabeler";
    public static final String PREF_SETUP_COMPLETE = "setupComplete";

    public static final String PREFS_INSTAGRAM_TOKEN = "InstagramToken";
    public static final String PREFS_INSTAGRAM_USER= "InstagramUser";

    public static void storeInstagramProfile(Context context, User user, String token){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.putString(PREFS_INSTAGRAM_USER, new Gson().toJson(user)).apply();
        editor.putString(PREFS_INSTAGRAM_TOKEN, token).apply();
    }

    public static String getInstagramToken(Context context){
        return context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(
                PREFS_INSTAGRAM_TOKEN, null);
    }

    public static User getInstagramUser(Context context){

        String json = context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(
                PREFS_INSTAGRAM_USER, null);
        return json == null ? null : new Gson().fromJson(json, User.class);
    }

    public static boolean isSetupComplete(Context context){
        return context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getBoolean(
                PREF_SETUP_COMPLETE, false);
    }

    public static void storeSetupComplete(Context context){
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(
                PREF_SETUP_COMPLETE, true).apply();
    }
}
