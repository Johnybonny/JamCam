package com.example.jamcam

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager

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

        // Set the desired orientation
        mediaRecorder?.setOrientationHint(90) // 90 degrees for vertical orientation

        val path = applicationContext.filesDir.path
        mediaRecorder!!.setOutputFile("${path}/original.mp4")
    }


    private fun startRecording(surfaceHolder: SurfaceHolder) {
        println("Start of recording")
        if(!isRecording){
            initialize(surfaceHolder)
            try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()
                UtilityClass.saveTimestamp(this, "camera_start.txt")
                isRecording = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        println("Stop of recording")
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

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }


    // Stop recording and remove SurfaceView
    override fun onDestroy() {
        super.onDestroy()
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


    override fun onBind(intent: Intent): IBinder? {
        return null
    }



}
