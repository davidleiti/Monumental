package ubb.thesis.david.domain

import io.reactivex.Completable
import io.reactivex.Single

interface ImageStorage {

    fun storeImage(userId: String, imageId: String, filePath: String): Completable
    fun downloadImage(userId: String, imageId: String, targetPath: String): Completable
    fun deleteImage(userId: String, imageId: String): Completable
    fun getImageUrl(userId: String, imageId: String): Single<String>

}