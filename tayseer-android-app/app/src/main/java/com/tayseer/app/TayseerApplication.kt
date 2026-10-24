package com.tayseer.app

import android.app.Application
import com.tayseer.app.data.AppDatabase
import com.tayseer.app.data.repository.TayseerRepository

class TayseerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: TayseerRepository by lazy { TayseerRepository(database) }
}
