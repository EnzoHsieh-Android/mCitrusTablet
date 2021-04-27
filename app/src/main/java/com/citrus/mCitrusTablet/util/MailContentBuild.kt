package com.citrus.mCitrusTablet.util

import android.content.Context
import com.citrus.mCitrusTablet.R



open class MailContentBuild(context: Context) {

    var mailContext = context

    fun genericMsg(
        storePic: String,
        storeName: String,
        guestName: String,
        day: String,
        date: String,
        adult: String,
        child: String,
        memo: String,
        time: String
    ): String {

        var mailBody =
            "<'div style='text-align:center;background:#f1f1f1;padding:35px;'><'div style='display: inline-block;min-width:500px;max-width:500px;font-family:微軟正黑體;box-shadow:rgba(0, 0 ,0 ,0.26);'>" +
                    "<'div style='text-align:center;background:#fefefe;padding:25px;padding-top:15px;'><'img style='width:150px;margin-top:20px;' src='" + storePic + "'> <'/div>" +
                    "<'div style='background:#fefefe;text-align:center;border-radius:3px;'>" +
                    "<'div style='font-size:19px;font-weight:bold;'>" + storeName + "<'/div>" +
                    "<'div style='color: #395D73;font-size:14px;padding:15px;'>" + getString(R.string.ThankYouForRes) + " ," + guestName + "<'br>" + getString(R.string.AlreadyRes) + "<'/div>" +
                    "<'/div>"

        mailBody += "<'div style='background:#fefefe;text-align:center;margin-top:5px;border-radius:3px;'>" +
                "<'div style='padding-top:15px;font-weight:bold;color:#395D73;font-size:17px;'>" + getString(R.string.AnoteForRes) + "<'/div>" +
                "<'div style='padding:5px;color:#395D73;font-size:14px;'>" +
                "<'span style='color: #60a4bf;font-weight:bold;'>" + day + "<'/span>," + date + "<'br/> " + " " + getString(R.string.adult) + " " + adult + ", " + getString(R.string.child) + " " + child + " " + getString(R.string.gustertip) + " " + getString(R.string.at) + "<'br/> <'span style='color:#60A4BF;font-size:52px;font-weight:bold;margin-bottom:20px;display:inline-block;'> " + time + "<'/span><'/div>"

        if (memo != "") {
            mailBody += "<'div style='padding:15px;padding-top:0;color:#a19f9f;font-size:14px;'>$memo<'/div>"
        }

        mailBody += "<'/div>" +
                "<'div style='margin-top:25px;font-size:15px;'>Power by  <'span style='color:#df9100;font-weight:bold;font-size:21px;margin:0 7px;letter-spacing:-1px;'>CATCH<'/span> © CiTRUS<'/div>" +
                "<'/div><'/div>"


        return mailBody
    }


    fun getString(id:Int):String{
        return mailContext.resources.getString(id)
    }
}