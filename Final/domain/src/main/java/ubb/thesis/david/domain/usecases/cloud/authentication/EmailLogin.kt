package ubb.thesis.david.domain.usecases.cloud.authentication

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.UserAuthenticator
import ubb.thesis.david.domain.usecases.base.CompletableUseCase

class EmailLogin(private val email: String,
                 private val password: String,
                 private val userAuthenticator: UserAuthenticator,
                 transformer: CompletableTransformer): CompletableUseCase(transformer) {

    override fun createSource(): Completable =
            userAuthenticator.emailAuth(email, password)

}