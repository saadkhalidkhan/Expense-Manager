package com.droidgeeks.expensemanager.view.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.datastore.UIModeImpl
import com.droidgeeks.expensemanager.data.local.model.Transaction
import com.droidgeeks.expensemanager.databinding.FragmentDashboardBinding
import com.droidgeeks.expensemanager.services.exportcsv.CreateCsvContract
import com.droidgeeks.expensemanager.services.exportcsv.OpenCsvContract
import com.droidgeeks.expensemanager.utils.action
import com.droidgeeks.expensemanager.utils.hide
import com.droidgeeks.expensemanager.utils.show
import com.droidgeeks.expensemanager.utils.snack
import com.droidgeeks.expensemanager.utils.usdCurrencyConvertor
import com.droidgeeks.expensemanager.utils.viewState.ExportState
import com.droidgeeks.expensemanager.utils.viewState.ViewState
import com.droidgeeks.expensemanager.view.adapter.TransactionAdapter
import com.droidgeeks.expensemanager.view.base.BaseFragment
import com.droidgeeks.expensemanager.view.dashboard.listener.IDashboard
import com.droidgeeks.expensemanager.view.main.viewmodel.TransactionViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding, TransactionViewModel>(), IDashboard {
    private lateinit var transactionAdapter: TransactionAdapter
    override val viewModel: TransactionViewModel by activityViewModels()

    @Inject
    lateinit var themeManager: UIModeImpl

    private val csvCreateRequestLauncher =
        registerForActivityResult(CreateCsvContract()) { uri: Uri? ->
            if (uri != null) {
                exportCSV(uri)
            } else {
                binding.root.snack(
                    string = R.string.failed_transaction_export
                )
            }
        }

    private val previewCsvRequestLauncher = registerForActivityResult(OpenCsvContract()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRV()
        initViews()
        observeFilter()
        observeTransaction()
        swipeToDelete()
    }

    private fun observeFilter() = with(binding) {
        viewModel.visibleBackPress.observe(viewLifecycleOwner) { visible ->
            if (!visible) {
                binding.btnAddTransaction.visibility = View.VISIBLE
                viewModel.overall()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.transactionFilter.collect { filter ->
                when (filter) {
                    "Overall" -> {
                        totalBalanceView.totalBalanceTitle.text =
                            getString(R.string.text_total_balance)
                        totalIncomeExpenseView.show()
                        incomeCardView.totalTitle.text = getString(R.string.text_total_income)
                        expenseCardView.totalTitle.text = getString(R.string.text_total_expense)
                        expenseCardView.totalIcon.setImageResource(R.drawable.ic_expense)
                    }

                    "Income" -> {
                        totalBalanceView.totalBalanceTitle.text =
                            getString(R.string.text_total_income)
                        totalIncomeExpenseView.hide()
                    }

                    "Expense" -> {
                        totalBalanceView.totalBalanceTitle.text =
                            getString(R.string.text_total_expense)
                        totalIncomeExpenseView.hide()
                    }
                }
                viewModel.getAllTransaction(filter)
            }
        }
    }

    private fun setupRV() = with(binding) {
        transactionAdapter = TransactionAdapter()
        transactionRv.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun swipeToDelete() {
        // init item touch callback for swipe action
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // get item position & delete notes
                val position = viewHolder.adapterPosition
                val transaction = transactionAdapter.differ.currentList[position]
                val transactionItem = Transaction(
                    transaction.title,
                    transaction.amount,
                    transaction.transactionType,
                    transaction.tag,
                    transaction.date,
                    transaction.note,
                    transaction.createdAt,
                    transaction.id,
                    transaction.tagIcon
                )
                viewModel.deleteTransaction(transactionItem)
                Snackbar.make(
                    binding.root,
                    getString(R.string.success_transaction_delete),
                    Snackbar.LENGTH_LONG
                )
                    .apply {
                        setAction(getString(R.string.text_undo)) {
                            viewModel.insertTransaction(
                                transactionItem
                            )
                        }
                        show()
                    }
            }
        }

        // attach swipe callback to rv
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.transactionRv)
        }
    }

    private fun onTotalTransactionLoaded(transaction: List<Transaction>) = with(binding) {
        val (totalIncome, totalExpense) = transaction.partition { it.transactionType == "Income" }
        val income = totalIncome.sumOf { it.amount }
        val expense = totalExpense.sumOf { it.amount }
        incomeCardView.total.text = "+ ".plus(usdCurrencyConvertor(income))
        expenseCardView.total.text = "- ".plus(usdCurrencyConvertor(expense))
        totalBalanceView.totalBalance.text = usdCurrencyConvertor(income - expense)
    }

    private fun observeTransaction() = lifecycleScope.launchWhenStarted {
        viewModel.uiState.collect { uiState ->
            when (uiState) {
                is ViewState.Loading -> {
                }

                is ViewState.Success -> {
                    showAllViews()
                    onTransactionLoaded(uiState.transaction)
                    onTotalTransactionLoaded(uiState.transaction)
                }

                is ViewState.Error -> {
                    binding.root.snack(
                        string = R.string.text_error
                    )
                }

                is ViewState.Empty -> {
                    hideAllViews()
                }
            }
        }
    }

    private fun showAllViews() = with(binding) {
        dashboardGroup.show()
        emptyStateLayout.emptyStateView.visibility = View.GONE
        transactionRv.show()
    }

    private fun hideAllViews() = with(binding) {
        dashboardGroup.hide()
        emptyStateLayout.emptyStateView.visibility = View.VISIBLE
    }

    private fun onTransactionLoaded(list: List<Transaction>) =
        transactionAdapter.differ.submitList(list)

    private fun initViews() = with(binding) {
        clickListener = this@DashboardFragment
        btnAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addTransactionFragment)
        }

        mainDashboardScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, sX, sY, oX, oY ->
                if (abs(sY - oY) > 10) {
                    when {
                        sY > oY -> btnAddTransaction.hide()
                        oY > sY -> btnAddTransaction.show()
                    }
                }
            }
        )

        transactionAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("transaction", it)
            }
            findNavController().navigate(
                R.id.action_dashboardFragment_to_transactionDetailsFragment,
                bundle
            )
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDashboardBinding.inflate(inflater, container, false)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ui, menu)

//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                lifecycleScope.launchWhenStarted {
//                    when (position) {
//                        0 -> {
//                            viewModel.overall()
//                            (view as TextView).setTextColor(resources.getColor(R.color.black))
//                        }
//
//                        1 -> {
//                            viewModel.allIncome()
//                            (view as TextView).setTextColor(resources.getColor(R.color.black))
//                        }
//
//                        2 -> {
//                            viewModel.allExpense()
//                            (view as TextView).setTextColor(resources.getColor(R.color.black))
//                        }
//                    }
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                lifecycleScope.launchWhenStarted {
//                    viewModel.overall()
//                }
//            }
//        }

        // Set the item state
        lifecycleScope.launchWhenStarted {
            val isChecked = viewModel.getUIMode.first()
            val uiMode = menu.findItem(R.id.action_night_mode)
            uiMode.isChecked = isChecked
            setUIMode(uiMode, isChecked)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        return when (item.itemId) {
            R.id.action_night_mode -> {
                item.isChecked = !item.isChecked
                setUIMode(item, item.isChecked)
                true
            }

            R.id.action_export -> {
                val csvFileName = "expense_manager_${System.currentTimeMillis()}"
                csvCreateRequestLauncher.launch(csvFileName)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportCSV(csvFileUri: Uri) {
        viewModel.exportTransactionsToCsv(csvFileUri)
        lifecycleScope.launchWhenCreated {
            viewModel.exportCsvState.collect { state ->
                when (state) {
                    ExportState.Empty -> {
                        /*do nothing*/
                    }

                    is ExportState.Error -> {
                        binding.root.snack(
                            string = R.string.failed_transaction_export
                        )
                    }

                    ExportState.Loading -> {
                        /*do nothing*/
                    }

                    is ExportState.Success -> {
                        binding.root.snack(string = R.string.success_transaction_export) {
                            action(text = R.string.text_open) {
                                previewCsvRequestLauncher.launch(state.fileUri)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isStoragePermissionGranted(): Boolean =
        isStorageReadPermissionGranted() && isStorageWritePermissionGranted()

    private fun isStorageWritePermissionGranted(): Boolean = ContextCompat
        .checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private fun isStorageReadPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun setUIMode(item: MenuItem, isChecked: Boolean) {
        if (isChecked) {
            viewModel.setDarkMode(true)
            item.setIcon(R.drawable.ic_night)
        } else {
            viewModel.setDarkMode(false)
            item.setIcon(R.drawable.ic_day)
        }
    }

    override fun onClickIncome() {
        viewModel.visibleBackPress.value = true
        binding.btnAddTransaction.visibility = View.GONE
        viewModel.allIncome()
    }

    override fun onClickExpense() {
        viewModel.visibleBackPress.value = true
        binding.btnAddTransaction.visibility = View.GONE
        viewModel.allExpense()
    }
}
