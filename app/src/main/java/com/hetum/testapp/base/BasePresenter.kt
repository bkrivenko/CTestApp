package com.hetum.testapp.base

import com.hetum.testapp.network.APIServices

abstract class BasePresenter<out V : BaseView>(protected val view: V) {

    internal val apiService by lazy {
        APIServices.create()
    }

    abstract fun onViewCreated(isFirstLaunch: Boolean)

    abstract fun onViewDestroyed()
}