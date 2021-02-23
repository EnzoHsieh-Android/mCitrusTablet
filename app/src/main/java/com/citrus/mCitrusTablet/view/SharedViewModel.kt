package com.citrus.mCitrusTablet.view


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.citrus.mCitrusTablet.util.SingleLiveEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*

class SharedViewModel @ViewModelInject constructor(@ApplicationContext context: Context): ViewModel() {

    private val _tvClock: TimeReceiveLiveData = TimeReceiveLiveData(context)
    val tvClock: LiveData<String>
        get() = _tvClock

    private val _changePageTrigger = SingleLiveEvent<Boolean>()
    val changePageTrigger: SingleLiveEvent<Boolean>
        get() = _changePageTrigger


    fun toWaitFragment(){
        _changePageTrigger.value = true
    }

    fun toReservationFragment(){
        _changePageTrigger.value = true
    }

    fun toSettingFragment(){
        _changePageTrigger.value = true
    }

}



class TimeReceiveLiveData(val context: Context) : MutableLiveData<String>() {
    val sdf = SimpleDateFormat("HH:mm")

    companion object {
        private lateinit var instance: TimeReceiveLiveData
        fun get(context: Context): TimeReceiveLiveData {
            instance = if (::instance.isInitialized){
                instance
            }else{
                TimeReceiveLiveData(context)
            }
            return instance
        }
    }

    private val timeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action: String = p1?.action!!
            if (action == Intent.ACTION_TIME_TICK) {
                value = sdf.format(Date())
            }
        }
    }

    override fun onActive() {
        super.onActive()
        value =  sdf.format(Date())
        val broadcastIntent = IntentFilter()
        broadcastIntent.addAction(Intent.ACTION_TIME_TICK)
        context.registerReceiver(timeBroadcastReceiver, broadcastIntent)
    }

    override fun onInactive() {
        super.onInactive()
        context.unregisterReceiver(timeBroadcastReceiver)
    }


}
