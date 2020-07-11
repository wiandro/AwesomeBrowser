package com.wiandro.awesomebrowser

import android.net.http.SslError
import android.webkit.SslErrorHandler

/**
 * CREATED BY Javadhme
 */
interface WebClientCallback{

    fun onLoadStart(url: String)

    fun onLoadFinish(url: String)

    fun needBackPress()

    fun onErrorHappened(errorCode: Int, description: String?, failingUrl: String?)

    fun onSslErrorHappened(handler: SslErrorHandler?, error: SslError?)
}