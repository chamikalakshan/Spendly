package com.spendly.financetracker.data.service

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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
        val snapshot = ref.putFile(uri, metadata).await()
        val uploadedRef = snapshot.storage
        val downloadUrl = runCatching { uploadedRef.downloadUrl.await().toString() }
            .recoverCatching { error ->
                if (error is StorageException && error.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    delay(DOWNLOAD_URL_RETRY_DELAY_MILLIS)
                    uploadedRef.downloadUrl.await().toString()
                } else {
                    throw error
                }
            }
            .getOrElse { error -> throw error.toUploadError(storage.reference.bucket, path) }
        UploadedProfileImage(downloadUrl = downloadUrl, storagePath = path)
    }

    private companion object {
        const val MAX_PROFILE_IMAGE_BYTES = 5L * 1024L * 1024L
        const val DOWNLOAD_URL_RETRY_DELAY_MILLIS = 500L
    }
}

private fun Throwable.toUploadError(bucket: String, path: String): Throwable {
    val storageError = this as? StorageException ?: return this
    val message = when (storageError.errorCode) {
        StorageException.ERROR_OBJECT_NOT_FOUND ->
            "Firebase Storage could not find the uploaded image at $path. Open Firebase Console > Storage, initialize bucket $bucket, deploy storage.rules, then try again."
        StorageException.ERROR_NOT_AUTHENTICATED ->
            "Please sign in again before uploading a profile image."
        StorageException.ERROR_NOT_AUTHORIZED ->
            "Firebase Storage denied the upload. Deploy storage.rules and confirm the signed-in user owns this profile."
        StorageException.ERROR_QUOTA_EXCEEDED ->
            "Firebase Storage quota has been exceeded."
        StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
            "Image upload timed out. Check the connection and try again."
        else -> storageError.localizedMessage ?: "Profile image upload failed."
    }
    return IllegalStateException(message, storageError)
}
