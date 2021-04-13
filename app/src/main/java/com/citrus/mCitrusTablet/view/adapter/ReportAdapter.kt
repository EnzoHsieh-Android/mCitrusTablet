package com.citrus.mCitrusTablet.view.adapter

import android.content.Context
import android.graphics.Color
import android.provider.SyncStateContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.RvReportItemBinding
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.Constants



class ReportAdapter(var context: Context,var list:MutableList<Any>,var reportType: Int):RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {


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

    fun setList(list: MutableList<Any>, type: Int){
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
                0,-1 -> {
                    binding.tvName.text = (any as ReservationGuests).mName
                    binding.tvContact.text = if(any.phone != null && any.phone != "")  any.phone else any.email
                    changeBackground(any.status)
                }

                1 -> {
                    binding.tvName.text = (any as Wait).mName
                    binding.tvContact.text = if(any.phone != null && any.phone != "")  any.phone else any.email
                    changeBackground(any.status)
                }
            }
        }

        private fun changeBackground(type:String){
            when(type){
                Constants.ADD -> {
                    binding.tvName.setTextColor(context.resources.getColor(R.color.chart_color_wait,null))
                    binding.tvContact.setTextColor(context.resources.getColor(R.color.chart_color_wait,null))
                }
                Constants.NOTICE -> {
                    binding.tvName.setTextColor(context.resources.getColor(R.color.chart_color_wait,null))
                    binding.tvContact.setTextColor(context.resources.getColor(R.color.chart_color_wait,null))
                }
                Constants.CHECK -> {
                    binding.tvName.setTextColor(context.resources.getColor(R.color.chart_color_check,null))
                    binding.tvContact.setTextColor(context.resources.getColor(R.color.chart_color_check,null))
                }
                Constants.CANCEL -> {
                    binding.tvName.setTextColor(context.resources.getColor(R.color.chart_color_cancel,null))
                    binding.tvContact.setTextColor(context.resources.getColor(R.color.chart_color_cancel,null))
                }
            }

        }

    }

}