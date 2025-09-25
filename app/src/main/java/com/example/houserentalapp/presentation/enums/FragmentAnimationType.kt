package com.example.houserentalapp.presentation.enums

import androidx.annotation.AnimRes
import com.example.houserentalapp.R

enum class FragmentAnimationType(
    @AnimRes val enter: Int,
    @AnimRes val exit: Int,
    @AnimRes val popEnter: Int,
    @AnimRes val popExit: Int,
) {
    SLIDE_HORIZONTAL(
        R.anim.slide_in_right,
        R.anim.slide_out_left,
        R.anim.slide_in_left,
        R.anim.slide_out_right
    ),
    SLIDE_VERTICAL(
        R.anim.slide_up,
        R.anim.fade_out,
        R.anim.fade_in,
        R.anim.slide_down
    ),
    FADE(
        R.anim.fade_in,
        R.anim.fade_out,
        R.anim.fade_in,
        R.anim.fade_out
    ),
    BOOM(
        R.anim.boom_enter,
        R.anim.boom_exit,
        R.anim.boom_enter,
        R.anim.boom_exit
    )
}