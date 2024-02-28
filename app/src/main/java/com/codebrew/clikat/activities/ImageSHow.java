package com.codebrew.clikat.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.codebrew.clikat.R;
import com.codebrew.clikat.utils.StaticFunction;

public  class ImageSHow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        setContentView(R.layout.activity_full_image);

        final ImageView sdvAds = findViewById(R.id.sdvAds);
        final ImageView icCross = findViewById(R.id.ic_cross);

        String topBanner=getIntent().getStringExtra("image");

        StaticFunction.INSTANCE.loadImage(topBanner,sdvAds,false,null,null);

        icCross.setOnClickListener(v -> finish());

    }
    @Override
    protected void onStop() {
        super.onStop();
        onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
    }



}