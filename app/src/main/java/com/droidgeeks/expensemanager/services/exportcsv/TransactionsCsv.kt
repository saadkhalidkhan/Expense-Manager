package com.droidgeeks.expensemanager.services.exportcsv

import com.opencsv.bean.CsvBindByName
import com.droidgeeks.expensemanager.data.local.model.Transaction

data class TransactionsCSV(
    @CsvBindByName(column = "title")
    val title: String,
    @CsvBindByName(column = "amount")
    val amount: Int,
    @CsvBindByName(column = "transactionType")
    val transactionType: String,
    @CsvBindByName(column = "tag")
    val tag: String,
    @CsvBindByName(column = "date")
    val date: String,
    @CsvBindByName(column = "note")
    val note: String,
    @CsvBindByName(column = "createdAt")
    val createdAtDate: String
)

fun List<Transaction>.toCsv() = map {
    TransactionsCSV(
        title = it.title,
        amount = it.amount,
        transactionType = it.transactionType,
        tag = it.tag,
        date = it.date,
        note = it.note,
        createdAtDate = it.createdAtDateFormat,
    )
}
