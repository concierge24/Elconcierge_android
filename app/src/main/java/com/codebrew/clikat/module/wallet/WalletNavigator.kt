package com.codebrew.clikat.module.wallet

import com.codebrew.clikat.base.BaseInterface

interface WalletNavigator : BaseInterface {
    fun onMoneySent()
    fun onAddMoneyToWallet()
}
