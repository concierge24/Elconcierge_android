package com.trava.user.ui.home.stories

import com.trava.user.webservices.models.storybonus.ResultItem
import com.trava.user.webservices.models.storybonus.StoryBonusPojo
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class StoriesContract{
    interface View: BaseView{
        fun onApiSuccess(responseData : ArrayList<com.trava.user.webservices.models.stories.ResultItem>)
        fun onBonusApiSuccess(responseData: StoryBonusPojo)
        fun onStoryWebsiteApiSuccess(msg: String)
    }

    interface Presenter: BasePresenter<View>{
        fun requestStories(map:HashMap<String,Any>)
        fun saveStoryBonus(map:HashMap<String,Any>)
        fun watchStoryWeb(story_id:String)
    }
}