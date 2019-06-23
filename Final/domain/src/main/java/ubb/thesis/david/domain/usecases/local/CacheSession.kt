package ubb.thesis.david.domain.usecases.local

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.CompletableUseCase

class CacheSession(private val backup: Backup,
                   private val sessionManager: SessionManager,
                   private val beaconManager: BeaconManager,
                   transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    override fun createSource(): Completable =
        sessionManager.wipeSession(backup.session.userId)
                .andThen(sessionManager.saveSessionBackup(backup))
                .doOnComplete { setupGeofences() }

    private fun setupGeofences() {
        for (landmark in backup.landmarks.keys) {
            beaconManager.setupBeacon(landmark.id, landmark.lat, landmark.lng, backup.session.userId)
        }
    }

}