package com.example.jamcam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val MULTIPLE_PERMISSIONS_CODE = 200

    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val SYSTEM_ALERT_WINDOW_CODE = 205

    private var permissionsGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startPregameButton: ImageButton = findViewById(R.id.btnNewMatch)

        if (!checkPermissionForSystemAlertWindow()) return

        if (checkPermissions()) {
            grantPermissions()
        }

        startPregameButton.setOnClickListener { startPregame() }
    }

    private fun grantPermissions() {
        permissionsGranted = true
    }

    private fun checkPermissions(): Boolean {
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            val result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS_CODE
            )
            return false
        }
        return true
    }

    private fun checkPermissionForSystemAlertWindow(): Boolean {
        if (!isPermissionForSystemAlertWindowGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, SYSTEM_ALERT_WINDOW_CODE)
            }
            return false
        }
        return true
    }

    private fun isPermissionForSystemAlertWindowGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted
                    grantPermissions()
                } else {
                    // permissions not granted
                    Toast.makeText(
                        this,
                        "Permissions denied. The app cannot start.",
                        Toast.LENGTH_LONG
                    ).show()
                    Toast.makeText(
                        this,
                        "Please re-start Open Dash Cam app and grant the requested permissions.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                return
            }
        }
    }

    private fun startPregame() {
        if(permissionsGranted){
            val intent = Intent(this, PregameActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(
                this,
                "Please re-start Open Dash Cam app and grant the requested permissions.",
                Toast.LENGTH_LONG
            ).show()
        }

    }
}