package com.droidgeeks.expensemanager.utils.viewState

import com.droidgeeks.expensemanager.data.local.model.Transaction

sealed class DetailState {
    object Loading : DetailState()
    object Empty : DetailState()
    data class Success(val transaction: Transaction) : DetailState()
    data class Error(val exception: Throwable) : DetailState()
}
