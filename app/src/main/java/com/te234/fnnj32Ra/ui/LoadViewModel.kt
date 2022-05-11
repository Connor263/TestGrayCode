package com.te234.fnnj32Ra.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.onesignal.OneSignal
import com.te234.fnnj32Ra.data.AppStorage
import com.te234.fnnj32Ra.data.model.MainLink

class LoadViewModel : ViewModel() {
    private val appStorage = AppStorage
    private val mainLink = MainLink()

    fun saveLink(context: Context): String {
        val link = mainLink.collectLink(context)
        appStorage.saveLink(context, link)
        return link
    }

    fun getLink(context: Context): String {
        return appStorage.getLink(context)
    }


    fun setGoogleID(googleId: String) {
        mainLink.googleId = googleId
        OneSignal.setExternalUserId(googleId)
        Log.e("googleId", googleId)
    }

    fun setUrl(url: String) {
        mainLink.url = url
    }

    fun setDeepLink(targetUri: Uri?) {
        mainLink.deepLink = targetUri?.toString()
        Log.d("TAG", "setDeepLink deepLink: $targetUri")

        mainLink.deepLink?.let {
            val arrayDeepLink = it.split("//")
            mainLink.subAll = arrayDeepLink[1].split("_")

            Log.d("TAG", "setDeepLink subAll: ${mainLink.subAll}")
        }
    }

    fun setAppsFlyerUserID(id: String?) {
        mainLink.appsFlyerUserId = id
    }


    fun afStatus(value: String) {
        Log.d("TAG", "afStatus value: $value")
        if (value == "Organic" && mainLink.deepLink == null) {
            mainLink.mediaSource = "organic"
        }
    }

    fun campaign(value: String) {
        mainLink.campaign = value
        mainLink.campaign?.let {
            mainLink.subAll = it.split("_")
        }
    }

    fun mediaSource(value: String) {
        mainLink.mediaSource = value
    }

    fun afChannel(value: String) {
        mainLink.afChannel = value
    }
}