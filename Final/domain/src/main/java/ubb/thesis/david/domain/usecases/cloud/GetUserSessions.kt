package ubb.thesis.david.domain.usecases.cloud

import io.reactivex.Observable
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class GetUserSessions(private val userId: String,
                      private val cloudDataSource: CloudDataSource,
                      transformer: Transformer<List<Session>>) : ObservableUseCase<List<Session>>(transformer) {

    override fun createSource(): Observable<List<Session>> =
        cloudDataSource.getUserSessions(userId).toObservable()

}
