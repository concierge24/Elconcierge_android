package com.trava.utilities;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;


/**
 * Created by Rohit Sharma on 19/12/16.
 */
public class AlertDialogUtil {

    private static AlertDialogUtil mInstance;

    public static AlertDialogUtil getInstance() {
        if (null == mInstance) {
            mInstance = new AlertDialogUtil();
        }
        return mInstance;
    }

    public AlertDialog createOkCancelDialog(final Context context, @StringRes int titleResourceId, @StringRes int messageResourceId,
                                            @StringRes int positiveResourceId, @StringRes int negativeResourceId, boolean cancelable,
                                            final OnOkCancelDialogListener listener) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        if (titleResourceId != 0) {
            alertDialog.setTitle(titleResourceId);
        }
        if (messageResourceId != 0) {
            alertDialog.setMessage(messageResourceId);
        }
        alertDialog.setCancelable(cancelable);

        if (positiveResourceId!=0)
        {
            alertDialog.setPositiveButton(positiveResourceId,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (listener != null)
                                listener.onOkButtonClicked();
                            dialog.dismiss();
                        }
                    });
        }
        if (negativeResourceId != 0) {
            alertDialog.setNegativeButton(negativeResourceId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null)
                        listener.onCancelButtonClicked();
                    dialog.dismiss();
                }
            });
        }
        final AlertDialog dialog = alertDialog.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat
                        .getColor(context, R.color.colorPrimary));
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat
                        .getColor(context, R.color.text_dark));
            }
        });
        return dialog;
    }



    public interface OnOkCancelDialogListener {
        void onOkButtonClicked();

        void onCancelButtonClicked();
    }
}