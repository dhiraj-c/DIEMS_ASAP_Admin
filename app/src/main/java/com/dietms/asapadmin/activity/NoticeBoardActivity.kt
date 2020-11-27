package com.dietms.asapadmin.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dietms.asapadmin.R
import com.dietms.asapadmin.adapter.NoticeBoardRecyclerAdapter
import com.dietms.asapadmin.model.Upload
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class NoticeBoardActivity : AppCompatActivity(), NoticeBoardRecyclerAdapter.OnItemClickListener {

    lateinit var recyclerNoticeBoard: RecyclerView

    lateinit var layoutManager: RecyclerView.LayoutManager

    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    lateinit var progressCircle: ProgressBar

    lateinit var recyclerAdapter: NoticeBoardRecyclerAdapter

    private lateinit var storage: FirebaseStorage

    private lateinit var databaseReference: DatabaseReference

    private lateinit var databaseListener: ValueEventListener

    private lateinit var uploads: List<Upload>

    var toolbarTitle: String? = "Notices"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_board)

        toolbar = findViewById(R.id.toolbar)
        setUpToolbar()

        recyclerNoticeBoard = findViewById(R.id.recyclerNoticeBoard)
        recyclerNoticeBoard.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)

        (layoutManager as LinearLayoutManager).reverseLayout = true
        (layoutManager as LinearLayoutManager).stackFromEnd = true

        progressCircle = findViewById(R.id.progress_circle)

//        recyclerAdapter = NoticeBoardRecyclerAdapter(this, noticeInfoList)
//
//        recyclerNoticeBoard.adapter = recyclerAdapter
//
        recyclerNoticeBoard.layoutManager = layoutManager

        uploads = arrayListOf()

        recyclerAdapter = NoticeBoardRecyclerAdapter(this@NoticeBoardActivity, uploads)

        recyclerNoticeBoard.adapter = recyclerAdapter

        recyclerAdapter.setOnItemClickListener(this@NoticeBoardActivity)

        storage = FirebaseStorage.getInstance()

        databaseReference = FirebaseDatabase.getInstance().getReference("notices")

        databaseListener = databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (uploads as ArrayList<Upload>).clear()
                for (postSnapshot in dataSnapshot.children) {
                    val upload = postSnapshot.getValue(Upload::class.java)
                    if (upload != null) {
                        upload.key = postSnapshot.key
                    }
                    (uploads as ArrayList<Upload>).add(upload!!)
                }

//                recyclerAdapter = NoticeBoardRecyclerAdapter(this@NoticeBoardActivity, uploads)
//
//                recyclerNoticeBoard.adapter = recyclerAdapter
//
//                recyclerAdapter.setOnItemClickListener(this@NoticeBoardActivity)

                recyclerAdapter.notifyDataSetChanged()

                progressCircle.visibility = View.INVISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@NoticeBoardActivity, databaseError.message, Toast.LENGTH_SHORT)
                    .show()

                progressCircle.visibility = View.INVISIBLE
            }
        })
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

    override fun onItemClick(position: Int) {
        // Do Nothing
    }

    override fun onWhateverClick(position: Int) {
//        Toast.makeText(this@NoticeBoardActivity, "Whatever Click at position", Toast.LENGTH_SHORT)
//            .show()
    }

    override fun onDeleteClick(position: Int) {
        val selectedItem = uploads[position]
        val selectedKey = selectedItem.key

        val imageReference = storage.getReferenceFromUrl(selectedItem.imageUrl)
        imageReference.delete().addOnSuccessListener {
            databaseReference.child(selectedKey).removeValue()
            Toast.makeText(this@NoticeBoardActivity, "Notice Deleted", Toast.LENGTH_SHORT).show()
        }
//        Toast.makeText(
//            this@NoticeBoardActivity,
//            "Delete Click at position" + position,
//            Toast.LENGTH_SHORT
//        )
//            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseReference.removeEventListener(databaseListener)
    }
}