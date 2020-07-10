package com.wiandro.awesomebrowser

import android.webkit.WebView

interface BrowserCallback {

    fun onOverrideUrl(view: WebView?, url: String?): Boolean

    fun onCloseBrowser()

}