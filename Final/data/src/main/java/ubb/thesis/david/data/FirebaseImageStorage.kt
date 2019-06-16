package ubb.thesis.david.data

import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Completable
import io.reactivex.Single
import ubb.thesis.david.domain.ImageStorage

class FirebaseImageStorage : ImageStorage {

    private val reference = FirebaseStorage.getInstance().getReference("images")

    override fun storeImage(userId: String, imageId: String, filePath: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun downloadImage(userId: String, imageUrl: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteImage(userId: String, imageId: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getImageUrl(userId: String, imageId: String): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}