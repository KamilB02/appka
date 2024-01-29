package com.example.myapplication

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Message
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private var button: Button? = null

    private var cancellationSignal: CancellationSignal? = null
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Błąd autoryzacji: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser("Zalogowano")
                    startActivity(Intent(this@MainActivity,GridActivity::class.java))
                }
            }
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button2)

        checkBiometricSupport()
        button?.setOnClickListener {

            val biometricPrompt = BiometricPrompt.Builder(this)
                .setTitle("Autoryzacja")
                .setSubtitle("Wymagana autoryzacja odciskiem palca")
                .setDescription("Dzięki autoryzacji twoje paragony są bezpieczne :)")
                .setNegativeButton("Anuluj", this.mainExecutor, DialogInterface.OnClickListener {dialog, which ->
                    notifyUser("Autoryzacja anulowana")
                }).build()

            biometricPrompt.authenticate(checkCancellationSignal(), mainExecutor, authenticationCallback)

        }

    }

    private fun checkBiometricSupport() : Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure){
            notifyUser("Autoryzacja odciskiem nie jest dostępna w opcjach")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC)!= PackageManager.PERMISSION_GRANTED){
            notifyUser("Nie ma zezwolona na autoryzacje odciskiem w opcjach")
            return false
        }
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            true
        }else true
    }

    private fun checkCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Autoryzacja została anulowana")
        }
        return  cancellationSignal as CancellationSignal
    }
    private fun  notifyUser(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

}

