package com.gigzz.android.common

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import com.gigzz.android.R
import com.gigzz.android.databinding.ActivityWebViewBinding
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewActivity : BaseActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.apply {
            title = getStringExtra("title") as String
        }

//        binding.toolbar.btnBack.show()
//        binding.toolbar.toolbarTitle.text = title
        binding.webView.webViewClient = MyWebViewClient()
        initView()

        loadWebView()
        initListeners()

    }

    private fun loadWebView() {
        if (title.isNotEmpty()) {
           /* when (title) {
                getString(R.string.terms_of_service) -> binding.webView.loadUrl(AppConstants.TERMS_URL)
                getString(R.string.privacy_policy) -> binding.webView.loadUrl(AppConstants.PRIVACY_URL)
                "Reward" -> binding.webView.loadUrl(AppConstants.REWARD_SYSTEM_URL)
            }*/
        }
    }

    private fun initListeners() {
//         binding.toolbar.btnBack.setOnClickListener { onBackPressed() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true // enable javascript
            loadWithOverviewMode = true
            useWideViewPort = false
            builtInZoomControls = true
            domStorageEnabled = true
        }


        //back_icon.setOnClickListener{ onBackPressed() }
        // disable scroll on touch
        binding.webView.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    binding.webProgressBar.visibility = View.GONE
                }
            }
        }

        binding.webView.clearCache(true)
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url: String = request?.url.toString()
            view?.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            webView.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            binding.webProgressBar.show(null)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            binding.webProgressBar.remove()
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            binding.webProgressBar.remove()
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNetworkStatusChangedToAvailable() {
        // binding.scrollView.show()
    }

    override fun onNetworkUnavailable() {
        // showToast(this, getString(R.string.oops_your_connection_seems_off))
    }

}