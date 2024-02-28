package com.trava.user.ui.home.comfirmbooking.payment

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText

class FourDigitCardFormatWatcher(private var Edt: EditText, private var list: ArrayList<String>, private var cardInterface: CardInterface) : TextWatcher {
    private var value = ""
    private var keyDel = 0

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {
        value = s.toString()
        val ccNum = value.replace(" ", "")
        for (i in list.indices) {
            if (ccNum.matches(list[i].toRegex())) {
                Log.e("DEBUG", "afterTextChanged : discover + : " + i)
                cardInterface.getSelectedCardData(i)
                break
            } else {
                cardInterface.getSelectedCardData(10)
            }
        }
    }

    companion object {
        private val space = ' '
    }
}

interface CardInterface {
    fun getSelectedCardData(position: Int)
}