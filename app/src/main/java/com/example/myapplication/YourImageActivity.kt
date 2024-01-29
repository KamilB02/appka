package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class YourImageActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var imageId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_your_image)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val captionTextView = findViewById<TextView>(R.id.textView10)
        val storeTextView = findViewById<TextView>(R.id.textView11)
        val warrantyMonthsView = findViewById<TextView>(R.id.textView12)
        val purchaseDateView = findViewById<TextView>(R.id.textView13)
        val returnPeriodView = findViewById<TextView>(R.id.textView14)
        val priceView = findViewById<TextView>(R.id.textView15)

        val deleteButton = findViewById<Button>(R.id.button4)
        val Button = findViewById<Button>(R.id.button3)

        // Pobierz dane przekazane z poprzedniej aktywności, w tym ID
        val imageURL = intent.getStringExtra("imageURL")
        val caption = intent.getStringExtra("caption")
        val store = intent.getStringExtra("store")
        val warrantyMonths = intent.getIntExtra("warrantyMonths",0)
        val purchaseDate = intent.getStringExtra("purchaseDate")
        val returnPeriod = intent.getIntExtra("returnPeriod",0)
        val price = intent.getDoubleExtra("price",0.0)
        imageId = intent.getStringExtra("id").toString()

        // Ustaw wybrane zdjęcie i podpis
        Glide.with(this).load(imageURL).into(imageView)
        captionTextView.text = caption
        storeTextView.text = store
        warrantyMonthsView.text = "$warrantyMonths miesiące/cy"
        purchaseDateView.text = purchaseDate
        returnPeriodView.text = "$returnPeriod dni"
        priceView.text = "$price zł"

        // Inicjalizacja referencji do bazy danych
        databaseReference = FirebaseDatabase.getInstance().getReference("Images")

        // Dodaj obsługę kliknięcia do przycisku usuwania
        deleteButton.setOnClickListener {
            deleteImageFromDatabase()
            val intent = Intent(this@YourImageActivity, GridActivity::class.java)
            startActivity(intent)
        }

        Button.setOnClickListener {
            val intent = Intent(this@YourImageActivity, GridActivity::class.java)
            startActivity(intent)
        }
    }

    private fun deleteImageFromDatabase() {
        // Sprawdź, czy masz dostęp do ID obrazu
        if (imageId.isNotEmpty()) {
            // Usuń obraz z bazy danych na podstawie ID
            databaseReference.child(imageId).removeValue().addOnSuccessListener {
                Toast.makeText(this@YourImageActivity, "Usunięto", Toast.LENGTH_SHORT).show()
                 // Zakończ aktywność po usunięciu obrazu
            }.addOnFailureListener {
                Toast.makeText(this@YourImageActivity, "Błąd", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@YourImageActivity, "Nie znaleziono id", Toast.LENGTH_SHORT).show()
        }
    }
}