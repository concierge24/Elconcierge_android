package com.codebrew.clikat.module.essentialHome

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ServiceListProvider {

    @ContributesAndroidInjector
    abstract fun provideServiceListFactory(): ServiceListFragment
}
