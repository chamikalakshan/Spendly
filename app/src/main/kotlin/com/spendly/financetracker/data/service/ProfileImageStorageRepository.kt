package com.spendly.financetracker.data.service

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UploadedProfileImage(
    val downloadUrl: String,
    val storagePath: String
)

@Singleton
class ProfileImageStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {
    suspend fun upload(uid: String, localUri: String): Result<UploadedProfileImage> = runCatching {
        if (localUri.startsWith("http://") || localUri.startsWith("https://")) {
            return@runCatching UploadedProfileImage(downloadUrl = localUri, storagePath = "users/$uid/profile/profile.jpg")
        }
        val uri = Uri.parse(localUri)
        val contentType = context.contentResolver.getType(uri)
            ?: error("Unable to determine image type")
        require(contentType.startsWith("image/")) { "Select a valid image file" }
        val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        require(size in 1..MAX_PROFILE_IMAGE_BYTES) { "Profile image must be smaller than 5 MB" }
        val path = "users/$uid/profile/profile.jpg"
        val ref = storage.reference.child(path)
        val metadata = StorageMetadata.Builder().setContentType(contentType).build()
        ref.putFile(uri, metadata).await()
        UploadedProfileImage(downloadUrl = ref.downloadUrl.await().toString(), storagePath = path)
    }

    private companion object {
        const val MAX_PROFILE_IMAGE_BYTES = 5L * 1024L * 1024L
    }
}
