package com.gigzz.android.data.preference

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

class PrefKeys {
    companion   object{
        val HAS_COOKIE = booleanPreferencesKey("has_cookie")
        val AVATAR = stringPreferencesKey("avatar")
        val DEVICE_TOKEN = stringPreferencesKey("device_token")
        val AUTH_KEY = stringPreferencesKey("auth_key")
        val BUCKET_NAME = stringPreferencesKey("BUCKET_NAME")
        val SECRET_KEY = stringPreferencesKey("SECRET_KEY")
        val ACCESS_KEY = stringPreferencesKey("ACCESS_KEY")
        val IS_LOGIN = booleanPreferencesKey("is_login")
        val IS_SECOND_TIME = booleanPreferencesKey("is_second_time")
    }
}