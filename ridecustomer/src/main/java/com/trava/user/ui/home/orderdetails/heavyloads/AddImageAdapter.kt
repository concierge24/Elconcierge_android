package com.trava.user.ui.home.orderdetails.heavyloads

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.databinding.ItemAddImageHeaderBinding
import com.trava.user.databinding.ItemAddImagesBinding
import com.trava.user.utils.ConfigPOJO
import kotlinx.android.synthetic.main.item_add_images.view.*

class AddImageAdapter(private val imageList: ArrayList<String>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 1
    private val TYPE_HEADER = 2

    lateinit var context: Context

    private var mCallback: AddImageCallback? = null

    fun settingCallback(callback: AddImageCallback) {
        mCallback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return if (viewType == TYPE_HEADER) {
            var addImageHeaderBinding:ItemAddImageHeaderBinding= DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.item_add_image_header, parent, false)
            addImageHeaderBinding.color = ConfigPOJO.Companion
            HeaderHolder(addImageHeaderBinding.root)
        } else {
            var addImagesBinding:ItemAddImagesBinding= DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.item_add_images, parent, false)
            addImagesBinding.color = ConfigPOJO.Companion
            ViewHolder(addImagesBinding.root)
        }
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }

    }


    override fun getItemCount(): Int {
        return imageList?.size!!+1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ViewHolder) {

            holder.onBind(imageList?.get(position-1))

        }
    }


    interface AddImageCallback {
        fun addImageHeader()
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView?: View(context)) {

        init {
            itemView?.ivCancel?.setOnClickListener {
                itemView.ivCancel?.isEnabled = false
                imageList?.removeAt(adapterPosition-1)
                notifyItemRemoved(adapterPosition-1)
                notifyItemRangeChanged(adapterPosition-1, (imageList?.size ?: 0) - 1)
                itemView.ivCancel?.isEnabled = false
            }
        }

        fun onBind(imagePath: String?) = with(itemView) {
            ivImage.setImageURI(Uri.parse(imagePath))
        }
    }

    inner class HeaderHolder(itemView: View?) : RecyclerView.ViewHolder(itemView?:View(context)) {

        init {
            itemView?.ivImage?.setOnClickListener {
                mCallback?.addImageHeader()
            }
        }
    }
}