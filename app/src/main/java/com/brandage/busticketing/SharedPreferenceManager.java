package com.brandage.busticketing;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Date;

public class SharedPreferenceManager {
    private SharedPreferences prefs;
    private static final String PREFS = "prefs";
    private static final String USER_NAME = "user_name";
    private static final String ADMIN_USER_NAME = "admin_user_name";
    private static final String PASSWORD = "password";
    private static final String ADMIN_PASSWORD = "admin_password";
    private static final String UUID = "uuid";
    private static SharedPreferenceManager Instance =  null;

    private SharedPreferenceManager(@NonNull Context context){
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Instance = this;
    }

    public static SharedPreferenceManager getInstance(@NonNull Context context){
        if (Instance == null){
            Instance = new SharedPreferenceManager(context);
        }
        return Instance;
    }

    public void save_password(String password){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PASSWORD, password);
        editor.apply();
    }

    public void save_admin_password(String password){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ADMIN_PASSWORD, password);
        editor.apply();
    }

    public void save_uuid(String uuid){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(UUID, uuid);
        editor.apply();
    }

    public void save_user_name(String user_name){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_NAME, user_name);
        editor.apply();
    }

    public void save_admin_user_name(String user_name){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ADMIN_USER_NAME, user_name);
        editor.apply();
    }

    public String get_user_name(){
        return prefs.getString(USER_NAME, null);
    }
    public String get_admin_user_name(){
        return prefs.getString(ADMIN_USER_NAME, null);
    }
    public String get_uuid(){
        return prefs.getString(UUID, null);
    }

    public String get_password(){
        return prefs.getString(PASSWORD, null);
    }
    public String get_admin_password(){
        return prefs.getString(ADMIN_PASSWORD, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
