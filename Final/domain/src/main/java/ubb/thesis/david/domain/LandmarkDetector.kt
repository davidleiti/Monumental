package ubb.thesis.david.domain

import io.reactivex.Observable
import ubb.thesis.david.domain.entities.Landmark

interface LandmarkDetector {

    fun filterImageOnDevice(imagePath: String): Observable<Boolean>
    fun filterImageCloud(imagePath: String): Observable<Boolean>
    fun detectLandmark(targetLandmark: Landmark, imagePath: String): Observable<String>

}