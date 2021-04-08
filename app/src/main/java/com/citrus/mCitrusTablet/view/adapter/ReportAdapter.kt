package com.citrus.mCitrusTablet.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.citrus.mCitrusTablet.databinding.RvReportItemBinding
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.view.report.ReportType


class ReportAdapter(var list:MutableList<Any>,var reportType: ReportType):RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = RvReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val item = list[position]
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    fun setList(list: MutableList<Any>, type: ReportType){
        this.list.clear()
        this.list.addAll(list)
        this.reportType = type
        notifyDataSetChanged()
    }


    inner class ReportViewHolder(private val binding: RvReportItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(any: Any) {
            when(reportType){
                ReportType.RESERVATION -> {
                    binding.tvName.text = (any as ReservationGuests).mName
                    binding.tvContact.text = if(any.phone != null && any.phone != "")  any.phone else any.email
                }

                ReportType.WAIT -> {
                    binding.tvName.text = (any as Wait).mName
                    binding.tvContact.text = if(any.phone != null && any.phone != "")  any.phone else any.email
                }
            }
        }

    }

}