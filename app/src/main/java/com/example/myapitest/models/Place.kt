package com.example.myapitest.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Place(
    val lat: Double,
    val long: Double
) : Parcelable
