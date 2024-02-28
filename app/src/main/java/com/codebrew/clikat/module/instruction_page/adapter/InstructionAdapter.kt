package com.codebrew.clikat.module.instruction_page.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.CommonUtils.Companion.hide
import com.codebrew.clikat.app_utils.CommonUtils.Companion.visible
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.databinding.LayoutPagerSkipBinding
import com.codebrew.clikat.databinding.PagerInstructionBinding
import com.codebrew.clikat.databinding.PagerInstructionV2Binding
import com.codebrew.clikat.databinding.PagerInstructionV3Binding
import com.codebrew.clikat.modal.other.InstructionDtaModel
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.layout_pager_skip.view.*
import kotlinx.android.synthetic.main.pager_instruction.view.*
import kotlinx.android.synthetic.main.pager_instruction_v2.view.*
import kotlinx.android.synthetic.main.pager_instruction_v2.view.btn_next_v2
import kotlinx.android.synthetic.main.pager_instruction_v3.view.*

class InstructionAdapter(private var mContext: Context, private val type: Int,
                         private val appUtils: AppUtils,
                         private val clientInfo: SettingModel.DataBean.SettingData?,
                         private val tutorialScreens: ArrayList<SettingModel.DataBean.TutorialItem>) : PagerAdapter() {

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    private val mLayoutInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val modelList: MutableList<InstructionDtaModel> = ArrayList()
    private var mCallback: InstructionCallback? = null

    // private var mContext:Context?=null
    fun settingCallback(mCallback: InstructionCallback?) {
        this.mCallback = mCallback
    }

    override fun getCount(): Int = modelList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding: ViewDataBinding =
                when {
                    clientInfo?.show_ecom_v2_theme == "1" -> {
                        DataBindingUtil.inflate(mLayoutInflater, R.layout.pager_instruction_v2, container, false) as PagerInstructionV2Binding
                    }
                    clientInfo?.is_skip_theme == "1" -> {
                        DataBindingUtil.inflate(mLayoutInflater, R.layout.layout_pager_skip, container, false) as LayoutPagerSkipBinding
                    }
                    clientInfo?.is_pickup_primary == "1" -> {
                        DataBindingUtil.inflate(mLayoutInflater, R.layout.pager_instruction_v3, container, false) as PagerInstructionV3Binding
                    }
                    else -> {
                        DataBindingUtil.inflate(mLayoutInflater, R.layout.pager_instruction, container, false) as PagerInstructionBinding
                    }
                }

        binding.root.tag = position
        val view = binding.root


        when (binding) {
            is PagerInstructionV2Binding -> {
                binding.color = Configurations.colors
                binding.drawables = Configurations.drawables
                binding.strings = appUtils.loadAppConfig(0).strings

                if (tutorialScreens.isNotEmpty() && clientInfo?.is_tutorial_screen_enable == "1")
                    Glide.with(mContext).load(modelList[position].imageUrl).into(view.iv_instruction_v2)
                else
                    view.iv_instruction_v2.setImageResource(modelList[position].image ?: 0)

                view.tv_title_v2.text = modelList[position].title
                view.tv_body_v2.text = modelList[position].body
                view.btn_next_v2.setOnClickListener { v: View? -> mCallback?.onNextButton(position) }
            }
            is LayoutPagerSkipBinding -> {
                binding.color = Configurations.colors
                binding.drawables = Configurations.drawables
                binding.strings = appUtils.loadAppConfig(0).strings

                if (tutorialScreens.isNotEmpty() && clientInfo?.is_tutorial_screen_enable == "1")
                    Glide.with(mContext).load(modelList[position].imageUrl).into(view.ivInstructions)
                else
                    view.ivInstructions.setImageResource(modelList[position].image ?: 0)

                view.tvTitle.text = modelList[position].title
                view.tvDescription.text = modelList[position].body
                view.btn_next_v2.setOnClickListener { v: View? -> mCallback?.onNextButton(position) }
            }
            is PagerInstructionBinding -> {
                binding.color = Configurations.colors
                binding.drawables = Configurations.drawables
                binding.strings = appUtils.loadAppConfig(0).strings
                if (tutorialScreens.isNotEmpty() && clientInfo?.is_tutorial_screen_enable == "1")
                    Glide.with(mContext).load(modelList[position].imageUrl).into(view.iv_instruction)
                else
                    view.iv_instruction.setImageResource(modelList[position].image ?: 0)
                view.tv_title.text = modelList[position].title
                view.tv_body.text = modelList[position].body
                if ((!tutorialScreens.isNullOrEmpty() && clientInfo?.is_tutorial_screen_enable == "1") || BuildConfig.CLIENT_CODE == "pulluppfood_0372") {
                    view.animation_view.visibility = View.INVISIBLE
                } else {
                    view.animation_view.visibility = View.VISIBLE
                }

                if (clientInfo?.is_wagon_app == "1") {
                    view.btn_change_lang?.visible()
                    mCallback?.onChangeLang()
                } else {
                    view.btn_change_lang?.hide()
                }

                view.btn_change_lang?.setOnClickListener {
                    mCallback?.onChangeLang()
                }
                view.btn_next?.setOnClickListener { v: View? -> mCallback?.onNextButton(position) }
            }

            is PagerInstructionV3Binding -> {
                binding.color = Configurations.colors
                binding.drawables = Configurations.drawables
                binding.strings = appUtils.loadAppConfig(0).strings
                if (tutorialScreens.isNotEmpty() && clientInfo?.is_tutorial_screen_enable == "1")
                    Glide.with(mContext).load(modelList[position].imageUrl).into(view.ivIntro)
                else
                    view.ivIntro.setImageResource(modelList[position].image ?: 0)
                view.tvIntroTitle.text = modelList[position].title
                view.tvTitleV2.text = modelList[position].body
                view.tvIntoDescription?.text = modelList[position].description
                view.tvIntroDescShort?.text = modelList[position].description2
            }
        }

        if ((clientInfo?.show_ecom_v2_theme == null || clientInfo.show_ecom_v2_theme == "0") &&
                (clientInfo?.is_pickup_primary == null || clientInfo.is_pickup_primary == "0")) {
            when (type) {
                1 -> view.animation_view?.setAnimation("food_ripple.json")
                2 -> view.animation_view?.setAnimation("market_ripple.json")
                4 -> view.animation_view?.setAnimation("home_ripple.json")
                5 -> view.animation_view?.setAnimation("cnstrction_ripple.json")
                6 -> view.animation_view?.setAnimation("party_ripple.json")
                else -> view.animation_view?.setAnimation("home_ripple.json")
            }
            view.animation_view?.playAnimation()
        }

        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        when (`object`) {
            is FrameLayout -> {
                container.removeView(`object`)
            }
            is ConstraintLayout -> {
                container.removeView(`object`)
            }
        }
    }

    interface InstructionCallback {
        fun onChangeLang()
        fun onNextButton(position: Int)
    }

    init {
        val imagelist: IntArray
        val headerlist: Array<String>
        val titlelist: Array<String>
        val descriptionList: Array<String>
        val description2List: Array<String>
        when (type) {
            AppDataType.Food.type -> {
                if (clientInfo?.is_pickup_primary == "1") {
                    headerlist = mContext.resources?.getStringArray(R.array.ecom_instruct_header_v3) as Array<String>
                    titlelist = mContext.resources?.getStringArray(R.array.ecom_instruct_title_v3) as Array<String>
                    imagelist = intArrayOf(R.drawable.img_1, R.drawable.img_2, R.drawable.img_3)
                    descriptionList = mContext.resources?.getStringArray(R.array.ecom_description_v3) as Array<String>
                    description2List = mContext.resources?.getStringArray(R.array.ecom_description_short_v3) as Array<String>
                } else {
                    headerlist = if (clientInfo?.is_skip_theme == "1")
                        mContext.resources?.getStringArray(R.array.skip_food_instruct_header) as Array<String>
                    else
                        mContext.resources?.getStringArray(R.array.food_instruct_header) as Array<String>

                    titlelist = mContext.resources?.getStringArray(R.array.food_instruct_title) as Array<String>
                    titlelist[0] = mContext.getString(R.string.food_instruction_first, textConfig?.suppliers)
                    imagelist = if (BuildConfig.CLIENT_CODE == "pulluppfood_0372") {
                        intArrayOf(R.drawable.instruction_pullup_1, R.drawable.instruction_pullup_2, R.drawable.instruction_pullup_3)
                    } else if (BuildConfig.CLIENT_CODE == "poneeex_0049") {
                        intArrayOf(R.drawable.ic_poneex, R.drawable.ic_poneex, R.drawable.ic_poneex)
                    } else if (clientInfo?.is_skip_theme == "1")
                        intArrayOf(R.drawable.skip_walkthrough_3, R.drawable.skip_walkthrough_2, R.drawable.skip_walkthrough_1)
                    else {
                        intArrayOf(R.drawable.instruction_food_1, R.drawable.instruction_food_2, R.drawable.instruction_food_3)
                    }
                    descriptionList = emptyArray()
                    description2List = emptyArray()
                }
            }
            AppDataType.Ecom.type -> {

                if (clientInfo?.show_ecom_v2_theme == "1") {
                    headerlist = mContext.resources?.getStringArray(R.array.ecom_instruct_header_v2) as Array<String>
                    titlelist = mContext.resources?.getStringArray(R.array.ecom_instruct_title_v2) as Array<String>
                    imagelist = intArrayOf(R.drawable.img_1, R.drawable.img_2, R.drawable.img_3)
                    descriptionList = emptyArray()
                    description2List = emptyArray()
                } else {
                    headerlist = mContext.resources?.getStringArray(R.array.ecom_instruct_header) as Array<String>
                    titlelist = mContext.resources?.getStringArray(R.array.ecom_instruct_title) as Array<String>
                    imagelist = intArrayOf(R.drawable.instruction_market_1, R.drawable.instruction_market_2, R.drawable.instruction_market_3)
                    descriptionList = emptyArray()
                    description2List = emptyArray()
                }

            }
            AppDataType.HomeServ.type -> {
                headerlist = mContext.resources?.getStringArray(R.array.ecom_instruct_header) as Array<String>
                titlelist = mContext.resources?.getStringArray(R.array.ecom_instruct_title) as Array<String>
                imagelist = intArrayOf(R.drawable.instruction_service_1, R.drawable.instruction_service_2, R.drawable.instruction_service_3)
                descriptionList = emptyArray()
                description2List = emptyArray()
            }
            else -> {
                headerlist = mContext.resources?.getStringArray(R.array.ecom_instruct_header) as Array<String>
                titlelist = mContext.resources?.getStringArray(R.array.ecom_instruct_title) as Array<String>
                imagelist = intArrayOf(R.drawable.instruction_ecommerce_1, R.drawable.instruction_ecommerce_2, R.drawable.instruction_ecommerce_3)
                descriptionList = emptyArray()
                description2List = emptyArray()
            }
        }

        if (tutorialScreens.isNotEmpty() && clientInfo?.is_tutorial_screen_enable == "1") {
            tutorialScreens.forEach {
                modelList.add(InstructionDtaModel(it.tutorial_title, it.tutorial_text, null, null, "", it.tutorial_image))
            }
        } else {
            if (descriptionList.isNotEmpty()) {
                modelList.add(InstructionDtaModel(headerlist[0], titlelist[0], imagelist[0], descriptionList[0], description2List[0], ""))
                modelList.add(InstructionDtaModel(headerlist[1], titlelist[1], imagelist[1], descriptionList[1], description2List[1], ""))
                modelList.add(InstructionDtaModel(headerlist[2], titlelist[2], imagelist[2], descriptionList[2], description2List[2], ""))
            } else {
                modelList.add(InstructionDtaModel(headerlist[0], titlelist[0], imagelist[0], "", "", ""))
                modelList.add(InstructionDtaModel(headerlist[1], titlelist[1], imagelist[1], "", "", ""))
                modelList.add(InstructionDtaModel(headerlist[2], titlelist[2], imagelist[2], "", "", ""))
            }
        }
    }
}