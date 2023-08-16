package com.droidgeeks.expensemanager.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.model.TransactionListModel
import com.droidgeeks.expensemanager.databinding.ItemAddtransactionLayoutBinding

class TransactionCategoryItemAdapter(
    val context: Context,
    val categoryList: ArrayList<TransactionListModel>
) :
    RecyclerView.Adapter<TransactionCategoryItemAdapter.TransactionVH>() {

    inner class TransactionVH(val binding: ItemAddtransactionLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionVH {
        val binding =
            ItemAddtransactionLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionVH(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: TransactionVH, position: Int) {

        val item = categoryList[position]
        holder.binding.apply {
            transactionName.text = context.getString(item.name)
            transactionIconView.setImageResource(item.icon)
            if (item.isSelected) {
                updateCardViewBackground(this, R.color.dark_surface, R.color.surface)
            } else {
                updateCardViewBackground(this, R.color.surface, android.R.color.transparent)
            }
            holder.itemView.setOnClickListener {
                onItemClickListener?.let { it(item) }
            }
        }

    }

    private fun updateCardViewBackground(
        binding: ItemAddtransactionLayoutBinding,
        bgColor: Int,
        strokeColor: Int
    ) {
        binding.transactionCardView.strokeColor = ContextCompat.getColor(context, strokeColor)
        binding.transactionCardView.setCardBackgroundColor(
            ContextCompat.getColor(
                context,
                bgColor
            )
        )
    }

    // on item click listener
    private var onItemClickListener: ((TransactionListModel) -> Unit)? = null
    fun setOnItemClickListener(listener: (TransactionListModel) -> Unit) {
        onItemClickListener = listener
    }
}
