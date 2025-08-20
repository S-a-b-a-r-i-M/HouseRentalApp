package com.example.houserentalapp.domain.utils

import androidx.fragment.app.Fragment
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.domain.usecase.CreatePropertyUseCase
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Fragment.createPropertyViewModelFactory(): CreatePropertyViewModelFactory {
    val useCase = CreatePropertyUseCase(PropertyRepoImpl(requireActivity()))
    return CreatePropertyViewModelFactory(useCase)
}

fun String.toEpoch(format: String = "dd/MM/yyyy"): Long {
    val formatter = DateTimeFormatter.ofPattern(format)
    val localDate = LocalDate.parse(this, formatter)
    return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
//    return localDate.toEpochSecond(ZoneId.systemDefault())
}

fun formatEpochTime(epochTime: Long) {
    val instant = Instant.ofEpochSecond(epochTime)
    val format1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val format2 = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    println("ZoneId.systemDefault(): ${ZoneId.systemDefault()}")
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    println(zonedDateTime.format(format1))
    println(zonedDateTime.format(format2))
}