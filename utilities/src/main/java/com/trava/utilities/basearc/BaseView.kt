package com.trava.utilities.basearc

interface BaseView {
    fun showLoader(isLoading: Boolean)
    fun apiFailure()
    fun handleApiError(code: Int?, error: String?)
}