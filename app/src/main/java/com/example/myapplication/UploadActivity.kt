package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UploadActivity : AppCompatActivity() {

    private lateinit var seekBar1: SeekBar
    private lateinit var textView1: TextView
    private lateinit var seekBar2: SeekBar
    private lateinit var textView2: TextView
    private var uploadButton: Button? = null
    private var button: Button? = null
    private var uploadImage: ImageView? = null
    private var uploadCaption: EditText? = null
    private var progressBar: ProgressBar? = null
    private var imageUri: Uri? = null
    private val databaseReference = FirebaseDatabase.getInstance().getReference("Images")
    private val storageReference = FirebaseStorage.getInstance().reference
    private lateinit var editTextDate: EditText
    private val calendar = Calendar.getInstance()
    private val CHANNEL_ID = "channel_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_upload)


        uploadButton = findViewById(R.id.uploadButton)
        button = findViewById(R.id.button)
        uploadCaption = findViewById(R.id.uploadCaption)
        uploadImage = findViewById(R.id.uploadImage)
        progressBar = findViewById(R.id.progressBar)
        progressBar?.visibility = View.INVISIBLE

        uploadButton?.text = "Dodaj"  // Pusty tekst

        seekBar1 = findViewById(R.id.seekBar1)
        textView1 = findViewById(R.id.textView1)

        seekBar2 = findViewById(R.id.seekBar2)
        textView2 = findViewById(R.id.textView2)

        seekBar1.min = 0
        seekBar1.max = 59

        seekBar2.min = 0
        seekBar2.max = 100

        editTextDate = findViewById(R.id.editTextDate)
        editTextDate.isClickable = true
        editTextDate.setOnClickListener {
            showDatePickerDialog()
        }

        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateText1(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }
        })
        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateText2(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }
        })

        val activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult?> {
                override fun onActivityResult(result: ActivityResult?) {
                    if (result?.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        if (data != null) {
                            if (data.data != null) {
                                // Zdjęcie wybrane z galerii
                                imageUri = data.data
                                uploadImage?.setImageURI(imageUri)
                            } else {
                                // Zdjęcie zrobione aparatem
                                val bitmap = data.extras?.get("data") as Bitmap
                                imageUri = getImageUriFromBitmap(bitmap)
                                uploadImage?.setImageBitmap(bitmap)
                            }
                        }
                    } else {
                        Toast.makeText(this@UploadActivity, "Nie wybrano zdjęcia", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        )

        uploadImage?.setOnClickListener {
            val photoPicker = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            activityResultLauncher.launch(photoPicker)
        }

        uploadButton?.setOnClickListener {
            if (imageUri != null ) {
                uploadButton?.text = ""  // Pusty tekst
                uploadToFirebase(imageUri!!)
            } else {
                Toast.makeText(this@UploadActivity, "Brak danych lub są one nie poprawne", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        button?.setOnClickListener {
            val intent = Intent(this@UploadActivity, GridActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun uploadToFirebase(uri: Uri) {
         val warrantyMonthsSeekBar = findViewById<SeekBar>(R.id.seekBar1)
         val returnPeriodSeekBar = findViewById<SeekBar>(R.id.seekBar2)
        val purchaseDateEditText = findViewById<EditText>(R.id.editTextDate)
         val storeEditText = findViewById<EditText>(R.id.Sklep)
         val priceEditText = findViewById<EditText>(R.id.editTextNumberDecimal)
        val warrantyMonths = warrantyMonthsSeekBar.progress

        val returnPeriod = returnPeriodSeekBar.progress

        val purchaseDateStr = purchaseDateEditText.text.toString()

        val store = storeEditText.text.toString()

        val price = priceEditText.text.toString().toDouble()

        val caption = uploadCaption?.text.toString()

        val adjustedWarrantyMonths = if (warrantyMonths == 59) {
            Int.MAX_VALUE
        } else if (warrantyMonths > 36) {
            val liczba = warrantyMonths - 36
            val mnozenie = liczba * 12
            36 + mnozenie
        }else{
            warrantyMonths
        }


        val imageReference = storageReference.child(
            "${System.currentTimeMillis()}.${getFileExtension(uri)}"
        )

        imageReference.putFile(uri).addOnSuccessListener {
            imageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                val key = databaseReference.push().key
                val dataClass = DataClass(key, downloadUri.toString(), caption,adjustedWarrantyMonths,purchaseDateStr,returnPeriod,store, price)
                if (key != null) {
                    databaseReference.child(key).setValue(dataClass)
                }
                progressBar?.visibility = View.INVISIBLE

                createNotificationChannel()

                var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                builder.setSmallIcon(R.drawable.baseline_circle_notifications_24)
                    .setContentTitle("Powiadomienie")
                    .setContentText("Pomyślnie dodano nowy paragon")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                with(NotificationManagerCompat.from(this)) {

                    if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        return@addOnSuccessListener
                    }
                    notify(1, builder.build())
                }

                Toast.makeText(this@UploadActivity, "Zakończono powodzeniem !", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@UploadActivity, GridActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnProgressListener {
            progressBar?.progressTintList = ColorStateList.valueOf(android.graphics.Color.WHITE)
            progressBar?.visibility = View.VISIBLE

        }.addOnFailureListener {
            progressBar?.visibility = View.INVISIBLE
            Toast.makeText(this@UploadActivity, "Błąd", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    private fun getFileExtension(fileUri: Uri): String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri))
    }

    private fun updateText1(value: Int) {
        val displayValue = when {
            value == 0 -> {
                "brak"
            }
            value == 1 -> {
                "miesiąc"
            }
            value == 59 -> {
                "Dożywotnia"
            }
            value > 36 -> {
                "${value - 33} lat(a)"
            }
            else -> {"${value} miesięcy"}
        }
        textView1.text = displayValue
    }
    private fun updateText2(value: Int) {
        val displayValue = when {
            value == 0 -> {
                "brak"
            }
            value == 1 -> {
                "dzień"
            }
            value == 100 -> {
                "Bez ograniczeń"
            }
            else -> {"${value} dni"}
        }
        textView2.text = displayValue
    }
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // Aktualizuj pole tekstowe po wybraniu daty
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateEditTextDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    private fun updateEditTextDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        editTextDate.setText(dateFormat.format(calendar.time))
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "First channel",
            NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Test description for my channel"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }
}

