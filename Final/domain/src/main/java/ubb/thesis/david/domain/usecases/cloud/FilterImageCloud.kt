package ubb.thesis.david.domain.usecases.cloud

import io.reactivex.Observable
import ubb.thesis.david.domain.LandmarkDetector
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class FilterImageCloud(private val photoPath: String,
                       private val landmarkDetector: LandmarkDetector,
                       transformer: Transformer<Boolean>)
    : ObservableUseCase<Boolean>(transformer) {

    override fun createSource(): Observable<Boolean> =
        landmarkDetector.filterImageCloud(photoPath)

}