package com.codebrew.clikat.module.cart

import com.codebrew.clikat.module.cart.addproduct.AddProductDialog
import com.codebrew.clikat.module.cart.v2.CartV2
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class CartProvider {

    @ContributesAndroidInjector
    abstract fun provideCartFactory(): Cart


    @ContributesAndroidInjector
    abstract fun provideCartV2Factory(): CartV2

    @ContributesAndroidInjector
    abstract fun provideAddProductFactory(): AddProductDialog
}
