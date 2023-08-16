package com.droidgeeks.expensemanager.utils

import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.model.TransactionListModel

object Constants {

    val transactionType = listOf("Income", "Expense")

    val transactionTags = arrayListOf(
        TransactionListModel(R.drawable.ic_bonus, R.string.bonus),
        TransactionListModel(R.drawable.ic_entertainment, R.string.entertainment),
        TransactionListModel(R.drawable.ic_food, R.string.food),
        TransactionListModel(R.drawable.ic_housing, R.string.housing),
        TransactionListModel(R.drawable.ic_healthcare, R.string.healthcare),
        TransactionListModel(R.drawable.ic_insurance, R.string.insurance),
        TransactionListModel(R.drawable.ic_personal_spending, R.string.personal_spending),
        TransactionListModel(R.drawable.ic_salary, R.string.salary),
        TransactionListModel(R.drawable.ic_savings, R.string.saving),
        TransactionListModel(R.drawable.ic_transport, R.string.transportation),
        TransactionListModel(R.drawable.ic_utilities, R.string.utilities),
        TransactionListModel(R.drawable.ic_others, R.string.other)
    )
}
