package com.example.ruleta;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenciasUsuario {
    private static final String PREFS_NAME = "prefsRuleta";

    public static boolean shouldPlayMusic(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("musicEnabled", true);  // true por defecto
    }

    public static void saveMusicPreference(Context context, boolean isEnabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("musicEnabled", isEnabled);
        editor.apply();
    }
}
