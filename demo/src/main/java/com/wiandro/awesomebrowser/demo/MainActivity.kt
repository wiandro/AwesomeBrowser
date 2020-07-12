package com.wiandro.awesomebrowser.demo

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.wiandro.awesomebrowser.BrowserFragment
import com.wiandro.awesomebrowser.demo.databinding.ActivityMainBinding
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val mBinding: ActivityMainBinding
        get() = _binding!!

    private var browserFragment: BrowserFragment? = null

    override fun onResume() {
        super.onResume()
        setupBrowserCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(mBinding.root)

        navigateToBrowserFragment()
    }

    override fun onBackPressed() {

        if (browserFragment != null && browserFragment!!.onBackPressed()) {
            return
        }

        super.onBackPressed()
    }

    private fun setupBrowserCallback() {
        browserFragment?.setCallback(BrowserCallback(WeakReference(this)))
    }

    private fun navigateToBrowserFragment(){
        browserFragment =
            BrowserFragment.Factory.Builder("https://www.google.com/")
                .showAddressBar(true)
                .setCacheMode(BrowserFragment.Factory.CacheMode.LOAD_DEFAULT)
                //.addHeader("token" , "1233223")
                //.addHeader("language" , "en")
                .build()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_container, browserFragment!!)
            .commitAllowingStateLoss()

        setupBrowserCallback()
    }
}