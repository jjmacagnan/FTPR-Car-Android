package com.example.myapitest.utils

object ValidationUtils {
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("\\+[1-9]\\d{1,14}"))
    }

    fun isValidYear(year: String): Boolean {
        return try {
            val yearInt = year.toInt()
            yearInt in 1900..2025
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun isValidPrice(price: String): Boolean {
        return try {
            val priceDouble = price.toDouble()
            priceDouble > 0
        } catch (e: NumberFormatException) {
            false
        }
    }
}