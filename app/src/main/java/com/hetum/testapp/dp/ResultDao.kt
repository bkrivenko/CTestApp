package com.hetum.testapp.dp

import android.arch.persistence.room.*
import com.hetum.testapp.model.Result
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ResultDao {
//    @Insert
//    fun insertResult(result: Result)

    @Insert
    fun insertResult(results: List<Result>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateResult(result: Result)

    @Query("SELECT text FROM result WHERE isChecked == :isChecked")
    fun getCheckedResult(isChecked: Boolean): Single<List<String>>

    @Query("SELECT text FROM result")
    fun getResultText(): Single<List<String>>

    @Query("SELECT * FROM result")
    fun getResults(): Flowable<List<Result>>

    @Query("DELETE FROM result")
    fun clearTable()
}