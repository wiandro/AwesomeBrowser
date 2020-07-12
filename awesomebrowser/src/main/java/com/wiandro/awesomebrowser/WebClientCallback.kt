package com.wiandro.awesomebrowser

/**
 * CREATED BY Javadhme
 *
 * To make an interaction between [BrowserFragment] and [WebClient]
 */
interface WebClientCallback{

    fun onLoadStart(url: String, sslError: Boolean)

    fun onLoadFinish(url: String, sslError: Boolean)

    fun needBackPress()

}