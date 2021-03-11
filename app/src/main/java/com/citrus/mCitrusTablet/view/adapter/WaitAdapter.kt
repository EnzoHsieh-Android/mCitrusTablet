package com.citrus.mCitrusTablet.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.citrus.mCitrusTablet.databinding.WaititemBinding
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.AsyncDiffUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat

class WaitAdapter(@ApplicationContext val context: Context,val onItemClick: (Wait) -> Unit,val onButtonClick: (Wait) -> Unit) :RecyclerView.Adapter<WaitAdapter.TasksViewHolder>() {
    private val asyncDiffUtil: AsyncDiffUtil<Wait> = AsyncDiffUtil(this, object : DiffUtil.ItemCallback<Wait>() {
        override fun areItemsTheSame(oldItem: Wait, newItem: Wait) = oldItem.isSame(newItem)

        override fun areContentsTheSame(oldItem: Wait, newItem: Wait): Boolean = oldItem.isContentSame(newItem)
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = WaititemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun getItemCount() = asyncDiffUtil.current().size

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val item = asyncDiffUtil.current()[position]
            holder.bind(item,asyncDiffUtil.current())
        }
    }

    fun update(items: List<Wait>) {
        asyncDiffUtil.update(items)
    }

    inner class TasksViewHolder(private val binding: WaititemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(wait: Wait, currentList: List<Wait>) {
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

                var hasDelivery = (wait.orderNo != null && wait.orderNo != "")


                if(hasDelivery){
                    imgDelivery.visibility = View.VISIBLE
                    root.setOnClickListener {
                        onItemClick(wait)
                    }
                }else{
                    imgDelivery.visibility = View.INVISIBLE
                }


                if(wait.status == "A"){
                    btnCheck.visibility = View.VISIBLE
                    tvCheck.visibility = View.GONE
                }else{
                    btnCheck.visibility = View.GONE
                    tvCheck.visibility = View.VISIBLE
                }

                btnCheck.setOnClickListener {
                    onButtonClick(wait)
                }
            }
        }
    }
}