package com.wiandro.awesomebrowser

/**
 * CREATED BY Javadhme
 */
interface WebClientCallback{

    fun onLoadStart(url: String, sslError: Boolean)

    fun onLoadFinish(url: String, sslError: Boolean)

    fun needBackPress()

}