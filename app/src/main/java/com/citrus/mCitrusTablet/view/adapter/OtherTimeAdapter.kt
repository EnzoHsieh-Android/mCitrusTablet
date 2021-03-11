package com.citrus.mCitrusTablet.view.adapter



import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.Floor
import com.citrus.mCitrusTablet.model.vo.OrderDateDatum
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import kotlinx.android.synthetic.main.seatitemview.view.*
import kotlinx.android.synthetic.main.timetitle.view.*



class OtherTimeAdapter(
    private val header: String,
    private val item: List<OrderDateDatum>,
    private val onButtonClick:(String) -> Unit
) :
    Section(
        SectionParameters.builder()
            .itemResourceId(R.layout.seatitemview)
            .headerResourceId(R.layout.timetitle)
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


    override fun onBindItemViewHolder(holder: ViewHolder, position: Int) {
        val itemHolder = holder as ItemViewHolder
        var orderDateDatum = item[position]
        var time = orderDateDatum.resDate.split(" ")
        itemHolder.btnTable.text = time[1]

        itemHolder.btnTable.setOnClickListener {
            onButtonClick(orderDateDatum.resDate.replace("-","/"))
        }

    }

    internal inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        val headerText: TextView = itemView.time
    }

    internal inner class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
        val btnTable:Button = itemView.btn_Seat
    }

}