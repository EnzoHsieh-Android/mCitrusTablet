package com.citrus.mCitrusTablet.view.dialog


import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import com.citrus.mCitrusTablet.R
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.citrus.mCitrusTablet.util.Constants.dateFormatSql
import com.citrus.mCitrusTablet.util.exhaustive
import com.citrus.mCitrusTablet.util.onSafeClick
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import com.citrus.mCitrusTablet.util.ui.TimePickerFragment
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.savvi.rangedatepicker.CalendarPickerView
import com.savvi.rangedatepicker.CalendarPickerView.SelectionMode.RANGE
import com.savvi.rangedatepicker.CalendarPickerView.SelectionMode.SINGLE
import com.wdullaer.materialdatetimepicker.time.Timepoint
import kotlinx.android.synthetic.main.dailog_date_picker.*
import kotlinx.android.synthetic.main.dialog_search_table.*
import org.jetbrains.anko.support.v4.toast
import java.text.ParseException
import java.util.*

sealed class CalendarType{
    object NoTimePickerForSearchReservation : CalendarType()
    object OneTimePickerForReservation : CalendarType()
    object NeedHistoryForDate : CalendarType()
}


class CustomDatePickerDialog(
    context: Context,
    var calendarType: CalendarType,
    var mode: CalendarPickerView.SelectionMode,
    private var startTime: String,
    private var endTime: String,
    private val listener: (seat: String,startTime: String, endTime: String, selectionMode: CalendarPickerView.SelectionMode,String,String) -> Unit
) : BaseDialogFragment() {

    var count:String = "1"
    var adultCount:String ="0"
    var childCount:String ="0"

    override fun getLayoutId(): Int {
        return R.layout.dailog_date_picker
    }

    override fun initView() {
        isCancelable = false
        setWindowWidthPercent()

        when(calendarType){
            is CalendarType.NoTimePickerForSearchReservation ->  {
                tvStartTimeTitle.visibility = View.GONE
                tvEndTimeTitle.visibility = View.GONE
                tvStartTime.visibility = View.GONE
                tvEndTime.visibility = View.GONE
                rgDateMode.visibility = View.GONE
            }
            is CalendarType.OneTimePickerForReservation -> {
                tvStartTimeTitle.visibility = View.VISIBLE
                tvEndTimeTitle.visibility = View.GONE
                tvStartTime.visibility = View.VISIBLE
                tvEndTime.visibility = View.GONE
                rgDateMode.visibility = View.GONE
                tv_seatCountTitle.visibility = View.GONE
                number_picker.visibility = View.GONE
            }
            is CalendarType.NeedHistoryForDate -> {
                tvStartTimeTitle.visibility = View.GONE
                tvEndTimeTitle.visibility = View.GONE
                tvStartTime.visibility = View.GONE
                tvEndTime.visibility = View.GONE
                rgDateMode.visibility = View.GONE
            }
        }.exhaustive

        val calendar = Calendar.getInstance()
        var minDate: Date
        minDate = if(calendarType != CalendarType.NeedHistoryForDate) {
            calendar.time
        }else{
            calendar.add(Calendar.DAY_OF_MONTH, -90)
            calendar.time
        }
            minDate = calendar.time

        val calendarMax = Calendar.getInstance()
        calendarMax.add(Calendar.DAY_OF_MONTH, 90)
            val maxDate = calendarMax.time
            calendarView.init(minDate, maxDate).inMode(mode)


        try {
            val date: Date? = if(dateFormatSql.parse(startTime) < minDate) minDate else dateFormatSql.parse(startTime)

            calendarView.selectDate(date, true)

        } catch (e: ParseException) {
            e.printStackTrace()
        }


        when (mode) {
            SINGLE -> rgDateMode.check(R.id.rbSingle)
            RANGE -> rgDateMode.check(R.id.rbRange)
        }

//        number_picker.setOnValueChangedListener { _, _, newVal ->
//            count = newVal.toString()
//        }

        rgDateMode.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            mode = SINGLE
            when (checkedId) {
                R.id.rbSingle -> mode = SINGLE
                R.id.rbRange -> mode = RANGE
            }
            calendarView.init(minDate, maxDate).inMode(mode)
        }

        tvStartTime.onSafeClick {
            showTimePickerDialog(it as TextView)
        }

        tvEndTime.onSafeClick {
            showTimePickerDialog(it as TextView)
        }


        tvPerson.onSafeClick {
            showNumberPickerDialog(it as TextView)
        }


        btnOK.onSafeClick {
            if (calendarView.selectedDates.size > 0) {
                if (calendarType == CalendarType.OneTimePickerForReservation && (tvStartTime.text == getString(R.string.time))) {
                    if (tvStartTime.text == getString(R.string.time)) {
                        YoYo.with(Techniques.Shake).duration(1000).playOn(tvStartTime)
                    }
                    toast(R.string.select_time)
                    return@onSafeClick
                }


                if (calendarType == CalendarType.OneTimePickerForReservation && tvPerson.text == getString(R.string.person)) {
                    YoYo.with(Techniques.Shake).duration(1000).playOn(tvPerson)
                    toast(R.string.select_cusNum)
                    return@onSafeClick
                }


                var start = ""
                var end = ""


                when(calendarType){
                    is CalendarType.NoTimePickerForSearchReservation ->  {
                        start = dateFormatSql.format(calendarView.selectedDates[0])
                        if (calendarView.selectedDates.size > 1) {
                            end =
                                dateFormatSql.format(calendarView.selectedDates[calendarView.selectedDates.size - 1])
                        }else{
                            end = dateFormatSql.format(calendarView.selectedDates[0])
                        }
                    }
                    is CalendarType.OneTimePickerForReservation -> {
                        start = dateFormatSql.format(calendarView.selectedDates[0])+ " " + tvStartTime.text.toString()
                        end = start
                    }
                    else -> {
                        start = dateFormatSql.format(calendarView.selectedDates[0])
                        if (calendarView.selectedDates.size > 1) {
                            end =
                                dateFormatSql.format(calendarView.selectedDates[calendarView.selectedDates.size - 1])
                        }else{
                            end = dateFormatSql.format(calendarView.selectedDates[0])
                        }
                    }
                }.exhaustive



                if (dateFormatSql.parse(start)?.after(dateFormatSql.parse(end)) == true) {
                    toast(R.string.input_err)
                    YoYo.with(Techniques.Shake).duration(1000).playOn(calendarView)
                    return@onSafeClick
                }

                listener(count,start, end, mode , adultCount, childCount)
                dismiss()
            } else {
                YoYo.with(Techniques.Shake).duration(1000).playOn(calendarView)
                toast(R.string.notFinish)
            }
        }
        btnClose.onSafeClick { dismiss() }
    }

    private fun showNumberPickerDialog(textView: TextView) {
            CustomNumberPickerDialog() { adultCount, childCount, totalCount ->
                textView.text = totalCount
                count = totalCount
                this.adultCount = adultCount
                this.childCount = childCount
            }.show(requireActivity().supportFragmentManager, "CustomNumberPickerDialog")
        }



    override fun initAction() {
    }

    private fun showTimePickerDialog(v: TextView) {

        when(calendarType){
            is CalendarType.OneTimePickerForReservation -> {
                val now = Calendar.getInstance()
                var timePickerDialog = TimePickerDialog.newInstance({ _, hourOfDay, minute, _ ->
                    v.text = "${String.format("%02d", hourOfDay)}:${String.format("%02d", minute)}"
                },now[Calendar.MINUTE],now[Calendar.MINUTE],true)
                timePickerDialog.setTimeInterval(1,15)
                timePickerDialog.setMinTime(Timepoint(10))
                timePickerDialog.setMaxTime(Timepoint(21))
         //       timePickerDialog.setDisabledTimes(arrayOf(Timepoint(23),Timepoint(22)))
                timePickerDialog.show(parentFragmentManager, "timePicker")
            }
            else -> {
                TimePickerFragment(v.text.toString()) {
                    v.text = it
                }.show(parentFragmentManager, "timePicker")
            }
        }
    }


    private fun setWindowWidthPercent() {
        dialog?.window?.let {
            val size = Point()
            val display = it.windowManager.defaultDisplay
            display.getSize(size)

            val width = size.x
            val height = size.y

            it.setLayout((width * 0.65).toInt(), (height * 0.75).toInt())
            it.setGravity(Gravity.CENTER)
        }
    }
}