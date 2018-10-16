package ru.shadowsparky.synthetictest

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.DEBUG
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.blockingSubscribeBy
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class Main : AppCompatActivity() {
    private val mIterations = 600000
    private var mCurrentPercent = 0
    private val mComposites = CompositeDisposable()
    private val mTimer = Stopwatch()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener { startTest() }
    }

    fun startTest() {
        setLoading(true)
        setPercentUI(mCurrentPercent)
        mTimer.start()
        mComposites.add(Observable
            .range(0, mIterations)
            .subscribeOn(Schedulers.computation())
            .subscribeBy(
                onNext = { isPrime(it) },
                onComplete = {
                    mTimer.stop()
                    runOnUiThread { setLoading(false) }
                    setResultUI(mTimer.elapsedTime)
                }
            )
        )

    }

    fun setPercent(value: Int) {
        if (value / (mIterations/100) == mCurrentPercent) {
            mCurrentPercent++
            setPercentUI(mCurrentPercent)
        }
    }

    fun setPercentUI(value: Int) = runOnUiThread { percentage.text = "$value%" }

    fun setResultUI(value: Long) = runOnUiThread { result.text = "$value" }

    fun isPrime(n: Int) : Boolean {
        setPercent(n)
        for (i in 2 until n) {
            if (n % i == 0)
                return false
        }
        return true
    }

    fun setLoading(value: Boolean) {
        if (value) {
            mCurrentPercent = 0
            loading.visibility = VISIBLE
            button.isEnabled = false
            percentage.visibility = VISIBLE
            result.visibility = GONE
        } else {
            loading.visibility = GONE
            percentage.visibility = GONE
            result.visibility = VISIBLE
            button.isEnabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mComposites.dispose()
    }
}
