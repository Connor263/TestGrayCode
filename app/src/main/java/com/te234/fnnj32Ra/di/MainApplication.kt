package com.te234.fnnj32Ra.di

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.onesignal.OneSignal
import com.te234.fnnj32Ra.R

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppsFlyerLib.getInstance()
            .init(resources.getString(R.string.apps_dev_key), appsFlyerConversionListener(), this)
        AppsFlyerLib.getInstance().start(this)

        OneSignal.initWithContext(this)
        OneSignal.setAppId(resources.getString(R.string.one_signal_key))
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
    }


    private fun appsFlyerConversionListener(): AppsFlyerConversionListener {
        return object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                data?.let {
                    liveDataAppsFlyer.postValue(it)
                }
            }

            override fun onConversionDataFail(error: String?) {}
            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {}
            override fun onAttributionFailure(error: String?) {}
        }
    }

    companion object {
        val liveDataAppsFlyer = MutableLiveData<MutableMap<String, Any>>()
    }
}