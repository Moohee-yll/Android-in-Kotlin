package com.example.fregmentbestpractice

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

//为了获取全局的context
class myApplication: Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}