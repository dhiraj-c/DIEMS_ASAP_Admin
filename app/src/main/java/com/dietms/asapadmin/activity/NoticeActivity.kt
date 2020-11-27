package com.dietms.asapadmin.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.dietms.asapadmin.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_notice.*

class NoticeActivity : AppCompatActivity() {

    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    lateinit var progressCircle: ProgressBar

    var toolbarTitle: String? = "Notices"

    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        toolbar = findViewById(R.id.toolbar)

        progressCircle = findViewById(R.id.progress_circle)

        progressCircle.visibility = View.VISIBLE

        if (intent != null) {
            toolbarTitle = intent.getStringExtra("Title")
            url = intent.getStringExtra("Image")
        }

        setUpToolbar()
        loadImage(url)
    }

    fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = toolbarTitle
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    fun loadImage(url: String) {
        Picasso.get().load(url).into(imgNoticeImageLarge)
//        progressCircle.visibility = View.INVISIBLE
    }
}