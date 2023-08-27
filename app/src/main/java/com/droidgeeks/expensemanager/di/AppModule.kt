package com.droidgeeks.expensemanager.di

import android.content.Context
import com.droidgeeks.expensemanager.app.ExpenseManager
import com.droidgeeks.expensemanager.data.local.datastore.UIModeDataStore
import com.droidgeeks.expensemanager.data.local.datastore.UIModeImpl
import com.droidgeeks.expensemanager.services.exportcsv.ExportCsvService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): ExpenseManager {
        return app as ExpenseManager
    }

    @Singleton
    @Provides
    fun providePreferenceManager(@ApplicationContext context: Context): UIModeImpl {
        return UIModeDataStore(context)
    }

    @Singleton
    @Provides
    fun provideExportCSV(@ApplicationContext context: Context): ExportCsvService {
        return ExportCsvService(appContext = context)
    }
}
