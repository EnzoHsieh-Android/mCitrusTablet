package com.citrus.mCitrusTablet.view.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.RvWaitItemBinding
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.AsyncDiffUtil
import com.citrus.mCitrusTablet.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

class WaitAdapter(var waitList:MutableList<Wait>,@ApplicationContext val context: Context,val onItemClick: (Wait,Boolean,Boolean) -> Unit,val onButtonClick: (Wait) -> Unit,val onNoticeClick: (Wait) -> Unit) :RecyclerView.Adapter<WaitAdapter.TasksViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = RvWaitItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun getItemCount() = waitList.size

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val item = waitList[position]
            holder.bind(item)
        }
    }



    fun update(items: MutableList<Wait>) {
        waitList.clear()
        waitList.addAll(items)
        this.notifyDataSetChanged()
    }

    inner class TasksViewHolder(private val binding: RvWaitItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(wait: Wait) {
            binding.apply {

                checkBlock.visibility = View.VISIBLE
                btnNotice.visibility = View.VISIBLE
                statusBlock.visibility = View.GONE

                name.text = wait.mName
                if(wait.phone != null && wait.phone != ""){
                    phone.text = wait.phone
                }else{
                    phone.text = wait.email!!.split("@")[0]
                }

                binding.root.setBackgroundColor(context.resources.getColor(R.color.selectColor))

                if(wait.isSelect){
                    itemView.setBackgroundColor(context.resources.getColor(R.color.selectColor))
                }else{
                    itemView.setBackgroundColor(Color.WHITE)
                }

                count.text = wait.custNum.toString()
                val date = Constants.inputFormat.parse(wait.reservationTime)
                val formattedDate = Constants.outputFormat.format(date)
                var timeStr = formattedDate.split(" ")
                reservationTime.text = timeStr[1]

                if(wait.status == "C"){
                    btnNotice.visibility = View.GONE
                    statusBlock.visibility = View.GONE
                    btnCheck.visibility = View.GONE
                    tvCheck.visibility = View.VISIBLE

                }else{
                    btnCheck.visibility = View.VISIBLE
                    tvCheck.visibility = View.GONE
                }



                when(wait.status){
                    "I" -> {
                        checkBlock.visibility = View.VISIBLE
                        btnNotice.visibility = View.GONE
                        statusBlock.visibility = View.VISIBLE
                        tvStatus.text = context.resources.getString(R.string.notified)
                        tvUpdateTime.text = wait.updateDate
                        tvStatus.textColor = context.resources.getColor(R.color.deepGray)
                        tvUpdateTime.textColor = context.resources.getColor(R.color.deepGray)
                        if(wait.isOverTime){
                            tvStatus.textColor = Color.RED
                            tvUpdateTime.textColor = Color.RED
                            tvStatus.text = context.resources.getString(R.string.timeOut)
                        }
                    }

                    "O" -> {
                        checkBlock.visibility = View.VISIBLE
                        btnNotice.visibility = View.GONE
                        statusBlock.visibility = View.VISIBLE
                        tvStatus.text = context.resources.getString(R.string.confirmed)
                        tvUpdateTime.text = wait.updateDate
                        tvStatus.textColor = context.resources.getColor(R.color.primaryDarkColor)
                        tvUpdateTime.textColor = context.resources.getColor(R.color.primaryDarkColor)
                    }

                    "D" -> {
                        checkBlock.visibility = View.GONE
                        btnNotice.visibility = View.GONE
                        statusBlock.visibility = View.VISIBLE
                        tvStatus.text = context.resources.getString(R.string.delete)
                        tvUpdateTime.text = wait.updateDate
                        tvStatus.textColor = Color.RED
                        tvUpdateTime.textColor = Color.RED
                    }
                }


                var hasMemo = (wait.memo != null && wait.memo != "")
                var hasDelivery = (wait.orderNo != null && wait.orderNo != "")


                if(hasDelivery){
                    imgDelivery.visibility = View.VISIBLE
                }else{
                    imgDelivery.visibility = View.INVISIBLE
                }


                root.setOnClickListener {
                    onItemClick(wait,hasMemo,hasDelivery)
                }

                btnCheck.setOnClickListener {
                    onButtonClick(wait)
                }
                btnNotice.setOnClickListener {
                    onNoticeClick(wait)
                }
            }
        }
    }
}