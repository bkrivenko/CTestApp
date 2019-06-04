package com.hetum.testapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.hetum.testapp.R
import com.hetum.testapp.base.BaseActivity
import com.hetum.testapp.databinding.ActivityMainBinding
import com.hetum.testapp.model.Result
import com.hetum.testapp.service.UpdateResultsService
import com.hetum.testapp.utils.DATA
import com.hetum.testapp.utils.FILTER
import com.hetum.testapp.utils.REQUEST_EXTERNAL_STORAGE
import com.hetum.testapp.utils.URL
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity<MainPresenter>(), MainView {

    private val serviceClass = UpdateResultsService::class.java

    private val PERMISSIONS_STORAGE =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var binding: ActivityMainBinding
    private val resultAdapter = ResultAdapter(object : OnItemClickListener {
        override fun onItemClick(result: Result) {
            presenter.updateResult(result)
        }
    })

    override fun init(isFirstLaunch: Boolean) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        rv_results.adapter = resultAdapter
        rv_results.layoutManager = LinearLayoutManager(this)
        rv_results.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        presenter.onViewCreated(isFirstLaunch)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(DATA, false)
    }


    override fun instantiatePresenter(): MainPresenter {
        return MainPresenter(this)
    }

    override fun updateResults(results: List<Result>) {
        hideLoading()
        resultAdapter.updateData(results)
    }

    override fun showToast(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    override fun showLoading() {
        binding.progressVisible = View.VISIBLE
        binding.loadEnabled = false
    }

    override fun hideLoading() {
        binding.progressVisible = View.GONE
        binding.loadEnabled = true
    }

    override fun hideKeyboard() {
        this.currentFocus?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        loadResults()
                    } else {
                        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    fun searchOnClick(v: View) {
        if (verifyStoragePermissions()) {
            loadResults()
        }
    }

    fun saveOnClick(v: View) {
        presenter.saveLinesToBuffer()
    }

    private fun loadResults() {
        hideKeyboard()
        if (verifyAvailableNetwork()) {
            startService()
        } else {
            showToast(getString(com.hetum.testapp.R.string.no_internet_connection))
        }
    }

    private fun startService() {
        if (!isServiceRunning(serviceClass)) {
            showLoading()
            val startMyService = Intent()
            startMyService.setClass(this, serviceClass)
            startMyService.putExtra(URL, et_url.text.toString())
            startMyService.putExtra(FILTER, et_filter.text.toString())
            startService(startMyService)
        } else {
            showToast(getString(R.string.wait))
        }
    }

    private fun verifyStoragePermissions(): Boolean {
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            return false
        }
        return true
    }

    @SuppressLint("ServiceCast")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
