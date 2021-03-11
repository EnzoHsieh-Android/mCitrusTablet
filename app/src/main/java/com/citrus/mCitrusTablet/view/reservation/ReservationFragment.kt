package com.citrus.mCitrusTablet.view.reservation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentReservationBinding
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.ReservationClass
import com.citrus.mCitrusTablet.model.vo.PostToSetReservation
import com.citrus.mCitrusTablet.util.Constants.defaultTimeStr
import com.citrus.mCitrusTablet.util.onSafeClick
import com.citrus.mCitrusTablet.view.adapter.ReservationAdapter
import com.citrus.mCitrusTablet.view.dialog.*
import com.citrus.util.onQueryTextChanged
import com.savvi.rangedatepicker.CalendarPickerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_reservation.*
import kotlinx.coroutines.flow.collect
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class ReservationFragment : Fragment(R.layout.fragment_reservation) {

    private val reservationFragmentViewModel: ReservationViewModel by viewModels()
    private var _binding: FragmentReservationBinding? = null
    private val binding get() = _binding!!

    private var mode = CalendarPickerView.SelectionMode.SINGLE
    private val itemPerLine: Int = 1
    private var tempSeat: String = ""
    private var tempTime: String = ""
    private var tempCount: Int = 0

    private var seatData = mutableListOf<String>()
    private var timeTitle = mutableListOf<String>()
    private val reservationAdapter by lazy{ SectionedRecyclerViewAdapter() }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReservationBinding.bind(view)

        binding.apply {
            date2Day(
                reservationFragmentViewModel.dateRange.value?.get(0) ?: SimpleDateFormat(
                    "yyyy/MM/dd"
                ).format(Date())
            )

            reservationRv.apply {
                val glm = GridLayoutManager(activity, itemPerLine)
                glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (reservationAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                            itemPerLine
                        } else {
                            1
                        }
                    }
                }
                this.layoutManager = glm
            }

            laySwipe.setOnRefreshListener {
                reservationFragmentViewModel.reload()
                laySwipe.isRefreshing = false
            }

            btPrev.setOnClickListener {
                dateChange(tvDate.text as String, -1)
            }

            btNext.setOnClickListener {
                dateChange(tvDate.text as String, 1)
            }


            btnSearchTable.setOnClickListener {
               activity?.let {
                    CustomSearchTableDialog(
                        requireActivity(),
                        reservationFragmentViewModel
                    ) { time,cusCount,seat ->
                        searchSeat(cusCount,time,seat, seat=="0")
                    }.show(it.supportFragmentManager, "CustomSearchTableDialog")
                }
            }

            /*上方日期按鈕*/
            llDate.onSafeClick {
                activity?.let {
                    CustomDatePickerDialog(
                        it,
                        CalendarType.NoTimePickerForSearchReservation,
                        mode,
                        reservationFragmentViewModel.dateRange.value?.get(0) ?: "",
                        reservationFragmentViewModel.dateRange.value?.get(1) ?: ""
                    ) { _, startTime, endTime, _ ->
                        reservationFragmentViewModel.setDateArray(arrayOf(startTime, endTime))
                        date2Day(startTime)
                    }.show(it.supportFragmentManager, "CustomDatePickerDialog")
                }
            }


            /*左側預約按鈕*/
            llDateReservation.onSafeClick {
                activity?.let {
                    CustomDatePickerDialog(
                        it,
                        CalendarType.OneTimePickerForReservation,
                        mode,
                        reservationFragmentViewModel.dateRange.value?.get(0) ?: "",
                        reservationFragmentViewModel.dateRange.value?.get(1) ?: ""
                    ) { cusCount, startTime, _, _ ->
                        searchSeat(cusCount,startTime,"",true)
                    }.show(it.supportFragmentManager, "CustomDatePickerDialog")
                }
            }


            searchView.onQueryTextChanged {
                if (it.isEmpty()) {
                    reservationFragmentViewModel.searchForStr(SearchViewStatus.IsEmpty)
                } else {
                    reservationFragmentViewModel.searchForStr(SearchViewStatus.NeedChange(it))
                }
            }

            animationReload.setOnClickListener {
                reservationFragmentViewModel.setDateArray(arrayOf(defaultTimeStr, defaultTimeStr))
                date2Day(defaultTimeStr)
            }


            seatPicker.setOnValueChangedListener { _, _, newVal ->
                tempSeat = seatData[newVal - 1]
            }


            btReservation.setOnSlideCompleteListener {
                var cusName = binding.name.text.toString().trim()
                var cusPhone = binding.phone.text.toString().trim()
                var cusMemo = binding.memo.text.toString().trim()
                var seat = tempSeat.split("-")

                if (cusName.isEmpty() || cusPhone.isEmpty()|| tempSeat == "") {
                    var dialog = activity?.let {
                        CustomAlertDialog(
                            it,
                            getString(R.string.submitErrorMsg),
                            "",
                            R.drawable.ic_baseline_error_24
                        )
                    }
                    dialog!!.show()
                }else{
                    var data = ReservationClass(
                        tempTime, tempCount, "", cusName, cusPhone, cusMemo, "A", seat[0], seat[1]
                    )

                    var uploadData = PostToSetReservation(prefs.rsno, data)

                    reservationFragmentViewModel.uploadReservation(uploadData)
                }
            }
        }


        reservationFragmentViewModel.titleData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                timeTitle = it as MutableList<String>
            }
        })


        reservationFragmentViewModel.seatData.observe(viewLifecycleOwner, { dataList ->
            if (dataList != null && dataList.isNotEmpty()) {
                binding.seatPicker.visibility = View.VISIBLE
                binding.tvSeat.visibility = View.VISIBLE

                seatData.clear()
                for (floor in dataList) {
                    seatData.add(floor.floorName + "-" + floor.roomName)
                }
                tempSeat = seatData[0]
                binding.seatPicker.maxValue = seatData.size
                binding.seatPicker.displayedValues = seatData.toTypedArray()
            }else{
                activity?.let {
                    CustomAlertDialog(
                        requireActivity(),
                        resources.getString(R.string.noSeat),
                        "",
                        0,
                        onConfirmListener = {

                        }
                    ).show()
                }
            }
        })


        reservationFragmentViewModel.datumData.observe(viewLifecycleOwner,{ datumList ->
            activity?.let {
                CustomOtherSeatDialog(
                    requireActivity(),
                    datumList,
                    onBtnClick = { seat,date ->
                        searchSeat(tempCount.toString(),date.replace("-","/"),seat,false)
                    }
                ).show(it.supportFragmentManager, "CustomOtherSeatDialog")
            }
        })


        reservationFragmentViewModel.allData.observe(viewLifecycleOwner, { guestsList ->
            if (guestsList.isNotEmpty()) {
                binding.reservationRv.visibility = View.VISIBLE
                binding.animationResultNotFound.visibility = View.GONE
                activity?.let {
                    reservationAdapter.removeAllSections()
                    for (index in 0 until timeTitle.size) {
                        reservationAdapter.addSection(
                            ReservationAdapter(
                                timeTitle[index],
                                guestsList[index],
                                onItemClick = { Guest ->
                                    activity?.let {
                                        CustomGuestDetailDialog(
                                            it,
                                            Guest
                                        ).show(it.supportFragmentManager,"CustomGuestDetailDialog")
                                    }
                                },
                                onButtonClick = {
                                    reservationFragmentViewModel.changeStatus(it)
                                }
                            )
                        )
                    }
                }
            } else {
                binding.reservationRv.visibility = View.GONE
                binding.animationResultNotFound.visibility = View.VISIBLE
                reservationAdapter.removeAllSections()
            }
            binding.reservationRv.adapter = reservationAdapter
        })

        reservationFragmentViewModel.cusCount.observe(viewLifecycleOwner, { cusCount ->
            binding.tvTotal.text = resources.getString(R.string.TotalForTheDay) + " " + cusCount
        })


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            reservationFragmentViewModel.tasksEvent.collect { event ->
                when (event) {
                    is TasksEvent.ShowSuccessMessage -> {
                        showInformation.visibility = View.GONE
                        hintBlock.visibility = View.VISIBLE
                        clearSubmitText(true)
                        var dialog = activity?.let {
                            CustomAlertDialog(
                                it,
                                resources.getString(R.string.res_ok),
                                "",
                                R.drawable.ic_check
                            )
                        }
                        dialog!!.show()
                    }
                    is TasksEvent.ShowFailMessage -> {
                        var dialog = activity?.let {
                            CustomAlertDialog(
                                it,
                                resources.getString(R.string.res_ng),
                                "",
                                R.drawable.ic_baseline_clear_24
                            )
                        }
                        dialog!!.show()
                    }
                }
            }
        }
    }

    private fun searchSeat(cusCount:String,dateStr:String,seat:String,isSearch:Boolean){
        clearSubmitText(isSearch)
        reservationFragmentViewModel.setDateArrayReservation(
            arrayOf(
                dateStr,
                dateStr
            )
        )
        tempTime = dateStr
        var date = dateStr.split(" ")
        reservationDate.text = date[0]
        reservationTime.text = date[1] + "  " + cusCount + getString(R.string.people)
        tempCount = cusCount.toInt()
        syncChangeDate(date[0])

        showInformation.visibility = View.VISIBLE
        hintBlock.visibility = View.GONE


        if(isSearch) {
            reservationFragmentViewModel.setSearchVal(prefs.rsno, dateStr, cusCount.toInt())
        }else{
            seatData.clear()
            seatData.add(seat)
            tempSeat = seatData[0]
            binding.seatPicker.maxValue = seatData.size
            binding.seatPicker.displayedValues = seatData.toTypedArray()
        }
    }

    private fun syncChangeDate(dateString: String) {
        reservationFragmentViewModel.setDateArray(arrayOf(dateString, dateString))
        date2Day(dateString)
    }

    private fun dateChange(dateString: String, controlDate: Int) {
        binding.searchView.setQuery("", false)
        val sdf = SimpleDateFormat("yyyy/MM/dd")
        var cal: Calendar? = null
        var dateStr = ""
        try {
            var date = sdf.parse(dateString)
            cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_MONTH, controlDate)

            dateStr = sdf.format(cal.time)

            date2Day(dateStr)
        } catch (e: ParseException) {
            println("Exception :$e")
        }
        reservationFragmentViewModel.setDateArray(arrayOf(dateStr, dateStr))
    }

    @Throws(ParseException::class)
    fun date2Day(dateString: String?) {
        val dateStringFormat = SimpleDateFormat("yyyy/MM/dd")
        val date = dateStringFormat.parse(dateString)
        val date2DayFormat = SimpleDateFormat("E")
        binding.tvDate.text = dateString
        binding.tvDay.text = date2DayFormat.format(date)
    }


    private fun clearSubmitText(isHideSeat:Boolean){
        binding.name.text.clear()
        binding.phone.text.clear()
        binding.memo.text.clear()
        if(isHideSeat) {
            tempSeat=""
            binding.tvSeat.visibility = View.INVISIBLE
            binding.seatPicker.visibility = View.INVISIBLE
        }else{
            binding.tvSeat.visibility = View.VISIBLE
            binding.seatPicker.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reservationAdapter.removeAllSections()
        binding.reservationRv.adapter = null
        _binding = null
    }

}