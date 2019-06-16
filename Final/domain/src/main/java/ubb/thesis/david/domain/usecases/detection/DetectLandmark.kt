package ubb.thesis.david.domain.usecases.detection

import io.reactivex.Observable
import ubb.thesis.david.domain.LandmarkDetector
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class DetectLandmark(private val targetLandmark: Landmark,
                     private val photoId: String,
                     private val landmarkDetector: LandmarkDetector,
                     transformer: Transformer<String>) : ObservableUseCase<String>(transformer) {

    override fun createSource(): Observable<String> =
        landmarkDetector.detectLandmark(targetLandmark, photoId)

}