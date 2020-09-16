package com.wiandro.awesomebrowser

import android.net.http.SslError
import android.webkit.SslErrorHandler

/**
 * CREATED BY Javadhme
 *
 * To make an interaction between [BrowserFragment] and [WebClient]
 */
interface WebClientCallback{

    fun onLoadStart(url: String)

    fun onLoadFinish(url: String)

    fun needBackPress()

    fun onErrorHappened(errorCode: Int, description: String?, failingUrl: String?)

    fun onSslErrorHappened(handler: SslErrorHandler?, error: SslError?)
}