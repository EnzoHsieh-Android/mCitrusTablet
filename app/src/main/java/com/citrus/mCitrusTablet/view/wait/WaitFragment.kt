package com.citrus.mCitrusTablet.view.wait

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentWaitBinding
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.view.adapter.WaitAdapter
import com.citrus.mCitrusTablet.view.dialog.CustomAlertDialog
import com.citrus.mCitrusTablet.view.dialog.CustomOrderDeliveryDialog
import com.citrus.mCitrusTablet.view.reservation.SearchViewStatus
import com.citrus.mCitrusTablet.view.reservation.TasksEvent
import com.citrus.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_reservation.*
import kotlinx.coroutines.flow.collect
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class WaitFragment : Fragment(R.layout.fragment_wait) {
    private val waitViewModel: WaitViewModel by viewModels()
    private var _binding: FragmentWaitBinding? = null
    private val binding get() = _binding!!
    private var sortOrderByTime: SortOrder = SortOrder.BY_TIME_LESS
    private var sortOrderByCount: SortOrder = SortOrder.BY_LESS
    private var isHideCheck = false
    private val waitAdapter by lazy{ WaitAdapter(requireActivity(),
        onItemClick = { wait ->
            activity?.let {
                CustomOrderDeliveryDialog(
                    wait,
                    waitViewModel
                ).show(it.supportFragmentManager, "CustomOrderDeliveryDialog")
            }
        }, onButtonClick = {
            waitViewModel.changeStatus(it, Constants.CHECK)
        }, onNoticeClick = {
            waitViewModel.sendNotice(it)
        }) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWaitBinding.bind(view)
        initView()
        initObserver()
    }


    private fun initView() {
        binding.apply {
            date2Day(SimpleDateFormat("yyyy/MM/dd").format(Date()))

            laySwipe.setOnRefreshListener {
                waitViewModel.reload()
                laySwipe.isRefreshing = false
            }

            reservationRv.apply {
                adapter = waitAdapter
                layoutManager = LinearLayoutManager(requireContext())
                waitAdapter.update(mutableListOf())


                ItemTouchHelper(
                    object : ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
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
                    timeSortStatus.setImageDrawable(resources.getDrawable(R.drawable.up))
                    SortOrder.BY_TIME_MORE
                } else {
                    timeSortStatus.setImageDrawable(resources.getDrawable(R.drawable.down))
                    SortOrder.BY_TIME_LESS
                }
                waitViewModel.sortList(sortOrderByTime)
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



            searchView.onQueryTextChanged {
                if (it.isEmpty()) {
                    waitViewModel.searchForStr(SearchViewStatus.IsEmpty)
                } else {
                    waitViewModel.searchForStr(SearchViewStatus.NeedChange(it))
                }
            }


            btReservation.setOnSlideCompleteListener {
                var cusName = binding.name.text.toString().trim()
                var cusPhone = binding.phone.text.toString().trim()
                var seat = binding.seat.text.toString().trim()

                if (cusName.isEmpty() || cusPhone.isEmpty() || seat.isEmpty()) {
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
                            seat.toInt(),
                            cusName,
                            cusPhone,
                            "",
                            Constants.ADD
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
                waitAdapter?.update(waitList)
                binding.reservationRv.visibility = View.VISIBLE
                binding.animationResultNotFound.visibility = View.GONE
            } else {
                binding.reservationRv.visibility = View.GONE
                binding.animationResultNotFound.visibility = View.VISIBLE
            }

            binding.reservationRv.post {
                binding.reservationRv.smoothScrollToPosition(0)
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
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.reservationRv.adapter = null
        _binding = null
    }

    private fun clearSubmitText(isHideSeat: Boolean) {
        binding.name.text.clear()
        binding.phone.text.clear()
        binding.seat.text.clear()
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