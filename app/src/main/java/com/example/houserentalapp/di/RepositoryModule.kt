package com.example.houserentalapp.di

import com.example.houserentalapp.data.repo.SearchHistoryRepoImpl
import com.example.houserentalapp.domain.repo.SearchHistoryRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindSearchHistoryRepo(impl: SearchHistoryRepoImpl): SearchHistoryRepo
}