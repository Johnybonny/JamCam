package com.example.jamcam

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Camera
import android.text.format.DateFormat
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_MOVIES
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class BackgroundVideoRecorder : Service(), SurfaceHolder.Callback {

    private lateinit var windowManager: WindowManager
    private lateinit var surfaceView: SurfaceView
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null

    private var isRecording = false
    private lateinit var notificationBuilder: Notification.Builder

    override fun onCreate() {
        // Start foreground service to avoid unexpected kill
        println("START")

        notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "your_channel_id"
            val channelName = "Your Channel Name"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }

        val notification = notificationBuilder
            .setContentTitle("Background Video Recorder")
            .setContentText("")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(1234, notification)

        // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        surfaceView = SurfaceView(this)
        val layoutParams = WindowManager.LayoutParams(
            1, 1,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        windowManager.addView(surfaceView, layoutParams)
        surfaceView.holder.addCallback(this)
    }

    // Method called right after Surface created (initializing and starting MediaRecorder)
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        camera = Camera.open()
        mediaRecorder = MediaRecorder()
        camera?.unlock()

        mediaRecorder?.setPreviewDisplay(surfaceHolder.surface)
        mediaRecorder?.setCamera(camera)
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder?.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))

        val outputFilePath = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES)}/" +
                "${DateFormat.format("yyyy-MM-dd_kk-mm-ss", Date().time)}.mp4"
        mediaRecorder?.setOutputFile(outputFilePath)

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        try {
//            mediaRecorder?.prepare()
//            mediaRecorder?.start()
//            val t = Timer()
//            println("STARTED")
//            t.schedule(object : TimerTask() {
//                override fun run() {
//                    println("STOPPPP!!!!!!")
//                    stopSelf()
//                }
//            }, 5000)
//        } catch (e: Exception) {
//            println("EXCEPTION:")
//            e.printStackTrace()
//        }

    }

    // Stop recording and remove SurfaceView
    override fun onDestroy() {
        println("DESTROYED")
        if (isRecording) {
            try {
                mediaRecorder?.stop()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }

            isRecording = false
        }
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null

        camera?.lock()
        camera?.release()
        camera = null

        windowManager.removeView(surfaceView)
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


}
