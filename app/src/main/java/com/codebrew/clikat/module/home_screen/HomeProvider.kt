package com.codebrew.clikat.module.home_screen

import com.codebrew.clikat.module.home_screen.resturant_home.wagon_pickup.WagonPickUp
import com.codebrew.clikat.module.home_screen.suppliers.SupplierListingFragment
import com.codebrew.clikat.module.home_screen.suppliers.SuppliersMapFragment
import com.codebrew.clikat.module.home_screen.viewAll.ViewAllCategoriesFragment
import com.codebrew.clikat.module.home_screen.viewAll.ViewAllSuppliersFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class HomeProvider {

    @ContributesAndroidInjector
    abstract fun provideHomeFactory(): HomeFragment


    @ContributesAndroidInjector
    abstract fun supplierListFactory():SupplierListingFragment

    @ContributesAndroidInjector
    abstract fun viewAllSuppliersList():ViewAllSuppliersFragment

    @ContributesAndroidInjector
    abstract fun viewAllCategoryList():ViewAllCategoriesFragment

    @ContributesAndroidInjector
    abstract fun provideWagonPickupFactory(): WagonPickUp
}
