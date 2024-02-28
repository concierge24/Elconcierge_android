package com.trava.utilities.basearc

interface BasePresenter<in V : BaseView>
{
    fun attachView(view: V)
    fun detachView()
}