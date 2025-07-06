package com.cp.fishthebreak.screens.fragments.auth

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cp.fishthebreak.databinding.FragmentWebPageBinding
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible

class WebPageFragment : Fragment() {

    private lateinit var binding: FragmentWebPageBinding
    private val navArgs: WebPageFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWebPageBinding.inflate(layoutInflater,container,false)
        initWebView()
        return binding.root
    }

    private fun initWebView(){
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        showHideLoader(true)
        binding.webView.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if(newProgress == 100){
                    showHideLoader(false)
                }
            }
        }
        binding.webView.webViewClient = object: WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return super.shouldOverrideUrlLoading(view, request)

            }
        }
        binding.webView.settings.loadsImagesAutomatically = true
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.setSupportZoom(true)
        if (Build.VERSION.SDK_INT < 8) {
            binding.webView.settings.mediaPlaybackRequiresUserGesture = true
        } else {
            binding.webView.settings.pluginState = WebSettings.PluginState.ON
        }
        binding.webView.loadUrl(navArgs.webUrl)
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }

}