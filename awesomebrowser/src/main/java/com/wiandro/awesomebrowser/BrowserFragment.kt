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
    private var requestHeaders: Map<String, String>? = null
    private lateinit var cacheModePolicy: CacheMode
    private var callback: BrowserCallback? = null
    private var mKeepScreenOn = false

    private var _binding: FragmentBrowserBinding? = null
    private val mBinding: FragmentBrowserBinding
        get() = _binding as FragmentBrowserBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null) return

        arguments?.apply {
            url = getString(KEY_PRODUCT_URL)
            showAddressBar = getBoolean(KEY_SHOW_URL_BAR)
            requestHeaders = getSerializable(KEY_REQUEST_HEADERS) as HashMap<String, String>?
            cacheModePolicy = getSerializable(KEY_REQUEST_CACHE_MODE) as CacheMode
            mKeepScreenOn = getBoolean(KEY_BROWSER_KEEP_SCREEN_ON)
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
            urlBarTextView.text = url
        }
        onLoadFinished(url)
    }

    override fun onLoadFinish(url: String) {
        mBinding.progressbar.visibility = View.GONE
        onLoadFinished(url)
    }

    override fun needBackPress() {
        activity?.onBackPressed() ?: apply {
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

        url?.let { changeURLtoSSLError(it) }

        with(mBinding) {

            progressbar.visibility = View.GONE
            sslErrorLayout.visibility = View.VISIBLE
            mBinding.urlBarTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_lock_red_24px,
                0,
                0,
                0
            )

            proceed.setOnClickListener { _ ->
                sslErrorLayout.visibility = View.GONE
                handler?.proceed()
            }

            cancel.setOnClickListener {
                sslErrorLayout.visibility = View.GONE
                handler?.cancel()
                activity?.onBackPressed()
            }
        }
    }

    fun onBackPressed() =
        if (mBinding.webview.canGoBack()) {
            mBinding.webview.goBack()
            true
        } else false


    fun setCallback(callback: BrowserCallback) {
        this.callback = callback
    }

    private fun loadUrl() {

        url?.let {
            if (requestHeaders == null) mBinding.webview.loadUrl(it)
            else mBinding.webview.loadUrl(it, requestHeaders!!)
        } ?: apply {
            // show empty View
        }
        Log.i(TAG, "onViewCreated: URL->$url")
    }

    private fun initializeWebView() {

        with(mBinding.webview) {

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            setupSetting(this)
            keepScreenOn = mKeepScreenOn
            webViewClient = WebClient(callback, this@BrowserFragment)
        }

    }

    private fun setupSetting(webView: WebView) {

        with(webView.settings) {

            lightTouchEnabled = true
            javaScriptEnabled = true //get this from builder
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
            mBinding.urlBarTextView.text = url
            return
        }

        val scheme = uri.scheme
        val host = uri.host
        val path = uri.path
        val query = uri.query

        if (scheme == null || host == null) {
            mBinding.urlBarTextView.text = url
            return
        }
        Log.i(
            TAG, "setBeautifyURL: uri={$uri}, " +
                    "scheme={$scheme}, " +
                    "host={$host}, " +
                    "path={$path}, " +
                    "queryyyy=={${uri.query}}, " +
                    "query={${uri.encodedQuery}} "
        )

        val schemeSpannable = getSchemeSpannable(scheme, isError)
        mBinding.urlBarTextView.text = schemeSpannable
        val hostSpannable = getSpannableByColor(host, COLOR_FOR_HOST)
        mBinding.urlBarTextView.append(hostSpannable)
        if (path != null) {
            val pathSpannable = getSpannableByColor(path, COLOR_FOR_PATH_OF_HOST)
            mBinding.urlBarTextView.append(pathSpannable)
        }
        if (query != null) {
            val pathSpannable = getSpannableByColor("?$query", COLOR_FOR_PATH_OF_HOST)
            mBinding.urlBarTextView.append(pathSpannable)
        }
    }


    private fun getSchemeSpannable(scheme: String, isError: Boolean): SpannableString? {
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
            mBinding.urlBarTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_lock_green_24px,
                0,
                0,
                0
            )
        } else {
            mBinding.urlBarTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_info_24px,
                0,
                0,
                0
            )
        }
    }

    private fun changeURLtoSSLError(url: String) {
        val uri = Uri.parse(url) ?: return
        setBeautifyURL(url, true)
        mBinding.urlBarTextView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_lock_red_24px,
            0,
            0,
            0
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding.webview.destroy()
        _binding = null
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
        private const val KEY_BROWSER_KEEP_SCREEN_ON = "KEEP_SCREEN_ON"


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
            private var keepScreenOn = false

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

            fun setKeepScreenOn(): Builder {
                keepScreenOn = true
                return this
            }

            fun build(): BrowserFragment {
                val bundle = Bundle().apply {
                    putString(KEY_PRODUCT_URL, url)
                    putBoolean(KEY_SHOW_URL_BAR, showAddressBar)
                    putSerializable(KEY_REQUEST_HEADERS, headers)
                    putSerializable(KEY_REQUEST_CACHE_MODE, cacheMode)
                    putBoolean(KEY_BROWSER_KEEP_SCREEN_ON, keepScreenOn)
                }
                return BrowserFragment().apply {
                    this.arguments = bundle
                }
            }

        }
    }

}
