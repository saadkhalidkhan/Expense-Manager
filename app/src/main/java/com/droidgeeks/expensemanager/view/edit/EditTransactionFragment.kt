package com.droidgeeks.expensemanager.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.model.Transaction
import com.droidgeeks.expensemanager.data.local.model.TransactionListModel
import com.droidgeeks.expensemanager.databinding.FragmentEditTransactionBinding
import com.droidgeeks.expensemanager.utils.Constants
import com.droidgeeks.expensemanager.utils.parseDouble
import com.droidgeeks.expensemanager.utils.snack
import com.droidgeeks.expensemanager.utils.transformIntoDatePicker
import com.droidgeeks.expensemanager.view.adapter.TransactionCategoryItemAdapter
import com.droidgeeks.expensemanager.view.base.BaseFragment
import com.droidgeeks.expensemanager.view.main.viewmodel.TransactionViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class EditTransactionFragment : BaseFragment<FragmentEditTransactionBinding, TransactionViewModel>() {

    private val args: EditTransactionFragmentArgs by navArgs()
    override val viewModel: TransactionViewModel by activityViewModels()
    private lateinit var transactionAdapter: TransactionCategoryItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // receiving bundles here
        val transaction = Gson().fromJson(args.transaction,Transaction::class.java)
        initViews()
        setupRV()
        loadData(transaction)
    }

    private fun loadData(transaction: Transaction) = with(binding) {
        addTransactionLayout.etTitle.setText(transaction.title)
        addTransactionLayout.etAmount.setText(transaction.amount.toString())
//        addTransactionLayout.etTransactionType.setText(transaction.transactionType, false)
//        addTransactionLayout.etTag.setText(transaction.tag, false)
        addTransactionLayout.etWhen.setText(transaction.date)
        addTransactionLayout.etNote.setText(transaction.note)

        setTitle(transaction)

        selectedCategory = transaction.tag
        selectedCategoryIcon = transaction.tagIcon

        Constants.transactionTags.find {
            getString(it.name).equals(
                transaction.tag,
                ignoreCase = true
            )
        }?.isSelected = true
        transactionAdapter.notifyDataSetChanged()
    }

    private fun setTitle(transaction: Transaction) {
        if (transaction.transactionType.equals(
                requireContext().getString(R.string.income),
                ignoreCase = true
            )
        ) {
            selectedExpenseText = requireContext().getString(R.string.income)
            updateBackground(
                R.drawable.bg_select_left,
                R.drawable.bg_non_select_right
            )
        } else {
            selectedExpenseText = requireContext().getString(R.string.expense)
            updateBackground(
                R.drawable.bg_non_select_left,
                R.drawable.bg_select_right
            )
        }
    }

    private var selectedCategory: String? = ""
    private var selectedCategoryIcon: Int? = R.drawable.ic_others
    private var selectedExpenseText: String = ""

    private fun initViews() = with(binding) {

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
                            clearData()
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

    private fun setupRV() = with(binding) {
        transactionAdapter = TransactionCategoryItemAdapter(requireContext(), Constants.transactionTags)
        addTransactionLayout.rvCategory.apply {
            layoutManager = GridLayoutManager(activity, 3)
            adapter = transactionAdapter
        }

        transactionAdapter.setOnItemClickListener { categoryModel ->
            selectedCategory = getString(categoryModel.name)
            selectedCategoryIcon = categoryModel.icon
            setCategoryItem(categoryModel)
            transactionAdapter.notifyDataSetChanged()
        }
    }

    private fun setCategoryItem(categoryModel: TransactionListModel) {
        Constants.transactionTags.filter { it.isSelected }.forEach { it.isSelected = false }
        Constants.transactionTags.find { it.name == categoryModel.name }?.isSelected = true
    }

    private fun updateBackground(incomeDrawable: Int, expenseDrawable: Int) {
        binding.addTransactionLayout.rbIncome.setBackgroundResource(incomeDrawable)
        binding.addTransactionLayout.rbExpense.setBackgroundResource(expenseDrawable)
    }

    private fun getTransactionContent(): Transaction = binding.addTransactionLayout.let {

        val transaction = Gson().fromJson(args.transaction,Transaction::class.java)
        val id = transaction.id
        val title = it.etTitle.text.toString()
        val amount = parseDouble(it.etAmount.text.toString())
        val transactionType = selectedExpenseText
        val tag = if (selectedCategory != null) selectedCategory!! else ""
        val date = it.etWhen.text.toString()
        val note = it.etNote.text.toString()
        val icon = selectedCategoryIcon

        return Transaction(
            title = title,
            amount = amount,
            transactionType = transactionType,
            tag = tag,
            date = date,
            note = note,
            createdAt = System.currentTimeMillis(),
            id = id,
            tagIcon = icon ?: R.drawable.ic_others
        )
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEditTransactionBinding.inflate(inflater, container, false)

    private fun clearData() {
        selectedExpenseText = ""
        selectedCategory = null
        Constants.transactionTags.forEach { it.isSelected = false }
    }

    override fun onDetach() {
        super.onDetach()
        clearData()
    }

}
