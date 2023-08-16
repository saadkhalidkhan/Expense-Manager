package com.droidgeeks.expensemanager.view.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.model.Transaction
import com.droidgeeks.expensemanager.data.local.model.TransactionListModel
import com.droidgeeks.expensemanager.databinding.FragmentAddTransactionBinding
import com.droidgeeks.expensemanager.utils.Constants.transactionTags
import com.droidgeeks.expensemanager.utils.parseDouble
import com.droidgeeks.expensemanager.utils.snack
import com.droidgeeks.expensemanager.utils.transformIntoDatePicker
import com.droidgeeks.expensemanager.view.adapter.TransactionCategoryItemAdapter
import com.droidgeeks.expensemanager.view.base.BaseFragment
import com.droidgeeks.expensemanager.view.main.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class AddTransactionFragment :
    BaseFragment<FragmentAddTransactionBinding, TransactionViewModel>() {

    private lateinit var transactionAdapter: TransactionCategoryItemAdapter
    override val viewModel: TransactionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupRV()
    }

    private fun initViews() {

        with(binding) {

            // Transform TextInputEditText to DatePicker using Ext function
            addTransactionLayout.etWhen.transformIntoDatePicker(
                requireContext(),
                "dd/MM/yyyy",
                Date()
            )
            btnSaveTransaction.setOnClickListener {
                binding.addTransactionLayout.apply {
                    val (title, amount, transactionType, tag, date, note) = getTransactionContent()
                    // validate if transaction content is empty or not
                    when {
                        transactionType.isEmpty() -> {
                            binding.root.snack(
                                string = R.string.select_transaction_type
                            )
                        }
                        title.isEmpty() -> {
                            this.etTitle.error = getString(R.string.title_must_not_be_empty)
                        }

                        amount.isNaN() -> {
                            this.etAmount.error = getString(R.string.amount_must_not_be_empty)
                        }
                        date.isEmpty() -> {
                            binding.root.snack(
                                string = R.string.date_must_not_be_empty
                            )
                        }
                        note.isEmpty() -> {
                            this.etNote.error = getString(R.string.note_must_not_be_empty)
                        }
                        tag.isEmpty() -> {
                            binding.root.snack(
                                string = R.string.tag_must_not_be_empty
                            )
                        }
                        else -> {
                            viewModel.insertTransaction(getTransactionContent()).run {
                                clearData()
                                binding.root.snack(
                                    string = R.string.success_expense_saved
                                )
                                findNavController().navigate(
                                    R.id.action_addTransactionFragment_to_dashboardFragment
                                )
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
    }

    private fun clearData() {
        selectedExpenseText = ""
        selectedModel = null
        transactionTags.forEach { it.isSelected = false }
    }

    private fun updateBackground(incomeDrawable: Int, expenseDrawable: Int) {
        binding.addTransactionLayout.rbIncome.setBackgroundResource(incomeDrawable)
        binding.addTransactionLayout.rbExpense.setBackgroundResource(expenseDrawable)
    }

    private var selectedModel: TransactionListModel? = null
    private var selectedExpenseText: String = ""

    private fun setupRV() = with(binding) {
        transactionAdapter = TransactionCategoryItemAdapter(requireContext(), transactionTags)
        addTransactionLayout.rvCategory.apply {
            layoutManager = GridLayoutManager(activity, 3)
            adapter = transactionAdapter
        }

        transactionAdapter.setOnItemClickListener { categoryModel ->
            selectedModel = categoryModel
            transactionTags.filter { it.isSelected }.forEach { it.isSelected = false }
            transactionTags.find { it.name == categoryModel.name }?.isSelected = true
            transactionAdapter.notifyDataSetChanged()
        }
    }

    private fun getTransactionContent(): Transaction = binding.addTransactionLayout.let {
        val title = it.etTitle.text.toString()
        val amount = parseDouble(it.etAmount.text.toString())
        val transactionType = selectedExpenseText
        val tag = if (selectedModel != null) requireContext().getString(selectedModel!!.name) else ""
        val date = it.etWhen.text.toString()
        val note = it.etNote.text.toString()

        return Transaction(title, amount, transactionType, tag, date, note, tagIcon = selectedModel?.icon ?: R.drawable.ic_others)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAddTransactionBinding.inflate(inflater, container, false)

    override fun onDetach() {
        super.onDetach()
        clearData()
    }
}
