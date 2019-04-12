package ubb.thesis.david.monumental.domain.usecases

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.monumental.domain.BeaconManager
import ubb.thesis.david.monumental.domain.SessionManager
import ubb.thesis.david.monumental.domain.usecases.base.CompletableUseCase

class WipeSession(private val userId: String,
                  private val sessionManager: SessionManager,
                  private val beaconManager: BeaconManager,
                  transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    override fun createSource(): Completable =
        sessionManager.wipeSession(userId)
                .doOnComplete { beaconManager.removeBeacons(userId) }

}