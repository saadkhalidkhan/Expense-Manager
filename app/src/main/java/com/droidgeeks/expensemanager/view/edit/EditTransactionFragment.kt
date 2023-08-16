package com.droidgeeks.expensemanager.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.model.Transaction
import com.droidgeeks.expensemanager.data.local.model.TransactionListModel
import com.droidgeeks.expensemanager.databinding.FragmentEditTransactionBinding
import com.droidgeeks.expensemanager.utils.parseDouble
import com.droidgeeks.expensemanager.utils.snack
import com.droidgeeks.expensemanager.utils.transformIntoDatePicker
import com.droidgeeks.expensemanager.view.base.BaseFragment
import com.droidgeeks.expensemanager.view.main.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class EditTransactionFragment : BaseFragment<FragmentEditTransactionBinding, TransactionViewModel>() {
    private val args: EditTransactionFragmentArgs by navArgs()
    override val viewModel: TransactionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // receiving bundles here
        val transaction = args.transaction
        initViews()
        loadData(transaction)
    }

    private fun loadData(transaction: Transaction) = with(binding) {
        addTransactionLayout.etTitle.setText(transaction.title)
        addTransactionLayout.etAmount.setText(transaction.amount.toString())
//        addTransactionLayout.etTransactionType.setText(transaction.transactionType, false)
//        addTransactionLayout.etTag.setText(transaction.tag, false)
        addTransactionLayout.etWhen.setText(transaction.date)
        addTransactionLayout.etNote.setText(transaction.note)
    }

    private var selectedModel: TransactionListModel? = null
    private var selectedExpenseText: String = ""

    private fun initViews() = with(binding) {

        // Set list to TextInputEditText adapter
//        addTransactionLayout.etTransactionType.setAdapter(transactionTypeAdapter)
//        addTransactionLayout.etTag.setAdapter(tagsAdapter)

        // Transform TextInputEditText to DatePicker using Ext function
        addTransactionLayout.etWhen.transformIntoDatePicker(
            requireContext(),
            "dd/MM/yyyy",
            Date()
        )
        btnSaveTransaction.setOnClickListener {
            binding.addTransactionLayout.apply {
                val (title, amount, transactionType, tag, date, note) =
                    getTransactionContent()
                // validate if transaction content is empty or not
                when {
                    title.isEmpty() -> {
                        this.etTitle.error = getString(R.string.title_must_not_be_empty)
                    }

                    amount.isNaN() -> {
                        this.etAmount.error = getString(R.string.amount_must_not_be_empty)
                    }

                    transactionType.isEmpty() -> {
                        binding.root.snack(
                            string = R.string.select_transaction_type
                        )
                    }

                    tag.isEmpty() -> {
                        binding.root.snack(
                            string = R.string.tag_must_not_be_empty
                        )
                    }

                    date.isEmpty() -> {
                        this.etWhen.error = getString(R.string.date_must_not_be_empty)
                    }

                    note.isEmpty() -> {
                        this.etNote.error = getString(R.string.note_must_not_be_empty)
                    }

                    else -> {
                        viewModel.updateTransaction(getTransactionContent()).also {

                            binding.root.snack(
                                string = R.string.success_expense_saved
                            ).run {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
        this.addTransactionLayout.rgExpense.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                this.addTransactionLayout.rbIncome.id -> {
                    selectedExpenseText = requireContext().getString(R.string.income)
                    updateBackground(
                        R.drawable.bg_select_left,
                        R.drawable.bg_non_select_right
                    )
                }

                this.addTransactionLayout.rbExpense.id -> {
                    selectedExpenseText = requireContext().getString(R.string.expense)
                    updateBackground(
                        R.drawable.bg_non_select_left,
                        R.drawable.bg_select_right
                    )
                }
            }

        }
    }

    private fun updateBackground(incomeDrawable: Int, expenseDrawable: Int) {
        binding.addTransactionLayout.rbIncome.setBackgroundResource(incomeDrawable)
        binding.addTransactionLayout.rbExpense.setBackgroundResource(expenseDrawable)
    }

    private fun getTransactionContent(): Transaction = binding.addTransactionLayout.let {

        val id = args.transaction.id
        val title = it.etTitle.text.toString()
        val amount = parseDouble(it.etAmount.text.toString())
        val transactionType = selectedExpenseText
        val tag = if (selectedModel != null) requireContext().getString(selectedModel!!.name) else ""
        val date = it.etWhen.text.toString()
        val note = it.etNote.text.toString()

        return Transaction(
            title = title,
            amount = amount,
            transactionType = transactionType,
            tag = tag,
            date = date,
            note = note,
            createdAt = System.currentTimeMillis(),
            id = id
        )
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEditTransactionBinding.inflate(inflater, container, false)
}
