package ubb.thesis.david.domain

import io.reactivex.Completable
import io.reactivex.Single

interface ImageStorage {

    fun storeImage(filePath: String): Single<String>
    fun getURL(id: String): Single<String>
    fun downloadImage(imageUrl: String): Completable
    fun deleteImage(id: String): Completable

}