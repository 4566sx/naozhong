package com.biblealarm.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 圣经闹钟应用程序主类
 * 使用Hilt进行依赖注入管理
 */
@HiltAndroidApp
class BibleAlarmApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化应用程序组件
        initializeComponents()
    }
    
    private fun initializeComponents() {
        // 这里可以添加应用启动时需要初始化的组件
        // 例如：日志系统、崩溃报告、性能监控等
    }
}