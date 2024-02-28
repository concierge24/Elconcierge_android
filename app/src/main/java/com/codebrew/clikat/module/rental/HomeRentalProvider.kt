package com.codebrew.clikat.module.rental

import com.codebrew.clikat.module.rental.boat_rental.BoatRentalFrag
import com.codebrew.clikat.module.rental.boat_rental.ChooseSlot
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class HomeRentalProvider {

    @ContributesAndroidInjector
    abstract fun provideHomeFactory(): HomeRental

    @ContributesAndroidInjector
    abstract fun provideBoatRentalFactory(): BoatRentalFrag

    @ContributesAndroidInjector
    abstract fun provideChooseSlotFactory(): ChooseSlot
}
