package com.te234.fnnj32Ra.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appsflyer.AppsFlyerLib
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.te234.fnnj32Ra.R
import com.te234.fnnj32Ra.databinding.FragmentLoadBinding
import com.te234.fnnj32Ra.di.MainApplication
import com.te234.fnnj32Ra.utils.checkForInternet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoadFragment : Fragment() {
    private var _binding: FragmentLoadBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoadViewModel by viewModels()

    private var fileData: ValueCallback<Uri>? = null
    private var filePath: ValueCallback<Array<Uri>>? = null
    private val startFileChooseForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                processFileChooseResult(result.data)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.webView.apply {
                    if (isFocused && canGoBack()) goBack()
                }
            }
        })

        val link = viewModel.getLink(requireContext())
        Log.d("TAG", "getCachedLink: $link")

        if (checkForInternet(requireContext())) {
            if (link != "") loadWebView(link) else setupBinomKeyJoeJol()
        } else {
            Toast.makeText(requireContext(), "No internet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView(link: String) = with(binding.webView) {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@with, true)
        }

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true

            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webViewClient = WebViewClient()
        webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                filePath = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
                startFileChooseForResult.launch(intent)
                return true
            }
        }
        clearCache(false)
        loadUrl(link)
        //loadUrl("google.com")
    }

    private fun processFileChooseResult(data: Intent?) {
        if (fileData == null && filePath == null) return

        var resultFileData: Uri? = null
        var resultsFilePath: Array<Uri>? = null

        if (data != null) {
            resultFileData = data.data
            resultsFilePath = arrayOf(Uri.parse(data.dataString))
        }
        fileData?.onReceiveValue(resultFileData)
        filePath?.onReceiveValue(resultsFilePath)
    }

    private fun setupBinomKeyJoeJol() {
        val firebaseConfig = FirebaseRemoteConfig.getInstance()
        val firebaseConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(2500)
            .build()
        firebaseConfig.setConfigSettingsAsync(firebaseConfigSettings)

        firebaseConfig.fetchAndActivate().addOnCompleteListener {
            val url = firebaseConfig.getString(getString(R.string.config_key))
            viewModel.setUrl(url)
            if (url.contains("http")) {
                startWork()
            }
            Log.d("TAG", "setupBinomKeyJoeJol: $url")
        }
    }

    private fun startWork() = lifecycleScope.launch(Dispatchers.IO) {
        getGoogleID()
        startInitFB()
        lifecycleScope.launch(Dispatchers.Main) {
            getAppsFlyerParams()
        }
    }

    private fun startInitFB() {
        FacebookSdk.setAutoInitEnabled(true)
        FacebookSdk.fullyInitialize()
        AppLinkData.fetchDeferredAppLinkData(requireContext()) {
            val targetUri = it?.targetUri.toString()
            viewModel.setDeepLink(targetUri)
        }
    }

    private fun getGoogleID() {
        val googleId = AdvertisingIdClient.getAdvertisingIdInfo(requireContext())
        viewModel.setGoogleID(googleId.id.toString())
    }

    private fun getAppsFlyerParams() {
        val appsFlyerUserId = AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext())
        viewModel.setAppsFlyerUserID(appsFlyerUserId)

        MainApplication.liveDataAppsFlyer.observe(viewLifecycleOwner) {
            for (inform in it) {
                when (inform.key) {
                    "af_status" -> {
                        viewModel.AppsFlyerParamSetup().afStatus(inform.value.toString())
                    }
                    "campaign" -> {
                        viewModel.AppsFlyerParamSetup().campaign(inform.value.toString())
                    }
                    "media_source" -> {
                        viewModel.AppsFlyerParamSetup().mediaSource(inform.value.toString())
                    }
                    "af_channel" -> {
                        viewModel.AppsFlyerParamSetup().afChannel(inform.value.toString())
                    }
                }
            }
            collectLink()
        }
    }

    private fun collectLink() {
        val mainLink = viewModel.saveLink(requireContext())
        loadWebView(mainLink)
    }
}