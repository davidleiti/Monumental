package ubb.thesis.david.domain.usecases.common

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.domain.usecases.base.CompletableUseCase
import java.util.*

class CreateSession(private val params: RequestValues,
                    private val cloudDataSource: CloudDataSource,
                    private val sessionManager: SessionManager,
                    private val beaconManager: BeaconManager,
                    transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    data class RequestValues(val userId: String, val landmarks: List<Landmark>)

    override fun createSource(): Completable {
        val sessionEntity = Session(userId = params.userId,
                                    landmarkCount = params.landmarks.size,
                                    timeStarted = Date())

        val cloudSetup = cloudDataSource.createSession(sessionEntity, params.landmarks)
        val localSetup = sessionManager.createSession(sessionEntity, params.landmarks)

        return cloudSetup.flatMapCompletable { id ->
            sessionEntity.sessionId = id
            localSetup.compose(transformer)
        }.doOnComplete { setupGeofences() }
    }

    private fun setupGeofences() {
        for (landmark in params.landmarks) {
            beaconManager.setupBeacon(landmark.id, landmark.lat, landmark.lng, params.userId)
        }
    }

}