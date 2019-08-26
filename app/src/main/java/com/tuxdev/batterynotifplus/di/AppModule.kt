package com.tuxdev.batterynotifplus.di

import com.tuxdev.batterynotifplus.utils.PrefManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

/**
 **********************************************
 * Created by ukie on 11/17/18 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 **********************************************
 * © 2018 | All Right Reserved
 */

val viewModelModule = module {
}

val globalModule = module {
    single { PrefManager(androidContext()) }
}

val batteryNotifApp = listOf(viewModelModule, globalModule)