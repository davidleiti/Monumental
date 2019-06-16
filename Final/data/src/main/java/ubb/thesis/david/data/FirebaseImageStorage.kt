package ubb.thesis.david.data

import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Completable
import io.reactivex.Single
import ubb.thesis.david.domain.ImageStorage

class FirebaseImageStorage : ImageStorage {

    private val storage = FirebaseStorage.getInstance()

    override fun storeImage(filePath: String): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getURL(id: String): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun downloadImage(imageUrl: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteImage(id: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}