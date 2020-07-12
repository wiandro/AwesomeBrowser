package com.wiandro.awesomebrowser

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.wiandro.awesomebrowser.databinding.FragmentBrowserBinding

/**
 * CREATED BY Javadhme
 */
class BrowserFragment : Fragment() {


    private var url: String? = ""
    private var shouldDisplayUrlBar = false
    private var _binding: FragmentBrowserBinding? = null
    private val mBinding: FragmentBrowserBinding
        get() = _binding as FragmentBrowserBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null) return

        url = arguments?.getString(KEY_PRODUCT_URL)
        shouldDisplayUrlBar = arguments?.getBoolean(KEY_SHOW_URL_BAR) as Boolean

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.headerLayout.visibility = if (shouldDisplayUrlBar) View.VISIBLE else View.GONE

        initializeWebView()

        //TODO generate headers

        if (url.isNullOrEmpty()) {
            //TODO show empty View
        } else {
            mBinding.webview.loadUrl(url)
        }

        Log.i(TAG, "onViewCreated: URL->$url")
    }

    fun onBackPressed() =
        if (mBinding.webview.canGoBack()) {
            mBinding.webview.goBack()
            true
        } else
            false

    private fun initializeWebView() {

        with(mBinding.webview) {

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            settings.apply {
                lightTouchEnabled = true
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE //TODO put it in builder
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

            webViewClient = WebClient(mBinding, object : WebClientCallback {

                override fun onLoadStart(url: String, sslError: Boolean) {
                    setBeautifyURL(url, false)
                    setRelatedIcon(url)
                }

                override fun onLoadFinish(url: String, sslError: Boolean) {
                    if (sslError)
                        changeURLtoSSLError(url)
                    else
                        onLoadFinished(url)
                }

                override fun needBackPress() {
                    activity?.onBackPressed()
                }

            })

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
        if (scheme == null || host == null) {
            mBinding.urlBarTextView.text = url
            return
        }
        Log.i(TAG, "setBeautifyURL: scheme={$scheme}, host={$host}")
        val schemeSpannable = getSchemeSpannable(scheme, isError)
        mBinding.urlBarTextView.text = schemeSpannable
        val hostSpannable = getSpannableByColor(host, "#222222")
        mBinding.urlBarTextView.append(hostSpannable)
        val path = uri.path
        if (path != null) {
            val pathSpannable = getSpannableByColor(path, "#777777")
            mBinding.urlBarTextView.append(pathSpannable)
        }
    }

    private fun getSchemeSpannable(
        scheme: String,
        isError: Boolean
    ): SpannableString {
        if (isError) return getSpannableByColor("$scheme://", "#FF0000")
        return if ("https".equals(scheme, ignoreCase = true)) getSpannableByColor(
            "$scheme://", "#08AB5F"
        ) else getSpannableByColor(
            "$scheme://",
            "#000000"
        )
    }

    private fun getSpannableByColor(
        string: String,
        color: String
    ): SpannableString {
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
            mBinding.urlBarTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_green, 0, 0, 0)
        } else {
            mBinding.urlBarTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, 0, 0)
        }
    }

    private fun changeURLtoSSLError(url: String) {
        val uri = Uri.parse(url) ?: return
        setBeautifyURL(url, true)
        mBinding.urlBarTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_red, 0, 0, 0)
    }


    companion object {
        private val TAG: String = BrowserFragment::class.java.simpleName
        private const val KEY_PRODUCT_URL = "PRODUCT_URL"
        private const val KEY_SHOW_URL_BAR = "SHOW_HEADER"

        fun newInstance(url: String, showUrlBar: Boolean) =
            BrowserFragment().also {
                it.arguments = Bundle().apply {
                    putString(KEY_PRODUCT_URL, url)
                    putBoolean(KEY_SHOW_URL_BAR, showUrlBar)
                }
            }
    }

}
