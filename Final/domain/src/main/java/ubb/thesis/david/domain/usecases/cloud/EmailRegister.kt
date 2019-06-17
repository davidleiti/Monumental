package ubb.thesis.david.domain.usecases.cloud

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.UserAuthenticator
import ubb.thesis.david.domain.usecases.base.CompletableUseCase

class EmailRegister(private val params: Params,
                    private val userAuthenticator: UserAuthenticator,
                    transformer: CompletableTransformer): CompletableUseCase(transformer) {

    data class Params(val email: String, val password: String)

    override fun createSource(): Completable =
            userAuthenticator.emailSignUp(params.email, params.password)


}