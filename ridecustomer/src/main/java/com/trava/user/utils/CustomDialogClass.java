package com.trava.user.utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.trava.user.R;

public class CustomDialogClass extends Dialog implements
        View.OnClickListener {

    public Activity c;
    public Dialog d;
    public TextView tvOk;

    public CustomDialogClass(Activity a, int style) {
        super(a, style);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        tvOk = (TextView) findViewById(R.id.tvOk);
        tvOk.setOnClickListener(this);
//    no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
/*            case R.id.tvOk:
                dismiss();
                break;*/
//    case R.id.btn_no:
//      dismiss();
//      break;
            default:
                break;
        }
        dismiss();
    }
}