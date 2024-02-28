package com.trava.user.ui.home.stories

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.trava.user.R
import kotlinx.android.synthetic.main.activity_stories_earned.*

class StoriesEarned : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stories_earned)

        var earn = intent.getIntExtra("kmEarned", 0)
        tvTotalEarnings.text = "$earn kilometers"

        btnEarnMore.setOnClickListener { finish() }
    }
}