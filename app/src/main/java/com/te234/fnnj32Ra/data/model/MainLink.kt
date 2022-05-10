package com.te234.fnnj32Ra.data.model

import android.content.Context
import com.te234.fnnj32Ra.R

data class MainLink(
    var googleId: String? = null,
    var appsFlyerUserId: String? = null,
    var subAll: List<String> = listOf("", "", "", "", "", "", "", "", "", ""),
    var deepLink: String? = null,
    var mediaSource: String? = null,
    var afChannel: String? = null,
    var campaign: String? = null,
    var url: String? = null,
) {

    fun collectLink(context: Context): String {
        val resources = context.resources
        val packageName = context.packageName
        val appsFlyerDevKey = resources.getString(R.string.apps_dev_key)
        val fbToken = resources.getString(R.string.facebook_token)
        val fbAppId = resources.getString(R.string.facebook_app_id)

        val subsString = ""
        subAll.forEachIndexed { index, string ->
            subsString.plus(
                "&sub${index.plus(1)}=" +
                        try {
                            string
                        } catch (e: Exception) {
                            ""
                        }
            )
        }

        return "$url?media_source=$mediaSource" +
                "&google_adid=$googleId" +
                "&af_userid=$appsFlyerUserId" +
                "&bundle=${packageName}" +
                "&dev_key=${appsFlyerDevKey}" +
                "&fb_at=${fbToken}" +
                "&fb_app_id=${fbAppId}" +
                "&af_channel=$afChannel" +
                "&campaign=$campaign".plus(subsString)
    }
}