package com.hetum.testapp.base

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hetum.testapp.utils.DATA

abstract class BaseActivity<P : BasePresenter<BaseView>> : BaseView, AppCompatActivity() {

    protected lateinit var presenter: P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = instantiatePresenter()
        if (savedInstanceState != null) {
            init(savedInstanceState.getBoolean(DATA))
        } else {
            init(true)
        }
    }

    override fun getContext(): Context {
        return this
    }

    fun verifyAvailableNetwork(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    protected abstract fun init(isFirstLaunch: Boolean)

    protected abstract fun instantiatePresenter(): P
}