package com.example.jamcam.videorecorder

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException

class VideoEditor(private val directoryName: String, private val fileName: String) {


    fun createHighlight(context: Context, start: String, stop: String, outputName: String) {
        val ffmpeg = FFmpeg.getInstance(context)
        try {
            ffmpeg.loadBinary(object : FFmpegLoadBinaryResponseHandler {
                override fun onFinish() {
                    Log.d("FFmpeg", "onFinish")
                }

                override fun onSuccess() {
                    Log.d("FFmpeg", "onSuccess")
                    val dirPath: String = context.filesDir.path
                    val input = "${dirPath}/$fileName"
                    val output = "${dirPath}/replays/$outputName"
                    val command = arrayOf("-y", "-i", input, "-ss", start, "-to", stop, "-c", "copy", output)
                    try {
                        ffmpeg.execute(command, object : ExecuteBinaryResponseHandler() {
                            override fun onSuccess(message: String?) {
                                super.onSuccess(message)
                                Log.d(ContentValues.TAG, "onSuccess: " + message!!)
                            }

                            override fun onProgress(message: String?) {
                                super.onProgress(message)
                                Log.d(ContentValues.TAG, "onProgress: " + message!!)
                            }

                            override fun onFailure(message: String?) {
                                super.onFailure(message)
                                Log.e(ContentValues.TAG, "onFailure: " + message!!)
                            }

                            override fun onStart() {
                                super.onStart()
                                Log.d(ContentValues.TAG, "onStart")
                            }

                            override fun onFinish() {
                                super.onFinish()
                                Log.d(ContentValues.TAG, "onFinish")
                            }
                        })
                    } catch (e: FFmpegCommandAlreadyRunningException) {
                        Log.e("FFmpeg", "FFmpeg runs already")
                    }
                }

                override fun onFailure() {
                    Log.e("FFmpeg", "onFailure")
                }

                override fun onStart() {
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            Log.e("FFmpeg", "Your device does not support FFmpeg")
        }
    }


}