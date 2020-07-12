package com.wiandro.awesomebrowser

import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.wiandro.awesomebrowser.databinding.FragmentBrowserBinding

/**
 * CREATED BY Javadhme
 */
class BrowserFragment : Fragment(), WebClientCallback {

    private var url: String? = ""
    private var showAddressBar = false
    private var requestHeaders: HashMap<String, String>? = null
    private lateinit var cacheModePolicy: CacheMode
    private var callback: BrowserCallback? = null

    private var _binding: FragmentBrowserBinding? = null
    private val mBinding: FragmentBrowserBinding
        get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null) return

        arguments?.apply {
            url = getString(KEY_PRODUCT_URL)
            showAddressBar = getBoolean(KEY_SHOW_URL_BAR)
            requestHeaders = getSerializable(KEY_REQUEST_HEADERS) as HashMap<String, String>?
            cacheModePolicy = getSerializable(KEY_REQUEST_CACHE_MODE) as CacheMode
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.headerLayout.visibility = if (showAddressBar) View.VISIBLE else View.GONE

        initializeWebView()

        loadUrl()

        mBinding.icClose.setOnClickListener {
            callback?.onCloseBrowser()
        }
    }


    override fun onLoadStart(url: String) {
        with(mBinding) {
            progressbar.visibility = View.VISIBLE
            errorLayout.visibility = View.GONE
            sslErrorLayout.visibility = View.GONE
            urlBar.text = url
        }
        onLoadFinished(url)
    }

    override fun onLoadFinish(url: String) {
        mBinding.progressbar.visibility = View.GONE
        onLoadFinished(url)
    }

    override fun needBackPress() {
        activity?.onBackPressed() ?:apply {
            Log.w(TAG, "needBackPress: activity is NULL")
        }
    }

    override fun onErrorHappened(errorCode: Int, description: String?, failingUrl: String?) {
        with(mBinding) {
            progressbar.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
        }
    }

    override fun onSslErrorHappened(handler: SslErrorHandler?, error: SslError?) {

        changeURLtoSSLError(url!!)

        with(mBinding) {

            progressbar.visibility = View.GONE
            sslErrorLayout.visibility = View.VISIBLE
            icSecureConnection.setImageResource(R.drawable.ic_lock_red)

            proceed.setOnClickListener { view12 ->
                sslErrorLayout.visibility = View.GONE
                handler!!.proceed()
            }

            cancel.setOnClickListener { view1 ->
                sslErrorLayout.visibility = View.GONE
                handler!!.cancel()
                if (activity != null) {
                    activity!!.onBackPressed()
                }
            }
        }
    }

    fun onBackPressed() =
        if (mBinding.webview.canGoBack()) {
            mBinding.webview.goBack()
            true
        } else
            false

    fun setCallback(callback: BrowserCallback) {
        this.callback = callback
    }

    private fun loadUrl() {

        url?.let {
            mBinding.webview.loadUrl(it, requestHeaders)
        } ?: apply {
            //TODO show empty View
        }

        Log.i(TAG, "onViewCreated: URL->$url")
    }

    private fun initializeWebView() {

        with(mBinding.webview) {

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            setupSetting(this)
            webViewClient = WebClient(callback, this@BrowserFragment)
        }

    }

    private fun setupSetting(webView: WebView) {

        with(webView.settings) {

            lightTouchEnabled = true
            javaScriptEnabled = true //TODO get this from builder
            cacheMode = cacheModePolicy.getModeValue()
            setAppCacheEnabled(true)
            domStorageEnabled = true
            allowFileAccess = true
            loadWithOverviewMode = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            useWideViewPort = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBinding.webview.settings.mixedContentMode =
                    WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

        }
    }

    private fun setBeautifyURL(url: String, isError: Boolean) {
        val uri = Uri.parse(url)

        if (uri == null) {
            mBinding.urlBar.text = url
            return
        }

        val scheme = uri.scheme
        val host = uri.host
        val path = uri.path

        if (scheme == null || host == null) {
            mBinding.urlBar.text = url
            return
        }

        val schemeSpannable = getSchemeSpannable(scheme, isError)
        mBinding.urlBar.text = schemeSpannable
        val hostSpannable =
            getSpannableByColor(host, COLOR_FOR_HOST)
        mBinding.urlBar.append(hostSpannable)

        if (path != null) {
            val pathSpannable =
                getSpannableByColor(path, COLOR_FOR_PATH_OF_HOST)
            mBinding.urlBar.append(pathSpannable)
        }
    }


    private fun getSchemeSpannable(
        scheme: String,
        isError: Boolean
    ): SpannableString? {
        Log.d(TAG, "getSchemeSpannable() called with: scheme = [$scheme], isError = [$isError]")

        if (isError) return getSpannableByColor("$scheme://", COLOR_RED_SCHEME_FOR_ERRORS)
        return if ("https".equals(scheme, ignoreCase = true))
            getSpannableByColor("$scheme://", COLOR_GREEN_FOR_HTTPS_SCHEME)
        else getSpannableByColor("$scheme://", COLOR_GRAY_FOR_HTTP_SCHEME)
    }


    private fun getSpannableByColor(string: String, color: String): SpannableString? {

        val spannableString = SpannableString(string)

        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor(color)),
            0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    private fun onLoadFinished(url: String) {
        val uri = Uri.parse(url) ?: return
        setBeautifyURL(url, false)
        setRelatedIcon(url)
    }

    private fun setRelatedIcon(url: String) {
        val uri = Uri.parse(url) ?: return
        val scheme = uri.scheme
        if ("https".equals(scheme, ignoreCase = true)) {
            mBinding.icSecureConnection.setImageResource(R.drawable.ic_lock_green)
        } else {
            mBinding.icSecureConnection.setImageResource(R.drawable.ic_info)
        }
    }

    private fun changeURLtoSSLError(url: String) {
        val uri = Uri.parse(url) ?: return
        setBeautifyURL(url, true)
        mBinding.icSecureConnection.setImageResource(R.drawable.ic_lock_red)
    }

    companion object {
        private val TAG: String = BrowserFragment::class.java.simpleName

        private const val COLOR_RED_SCHEME_FOR_ERRORS = "#FF0000"
        private const val COLOR_GREEN_FOR_HTTPS_SCHEME = "#08AB5F"
        private const val COLOR_GRAY_FOR_HTTP_SCHEME = "#000000"
        private const val COLOR_FOR_HOST = "#333333"
        private const val COLOR_FOR_PATH_OF_HOST = "#777777"

        private const val KEY_PRODUCT_URL = "PRODUCT_URL"
        private const val KEY_SHOW_URL_BAR = "SHOW_HEADER"
        private const val KEY_REQUEST_HEADERS = "REQUEST_HEADERS"
        private const val KEY_REQUEST_CACHE_MODE = "REQUEST_CACHE_MODE"

        enum class CacheMode(private var modeValue: Int) {
            LOAD_DEFAULT(WebSettings.LOAD_DEFAULT),
            LOAD_NORMAL(WebSettings.LOAD_NORMAL),
            LOAD_CACHE_ELSE_NETWORK(WebSettings.LOAD_CACHE_ELSE_NETWORK),
            LOAD_NO_CACHE(WebSettings.LOAD_NO_CACHE),
            LOAD_CACHE_ONLY(WebSettings.LOAD_CACHE_ONLY);

            fun getModeValue(): Int {
                return modeValue
            }
        }

        class Builder(private val url: String) {
            private var showAddressBar = false
            private val headers = HashMap<String, String>()
            private var cacheMode: CacheMode? = CacheMode.LOAD_NO_CACHE

            fun showAddressBar(showAddressBar: Boolean): Builder {
                this.showAddressBar = showAddressBar
                return this
            }

            fun addHeader(key: String, value: String): Builder {
                headers[key] = value
                return this
            }


            fun setCacheMode(cacheMode: CacheMode): Builder {
                this.cacheMode = cacheMode
                return this
            }

            fun build(): BrowserFragment {
                val bundle = Bundle()
                bundle.putString(KEY_PRODUCT_URL, url)
                bundle.putBoolean(KEY_SHOW_URL_BAR, showAddressBar)
                bundle.putSerializable(KEY_REQUEST_HEADERS, headers)
                bundle.putSerializable(KEY_REQUEST_CACHE_MODE, cacheMode)
                val fragment = BrowserFragment()
                fragment.arguments = bundle
                return fragment
            }

        }
    }

}
