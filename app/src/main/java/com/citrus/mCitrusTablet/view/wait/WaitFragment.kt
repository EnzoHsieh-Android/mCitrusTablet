package com.citrus.mCitrusTablet.view.wait

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.citrus.mCitrusTablet.util.HideCheck
import com.citrus.mCitrusTablet.util.ui.BaseFragment
import com.citrus.mCitrusTablet.view.SharedViewModel
import com.citrus.mCitrusTablet.view.adapter.WaitAdapter
import com.citrus.mCitrusTablet.view.dialog.CustomAlertDialog
import com.citrus.mCitrusTablet.view.dialog.CustomFilterCheckBoxDialog
import com.citrus.mCitrusTablet.view.dialog.CustomNumberPickerDialog
import com.citrus.mCitrusTablet.view.dialog.CustomOrderDeliveryDialog
import com.citrus.mCitrusTablet.view.reservation.CusNumType
import com.citrus.mCitrusTablet.view.reservation.SearchViewStatus
import com.citrus.mCitrusTablet.view.reservation.TasksEvent
import com.citrus.util.onQueryTextChanged
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
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
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val waitViewModel: WaitViewModel by activityViewModels()
    private var _binding: FragmentWaitBinding? = null
    private val binding get() = _binding!!
    private var sortOrderByTime: SortOrder = SortOrder.BY_TIME_MORE
    private var sortOrderByCount: SortOrder = SortOrder.BY_LESS
    private var filterType = Filter.SHOW_ALL
    private var cusNumType = CusNumType.SHOW_DETAIL
    private var isHideCheck = false
    private var isSwapEmail = false
    private var cusNum = 0
    private var adultNum = 0
    private var childNum = 0
    private var tempWaitList = mutableListOf<Wait>()
    private val waitAdapter by lazy {
        WaitAdapter(
            mutableListOf(),
            cusNumType,
            requireActivity(),
            onImgClick = { wait ->
                waitViewModel.itemSelect(wait) },
            onItemClick = { wait ->
                waitViewModel.itemSelect(wait)
            }, onButtonClick = {
                waitViewModel.changeStatus(it, Constants.CHECK)
            }, onNoticeClick = {
                waitViewModel.sendNotice(it)
            }, onDeliveryClick = {
                waitViewModel.itemSelect(it)
                CustomOrderDeliveryDialog(
                    requireActivity(),
                    it,
                    waitViewModel
                ).show(requireActivity().supportFragmentManager, "CustomOrderDeliveryDialog")
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
                binding.loading.visibility = View.VISIBLE
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


            changeType.setOnClickListener {
                cusNumType = when(cusNumType) {
                    CusNumType.SHOW_DETAIL -> {
                        CusNumType.SHOW_TOTAL
                    }
                    CusNumType.SHOW_TOTAL -> {
                        CusNumType.SHOW_DETAIL
                    }
                }
              waitAdapter.changeType(cusNumType)
            }

            sortByCount.setOnClickListener {
                sortOrderByCount = if (sortOrderByCount == SortOrder.BY_LESS) {
                    SortOrder.BY_MORE
                } else {
                    SortOrder.BY_LESS
                }
                waitViewModel.sortList(sortOrderByCount)
            }

            sortByTime.setOnClickListener {
                sortOrderByTime = if (sortOrderByTime == SortOrder.BY_TIME_LESS) {
                    SortOrder.BY_TIME_MORE
                } else {
                    SortOrder.BY_TIME_LESS
                }
                waitViewModel.sortList(sortOrderByTime)
            }


            contentSwap.setOnClickListener {
                isSwapEmail = !isSwapEmail
                if(isSwapEmail){
                    phoneTextInputLayout.visibility = View.GONE
                    mailTextInputLayout.visibility = View.VISIBLE
                }else{
                    phoneTextInputLayout.visibility = View.VISIBLE
                    mailTextInputLayout.visibility = View.GONE
                }
            }


            hideCheckBlock.setOnClickListener {
                waitViewModel.hideChecked(isHideCheck)
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


            seat.setOnFocusChangeListener { _ , hasFocus ->
                if(hasFocus){
                    CustomNumberPickerDialog { adultCount, childCount, totalCount ->
                        seat.setText(totalCount,false)
                        cusNum = totalCount.toInt()
                        adultNum = adultCount.toInt()
                        childNum = childCount.toInt()
                    }.show(requireActivity().supportFragmentManager, "CustomNumberPickerDialog")
                    seat.clearFocus()
                }
            }


//            seatTextInputLayout.setOnClickListener {
//                CustomNumberPickerDialog { adultCount, childCount, totalCount ->
//                    seat.setText(totalCount,false)
//                    cusNum = totalCount.toInt()
//                    adultNum = adultCount.toInt()
//                    childNum = childCount.toInt()
//                }.show(requireActivity().supportFragmentManager, "CustomNumberPickerDialog")
//            }


            btReservation.setOnSlideCompleteListener {
                var cusName = binding.name.text.toString().trim()
                var cusPhone = binding.phone.text.toString().trim()
                var cusEmail = binding.mail.text.toString().trim()
                var seat = binding.seat.text.toString().trim()
                var memo = binding.memo.text.toString().trim()


                if(cusName.isEmpty()) {
                    YoYo.with(Techniques.Shake).duration(1000).playOn(binding.nameTextInputLayout)
                    YoYo.with(Techniques.Shake).duration(1000).playOn(binding.name)
                    return@setOnSlideCompleteListener
                }

                if(cusPhone.isEmpty() && cusEmail.isEmpty() ) {
                    YoYo.with(Techniques.Shake).duration(1000).playOn(binding.phoneTextInputLayout)
                    YoYo.with(Techniques.Shake).duration(1000).playOn(binding.phone)
                    YoYo.with(Techniques.Shake).duration(1000).playOn(binding.mailTextInputLayout)
                    YoYo.with(Techniques.Shake).duration(1000).playOn(binding.mail)
                    return@setOnSlideCompleteListener
                }

                if(seat.isEmpty() ) {
                    YoYo.with(Techniques.Shake).duration(1000).playOn(binding.seatTextInputLayout)
                    YoYo.with(Techniques.Shake).duration(1000).playOn(binding.seat)
                    return@setOnSlideCompleteListener
                }

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

    private fun initObserver() {


        waitViewModel.resHasNewData.observe(viewLifecycleOwner, {
            sharedViewModel.newDataNotify(Constants.KEY_RESERVATION_NUM,it)
        })




        waitViewModel.allData.observe(viewLifecycleOwner, { waitList ->
            binding.loading.visibility = View.GONE
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

        waitViewModel.hideCheckType.observe(viewLifecycleOwner,{ status ->
            when(status){
                HideCheck.HIDE_TRUE -> {
                    isHideCheck = false
                    hideCheck.setImageDrawable(resources.getDrawable(R.drawable.visibility))
                    tv_hideCheck.text = resources.getString(R.string.hide_check)
                }
                HideCheck.HIDE_FALSE -> {
                    isHideCheck = true
                    hideCheck.setImageDrawable(resources.getDrawable(R.drawable.eye))
                    tv_hideCheck.text = resources.getString(R.string.show_check)

                }
            }
        })

        waitViewModel.filterType.observe(viewLifecycleOwner,{ status ->
            filterType = status
            when(status){
                Filter.SHOW_ALL -> {  binding.tvFilterType.text = resources.getString(R.string.filter_all) }
                Filter.SHOW_CANCELLED -> {  binding.tvFilterType.text = resources.getString(R.string.filter_cancelled) }
                Filter.SHOW_NOTIFIED -> {  binding.tvFilterType.text = resources.getString(R.string.filter_notified) }
                Filter.SHOW_CONFIRM -> {  binding.tvFilterType.text = resources.getString(R.string.filter_confirm) }
                Filter.SHOW_WAIT -> {  binding.tvFilterType.text = resources.getString(R.string.filter_wait) }
            }
        })

        waitViewModel.sortType.observe(viewLifecycleOwner,{ status ->
            when(status){
                SortOrder.BY_TIME_MORE -> {
                    sortOrderByTime = status
                    binding.timeSortStatus.visibility = View.VISIBLE
                    binding.timeSortStatus.setImageDrawable(resources.getDrawable(R.drawable.down))
                    binding.groupSortStatus.visibility = View.INVISIBLE
                }
                SortOrder.BY_TIME_LESS -> {
                    sortOrderByTime = status
                    binding.timeSortStatus.visibility = View.VISIBLE
                    binding.timeSortStatus.setImageDrawable(resources.getDrawable(R.drawable.up))
                    binding.groupSortStatus.visibility = View.INVISIBLE
                }
                SortOrder.BY_MORE -> {
                    sortOrderByCount = status
                    binding.groupSortStatus.visibility = View.VISIBLE
                    binding.groupSortStatus.setImageDrawable(resources.getDrawable(R.drawable.up))
                    binding.timeSortStatus.visibility = View.INVISIBLE
                }
                SortOrder.BY_LESS -> {
                    sortOrderByCount = status
                    binding.groupSortStatus.visibility = View.VISIBLE
                    binding.groupSortStatus.setImageDrawable(resources.getDrawable(R.drawable.down))
                    binding.timeSortStatus.visibility = View.INVISIBLE
                }
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
        waitViewModel.onDetachView()
        super.onDestroyView()
    }

    private fun clearSubmitText(isHideSeat: Boolean) {
        binding.name.text.clear()
        binding.phone.text.clear()
        binding.mail.text.clear()
        binding.memo.text.clear()
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