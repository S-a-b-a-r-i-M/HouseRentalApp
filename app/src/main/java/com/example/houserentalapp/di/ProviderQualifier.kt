package com.example.houserentalapp.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkProvider