package ubb.thesis.david.monumental.domain.usecases

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.monumental.domain.BeaconManager
import ubb.thesis.david.monumental.domain.SessionManager
import ubb.thesis.david.monumental.domain.entities.Landmark
import ubb.thesis.david.monumental.domain.usecases.base.CompletableUseCase

class CreateSession(private val params: RequestValues,
                    private val sessionManager: SessionManager,
                    private val beaconManager: BeaconManager,
                    transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    data class RequestValues(val userId: String, val landmarks: List<Landmark>)

    override fun createSource(): Completable =
        sessionManager.setupSession(params.userId, params.landmarks)
                .doOnComplete { setupGeofences() }

    private fun setupGeofences() {
        for (landmark in  params.landmarks) {
            beaconManager.setupBeacon(landmark.id, landmark.lat, landmark.lng, params.userId)
        }
    }
}