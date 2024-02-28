package com.codebrew.clikat.module.rental.rental_checkout

import com.codebrew.clikat.base.BaseInterface
import java.util.ArrayList

interface CheckoutNavigator : BaseInterface {

    fun onOrderPlaced(data: ArrayList<Int>)
}
