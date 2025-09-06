package com.example.myapitest.utils

import android.content.Context
import android.widget.EditText
import android.widget.Toast

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun EditText.validateNotEmpty(errorMessage: String): Boolean {
    return if (text.toString().trim().isEmpty()) {
        error = errorMessage
        false
    } else {
        error = null
        true
    }
}

fun Double.formatCurrency(): String {
    return "R$ ${String.format("%.2f", this)}"
}