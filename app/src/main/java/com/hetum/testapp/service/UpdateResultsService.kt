package com.hetum.testapp.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.hetum.testapp.R
import com.hetum.testapp.dp.AppDatabase
import com.hetum.testapp.dp.ResultDao
import com.hetum.testapp.model.Result
import com.hetum.testapp.network.APIServices
import com.hetum.testapp.utils.FILTER
import com.hetum.testapp.utils.URL
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.*
import java.util.regex.Pattern

class UpdateResultsService : Service() {

    private var db: AppDatabase? = null
    private lateinit var resultDao: ResultDao
    private var disposable: Disposable? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        disposable = CompositeDisposable()

        db = AppDatabase.getAppDataBase(context = this)
        resultDao = db!!.resultDao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!!.extras != null) {
            val url = intent.extras!!.getString(URL)!!
            val filter = intent.extras!!.getString(FILTER)!!

            clearTable(url, filter)
        }
        return START_NOT_STICKY
    }


    @SuppressLint("CheckResult")
    private fun clearTable(url: String, regexpVal: String) {
        Observable.fromCallable { resultDao.clearTable() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                loadData(url, regexpVal)
            }
    }

    private fun loadData(url: String, regexpVal: String) {
        disposable = APIServices.create()
            .getResults(url)
            .observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { response -> fillList(response, regexpVal) },
                { error -> showUrlError(error.toString()) }
            )
    }

    @SuppressLint("CheckResult")
    private fun addResults(results: List<Result>) {
        Single.fromCallable { resultDao.insertResult(results) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    private fun fillList(response: ResponseBody, regexpVal: String) {
        val results = mutableListOf<Result>()
        val input = BufferedReader(InputStreamReader(response.byteStream()))
        var line: String? = null
        var filter = regexpVal
        if (regexpVal.isNotEmpty()) {
            filter = filter.replace("*", ".*").replace("?", ".?")
        }
        val pattern = Pattern.compile(filter)

        while ({ line = input.readLine(); line }() != null) {
            if (filter.isEmpty() || pattern.matcher(line).matches()) {
                results.add(Result(text = line!!))
            }
            if (results.size == 100) {
                val newList = results.toList()
                addResults(newList)
                results.clear()
            }
        }
        input.close()
        addResults(results)
        saveResultsToFile()
    }

    private fun showUrlError(error: String) {
        Log.e("API", error)
        showToast(error)
        stopSelf()
    }

    private fun saveResultsToFile() {
        resultDao.getResultText()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<List<String>>() {
                override fun onSuccess(results: List<String>) {
                    if (results.isNotEmpty()) {
                        savingFile(results)
                    }
                    stopSelf()
                }

                override fun onError(e: Throwable) {
                    showToast(R.string.save_error)
                    stopSelf()
                }
            })
    }

    private fun savingFile(results: List<String>) {
        val root = filesDir.absolutePath
        val myDir = File(root)
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val file = File(root, "results.log")
        if (!file.exists()) {
            file.createNewFile()
        }
        try {
            val out = FileOutputStream(file)
            val os = ObjectOutputStream(out)
            os.writeObject(results)
            os.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showToast(resId: Int) {
        Handler(mainLooper).post {
            Toast.makeText(applicationContext, resId, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(error: String) {
        Handler(mainLooper).post {
            Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
        }
    }
}