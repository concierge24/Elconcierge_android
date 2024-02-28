package com.trava.user.ui.home.stories

import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.earnings.AdsEarningResponse
import com.trava.user.webservices.models.storybonus.ResultItem
import com.trava.user.webservices.models.stories.StoriesPojo
import com.trava.user.webservices.models.storybonus.StoryBonusPojo
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoriesPresenter : BasePresenterImpl<StoriesContract.View>(), StoriesContract.Presenter {

    override fun requestStories(map : HashMap<String,Any>) {
        getView()?.showLoader(true)
        RestClient.get().getStories(map).enqueue(object : Callback<StoriesPojo<ArrayList<com.trava.user.webservices.models.stories.ResultItem>>> {

            override fun onResponse(call: Call<StoriesPojo<ArrayList<com.trava.user.webservices.models.stories.ResultItem>>>?, response: Response<StoriesPojo<ArrayList<com.trava.user.webservices.models.stories.ResultItem>>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onApiSuccess(it as ArrayList<com.trava.user.webservices.models.stories.ResultItem>) }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    val errorModel = getApiError(errorBody)
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<StoriesPojo<ArrayList<com.trava.user.webservices.models.stories.ResultItem>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun saveStoryBonus(map : HashMap<String,Any>) {
        getView()?.showLoader(true)
        RestClient.get().saveStoryBonous(map).enqueue(object : Callback<StoryBonusPojo> {

            override fun onResponse(call: Call<StoryBonusPojo>?, response: Response<StoryBonusPojo>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.let { getView()?.onBonusApiSuccess(it) }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    val errorModel = getApiError(errorBody)
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<StoryBonusPojo>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun watchStoryWeb(story_id: String) {
        getView()?.showLoader(true)
        RestClient.get().websiteClick(story_id).enqueue(object : Callback<ApiResponseNew> {

            override fun onResponse(call: Call<ApiResponseNew>?, response: Response<ApiResponseNew>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.let { getView()?.onStoryWebsiteApiSuccess(it?.msg?:"") }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    val errorModel = getApiError(errorBody)
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseNew>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }
}