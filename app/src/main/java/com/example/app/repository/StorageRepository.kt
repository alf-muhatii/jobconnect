package com.example.app.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class StorageRepository(private val context: Context) {

    fun initCloudinary(cloudName: String, apiKey: String) {
        try {
            MediaManager.init(context, mapOf(
                "cloud_name" to cloudName,
                "api_key" to apiKey
            ))
        } catch (e: Exception) {
            // Already initialized
        }
    }

    suspend fun uploadProfilePicture(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .unsigned("kazikenya_preset")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<out Any?, Any?>?) {
                    val url = resultData?.get("secure_url")?.toString() ?: ""
                    continuation.resume(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    android.util.Log.e("Cloudinary", "Upload error: ${error?.description} code: ${error?.code}")
                    continuation.resumeWithException(Exception(error?.description ?: "Unknown Cloudinary Error"))
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}
