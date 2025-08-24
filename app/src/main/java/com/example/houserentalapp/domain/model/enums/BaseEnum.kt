package com.example.houserentalapp.domain.model.enums


interface ReadableEnum {
    val readable: String

    // Nested Interface with Generic Constraints
    interface Companion<T> where T : Enum<T>, T : ReadableEnum {
        val values: Array<T>

        fun fromString(inputStr: String): T? = values.find {
            it.readable.contentEquals(inputStr, true) ||
            it.name.contentEquals(inputStr, true)
        }

        fun isValid(inputStr: String): Boolean = values.any {
            it.readable.contentEquals(inputStr, true) ||
            it.name.contentEquals(inputStr, true)
        }
    }
}
