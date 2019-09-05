package com.blogspot.android_czy_java.beautytips.di.view.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.blogspot.android_czy_java.beautytips.di.core.ViewModelKey
import com.blogspot.android_czy_java.beautytips.di.livedata.LiveDataModule
import com.blogspot.android_czy_java.beautytips.di.usecase.account.AccountUseCaseModule
import com.blogspot.android_czy_java.beautytips.di.view.detail.DetailActivityModule
import com.blogspot.android_czy_java.beautytips.livedata.common.NetworkNeededNotAvailableLiveData
import com.blogspot.android_czy_java.beautytips.usecase.account.login.LoginUseCase
import com.blogspot.android_czy_java.beautytips.usecase.account.userlist.CreateUserListRequestsUseCase
import com.blogspot.android_czy_java.beautytips.usecase.account.userlist.LoadRecipesFromUserListUseCase
import com.blogspot.android_czy_java.beautytips.view.account.UserListFragment
import com.blogspot.android_czy_java.beautytips.viewmodel.account.UserListViewModel
import com.blogspot.android_czy_java.beautytips.viewmodel.detail.DetailActivityViewModel
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [
    UserListFragmentModule.ProvideViewModel::class,
    AccountUseCaseModule::class,
    LiveDataModule::class
])
abstract class UserListFragmentModule {

    @ContributesAndroidInjector(modules = [
        UserListFragmentModule.InjectViewModel::class,
        DetailActivityModule.ProvideViewModel::class
    ])
    abstract fun bind(): UserListFragment

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(UserListViewModel::class)
        fun provideUserListViewModel(createUserListRequestsUseCase: CreateUserListRequestsUseCase,
                                     loadRecipesFromUserListUseCase: LoadRecipesFromUserListUseCase,
                                     networkNeededNotAvailableLiveData: NetworkNeededNotAvailableLiveData,
                                     loginUseCase: LoginUseCase): ViewModel =
                UserListViewModel(createUserListRequestsUseCase, loadRecipesFromUserListUseCase, networkNeededNotAvailableLiveData, loginUseCase)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideUserListViewModel(
                factory: ViewModelProvider.Factory,
                target: UserListFragment
        ): UserListViewModel =
                ViewModelProviders.of(target, factory).get(UserListViewModel::class.java)

        @Provides
        fun provideDetailActivityViewModel(
                factory: ViewModelProvider.Factory,
                target: UserListFragment
        ): DetailActivityViewModel =
                ViewModelProviders.of(target.requireActivity(), factory).get(DetailActivityViewModel::class.java)
    }


}