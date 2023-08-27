package com.droidgeeks.expensemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.droidgeeks.expensemanager.data.local.dao.TransactionDao
import com.droidgeeks.expensemanager.data.local.model.Transaction

@Database(
    entities = [Transaction::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getTransactionDao(): TransactionDao
}
