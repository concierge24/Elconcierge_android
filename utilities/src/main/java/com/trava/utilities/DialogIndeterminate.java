package com.trava.utilities;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;


/**
 * Created by ER.Rohit Sharma on 13-Dec-16.
 */
public class DialogIndeterminate {

    private Dialog mDialog;
    View dialogView;

    public DialogIndeterminate(Context context) {
        LayoutInflater li = LayoutInflater.from(context);
        dialogView = li.inflate(R.layout.progress_wheel, null, false);
        mDialog = new Dialog(context, R.style.NewDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        ImageView imageView = dialogView.findViewById(R.id.loader);
//        Glide.with(context.getApplicationContext())
//                .asGif()
//                .load(R.raw.udown_loader)
//                .into(imageView);
    }

    public void setProgressColor(int color){
        ((SpinKitView)dialogView.findViewById(R.id.progressbar)).setColor(color);
    }

    public void showText(String text) {
        ((TextView) dialogView.findViewById(R.id.textView)).setText(text);
    }

    public void show(boolean show){
        if (show){
           show();
        }else{
            dismiss();
        }
    }

    public void show() {
        try {
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismiss() {
        try {
            mDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}