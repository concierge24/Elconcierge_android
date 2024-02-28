package com.codebrew.clikat.module.rental.boat_rental.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.SlotData
import com.codebrew.clikat.data.model.others.RentalDayModel
import com.codebrew.clikat.databinding.DgflowItemAddressBinding
import com.codebrew.clikat.databinding.ItemAvailDateBinding
import com.codebrew.clikat.databinding.ItemBoatslotBinding
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SlotsAdapter : ListAdapter<SlotItem, RecyclerView.ViewHolder>(AdrsListDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    private lateinit var mCallback: SlotListener

    fun settingCallback(mCallback: SlotListener) {
        this.mCallback = mCallback
    }

    fun submitItemList(list: List<SlotData>?) {
        adapterScope.launch {
            val items = list?.map { SlotItem.AddonItem(it) }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SlotViewHolder -> {
                val modelItem = getItem(position) as SlotItem.AddonItem
                holder.bind(modelItem.mDayData, mCallback)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SlotViewHolder.from(parent)
    }
}


class SlotViewHolder private constructor(val binding: ItemBoatslotBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlotData, listener: SlotListener) {

        binding.slotItem = item
        binding.listener = listener
        binding.color = Configurations.colors
        binding.currency = AppConstants.CURRENCY_SYMBOL

        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): SlotViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemBoatslotBinding.inflate(layoutInflater, parent, false)
            return SlotViewHolder(binding)
        }
    }
}


class AdrsListDiffCallback : DiffUtil.ItemCallback<SlotItem>() {
    override fun areItemsTheSame(oldItem: SlotItem, newItem: SlotItem): Boolean {
        return false
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: SlotItem, newItem: SlotItem): Boolean {
        return false
    }
}

class SlotListener(val clickListener: (rentalModel: SlotData) -> Unit) {
    fun adrslick(slot: SlotData) = clickListener(slot)
}


sealed class SlotItem {
    data class AddonItem(val mDayData: SlotData) : SlotItem() {
        override val dayData = mDayData
    }

    abstract val dayData: SlotData
}

