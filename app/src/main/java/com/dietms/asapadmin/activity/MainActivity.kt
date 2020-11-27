package com.dietms.asapadmin.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.dietms.asapadmin.R
import com.dietms.asapadmin.model.Upload
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var etNoticeTitle: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var btnSelectPdf: Button
    private lateinit var imgSelectedImage: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnPostNotice: Button

    private lateinit var txtOrigin: TextView
    private lateinit var spinnerOrigin: Spinner

    private lateinit var imageUri: Uri

    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference

    // Notification
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var uploads: List<Upload>

    private var uploadTask: StorageTask<*>? = null

    private lateinit var sharedPreferences: SharedPreferences

    lateinit var date: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etNoticeTitle = findViewById(R.id.etNoticeTitle)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSelectPdf = findViewById(R.id.btnSelectPdf)
        imgSelectedImage = findViewById(R.id.imgSelectedImage)
        progressBar = findViewById(R.id.progressBar)
        btnPostNotice = findViewById(R.id.btnPostNotice)

        txtOrigin = findViewById(R.id.txtOrigin)
        spinnerOrigin = findViewById(R.id.spinnerOrigin)

        storageReference = FirebaseStorage.getInstance().getReference("notices")
        databaseReference = FirebaseDatabase.getInstance().getReference("notices")

        // Notification Android Oreo
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                notification()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
//        databaseReference.child("notices").addValueEventListener(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//            }
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                (uploads as ArrayList<Upload>).clear()
//                for (postSnapshot in dataSnapshot.children) {
//                    val upload = postSnapshot.getValue(Upload::class.java)
//                    if (upload != null) {
//                        upload.key = postSnapshot.key
//                    }
//                    (uploads as ArrayList<Upload>).add(upload!!)
//                }
//            }
//        })

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        setUpToolbar()

        val departments = arrayListOf<String>()
        departments.add(0, "Choose Source")
        departments.add("Director")
        departments.add("HOD")
        departments.add("Students Section")
        departments.add("Accounts Section")

        val dataAdapter: ArrayAdapter<String>
        dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerOrigin.adapter = dataAdapter

        spinnerOrigin.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>, view: View?, i: Int, l: Long
            ) {
                if (adapterView.getItemAtPosition(i) == "Choose Source") {
                    // Do nothing
                } else {
                    txtOrigin.text = adapterView.selectedItem.toString()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                // Operation to perform when nothing selected
            }
        }

        btnSelectImage.setOnClickListener {
            openFileChooser()
        }

        btnSelectPdf.setOnClickListener {
            openFileChooser()
        }

        btnPostNotice.setOnClickListener {
            if (uploadTask != null && uploadTask!!.isInProgress()) {

                Toast.makeText(this@MainActivity, "Upload in progress", Toast.LENGTH_SHORT).show()

            } else {
                uploadFile()
            }
            btnPostNotice.isEnabled = false
            Handler().postDelayed({
                btnPostNotice.isEnabled = true
            }, 10000)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_drawer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.board -> {
                startActivity(Intent(this@MainActivity, NoticeBoardActivity::class.java))
                true
            }
            R.id.logOut -> {
                sharedPreferences.edit().clear().apply()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) {
            imageUri = data.data!!
            Picasso.get().load(imageUri).into(imgSelectedImage)
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile() {
        if (imageUri != null) {
            val fileReference: StorageReference = storageReference.child(
                System.currentTimeMillis()
                    .toString() + "." + getFileExtension(imageUri)
            )
            uploadTask = fileReference.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    val handler = Handler()
                    handler.postDelayed({ progressBar.progress = 0 }, 500)
                    Toast.makeText(this@MainActivity, "Notice OnBoard", Toast.LENGTH_LONG)
                        .show()
//                    val upload = Upload(
//                        etNoticeTitle.text.toString().trim(),
//                        txtOrigin.text.toString().trim(),
//                        taskSnapshot.uploadSessionUri.toString()
//                    )
//                    val uploadId: String = databaseReference.push().key.toString()
//                    databaseReference.child(uploadId).setValue(upload)
                    val urlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                    while (!urlTask.isSuccessful());
                    val downloadUrl: Uri? = urlTask.getResult()

                    //Log.d(TAG, "onSuccess: firebase download url: " + downloadUrl.toString()); //use if testing...don't need this line.

                    //Log.d(TAG, "onSuccess: firebase download url: " + downloadUrl.toString()); //use if testing...don't need this line.
                    val upload = Upload(
                        etNoticeTitle.getText().toString().trim(),
                        txtOrigin.text.toString().trim(),
                        downloadUrl.toString(),
                        date.trim()
                    )

                    val uploadId: String? = databaseReference.push().getKey()
                    if (uploadId != null) {
                        databaseReference.child(uploadId).setValue(upload)
                    }
                    etNoticeTitle.text.clear()
                    txtOrigin.text = ""
                    imgSelectedImage.setImageResource(android.R.color.transparent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@MainActivity,
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressBar.progress = progress.toInt()
                }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Notification Android Oreo
    @RequiresApi(Build.VERSION_CODES.O)
    private fun notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("n", "n", NotificationManager.IMPORTANCE_DEFAULT)
            val manager: NotificationManager = getSystemService<NotificationManager>(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
        val builder: Notification.Builder = Notification.Builder(this, "n")
            .setContentTitle("DIEMS ASAP")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentText("New Notice")
        val managerCompat: NotificationManagerCompat = NotificationManagerCompat.from(this)
        managerCompat.notify(999, builder.build())

        // Notification Intent
//        val contentIntent: PendingIntent = PendingIntent.getActivity(
//            this, 0,
//            Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        builder.setContentIntent(contentIntent)
    }
}