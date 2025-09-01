package com.example.houserentalapp.presentation.utils.helpers

import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.AmenityDomain
import com.example.houserentalapp.domain.model.enums.AmenityBaseEnum
import com.example.houserentalapp.domain.model.enums.CountableInternalAmenity
import com.example.houserentalapp.domain.model.enums.InternalAmenity
import com.example.houserentalapp.domain.model.enums.SocialAmenity

val amenityDrawables = mapOf<AmenityBaseEnum, Int>(
    // SocialAmenity
    SocialAmenity.POWER_BACKUP to R.drawable.outline_power_24,
    SocialAmenity.SWIMMING_POOL to R.drawable.outline_pool_24,
    SocialAmenity.GYM to R.drawable.outline_fitness_center_24,
    SocialAmenity.LIFT to R.drawable.outline_elevator_24,
    SocialAmenity.PLAY_AREA to R.drawable.outline_sports_volleyball_24,
    SocialAmenity.GATED_COMMUNITY to  R.drawable.outline_security_24,
    SocialAmenity.REGULAR_WATER_SUPPLY to R.drawable.outline_water_drop_24,
    // InternalAmenity
    InternalAmenity.WIFI to R.drawable.outline_android_wifi_3_bar_24,
    InternalAmenity.SOFA to R.drawable.outline_chair_24,
    InternalAmenity.FRIDGE to R.drawable.outline_fridge,
    InternalAmenity.CHIMNEY to R.drawable.baseline_cabin_24,
    InternalAmenity.MICROWAVE to R.drawable.outline_microwave_24,
    InternalAmenity.WASHING_MACHINE to R.drawable.baseline_washing,
    InternalAmenity.WATER_PURIFIER to R.drawable.outline_local_drink_24,
    InternalAmenity.WATER_HEATER to R.drawable.outline_device_thermostat_24,
    // CountableInternalAmenity
    CountableInternalAmenity.AC to R.drawable.outline_ac_unit_24,
    CountableInternalAmenity.TV to R.drawable.outline_live_tv_24,
    CountableInternalAmenity.BED to R.drawable.outline_bed_24,
    CountableInternalAmenity.FAN to R.drawable.outline_mode_fan_24,
    CountableInternalAmenity.LIGHT to R.drawable.outline_lightbulb_24,
)

fun getAmenityDrawable(amenity: AmenityDomain, defaultDrawable: Int = R.drawable.outline_chair_24) : Int {
    return amenityDrawables[amenity.name] ?: defaultDrawable
}