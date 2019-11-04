package com.blogspot.android_czy_java.beautytips.di.usecase.detail

import com.blogspot.android_czy_java.beautytips.di.repository.detail.DetailRepositoryModule
import com.blogspot.android_czy_java.beautytips.di.usecase.account.AccountUseCaseModule
import com.blogspot.android_czy_java.beautytips.repository.forViewModels.detail.RecipeDetailRepository
import com.blogspot.android_czy_java.beautytips.repository.forViewModels.recipe.UserListRecipeRepository
import com.blogspot.android_czy_java.beautytips.usecase.account.GetCurrentUserUseCase
import com.blogspot.android_czy_java.beautytips.usecase.comment.GetCommentNumberUseCase
import com.blogspot.android_czy_java.beautytips.usecase.detail.LoadDescriptionFragmentDataUseCase
import com.blogspot.android_czy_java.beautytips.usecase.detail.LoadHeaderFragmentDataUseCase
import com.blogspot.android_czy_java.beautytips.usecase.detail.LoadHeartDataForHeaderFragmentUseCase
import com.blogspot.android_czy_java.beautytips.usecase.detail.LoadIngredientsUseCase
import dagger.Module
import dagger.Provides

@Module(includes = [
    DetailRepositoryModule::class,
    AccountUseCaseModule::class
])
class DetailUseCaseModule {

    @Provides
    fun provideLoadBaseDetailUseCase(recipeDetailRepository: RecipeDetailRepository) =
            LoadDescriptionFragmentDataUseCase(recipeDetailRepository)

    @Provides
    fun provideLoadHeaderFragmentDataUseCase(recipeDetailRepository: RecipeDetailRepository) =
            LoadHeaderFragmentDataUseCase(recipeDetailRepository)

    @Provides
    fun provideLoadHeartDataForHeaderFragmentUseCase(recipeDetailRepository: RecipeDetailRepository,
                                                     userListRecipeRepository: UserListRecipeRepository,
                                                     getCurrentUserUseCase: GetCurrentUserUseCase) =
            LoadHeartDataForHeaderFragmentUseCase(
                    recipeDetailRepository,
                    userListRecipeRepository,
                    getCurrentUserUseCase
            )

    @Provides
    fun provideLoadIngredientsUseCase(recipeDetailRepository: RecipeDetailRepository) =
            LoadIngredientsUseCase(recipeDetailRepository)

}