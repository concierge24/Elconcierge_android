package com.codebrew.clikat.module.dialog_adress.interfaces

import com.codebrew.clikat.data.model.api.AddressBean

interface AddressDialogListener {
    fun onAddressSelect(adrsBean: AddressBean)
    fun onDestroyDialog()
}