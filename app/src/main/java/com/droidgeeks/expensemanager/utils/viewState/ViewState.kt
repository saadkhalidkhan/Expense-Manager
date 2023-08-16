package com.droidgeeks.expensemanager.utils.viewState

import com.droidgeeks.expensemanager.data.local.model.Transaction

sealed class ViewState {
    object Loading : ViewState()
    object Empty : ViewState()
    data class Success(val transaction: List<Transaction>) : ViewState()
    data class Error(val exception: Throwable) : ViewState()
}
