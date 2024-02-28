package com.trava.user.ui.home.chatModule

import android.annotation.SuppressLint
import android.app.Dialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.View
import android.widget.TextView
import com.trava.user.R

class BottomSheetMedia : BottomSheetDialogFragment() {

    private var cameraListener: View.OnClickListener? = null
    private var galleryListener: View.OnClickListener? = null
    private var cancelListener: View.OnClickListener? = null

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(activity, R.layout.fragment_bottom_sheet_media, null)
        dialog.setContentView(contentView)
        contentView.findViewById<TextView>(R.id.tvCamera).setOnClickListener(cameraListener)
        contentView.findViewById<TextView>(R.id.tvGallery).setOnClickListener(galleryListener)
        contentView.findViewById<TextView>(R.id.tvCancel).setOnClickListener(cancelListener)

    }

    fun setOnCameraListener(onClickListener: View.OnClickListener) {
        this.cameraListener = onClickListener
    }

    fun setOnGalleryListener(onClickListener: View.OnClickListener) {
        this.galleryListener = onClickListener
    }

    fun setOnCancelListener(onClickListener: View.OnClickListener) {
        this.cancelListener = onClickListener
    }
}