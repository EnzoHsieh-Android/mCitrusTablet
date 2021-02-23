package com.citrus.mCitrusTablet.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.citrus.mCitrusTablet.databinding.WaititemBinding
import com.citrus.mCitrusTablet.model.vo.Wait
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat

class WaitAdapter(@ApplicationContext val context: Context, private val listener: OnItemClickListener,) : ListAdapter<Wait, WaitAdapter.TasksViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = WaititemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }


    interface OnItemClickListener {
        fun onItemClick(wait: Wait)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, position)
    }

    inner class TasksViewHolder(private val binding: WaititemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(wait: Wait, position: Int) {
            binding.apply {
                name.text = wait.mName
                phone.text = wait.phone
                count.text = wait.custNum.toString()
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val outputFormat = SimpleDateFormat("MM/dd HH:mm")
                val date = inputFormat.parse(wait.reservationTime)
                val formattedDate = outputFormat.format(date)
                var timeStr = formattedDate.split(" ")
                reservationTime.text = timeStr[1]

                if(wait.status == "A"){
                    btnCheck.visibility = View.VISIBLE
                    tvCheck.visibility = View.GONE
                }else{
                    btnCheck.visibility = View.GONE
                    tvCheck.visibility = View.VISIBLE
                }

                btnCheck.setOnClickListener { listener.onItemClick(wait) }
            }

        }
    }



    class DiffCallback : DiffUtil.ItemCallback<Wait>() {
        override fun areItemsTheSame(oldItem: Wait, newItem: Wait) =
            oldItem.tkey == newItem.tkey

        override fun areContentsTheSame(oldItem: Wait, newItem: Wait) =
            oldItem == newItem
    }
}