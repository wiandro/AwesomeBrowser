package com.wiandro.awesomebrowser

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.util.Log
import android.webkit.*
import androidx.annotation.RequiresApi

/**
 * CREATED BY Javadhem
 *
 */
class WebClient(
    private val browserCallback: BrowserCallback?,
    private val webClientCallback: WebClientCallback
) : WebViewClient() {

    private var isSSLError = false

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        Log.i(TAG, "onPageStarted() called with: url = [$url]")

        webClientCallback.onLoadStart(url)
        isSSLError = false

        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        Log.v(TAG, "onPageFinished() called with: url = [$url]")

        if (!isSSLError)
            webClientCallback.onLoadFinish(url)

        super.onPageFinished(view, url)
    }

    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        Log.v(
            TAG,
            "onReceivedClientCertRequest() called with: view = [$view], request = [$request]"
        )
        super.onReceivedClientCertRequest(view, request)
    }

    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        Log.e(
            TAG,
            "onReceivedError() called with: errorCode = [" + errorCode + "], " +
                    "description = [" + description + "], " +
                    "failingUrl = [" + failingUrl + "]"
        )
        val isFailed = internalOnReceivedError(errorCode, description, failingUrl)
        if (!isFailed) super.onReceivedError(view, errorCode, description, failingUrl)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        Log.e(
            TAG, "onReceivedError() 2 called with: view = [" + view + "], " +
                    "request url= [" + request.url + "], " +
                    "error DESC= [" + error.description + "]," +
                    "error code=[" + error.errorCode + " ]"
        )
        val isFailed = internalOnReceivedError(
            error.errorCode,
            error.description.toString(),
            request.url.toString()
        )
        if (!isFailed) super.onReceivedError(view, request, error)
    }

    override fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: SslError
    ) {
        Log.e(
            TAG,
            "onReceivedSslError() called with: view = [$view], handler = [$handler], error = [$error]"
        )
        isSSLError = true

        webClientCallback.onSslErrorHappened(handler, error)
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        Log.i(
            TAG,
            "onPageCommitVisible() called with: url = [$url]"
        )
        super.onPageCommitVisible(view, url)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        Log.e(
            TAG, "onReceivedHttpError() called with: " +
                    "request = [" + request.url + "], " +
                    "method=[" + request.method + "], " +
                    "statuc code = [" + errorResponse.statusCode + "], " +
                    "reason=[" + errorResponse.reasonPhrase + "]"
        )
        super.onReceivedHttpError(view, request, errorResponse)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView,
        handler: HttpAuthHandler,
        host: String,
        realm: String
    ) {
        Log.i(
            TAG,
            "onReceivedHttpAuthRequest() called with: handler = [$handler], host = [$host], realm = [$realm]"
        )
        super.onReceivedHttpAuthRequest(view, handler, host, realm)
    }

    override fun onRenderProcessGone(
        view: WebView,
        detail: RenderProcessGoneDetail
    ): Boolean {
        Log.d(
            TAG,
            "onRenderProcessGone() called with: detail = [$detail]"
        )
        return super.onRenderProcessGone(view, detail)
    }

    override fun onLoadResource(view: WebView, url: String) {
        Log.v(
            TAG,
            "onLoadResource() called with: url = [$url]"
        )
        super.onLoadResource(view, url)
    }

    override fun onTooManyRedirects(
        view: WebView,
        cancelMsg: Message,
        continueMsg: Message
    ) {
        Log.i(
            TAG,
            "onTooManyRedirects() called with: view = cancelMsg = [$cancelMsg], continueMsg = [$continueMsg]"
        )
        super.onTooManyRedirects(view, cancelMsg, continueMsg)
    }

    override fun onSafeBrowsingHit(
        view: WebView,
        request: WebResourceRequest,
        threatType: Int,
        callback: SafeBrowsingResponse
    ) {
        Log.i(
            TAG,
            "onSafeBrowsingHit() called with: request = [$request], threatType = [$threatType], callback = [$callback]"
        )
        super.onSafeBrowsingHit(view, request, threatType, callback)
    }

    override fun doUpdateVisitedHistory(
        view: WebView,
        url: String,
        isReload: Boolean
    ) {
        Log.i(
            TAG,
            "doUpdateVisitedHistory() called with: view = [$view], url = [$url], isReload = [$isReload]"
        )
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView,
        url: String
    ): Boolean {
        Log.v(
            TAG,
            "shouldOverrideUrlLoading() 1 called with: view = [$view], url = [$url]"
        )
        val isSuccess = overrideUrlLoad(view, url)
        return if (!isSuccess) {
            super.shouldOverrideUrlLoading(view, url)
        } else true
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {
        Log.v(
            TAG, "shouldOverrideUrlLoading() 2 called with: " +
                    "request = [" + request.url + "], " +
                    "method=[" + request.method + "], " +
                    "is redirect=[" + request.isRedirect + "], "
        )
        val url = request.url.toString()
        val isSuccess = overrideUrlLoad(view, url)
        return if (!isSuccess) super.shouldOverrideUrlLoading(view, request) else true
    }

    private fun internalOnReceivedError(
        errorCode: Int,
        description: String,
        failingUrl: String
    ): Boolean {
        Log.e(
            TAG, "internalOnReceivedError() called with: " +
                    "errorCode = [" + errorCode + "], " +
                    "description = [" + description + "], " +
                    "failingUrl = [" + failingUrl + "]"
        )
        if (errorCode == -2 || failingUrl.contains("google")
            || failingUrl.endsWith(".js")
            || failingUrl.endsWith(".css")
        ) {
            //sometimes falling in error when trying to request to
            // https://www.googletagmanager.com/gtag/js...
            // or
            // https://www.google-analytics.com/analytics.js
            // so there is no need to raise an error and disappear the webView
            return true
        }

        webClientCallback.onErrorHappened(errorCode, description, failingUrl)
        return false
    }

    private fun overrideUrlLoad(view: WebView, url: String): Boolean {
        Log.d(TAG, "overrideUrlLoad() called with: view = [$view], url = [$url]")

        return if (browserCallback != null && browserCallback.onOverrideUrl(view, url))
            browserCallback.onOverrideUrl(view, url)
        else false
    }


    companion object {

        private val TAG = WebClient::class.java.simpleName
    }

}

