package com.droidgeeks.expensemanager.di

import android.content.Context
import androidx.room.Room
import com.droidgeeks.expensemanager.data.local.AppDatabase
import com.droidgeeks.expensemanager.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DBModule {

    @Singleton
    @Provides
    fun provideNoteDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "expense_transaction.db")
            .fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideCacheDAO(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.getTransactionDao()
    }

}
