package com.te234.fnnj32Ra.data

import android.content.Context
import android.content.SharedPreferences

const val APP_STORAGE = "APP_STORAGE"
const val CACHE_LINK = "CACHE_LINK"

object AppStorage {
    private fun getAppStorage(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_STORAGE, Context.MODE_PRIVATE)
    }

    fun saveLink(context: Context, link: String) {
        getAppStorage(context).edit().putString(CACHE_LINK, link).apply()
    }

    fun getLink(context: Context): String {
        return getAppStorage(context).getString(CACHE_LINK, "") ?: ""
    }
}