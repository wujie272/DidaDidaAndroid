package com.jaye.didadida

import android.app.Application
import com.jaye.didadida.data.WorkLogRepository
import com.jaye.didadida.data.WorkLogStorage

class App : Application() {

    lateinit var repository: WorkLogRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val storage = WorkLogStorage(this)
        repository = WorkLogRepository(storage)
    }
}
