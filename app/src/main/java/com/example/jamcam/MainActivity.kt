package com.example.jamcam

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // Permissions
    private val MULTIPLE_PERMISSIONS_CODE = 200
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val SYSTEM_ALERT_WINDOW_CODE = 205
    private var permissionsGranted = false

    // Settings
    private var highlightLength = 11
    private var chosenTypes = listOf("two-pointer", "three-pointer")
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data != null) {
                    highlightLength = data.getIntExtra("highlightLength", 11)
                    chosenTypes =
                        data.getStringArrayExtra("chosenTypes")?.toList() ?: chosenTypes
                    saveSettings()
                }
            } else {
                Toast.makeText(this, "Settings not saved", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartPregame: ImageButton = findViewById(R.id.btnNewMatch)
        val btnReplays: ImageButton = findViewById(R.id.btnReplays)
        val btnSettings: ImageButton = findViewById(R.id.btnSettings)

        if (!checkPermissionForSystemAlertWindow()) return

        if (checkPermissions()) {
            grantPermissions()
        }

        readSettings()

        btnStartPregame.setOnClickListener { startPregame() }
        btnReplays.setOnClickListener { startReplays() }
        btnSettings.setOnClickListener { startSettings() }
    }

    private fun convertToString(chosenTypesList: List<String>): String {
        var playTypesString = ""
        for (playType in chosenTypesList) {
            playTypesString += "$playType,"
            println(playTypesString)
        }
        return playTypesString
    }

    private fun saveSettings() {
        UtilityClass.saveToFile(
            this, "Settings", "highlight_length.txt",
            highlightLength.toString()
        )

        UtilityClass.saveToFile(
            this, "Settings", "highlight_types.txt",
            convertToString(chosenTypes)
        )
    }

    private fun readSettings() {
        val readHighlightLength = UtilityClass.readFile(this, "Settings", "highlight_length.txt")
        if (readHighlightLength != null) {
            highlightLength = readHighlightLength.toInt() + 1
        }

        val chosenTypesString = UtilityClass.readFile(this, "Settings", "highlight_types.txt")
        if (chosenTypesString != null) {
            chosenTypes = chosenTypesString.split(",").toList()
            println(chosenTypes)
        }
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
        if (permissionsGranted) {
            val intent = Intent(this, PregameActivity::class.java)
            intent.putExtra("highlightLength", highlightLength)
            intent.putExtra("chosenTypes", chosenTypes.toTypedArray())
            startActivity(intent)
        } else {
            Toast.makeText(
                this,
                "Please re-start Open Dash Cam app and grant the requested permissions.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startReplays() {
        val intent = Intent(this, ReplaysActivity::class.java)
        startActivity(intent)
    }

    private fun startSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        resultLauncher.launch(intent)
    }

}