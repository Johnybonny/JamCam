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
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_MOVIES
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import kotlinx.coroutines.NonCancellable.start
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class BackgroundVideoRecorder : Service(), SurfaceHolder.Callback {

    private lateinit var windowManager: WindowManager
    private lateinit var surfaceView: SurfaceView
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private val NOTIFICATION_ID = 1234
    private val NOTIFICATION_CHANNEL_ID = "your_channel_id"

    private var isRecording = false
    private lateinit var notificationBuilder: Notification.Builder
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
            val handler = Handler()
            handler.post {
                // Start foreground service to avoid unexpected kill
                notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelId = NOTIFICATION_CHANNEL_ID
                    val channelName = "Your Channel Name"
                    val channel =
                        NotificationChannel(
                            channelId,
                            channelName,
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                    notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager.createNotificationChannel(channel)

                    Notification.Builder(this, channelId)
                } else {
                    Notification.Builder(this)
                }

                val notification = notificationBuilder
                    .setContentTitle("Background Video Recorder")
                    .setContentText("Recording started")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build()
                startForeground(NOTIFICATION_ID, notification)

                // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
                windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                surfaceView = SurfaceView(this)
                val layoutParams = WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
                )
                layoutParams.gravity = Gravity.LEFT or Gravity.TOP
                windowManager.addView(surfaceView, layoutParams)
                surfaceView.holder.addCallback(this)
        }
    }


    // Method called right after Surface created (initializing and starting MediaRecorder)
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        // Create a Timer to schedule the recording duration
        val timer = Timer()
        val recordingDuration = 10000L // 10 seconds

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isRecording) {
                    println("10 seconds passed")
                    stopRecording()
                    startRecording(surfaceHolder)
                }
            }
        }, 0, recordingDuration)

        // Start the initial recording
        startRecording(surfaceHolder)
    }

    private fun initialize(surfaceHolder: SurfaceHolder) {
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
    }


    private fun startRecording(surfaceHolder: SurfaceHolder) {
        if(!isRecording){
            initialize(surfaceHolder)

            try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()
                isRecording = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        println("stopped")
        if (isRecording) {
            try {
                mediaRecorder?.stop()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }

            isRecording = false
            println("Officially stopped")
        }
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null

//        if (isRecording) {
//            try {
//                mediaRecorder?.stop()
//            } catch (e: RuntimeException) {
//                e.printStackTrace()
//            }
//
//            mediaRecorder?.reset()
//            isRecording = false
//
//        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }


    // Stop recording and remove SurfaceView
    override fun onDestroy() {
        super.onDestroy()
//        if (isRecording) {
//            try {
//                mediaRecorder?.stop()
//            } catch (e: RuntimeException) {
//                e.printStackTrace()
//            }
//
//            isRecording = false
//        }
//        mediaRecorder?.reset()
//        mediaRecorder?.release()
//        mediaRecorder = null

        stopRecording()

        camera?.lock()
        camera?.release()
        camera = null

        windowManager.removeView(surfaceView)
        stopForeground(true)
    }


    override fun surfaceChanged(
        surfaceHolder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}


    inner class MyBinder : Binder() {
        fun getService(): BackgroundVideoRecorder {
            return this@BackgroundVideoRecorder
        }
    }


    override fun onBind(intent: Intent): IBinder? {
//        return MyBinder()
        return null
    }



}
