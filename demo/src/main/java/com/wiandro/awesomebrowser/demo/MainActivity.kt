package com.wiandro.awesomebrowser.demo

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.wiandro.awesomebrowser.BrowserFragment
import com.wiandro.awesomebrowser.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val mBinding: ActivityMainBinding
        get() = _binding!!

    private var browserFragment: BrowserFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(mBinding.root)

        browserFragment =
            BrowserFragment.newInstance("https://www.google.com/", true)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_container, browserFragment!!)
            .commitAllowingStateLoss()

    }

    override fun onBackPressed() {

        if (browserFragment != null && browserFragment!!.onBackPressed()) {
            return
        }

        super.onBackPressed()
    }

}