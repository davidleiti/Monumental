package ubb.thesis.david.domain

import io.reactivex.Single
import ubb.thesis.david.domain.entities.Landmark

interface LandmarkDetector {

    fun filterImageLocal(imagePath: String): Single<Boolean>
    fun filterImageCloud(imagePath: String): Single<Boolean>
    fun detectLandmark(targetLandmark: Landmark, imagePath: String): Single<String>

}