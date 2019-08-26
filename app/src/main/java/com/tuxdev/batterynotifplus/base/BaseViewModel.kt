package com.tuxdev.batterynotifplus.base

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * *********************************************
 * Created by ukie on 9/27/18 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 * *********************************************
 * © 2018 | All Right Reserved
 */

abstract class BaseViewModel : ViewModel() {
    private val composite = CompositeDisposable()

    fun composite(job: () -> Disposable) {
        composite.add(job())
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        composite.clear()
    }
}
