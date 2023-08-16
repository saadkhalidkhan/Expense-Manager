package com.droidgeeks.expensemanager.repo

import com.droidgeeks.expensemanager.data.local.dao.TransactionDao
import com.droidgeeks.expensemanager.data.local.model.Transaction
import javax.inject.Inject

class TransactionRepo @Inject constructor(private val appTransactionDao: TransactionDao) {

    // insert transaction
    suspend fun insert(transaction: Transaction) = appTransactionDao.insertTransaction(
        transaction
    )

    // update transaction
    suspend fun update(transaction: Transaction) = appTransactionDao.updateTransaction(
        transaction
    )

    // delete transaction
    suspend fun delete(transaction: Transaction) = appTransactionDao.deleteTransaction(
        transaction
    )

    // get all transaction
    fun getAllTransactions() = appTransactionDao.getAllTransactions()

    // get single transaction type - Expense or Income or else overall
    fun getAllSingleTransaction(transactionType: String) = if (transactionType == "Overall") {
        getAllTransactions()
    } else {
        appTransactionDao.getAllSingleTransaction(transactionType)
    }

    // get transaction by ID
    fun getByID(id: Int) = appTransactionDao.getTransactionByID(id)

    // delete transaction by ID
    suspend fun deleteByID(id: Int) = appTransactionDao.deleteTransactionByID(id)
}
