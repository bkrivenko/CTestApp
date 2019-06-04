package com.hetum.testapp.network

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface APIServices {

    @Streaming
    @GET
    fun getResults(@Url url: String): Observable<ResponseBody>

    companion object {
        fun create(): APIServices {

            val retrofit = Retrofit.Builder()
                .baseUrl("http://api.test.org/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(APIServices::class.java)
        }
    }

}