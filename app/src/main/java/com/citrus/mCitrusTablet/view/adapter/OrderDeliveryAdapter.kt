package com.citrus.mCitrusTablet.view.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.citrus.mCitrusTablet.databinding.RvDeliveryItemBinding
import com.citrus.mCitrusTablet.model.vo.OrdersItemDelivery
import com.citrus.mCitrusTablet.util.AsyncDiffUtil


class OrderDeliveryAdapter :RecyclerView.Adapter<OrderDeliveryAdapter.TasksViewHolder>() {

    private val asyncDiffUtil: AsyncDiffUtil<OrdersItemDelivery> = AsyncDiffUtil(this, object : DiffUtil.ItemCallback<OrdersItemDelivery>() {
        override fun areItemsTheSame(oldItem: OrdersItemDelivery, newItem: OrdersItemDelivery) = oldItem.isSame(newItem)

        override fun areContentsTheSame(oldItem: OrdersItemDelivery, newItem: OrdersItemDelivery): Boolean = oldItem.isContentSame(newItem)
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = RvDeliveryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun getItemCount() = asyncDiffUtil.current().size

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val item = asyncDiffUtil.current()[position]
            holder.bind(item,asyncDiffUtil.current(),position)
        }
    }

    fun update(items: List<OrdersItemDelivery>) {
        asyncDiffUtil.update(items)
    }

    inner class TasksViewHolder(private val binding: RvDeliveryItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(item: OrdersItemDelivery, currentList: List<OrdersItemDelivery>, position: Int) {
            binding.apply {
                flavorNameBlock.visibility =  View.GONE
                addNameBlock.visibility =  View.GONE
                gName.text = (position+1).toString()+". " + item.gname
                price.text = "$"+item.price.toString()
                qty.text = "x" + item.qty

                if(item.addName.isNotEmpty() && item.addName != ""){
                    addNameBlock.visibility =  View.VISIBLE
                    addName.text = item.addName
                }else{
                    addNameBlock.visibility = View.GONE
                }

                if(item.flavorName.isNotEmpty() && item.flavorName != ""){
                    flavorNameBlock.visibility =  View.VISIBLE
                    flavorName.text = item.flavorName
                }else{
                    flavorNameBlock.visibility = View.GONE
                }
            }
        }
    }
}