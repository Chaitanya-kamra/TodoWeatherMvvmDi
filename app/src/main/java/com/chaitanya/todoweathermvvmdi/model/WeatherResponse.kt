package com.chaitanya.todoweathermvvmdi.model

import com.chaitanya.todoweathermvvmdi.model.Current
import com.chaitanya.todoweathermvvmdi.model.Location

data class WeatherResponse(
    val current: Current,
    val location: Location
)