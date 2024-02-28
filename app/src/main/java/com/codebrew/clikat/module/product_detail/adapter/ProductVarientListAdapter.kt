package com.codebrew.clikat.module.product_detail.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.databinding.ItemFilterCheckcolorBinding
import com.codebrew.clikat.databinding.ItemFilterChecksizeBinding
import com.codebrew.clikat.modal.other.VariantValuesBean
import com.codebrew.clikat.utils.StaticFunction.varientColor
import com.codebrew.clikat.utils.Utils
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.button.MaterialButton

class ProductVarientListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val SIZE = 1
    private val COLOR = 2
    private var viewType: String? = null
    private val varientList: MutableList<VariantValuesBean> = ArrayList()

    //    private val varientFilteredList: MutableList<VariantValuesBean> = ArrayList()
    private var mVarientCallback: FilterVarientCallback? = null
    private var mposition = 0
    private var filterStatus = false
    private lateinit var context: Context


    fun settingCallback(context: Context, mVarientCallback: FilterVarientCallback?, viewType: String?, mChecklist: List<VariantValuesBean>?, mposition: Int, notify: Boolean) {
        varientList.clear()
        varientList.addAll(mChecklist!!)
        this.mVarientCallback = mVarientCallback
        this.viewType = viewType
        this.context = context
        this.mposition = mposition


    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        return if (i == SIZE) {
            val binding: ItemFilterChecksizeBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context),
                    R.layout.item_filter_checksize, viewGroup, false)
            binding.color = Configurations.colors
            ViewHolder(binding.root)
        } else {
            val binding: ItemFilterCheckcolorBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context),
                    R.layout.item_filter_checkcolor, viewGroup, false)
            binding.color = Configurations.colors
            ColorViewHolder(binding.root)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        val pos = viewHolder.adapterPosition
        if (viewHolder is ViewHolder) {
            val holder = viewHolder

            holder.tvName.text = varientList[pos].variant_value

            if (varientList[pos].isSelected) {

                holder.itemView.visibility = View.VISIBLE
                val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt());
                holder.itemView.layoutParams = params

                holder.tvName.setBackgroundColor(Color.parseColor(Configurations.colors.primaryColor))
                holder.tvName.setTextColor(Color.parseColor(Configurations.colors.appBackground))
                holder.tvName.isEnabled = false

                //checked

            } else {

                if (varientList[pos].isNotNeeded) {

                    holder.itemView.visibility = View.GONE
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)

                } else {

                    holder.itemView.visibility = View.VISIBLE
                    val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.setMargins(Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt());
                    holder.itemView.layoutParams = params

                    holder.tvName.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground))
                    holder.tvName.strokeColor = ColorStateList.valueOf(Color.parseColor(Configurations.colors.primaryColor))
                    holder.tvName.setTextColor(Color.parseColor(Configurations.colors.textHead))
                    holder.tvName.isEnabled = true

                }
                //unchecked

            }

        } else {
            val holder = viewHolder as ColorViewHolder

            holder.tvShape.background = varientColor(varientList[pos].variant_value!!, Configurations.colors.primaryColor!!, GradientDrawable.RADIAL_GRADIENT)


            if (varientList[pos].isSelected) {

                holder.itemView.visibility = View.VISIBLE
                val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt());
                holder.itemView.layoutParams = params

                holder.itemLayout.background = varientColor(Configurations.colors.appBackground, Configurations.colors.primaryColor!!, GradientDrawable.RECTANGLE)

                holder.tvShape.isEnabled = false

                //checked

            } else {

                if (varientList[pos].isNotNeeded) {

                    holder.itemView.visibility = View.GONE
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)

                } else {

                    holder.itemView.visibility = View.VISIBLE
                    val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.setMargins(Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt(), Utils.dpToPx(2).toInt());
                    holder.itemView.layoutParams = params

                    holder.itemLayout.background = varientColor(Configurations.colors.appBackground, Configurations.colors.app_light_bg, GradientDrawable.RECTANGLE)

                    holder.tvShape.isEnabled = true

                }
                //unchecked
            }
        }
    }

    override fun getItemCount(): Int {
        return varientList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (viewType == "color") COLOR else SIZE
    }


    interface FilterVarientCallback {
        fun onFilterVarient(variantValuesBean: VariantValuesBean?, varientId: String?, adpaterPosition: Int)
        fun onSelectedFlavor(position: Int, variantValuesBean: VariantValuesBean)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: MaterialButton

        init {
            tvName = itemView.findViewById(R.id.tv_name)
            /*  tvName.setOnClickListener { v: View? ->
                  if (adapterPosition != -1) {
                      filterData.put(mposition, varientList[adapterPosition].unid!!)
                      if (varientList[adapterPosition].filter_product_id != null) mVarientCallback!!.onFilterVarient(varientList[adapterPosition], varientList[adapterPosition].filter_product_id.toString(), mposition) else mVarientCallback!!.onFilterVarient(varientList[adapterPosition], varientList[adapterPosition].product_id.toString(), mposition)
                  }
              }*/

            tvName.setOnClickListener { v: View? ->
                if (adapterPosition != -1) {
                    mVarientCallback?.onSelectedFlavor(mposition, varientList[position])
                }
            }
        }
    }

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvShape: TextView
        var itemLayout: ConstraintLayout

        init {
            tvShape = itemView.findViewById(R.id.tv_shape)
            itemLayout = itemView.findViewById(R.id.itemLayout)
            itemLayout.setOnClickListener { v: View? ->
                if (adapterPosition != -1) {
                    mVarientCallback?.onSelectedFlavor(mposition, varientList[position])
                }
            }
        }
    }
}