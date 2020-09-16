package com.wiandro.awesomebrowser.demo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import androidx.core.content.ContextCompat.startActivity
import com.wiandro.awesomebrowser.BrowserCallback
import java.lang.ref.WeakReference


/*
* Created by Esi on 7/11/20. 
* For Awesome Browser
*/
class BrowserCallback(private val weakActivity: WeakReference<Activity>) : BrowserCallback {

    override fun onOverrideUrl(view: WebView, url: String): Boolean {

        return if (url.startsWith("http://play.google") || url.startsWith("https://paly.google")) {
            val uri = Uri.parse(url)
            val lastPath = uri.lastPathSegment

                //Do any thing that you want now :)
            weakActivity.get()?.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/")
                )
            )
            true
        } else false
    }

    override fun onCloseBrowser() {
        Log.d(TAG, "closeBrowser: called")

        weakActivity.get()?.finish()?:apply {
            Log.w(TAG, "closeBrowser: weakReference of activity is NULL")
        }
    }

    companion object{
        private const val TAG = "BrowserCallback"
    }
}
