package com.citrus.mCitrusTablet.view.wait

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentReservationBinding
import com.citrus.mCitrusTablet.databinding.FragmentWaitBinding
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.ReservationClass
import com.citrus.mCitrusTablet.model.vo.ReservationUpload
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.ui.CustomAlertDialog
import com.citrus.mCitrusTablet.view.adapter.WaitAdapter
import com.citrus.mCitrusTablet.view.reservation.ReservationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class WaitFragment : Fragment(R.layout.fragment_wait),WaitAdapter.OnItemClickListener {
    private val waitViewModel: WaitViewModel by viewModels()
    private var _binding: FragmentWaitBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWaitBinding.bind(view)
        val waitAdapter = activity?.let { WaitAdapter(it,this) }

        binding.apply {
            date2Day(SimpleDateFormat("yyyy/MM/dd").format(Date()))






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
        }

        waitViewModel.allData.observe(viewLifecycleOwner,{ waitList ->
            waitAdapter?.submitList(waitList.sortedBy { it.reservationTime})
        })


        waitViewModel.cusCount.observe(viewLifecycleOwner, { cusCount ->
            binding.tvTotal.text = resources.getString(R.string.TotalForTheDay)+" "+cusCount
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(wait: Wait) {

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