package com.citrus.mCitrusTablet.view.adapter


import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.view.reservation.CancelFilter
import com.citrus.mCitrusTablet.view.reservation.CusNumType
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import kotlinx.android.synthetic.main.rv_reservation_item.view.*
import kotlinx.android.synthetic.main.rv_title_item.view.*
import java.text.SimpleDateFormat


class ReservationAdapter(
    val activity: FragmentActivity,
    private val header: String,
    private val cancelFilter: CancelFilter,
    private var cusNumType: CusNumType,
    private val index:Int,
    private val item: List<ReservationGuests>,
    private val onItemClick: (ReservationGuests) -> Unit,
    private val onButtonClick: (ReservationGuests) -> Unit,
    private val onFilterClick: () -> Unit,
    private val onChangeTypeClick: (CusNumType) -> Unit
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


        if(index == 0){
            when(cancelFilter){
                CancelFilter.SHOW_CANCELLED -> {headerHolder.tvFilterType.text = activity.getString(R.string.filter_cancelled)}
                CancelFilter.HIDE_CANCELLED -> {headerHolder.tvFilterType.text = activity.getString(R.string.filter_cancelled_H)}
            }
            headerHolder.statusFilter.visibility = View.VISIBLE
            headerHolder.statusFilter.setOnClickListener {
                onFilterClick()
            }


            var changeValue:CusNumType = when(cusNumType){
                CusNumType.SHOW_DETAIL -> {
                    headerHolder.tv_changeType.text = activity.resources.getString(R.string.showDetail)
                    CusNumType.SHOW_TOTAL
                }
                CusNumType.SHOW_TOTAL -> {
                    headerHolder.tv_changeType.text = activity.resources.getString(R.string.showTotal)
                    CusNumType.SHOW_DETAIL
                }
            }

            headerHolder.changeType.visibility = View.VISIBLE
            headerHolder.changeType.setOnClickListener {
                onChangeTypeClick(changeValue)
                cusNumType = changeValue
            }

        }else{
            headerHolder.statusFilter.visibility = View.GONE
            headerHolder.changeType.visibility = View.GONE
        }

        headerHolder.headerText.text = header


    }

    fun getCurrentList(): List<ReservationGuests> {
        return item
    }


    override fun onBindItemViewHolder(holder: ViewHolder, position: Int) {
        val itemHolder = holder as ItemViewHolder


        var guest = item[position]


        when(cusNumType){
            CusNumType.SHOW_DETAIL -> {
                itemHolder.adultBlock.visibility = View.VISIBLE
                itemHolder.childBlock.visibility = View.VISIBLE
                itemHolder.itemCount.visibility = View.GONE
            }
            CusNumType.SHOW_TOTAL -> {
                itemHolder.adultBlock.visibility = View.GONE
                itemHolder.childBlock.visibility = View.GONE
                itemHolder.itemCount.visibility = View.VISIBLE
            }
        }

        itemHolder.itemRoot.setBackgroundColor(Color.WHITE)
        itemHolder.itemName.text = guest.mName
        if(itemHolder.itemName.text.length>6){
            val textSize = activity.resources.getDimensionPixelSize(R.dimen.sp_8)
            itemHolder.itemName.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize.toFloat())
        }
        itemHolder.itemPhone.text = guest.phone
        if(guest.phone != null && guest.phone != ""){
            itemHolder.itemEmail.visibility = View.GONE
            itemHolder.itemPhone.visibility = View.VISIBLE
            itemHolder.itemPhone.text = guest.phone
        }else{
            itemHolder.itemEmail.visibility = View.VISIBLE
            itemHolder.itemPhone.visibility = View.GONE
            itemHolder.itemEmail.text = guest.email!!.split("@")[0]
        }

        itemHolder.itemSeats.text = guest.floorName+"-"+guest.roomName
        itemHolder.itemCount.text = guest.custNum.toString()

        itemHolder.tvAdult.text = guest.adultCount.toString()
        itemHolder.tvChild.text = guest.kidCount.toString()

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
            itemHolder.tvMemo.text = guest.memo.toString()
            itemHolder.imgMemo.visibility = View.VISIBLE
        }else{
            itemHolder.imgMemo.visibility = View.INVISIBLE
        }

        if(guest.isNew){
            itemHolder.hintNew.visibility = View.VISIBLE
        }else{
            itemHolder.hintNew.visibility = View.GONE
        }
        guest.isNew = false

        if(guest.isExpend){
            itemHolder.tvMemo.visibility = View.VISIBLE
        }else{
            itemHolder.tvMemo.visibility = View.GONE
        }

        itemHolder.imgMemo.setOnClickListener {
                if(itemHolder.tvMemo.visibility == View.GONE) {
                    guest.isExpend = true
                    itemHolder.tvMemo.visibility = View.VISIBLE
                }else{
                    guest.isExpend = false
                    itemHolder.tvMemo.visibility = View.GONE
                }
            onItemClick(item[position])
        }

        itemHolder.itemRoot.setOnClickListener {
            onItemClick(item[position])
        }

        when(guest.status){
            "A" -> {
                itemHolder.statusBlock.visibility = View.GONE
                itemHolder.btnCheck.visibility = View.VISIBLE
                itemHolder.tvCheck.visibility = View.GONE
            }

            "C" -> {
                itemHolder.statusBlock.visibility = View.GONE
                itemHolder.btnCheck.visibility = View.GONE
                itemHolder.tvCheck.visibility = View.VISIBLE

                if(itemHolder.tvCheck.text.length > 5){
                    val textSize = activity.resources.getDimensionPixelSize(R.dimen.sp_6)
                    itemHolder.tvCheck.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize.toFloat())
                }
            }

            "D" -> {
                var formattedDate: String = if(guest.updateDate.length < 12){
                    guest.updateDate
                }else{
                    outputFormat.format(inputFormat.parse(guest.updateDate))
                }

                itemHolder.btnCheck.visibility = View.GONE
                itemHolder.tvCheck.visibility = View.GONE
                itemHolder.tvUpdateTime.text = formattedDate.split(" ")[0] + "\n" + formattedDate.split(" ")[1]
                itemHolder.statusBlock.visibility = View.VISIBLE
            }
        }

        itemHolder.btnCheck.setOnClickListener {
            onButtonClick(item[position])
        }

    }

    internal inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        val headerText: TextView = itemView.time
        val statusFilter:LinearLayout = itemView.statusFilter
        val tvFilterType:TextView = itemView.tv_filterType
        val changeType:LinearLayout = itemView.changeType
        val tv_changeType:TextView = itemView.tv_changeType
    }

    internal inner class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
        val itemRoot: ConstraintLayout = itemView.dataBlock
        val itemName: TextView = itemView.name
        val itemPhone: TextView = itemView.phone
        val itemEmail: TextView = itemView.mail
        val itemSeats: TextView = itemView.seats
        val itemCount: TextView = itemView.count
        val itemReservationTime: TextView = itemView.reservationTime
        val imgMemo:ImageView = itemView.img_memo
        val btnCheck:Button = itemView.btn_check
        val tvCheck:Button = itemView.tv_check
        val tvAdult:TextView = itemView.adult
        val tvChild:TextView = itemView.child
        val tvMemo:TextView = itemView.tvMemo
        val statusBlock:LinearLayout = itemView.statusBlock
        val tvUpdateTime:TextView = itemView.tv_updateTime
        val tvStatus:TextView = itemView.tv_status
        val hintNew:ImageView = itemView.new_hint
        val adultBlock:LinearLayout = itemView.adultNum
        val childBlock:LinearLayout = itemView.childNum
    }

}