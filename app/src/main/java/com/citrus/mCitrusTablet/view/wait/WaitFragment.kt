package com.citrus.mCitrusTablet.view.wait

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentReservationBinding
import com.citrus.mCitrusTablet.databinding.FragmentWaitBinding
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.util.ui.BaseFragment
import com.citrus.mCitrusTablet.view.adapter.WaitAdapter
import com.citrus.mCitrusTablet.view.dialog.CustomAlertDialog
import com.citrus.mCitrusTablet.view.dialog.CustomFilterCheckBoxDialog
import com.citrus.mCitrusTablet.view.dialog.CustomNumberPickerDialog
import com.citrus.mCitrusTablet.view.dialog.CustomOrderDeliveryDialog
import com.citrus.mCitrusTablet.view.reservation.SearchViewStatus
import com.citrus.mCitrusTablet.view.reservation.TasksEvent
import com.citrus.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_reservation.*
import kotlinx.coroutines.flow.collect
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class
WaitFragment : BaseFragment() {
    private val waitViewModel: WaitViewModel by viewModels()
    private var _binding: FragmentWaitBinding? = null
    private val binding get() = _binding!!
    private var sortOrderByTime: SortOrder = SortOrder.BY_TIME_MORE
    private var sortOrderByCount: SortOrder = SortOrder.BY_LESS
    private var filterType = Filter.SHOW_ALL
    private var isHideCheck = false
    private var isSwapEmail = false
    private var cusNum = 0
    private var adultNum = 0
    private var childNum = 0
    private var tempWaitList = mutableListOf<Wait>()
    private val waitAdapter by lazy {
        WaitAdapter(
            mutableListOf(),
            requireActivity(),
            onImgClick = { wait ->
                waitViewModel.itemSelect(wait) },
            onItemClick = { wait,hasMemo,hasDelivery ->
                waitViewModel.itemSelect(wait)
                if(hasDelivery) {
                    CustomOrderDeliveryDialog(
                        requireActivity(),
                        wait,
                        waitViewModel
                    ).show(requireActivity().supportFragmentManager, "CustomOrderDeliveryDialog")
                }
            }, onButtonClick = {
                waitViewModel.changeStatus(it, Constants.CHECK)
            }, onNoticeClick = {
                waitViewModel.sendNotice(it)
            })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWaitBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWaitBinding.bind(view)
        initView()
        initObserver()
        waitViewModel.startFetchJob()
    }


    private fun initView() {
        binding.apply {
            date2Day(SimpleDateFormat("yyyy/MM/dd").format(Date()))


            btn_reloadBlock.setOnClickListener {
                waitViewModel.reload()
            }


            reservationRv.apply {
                adapter = waitAdapter
                addItemDecoration(DividerItemDecoration(this.context,DividerItemDecoration.VERTICAL))
                layoutManager = LinearLayoutManager(requireContext())
                waitAdapter.update(mutableListOf())


                ItemTouchHelper(
                    object : ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    ) {
                        override fun onMove(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                        ): Boolean {
                            return false
                        }

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            var guest = tempWaitList[viewHolder.adapterPosition]


                            if(guest.status != "C" && guest.status != "D") {
                                waitViewModel.changeStatus(guest, Constants.CANCEL)
                            }else{
                                waitAdapter.notifyDataSetChanged()
                            }

                        }

                    }).attachToRecyclerView(this)
            }

            btReservation.setOnSlideCompleteListener {
                var dialog = activity?.let {
                    CustomAlertDialog(
                        it,
                        getString(R.string.submitErrorMsg),
                        "",
                        0
                    )
                }
                dialog!!.show()
            }


            sortByCount.setOnClickListener {
                sortOrderByCount = if (sortOrderByCount == SortOrder.BY_LESS) {
                    groupSortStatus.setImageDrawable(resources.getDrawable(R.drawable.up))
                    SortOrder.BY_MORE
                } else {
                    groupSortStatus.setImageDrawable(resources.getDrawable(R.drawable.down))
                    SortOrder.BY_LESS
                }
                waitViewModel.sortList(sortOrderByCount)
            }

            sortByTime.setOnClickListener {
                sortOrderByTime = if (sortOrderByTime == SortOrder.BY_TIME_LESS) {
                    timeSortStatus.setImageDrawable(resources.getDrawable(R.drawable.down))
                    SortOrder.BY_TIME_MORE
                } else {
                    timeSortStatus.setImageDrawable(resources.getDrawable(R.drawable.up))
                    SortOrder.BY_TIME_LESS
                }
                waitViewModel.sortList(sortOrderByTime)
            }


            contentSwap.setOnClickListener {
                isSwapEmail = !isSwapEmail
                if(isSwapEmail){
                    phone.visibility = View.GONE
                    mail.visibility = View.VISIBLE
                }else{
                    phone.visibility = View.VISIBLE
                    mail.visibility = View.GONE
                }
            }


            hideCheckBlock.setOnClickListener {
                waitViewModel.hideChecked(isHideCheck)
                isHideCheck = !isHideCheck
                if (isHideCheck) {
                    hideCheck.setImageDrawable(resources.getDrawable(R.drawable.eye))
                    tv_hideCheck.text = resources.getString(R.string.show_check)
                } else {
                    hideCheck.setImageDrawable(resources.getDrawable(R.drawable.visibility))
                    tv_hideCheck.text = resources.getString(R.string.hide_check)
                }
            }


            statusFilter.setOnClickListener {
                var dialog = activity?.let {
                    CustomFilterCheckBoxDialog(
                        it,
                        filterType,
                        onCheckChange = { filter ->
                            filterType = filter

                            when(filterType){
                                Filter.SHOW_ALL -> {  tvFilterType.text = resources.getString(R.string.filter_all) }
                                Filter.SHOW_CANCELLED -> {  tvFilterType.text = resources.getString(R.string.filter_cancelled) }
                                Filter.SHOW_NOTIFIED -> {  tvFilterType.text = resources.getString(R.string.filter_notified) }
                                Filter.SHOW_CONFIRM -> {  tvFilterType.text = resources.getString(R.string.filter_confirm) }
                                Filter.SHOW_WAIT -> {  tvFilterType.text = resources.getString(R.string.filter_wait) }
                            }

                            waitViewModel.changeFilter(filterType)
                        }
                    )
                }
                dialog!!.show()
            }


            searchView.onQueryTextChanged {
                if (it.isEmpty()) {
                    waitViewModel.searchForStr(SearchViewStatus.IsEmpty)
                } else {
                    waitViewModel.searchForStr(SearchViewStatus.NeedChange(it))
                }
            }

            seat.setOnClickListener {
                CustomNumberPickerDialog() { adultCount, childCount, totalCount ->
                    seat.text = totalCount
                    cusNum = totalCount.toInt()
                    adultNum = adultCount.toInt()
                    childNum = childCount.toInt()
                }.show(requireActivity().supportFragmentManager, "CustomNumberPickerDialog")
            }


            btReservation.setOnSlideCompleteListener {
                var cusName = binding.name.text.toString().trim()
                var cusPhone = binding.phone.text.toString().trim()
                var cusEmail = binding.mail.text.toString().trim()
                var seat = binding.seat.text.toString().trim()
                var memo = binding.memo.text.toString().trim()

                if (cusName.isEmpty() || (cusPhone.isEmpty() && cusEmail.isEmpty()) || seat.isEmpty()) {
                    var dialog = activity?.let {
                        CustomAlertDialog(
                            it,
                            getString(R.string.submitErrorMsg),
                            "",
                            R.drawable.ic_baseline_error_24
                        )
                    }
                    dialog!!.show()
                } else {
                    var data = PostToSetWaiting(
                        WaitGuestData(
                            prefs.storeId.toInt(),
                            cusNum,
                            cusName,
                            cusPhone,
                            cusEmail,
                            memo,
                            Constants.ADD,
                            adultNum,
                            childNum
                        )
                    )
                    waitViewModel.uploadWait(data)
                }
            }

        }
    }

    private fun initObserver() {
        waitViewModel.allData.observe(viewLifecycleOwner, { waitList ->
            if (waitList.isNotEmpty()) {
                tempWaitList = waitList as MutableList<Wait>
                waitAdapter?.update(waitList)
                binding.reservationRv.visibility = View.VISIBLE
                binding.animationResultNotFound.visibility = View.GONE
            } else {
                binding.reservationRv.visibility = View.GONE
                binding.animationResultNotFound.visibility = View.VISIBLE
            }

        })


        waitViewModel.cusCount.observe(viewLifecycleOwner, { cusCount ->
            binding.tvTotal.text = resources.getString(R.string.TotalForTheDay) + " " + cusCount
        })


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            waitViewModel.tasksEvent.collect { event ->
                when (event) {
                    is TasksEvent.ShowSuccessMessage -> {
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

                    is TasksEvent.ShowUndoDeleteTaskMessageW -> {
                        var mSnackBar = Snackbar.make(requireView(), R.string.FinishMsg, Snackbar.LENGTH_LONG)
                            .setAction(R.string.Undo) {
                                waitViewModel.onUndoFinish(event.wait)
                            }
                        adjustSnackBar(mSnackBar).show()
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        binding.reservationRv.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun clearSubmitText(isHideSeat: Boolean) {
        binding.name.text.clear()
        binding.phone.text.clear()
        binding.mail.text.clear()
        binding.memo.text.clear()
        binding.seat.text = ""
    }

    @Throws(ParseException::class)
    fun date2Day(dateString: String?) {
        val dateStringFormat = SimpleDateFormat("yyyy/MM/dd")
        val date = dateStringFormat.parse(dateString)
        val date2DayFormat = SimpleDateFormat("E")
        binding.tvDate.text = dateString
        binding.tvDay.text = date2DayFormat.format(date)
    }
}