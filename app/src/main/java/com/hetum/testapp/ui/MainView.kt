package com.hetum.testapp.ui

import com.hetum.testapp.base.BaseView
import com.hetum.testapp.model.Result

interface MainView : BaseView {

    fun updateResults(results: List<Result>)

    fun showToast(error: String)

    fun showLoading()

    fun hideLoading()

    fun hideKeyboard()
}