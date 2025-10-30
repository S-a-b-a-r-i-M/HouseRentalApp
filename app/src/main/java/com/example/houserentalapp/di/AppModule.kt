package com.example.houserentalapp.di

import android.content.Context
import com.example.houserentalapp.data.local.db.DatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideLocalDataBase(@ApplicationContext appContext: Context) = DatabaseHelper.getInstance(
        appContext
    )
}