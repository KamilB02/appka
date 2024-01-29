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
import android.widget.GridView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GridActivity : AppCompatActivity() {

    private lateinit var iconImageView: ImageView
    private var isIcon1 = true
    var cameraFlesh = false
    var flashOn = false
    private var vibrator: Vibrator? = null
    private val YOUR_REQUEST_CODE = 123
    private val REQUEST_CODE = 123
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var isTorchOn: Boolean = false
    private lateinit var gridView: GridView
    private lateinit var dataList: ArrayList<DataClass>
    private lateinit var adapter: MyAdapter
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Images")

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == YOUR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshYourData()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gridview)

        gridView = findViewById(R.id.gridView1)
        dataList = ArrayList()
        adapter = MyAdapter(this, dataList)
        gridView.adapter = adapter
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.itemIconTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        bottomNavigationView.itemTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
        bottomNavigationView.itemRippleColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange))

        supportActionBar?.hide()

        cameraFlesh = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val dataClass: DataClass? = dataSnapshot.getValue(DataClass::class.java)
                    if (dataClass != null) {
                        dataList.add(dataClass)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędu pobierania danych
            }
        })
        gridView.setOnItemClickListener { _, _, position, _ ->
            // Obsługa kliknięcia na element siatki
            val selectedData = dataList[position]
            val intent = Intent(applicationContext, YourImageActivity::class.java)
            intent.putExtra("imageURL", selectedData.imageURL)
            intent.putExtra("caption", selectedData.caption)
            intent.putExtra("store", selectedData.store)
            intent.putExtra("warrantyMonths", selectedData.warrantyMonths)
            intent.putExtra("purchaseDate", selectedData.purchaseDate)
            intent.putExtra("returnPeriod", selectedData.returnPeriod)
            intent.putExtra("price", selectedData.price)
            intent.putExtra("id", selectedData.id)
            startActivityForResult(intent, REQUEST_CODE)
        }

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    val intentdUpload = Intent(applicationContext, UploadActivity::class.java)
                    startActivity(intentdUpload)
                    vibrate()
                    true
                }
                R.id.bottom_profile -> {
                    if(cameraFlesh){
                        if (flashOn){
                            flashOn = false
                            bottomNavigationView.menu.findItem(R.id.bottom_profile).setIcon(R.drawable.baseline_flashlight_off_24)
                            flashLightOff()

                        }
                        else{
                            flashOn = true
                            bottomNavigationView.menu.findItem(R.id.bottom_profile).setIcon(R.drawable.baseline_flashlight_on_24)
                            flashLightOn()

                        }
                        vibrate()
                    }
                    true
                }
                else -> false
            }
        }

    }
    private fun flashLightOn() {
        val cameraManager: CameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        var cameraId: String
        try{
            cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
        }catch (e:Exception){}
    }

    private fun flashLightOff() {
        val cameraManager: CameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        var cameraId: String
        try{
            cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
        }catch (e:Exception){}
    }
    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // Deprecated in API 26 (Oreo)
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }
    private fun refreshYourData() {

        dataList.clear()
        adapter.notifyDataSetChanged()
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val dataClass: DataClass? = dataSnapshot.getValue(DataClass::class.java)
                    if (dataClass != null) {
                        dataList.add(dataClass)
                    }
                }
                adapter.notifyDataSetChanged()  // Ponownie powiadom adapter o zmianie danych
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędu pobierania danych
            }
        })
    }
}