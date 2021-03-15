package com.citrus.mCitrusTablet.view.adapter



import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.Floor
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import kotlinx.android.synthetic.main.rv_seat_item.view.*
import kotlinx.android.synthetic.main.rv_title_item.view.*


class OtherSeatAdapter(
    private val header: String,
    private val item: List<Floor>,
    private val onButtonClick:(Floor,String) -> Unit
) :
    Section(
        SectionParameters.builder()
            .itemResourceId(R.layout.rv_seat_item)
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
        var time = header.split(" ")
        headerHolder.headerText.text = time[1]
    }


    override fun onBindItemViewHolder(holder: ViewHolder, position: Int) {
        val itemHolder = holder as ItemViewHolder
        var seat = item[position]
        itemHolder.btnTable.text = seat.floorName + "-" + seat.roomName

        itemHolder.btnTable.setOnClickListener {
            onButtonClick(item[position],header)
        }

    }

    internal inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        val headerText: TextView = itemView.time
    }

    internal inner class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
        val btnTable:Button = itemView.btn_Seat
    }

}