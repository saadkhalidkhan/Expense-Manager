package com.droidgeeks.expensemanager.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.model.Transaction
import com.droidgeeks.expensemanager.databinding.ItemTransactionLayoutBinding
import com.droidgeeks.expensemanager.utils.usdCurrencyConvertor

class TransactionAdapter(private val context: Context) :
    RecyclerView.Adapter<TransactionAdapter.TransactionVH>() {

    inner class TransactionVH(val binding: ItemTransactionLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionVH {
        val binding =
            ItemTransactionLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionVH(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TransactionVH, position: Int) {

        val item = differ.currentList[position]
        holder.binding.apply {

            transactionName.text = item.title
            transactionCategory.text = item.tag
            transactionDate.text = item.date

            when (item.transactionType) {
                context.resources.getString(R.string.income) -> {
                    transactionAmount.setTextColor(
                        ContextCompat.getColor(
                            transactionAmount.context,
                            R.color.income
                        )
                    )

                    transactionAmount.text = "+ ".plus(usdCurrencyConvertor(item.amount))
                }

                context.resources.getString(R.string.expense) -> {
                    transactionAmount.setTextColor(
                        ContextCompat.getColor(
                            transactionAmount.context,
                            R.color.expense
                        )
                    )
                    transactionAmount.text = "- ".plus(usdCurrencyConvertor(item.amount))
                }
            }
            transactionIconView.setImageResource(item.tagIcon)

            // on item click
            holder.itemView.setOnClickListener {
                onItemClickListener?.let { it(item) }
            }
        }
    }

    // on item click listener
    private var onItemClickListener: ((Transaction) -> Unit)? = null
    fun setOnItemClickListener(listener: (Transaction) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
