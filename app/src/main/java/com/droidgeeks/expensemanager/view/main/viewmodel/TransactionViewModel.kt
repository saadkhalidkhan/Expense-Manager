package com.droidgeeks.expensemanager.view.main.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.app.ExpenseManager
import com.droidgeeks.expensemanager.data.local.datastore.UIModeImpl
import com.droidgeeks.expensemanager.data.local.model.Transaction
import com.droidgeeks.expensemanager.repo.TransactionRepo
import com.droidgeeks.expensemanager.services.exportcsv.ExportCsvService
import com.droidgeeks.expensemanager.services.exportcsv.toCsv
import com.droidgeeks.expensemanager.utils.viewState.DetailState
import com.droidgeeks.expensemanager.utils.viewState.ExportState
import com.droidgeeks.expensemanager.utils.viewState.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val exportService: ExportCsvService,
    private val uiModeDataStore: UIModeImpl,
    private val context: ExpenseManager
) : ViewModel() {

    // state for export csv status
    private val _exportCsvState = MutableStateFlow<ExportState>(ExportState.Empty)
    val exportCsvState: StateFlow<ExportState> = _exportCsvState

    private val _transactionFilter = MutableStateFlow(context.resources.getString(R.string.overall))
    val transactionFilter: StateFlow<String> = _transactionFilter

    var visibleBackPress: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _uiState = MutableStateFlow<ViewState>(ViewState.Loading)
    private val _detailState = MutableStateFlow<DetailState>(DetailState.Loading)

    // UI collect from this stateFlow to get the state updates
    val uiState: StateFlow<ViewState> = _uiState
    val detailState: StateFlow<DetailState> = _detailState

    // get ui mode
    val getUIMode = uiModeDataStore.uiMode

    // save ui mode
    fun setDarkMode(isNightMode: Boolean) {
        viewModelScope.launch(IO) {
            uiModeDataStore.saveToDataStore(isNightMode)
        }
    }

    // export all Transactions to csv file
    fun exportTransactionsToCsv(csvFileUri: Uri) = viewModelScope.launch {
        _exportCsvState.value = ExportState.Loading
        transactionRepo
            .getAllTransactions()
            .flowOn(Dispatchers.IO)
            .map { it.toCsv() }
            .flatMapMerge { exportService.writeToCSV(csvFileUri, it) }
            .catch { error ->
                _exportCsvState.value = ExportState.Error(error)
            }.collect { uriString ->
                _exportCsvState.value = ExportState.Success(uriString)
            }
    }

    // insert transaction
    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepo.insert(transaction)
    }

    // update transaction
    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepo.update(transaction)
    }

    // delete transaction
    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepo.delete(transaction)
    }

    // get all transaction
    fun getAllTransaction(type: String) = viewModelScope.launch {
        transactionRepo.getAllSingleTransaction(type, context).collect { result ->
            if (result.isNullOrEmpty()) {
                _uiState.value = ViewState.Empty
            } else {
                _uiState.value = ViewState.Success(result)
                Log.i("Filter", "Transaction filter is ${transactionFilter.value}")
            }
        }
    }

    // get transaction by id
    fun getByID(id: Int) = viewModelScope.launch {
        _detailState.value = DetailState.Loading
        transactionRepo.getByID(id).collect { result: Transaction? ->
            if (result != null) {
                _detailState.value = DetailState.Success(result)
            }
        }
    }

    // delete transaction
    fun deleteByID(id: Int) = viewModelScope.launch {
        transactionRepo.deleteByID(id)
    }

    fun allIncome() {
        _transactionFilter.value = context.resources.getString(R.string.income)
    }

    fun allExpense() {
        _transactionFilter.value = context.resources.getString(R.string.expense)
    }

    fun overall() {
        _transactionFilter.value = context.resources.getString(R.string.overall)
    }
}
