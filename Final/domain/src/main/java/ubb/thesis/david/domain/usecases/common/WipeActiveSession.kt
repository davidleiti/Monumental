package ubb.thesis.david.domain.usecases.common

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.usecases.base.CompletableUseCase

class WipeActiveSession(private val userId: String,
                        private val cloudDataSource: CloudDataSource,
                        private val sessionManager: SessionManager,
                        private val beaconManager: BeaconManager,
                        transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    override fun createSource(): Completable =
        cloudDataSource.wipeSessionBackup(userId)
                .concatWith(sessionManager.wipeSession(userId).compose(transformer))
                .doOnComplete { beaconManager.wipeBeacons(userId) }

}