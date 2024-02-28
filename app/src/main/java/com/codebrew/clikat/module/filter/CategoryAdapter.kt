package com.codebrew.clikat.module.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.CategoryItemBinding
import com.codebrew.clikat.modal.other.English
import com.codebrew.clikat.modal.other.FilterEvent
import com.codebrew.clikat.module.filter.BottomSheetFragment.Companion.varientData
import com.codebrew.clikat.module.filter.CategoryAdapter.ViewHolder
import com.codebrew.clikat.module.filter.category_selection.CategoryFragment.OnListFragmentInteractionListener
import com.codebrew.clikat.utils.configurations.Configurations
import org.greenrobot.eventbus.EventBus

class CategoryAdapter(private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<ViewHolder>() {
    private var mSelectedPosition = -1
    private var mCategoryList: ArrayList<English>?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: CategoryItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.category_item, parent, false)
        binding.color = Configurations.colors
        val view = binding.root
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rbCategory.text = mCategoryList?.get(position)?.name
        holder.rbCategory.isChecked = varientData.catId == mCategoryList?.get(position)?.id
        /* if(categoryList.get(position).getId()==FilterScreenActivity.varientData.getCatId())
        {
            holder.rbCategory.setChecked(true);
        }*/
    }

    override fun getItemCount(): Int {
        return mCategoryList?.size?:0
    }

    fun updateCategoryList(categoryList: ArrayList<English>)
    {
        this.mCategoryList=categoryList
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var rbCategory: RadioButton
        override fun toString(): String {
            return super.toString() + " '" + rbCategory.text + "'"
        }

        init {
            rbCategory = view.findViewById(R.id.rb_category)
            rbCategory.setOnClickListener { v: View? ->
                // if (null != mListener) {
                mSelectedPosition = adapterPosition
                //  mListener.onCategoryInteraction(categoryList.get(getAdapterPosition()).getId(),categoryList.get(getAdapterPosition()).getName(),0);
                EventBus.getDefault().post(FilterEvent(AppConstants.CATEGORY_SELECT, mCategoryList?.get(adapterPosition)?.id
                        ?: 0,
                    mCategoryList?.get(adapterPosition)?.name
                                ?: "", 0, mCategoryList?.get(adapterPosition)?.sub_category))
                notifyDataSetChanged()
            }
        }
    }

}