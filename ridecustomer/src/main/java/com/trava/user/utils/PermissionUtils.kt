package com.trava.user.utils

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import permissions.dispatcher.PermissionRequest

object PermissionUtils
{
    fun showRationalDialog(context: Context, @StringRes messageResId: Int, request: PermissionRequest)
    {
        AlertDialog.Builder(context)
                .setPositiveButton("Allow", { _, _ -> request.proceed() })
                .setNegativeButton("Deny", { _, _ -> request.cancel() })
                .setCancelable(false)
                .setMessage(messageResId)
                .show()
    }

    fun showAppSettingsDialog(context: Context, @StringRes messageResId: Int)
    {
        AlertDialog.Builder(context)
                .setPositiveButton("Settings", { _, _ ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    context.startActivity(intent)
                })
                .setNegativeButton(android.R.string.cancel, object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }

                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show()
    }
}