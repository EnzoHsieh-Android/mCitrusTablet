package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/*output*/
data class ChangeStatus( @SerializedName("Reservation") var reservation: Reservation):Serializable
data class Reservation(var tkey:String,@SerializedName("Status") var status:String):Serializable



/*input*/
data class UpdateStatus (val status: Int, val data: String):Serializable
