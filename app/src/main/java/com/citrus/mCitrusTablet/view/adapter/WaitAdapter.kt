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
import com.citrus.mCitrusTablet.view.reservation.CusNumType
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

class WaitAdapter(var waitList:MutableList<Wait>,var cusNumType: CusNumType  ,@ApplicationContext val context: Context, val onItemClick: (Wait) -> Unit, val onButtonClick: (Wait) -> Unit, val onNoticeClick: (Wait) -> Unit, val onImgClick:(Wait) -> Unit, val onDeliveryClick:(Wait) -> Unit) :RecyclerView.Adapter<WaitAdapter.TasksViewHolder>() {



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


    fun  changeType(cusNumType: CusNumType) {
        this.cusNumType = cusNumType
        this.notifyDataSetChanged()
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

                recoveryItemView()

                name.text = wait.mName
                if(wait.phone != null && wait.phone != ""){
                    mail.visibility = View.GONE
                    phone.visibility = View.VISIBLE
                    phone.text = wait.phone
                }else{
                    mail.visibility = View.VISIBLE
                    phone.visibility = View.GONE
                    mail.text = wait.email!!.split("@")[0]
                }

                if(wait.isNew){
                    newHint.visibility = View.VISIBLE
                }else{
                    newHint.visibility = View.GONE
                }
                wait.isNew = false


                when(cusNumType){
                    CusNumType.SHOW_DETAIL -> {
                        adultNum.visibility = View.VISIBLE
                        childNum.visibility = View.VISIBLE
                        count.visibility = View.GONE
                    }
                    CusNumType.SHOW_TOTAL -> {
                        adultNum.visibility = View.GONE
                        childNum.visibility = View.GONE
                        count.visibility = View.VISIBLE
                    }
                }

                adult.text = wait.adultCount.toString()
                child.text = wait.kidCount.toString()


                waitNo.text = wait.tkey.substring(wait.tkey.length-3,wait.tkey.length)



                if(wait.isExpend){
                    tvMemo.text = wait.memo
                    tvMemo.visibility = View.VISIBLE
                }else{
                    tvMemo.visibility = View.GONE
                }


               root.setBackgroundColor(context.resources.getColor(R.color.selectColor))

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
                        tvStatus.text = context.resources.getString(R.string.notified)
                        tvUpdateTime.text = wait.updateDate
                        tvStatus.textColor = context.resources.getColor(R.color.deepGray)
                        tvUpdateTime.textColor = context.resources.getColor(R.color.deepGray)
                        statusBlock.visibility = View.VISIBLE
                        if(wait.isOverTime){
                            tvStatus.textColor = context.resources.getColor(R.color.red)
                            tvUpdateTime.textColor = context.resources.getColor(R.color.red)
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
                        tvStatus.textColor = context.resources.getColor(R.color.red)
                        tvUpdateTime.textColor = context.resources.getColor(R.color.red)
                    }
                }


                var hasMemo = (wait.memo != null && wait.memo != "")
                var hasDelivery = (wait.orderNo != null && wait.orderNo != "")

                if(hasMemo){
                    imgMemo.visibility = View.VISIBLE
                }else{
                    imgMemo.visibility = View.INVISIBLE
                }


                if(hasDelivery){
                    imgDelivery.visibility = View.VISIBLE
                }else{
                    imgDelivery.visibility = View.INVISIBLE
                }

                imgMemo.setOnClickListener {
                    if(tvMemo.visibility == View.GONE) {
                        wait.isExpend = true
                        tvMemo.visibility = View.VISIBLE
                    }else{
                        wait.isExpend = false
                        tvMemo.visibility = View.GONE
                    }
                    onImgClick(wait)
                }


                root.setOnClickListener {
                    onItemClick(wait)
                }

                imgDelivery.setOnClickListener {
                    onDeliveryClick(wait)
                }

                btnCheck.setOnClickListener {
                    onButtonClick(wait)
                }

                btnNotice.setOnClickListener {
                    onNoticeClick(wait)
                }
            }
        }



        private fun recoveryItemView(){
            binding.apply {
                checkBlock.visibility = View.VISIBLE
                btnNotice.visibility = View.VISIBLE
                statusBlock.visibility = View.GONE
            }

        }
    }
}