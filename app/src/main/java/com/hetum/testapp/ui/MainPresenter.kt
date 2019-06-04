package com.hetum.testapp.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.hetum.testapp.base.BasePresenter
import com.hetum.testapp.dp.AppDatabase
import com.hetum.testapp.dp.ResultDao
import com.hetum.testapp.model.Result
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class MainPresenter(mainView: MainView) : BasePresenter<MainView>(mainView) {

    private var db: AppDatabase? = null
    private lateinit var resultDao: ResultDao
    private val clipboard = mainView.getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private var disposable: Disposable? = null

    override fun onViewCreated(isFirstLaunch: Boolean) {
        disposable = CompositeDisposable()

        db = AppDatabase.getAppDataBase(context = view.getContext())
        resultDao = db!!.resultDao()

        if (isFirstLaunch) {
            clearAll()
        } else {
            updateResults()
        }
    }

    override fun onViewDestroyed() {
    }

    fun saveLinesToBuffer() {
        resultDao.getCheckedResult(true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<List<String>>() {
                override fun onSuccess(results: List<String>) {
                    if (results.isNotEmpty()) {
                        val clip = ClipData.newPlainText("line", results.joinToString("\n"))
                        clipboard.primaryClip = clip
                        view.showToast(view.getContext().getString(com.hetum.testapp.R.string.save_data))
                    } else {
                        view.showToast(view.getContext().getString(com.hetum.testapp.R.string.empty))
                    }
                }

                override fun onError(e: Throwable) {
                    view.showToast(view.getContext().getString(com.hetum.testapp.R.string.empty))
                }
            })
    }

    @SuppressLint("CheckResult")
    fun updateResult(result: Result) {
        Completable.fromAction {
            resultDao.updateResult(result)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
            }
    }

    @SuppressLint("CheckResult")
    fun updateResults() {
        resultDao.getResults()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { results -> view.updateResults(results) }
    }

    @SuppressLint("CheckResult")
    private fun clearAll() {
        Observable.fromCallable { resultDao.clearTable() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                updateResults()
            }
    }
}