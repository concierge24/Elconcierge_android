package com.codebrew.clikat.module.loyaltyPoints

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class LoyaltyPointsProvider {

    @ContributesAndroidInjector
    abstract fun loyaltyPointsFactory(): LoyaltyPointsFragment

}

