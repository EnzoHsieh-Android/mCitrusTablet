package com.citrus.mCitrusTablet.view.adapter


import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import kotlinx.android.synthetic.main.rv_reservation_item.view.*
import kotlinx.android.synthetic.main.rv_title_item.view.*
import java.text.SimpleDateFormat


class ReservationAdapter(
    val activity: FragmentActivity,
    private val header: String,
    private val item: List<ReservationGuests>,
    private val onItemClick: (ReservationGuests,Boolean) -> Unit,
    private val onButtonClick: (ReservationGuests) -> Unit
) :
    Section(
        SectionParameters.builder()
            .itemResourceId(R.layout.rv_reservation_item)
            .headerResourceId(R.layout.rv_title_item)
            .build()
    ) {

    override fun getContentItemsTotal(): Int {
        return item.size
    }

    override fun getHeaderViewHolder(view: View): ViewHolder {
        return HeaderViewHolder(view)
    }

    override fun getItemViewHolder(view: View): ViewHolder {
        return ItemViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: ViewHolder) {
        val headerHolder = holder as HeaderViewHolder
        headerHolder.headerText.text = header
    }

    fun getCurrentList(): List<ReservationGuests> {
        return item
    }


    override fun onBindItemViewHolder(holder: ViewHolder, position: Int) {
        val itemHolder = holder as ItemViewHolder


        var guest = item[position]
        itemHolder.itemRoot.setBackgroundColor(Color.WHITE)
        itemHolder.itemName.text = guest.mName
        itemHolder.itemPhone.text = guest.phone
        itemHolder.itemSeats.text = guest.floorName+"-"+guest.roomName
        itemHolder.itemCount.text = guest.custNum.toString()

        if(guest.isSelect){
            itemHolder.itemRoot.setBackgroundColor(activity.resources.getColor(R.color.selectColor))
        }else{
            itemHolder.itemRoot.setBackgroundColor(Color.WHITE)
        }

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val outputFormat = SimpleDateFormat("MM/dd HH:mm")
        val date = inputFormat.parse(guest.reservationTime)
        val formattedDate = outputFormat.format(date)
        var timeStr = formattedDate.split(" ")
        itemHolder.itemReservationTime.text = timeStr[1]

        var hasMemo = (guest.memo != null && guest.memo != "")

        if(hasMemo){
            itemHolder.imgMemo.visibility = View.VISIBLE
        }else{
            itemHolder.imgMemo.visibility = View.INVISIBLE
        }

        itemHolder.itemRoot.setOnClickListener {
            onItemClick(item[position],hasMemo)
        }

        when(guest.status){
            "A" -> {
                itemHolder.btnCheck.visibility = View.VISIBLE
                itemHolder.tvCheck.visibility = View.GONE
            }

            "C" -> {
                itemHolder.btnCheck.visibility = View.GONE
                itemHolder.tvCheck.visibility = View.VISIBLE
            }
        }

        itemHolder.btnCheck.setOnClickListener {
            onButtonClick(item[position])
        }

    }

    internal inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        val headerText: TextView = itemView.time
    }

    internal inner class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
        val itemRoot: ConstraintLayout = itemView.dataBlock
        val itemName: TextView = itemView.name
        val itemPhone: TextView = itemView.phone
        val itemSeats: TextView = itemView.seats
        val itemCount: TextView = itemView.count
        val itemReservationTime: TextView = itemView.reservationTime
        val imgMemo:ImageView = itemView.img_memo
        val btnCheck:Button = itemView.btn_check
        val tvCheck:TextView = itemView.tv_check
    }

}