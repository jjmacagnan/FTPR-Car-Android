package com.example.myapitest.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Car(
    val id: String? = null,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
    val price: Double,
    val imageUrl: String,
    val location: Location
) : Parcelable